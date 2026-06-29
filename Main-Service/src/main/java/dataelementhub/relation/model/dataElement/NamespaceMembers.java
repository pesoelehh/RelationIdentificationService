package dataelementhub.relation.model.dataElement;

import lombok.Data;

import java.io.Serializable;

@Data
public class NamespaceMembers implements Serializable {
    private String elementUrn;
    private String status;
}
