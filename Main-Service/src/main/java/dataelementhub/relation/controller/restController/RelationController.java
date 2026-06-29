package dataelementhub.relation.controller.restController;

import dataelementhub.relation.model.dataElement.DataElement;
import dataelementhub.relation.model.dataElement.compact.DataElementSummary;
import dataelementhub.relation.model.dataElement.Namespace;
import dataelementhub.relation.model.dataElement.ValueDomain;
import dataelementhub.relation.service.restClientService.RestClientService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
/**
 * REST controller providing access to Data Element metadata.
 *
 * This controller exposes endpoints for retrieving namespaces,
 * Data Elements, value domain information, and aggregated
 * Data Element summaries. The provided information serves as
 * input for relation analysis and mapping processes.
 */
@RestController
@RequestMapping("/relation")
public class RelationController {
    // Service responsible for retrieving metadata from the external repository.
    private final RestClientService restClientService;

    /**
     * Creates a new controller instance.
     *
     * @param restClientService Service used to retrieve Data Element metadata.
     */
    public RelationController(RestClientService restClientService) {
        this.restClientService = restClientService;
    }

    /**
     * Retrieves all available namespaces.
     *
     * Namespaces represent logical collections of Data Elements
     * within the metadata repository.
     *
     * @return List of available namespaces.
     */
    @GetMapping("/namespaces")
    public List<Namespace> getNamespaces() {
        return restClientService.getNamespaces();
    }

    /**
     * Retrieves all Data Elements belonging to a namespace.
     *
     * The returned elements contain the metadata required for
     * relation analysis and comparison.
     *
     * @param namespaceIdentifier Unique namespace identifier.
     * @return List of Data Elements within the namespace.
     */
    @GetMapping("/namespace/{namespaceIdentifier}")
    public List<DataElementSummary> getElementsFromNamespace(@PathVariable int namespaceIdentifier) {
        return restClientService.getDataElementsByNamespace(namespaceIdentifier);
    }

    /**
     * Retrieves the complete metadata of a Data Element.
     *
     * @param urn Unique identifier of the Data Element.
     * @return Data Element metadata.
     */
    @GetMapping("/element/{urn}")
    public DataElement getElement(@PathVariable String urn) {
        return restClientService.getDataElementByUrn(urn);
    }

    /**
     * Retrieves the value domain definition of a Data Element.
     *
     * The value domain contains datatype-specific information such
     * as text constraints, numeric ranges, datetime formats,
     * or permitted values.
     *
     * @param urn Unique identifier of the Data Element.
     * @return Value domain information.
     */
    @GetMapping("/valueDomainValue/{urn}")
    public ValueDomain getValueDomainValues(@PathVariable String urn) {
        return restClientService.getDataElementValueDomainByUrn(urn);
    }

    /**
     * Retrieves a compact Data Element representation.
     *
     * This endpoint combines metadata and value domain information
     * into a single summary object that can be directly used by
     * the relation analysis workflow.
     *
     * @param urn Unique identifier of the Data Element.
     * @return Aggregated Data Element summary.
     */
    @GetMapping("/elementsummary/{urn}")
    public DataElementSummary getElementSummary(@PathVariable String urn) {
        return restClientService.getDataElementWithValueDomainTyp(urn);
    }

}
