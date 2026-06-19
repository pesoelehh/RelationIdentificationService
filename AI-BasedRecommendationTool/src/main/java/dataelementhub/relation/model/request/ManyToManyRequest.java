package dataelementhub.relation.model.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class ManyToManyRequest implements Serializable {
    private int source_a;
    private int source_b;
}
