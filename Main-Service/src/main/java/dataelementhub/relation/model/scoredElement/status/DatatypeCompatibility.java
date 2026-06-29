package dataelementhub.relation.model.scoredElement.status;

/**
 * Defines the compatibility levels between two datatype definitions.

 * The compatibility status is used during structural comparison to
 * determine whether two Data Elements can be considered technically
 * compatible.
 */
public enum DatatypeCompatibility {
    EXACT,
    COMPATIBLE,
    INCOMPATIBLE
}
