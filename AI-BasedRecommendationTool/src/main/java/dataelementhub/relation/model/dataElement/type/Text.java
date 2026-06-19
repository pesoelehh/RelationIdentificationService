package dataelementhub.relation.model.dataElement.type;

import lombok.Data;

import java.io.Serializable;

@Data
public class Text implements Serializable {
    private Boolean useRegEx;
    private Boolean useMaximumLength;
    private String regEx;
    private String maximumLength;

}
