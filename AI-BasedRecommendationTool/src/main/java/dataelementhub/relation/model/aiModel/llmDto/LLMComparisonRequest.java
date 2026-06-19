package dataelementhub.relation.model.aiModel.llmDto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class LLMComparisonRequest implements Serializable {
 private List<LLMDataelement> source_a;
 private List<LLMDataelementCompareTo> source_b;
}
