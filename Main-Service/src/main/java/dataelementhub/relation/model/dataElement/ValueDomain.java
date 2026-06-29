package dataelementhub.relation.model.dataElement;

import dataelementhub.relation.model.dataElement.type.Datetime;
import dataelementhub.relation.model.dataElement.type.Numeric;
import dataelementhub.relation.model.dataElement.type.PermittedValue;
import dataelementhub.relation.model.dataElement.type.Text;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;


@EqualsAndHashCode(callSuper = true)
@Data
public class ValueDomain extends DataElement implements Serializable {

    public static final String TYPE_STRING = "STRING";
    public static final String TYPE_NUMERIC = "NUMERIC";
    public static final String TYPE_BOOLEAN = "BOOLEAN";
    public static final String TYPE_TBD = "TBD";
    public static final String TYPE_ENUMERATED = "ENUMERATED";
    public static final String TYPE_DATE = "DATE";
    public static final String TYPE_DATETIME = "DATETIME";
    public static final String TYPE_TIME = "TIME";
    private String type;
    private Text text;
    private Numeric numeric;
    private Datetime datetime;
    private List<PermittedValue> permittedValues;
}
