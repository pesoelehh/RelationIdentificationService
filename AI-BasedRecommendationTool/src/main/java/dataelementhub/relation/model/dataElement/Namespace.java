package dataelementhub.relation.model.dataElement;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class Namespace implements Serializable {
    private Identification identification;
    private List<Definitions> definitions;

}
