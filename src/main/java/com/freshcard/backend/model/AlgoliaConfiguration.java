package com.freshcard.backend.model;

import com.wordnik.swagger.annotations.ApiModel;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;

/**
 * Created by willy on 13.04.14.
 */
@XmlRootElement
@ApiModel()
public class AlgoliaConfiguration implements Serializable {
    String algoliaApplicationID;
    String algoliaSearchKey;

    public String getAlgoliaApplicationID() {
        return algoliaApplicationID;
    }

    public void setAlgoliaApplicationID(String algoliaApplicationID) {
        this.algoliaApplicationID = algoliaApplicationID;
    }

    public String getAlgoliaSearchKey() {
        return algoliaSearchKey;
    }

    public void setAlgoliaSearchKey(String algoliaSearchKey) {
        this.algoliaSearchKey = algoliaSearchKey;
    }
}
