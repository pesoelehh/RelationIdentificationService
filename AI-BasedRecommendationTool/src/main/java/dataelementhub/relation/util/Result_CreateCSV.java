package dataelementhub.relation.util;

import com.opencsv.CSVWriter;
import dataelementhub.relation.model.aiModel.llmDto.LLMResult;
import dataelementhub.relation.model.dataElement.Definitions;
import dataelementhub.relation.model.dataElement.compact.DataElementSummary;
import dataelementhub.relation.model.scoredElement.RelationSuggestion;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class Result_CreateCSV {
    /**
     * Helper method to create a CSV file "relation_mapping.csv"
     */
    public void writeRelationMappingCsv(List<RelationSuggestion> suggestions) {
        String filePath = "C:\\PC_Data\\THM\\Master\\Result\\nlp_result.csv";

        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
            // Header
            String[] header = {"Source A (German Text)", "Source B (German Text)", "Wider_Score", "Specific_Score","NLI_Confidence", "Similarity_Score", "NLI_relation", "Relation_Type"};
            writer.writeNext(header);

            for (RelationSuggestion rs : suggestions) {
                // ✅ Extract normalized German text for A and B
                String sourceA = extractGermanText(rs.getTextA());
                String sourceB = extractGermanText(rs.getTextB());
                String wider_score = String.valueOf(rs.getNliResult().getWiderScore());
                String specific_score = String.valueOf(rs.getNliResult().getSpecificScore());
                String nli_confidence = String.valueOf(rs.getNliResult().getNliConfidence());
                String similarity_score = String.valueOf(rs.getNliResult().getSimilarityScore());
                String nli_relation = String.valueOf(rs.getNliResult().getNliRelation());
                String relation_typ = String.valueOf(rs.getRelationType());



                writer.writeNext(new String[]{sourceA, sourceB, wider_score, specific_score, nli_confidence, similarity_score, nli_relation, relation_typ});
            }

            System.out.println("✅ CSV file with German text created successfully: " + filePath);
        } catch (IOException e) {
            System.err.println("❌ Error writing CSV file: " + e.getMessage());
        }
    }

    public void writeLLMResultCsv(List<LLMResult> results) {
        String filePath = "C:\\PC_Data\\THM\\Master\\Result\\llm_result.csv";

        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
            // Header
            String[] header = {"Source A (German Text)", "Source B (German Text)", "Relation"};
            writer.writeNext(header);

            for (LLMResult rs : results) {
                // ✅ Extract normalized German text for A and B
                String sourceA = rs.getSource_a_designation();
                String sourceB = rs.getSource_b_designation();
                String relation = rs.getRelation();


                writer.writeNext(new String[]{sourceA, sourceB, relation});
            }

            System.out.println("✅ CSV file with German text created successfully: " + filePath);
        } catch (IOException e) {
            System.err.println("❌ Error writing CSV file: " + e.getMessage());
        }
    }


    /**
     * Extracts German text ("designation - definition") from a DataElementSummary.
     */
    public String extractGermanText(DataElementSummary element) {
        if (element == null || element.getDefinitions() == null || element.getDefinitions().isEmpty()) {
            return "";
        }

        // ✅ Get the German definition object (your LanguageUnification function)
        Definitions germanDef = PreProcessingText.getGermanDefinition(element);

        // Normalize both designation + definition (optional, if you want cleaned text)
        String designation = germanDef.getDesignation() != null ? germanDef.getDesignation().trim() : "";
        String definition = germanDef.getDefinition() != null ? germanDef.getDefinition().trim() : "";

        // Join as one readable text
        return designation + " - " + definition;
    }

}
