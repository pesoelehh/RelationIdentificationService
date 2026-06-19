package dataelementhub.relation.model.scoredElement;

import lombok.Data;

import java.io.Serializable;

@Data
public class RankRequest implements Serializable {
    private String elementAUrn;
    private int sourceB;
    private String relationType;
}