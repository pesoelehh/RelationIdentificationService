package dataelementhub.relation.model.dataElement.compact;

import dataelementhub.relation.model.dataElement.ConceptAssociations;
import dataelementhub.relation.model.dataElement.Definitions;
import dataelementhub.relation.model.dataElement.Identification;
import dataelementhub.relation.model.dataElement.type.Datetime;
import dataelementhub.relation.model.dataElement.type.Numeric;
import dataelementhub.relation.model.dataElement.type.PermittedValue;
import dataelementhub.relation.model.dataElement.type.Text;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
/**
 * Compact Data Element representation used by the relation mapping workflow.

 * The object aggregates metadata, definitions, datatype information,
 * and value domain attributes required for NLP-based relation analysis
 * and structural comparison.
 */
@Data
public class DataElementSummary implements Serializable {
    private Identification identification;
    private List<Definitions> definitions;
    private String valueDomainType;
    private Text text;
    private Numeric numeric;
    private Datetime datetime;
    private List<PermittedValue> permittedValues;
    private List<ConceptAssociations> conceptAssociations;
}
