package dataelementhub.relation.service.nlpCases.rankingService;

import dataelementhub.relation.model.aiModel.SimilarityResponse;
import dataelementhub.relation.model.aiModel.SimilarityResult;
import dataelementhub.relation.model.aiModel.TextItem;
import dataelementhub.relation.model.dataElement.compact.DataElementSummary;
import dataelementhub.relation.model.scoredElement.ScoredElements;
import dataelementhub.relation.service.aiService.AIClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class EqualRanking {

    private final AIClient aiClient;
//
//    public List<ScoredElements> rank(DataElementSummary a, List<DataElementSummary> bList, String relationType) {
//
//        // 1. Filter according to relationType
//        List<DataElementSummary> filtered = filterByRelationType(a, bList, relationType);
//
//        // 2. Prepare AI input
//        TextItem text1 = new TextItem();
//        text1.setDesignation(a.getDefinitions().get(0).getDesignation());
//        text1.setDefinition(a.getDefinitions().get(0).getDefinition());
//
//        List<TextItem> text2List = filtered.stream()
//                .map(b -> {
//                    TextItem item = new TextItem();
//                    item.setDesignation(b.getDefinitions().get(0).getDesignation());
//                    item.setDefinition(b.getDefinitions().get(0).getDefinition());
//                    return item;
//                })
//                .toList();
//
//
//        // 3. Get similarity scores
//        List<Double> scores = aiClient.getSimilarities(text1, text2List);
//
//
//        // 4. Pair A with each B + score
//        List<ScoredElements> result = new ArrayList<>();
//        for (int i = 0; i < text2List.size(); i++) {
//
//            result.add(new ScoredElements(text1, text2List.get(i), scores.get(i)));
//
//
//        }
//
//
//        // 5. Sort by highest score first
//        result.sort((x, y) -> Double.compare(y.getScore(), x.getScore()));
//
//
//        return result;
//    }

    private List<DataElementSummary> filterByRelationType(DataElementSummary a, List<DataElementSummary> bList, String type) {
        if (Objects.equals(type, "EQUAL")) {
            return bList.stream()
                    .filter(b -> Objects.equals(a.getValueDomainType(), b.getValueDomainType()))
                    .toList();
        }
        return bList;
    }
}
