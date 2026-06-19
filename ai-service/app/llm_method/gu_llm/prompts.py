
def build_prompt_pair(a: dict, b: dict, datatypeCompatibility: str, attributeMatch: bool, unitMatch: bool) -> str:
    return f"""
You are a metadata harmonization and data standards expert.
Your task is to determine the semantic relationship between two data elements.
Each element contains the following attributes:

Designation (name of the element)
Definition (description of the meaning)
Datatype

Structural facts:
Datatype compatibility: {datatypeCompatibility}
Datatype attributes equal: {attributeMatch}
Datatype unit equal: {unitMatch}

You must return EXACTLY ONE of the following labels:

EQUAL
EQUIVALENT
A_SUBSUMES_B
A_SPECIALIZES_B
UNRELATED

Relationship definitions:

EQUAL
Both elements represent the same concept, have the same scope, and are structurally compatible.

EQUIVALENT
Both elements represent the same real-world concept, but wording, datatype, representation, or contextual description differs.

A_SUBSUMES_B
Element A represents a broader concept, while element B is a more specific instance or specialization of A.

A_SPECIALIZES_B
Element A represents a more specific concept, while element B is broader.

UNRELATED
The elements represent different concepts without a meaningful semantic relationship.

Decision Rules (apply in order):

1. Datatype incompatibility rule
   If datatype compatibility is INCOMPATIBLE, classify as UNRELATED.

2. Designation priority rule
   The designation is the most important indicator of meaning.
   If the designations are identical or nearly identical, assume the elements are semantically related unless the definitions clearly contradict each other.

3. Exact equivalence rule
   If:

designation matches,
definitions describe the same meaning,
datatype compatibility is EXACT,
datatype attributes equal is true,
  → classify as EQUAL.

4. Semantic equivalence rule
   If the elements represent the same real-world concept but:

wording differs,
definitions are paraphrased,
datatype compatibility is not exact,
datatype attributes differ
  → classify as EQUIVALENT.

5. Generalization rule
   If element A describes a broader concept and element B describes a more specific subtype or refinement, classify as A_SUBSUMES_B.

6. Specialization rule
   If element A describes a specific case of the broader concept described by element B, classify as A_SPECIALIZES_B.

7. Definition interpretation rule
   Definitions may use different wording or contextual explanations but still describe the same concept. Treat paraphrases as equivalent meanings.

Data Element A:
Designation: {a["designation"]}
Definition: {a["definition"]}
Datatype: {a["datatype"]}

Data Element B:
Designation: {b["designation"]}
Definition: {b["definition"]}
Datatype: {b["datatype"]}

Return ONLY the label:
EQUAL, EQUIVALENT, A_SUBSUMES_B, A_SPECIALIZES_B, or UNRELATED.

Do NOT include explanations.

"""
