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

/**
 * Service responsible for retrieving Data Element information from the
 * external Data Element Hub REST API.
 *
 * This component acts as the integration layer between the relation
 * analysis service and the external metadata repository. It provides
 * methods for retrieving namespaces, data elements, and value domain
 * information required during relation mapping and similarity analysis.
 */
@Service
public class RestClientService {
    // HTTP client used to communicate with the external REST service.
    private final RestTemplate restTemplate = new RestTemplate();

    // Base URL of the external Data Element Hub API.
    @Value("${restproject.baseurl}")
    private String BASE_URL;

    /**
     * Retrieves all available namespaces from the external repository.
     *
     * Only namespaces accessible through the READ permission group are
     * returned and used as potential sources for relation analysis.
     *
     * @return List of available namespaces.
     */
    public List<Namespace> getNamespaces() {
        // Request namespace information from the external API.
        String url = BASE_URL + "/namespaces";

        // Use exchange with ParameterizedTypeReference to extract the list directly
        ResponseEntity<Map<String, List<Namespace>>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        Map<String, List<Namespace>> body = response.getBody();

        // Extract namespaces belonging to the READ category.
        if (body != null && body.containsKey("READ")) {
            return body.get("READ");
        }

        return Collections.emptyList();
    }

    /**
     * Retrieves all eligible Data Elements within a namespace.
     *
     * For each namespace member, detailed metadata and value domain
     * information are loaded. Only Data Elements with status
     * DRAFT or RELEASED are considered for further processing.
     *
     * @param namespaceIdentifier Unique namespace identifier.
     * @return List of DataElementSummary objects.
     */
    public List<DataElementSummary> getDataElementsByNamespace(int namespaceIdentifier) {
        // Retrieve all Data Element references belonging to the namespace.
        String url = BASE_URL + "/namespaces/" + namespaceIdentifier + "/members?elementType=DATAELEMENT";
        NamespaceMembers[] summaries = restTemplate.getForObject(url, NamespaceMembers[].class);

        // Load detailed metadata for each referenced Data Element.
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

    /**
     * Retrieves the metadata of a single Data Element.
     *
     * @param elementUrn Unique Data Element identifier.
     * @return Complete DataElement metadata.
     */
    public DataElement getDataElementByUrn(String elementUrn) {
        String url = BASE_URL + "/element/" + elementUrn;
        return restTemplate.getForObject(url, DataElement.class);
    }

    /**
     * Retrieves the value domain definition of a Data Element.
     *
     * The value domain contains datatype-specific information such as
     * text constraints, numeric ranges, datetime formats, or
     * permitted values.
     *
     * @param elementUrn Unique Data Element identifier.
     * @return ValueDomain associated with the Data Element.
     */
    public ValueDomain getDataElementValueDomainByUrn(String elementUrn) {
        String url = BASE_URL + "/element/" + elementUrn + "/valuedomain";
        return restTemplate.getForObject(url, ValueDomain.class);
    }

    /**
     * Creates a compact DataElementSummary containing both metadata
     * and value domain information.
     *
     * This method combines information retrieved from the Data Element
     * endpoint and the Value Domain endpoint into a single object that
     * can be processed by the relation analysis workflow.
     *
     * Depending on the value domain type, the corresponding datatype
     * attributes are populated within the summary object.
     *
     * @param elementUrn Unique Data Element identifier.
     * @return Enriched DataElementSummary instance.
     */
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
