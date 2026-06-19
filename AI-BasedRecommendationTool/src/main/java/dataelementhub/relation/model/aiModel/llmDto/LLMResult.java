package dataelementhub.relation.model.aiModel.llmDto;

import lombok.Data;

import java.io.Serializable;

@Data
public class LLMResult implements Serializable {
    private String source_a_id;
    private String source_a_designation;
    private String source_b_id;
    private String source_b_designation;
    private String relation;
}
