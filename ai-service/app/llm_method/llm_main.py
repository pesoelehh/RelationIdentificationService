from app.llm_method.gu_llm.gu_llama_client import GUClient
from app.llm_method.gu_llm.prompts import build_prompt_pair
from app.llm_method.models.request_model import ComparisonRequest


class LLMMethod:

    def __init__(self):
        self.client = GUClient()
    def compare_pair(self, a: dict, b: dict, datatypeCompatibility: str, datatypeAttributesEqual: bool, unitMatch) -> dict:

        if datatypeCompatibility == "INCOMPATIBLE":
            return {
                "relation": "UNRELATED",
                "source": "rule",
                "confidence": 1.0
            }

        prompt = build_prompt_pair(a, b, datatypeCompatibility, datatypeAttributesEqual, unitMatch)
        label = self.client.classify(prompt)

        return {
            "relation": label,
            "source": "llm",
            "confidence": None
        }

    def compare_one_to_many(self, request: ComparisonRequest):
        results_one_to_many = []

        for a in request.source_a:
            for target in request.source_b:
                b = target.element

                result = self.compare_pair(
                    a.model_dump(),
                    b.model_dump(),
                    target.comparisonResult.datatypeCompatibility.value,  # Enum → string
                    target.comparisonResult.attributesMatch,
                    target.comparisonResult.unitMatch
                )

                if result["relation"] is not None:
                    results_one_to_many.append({
                        "source_a_id": a.id,
                        "source_a_designation": a.designation,
                        "source_b_id": b.id,
                        "source_b_designation": b.designation,
                        "relation": result["relation"]
                    })

        return {"results": results_one_to_many }

    def compare_many_to_many(self, request: ComparisonRequest):
        results_one_to_many = []

        for a in request.source_a:
            for target in request.source_b:
                b = target.element

                result = self.compare_pair(
                    a.model_dump(),
                    b.model_dump(),
                    target.comparisonResult.datatypeCompatibility.value,  # Enum → string
                    target.comparisonResult.attributesMatch,
                    target.comparisonResult.unitMatch
                )

                if result["relation"] != "UNRELATED":
                     results_one_to_many.append({
                        "source_a_id": a.id,
                        "source_a_designation": a.designation,
                        "source_b_id": b.id,
                        "source_b_designation": b.designation,
                        "relation": result["relation"]
                })

        return {"results": results_one_to_many }

