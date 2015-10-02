package com.freshcard.backend.model;

import com.wordnik.swagger.annotations.ApiModel;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * Created by willy on 13.04.14.
 */
@XmlRootElement
@ApiModel()
public class Status implements Serializable {
    String message;

    public Status() {
    }

    public Status(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
