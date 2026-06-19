package dataelementhub.relation.model.aiModel;

import dataelementhub.relation.model.scoredElement.NliResult;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class WiderSortingResponse implements Serializable {
    List<NliResult> results;
}
