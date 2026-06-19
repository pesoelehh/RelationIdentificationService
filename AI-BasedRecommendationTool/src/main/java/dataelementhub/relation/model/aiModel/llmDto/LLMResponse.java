package dataelementhub.relation.model.aiModel.llmDto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class LLMResponse implements Serializable {
    List<LLMResult> results;
}
