package dataelementhub.relation.model.aiModel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TextItem implements Serializable {
    private String designation;
    private String definition;
}
