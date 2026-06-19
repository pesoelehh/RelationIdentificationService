package dataelementhub.relation.model.aiModel.llmDto;

import lombok.Data;

import java.io.Serializable;

@Data
public class LLMDataelement implements Serializable {
    private String id;
    private String designation;
    private String definition;
    private String datatype;
}
