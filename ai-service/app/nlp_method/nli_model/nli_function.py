from transformers import pipeline
import torch
from typing import Dict, List

DEVICE = 0 if torch.cuda.is_available() else -1

nli_model = pipeline(
    "zero-shot-classification",
    model="joeddav/xlm-roberta-large-xnli",
    device=DEVICE
)

def _compose_text(element: Dict[str, str]) -> str:
    designation = element.get("designation", "").strip()
    definition = element.get("definition", "").strip()

    if designation and definition:
        return f"{designation} - {definition}"

    return designation or definition


def classify_relation_batch(
        reference: Dict[str, str],
        candidates: List[Dict[str, str]],
        nli_model,
        batch_size: int = 16,
        threshold: float = 0.20,
        margin: float = 0.10,
):

    ref_text = _compose_text(reference)

    results = []

    candidate_labels = [
        f"allgemeiner als {ref_text}",
        f"spezifischer als {ref_text}",
        f"nicht verwandt mit {ref_text}"
    ]

    hypothesis_template = "Dieses Datenelement ist {}."

    for i in range(0, len(candidates), batch_size):

        batch_candidates = candidates[i:i + batch_size]

        sequences = [
            _compose_text(candidate)
            for candidate in batch_candidates
        ]

        outputs = nli_model(
            sequences,
            candidate_labels,
            hypothesis_template=hypothesis_template,
            multi_label=False
        )

        # when batch size == 1 transformers returns dict instead of list
        if isinstance(outputs, dict):
            outputs = [outputs]

        for candidate, output in zip(batch_candidates, outputs):

            labels = output["labels"]
            scores = output["scores"]

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

            if (
                    wider_score >= threshold
                    and wider_score - specific_score >= margin
            ):
                relation = "wider"
                confidence = wider_score

            elif (
                    specific_score >= threshold
                    and specific_score - wider_score >= margin
            ):
                relation = "specific"
                confidence = specific_score

            else:
                relation = "unknown"
                confidence = max(
                    wider_score,
                    specific_score
                )

            results.append({
                "designation": candidate.get("designation", ""),

                "nli_relation": relation,
                "nli_confidence": round(float(confidence), 4),

                "wider_score": round(float(wider_score), 4),
                "specific_score": round(float(specific_score), 4),
                "unrelated_score": round(float(unrelated_score), 4),
            })

    return results