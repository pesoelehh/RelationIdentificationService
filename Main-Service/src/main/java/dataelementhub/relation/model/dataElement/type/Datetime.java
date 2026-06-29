package dataelementhub.relation.model.dataElement.type;

import lombok.Data;

import java.io.Serializable;

@Data
public class Datetime implements Serializable {
    private String date;
    private String time;
    private String hourFormat;
}

