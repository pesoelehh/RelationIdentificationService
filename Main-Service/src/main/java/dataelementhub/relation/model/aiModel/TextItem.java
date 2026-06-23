package dataelementhub.relation.model.aiModel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Lightweight text representation used as input for NLP analysis.

 * The object contains the designation and definition of a Data Element
 * and is exchanged between the relation service and the AI service.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TextItem implements Serializable {
    private String designation;
    private String definition;
}
