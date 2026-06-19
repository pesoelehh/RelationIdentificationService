package dataelementhub.relation.model.scoredElement;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NliResult implements Serializable {

    @JsonProperty("reference_designation")
    private String referenceDesignation;

    private String designation;

    private String definition;

    @JsonProperty("similarity_score")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal similarityScore;

    @JsonProperty("nli_relation")
    private String nliRelation;

    @JsonProperty("nli_confidence")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal nliConfidence;

    @JsonProperty("wider_score")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal widerScore;

    @JsonProperty("specific_score")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal specificScore;
}

