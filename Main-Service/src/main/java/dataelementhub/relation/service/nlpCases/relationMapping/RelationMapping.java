package dataelementhub.relation.service.nlpCases.relationMapping;

import dataelementhub.relation.model.aiModel.TextItem;
import dataelementhub.relation.model.dataElement.compact.DataElementSummary;
import dataelementhub.relation.model.scoredElement.RelationAnalysisResult;
import dataelementhub.relation.model.scoredElement.RelationSuggestion;
import dataelementhub.relation.service.aiService.AIClient;
import dataelementhub.relation.util.PreProcessingText;
import dataelementhub.relation.util.Result_CreateCSV;
import lombok.RequiredArgsConstructor;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static dataelementhub.relation.util.PreProcessingText.toTextItem;

/**
 * Service responsible for generating relation mapping suggestions between Data Elements.
 *
 * This component coordinates the complete relation mapping workflow:
 * preprocessing, conversion to NLP input objects, AI-based relation scoring,
 * datatype/attribute comparison, and rule-based classification using Drools.
 */
@Service
@RequiredArgsConstructor
public class RelationMapping {

    // Client used to request NLP-based relation scores from the AI service.
    private final AIClient aiClient;
    // Drools container used to create rule sessions for relation classification.
    private final KieContainer kieContainer;

    /**
     * Creates relation suggestions between one source Data Element and multiple target Data Elements.
     *
     * The method preprocesses the input elements, sends their textual representations
     * to the AI service, compares datatype-specific attributes, and applies Drools
     * rules to determine the final relation type.
     *
     * @param a Source Data Element.
     * @param bList Target Data Elements.
     * @param agendaGroup Drools agenda group containing the rules to execute.
     * @return List of generated relation suggestions.
     */
    public List<RelationSuggestion> relationMappingOneToMany(
            DataElementSummary a,
            List<DataElementSummary> bList,
            String agendaGroup
    ) {

        // Normalize source and target elements before NLP processing.
        DataElementSummary normalizedA =
                PreProcessingText.preprocessing(a);

        List<DataElementSummary> normalizedBList = bList.stream()
                .map(PreProcessingText::preprocessing)
                .toList();

        // Convert selected definitions into AI-compatible text objects.
        TextItem textA =
                toTextItem(normalizedA.getDefinitions().get(0));

        List<TextItem> textBList = normalizedBList.stream()
                .map(b -> toTextItem(b.getDefinitions().get(0)))
                .toList();

        // Request NLP relation scores for the source against all target elements.
        List<RelationAnalysisResult> relationAnalysisResults =
                aiClient.getNLPRelationOneToMany(textA, textBList);

        // Index NLI results by target designation for efficient lookup.
        Map<String, RelationAnalysisResult> nliByB =
                relationAnalysisResults.stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toMap(
                                r -> r.getDesignation().trim(),
                                r -> r,
                                (a1, a2) -> a1
                        ));

        // Create a new Drools session for rule-based relation classification.
        List<RelationSuggestion> results = new ArrayList<>();
        KieSession kieSession = kieContainer.newKieSession();

