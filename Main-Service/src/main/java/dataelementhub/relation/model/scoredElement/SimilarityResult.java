package dataelementhub.relation.model.scoredElement;

import dataelementhub.relation.model.aiModel.TextItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
/**
 * Stores the similarity score of two text items.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SimilarityResult implements Serializable {
    private TextItem textA;
    private TextItem textB;
    private double score;
}
