"""
NLP Relation Scoring Engine

This module implements the core business logic for semantic relation analysis
between reference and candidate designations/definitions.

The process consists of two stages:

1. SBERT Similarity Scoring
   - Generates embeddings for designations, definitions, and combined text.
   - Computes cosine similarity scores using Sentence-BERT.
   - Produces a weighted similarity score used for candidate ranking.

2. NLI Relation Classification
   - Selects the most relevant candidates based on similarity thresholds
     and Top-K ranking.
   - Uses a Natural Language Inference (NLI) model to classify semantic
     relationships between reference and candidate texts.
   - Identifies whether a candidate is broader, more specific, related,
     or unrelated to the reference term.

Key Features:
- One-to-many relation analysis.
- Many-to-many relation analysis.
- Top-K candidate filtering.
- Similarity-based candidate selection.
- SBERT embedding generation and cosine similarity scoring.
- NLI-based semantic relation classification.

This module serves as the primary processing layer used by the FastAPI
endpoints exposed by the NLP Relation API.

Author: Gabriel Nathanael da Gomez
Email: gabrieldagomez@gmail.com
"""

from app.nlp_method.sbert_model.emb_main import embed_sbert, sbert_model
from app.nlp_method.nli_model import nli_function
from fastapi.concurrency import run_in_threadpool

# Minimum SBERT similarity score required for a candidate to be selected
# for NLI evaluation, unless it is already included in the Top-N results.
SBERT_THRESHOLD = 0.6
# Number of highest-ranked candidates that should always be considered
# for NLI evaluation, even if their similarity score is below the threshold.
TOP_N = 3


def apply_top_k_relation_filter(results, top_n=TOP_N):
    """
       Sort relation results by similarity score and clean up NLI relation labels.

       This function ensures that:
       - Results are ordered from highest to lowest similarity.
       - Items that were not evaluated by NLI are marked as "not evaluated".
       - Items outside the Top-K with weak NLI confidence are marked as "unrelated".

       Args:
           results (list[dict]):
               List of relation result dictionaries containing similarity and NLI fields.

           top_n (int):
               Number of top similarity results to keep as priority candidates.

       Returns:
           list[dict]:
               Sorted and post-processed relation results.
   """
    # Sort results by similarity score in descending order.
    sorted_results = sorted(
        results,
        key=lambda x: x["similarity_score"],
        reverse=True
    )

    for idx, item in enumerate(sorted_results):
        # Check whether the current item is outside the Top-K results.
        outside_top_k = idx >= top_n
        # Check if NLI was executed and returned a strong confidence score.
        strong_nli = (
                item.get("nli_confidence") is not None
                and item["nli_confidence"] >= 0.6
        )
        # Check if NLI was not executed for this item.
        nli_not_executed = (
                item.get("nli_confidence") is None
        )

        # Mark items that were not processed by the NLI model.
        if nli_not_executed:
            item["nli_relation"] = "not evaluated"

        # Mark low-confidence items outside Top-K as unrelated.
        elif outside_top_k and not strong_nli:
            item["nli_relation"] = "unrelated"

    return sorted_results


def select_nli_candidates(rows):
    """
      Select candidates that should be evaluated by the NLI model.

      Candidates are selected when:
      - Their SBERT similarity score is greater than or equal to SBERT_THRESHOLD.
      - OR they are included in the Top-N highest similarity results.

      This reduces unnecessary NLI processing while still keeping the most
      relevant candidates.

      Args:
          rows (list[dict]):
              Similarity result rows produced by the cosine_similarity function.

      Returns:
          list[tuple[int, dict]]:
              A list of selected candidates as tuples containing:
              - Original index of the candidate
              - Candidate similarity result dictionary
  """

    # Rank candidates by similarity score in descending order.
    ranked = sorted(
        list(enumerate(rows)),
        key=lambda x: x[1]["score"],
        reverse=True
    )
    selected = []
    for rank, (idx, r) in enumerate(ranked, start=1):
        # Select candidates that pass the threshold or are in the Top-N.
        if r["score"] >= SBERT_THRESHOLD or rank <= TOP_N:
            selected.append((idx, r))

    return selected

