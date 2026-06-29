package dataelementhub.relation.util;

import com.opencsv.CSVWriter;
import dataelementhub.relation.model.dataElement.Definitions;
import dataelementhub.relation.model.dataElement.compact.DataElementSummary;
import dataelementhub.relation.model.scoredElement.RelationSuggestion;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
/**
 * Utility class for exporting relation analysis results to CSV files.
 *
 * This component generates structured CSV outputs containing NLP relation
 * predictions and LLM-based relation results. The exported files are used
 * for result inspection, evaluation, and comparison during the research process.
 */

public class Result_CreateCSV {
    /**
     * Exports relation analysis results to a CSV file.
     *
     * The generated file contains similarity scores, NLI predictions,
     * confidence values, and the final relation type for each suggested
     * relation between two data elements.
     *
     * @param suggestions List of relation suggestions to export.
     */
    public void writeRelationMappingCsv(List<RelationSuggestion> suggestions) {
        // Define output location for relation analysis results.
        String filePath = "C:\\PC_Data\\THM\\Master\\Result\\nlp_result.csv";

        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
            // Write CSV column headers.
            String[] header = {"Source A (German Text)", "Source B (German Text)", "Wider_Score", "Specific_Score","NLI_Confidence", "Similarity_Score", "NLI_relation", "Relation_Type"};
            writer.writeNext(header);

            for (RelationSuggestion rs : suggestions) {
                // Extract readable German text representations of both data elements.
                String sourceA = extractGermanText(rs.getTextA());
                String sourceB = extractGermanText(rs.getTextB());
                String wider_score = String.valueOf(rs.getRelationAnalysisResult().getWiderScore());
                String specific_score = String.valueOf(rs.getRelationAnalysisResult().getSpecificScore());
                String nli_confidence = String.valueOf(rs.getRelationAnalysisResult().getNliConfidence());
                String similarity_score = String.valueOf(rs.getRelationAnalysisResult().getSimilarityScore());
                String nli_relation = String.valueOf(rs.getRelationAnalysisResult().getNliRelation());
                String relation_typ = String.valueOf(rs.getRelationType());

                // Write a single relation result to the CSV file.
                writer.writeNext(new String[]{sourceA, sourceB, wider_score, specific_score, nli_confidence, similarity_score, nli_relation, relation_typ});
            }

            System.out.println("CSV file with German text created successfully: " + filePath);
        } catch (IOException e) {
            System.err.println("Error writing CSV file: " + e.getMessage());
        }
    }


    /**
     * Extracts a readable German text representation from a DataElement.
     *
     * The returned value combines the selected designation and definition
     * into a single string that can be used for reporting and result
     * visualization.
     *
     * @param element DataElement from which the text is extracted.
     * @return Combined designation and definition string.
     */
    public String extractGermanText(DataElementSummary element) {
        // Return an empty string when no definition is available.
        if (element == null || element.getDefinitions() == null || element.getDefinitions().isEmpty()) {
            return "";
        }

        // Select the preferred German definition.
        Definitions germanDef = PreProcessingText.getGermanDefinition(element);

        // Clean textual fields before generating the output representation.
        String designation = germanDef.getDesignation() != null ? germanDef.getDesignation().trim() : "";
        String definition = germanDef.getDefinition() != null ? germanDef.getDefinition().trim() : "";

        // Combine designation and definition into a readable format.
        return designation + " - " + definition;
    }

}
