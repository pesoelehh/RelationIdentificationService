package dataelementhub.relation.service.nlpCases.rankingService;

import dataelementhub.relation.model.aiModel.TextItem;
import dataelementhub.relation.model.dataElement.compact.DataElementSummary;
import dataelementhub.relation.model.scoredElement.ScoredElements;
import dataelementhub.relation.service.aiService.AIClient;
import static dataelementhub.relation.util.PreProcessingText.*;

import dataelementhub.relation.util.PreProcessingText;
import lombok.RequiredArgsConstructor;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class EquivalentRanking {

    private final AIClient aiClient;
    private final KieContainer kieContainer;

//
//    public List<ScoredElements> sortingEquivalent(DataElementSummary a, List<DataElementSummary> bList, String relationType) {
//        // 1. Filter using Drools
//        List<DataElementSummary> filtered = filterByDrools(a, bList, relationType);
//
//        // 2. Prepare AI input
//        TextItem text1 = toTextItem(getGermanDefinition(a));
//
//        List<TextItem> text2List = filtered.stream()
//                .map(PreProcessingText::getGermanDefinition)
//                .map(PreProcessingText::toTextItem)
//                .toList();
//
//        // 3. Get similarity scores
//        List<Double> scores = aiClient.getSimilarities(text1, text2List);
//
//        // 4. Pair A with each B + score
//        List<ScoredElements> scoredElements = new ArrayList<>();
//        for (int i = 0; i < text2List.size(); i++) {
//            scoredElements.add(new ScoredElements(text1, text2List.get(i), scores.get(i)));
//        }
//
//        // Stage 2: Filter scored list using Drools (score rules)
//        return filterByScoreDrools(scoredElements, relationType);
//    }

    private List<DataElementSummary> filterByDrools(DataElementSummary a, List<DataElementSummary> bList, String relationType) {
        KieBase kieBase = kieContainer.getKieBase();  // programmatic default KieBase
        KieSession kieSession = kieBase.newKieSession(); // programmatic session

        try {
            kieSession.setGlobal("leftElement",a);
            kieSession.insert(a);
            bList.forEach(kieSession::insert);
            kieSession.insert(relationType);

            kieSession.fireAllRules();

            return bList.stream()
                    .filter(b -> !kieSession.getObjects(o -> o.equals(b)).isEmpty())
                    .toList();
        } finally {
            kieSession.dispose();
        }
    }

    private List<ScoredElements> filterByScoreDrools(List<ScoredElements> elements, String relationType) {
        KieBase kieBase = kieContainer.getKieBase();
        KieSession kieSession = kieBase.newKieSession();

        try {
            kieSession.insert(relationType);
            elements.forEach(kieSession::insert);

            kieSession.fireAllRules();

            return kieSession.getObjects(o -> o instanceof ScoredElements)
                    .stream()
                    .map(o -> (ScoredElements) o)
                    .sorted((x, y) -> Double.compare(y.getScore(), x.getScore()))
                    .toList();
        } finally {
            kieSession.dispose();
        }
    }


}
