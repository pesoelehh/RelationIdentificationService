package dataelementhub.relation.service.nlpCases.relationMapping;

import dataelementhub.relation.model.dataElement.compact.DataElementSummary;
import dataelementhub.relation.model.dataElement.type.*;
import dataelementhub.relation.model.scoredElement.relationMapping.ComparisonResult;
import dataelementhub.relation.model.scoredElement.status.DatatypeCompatibility;
import dataelementhub.relation.util.DatatypeCompatibilityUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
/**
 * Compares two Data Elements based on their datatype definitions and
 * datatype-specific attributes.
 *
 * This component performs a structural comparison that complements the
 * NLP-based relation analysis. The comparison evaluates datatype
 * compatibility as well as constraints and metadata associated with
 * the corresponding value domains.
 */
public class DataElementComparator {
    /**
     * Compares two Data Elements and evaluates their structural compatibility.
     *
     * The comparison process consists of:
     * 1. Determining datatype compatibility.
     * 2. Comparing datatype-specific attributes.
     * 3. Evaluating unit compatibility for numeric values.
     *
     * Depending on the datatype, different attributes such as regular
     * expressions, numeric ranges, date formats, or permitted values
     * are considered during the comparison.
     *
     * @param a First Data Element.
     * @param b Second Data Element.
     * @return Comparison result containing compatibility information.
     */
    public static ComparisonResult compare(DataElementSummary a, DataElementSummary b) {
        ComparisonResult result = new ComparisonResult();
        // Determine the value domain types of both Data Elements.
        String typeA = a.getValueDomainType();
        String typeB = b.getValueDomainType();

        // Evaluate datatype compatibility before comparing attributes.
        DatatypeCompatibility compatibility = DatatypeCompatibilityUtil.checkCompatibility(typeA, typeB);
        result.setDatatypeCompatibility(compatibility);

        // Default assumption: attributes match unless a mismatch is detected.
        boolean attributesMatch = true;
        // Unit comparison is only relevant for numeric datatypes.
        boolean unitMatch = true;
        if (compatibility != DatatypeCompatibility.INCOMPATIBLE) {
            switch (typeA.toLowerCase()) {

                // Compare text-specific constraints.
                case "string" -> {
                    Text ta = a.getText();
                    Text tb = b.getText();
                    if (ta != null && tb != null) {

                        // Compare regular expression constraints when both elements define one.
                        if (Boolean.TRUE.equals(ta.getUseRegEx()) && Boolean.TRUE.equals(tb.getUseRegEx())) {
                            if (!Objects.equals(ta.getRegEx(), tb.getRegEx())) {
                                attributesMatch = false;
                            }
                        }

                        // Compare maximum length restrictions when enabled on both sides.
                        if (Boolean.TRUE.equals(ta.getUseMaximumLength()) && Boolean.TRUE.equals(tb.getUseMaximumLength())) {
                            if (!Objects.equals(ta.getMaximumLength(), tb.getMaximumLength())) {
                                attributesMatch = false;
                            }
                        }
                    }
                }

                // Compare numeric constraints and units of measure.
                case "numeric" -> {
                    Numeric na = a.getNumeric();
                    Numeric nb = b.getNumeric();
                    if (na != null && nb != null) {
                        // Evaluate whether both elements use the same measurement unit.
                        unitMatch = Objects.equals(na.getUnitOfMeasure(), nb.getUnitOfMeasure());

                        if (na instanceof NumericInteger niA && nb instanceof NumericInteger niB) {
                            // Compare minimum and maximum boundaries for integer values.
                            attributesMatch = Objects.equals(niA.getMinimum(), niB.getMinimum())
                                    && Objects.equals(niA.getMaximum(), niB.getMaximum());
                        } else if (na instanceof NumericFloat nfA && nb instanceof NumericFloat nfB) {
                            // Compare minimum and maximum boundaries for floating-point values.
                            attributesMatch = Objects.equals(nfA.getMinimum(), nfB.getMinimum())
                                    && Objects.equals(nfA.getMaximum(), nfB.getMaximum());
                        } else {
                            // Different numeric subtypes are considered compatible but not identical.
                            attributesMatch = false;
                        }
                    }
                }

                // Compare complete datetime specifications.
                case "datetime" -> {
                    Datetime da = a.getDatetime();
                    Datetime db = b.getDatetime();
                    if (da != null && db != null) {
                        attributesMatch = Objects.equals(da.getDate(), db.getDate())
                                && Objects.equals(da.getTime(), db.getTime())
                                && Objects.equals(da.getHourFormat(), db.getHourFormat());
                    }
                }

                // Compare date format definitions.
                case "date" -> {
                    Datetime da = a.getDatetime();
                    Datetime db = b.getDatetime();
                    if (da != null && db != null) {
                        attributesMatch = Objects.equals(da.getDate(), db.getDate());
                    }
                }

                // Compare date format definitions.
                case "time" -> {
                    Datetime da = a.getDatetime();
                    Datetime db = b.getDatetime();
                    if (da != null && db != null) {
                        attributesMatch = Objects.equals(da.getTime(), db.getTime())
                                && Objects.equals(da.getHourFormat(), db.getHourFormat());
                    }
                }

                // Compare the set of permitted boolean values.
                case "boolean" -> {
                    List<PermittedValue> pa = a.getPermittedValues();
                    List<PermittedValue> pb = b.getPermittedValues();
                    if (pa != null && pb != null) {
                        attributesMatch = new HashSet<>(pa).containsAll(pb) && new HashSet<>(pb).containsAll(pa);
                    }
                }

                // Compare the set of allowed enumerated values.
                case "enumerated" -> {
                    List<PermittedValue> ea = a.getPermittedValues();
                    List<PermittedValue> eb = b.getPermittedValues();
                    if (ea != null && eb != null) {
                        attributesMatch = new HashSet<>(ea).containsAll(eb) && new HashSet<>(eb).containsAll(ea);
                    }
                }

                // Compare the set of allowed enumerated values.
                case "catalog" -> {
                    attributesMatch = Objects.equals(a.getIdentification(), b.getIdentification());
                }

                // Unsupported datatype comparison.
                default -> attributesMatch = false;
            }
        }

        // Store comparison results.
        result.setAttributesMatch(attributesMatch);
        result.setUnitMatch(unitMatch);

        return result;
    }
}
