package dataelementhub.relation.model.aiModel.llmDto;

import dataelementhub.relation.model.scoredElement.relationMapping.ComparisonResult;
import lombok.Data;

import java.io.Serializable;

@Data
public class LLMDataelementCompareTo implements Serializable {
    private LLMDataelement element;
    private ComparisonResult comparisonResult;
}
