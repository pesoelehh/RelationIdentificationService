package dataelementhub.relation.model.dataElement.type;

import dataelementhub.relation.model.dataElement.Definitions;
import dataelementhub.relation.model.dataElement.Identification;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PermittedValue implements Serializable {
    private Identification identification;
    private List<Definitions> definitions;
    private String value;

}

