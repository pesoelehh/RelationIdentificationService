package dataelementhub.relation.model.dataElement;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class DataElement implements Serializable {
    private Identification identification;
    private List<Definitions> definitions;
    private String valueDomainUrn;
}
