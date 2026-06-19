package dataelementhub.relation.model.dataElement.type;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class NumericInteger extends Numeric implements Serializable {

    private Long minimum;
    private Long maximum;

    public NumericInteger() {
        this.setType(Numeric.TYPE_INTEGER);
    }
}
