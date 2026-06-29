package dataelementhub.relation.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * Request object for one-to-many relation analysis.
 *
 * The request specifies a reference Data Element and a target
 * namespace whose Data Elements will be evaluated as candidates.
 */
@Data
public class OneToManyRequest implements Serializable {
    private String elementAUrn;
    private int sourceB;
}
