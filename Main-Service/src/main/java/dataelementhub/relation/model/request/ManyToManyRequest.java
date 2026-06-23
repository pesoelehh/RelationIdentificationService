package dataelementhub.relation.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * Request object for many-to-many relation analysis.
 *
 * The request specifies two namespaces whose Data Elements
 * will be compared against each other.
 */
@Data
public class ManyToManyRequest implements Serializable {
    private int source_a;
    private int source_b;
}
