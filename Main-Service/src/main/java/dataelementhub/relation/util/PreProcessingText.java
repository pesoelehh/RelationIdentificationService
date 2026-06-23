package dataelementhub.relation.util;

import dataelementhub.relation.model.aiModel.TextItem;
import dataelementhub.relation.model.dataElement.Definitions;
import dataelementhub.relation.model.dataElement.compact.DataElementSummary;
import dataelementhub.relation.model.dataElement.type.Datetime;
import dataelementhub.relation.model.dataElement.type.Numeric;
import dataelementhub.relation.model.dataElement.type.Text;

import java.text.Normalizer;
import java.util.stream.Collectors;

/**
 * Utility class responsible for text preprocessing and normalization.
 *
 * This component prepares DataElement objects for semantic comparison and
 * relation analysis by applying a series of normalization steps. The goal
 * is to ensure consistent textual representations and reduce variations
 * caused by formatting, language selection, casing, whitespace, or missing
 * values.
 *
 * Main responsibilities:
 * - Convert domain objects into AI-compatible text representations.
 * - Select the most relevant definition for processing.
 * - Normalize textual content before similarity evaluation.
 * - Standardize DataElement attributes to improve comparison reliability.
 *
 * The preprocessing pipeline is executed before similarity scoring and
 * relation classification are performed.
 */

public class PreProcessingText {

    /**
     * Converts a Definition into a TextItem used by the NLP layer.
     */
    public static TextItem toTextItem(Definitions def) {
        TextItem item = new TextItem();
        item.setDesignation(def.getDesignation());
        item.setDefinition(def.getDefinition());
        return item;
    }

    /**
     * Returns the German definition or falls back to the first available definition.
     */
    public static Definitions getGermanDefinition(DataElementSummary summary) {
        return summary.getDefinitions().stream()
                .filter(d -> "de".equalsIgnoreCase(d.getLanguage()))
                .findFirst()
                .orElse(summary.getDefinitions().get(0));
    }

    /**
     * Normalizes text values for consistent comparison and embedding generation.
     */
    public static String normalizeString(String text) {
        if (text == null) return null;
        return Normalizer.normalize(text, Normalizer.Form.NFKC)
                .toLowerCase()
                .trim()
                .replaceAll("\\s+", " "); // collapse spaces
    }

    /**
     * Normalizes all relevant attributes of a DataElementSummary.
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


        return element;
    }

    /**
     * Executes the complete preprocessing pipeline used before NLP analysis.
     */
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
