package dataelementhub.relation.controller.aiController;

import dataelementhub.relation.model.aiModel.SimilarityRequest;
import dataelementhub.relation.model.aiModel.SimilarityResponse;
import dataelementhub.relation.model.aiModel.SimilarityResult;
import dataelementhub.relation.model.aiModel.llmDto.LLMResult;
import dataelementhub.relation.model.dataElement.compact.DataElementSummary;
import dataelementhub.relation.model.request.ManyToManyRequest;
import dataelementhub.relation.model.scoredElement.RelationSuggestion;
import dataelementhub.relation.model.request.OneToManyRequest;
import dataelementhub.relation.service.aiService.AIClient;
import dataelementhub.relation.service.nlpCases.rankingService.EqualRanking;
import dataelementhub.relation.service.nlpCases.rankingService.EquivalentRanking;
import dataelementhub.relation.service.nlpCases.rankingService.WiderRanking;
import dataelementhub.relation.service.nlpCases.relationMapping.RelationMapping;
import dataelementhub.relation.service.restClientService.RestClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class SimilarityController {
    @Autowired
    private AIClient aiClient;
    @Autowired
    private RestClientService restClient;
    @Autowired
    private EqualRanking equalRanking;
    @Autowired
    private EquivalentRanking equivalentRankingService;
    @Autowired
    private WiderRanking widerRanking;
    @Autowired
    private RelationMapping relationMapping;



    @PostMapping("/compare-elements")
    public ResponseEntity<SimilarityResponse> compareDataElements(
            @RequestBody SimilarityRequest request
    ) {
        Map<String, List<SimilarityResult>> similarities =
                aiClient.getSimilaritiesManyToMany(
                        request.getText1_list(),
                        request.getText2_list()
                );

        SimilarityResponse response = new SimilarityResponse();
        response.setSimilarities(similarities);

        return ResponseEntity.ok(response);
    }


    //TODO: Need improvement in single search for relation
//    @PostMapping("/rank")
//    public List<ScoredElements> rank(@RequestBody RankRequest request) {
//        DataElementSummary a = restClient.getDataElementWithValueDomainTyp(request.getElementAUrn());
//        List<DataElementSummary> bList = restClient.getDataElementsByNamespace(request.getSourceB());
//
//        String relationType = request.getRelationType();
//
//        return switch (relationType.toUpperCase()) {
//            case "EQUIVALENT" -> equivalentRankingService.sortingEquivalent(a, bList, relationType);
//            case "EQUAL" -> equalRanking.rank(a, bList, relationType);
//            case "WIDER" -> widerRanking.getWiderRanking(a, bList, relationType);
//            default -> throw new IllegalArgumentException("Unknown relation type: " + relationType);
//        };
//    }

    @PostMapping("/suggestionRelation")
    public List<RelationSuggestion> suggestionsRelation(@RequestBody OneToManyRequest request) {
        DataElementSummary a = restClient.getDataElementWithValueDomainTyp(request.getElementAUrn());
        List<DataElementSummary> bList = restClient.getDataElementsByNamespace(request.getSourceB());
        return relationMapping.relationMappingOneToMany(a,bList, "relation");
    }

    @PostMapping("/compareUsingNlpManyToMany")
    public List<RelationSuggestion> compareUsingNLPManyToMany(@RequestBody ManyToManyRequest request) {
        List<DataElementSummary> a = restClient.getDataElementsByNamespace(request.getSource_a());
        List<DataElementSummary> bList = restClient.getDataElementsByNamespace(request.getSource_b());
        return relationMapping.relationMappingManyToMany(a,bList, "relation");
    }

    @PostMapping("/compareUsingLLM")
    public List<LLMResult> compareUsingLLM(@RequestBody OneToManyRequest request) {
        DataElementSummary a = restClient.getDataElementWithValueDomainTyp(request.getElementAUrn());
        List<DataElementSummary> bList = restClient.getDataElementsByNamespace(request.getSourceB());

        return aiClient.getLLMRelation(a, bList);
    }

    @PostMapping("/compareUsingLlmForManyToMany")
    public List<LLMResult> compareUsingLLM(@RequestBody ManyToManyRequest request) {
        List<DataElementSummary> a = restClient.getDataElementsByNamespace(request.getSource_a());
        List<DataElementSummary> bList = restClient.getDataElementsByNamespace(request.getSource_b());

        return aiClient.getLLMRelationManyToMany(a, bList);
    }
}
