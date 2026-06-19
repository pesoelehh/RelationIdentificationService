package dataelementhub.relation.model.scoredElement.relationMapping;

import dataelementhub.relation.model.scoredElement.status.DatatypeCompatibility;
import lombok.Data;

import java.io.Serializable;

@Data
public class ComparisonResult implements Serializable {
    private DatatypeCompatibility datatypeCompatibility;
    private boolean attributesMatch;
    private boolean unitMatch;
}
