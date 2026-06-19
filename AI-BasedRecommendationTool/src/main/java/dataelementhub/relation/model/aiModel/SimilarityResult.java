package dataelementhub.relation.model.aiModel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SimilarityResult implements Serializable {
    private String text2;
    private double score;
}
