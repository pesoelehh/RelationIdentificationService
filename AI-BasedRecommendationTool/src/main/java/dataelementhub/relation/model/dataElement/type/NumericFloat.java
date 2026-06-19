package dataelementhub.relation.model.dataElement.type;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class NumericFloat extends Numeric implements Serializable {
    private Double minimum;
    private Double maximum;

    public NumericFloat() {
        this.setType(Numeric.TYPE_FLOAT);
    }
}
