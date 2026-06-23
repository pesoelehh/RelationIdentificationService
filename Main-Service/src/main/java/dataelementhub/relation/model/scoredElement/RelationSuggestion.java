package dataelementhub.relation.model.scoredElement;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import dataelementhub.relation.model.dataElement.compact.DataElementSummary;
import dataelementhub.relation.model.scoredElement.relationMapping.ComparisonResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Central domain object representing a potential relation between two Data Elements.

 * The object aggregates:
 * - Structural comparison results
 * - NLP-based relation analysis results
 * - Rule-based relation classification results

 * It serves as the primary fact object processed by the Drools rule engine.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder({
        "textA",
        "textB",
        "comparisonResult",
        "relationAnalysisResult",
        "relationType",
        "matchQuality"
})
public class RelationSuggestion implements Serializable {

    // -------- INTERNAL FIELDS (hidden from JSON) --------
    @JsonIgnore
    private DataElementSummary textA;

    @JsonIgnore
    private DataElementSummary textB;


    @JsonGetter("textA")
    public List<?> getTextADefinitions() {
        return textA != null ? textA.getDefinitions() : null;
    }

    @JsonGetter("textB")
    public List<?> getTextBDefinitions() {
        return textB != null ? textB.getDefinitions() : null;
    }

    // -------- SHOWN AS-IS --------
    private ComparisonResult comparisonResult;
    private RelationAnalysisResult relationAnalysisResult;
    private String relationType;

    @JsonIgnore
    public String getLanguageA() {
        if (textA == null || textA.getDefinitions() == null || textA.getDefinitions().isEmpty()) {
            return null;
        }
        return textA.getDefinitions().get(0).getLanguage();
    }

    @JsonIgnore
    public String getLanguageB() {
        if (textB == null || textB.getDefinitions() == null || textB.getDefinitions().isEmpty()) {
            return null;
        }
        return textB.getDefinitions().get(0).getLanguage();
    }

    public boolean isSameLanguage() {
        return getLanguageA() != null
                && getLanguageA().equalsIgnoreCase(getLanguageB());
    }
}
