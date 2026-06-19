package dataelementhub.relation.model.dataElement;

import lombok.Data;

import java.io.Serializable;

@Data
public class Identification implements Serializable {
    private String urn;
    private String namespaceUrn;
    private String elementType;
    private int identifier;
    private int revision;
    private String status;
}
