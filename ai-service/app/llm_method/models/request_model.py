from pydantic import BaseModel
from enum import Enum

class DatatypeCompatibility(str, Enum):
    EXACT = "EXACT"
    COMPATIBLE = "COMPATIBLE"
    INCOMPATIBLE = "INCOMPATIBLE"

class DataElement(BaseModel):
    id: str | None = None
    designation: str
    definition: str
    datatype: str

class ComparisonResult(BaseModel):
    datatypeCompatibility: DatatypeCompatibility
    attributesMatch: bool
    unitMatch: bool


class TargetElement(BaseModel):
    element: DataElement
    comparisonResult: ComparisonResult


class ComparisonRequest(BaseModel):
    source_a: list[DataElement]
    source_b: list[TargetElement]