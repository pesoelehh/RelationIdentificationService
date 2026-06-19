package dataelementhub.relation.controller.restController;

import dataelementhub.relation.model.dataElement.DataElement;
import dataelementhub.relation.model.dataElement.compact.DataElementSummary;
import dataelementhub.relation.model.dataElement.Namespace;
import dataelementhub.relation.model.dataElement.ValueDomain;
import dataelementhub.relation.service.restClientService.RestClientService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/relation")
public class RelationController {

    private final RestClientService restClientService;

    public RelationController(RestClientService restClientService) {
        this.restClientService = restClientService;
    }


    @GetMapping("/namespaces")
    public List<Namespace> getNamespaces() {
        return restClientService.getNamespaces();
    }
    @GetMapping("/namespace/{namespaceIdentifier}")
    public List<DataElementSummary> getElementsFromNamespace(@PathVariable int namespaceIdentifier) {
        return restClientService.getDataElementsByNamespace(namespaceIdentifier);
    }

    @GetMapping("/element/{urn}")
    public DataElement getElement(@PathVariable String urn) {
        return restClientService.getDataElementByUrn(urn);
    }

    @GetMapping("/valueDomainValue/{urn}")
    public ValueDomain getValueDomainValues(@PathVariable String urn) {
        return restClientService.getDataElementValueDomainByUrn(urn);
    }

    @GetMapping("/elementsummary/{urn}")
    public DataElementSummary getElementSummary(@PathVariable String urn) {
        return restClientService.getDataElementWithValueDomainTyp(urn);
    }

}
