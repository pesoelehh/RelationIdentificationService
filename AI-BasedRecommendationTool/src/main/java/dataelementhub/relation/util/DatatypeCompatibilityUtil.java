package dataelementhub.relation.util;

import dataelementhub.relation.model.scoredElement.status.DatatypeCompatibility;

public class DatatypeCompatibilityUtil {
    public static DatatypeCompatibility checkCompatibility(String typeA, String typeB) {
        if (typeA == null || typeB == null) {
            return DatatypeCompatibility.INCOMPATIBLE;
        }

        typeA = typeA.toLowerCase();
        typeB = typeB.toLowerCase();

        if (typeA.equals(typeB)) {
            return DatatypeCompatibility.EXACT;
        }

        // Integer vs Decimal/Float
        if ((typeA.equals("integer") && (typeB.equals("float") || typeB.equals("decimal")))
                || (typeB.equals("integer") && (typeA.equals("float") || typeA.equals("decimal")))) {
            return DatatypeCompatibility.COMPATIBLE;
        }

        // Date vs Datetime
        if ((typeA.equals("date") && typeB.equals("datetime"))
                || (typeB.equals("date") && typeA.equals("datetime"))) {
            return DatatypeCompatibility.COMPATIBLE;
        }

        // Datetime vs Time
        if ((typeA.equals("datetime") && typeB.equals("time"))
                || (typeB.equals("time") && typeA.equals("datetime"))) {
            return DatatypeCompatibility.COMPATIBLE;
        }

        // EnumeratedValue vs Catalog
        if ((typeA.equals("enumeratedvalue") && typeB.equals("catalog"))
                || (typeB.equals("catalog") && typeA.equals("enumeratedvalue"))) {
            return DatatypeCompatibility.COMPATIBLE;
        }

        return DatatypeCompatibility.INCOMPATIBLE;
    }
}
