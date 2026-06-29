package dataelementhub.relation.model.aiModel;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
/**
 * Request object used for NLP-based similarity and relation analysis.

 * This DTO encapsulates the input data sent to the external AI service.
 * Depending on the analysis mode, the request can contain either a
 * single reference element or multiple reference elements together
 * with a list of candidate elements.
 */
@JsonInclude(NON_NULL)
@Data
public class SimilarityRequest implements Serializable {
    private TextItem text1;
    private List<TextItem> text1_list;
    private List<TextItem> text2_list;
}
