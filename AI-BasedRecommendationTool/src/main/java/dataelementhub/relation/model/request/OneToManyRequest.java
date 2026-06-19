package dataelementhub.relation.model.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class OneToManyRequest implements Serializable {
    private String elementAUrn;
    private int sourceB;
}
