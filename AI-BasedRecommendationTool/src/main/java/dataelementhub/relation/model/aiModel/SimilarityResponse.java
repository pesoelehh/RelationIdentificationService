package dataelementhub.relation.model.aiModel;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class SimilarityResponse implements Serializable {
    private Map<String, List<SimilarityResult>> similarities;
}
