package dataelementhub.relation.service.restClientService;

import dataelementhub.relation.model.dataElement.*;
import dataelementhub.relation.model.dataElement.compact.DataElementSummary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class RestClientService {
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${restproject.baseurl}")
    private String BASE_URL;

    // Fetch urn list
    public List<Namespace> getNamespaces() {
        String url = BASE_URL + "/namespaces";

        // Use exchange with ParameterizedTypeReference to extract the list directly
        ResponseEntity<Map<String, List<Namespace>>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        Map<String, List<Namespace>> body = response.getBody();

        if (body != null && body.containsKey("READ")) {
            return body.get("READ");
        }

        return Collections.emptyList();
    }

    // Fetch list of data elements in a namespace
    public List<DataElementSummary> getDataElementsByNamespace(int namespaceIdentifier) {
        String url = BASE_URL + "/namespaces/" + namespaceIdentifier + "/members?elementType=DATAELEMENT";
        NamespaceMembers[] summaries = restTemplate.getForObject(url, NamespaceMembers[].class);

        // Step 2: Fetch full details by URN
        List<DataElementSummary> fullElements = new ArrayList<>();
        if (summaries != null) {
            for (NamespaceMembers summary : summaries) {
                DataElementSummary full = getDataElementWithValueDomainTyp(summary.getElementUrn());
                if (full != null
                        && full.getIdentification() != null
                        && (
                                "DRAFT".equalsIgnoreCase(full.getIdentification().getStatus())
                            || "RELEASED".equalsIgnoreCase(full.getIdentification().getStatus()))) {
                    fullElements.add(full);
                }
            }
        }

        return fullElements;
    }

    // Fetch detail of one data element
    public DataElement getDataElementByUrn(String elementUrn) {
        String url = BASE_URL + "/element/" + elementUrn;
        return restTemplate.getForObject(url, DataElement.class);
    }

    // Fetch detail of domain value
    public ValueDomain getDataElementValueDomainByUrn(String elementUrn) {
        String url = BASE_URL + "/element/" + elementUrn + "/valuedomain";
        return restTemplate.getForObject(url, ValueDomain.class);
    }

    public DataElementSummary getDataElementWithValueDomainTyp(String elementUrn) {
        // Fetch DataElement and ValueDomain
        DataElement dataElement = getDataElementByUrn(elementUrn);
        ValueDomain valueDomain = getDataElementValueDomainByUrn(elementUrn);

        DataElementSummary summary = new DataElementSummary();
        summary.setIdentification(dataElement.getIdentification());
        summary.setDefinitions(dataElement.getDefinitions());
        summary.setValueDomainType(valueDomain.getType());

        // Populate the corresponding value object based on type
        if (valueDomain.getType() != null) {
            switch (valueDomain.getType().toUpperCase()) {
                case ValueDomain.TYPE_STRING -> summary.setText(valueDomain.getText());
                case ValueDomain.TYPE_NUMERIC -> summary.setNumeric(valueDomain.getNumeric());
                case ValueDomain.TYPE_DATETIME, ValueDomain.TYPE_DATE, ValueDomain.TYPE_TIME -> summary.setDatetime(valueDomain.getDatetime());
                case ValueDomain.TYPE_BOOLEAN, ValueDomain.TYPE_ENUMERATED -> summary.setPermittedValues(valueDomain.getPermittedValues());
                // Catalog or other types can be handled if needed
                default -> { /* leave as null */ }
            }
        }

        return summary;
    }

}
