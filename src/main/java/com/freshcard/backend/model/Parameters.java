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
public class Parameters implements Serializable {
    Integer userId;
    Integer otherUserId;
    String username;
    String password;
    String otherUsername;
    Integer organizationId;
    Integer otherContactId;
    Integer contactId;
    String vcard;
    List<String> vcards;
    Boolean isTestRun;
    Integer offset;
    String recipientEMailAddress;
    String hashCode;
    String connectionHashCode;
    String format;
    String notes;
    Long updatedOffline;
    Long addedAt;
    Long connectionsLastUpdated;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getOtherUserId() {
        return otherUserId;
    }

    public void setOtherUserId(Integer otherUserId) {
        this.otherUserId = otherUserId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getOtherUsername() {
        return otherUsername;
    }

    public void setOtherUsername(String otherUsername) {
        this.otherUsername = otherUsername;
    }

    public Integer getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Integer organizationId) {
        this.organizationId = organizationId;
    }

    public Integer getOtherContactId() {
        return otherContactId;
    }

    public void setOtherContactId(Integer otherContactId) {
        this.otherContactId = otherContactId;
    }

    public String getVcard() {
        return vcard;
    }

    public void setVcard(String vcard) {
        this.vcard = vcard;
    }

    public Boolean getIsTestRun() {
        return isTestRun;
    }

    public void setIsTestRun(Boolean isTestRun) {
        this.isTestRun = isTestRun;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public String getRecipientEMailAddress() {
        return recipientEMailAddress;
    }

    public void setRecipientEMailAddress(String recipientEMailAddress) {
        this.recipientEMailAddress = recipientEMailAddress;
    }

    public String getHashCode() {
        return hashCode;
    }

    public void setHashCode(String hashCode) {
        this.hashCode = hashCode;
    }

    public String getConnectionHashCode() {
        return connectionHashCode;
    }

    public void setConnectionHashCode(String connectionHashCode) {
        this.connectionHashCode = connectionHashCode;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Long getUpdatedOffline() {
        return updatedOffline;
    }

    public void setUpdatedOffline(Long updatedOffline) {
        this.updatedOffline = updatedOffline;
    }

    public Long getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(Long addedAt) {
        this.addedAt = addedAt;
    }

    public Long getConnectionsLastUpdated() {
        return connectionsLastUpdated;
    }

    public void setConnectionsLastUpdated(Long connectionsLastUpdated) {
        this.connectionsLastUpdated = connectionsLastUpdated;
    }

    public List<String> getVcards() {
        return vcards;
    }

    public void setVcards(List<String> vcards) {
        this.vcards = vcards;
    }

    public Integer getContactId() {
        return contactId;
    }

    public void setContactId(Integer contactId) {
        this.contactId = contactId;
    }
}
