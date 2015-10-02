package com.freshcard.backend.security;

import com.freshcard.backend.model.Contact;
import com.freshcard.backend.model.Organization;

import java.util.List;
import java.util.Map;

/**
 * Created by willy on 18.08.14.
 */
public class UserTransfer {
    private final Integer id;

    private final String name;

    private final Map<String, Boolean> roles;

    private final List<Organization> organizations;

    private final List<Contact> ownContacts;

    private final Integer currentOrganizationId;

    private String hashCode;

    private Boolean confirmed;

    private String customSignature;

    private String profilePicturePath;

    private String searchPublicKey;

    private String searchPublicKeyForCoWorkers;

    public UserTransfer(
            Integer id,
            String userName,
            Map<String, Boolean> roles,
            List<Organization> organizations,
            Integer currentOrganizationId,
            List<Contact> contacts,
            String hashCode,
            Boolean confirmed,
            String customSignature,
            String profilePicturePath,
            String searchPublicKey,
            String searchPublicKeyForCoWorkers
    ) {
        this.id = id;
        this.name = userName;
        this.roles = roles;
        this.organizations = organizations;
        this.currentOrganizationId = currentOrganizationId;
        this.ownContacts = contacts;
        this.hashCode = hashCode;
        this.confirmed = confirmed;
        this.customSignature = customSignature;
        this.profilePicturePath = profilePicturePath;
        this.searchPublicKey = searchPublicKey;
        this.searchPublicKeyForCoWorkers = searchPublicKeyForCoWorkers;
    }

    public Integer getId() {
        return id;
    }

    public String getName()
    {
        return this.name;
    }

    public Map<String, Boolean> getRoles()
    {
        return this.roles;
    }

    public List<Organization> getOrganizations() {
        return organizations;
    }

    public Integer getCurrentOrganizationId() {
        return currentOrganizationId;
    }

    public List<Contact> getOwnContacts() {
        return ownContacts;
    }

    public String getHashCode() {
        return hashCode;
    }

    public Boolean getConfirmed() {
        return confirmed;
    }

    public String getCustomSignature() {
        return customSignature;
    }

    public String getProfilePicturePath() {
        return profilePicturePath;
    }

    public String getSearchPublicKey() {
        return searchPublicKey;
    }

    public String getSearchPublicKeyForCoWorkers() {
        return searchPublicKeyForCoWorkers;
    }
}