        try {
            for (int i = 0; i < textBList.size(); i++) {

                TextItem textB = textBList.get(i);
                DataElementSummary originalB = normalizedBList.get(i);
                // Build relation suggestion objects for each source-target pair.
                RelationSuggestion rs = new RelationSuggestion();
                rs.setTextA(a);
                rs.setTextB(originalB);
                // Compare datatype compatibility and datatype-specific attributes.
                rs.setComparisonResult(
                        DataElementComparator.compare(a, originalB)
                );
                // Use a default unrelated result when the AI service returns no match.
                RelationAnalysisResult nli = nliByB.getOrDefault(
                        textB.getDesignation().trim(),
                        new RelationAnalysisResult(
                                textA.getDesignation(),
                                textB.getDesignation(),
                                textB.getDefinition(),
                                BigDecimal.ZERO,
                                "unrelated",
                                BigDecimal.ZERO,
                                BigDecimal.ZERO,
                                BigDecimal.ZERO
                        )
                );

                rs.setRelationAnalysisResult(nli);

                kieSession.insert(rs);
                results.add(rs);
            }
            // Execute only the selected Drools agenda group.
            kieSession.getAgenda()
                    .getAgendaGroup(agendaGroup)
                    .setFocus();
            kieSession.fireAllRules();

        } finally {
            // Always dispose the Drools session to release resources.
            kieSession.dispose();
        }
        // Export generated relation suggestions for evaluation.
        new Result_CreateCSV().writeRelationMappingCsv(results);
        return results;
    }


    /**
     * Creates relation suggestions between multiple source and target Data Elements.
     *
     * This method performs batch-based relation mapping by comparing every source
     * element with every target element. NLP results are combined with structural
     * comparison results and then processed by Drools rules to assign the final
     * relation type.
     *
     * @param aList Source Data Elements.
     * @param bList Target Data Elements.
     * @param agendaGroup Drools agenda group containing the rules to execute.
     * @return List of generated relation suggestions.
     */
    public List<RelationSuggestion> relationMappingManyToMany(
            List<DataElementSummary> aList,
            List<DataElementSummary> bList,
            String agendaGroup
    ) {

        // Normalize all source and target elements before comparison.
        List<DataElementSummary> normalizedAList = aList.stream()
                .map(PreProcessingText::preprocessing)
                .toList();

        List<DataElementSummary> normalizedBList = bList.stream()
                .map(PreProcessingText::preprocessing)
                .toList();

        // Convert normalized Data Elements into NLP input objects.
        List<TextItem> textAList = normalizedAList.stream()
                .map(a -> toTextItem(a.getDefinitions().get(0)))
                .toList();

        List<TextItem> textBList = normalizedBList.stream()
                .map(b -> toTextItem(b.getDefinitions().get(0)))
                .toList();

        // Request NLP relation scores for all source-target combinations.
        List<RelationAnalysisResult> flatNliList =
                aiClient.getNLPRelationManyToMany(textAList, textBList);

        // Group NLI results by source designation and target designation.
        Map<String, Map<String, RelationAnalysisResult>> nliMap =
                flatNliList.stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.groupingBy(
                                r -> r.getReferenceDesignation().trim(),
                                Collectors.toMap(
                                        r -> r.getDesignation().trim(),
                                        r -> r,
                                        (a, b) -> a
                                )
                        ));

        // Create a new Drools session for rule execution.
        List<RelationSuggestion> results = new ArrayList<>();
        KieSession kieSession = kieContainer.newKieSession();

        try {
            // Iterate over every source-target combination.
            for (int i = 0; i < textAList.size(); i++) {

                TextItem textA = textAList.get(i);
                DataElementSummary originalA = normalizedAList.get(i);

                Map<String, RelationAnalysisResult> nliByB =
                        nliMap.getOrDefault(textA.getDesignation(), Map.of());

                for (int j = 0; j < textBList.size(); j++) {

                    TextItem textB = textBList.get(j);
                    DataElementSummary originalB = normalizedBList.get(j);

                    RelationSuggestion rs = new RelationSuggestion();
                    rs.setTextA(originalA);
                    rs.setTextB(originalB);

                    // Compare datatype compatibility and datatype-specific attributes.
                    rs.setComparisonResult(
                            DataElementComparator.compare(originalA, originalB)
                    );

                    // Use default unrelated values if no NLI result is available.
                    RelationAnalysisResult nli = nliByB.getOrDefault(
                            textB.getDesignation().trim(),
                            new RelationAnalysisResult(
                                    textA.getDesignation(),
                                    textB.getDesignation(),
                                    textB.getDefinition(),
                                    BigDecimal.ZERO,
                                    "unrelated",
                                    BigDecimal.ZERO,
                                    BigDecimal.ZERO,
                                    BigDecimal.ZERO
                            )
                    );

                    rs.setRelationAnalysisResult(nli);
                    // Insert the suggestion into the Drools working memory.
                    kieSession.insert(rs);
                    results.add(rs);
                }
            }
            // Fire rules from the selected agenda group.
            kieSession.getAgenda()
                    .getAgendaGroup(agendaGroup)
                    .setFocus();
            kieSession.fireAllRules();

        } finally {
            // Dispose the session after rule execution.
            kieSession.dispose();
        }
        // Export final mapping results to CSV for thesis evaluation.
        new Result_CreateCSV().writeRelationMappingCsv(results);
        return results;
    }
}
