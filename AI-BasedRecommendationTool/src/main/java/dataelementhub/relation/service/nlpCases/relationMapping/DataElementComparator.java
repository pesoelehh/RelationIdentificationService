package dataelementhub.relation.service.nlpCases.relationMapping;

import dataelementhub.relation.model.dataElement.compact.DataElementSummary;
import dataelementhub.relation.model.dataElement.type.*;
import dataelementhub.relation.model.scoredElement.relationMapping.ComparisonResult;
import dataelementhub.relation.model.scoredElement.status.DatatypeCompatibility;
import dataelementhub.relation.util.DatatypeCompatibilityUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class DataElementComparator {

    public static ComparisonResult compare(DataElementSummary a, DataElementSummary b) {
        ComparisonResult result = new ComparisonResult();

        String typeA = a.getValueDomainType();
        String typeB = b.getValueDomainType();

        // === 1. Check datatype compatibility ===
        DatatypeCompatibility compatibility = DatatypeCompatibilityUtil.checkCompatibility(typeA, typeB);
        result.setDatatypeCompatibility(compatibility);

        // === 2. Initialize attributes ===
        boolean attributesMatch = true;   // assume match until proven otherwise
        boolean unitMatch = true;         // only relevant for numeric types

        if (compatibility != DatatypeCompatibility.INCOMPATIBLE) {
            switch (typeA.toLowerCase()) {

                // === Text / String type ===
                case "string" -> {
                    Text ta = a.getText();
                    Text tb = b.getText();
                    if (ta != null && tb != null) {

                        // Compare regEx only if both use it
                        if (Boolean.TRUE.equals(ta.getUseRegEx()) && Boolean.TRUE.equals(tb.getUseRegEx())) {
                            if (!Objects.equals(ta.getRegEx(), tb.getRegEx())) {
                                attributesMatch = false;
                            }
                        }

                        // Compare maximumLength only if both use it
                        if (Boolean.TRUE.equals(ta.getUseMaximumLength()) && Boolean.TRUE.equals(tb.getUseMaximumLength())) {
                            if (!Objects.equals(ta.getMaximumLength(), tb.getMaximumLength())) {
                                attributesMatch = false;
                            }
                        }
                    }
                }

                // === Numeric type (Integer / Float / Decimal) ===
                case "numeric" -> {
                    Numeric na = a.getNumeric();
                    Numeric nb = b.getNumeric();
                    if (na != null && nb != null) {
                        unitMatch = Objects.equals(na.getUnitOfMeasure(), nb.getUnitOfMeasure());

                        if (na instanceof NumericInteger niA && nb instanceof NumericInteger niB) {
                            attributesMatch = Objects.equals(niA.getMinimum(), niB.getMinimum())
                                    && Objects.equals(niA.getMaximum(), niB.getMaximum());
                        } else if (na instanceof NumericFloat nfA && nb instanceof NumericFloat nfB) {
                            attributesMatch = Objects.equals(nfA.getMinimum(), nfB.getMinimum())
                                    && Objects.equals(nfA.getMaximum(), nfB.getMaximum());
                        } else {
                            // Integer vs Float → compatible but not exactly the same
                            attributesMatch = false;
                        }
                    }
                }

                // === Datetime type (date + time) ===
                case "datetime" -> {
                    Datetime da = a.getDatetime();
                    Datetime db = b.getDatetime();
                    if (da != null && db != null) {
                        attributesMatch = Objects.equals(da.getDate(), db.getDate())
                                && Objects.equals(da.getTime(), db.getTime())
                                && Objects.equals(da.getHourFormat(), db.getHourFormat());
                    }
                }

                // === Date only ===
                case "date" -> {
                    Datetime da = a.getDatetime();
                    Datetime db = b.getDatetime();
                    if (da != null && db != null) {
                        attributesMatch = Objects.equals(da.getDate(), db.getDate());
                    }
                }

                // === Time only ===
                case "time" -> {
                    Datetime da = a.getDatetime();
                    Datetime db = b.getDatetime();
                    if (da != null && db != null) {
                        attributesMatch = Objects.equals(da.getTime(), db.getTime())
                                && Objects.equals(da.getHourFormat(), db.getHourFormat());
                    }
                }

                // === Boolean ===
                case "boolean" -> {
                    List<PermittedValue> pa = a.getPermittedValues();
                    List<PermittedValue> pb = b.getPermittedValues();
                    if (pa != null && pb != null) {
                        attributesMatch = new HashSet<>(pa).containsAll(pb) && new HashSet<>(pb).containsAll(pa);
                    }
                }

                // === Enumerated Values ===
                case "enumerated" -> {
                    List<PermittedValue> ea = a.getPermittedValues();
                    List<PermittedValue> eb = b.getPermittedValues();
                    if (ea != null && eb != null) {
                        attributesMatch = new HashSet<>(ea).containsAll(eb) && new HashSet<>(eb).containsAll(ea);
                    }
                }

                // === Catalog ===
                case "catalog" -> {
                    attributesMatch = Objects.equals(a.getIdentification(), b.getIdentification());
                }

                default -> attributesMatch = false;
            }
        }

        // === 3. Set result values ===
        result.setAttributesMatch(attributesMatch);
        result.setUnitMatch(unitMatch);

        return result;
    }
}
