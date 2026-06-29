package dataelementhub.relation.controller.aiController;

import dataelementhub.relation.model.dataElement.compact.DataElementSummary;
import dataelementhub.relation.model.request.ManyToManyRequest;
import dataelementhub.relation.model.scoredElement.RelationSuggestion;
import dataelementhub.relation.model.request.OneToManyRequest;
import dataelementhub.relation.service.nlpCases.relationMapping.RelationMapping;
import dataelementhub.relation.service.restClientService.RestClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller exposing NLP-based relation mapping endpoints.
 *
 * This controller serves as the entry point for relation analysis requests.
 * It retrieves the required Data Elements, invokes the relation mapping
 * service, and returns the generated relation suggestions to the client.
 *
 * Supported analysis modes:
 * - One-to-many relation mapping
 * - Many-to-many relation mapping
 */
@RestController
public class RelationMappingController {
    // Service used to retrieve Data Elements from the external repository.
    @Autowired
    private RestClientService restClient;
    // Service responsible for generating NLP-based relation suggestions.
    @Autowired
    private RelationMapping relationMapping;


    /**
     * Performs relation mapping between one Data Element and all Data Elements
     * contained in a target namespace.
     *
     * The source Data Element is identified by its URN, while the target
     * candidates are retrieved from the specified namespace. The resulting
     * relation suggestions are generated using NLP analysis and rule-based
     * evaluation.
     *
     * @param request Request containing the source Data Element and target namespace.
     * @return List of generated relation suggestions.
     */
    @PostMapping("/compareUsingNlpOneToMany")
    public List<RelationSuggestion> suggestionsRelation(@RequestBody OneToManyRequest request) {
        // Retrieve the source Data Element including its datatype information.
        DataElementSummary a = restClient.getDataElementWithValueDomainTyp(request.getElementAUrn());
        // Retrieve all candidate Data Elements from the target namespace.
        List<DataElementSummary> bList = restClient.getDataElementsByNamespace(request.getSourceB());
        // Execute the relation mapping workflow.
        return relationMapping.relationMappingOneToMany(a,bList, "relation");
    }

    /**
     * Performs relation mapping between all Data Elements of two namespaces.
     *
     * Every Data Element from the first namespace is compared against every
     * Data Element from the second namespace. The generated relation
     * suggestions are based on NLP analysis and rule-based classification.
     *
     * @param request Request containing the source and target namespaces.
     * @return List of generated relation suggestions.
     */
    @PostMapping("/compareUsingNlpManyToMany")
    public List<RelationSuggestion> compareUsingNLPManyToMany(@RequestBody ManyToManyRequest request) {
        // Retrieve all Data Elements from the source namespace.
        List<DataElementSummary> a = restClient.getDataElementsByNamespace(request.getSource_a());
        // Retrieve all Data Elements from the target namespace.
        List<DataElementSummary> bList = restClient.getDataElementsByNamespace(request.getSource_b());
        // Execute the many-to-many relation mapping workflow.
        return relationMapping.relationMappingManyToMany(a,bList, "relation");
    }

}
