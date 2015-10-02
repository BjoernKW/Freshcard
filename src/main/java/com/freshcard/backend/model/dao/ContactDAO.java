package com.freshcard.backend.model.dao;

import com.algolia.search.saas.Index;
import com.freshcard.backend.model.Contact;
import com.freshcard.backend.model.User;
import org.json.JSONObject;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.DuplicateKeyException;

import java.sql.Timestamp;
import java.util.List;

/**
 * Created by willy on 15.08.14.
 */
public interface ContactDAO {
    @Cacheable("sql.contacts")
    public Contact find(Integer id);

    public Contact getUserConnection(Integer userId, Integer contactId, Integer organizationId);

    @Cacheable("sql.contacts")
    public Contact getCoWorker(Integer userId, Integer contactId, Integer organizationId);

    @Cacheable("sql.contacts")
    public Contact findByHashCode(String hashCode);

    @Cacheable("sql.contacts.lists")
    public List<Contact> findByUserId(Integer id);

    @Cacheable("sql.contacts.search")
    public List<Contact> getAllConnections();

    @Cacheable("sql.contacts.search")
    public List<Contact> getConnectionsByContactId(final Integer contactId);

    @Cacheable("sql.contacts.lists")
    public List<Contact> getUserContacts(Integer userId, Integer organizationId);

    @Cacheable("sql.userConnections.lists")
    public List<Contact> getUserConnections(Integer userId, Integer organizationId);

    public List<Contact> getUserConnections(Integer userId, Integer organizationId, Integer offset);

    @Cacheable("sql.coWorkers.lists")
    public List<Contact> getCoWorkers(Integer userId, Integer organizationId, Integer offset);

    @CacheEvict(
            value = "sql.userConnections.lists",
            allEntries = true
    )
    public Contact addConnection(Integer userId, User otherUser, Integer organizationId) throws DuplicateKeyException;

    @CacheEvict(
            value = "sql.userConnections.lists",
            allEntries = true
    )
    public Contact addConnection(Integer userId, User otherUser, Integer organizationId, String notes, Timestamp updatedOffline, Timestamp addedAt) throws DuplicateKeyException;

    @CacheEvict(
            value = "sql.userConnections.lists",
            allEntries = true
    )
    public Boolean updateConnection(final Integer userId, final Integer contactId, final Integer organizationId, final String notes, final Timestamp updatedOffline);

    @CacheEvict(
            value = "sql.coWorkers.lists",
            allEntries = true
    )
    public Boolean addCoWorker(Integer userId, Integer organizationId);

    @CacheEvict(
            value = "sql.userConnections.lists",
            allEntries = true
    )
    public Boolean removeConnection(Integer userId, Integer otherContactId, Integer organizationId);

    @CacheEvict(
            value = "sql.userConnections.lists",
            allEntries = true
    )
    public Boolean removePrimaryContactConnectionByUserId(final Integer userId, final Integer otherUserId, final Integer organizationId);

    @CacheEvict(
            value = "sql.userConnections.lists",
            allEntries = true
    )
    public Boolean removeConnectionByUserId(final Integer userId, final Integer otherUserId, final Integer organizationId);

    @CacheEvict(
            value = "sql.coWorkers.lists",
            allEntries = true
    )
    public Boolean removeCoWorker(Integer userId, Integer organizationId);

    @Caching(
            evict = {
                    @CacheEvict(
                            value = "sql.userConnections.lists",
                            allEntries = true
                    ),
                    @CacheEvict(
                            value = "sql.users",
                            allEntries = true
                    )
            }
    )
    public Number addContact(Integer userId, Integer organizationId, Contact contact, final Timestamp updatedOffline, final Timestamp addedAt);

    @CacheEvict(
            value = {
                    "sql.contacts",
                    "sql.contacts.lists"
            },
            allEntries = true
    )
    public Number insert(Contact Contact);

    @CacheEvict(
            value = {
                    "sql.contacts",
                    "sql.contacts.lists"
            },
            allEntries = true
    )
    public Number insertBatch(List<Contact> contacts);

    @CacheEvict(
            value = {
                    "sql.contacts",
                    "sql.contacts.lists"
            },
            allEntries = true)
    public void update(Integer id, Contact Contact);

    @CacheEvict(
            value = {
                    "sql.contacts",
                    "sql.contacts.lists"
            },
            allEntries = true)
    public void updateConnectionTimestamps(Integer userId, Integer organizationId, Integer contactId, Timestamp updatedOffline, Timestamp addedAt);

    @CacheEvict(
            value = {
                    "sql.contacts",
                    "sql.contacts.lists"
            },
            allEntries = true)
    public void delete(Integer id);

    public void initializeSearchIndex();

    public Boolean updateSearchIndex(Contact contact);

    public Boolean partiallyUpdateSearchIndex(JSONObject jsonContact, Contact contact);

    public Boolean deleteFromSearchIndex(Contact contact);

    public JSONObject convertContactToJsonObject(Contact contact);

    public JSONObject search(String query);

    public void setSkipJCard(Boolean skipJCard);

    public void setSkipSearchIndex(Boolean skipSearchIndex);

    public Index getIndex();
}
