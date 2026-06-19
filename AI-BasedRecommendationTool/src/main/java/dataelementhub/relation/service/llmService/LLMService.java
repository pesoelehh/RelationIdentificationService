package dataelementhub.relation.service.llmService;

import dataelementhub.relation.model.aiModel.llmDto.LLMComparisonRequest;
import dataelementhub.relation.model.aiModel.llmDto.LLMDataelement;
import dataelementhub.relation.model.aiModel.llmDto.LLMDataelementCompareTo;
import dataelementhub.relation.model.dataElement.compact.DataElementSummary;
import dataelementhub.relation.model.scoredElement.relationMapping.ComparisonResult;
import dataelementhub.relation.service.nlpCases.relationMapping.DataElementComparator;
import dataelementhub.relation.util.PreProcessingText;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LLMService {
    public LLMComparisonRequest getLLMComparisonRequest(DataElementSummary source_a, List<DataElementSummary> source_b) {
        LLMComparisonRequest request = new LLMComparisonRequest();

        // initialize lists
        request.setSource_a(new ArrayList<>());
        request.setSource_b(new ArrayList<>());

        // --- Normalize A ---
        DataElementSummary normalizedA =
                PreProcessingText.preprocessing(source_a);

        // --- Build Source A ---
        LLMDataelement a = new LLMDataelement();
        a.setId(normalizedA.getIdentification().getUrn());
        a.setDesignation(normalizedA.getDefinitions().get(0).getDesignation());
        a.setDefinition(normalizedA.getDefinitions().get(0).getDefinition());
        a.setDatatype(normalizedA.getValueDomainType());

        request.getSource_a().add(a);

        // --- Build Source B ---
        for (DataElementSummary normalizedB :
                source_b.stream()
                        .map(PreProcessingText::preprocessing)
                        .toList()) {

            ComparisonResult comp =
                    DataElementComparator.compare(normalizedA, normalizedB);

            // Build B element
            LLMDataelement bElement = new LLMDataelement();
            bElement.setId(normalizedB.getIdentification().getUrn());
            bElement.setDesignation(normalizedB.getDefinitions().get(0).getDesignation());
            bElement.setDefinition(normalizedB.getDefinitions().get(0).getDefinition());
            bElement.setDatatype(normalizedB.getValueDomainType());

            // Wrap B into compare-to object
            LLMDataelementCompareTo target =
                    new LLMDataelementCompareTo();

            target.setElement(bElement);
            target.setComparisonResult(comp);


            request.getSource_b().add(target);
        }
        return request;
    }

    public LLMComparisonRequest getLLMComparisonForManyToMany(
            List<DataElementSummary> source_a,
            List<DataElementSummary> source_b
    ) {

        LLMComparisonRequest request = new LLMComparisonRequest();
        request.setSource_a(new ArrayList<>());
        request.setSource_b(new ArrayList<>());

        for (DataElementSummary eachA : source_a) {

            DataElementSummary normalizedA =
                    PreProcessingText.preprocessing(eachA);

            LLMDataelement a = new LLMDataelement();
            a.setId(normalizedA.getIdentification().getUrn());
            a.setDesignation(normalizedA.getDefinitions().get(0).getDesignation());
            a.setDefinition(normalizedA.getDefinitions().get(0).getDefinition());
            a.setDatatype(normalizedA.getValueDomainType());

            request.getSource_a().add(a);
        }

        // B side built once
        if (!source_a.isEmpty()) {
            request.getSource_b().addAll(
                    getLLMComparisonRequest(
                            source_a.get(0), source_b
                    ).getSource_b()
            );
        }

        return request;
    }

}
