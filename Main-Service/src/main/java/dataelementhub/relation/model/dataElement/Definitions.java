package dataelementhub.relation.model.dataElement;

import lombok.Data;

import java.io.Serializable;

@Data
public class Definitions implements Serializable {
    private String designation;
    private String definition;
    private String language;
}
