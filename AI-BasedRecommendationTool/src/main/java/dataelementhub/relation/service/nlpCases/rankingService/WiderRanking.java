package dataelementhub.relation.service.nlpCases.rankingService;

import dataelementhub.relation.model.aiModel.TextItem;
import dataelementhub.relation.model.dataElement.compact.DataElementSummary;
import dataelementhub.relation.model.scoredElement.NliResult;
import dataelementhub.relation.model.scoredElement.ScoredElements;
import dataelementhub.relation.service.aiService.AIClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WiderRanking {
    private final AIClient aiClient;

//    public List<ScoredElements> getWiderRanking(DataElementSummary a, List<DataElementSummary> bList, String relationTyp) {
//        //Prefiltered the data
//        //TODO: Filtered the data in source B
//
//        // Prepare AI input
//        TextItem text1 = new TextItem();
//        text1.setDesignation(a.getDefinitions().get(0).getDesignation());
//        text1.setDefinition(a.getDefinitions().get(0).getDefinition());
//
//        List<TextItem> text2List = bList.stream()
//                .map(b -> {
//                    TextItem item = new TextItem();
//                    item.setDesignation(b.getDefinitions().get(0).getDesignation());
//                    item.setDefinition(b.getDefinitions().get(0).getDefinition());
//                    return item;
//                })
//                .toList();
//
//        // Get wider scores
//        List<Double> scores = aiClient.getWiderScores(text1,text2List);
//
//        // Pair A with each B + score
//        List<ScoredElements> result = new ArrayList<>();
//        for (int i = 0; i < text2List.size(); i++) {
//            result.add(new ScoredElements(text1, text2List.get(i), scores.get(i)));
//        }
//
//        // Sort by highest score first
//        //TODO: This need to adjust
//        //result.sort((x,y) -> Double.compare(y.getScore(),x.getScore()));
//
//        return result;
//    }
}
