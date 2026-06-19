package dataelementhub.relation.service.aiService;

import dataelementhub.relation.service.llmService.LLMService;
import dataelementhub.relation.model.aiModel.*;
import dataelementhub.relation.model.aiModel.llmDto.*;
import dataelementhub.relation.model.dataElement.compact.DataElementSummary;
import dataelementhub.relation.model.scoredElement.NliResult;
import dataelementhub.relation.util.Result_CreateCSV;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AIClient {
    @Autowired
    private LLMService llmService;

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${ai.service.url}")
    private String aiBaseUrl;
    //
    // Call similarities scoring endpoint
    //
    public List<SimilarityResult> getSimilaritiesOneToMany(TextItem text1, List<TextItem> text2List) {
        // Build request using your model
        SimilarityRequest request = new SimilarityRequest();
        request.setText1(text1);
        request.setText2_list(text2List);

        // Send request and map directly to your response model
        SimilarityResponse response = restTemplate.postForObject(
                aiBaseUrl + "/similarities/one-to-many",
                request,
                SimilarityResponse.class
        );

        if (response == null || response.getSimilarities() == null) {
            return List.of();
        }

        String key = text1.getDesignation() + " - " + text1.getDefinition();

        return response.getSimilarities()
                .getOrDefault(key, List.of());
    }

    public Map<String, List<SimilarityResult>> getSimilaritiesManyToMany(
            List<TextItem> text1List,
            List<TextItem> text2List
    ) {
        SimilarityRequest request = new SimilarityRequest();
        request.setText1_list(text1List);
        request.setText2_list(text2List);

        SimilarityResponse response = restTemplate.postForObject(
                aiBaseUrl + "/similarities/many-to-many",
                request,
                SimilarityResponse.class
        );

        if (response == null || response.getSimilarities() == null) {
            return Map.of();
        }

        return response.getSimilarities();
    }


//    //
//    // Call wider scoring endpoint
//    // TODO: need be adjust (ranking for wider or narrower)
//    //
//    public List<Double> getWiderScores(TextItem text1, List<TextItem> text2List) {
//        SimilarityRequest request = new SimilarityRequest();
//        request.setText1(text1);
//        request.setText2_list(text2List);
//
//        SimilarityResponse response = restTemplate.postForObject(
//                aiBaseUrl + "/widersorting",
//                request,
//                SimilarityResponse.class
//        );
//
//        return response != null ? response.getSimilarities() : List.of();
//    }

    public List<NliResult> getNLNRelationOneToMany(TextItem text1, List<TextItem> text2List) {
        long start = System.nanoTime();

        SimilarityRequest request = new SimilarityRequest();
        request.setText1(text1);
        request.setText2_list(text2List);

        WiderSortingResponse response = restTemplate.postForObject(
                aiBaseUrl + "/relationUsingNlp/one-to-many",
                request,
                WiderSortingResponse.class
        );

        long end = System.nanoTime();
        double seconds = (end - start) / 1_000_000_000.0;
        System.out.println("NLP call took: " + seconds + " seconds");

        return response != null ? response.getResults() : List.of();
    }

    public List<NliResult> getNLNRelationManyToMany(List<TextItem> text1, List<TextItem> text2List) {
        long start = System.nanoTime();

        SimilarityRequest request = new SimilarityRequest();
        request.setText1_list(text1);
        request.setText2_list(text2List);

        WiderSortingResponse response = restTemplate.postForObject(
                aiBaseUrl + "/relationUsingNlp/many-to-many",
                request,
                WiderSortingResponse.class
        );

        long end = System.nanoTime();
        double seconds = (end - start) / 1_000_000_000.0;

        System.out.printf("NLP many-to-many call took: %.3f seconds%n", seconds);

        return response != null ? response.getResults() : List.of();
    }

    public List<LLMResult> getLLMRelation(DataElementSummary source_a, List<DataElementSummary> source_b) {
        long start = System.nanoTime();

        LLMComparisonRequest request = llmService.getLLMComparisonRequest(source_a, source_b);

        // --- Call Python API ---
        LLMResponse response = restTemplate.postForObject(
                aiBaseUrl + "/compare/oneToMany",
                request,
                LLMResponse.class
        );

        assert response != null;
        long end = System.nanoTime();
        double seconds = (end - start) / 1_000_000_000.0;
        System.out.println("LLM call took: " + seconds+ " seconds");
        new Result_CreateCSV().writeLLMResultCsv(response.getResults());
        return response.getResults();
    }

    public List<LLMResult> getLLMRelationManyToMany(List<DataElementSummary> source_a, List<DataElementSummary> source_b) {
        long start = System.nanoTime();

        LLMComparisonRequest request = llmService.getLLMComparisonForManyToMany(source_a, source_b);

        // --- Call Python API ---
        LLMResponse response = restTemplate.postForObject(
                aiBaseUrl + "/compare/manyToMany",
                request,
                LLMResponse.class
        );

        assert response != null;
        long end = System.nanoTime();
        double seconds = (end - start) / 1_000_000_000.0;
        System.out.println("LLM call took: " + seconds + " seconds");
        new Result_CreateCSV().writeLLMResultCsv(response.getResults());
        return response.getResults();
    }
}
