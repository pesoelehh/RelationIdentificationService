package dataelementhub.relation.model.scoredElement.relationMapping;

import dataelementhub.relation.model.scoredElement.status.DatatypeCompatibility;
import lombok.Data;

import java.io.Serializable;

/**
 * Result of a structural comparison between two Data Elements.

 * The object stores datatype compatibility information and the outcome
 * of datatype-specific attribute comparisons.
 */
@Data
public class ComparisonResult implements Serializable {
    private DatatypeCompatibility datatypeCompatibility;
    private boolean attributesMatch;
    private boolean unitMatch;
}
