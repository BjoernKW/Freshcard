package com.freshcard.backend.model;

import com.wordnik.swagger.annotations.ApiModel;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by willy on 13.04.14.
 */
@XmlRootElement
@ApiModel()
public class Organization implements Serializable {
    Integer id;
    String name;
    String url;
    String hashCode;
    List<User> users = new ArrayList<User>();
    Timestamp updatedAt;
    String templateImagePath;
    String templateLayout;
    String searchPublicKey;
    String searchPublicKeyForCoWorkers;
    String templateAsSVG;
    String logoImagePath;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getHashCode() {
        return hashCode;
    }

    public void setHashCode(String hashCode) {
        this.hashCode = hashCode;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getTemplateImagePath() {
        return templateImagePath;
    }

    public void setTemplateImagePath(String templateImagePath) {
        this.templateImagePath = templateImagePath;
    }

    public String getTemplateLayout() {
        return templateLayout;
    }

    public void setTemplateLayout(String templateLayout) {
        this.templateLayout = templateLayout;
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

    public String getTemplateAsSVG() {
        return templateAsSVG;
    }

    public void setTemplateAsSVG(String templateAsSVG) {
        this.templateAsSVG = templateAsSVG;
    }

    public String getLogoImagePath() {
        return logoImagePath;
    }

    public void setLogoImagePath(String logoImagePath) {
        this.logoImagePath = logoImagePath;
    }
}
