package dataelementhub.relation.util;

import dataelementhub.relation.model.scoredElement.status.DatatypeCompatibility;

/**
 * Utility class for evaluating datatype compatibility between data elements.
 *
 * This component determines whether two datatypes are identical, compatible,
 * or incompatible according to predefined matching rules. The compatibility
 * information is used during relation analysis to avoid suggesting mappings
 * between semantically incompatible data elements.
 */
public class DatatypeCompatibilityUtil {
    /**
     * Evaluates the compatibility between two datatype definitions.
     *
     * Compatibility levels:
     * - EXACT: Both datatypes are identical.
     * - COMPATIBLE: Datatypes differ but can be considered interoperable.
     * - INCOMPATIBLE: Datatypes cannot be safely matched.
     *
     * @param typeA Datatype of the first data element.
     * @param typeB Datatype of the second data element.
     * @return Compatibility status between the two datatypes.
     */
    public static DatatypeCompatibility checkCompatibility(String typeA, String typeB) {
        // Missing datatype information cannot be evaluated.
        if (typeA == null || typeB == null) {
            return DatatypeCompatibility.INCOMPATIBLE;
        }

        typeA = typeA.toLowerCase();
        typeB = typeB.toLowerCase();

        // Identical datatypes represent an exact match.
        if (typeA.equals(typeB)) {
            return DatatypeCompatibility.EXACT;
        }

        // Numeric compatibility: integer values can typically be represented as decimal or float.
        if ((typeA.equals("integer") && (typeB.equals("float") || typeB.equals("decimal")))
                || (typeB.equals("integer") && (typeA.equals("float") || typeA.equals("decimal")))) {
            return DatatypeCompatibility.COMPATIBLE;
        }

        // Temporal compatibility: date values can be converted to datetime values.
        if ((typeA.equals("date") && typeB.equals("datetime"))
                || (typeB.equals("date") && typeA.equals("datetime"))) {
            return DatatypeCompatibility.COMPATIBLE;
        }

        // Temporal compatibility: datetime values contain time information.
        if ((typeA.equals("datetime") && typeB.equals("time"))
                || (typeB.equals("time") && typeA.equals("datetime"))) {
            return DatatypeCompatibility.COMPATIBLE;
        }

        // Semantic compatibility between controlled vocabulary representations.
        if ((typeA.equals("enumeratedvalue") && typeB.equals("catalog"))
                || (typeB.equals("catalog") && typeA.equals("enumeratedvalue"))) {
            return DatatypeCompatibility.COMPATIBLE;
        }

        // No compatibility rule found.
        return DatatypeCompatibility.INCOMPATIBLE;
    }
}
