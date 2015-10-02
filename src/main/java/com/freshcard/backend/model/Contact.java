package com.freshcard.backend.model;

import com.wordnik.swagger.annotations.ApiModel;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Created by willy on 13.04.14.
 */
@XmlRootElement
@ApiModel()
public class Contact implements Serializable {
    Integer id;
    String vcard;
    String hashCode;
    Timestamp updatedAt;
    Timestamp updatedOffline;
    Timestamp addedAt;
    Timestamp joinedUpdatedAt;
    String notes;
    String tags;
    Integer total;
    Integer coWorkerUserId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getVcard() {
        return vcard;
    }

    public void setVcard(String vcard) {
        this.vcard = vcard;
    }

    public String getHashCode() {
        return hashCode;
    }

    public void setHashCode(String hashCode) {
        this.hashCode = hashCode;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Timestamp getUpdatedOffline() {
        return updatedOffline;
    }

    public void setUpdatedOffline(Timestamp updatedOffline) {
        this.updatedOffline = updatedOffline;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Timestamp getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(Timestamp addedAt) {
        this.addedAt = addedAt;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public Timestamp getJoinedUpdatedAt() {
        return joinedUpdatedAt;
    }

    public void setJoinedUpdatedAt(Timestamp joinedUpdatedAt) {
        this.joinedUpdatedAt = joinedUpdatedAt;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Integer getCoWorkerUserId() {
        return coWorkerUserId;
    }

    public void setCoWorkerUserId(Integer coWorkerUserId) {
        this.coWorkerUserId = coWorkerUserId;
    }
}
