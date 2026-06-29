"""
Natural Language Inference (NLI) Relation Classifier

This module uses a multilingual Zero-Shot Classification model to determine
the semantic relationship between a reference term and a set of candidate terms.

The classifier evaluates whether a candidate is:
- Wider (more general) than the reference.
- Specific (more specific) than the reference.
- Unrelated to the reference.

The model is based on XLM-RoBERTa and supports multilingual input,
making it suitable for semantic taxonomy and hierarchy detection tasks.

Author: Gabriel Nathanael da Gomez
Email: gabrieldagomez@gmail.com
"""

from transformers import pipeline
import torch
from typing import Dict, List

# Use GPU when available; otherwise fall back to CPU.
DEVICE = 0 if torch.cuda.is_available() else -1

# Load the multilingual XNLI model for zero-shot relation classification.
# The model evaluates semantic relationships between reference and candidate texts.
nli_model = pipeline(
    "zero-shot-classification",
    model="joeddav/xlm-roberta-large-xnli",
    device=DEVICE
)

def _compose_text(element: Dict[str, str]) -> str:
    """
       Build a readable text representation from a designation-definition pair.

       If both fields are present, they are combined using a separator.
       Otherwise, the available field is returned.

       Args:
           element (Dict[str, str]):
               Dictionary containing designation and definition fields.

       Returns:
           str:
               Combined text used as model input.
   """
    designation = element.get("designation", "").strip()
    definition = element.get("definition", "").strip()

    # Combine designation and definition when both are available.
    if designation and definition:
        return f"{designation} - {definition}"

    # Otherwise return whichever field is populated.
    return designation or definition


def classify_relation_batch(
        reference: Dict[str, str],
        candidates: List[Dict[str, str]],
        nli_model,
        batch_size: int = 16,
        threshold: float = 0.20,
        margin: float = 0.10,
):
    """
       Classify semantic relationships between a reference item and multiple
       candidate items using an NLI model.

       The classifier evaluates whether each candidate is:
       - Wider (more general than the reference)
       - Specific (more specific than the reference)
       - Unrelated to the reference

       Classification decisions are based on:
       - Minimum confidence threshold
       - Score margin between competing relation labels

       Args:
           reference (Dict[str, str]):
               Reference designation and definition.

           candidates (List[Dict[str, str]]):
               Candidate items to compare against the reference.

           nli_model:
               Loaded Hugging Face zero-shot classification pipeline.

           batch_size (int):
               Number of candidates processed in a single inference batch.

           threshold (float):
               Minimum confidence score required to assign a relation.

           margin (float):
               Minimum score difference required between wider and specific
               predictions to avoid ambiguous classifications.

       Returns:
           List[Dict]:
               Classification results containing relation labels,
               confidence scores, and individual label probabilities.
       """

    # Build a text representation of the reference item.
    ref_text = _compose_text(reference)

    results = []

    # Candidate relation labels evaluated by the NLI model.
    candidate_labels = [
        f"allgemeiner als {ref_text}",
        f"spezifischer als {ref_text}",
        f"nicht verwandt mit {ref_text}"
    ]

    # Template used to generate NLI hypotheses.
    hypothesis_template = "Dieses Datenelement ist {}."

    # Process candidates in batches to improve inference performance.
    for i in range(0, len(candidates), batch_size):

        batch_candidates = candidates[i:i + batch_size]

        # Convert candidate objects into plain text sequences.
        sequences = [
            _compose_text(candidate)
            for candidate in batch_candidates
        ]

        # Run zero-shot classification.
        outputs = nli_model(
            sequences,
            candidate_labels,
            hypothesis_template=hypothesis_template,
            multi_label=False
        )

        # Transformers returns a dictionary instead of a list
        # when only one sequence is processed.
        if isinstance(outputs, dict):
            outputs = [outputs]

        # Process prediction results for each candidate.
        for candidate, output in zip(batch_candidates, outputs):

            labels = output["labels"]
            scores = output["scores"]

            # Create label-to-score mapping for easier lookup.
            label_scores = dict(zip(labels, scores))

            wider_score = label_scores.get(
                f"allgemeiner als {ref_text}",
                0.0
            )

            specific_score = label_scores.get(
                f"spezifischer als {ref_text}",
                0.0
            )

            unrelated_score = label_scores.get(
                f"nicht verwandt mit {ref_text}",
                0.0
            )

            # Candidate is more general than the reference.
            if (
                    wider_score >= threshold
                    and wider_score - specific_score >= margin
            ):
                relation = "wider"
                confidence = wider_score

            # Candidate is more specific than the reference.
            elif (
                    specific_score >= threshold
                    and specific_score - wider_score >= margin
            ):
                relation = "specific"
                confidence = specific_score

            # Classification is uncertain or ambiguous.
            else:
                relation = "unknown"
                confidence = max(
                    wider_score,
                    specific_score
                )

            # Store classification result.
            results.append({
                "designation": candidate.get("designation", ""),

                "nli_relation": relation,
                "nli_confidence": round(float(confidence), 4),

                "wider_score": round(float(wider_score), 4),
                "specific_score": round(float(specific_score), 4),
                "unrelated_score": round(float(unrelated_score), 4),
            })

    return results