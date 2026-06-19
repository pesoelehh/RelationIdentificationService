from app.nlp_method.sbert_model.emb_main import embed_sbert, sbert_model
from app.nlp_method.nli_model import nli_function
from fastapi.concurrency import run_in_threadpool


SBERT_THRESHOLD = 0.6
TOP_N = 3


def apply_top_k_relation_filter(results, top_n=TOP_N):

    sorted_results = sorted(
        results,
        key=lambda x: x["similarity_score"],
        reverse=True
    )

    for idx, item in enumerate(sorted_results):

        outside_top_k = idx >= top_n

        strong_nli = (
                item.get("nli_confidence") is not None
                and item["nli_confidence"] >= 0.6
        )

        nli_not_executed = (
                item.get("nli_confidence") is None
        )

        # NLI never executed
        if nli_not_executed:
            item["nli_relation"] = "not evaluated"

        # outside Top-K and weak NLI
        elif outside_top_k and not strong_nli:
            item["nli_relation"] = "unrelated"

    return sorted_results


def select_nli_candidates(rows):
    ranked = sorted(
        list(enumerate(rows)),
        key=lambda x: x[1]["score"],
        reverse=True
    )
    selected = []
    for rank, (idx, r) in enumerate(ranked, start=1):
        if r["score"] >= SBERT_THRESHOLD or rank <= TOP_N:
            selected.append((idx, r))

    return selected

def cosine_similarity(data, text1_list):
    results = {}

    text2_designations = [item.designation or "" for item in data.text2_list]
    text2_definitions = [item.definition or "" for item in data.text2_list]

    text2_joint = [
        f"{d} {df}".strip()
        for d, df in zip(text2_designations, text2_definitions)
    ]

    emb2_designations = embed_sbert(text2_designations)
    emb2_definitions = embed_sbert(text2_definitions)
    emb2_joint = embed_sbert(text2_joint)

    for text1_item in text1_list:
        designation1 = text1_item.designation or ""
        definition1 = text1_item.definition or ""

        text1_key = f"{designation1} - {definition1}"
        joint1 = f"{designation1} {definition1}".strip()

        emb1_designation = embed_sbert([designation1])
        emb1_definition = embed_sbert([definition1])
        emb1_joint = embed_sbert([joint1])

        sim_designation = sbert_model.similarity(emb1_designation, emb2_designations)
        sim_definition = sbert_model.similarity(emb1_definition, emb2_definitions)
        sim_joint = sbert_model.similarity(emb1_joint, emb2_joint)

        results[text1_key] = []

        for idx in range(len(text2_designations)):
            s_label = float(sim_designation[0][idx])
            s_def = float(sim_definition[0][idx])
            s_joint = float(sim_joint[0][idx])

            boost_score = s_label + 0.15 * s_def

            final_score = max(
                s_label,
                s_joint,
                boost_score
            )

            final_score = min(1.0, final_score)

            results[text1_key].append({
                "text2": f"{text2_designations[idx]} - {text2_definitions[idx]}",
                "score": round(final_score, 4),
                "designation_score": round(s_label, 4),
                "definition_score": round(s_def, 4),
                "joint_score": round(s_joint, 4)
            })

    return results


async def nlp_relation_score_one_to_many(data):
    cosine_results = cosine_similarity(data, [data.text1])
    text1_str = f"{data.text1.designation} - {data.text1.definition}"
    rows = cosine_results.get(text1_str, [])

    final_results = []

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

    selected_candidates = select_nli_candidates(rows)

    filtered_candidates = [
        data.text2_list[idx]
        for idx, r in selected_candidates
    ]

    if filtered_candidates:
        ref_dict = {
            "designation": data.text1.designation,
            "definition": data.text1.definition or ""
        }

        candidate_dicts = [
            {"designation": c.designation, "definition": c.definition or ""}
            for c in filtered_candidates
        ]

        results = await run_in_threadpool(
            nli_function.classify_relation_batch,
            ref_dict,
            candidate_dicts,
            nli_function.nli_model
        )

        nli_by_designation = {
            r["designation"]: r
            for r in results
        }

        for item in final_results:
            nli = nli_by_designation.get(item["designation"])
            if nli:
                item["nli_relation"] = nli["nli_relation"]
                item["nli_confidence"] = nli["nli_confidence"]
                item["wider_score"] = nli["wider_score"]
                item["specific_score"] = nli["specific_score"]

    final_results = apply_top_k_relation_filter(final_results, TOP_N)

    return {"results": final_results}


async def nlp_relation_score_many_to_many(data):
    cosine_results = cosine_similarity(data, data.text1_list)
    final_results = []

    for text1_item in data.text1_list:
        text1_str = f"{text1_item.designation} - {text1_item.definition}"
        rows = cosine_results.get(text1_str, [])

        base_results = []

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

        selected_candidates = select_nli_candidates(rows)

        filtered_candidates = [
            data.text2_list[idx]
            for idx, r in selected_candidates
        ]

        if filtered_candidates:
            ref_dict = {
                "designation": text1_item.designation,
                "definition": text1_item.definition or ""
            }

            candidate_dicts = [
                {"designation": c.designation, "definition": c.definition or ""}
                for c in filtered_candidates
            ]

            results = await run_in_threadpool(
                nli_function.classify_relation_batch,
                ref_dict,
                candidate_dicts,
                nli_function.nli_model
            )

            nli_by_designation = {
                r["designation"]: r
                for r in results
            }

            for item in base_results:
                nli = nli_by_designation.get(item["designation"])
                if nli:
                    item["nli_relation"] = nli["nli_relation"]
                    item["nli_confidence"] = nli["nli_confidence"]
                    item["wider_score"] = nli["wider_score"]
                    item["specific_score"] = nli["specific_score"]

        base_results = apply_top_k_relation_filter(base_results, TOP_N)
        final_results.extend(base_results)

    return {"results": final_results}