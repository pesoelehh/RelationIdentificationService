package dataelementhub.relation.model.dataElement;

import lombok.Data;

import java.io.Serializable;

@Data
public class ConceptAssociations implements Serializable {
    private int conceptId;
    private String system;
    private int sourceId;
    private String version;
    private String term;
    private String text;
    private String linkType;
    private int scopedIdentifierId;
}