def cosine_similarity(data, text1_list):
    """
       Calculate SBERT-based cosine similarity between text1 items and text2 items.

       This function compares each reference item in text1_list against every
       candidate item in data.text2_list.

       It calculates similarity using three text representations:
       - Designation only
       - Definition only
       - Combined designation and definition

       The final similarity score is calculated using the strongest score among:
       - Designation similarity
       - Joint text similarity
       - Boosted designation score using definition similarity

       Args:
           data:
               Request data containing text2_list and related text fields.

           text1_list (list):
               List of reference text objects to compare against text2_list.

       Returns:
           dict:
               Dictionary where each key represents a reference text and each value
               is a list of similarity results against all candidate texts.
   """
    results = {}

    # Extract designation and definition values from candidate texts.
    text2_designations = [item.designation or "" for item in data.text2_list]
    text2_definitions = [item.definition or "" for item in data.text2_list]

    # Combine designation and definition for joint similarity comparison.
    text2_joint = [
        f"{d} {df}".strip()
        for d, df in zip(text2_designations, text2_definitions)
    ]

    # Generate SBERT embeddings for all candidate text fields.
    emb2_designations = embed_sbert(text2_designations)
    emb2_definitions = embed_sbert(text2_definitions)
    emb2_joint = embed_sbert(text2_joint)

    # Compare each reference text against all candidate texts.
    for text1_item in text1_list:
        designation1 = text1_item.designation or ""
        definition1 = text1_item.definition or ""

        # Create readable key for this reference item.
        text1_key = f"{designation1} - {definition1}"

        # Combine designation and definition for joint embedding.
        joint1 = f"{designation1} {definition1}".strip()

        # Generate SBERT embeddings for the reference text.
        emb1_designation = embed_sbert([designation1])
        emb1_definition = embed_sbert([definition1])
        emb1_joint = embed_sbert([joint1])

        # Calculate cosine similarity against all candidates.
        sim_designation = sbert_model.similarity(emb1_designation, emb2_designations)
        sim_definition = sbert_model.similarity(emb1_definition, emb2_definitions)
        sim_joint = sbert_model.similarity(emb1_joint, emb2_joint)

        results[text1_key] = []

        for idx in range(len(text2_designations)):
            # Convert tensor similarity scores into float values.
            s_label = float(sim_designation[0][idx])
            s_def = float(sim_definition[0][idx])
            s_joint = float(sim_joint[0][idx])

            # Add a small definition-based boost to the designation score.
            boost_score = s_label + 0.15 * s_def

            # Use the strongest available similarity signal.
            final_score = max(
                s_label,
                s_joint,
                boost_score
            )

            # Ensure final score does not exceed 1.0.
            final_score = min(1.0, final_score)

            # Store similarity details for this candidate.
            results[text1_key].append({
                "text2": f"{text2_designations[idx]} - {text2_definitions[idx]}",
                "score": round(final_score, 4),
                "designation_score": round(s_label, 4),
                "definition_score": round(s_def, 4),
                "joint_score": round(s_joint, 4)
            })

    return results


