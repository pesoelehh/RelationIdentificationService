package dataelementhub.relation.util;

import dataelementhub.relation.model.aiModel.TextItem;
import dataelementhub.relation.model.dataElement.Definitions;
import dataelementhub.relation.model.dataElement.compact.DataElementSummary;
import dataelementhub.relation.model.dataElement.type.Datetime;
import dataelementhub.relation.model.dataElement.type.Numeric;
import dataelementhub.relation.model.dataElement.type.Text;

import java.text.Normalizer;
import java.util.stream.Collectors;

public class PreProcessingText {

    /**
     * Convert a Definition into a TextItem.
     */
    public static TextItem toTextItem(Definitions def) {
        TextItem item = new TextItem();
        item.setDesignation(def.getDesignation());
        item.setDefinition(def.getDefinition());
        return item;
    }

    /**
     * Get the German definition if available, otherwise fallback to the first one.
     */
    public static Definitions getGermanDefinition(DataElementSummary summary) {
        return summary.getDefinitions().stream()
                .filter(d -> "de".equalsIgnoreCase(d.getLanguage()))
                .findFirst()
                .orElse(summary.getDefinitions().get(0));
    }

    /**
     * Normalize free-text fields for embeddings or comparisons.
     */
    public static String normalizeString(String text) {
        if (text == null) return null;
        return Normalizer.normalize(text, Normalizer.Form.NFKC)
                .toLowerCase()
                .trim()
                .replaceAll("\\s+", " "); // collapse spaces
    }

    /**
     * Normalize a DataElementSummary and return a cleaned clone-like object.
     * This ensures that comparisons don't break due to null vs "" vs case sensitivity.
     */
    public static DataElementSummary normalize(DataElementSummary element) {
        if (element == null) return null;

        // --- Normalize Definitions ---
        if (element.getDefinitions() != null) {
            element.setDefinitions(
                    element.getDefinitions().stream()
                            .map(d -> {
                                Definitions def = new Definitions();
                                def.setLanguage(normalizeString(d.getLanguage()));
                                def.setDesignation(normalizeString(d.getDesignation()));
                                def.setDefinition(normalizeString(d.getDefinition()));
                                return def;
                            })
                            .collect(Collectors.toList())
            );
        }

        // --- Normalize Text type ---
        Text text = element.getText();
        if (text != null) {
            text.setRegEx(normalizeString(text.getRegEx()));
            // normalize null/empty length
            if (text.getMaximumLength() != null && !text.getMaximumLength().equals("")) {
                text.setMaximumLength(null);
            }
        }


        // --- Normalize Numeric type ---
        Numeric num = element.getNumeric();
        if (num != null) {
            num.setUnitOfMeasure(normalizeString(num.getUnitOfMeasure()));
            // normalize min/max → treat null as null
            if (Boolean.FALSE.equals(num.getUseMinimum())) num.setUseMinimum(null);
            if (Boolean.FALSE.equals(num.getUseMaximum())) num.setUseMaximum(null);
        }

        // --- Normalize Datetime ---
        Datetime dt = element.getDatetime();
        if (dt != null) {
            dt.setDate(normalizeString(dt.getDate()));
            dt.setTime(normalizeString(dt.getTime()));
            dt.setHourFormat(normalizeString(dt.getHourFormat()));
        }


        // TODO: normalize Catalog or ConceptAssociations if needed

        return element;
    }

    public static DataElementSummary preprocessing(DataElementSummary summary) {
        if (summary == null) return null;

        // 1. Normalize everything
        DataElementSummary normalized = normalize(summary);

        // 2. Select German definition (or fallback)
        Definitions selectedDef = getGermanDefinition(normalized);

        // 3. Keep only this definition
        normalized.setDefinitions(
                selectedDef == null
                        ? null
                        : java.util.List.of(selectedDef)
        );

        return normalized;
    }


}
