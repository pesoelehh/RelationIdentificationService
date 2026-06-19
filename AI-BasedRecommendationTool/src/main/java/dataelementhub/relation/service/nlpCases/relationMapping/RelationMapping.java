package dataelementhub.relation.service.nlpCases.relationMapping;

import dataelementhub.relation.model.aiModel.TextItem;
import dataelementhub.relation.model.dataElement.compact.DataElementSummary;
import dataelementhub.relation.model.scoredElement.NliResult;
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

@Service
@RequiredArgsConstructor
public class RelationMapping {

    private final AIClient aiClient;
    private final KieContainer kieContainer;

    public List<RelationSuggestion> relationMappingOneToMany(
            DataElementSummary a,
            List<DataElementSummary> bList,
            String agendaGroup
    ) {

        // ---------------------------
        // 1. Normalize
        // ---------------------------
        DataElementSummary normalizedA =
                PreProcessingText.preprocessing(a);

        List<DataElementSummary> normalizedBList = bList.stream()
                .map(PreProcessingText::preprocessing)
                .toList();

        // ---------------------------
        // 2. Convert to TextItem
        // ---------------------------
        TextItem textA =
                toTextItem(normalizedA.getDefinitions().get(0));

        List<TextItem> textBList = normalizedBList.stream()
                .map(b -> toTextItem(b.getDefinitions().get(0)))
                .toList();

        // ---------------------------
        // 3. AI call (NLI ONLY)
        // ---------------------------
        List<NliResult> nliResults =
                aiClient.getNLNRelationOneToMany(textA, textBList);

        Map<String, NliResult> nliByB =
                nliResults.stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toMap(
                                r -> r.getDesignation().trim(),
                                r -> r,
                                (a1, a2) -> a1
                        ));

        // ---------------------------
        // 4. Drools session
        // ---------------------------
        List<RelationSuggestion> results = new ArrayList<>();
        KieSession kieSession = kieContainer.newKieSession();

        try {
            for (int i = 0; i < textBList.size(); i++) {

                TextItem textB = textBList.get(i);
                DataElementSummary originalB = normalizedBList.get(i);

                RelationSuggestion rs = new RelationSuggestion();
                rs.setTextA(a);
                rs.setTextB(originalB);

                rs.setComparisonResult(
                        DataElementComparator.compare(a, originalB)
                );

                NliResult nli = nliByB.getOrDefault(
                        textB.getDesignation().trim(),
                        new NliResult(
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

                rs.setNliResult(nli);

                kieSession.insert(rs);
                results.add(rs);
            }

            kieSession.getAgenda()
                    .getAgendaGroup(agendaGroup)
                    .setFocus();
            kieSession.fireAllRules();

        } finally {
            kieSession.dispose();
        }

        new Result_CreateCSV().writeRelationMappingCsv(results);
        return results;
    }



    public List<RelationSuggestion> relationMappingManyToMany(
            List<DataElementSummary> aList,
            List<DataElementSummary> bList,
            String agendaGroup
    ) {

        // ---------------------------
        // 1. Normalize inputs
        // ---------------------------
        List<DataElementSummary> normalizedAList = aList.stream()
                .map(PreProcessingText::preprocessing)
                .toList();

        List<DataElementSummary> normalizedBList = bList.stream()
                .map(PreProcessingText::preprocessing)
                .toList();

        // ---------------------------
        // 2. Convert to TextItem
        // ---------------------------
        List<TextItem> textAList = normalizedAList.stream()
                .map(a -> toTextItem(a.getDefinitions().get(0)))
                .toList();

        List<TextItem> textBList = normalizedBList.stream()
                .map(b -> toTextItem(b.getDefinitions().get(0)))
                .toList();

        // ---------------------------
        // 3. AI call (NLI ONLY)
        // ---------------------------
        List<NliResult> flatNliList =
                aiClient.getNLNRelationManyToMany(textAList, textBList);

        // reference A -> (B -> result)
        Map<String, Map<String, NliResult>> nliMap =
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

        // ---------------------------
        // 4. Drools session
        // ---------------------------
        List<RelationSuggestion> results = new ArrayList<>();
        KieSession kieSession = kieContainer.newKieSession();

        try {
            for (int i = 0; i < textAList.size(); i++) {

                TextItem textA = textAList.get(i);
                DataElementSummary originalA = normalizedAList.get(i);

                Map<String, NliResult> nliByB =
                        nliMap.getOrDefault(textA.getDesignation(), Map.of());

                for (int j = 0; j < textBList.size(); j++) {

                    TextItem textB = textBList.get(j);
                    DataElementSummary originalB = normalizedBList.get(j);

                    RelationSuggestion rs = new RelationSuggestion();
                    rs.setTextA(originalA);
                    rs.setTextB(originalB);

                    // Comparator
                    rs.setComparisonResult(
                            DataElementComparator.compare(originalA, originalB)
                    );

                    // NLI + similarity (ALWAYS present)
                    NliResult nli = nliByB.getOrDefault(
                            textB.getDesignation().trim(),
                            new NliResult(
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

                    rs.setNliResult(nli);

                    kieSession.insert(rs);
                    results.add(rs);
                }
            }

            kieSession.getAgenda()
                    .getAgendaGroup(agendaGroup)
                    .setFocus();
            kieSession.fireAllRules();

        } finally {
            kieSession.dispose();
        }

        new Result_CreateCSV().writeRelationMappingCsv(results);
        return results;
    }
}
