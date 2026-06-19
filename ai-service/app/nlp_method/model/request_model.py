from pydantic import BaseModel, Field
from typing import List

class TextItem(BaseModel):
    designation: str
    definition: str

class SimilarityRequest(BaseModel):
    text1: TextItem = None
    text1_list: List[TextItem] = None
    text2_list: List[TextItem] = Field(..., alias="text2_list")
    class Config:
        validate_by_name = True
