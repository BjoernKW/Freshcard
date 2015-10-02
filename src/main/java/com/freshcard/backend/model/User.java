package com.freshcard.backend.model;

import com.wordnik.swagger.annotations.ApiModel;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.*;

/**
 * Created by willy on 13.04.14.
 */
@XmlRootElement
@ApiModel()
public class User implements Serializable, UserDetails {
    public static String GITHUB_SERVICE_NAME = "GitHub";
    public static String LINKEDIN_SERVICE_NAME = "LinkedIn";
    public static String TWITTER_SERVICE_NAME = "Twitter";
    public static String XING_SERVICE_NAME = "XING";

    Integer id;
    String username;
    String password;
    Boolean enabled;
    String hashCode;
    Organization currentOrganization;
    Set<String> roles = new HashSet<String>();
    List<Contact> ownContacts = new ArrayList<Contact>();
    List<Contact> connectionContacts = new ArrayList<Contact>();
    List<Organization> organizations = new ArrayList<Organization>();
    String preferredLanguage;
    String confirmationHashCode;
    Boolean confirmed = false;
    Boolean useOAuth = false;
    String mostRecentlyUsedOAuthService;
    String gitHubAccessToken;
    String linkedInAccessToken;
    String twitterAccessToken;
    String xingAccessToken;
    String accessToken;
    Timestamp updatedAt;
    Timestamp synchronizedAt;
    Boolean temporaryUser = false;
    String customSignature;
    String profilePicturePath;
    Timestamp connectionsLastUpdated;
    String searchPublicKey;
    String searchPublicKeyForCoWorkers;
    String newUsername;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getHashCode() {
        return hashCode;
    }

    public void setHashCode(String hashCode) {
        this.hashCode = hashCode;
    }

    public Organization getCurrentOrganization() {
        return currentOrganization;
    }

    public void setCurrentOrganization(Organization currentOrganization) {
        this.currentOrganization = currentOrganization;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public boolean isAccountNonExpired() {
        return true;
    }

    public boolean isAccountNonLocked() {
        return true;
    }

    public boolean isCredentialsNonExpired() {
        return true;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<String> roles = this.getRoles();

        if (roles == null) {
            return Collections.emptyList();
        }

        Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority(role));
        }

        return authorities;
    }

    public List<Contact> getOwnContacts() {
        return ownContacts;
    }

    public void setOwnContacts(List<Contact> ownContacts) {
        this.ownContacts = ownContacts;
    }

    public List<Contact> getConnectionContacts() {
        return connectionContacts;
    }

    public void setConnectionContacts(List<Contact> connectionContacts) {
        this.connectionContacts = connectionContacts;
    }

    public List<Organization> getOrganizations() {
        return organizations;
    }

    public void setOrganizations(List<Organization> organizations) {
        this.organizations = organizations;
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    public String getConfirmationHashCode() {
        return confirmationHashCode;
    }

    public void setConfirmationHashCode(String confirmationHashCode) {
        this.confirmationHashCode = confirmationHashCode;
    }

    public Boolean getConfirmed() {
        return confirmed;
    }

    public void setConfirmed(Boolean confirmed) {
        this.confirmed = confirmed;
    }

    public Boolean getUseOAuth() {
        return useOAuth;
    }

    public void setUseOAuth(Boolean useOAuth) {
        this.useOAuth = useOAuth;
    }

    public String getMostRecentlyUsedOAuthService() {
        return mostRecentlyUsedOAuthService;
    }

    public void setMostRecentlyUsedOAuthService(String mostRecentlyUsedOAuthService) {
        this.mostRecentlyUsedOAuthService = mostRecentlyUsedOAuthService;
    }

    public String getGitHubAccessToken() {
        return gitHubAccessToken;
    }

    public void setGitHubAccessToken(String gitHubAccessToken) {
        this.gitHubAccessToken = gitHubAccessToken;
    }

    public String getLinkedInAccessToken() {
        return linkedInAccessToken;
    }

    public void setLinkedInAccessToken(String linkedInAccessToken) {
        this.linkedInAccessToken = linkedInAccessToken;
    }

    public String getTwitterAccessToken() {
        return twitterAccessToken;
    }

    public void setTwitterAccessToken(String twitterAccessToken) {
        this.twitterAccessToken = twitterAccessToken;
    }

    public String getXingAccessToken() {
        return xingAccessToken;
    }

    public void setXingAccessToken(String xingAccessToken) {
        this.xingAccessToken = xingAccessToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Timestamp getSynchronizedAt() {
        return synchronizedAt;
    }

    public void setSynchronizedAt(Timestamp synchronizedAt) {
        this.synchronizedAt = synchronizedAt;
    }

    public Boolean getTemporaryUser() {
        return temporaryUser;
    }

    public void setTemporaryUser(Boolean temporaryUser) {
        this.temporaryUser = temporaryUser;
    }

    public String getCustomSignature() {
        return customSignature;
    }

    public void setCustomSignature(String customSignature) {
        this.customSignature = customSignature;
    }

    public String getProfilePicturePath() {
        return profilePicturePath;
    }

    public void setProfilePicturePath(String profilePicturePath) {
        this.profilePicturePath = profilePicturePath;
    }

    public Timestamp getConnectionsLastUpdated() {
        return connectionsLastUpdated;
    }

    public void setConnectionsLastUpdated(Timestamp connectionsLastUpdated) {
        this.connectionsLastUpdated = connectionsLastUpdated;
    }

    public String getSearchPublicKey() {
        return searchPublicKey;
    }

    public void setSearchPublicKey(String searchPublicKey) {
        this.searchPublicKey = searchPublicKey;
    }

    public String getSearchPublicKeyForCoWorkers() {
        return searchPublicKeyForCoWorkers;
    }

    public void setSearchPublicKeyForCoWorkers(String searchPublicKeyForCoWorkers) {
        this.searchPublicKeyForCoWorkers = searchPublicKeyForCoWorkers;
    }

    public String getNewUsername() {
        return newUsername;
    }

    public void setNewUsername(String newUsername) {
        this.newUsername = newUsername;
    }
}
