"""
Request Models

This module defines the Pydantic data models used for request validation
within the NLP API.

Models:
- TextItem:
    Represents a single text entity consisting of a designation and definition.

- SimilarityRequest:
    Represents the request payload used for similarity and relation analysis
    endpoints. Supports both one-to-many and many-to-many comparison workflows.

Author: Gabriel Nathanael da Gomez
Email: gabrieldagomez@gmail.com
"""

from pydantic import BaseModel, Field, ConfigDict
from typing import List

class TextItem(BaseModel):
    """
       Represents a single text item used in similarity and relation analysis.

       Attributes:
           designation (str):
               Primary label, title, or term being evaluated.

           definition (str):
               Descriptive text that provides additional context for the designation.
   """
    designation: str
    definition: str

class SimilarityRequest(BaseModel):
    """
       Request model for similarity and NLP relation analysis operations.

       This model supports two comparison modes:

       1. One-to-many:
          - text1 contains a single reference item.
          - text2_list contains candidate items.

       2. Many-to-many:
          - text1_list contains multiple reference items.
          - text2_list contains candidate items.

       Attributes:
           text1 (TextItem, optional):
               Single reference item for one-to-many comparisons.

           text1_list (List[TextItem], optional):
               Multiple reference items for many-to-many comparisons.

           text2_list (List[TextItem]):
               Candidate items to be compared against the reference item(s).
   """
    # Single reference item used for one-to-many comparisons.
    text1: TextItem | None = None

    # Multiple reference items used for many-to-many comparisons.
    text1_list: List[TextItem] | None = None

    # Candidate items that will be compared against the reference items.
    text2_list: List[TextItem] = Field(..., alias="text2_list")

    model_config = ConfigDict(
        populate_by_name=True
    )