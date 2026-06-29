package dataelementhub.relation.model.aiModel;

import dataelementhub.relation.model.scoredElement.RelationAnalysisResult;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Response object returned by the NLP relation analysis service.
 *
 * The response contains the generated relation analysis results,
 * including similarity scores and inferred semantic relationships
 * between Data Elements.
 */
@Data
public class NlpRelationResponse implements Serializable {
    List<RelationAnalysisResult> results;
}
