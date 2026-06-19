package dataelementhub.relation.model.dataElement.type;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

import java.io.Serializable;


@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type" // or "@class" if you want full class names
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = NumericInteger.class, name = "INTEGER"),
        @JsonSubTypes.Type(value = NumericFloat.class, name = "FLOAT")
})
@Data
public abstract class Numeric implements Serializable {
    public static final String TYPE_INTEGER = "INTEGER";
    public static final String TYPE_FLOAT = "FLOAT";

    private String type;
    private Boolean useMinimum;
    private Boolean useMaximum;
    private String unitOfMeasure;
}
