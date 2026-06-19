package dataelementhub.relation.model.aiModel;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonInclude(NON_NULL)
@Data
public class SimilarityRequest implements Serializable {
    private TextItem text1;
    private List<TextItem> text1_list;
    private List<TextItem> text2_list;
}