async def nlp_relation_score_one_to_many(data):
    """
        Calculate NLP relation scores for one reference text against many candidates.

        Workflow:
        1. Calculate SBERT similarity scores.
        2. Build the initial result list.
        3. Select candidates for NLI evaluation.
        4. Run NLI classification on selected candidates.
        5. Merge NLI results back into the final output.
        6. Sort and filter the final relation results.

        Args:
            data:
                Request data containing:
                - text1 as the reference item
                - text2_list as candidate items

        Returns:
            dict:
                Dictionary containing final relation scoring results.
    """
    # Calculate similarity between the reference text and all candidates.
    cosine_results = cosine_similarity(data, [data.text1])
    # Build the key used to retrieve similarity rows for the reference text.
    text1_str = f"{data.text1.designation} - {data.text1.definition}"
    rows = cosine_results.get(text1_str, [])

    final_results = []

    # Create base result objects with similarity scores.
    for idx, r in enumerate(rows):
        text2 = data.text2_list[idx]

        final_results.append({
            "reference_designation": data.text1.designation,
            "designation": text2.designation,
            "definition": text2.definition or "",
            "similarity_score": r["score"],
            "nli_relation": None,
            "nli_confidence": None,
            "wider_score": None,
            "specific_score": None
        })

    # Select candidates that are worth evaluating with the NLI model.
    selected_candidates = select_nli_candidates(rows)

    filtered_candidates = [
        data.text2_list[idx]
        for idx, r in selected_candidates
    ]

    if filtered_candidates:
        # Convert the reference text into the format expected by the NLI model.
        ref_dict = {
            "designation": data.text1.designation,
            "definition": data.text1.definition or ""
        }

        # Convert selected candidates into the format expected by the NLI model.
        candidate_dicts = [
            {"designation": c.designation, "definition": c.definition or ""}
            for c in filtered_candidates
        ]

        # Run NLI classification in a thread pool to avoid blocking the event loop.
        results = await run_in_threadpool(
            nli_function.classify_relation_batch,
            ref_dict,
            candidate_dicts,
            nli_function.nli_model
        )

        # Create a lookup dictionary for faster result matching.
        nli_by_designation = {
            r["designation"]: r
            for r in results
        }

        # Merge NLI results into the final output.
        for item in final_results:
            nli = nli_by_designation.get(item["designation"])
            if nli:
                item["nli_relation"] = nli["nli_relation"]
                item["nli_confidence"] = nli["nli_confidence"]
                item["wider_score"] = nli["wider_score"]
                item["specific_score"] = nli["specific_score"]

    # Sort results and apply final relation filtering rules.
    final_results = apply_top_k_relation_filter(final_results, TOP_N)

    return {"results": final_results}


async def nlp_relation_score_many_to_many(data):
    """
        Calculate NLP relation scores for many reference texts against many candidates.

        This function applies the same workflow as one-to-many scoring, but repeats
        the process for every item in data.text1_list.

        Workflow for each reference item:
        1. Retrieve SBERT similarity results.
        2. Build base result objects.
        3. Select candidates for NLI evaluation.
        4. Run NLI classification.
        5. Merge NLI output with similarity results.
        6. Apply Top-K relation filtering.
        7. Add processed results to the final response.

        Args:
            data:
                Request data containing:
                - text1_list as reference items
                - text2_list as candidate items

        Returns:
            dict:
                Dictionary containing relation scoring results for all references.
    """
    # Calculate similarity between all reference texts and all candidates.
    cosine_results = cosine_similarity(data, data.text1_list)
    final_results = []

    # Process each reference text independently.
    for text1_item in data.text1_list:
        text1_str = f"{text1_item.designation} - {text1_item.definition}"
        rows = cosine_results.get(text1_str, [])

        base_results = []

        # Create base result objects with similarity scores.
        for idx, r in enumerate(rows):
            text2 = data.text2_list[idx]

            base_results.append({
                "reference_designation": text1_item.designation,
                "designation": text2.designation,
                "definition": text2.definition or "",
                "similarity_score": r["score"],
                "nli_relation": None,
                "nli_confidence": None,
                "wider_score": None,
                "specific_score": None
            })

        # Select candidates that should be evaluated by the NLI model.
        selected_candidates = select_nli_candidates(rows)

        filtered_candidates = [
            data.text2_list[idx]
            for idx, r in selected_candidates
        ]

        if filtered_candidates:
            # Convert the current reference text into the NLI input format.
            ref_dict = {
                "designation": text1_item.designation,
                "definition": text1_item.definition or ""
            }
            # Convert selected candidates into the NLI input format.
            candidate_dicts = [
                {"designation": c.designation, "definition": c.definition or ""}
                for c in filtered_candidates
            ]

            # Run NLI classification in a thread pool.
            results = await run_in_threadpool(
                nli_function.classify_relation_batch,
                ref_dict,
                candidate_dicts,
                nli_function.nli_model
            )

            # Create a lookup dictionary for fast NLI result access.
            nli_by_designation = {
                r["designation"]: r
                for r in results
            }

            # Merge NLI results into the base result objects.
            for item in base_results:
                nli = nli_by_designation.get(item["designation"])
                if nli:
                    item["nli_relation"] = nli["nli_relation"]
                    item["nli_confidence"] = nli["nli_confidence"]
                    item["wider_score"] = nli["wider_score"]
                    item["specific_score"] = nli["specific_score"]

        # Sort and refine results for the current reference text.
        base_results = apply_top_k_relation_filter(base_results, TOP_N)
        # Add processed results to the final response list.
        final_results.extend(base_results)

    return {"results": final_results}