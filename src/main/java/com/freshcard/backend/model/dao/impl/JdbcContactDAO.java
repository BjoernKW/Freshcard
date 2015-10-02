package com.freshcard.backend.model.dao.impl;

import com.algolia.search.saas.APIClient;
import com.algolia.search.saas.AlgoliaException;
import com.algolia.search.saas.Index;
import com.algolia.search.saas.Query;
import com.freshcard.backend.model.Contact;
import com.freshcard.backend.model.User;
import com.freshcard.backend.model.dao.ContactDAO;
import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.property.Photo;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.postgresql.util.PGobject;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by willy on 15.08.14.
 */
public class JdbcContactDAO implements ContactDAO {
    private static final Logger logger = Logger.getLogger(JdbcContactDAO.class);

    private DataSource dataSource;

    private PasswordEncoder passwordEncoder;

    private JdbcTemplate jdbcTemplate;

    private String applicationID;

    private String apiKey;

    private String indexName;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    private Boolean skipJCard = false;

    private Boolean skipSearchIndex = false;

    private Index index;

    public Contact find(Integer id) {
        String sql = "SELECT * FROM contacts WHERE \"id\" = ?";

        jdbcTemplate = new JdbcTemplate(dataSource);
        Contact contact =
                jdbcTemplate.query(sql, new Object[] { id }, new ContactMapper()).get(0);

        return contact;
    }

    public Contact getUserConnection(Integer userId, Integer contactId, Integer organizationId) {
        String sql = "SELECT c.*, cu.notes, cu.connection_updated_offline, cu.connection_added_at FROM contacts c JOIN contacts_users cu ON c.\"id\" = cu.contact_id WHERE cu.user_id = ? AND cu.contact_id = ? AND cu.organization_id = ? AND cu.user_is_owner = false";

        jdbcTemplate = new JdbcTemplate(dataSource);
        Contact contact =
                jdbcTemplate.query(sql, new Object[]{ userId, contactId, organizationId }, new ConnectionsMapper()).get(0);

        return contact;
    }

    public Contact getCoWorker(Integer userId, Integer contactId, Integer organizationId) {
        String sql = "SELECT c.* FROM contacts c JOIN contacts_users cu ON c.\"id\" = cu.contact_id WHERE cu.user_id = ? AND cu.contact_id = ? AND cu.organization_id = ? AND cu.user_is_owner = true";

        jdbcTemplate = new JdbcTemplate(dataSource);
        Contact contact =
                jdbcTemplate.query(sql, new Object[]{ userId, contactId, organizationId }, new ContactMapper()).get(0);

        return contact;
    }

    public Contact findByHashCode(String hashCode) {
        String sql = "SELECT * FROM contacts WHERE hash_code = ?";
        jdbcTemplate = new JdbcTemplate(dataSource);
        Contact contact =
                jdbcTemplate.query(sql, new Object[] { hashCode }, new ContactMapper()).get(0);

        return contact;
    }

    public List<Contact> findByUserId(Integer id) {
        String sql = "SELECT * FROM contacts c JOIN contacts_users cu ON c.\"id\" = cu.contact_id WHERE cu.user_id = ?";

        jdbcTemplate = new JdbcTemplate(dataSource);
        List<Contact> contacts =
                jdbcTemplate.query(sql, new Object[]{ id }, new ContactMapper());

        return contacts;
    }

    public List<Contact> getAllConnections() {
        String sql = "SELECT c.*, cu.*, 'user_' || cu.user_id || '_organization_' || cu.organization_id AS _tags, CASE WHEN c.updated_at > cu.connection_updated_offline THEN c.updated_at ELSE cu.connection_updated_offline END AS joined_updated_at FROM contacts c JOIN contacts_users cu ON c.\"id\" = cu.contact_id WHERE cu.user_is_owner = false";

        jdbcTemplate = new JdbcTemplate(dataSource);
        List<Contact> contacts =
                jdbcTemplate.query(sql, new ConnectionsSearchMapper());

        return contacts;
    }

    public List<Contact> getConnectionsByContactId(final Integer contactId) {
        String sql = "SELECT c.*, cu.*, 'user_' || cu.user_id || '_organization_' || cu.organization_id  AS _tags, CASE WHEN c.updated_at > cu.connection_updated_offline THEN c.updated_at ELSE cu.connection_updated_offline END AS joined_updated_at FROM contacts c JOIN contacts_users cu ON c.\"id\" = cu.contact_id WHERE cu.user_is_owner = false AND cu.contact_id = ?";

        jdbcTemplate = new JdbcTemplate(dataSource);
        List<Contact> contacts =
                jdbcTemplate.query(sql, new Object[]{ contactId }, new ConnectionsSearchMapper());

        return contacts;
    }

    public List<Contact> getUserContacts(Integer userId, Integer organizationId) {
        String sql = "SELECT c.*, cu.notes, cu.connection_updated_offline, cu.connection_added_at FROM contacts c JOIN contacts_users cu ON c.\"id\" = cu.contact_id WHERE cu.user_id = ? AND cu.organization_id = ? AND cu.user_is_owner = true";

        jdbcTemplate = new JdbcTemplate(dataSource);
        List<Contact> contacts =
                jdbcTemplate.query(sql, new Object[]{ userId, organizationId }, new ConnectionsMapper());

        return contacts;
    }

    public List<Contact> getUserConnections(Integer userId, Integer organizationId) {
        return getUserConnections(userId, organizationId, 0);
    }

    public List<Contact> getUserConnections(Integer userId, Integer organizationId, Integer offset) {
        String sql = "SELECT c.*, cu.notes, cu.connection_updated_offline, cu.connection_added_at FROM contacts c JOIN contacts_users cu ON c.\"id\" = cu.contact_id WHERE cu.user_id = ? AND cu.organization_id = ? AND cu.user_is_owner = false LIMIT 10 OFFSET ?";

        jdbcTemplate = new JdbcTemplate(dataSource);
        List<Contact> contacts =
                jdbcTemplate.query(sql, new Object[]{ userId, organizationId, offset }, new ConnectionsMapper());

        return contacts;
    }

    public List<Contact> getCoWorkers(Integer userId, Integer organizationId, Integer offset) {
        String sql = "SELECT * FROM organizations_users ou JOIN users u ON ou.user_id = u.\"id\" JOIN contacts_users cu ON cu.user_id = u.\"id\" JOIN contacts c ON c.\"id\" = cu.contact_id WHERE ou.organization_id = ? AND ou.user_id != ? LIMIT 10 OFFSET ?";

        jdbcTemplate = new JdbcTemplate(dataSource);
        List<Contact> contacts =
                jdbcTemplate.query(sql, new Object[]{ organizationId, userId, offset }, new CoWorkersMapper());

        return contacts;
    }

    public Contact addConnection(final Integer userId, final User otherUser, final Integer organizationId) throws DuplicateKeyException {
        return addConnection(userId, otherUser, organizationId, null, null, null);
    }

    public Contact addConnection(final Integer userId, final User otherUser, final Integer organizationId, final String notes, final Timestamp updatedOffline, final Timestamp addedAt) throws DuplicateKeyException {
        Contact primaryContact = null;
        List<Contact> contactList = getUserContacts(otherUser.getId(), otherUser.getCurrentOrganization().getId());
        if (contactList.size() > 0) {
            primaryContact = contactList.get(0);
        }

        final Integer contactId = primaryContact.getId();
        final String sqlContactsUsers = "INSERT INTO contacts_users (contact_id, user_id, organization_id, notes, connection_updated_offline, connection_added_at) VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate = new JdbcTemplate(dataSource);

        PreparedStatementCreator preparedStatementCreator = new PreparedStatementCreator() {
            public PreparedStatement createPreparedStatement(Connection connection)
                    throws SQLException {
                PreparedStatement preparedStatement = connection.prepareStatement(sqlContactsUsers, new String[] { "id" });

                preparedStatement.setObject(1, contactId);
                preparedStatement.setObject(2, userId);
                preparedStatement.setObject(3, organizationId);
                preparedStatement.setObject(4, notes);
                preparedStatement.setObject(5, updatedOffline);
                preparedStatement.setObject(6, addedAt);

                return preparedStatement;
            }
        };

        Integer numberOfRows = jdbcTemplate.update(
                preparedStatementCreator
        );

        primaryContact = find(primaryContact.getId()); // refresh contact

        if (numberOfRows <= 0) {
            primaryContact = null;
        }

        updateSearchIndex(prepareContactForSearchIndex(primaryContact, userId, organizationId));

        return primaryContact;
    }

    public Boolean updateConnection(final Integer userId, final Integer contactId, final Integer organizationId, final String notes, final Timestamp updatedOffline) {
        final String sqlContactsUsers = "UPDATE contacts_users SET notes = ?, connection_updated_offline = ?, version = version + 1 WHERE contact_id = ? AND user_id = ? AND organization_id = ? AND user_is_owner = false";
        jdbcTemplate = new JdbcTemplate(dataSource);

        PreparedStatementCreator preparedStatementCreator = new PreparedStatementCreator() {
            public PreparedStatement createPreparedStatement(Connection connection)
                    throws SQLException {
                PreparedStatement preparedStatement = connection.prepareStatement(sqlContactsUsers, new String[] { "id" });

                preparedStatement.setObject(1, notes);
                preparedStatement.setObject(2, updatedOffline);
                preparedStatement.setObject(3, contactId);
                preparedStatement.setObject(4, userId);
                preparedStatement.setObject(5, organizationId);

                return preparedStatement;
            }
        };

        Integer numberOfRows = jdbcTemplate.update(
                preparedStatementCreator
        );

        updateSearchIndex(prepareContactForSearchIndex(find(contactId), userId, organizationId));

        return numberOfRows > 0;
    }

    public Boolean addCoWorker(final Integer userId, final Integer organizationId) {
        final String sqlOrganizationsUsers = "INSERT INTO organizations_users (user_id, organization_id) VALUES (?, ?)";
        jdbcTemplate = new JdbcTemplate(dataSource);

        PreparedStatementCreator preparedStatementCreator = new PreparedStatementCreator() {
            public PreparedStatement createPreparedStatement(Connection connection)
                    throws SQLException {
                PreparedStatement preparedStatement = connection.prepareStatement(sqlOrganizationsUsers, new String[] { "id" });

                preparedStatement.setObject(1, userId);
                preparedStatement.setObject(2, organizationId);

                return preparedStatement;
            }
        };
        Integer numberOfRows = jdbcTemplate.update(
                preparedStatementCreator
        );

        Contact primaryContact;
        List<Contact> contactList = getUserContacts(userId, organizationId);
        if (contactList.size() > 0) {
            primaryContact = contactList.get(0);
            updateSearchIndex(prepareCoWorkerForSearchIndex(primaryContact, userId, organizationId));
        }

        return numberOfRows > 0;
    }

    public Boolean removePrimaryContactConnectionByUserId(final Integer userId, final Integer otherUserId, final Integer organizationId) {
        Contact primaryContact = null;
        List<Contact> contactList = findByUserId(otherUserId);
        if (contactList.size() > 0) {
            primaryContact = contactList.get(0);
        }
        if (primaryContact == null) {
            return false;
        }

        final Integer contactId = primaryContact.getId();
        final String sqlContactsUsers = "DELETE FROM contacts_users WHERE contact_id = ? AND user_id = ? AND organization_id = ? AND user_is_owner = false";
        jdbcTemplate = new JdbcTemplate(dataSource);

        PreparedStatementCreator preparedStatementCreator = new PreparedStatementCreator() {
            public PreparedStatement createPreparedStatement(Connection connection)
                    throws SQLException {
                PreparedStatement preparedStatement = connection.prepareStatement(sqlContactsUsers);

                preparedStatement.setObject(1, contactId);
                preparedStatement.setObject(2, userId);
                preparedStatement.setObject(3, organizationId);

                return preparedStatement;
            }
        };
        Integer numberOfRows = jdbcTemplate.update(
                preparedStatementCreator
        );

        deleteFromSearchIndex(prepareContactForSearchIndex(primaryContact, userId, organizationId));

        return numberOfRows > 0;
    }

    public Boolean removeConnectionByUserId(final Integer userId, final Integer otherUserId, final Integer organizationId) {
        jdbcTemplate = new JdbcTemplate(dataSource);

        final String selectSqlContactsUsers = "SELECT cu.\"id\", cu.contact_id, 'user_' || cu.user_id || '_organization_' || cu.organization_id AS _tags FROM contacts_users cu WHERE cu.\"id\" IN (SELECT cu.\"id\" FROM contacts_users cu JOIN contacts_users cuo ON cuo.contact_id = cu.contact_id JOIN users u ON u.\"id\" = cuo.user_id WHERE cuo.user_is_owner = true AND cuo.user_id = ? AND cu.user_id = ? AND cu.organization_id = ? AND cu.user_is_owner = false)";
        List<Contact> contacts =
                jdbcTemplate.query(selectSqlContactsUsers, new Object[]{ otherUserId, userId, organizationId }, new ConnectionsForDeletionMapper());
        batchDeleteFromSearchIndex(contacts);

        final String sqlContactsUsers = "DELETE FROM contacts_users cu WHERE cu.\"id\" IN (SELECT cu.\"id\" FROM contacts_users cu JOIN contacts_users cuo ON cuo.contact_id = cu.contact_id JOIN users u ON u.\"id\" = cuo.user_id WHERE cuo.user_is_owner = true AND cuo.user_id = ? AND cu.user_id = ? AND cu.organization_id = ? AND cu.user_is_owner = false)";

        PreparedStatementCreator preparedStatementCreator = new PreparedStatementCreator() {
            public PreparedStatement createPreparedStatement(Connection connection)
                    throws SQLException {
                PreparedStatement preparedStatement = connection.prepareStatement(sqlContactsUsers);

                preparedStatement.setObject(1, otherUserId);
                preparedStatement.setObject(2, userId);
                preparedStatement.setObject(3, organizationId);

                return preparedStatement;
            }
        };
        Integer numberOfRows = jdbcTemplate.update(
                preparedStatementCreator
        );

        return numberOfRows > 0;
    }

    public Boolean removeConnection(final Integer userId, final Integer otherContactId, final Integer organizationId) {
        Contact primaryContact = find(otherContactId);
        if (primaryContact == null) {
            return false;
        }

        final Integer contactId = primaryContact.getId();
        final String sqlContactsUsers = "DELETE FROM contacts_users WHERE contact_id = ? AND user_id = ? AND organization_id = ? AND user_is_owner = false";
        jdbcTemplate = new JdbcTemplate(dataSource);

        PreparedStatementCreator preparedStatementCreator = new PreparedStatementCreator() {
            public PreparedStatement createPreparedStatement(Connection connection)
                    throws SQLException {
                PreparedStatement preparedStatement = connection.prepareStatement(sqlContactsUsers);

                preparedStatement.setObject(1, contactId);
                preparedStatement.setObject(2, userId);
                preparedStatement.setObject(3, organizationId);

                return preparedStatement;
            }
        };
        Integer numberOfRows = jdbcTemplate.update(
                preparedStatementCreator
        );

        deleteFromSearchIndex(prepareContactForSearchIndex(primaryContact, userId, organizationId));

        return numberOfRows > 0;
    }

    public Boolean removeCoWorker(final Integer userId, final Integer organizationId) {
        jdbcTemplate = new JdbcTemplate(dataSource);

        final String sqlOrganizationsUsers = "DELETE FROM organizations_users WHERE user_id = ? AND organization_id = ?";

        PreparedStatementCreator preparedStatementCreator = new PreparedStatementCreator() {
            public PreparedStatement createPreparedStatement(Connection connection)
                    throws SQLException {
                PreparedStatement preparedStatement = connection.prepareStatement(sqlOrganizationsUsers);

                preparedStatement.setObject(1, userId);
                preparedStatement.setObject(2, organizationId);

                return preparedStatement;
            }
        };
        Integer numberOfRows = jdbcTemplate.update(
                preparedStatementCreator
        );

        Contact primaryContact;
        List<Contact> contactList = getUserContacts(userId, organizationId);
        if (contactList.size() > 0) {
            primaryContact = contactList.get(0);
            deleteFromSearchIndex(prepareCoWorkerForSearchIndex(primaryContact, userId, organizationId));

            String contactsRemovalSql = "DELETE FROM contacts_users WHERE contact_id = ? AND user_id = ? AND organization_id = ?";
            jdbcTemplate.update(contactsRemovalSql, new Object[] { primaryContact.getId(), userId, organizationId });
        }

        return numberOfRows > 0;
    }

    public Number addContact(final Integer userId, final Integer organizationId, Contact contact, final Timestamp updatedOffline, final Timestamp addedAt) {
        final Number contactId = insert(contact);

        final String sqlContactsUsers = "INSERT INTO contacts_users (contact_id, user_id, organization_id, connection_updated_offline, connection_added_at, user_is_owner) VALUES (?, ?, ?, ?, ?, true)";
        jdbcTemplate = new JdbcTemplate(dataSource);

        PreparedStatementCreator preparedStatementCreator = new PreparedStatementCreator() {
            public PreparedStatement createPreparedStatement(Connection connection)
                    throws SQLException {
                PreparedStatement preparedStatement = connection.prepareStatement(sqlContactsUsers, new String[] { "id" });

                preparedStatement.setObject(1, contactId.intValue());
                preparedStatement.setObject(2, userId);
                preparedStatement.setObject(3, organizationId);
                preparedStatement.setObject(4, updatedOffline);
                preparedStatement.setObject(5, addedAt);

                return preparedStatement;
            }
        };
        jdbcTemplate.update(
                preparedStatementCreator
        );

        return contactId;
    }

    public Number insert(final Contact contact) {
        final String sql = "INSERT INTO contacts (vcard, hash_code) VALUES (?, ?)";

        jdbcTemplate = new JdbcTemplate(dataSource);
        KeyHolder keyHolder = new GeneratedKeyHolder();

        final PGobject dataObject = new PGobject();
        dataObject.setType("json");

        try {
            dataObject.setValue(contact.getVcard());

            PreparedStatementCreator preparedStatementCreator = new PreparedStatementCreator() {
                public PreparedStatement createPreparedStatement(Connection connection)
                        throws SQLException {
                    PreparedStatement preparedStatement = connection.prepareStatement(sql, new String[] { "id" });

                    preparedStatement.setObject(1, skipJCard ? contact.getVcard() : dataObject);
                    preparedStatement.setString(2, passwordEncoder.encode(contact.getVcard()));

                    return preparedStatement;
                }
            };
            jdbcTemplate.update(
                    preparedStatementCreator,
                    keyHolder
            );
        } catch (SQLException e) {
            logger.debug(e.getMessage());
        }

        return keyHolder.getKey();
    }

    public Number insertBatch(final List<Contact> contacts) {
        final String sql = "INSERT INTO contacts (vcard, hash_code) VALUES (?, ?)";

        jdbcTemplate = new JdbcTemplate(dataSource);

        int[] rowsAffected = jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
                Contact contact = contacts.get(i);

                PGobject dataObject = new PGobject();
                dataObject.setType("json");
                dataObject.setValue(contact.getVcard());

                preparedStatement.setObject(1, dataObject);
                preparedStatement.setString(2, passwordEncoder.encode(contact.getVcard()));
            }

            public int getBatchSize() {
                return contacts.size();
            }
        });

        return rowsAffected.length;
    }

    public void update(Integer id, Contact contact) {
        String sql = "UPDATE contacts SET vcard = ?, updated_at = CURRENT_TIMESTAMP WHERE \"id\" = ?";

        PGobject dataObject = new PGobject();
        dataObject.setType("json");
        try {
            dataObject.setValue(contact.getVcard());

            jdbcTemplate = new JdbcTemplate(dataSource);
            jdbcTemplate.update(
                    sql,
                    new Object[] {
                            dataObject,
                            id
                    }
            );

            batchUpdateSearchIndex(getConnectionsByContactId(id));
        } catch (SQLException e) {
            logger.debug(e.getMessage());
        }
    }

    public void updateConnectionTimestamps(Integer userId, Integer organizationId, Integer contactId, Timestamp updatedOffline, Timestamp addedAt) {
        String sql = "UPDATE contacts_users SET connection_updated_offline = ?, connection_added_at = ?, version = version + 1 WHERE contact_id = ? AND user_id = ? AND organization_id = ?";

        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.update(
                sql,
                new Object[] {
                        updatedOffline,
                        addedAt,
                        contactId,
                        userId,
                        organizationId
                }
        );
    }

    public void delete(Integer id) {
        batchDeleteFromSearchIndex(getConnectionsByContactId(id));

        String sql = "DELETE FROM contacts WHERE \"id\" = ?";

        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.update(sql, new Object[] { id });

        sql = "DELETE FROM contacts_users WHERE contact_id = ?";
        jdbcTemplate.update(sql, new Object[] { id });
    }

    public void initializeSearchIndex() {
        if (index == null) {
            APIClient client = new APIClient(applicationID, apiKey);
            index = client.initIndex(indexName);
        }
    }

    public Boolean updateSearchIndex(Contact contact) {
        initializeSearchIndex();

        Boolean updateSuccessful = false;

        JSONObject jsonContact = convertContactToJsonObject(contact);

        try {
            updateSuccessful = index.saveObject(jsonContact, "contact_" + contact.getId() + "_" + contact.getTags()) != null;
        } catch (AlgoliaException e) {
            logger.debug(e.getMessage());
        }

        return updateSuccessful;
    }

    public Boolean partiallyUpdateSearchIndex(JSONObject jsonContact, Contact contact) {
        initializeSearchIndex();

        Boolean updateSuccessful = false;

        try {
            updateSuccessful = index.partialUpdateObject(jsonContact, "contact_" + contact.getId() + "_" + contact.getTags()) != null;
        } catch (AlgoliaException e) {
            logger.debug(e.getMessage());
        }

        return updateSuccessful;
    }

    public Boolean deleteFromSearchIndex(Contact contact) {
        initializeSearchIndex();

        Boolean deleteSuccessful = false;

        try {
            deleteSuccessful = index.deleteObject("contact_" + contact.getId() + "_" + contact.getTags()) != null;
        } catch (AlgoliaException e) {
            logger.debug(e.getMessage());
        }

        return deleteSuccessful;
    }

    public JSONObject convertContactToJsonObject(Contact contact) {
        List<VCard> vCards = Ezvcard.parseJson(contact.getVcard()).all();
        for (VCard vCard : vCards) {
            List<Photo> photos = vCard.getPhotos();
            for (Photo photo : photos) {
                vCard.removeProperty(photo);
            }
        }
        String contactVCard = Ezvcard.writeJson(vCards).go();

        JSONObject jsonContact = new JSONObject();
        jsonContact.put("id", contact.getId());
        jsonContact.put("objectID", "contact_" + contact.getId() + "_" + contact.getTags());
        jsonContact.put("vcard", contactVCard);
        jsonContact.put("notes", contact.getNotes());
        jsonContact.put("updated_at", contact.getJoinedUpdatedAt());
        jsonContact.put("_tags", contact.getTags());
        if (contact.getCoWorkerUserId() != null) {
            jsonContact.put("co_worker_user_id", contact.getCoWorkerUserId());
        }
        return jsonContact;
    }

    public JSONObject search(String query) {
        initializeSearchIndex();

        JSONObject answer = null;

        try {
            answer = index.search(new Query(query));
        } catch (AlgoliaException e) {
            logger.debug(e.getMessage());
        }

        return answer;
    }

    private Boolean batchUpdateSearchIndex(List<Contact> contactList) {
        initializeSearchIndex();

        Boolean updateSuccessful = true;

        List<JSONObject> jsonContacts = new ArrayList<JSONObject>();
        for (Contact contact : contactList) {
            jsonContacts.add(convertContactToJsonObject(contact));

            if (jsonContacts.size() == 10000) {
                try {
                    updateSuccessful = index.saveObjects(jsonContacts) != null;
                } catch (AlgoliaException e) {
                    logger.debug(e.getMessage());
                    updateSuccessful = false;
                }
                jsonContacts.clear();
            }
        }

        if (!jsonContacts.isEmpty()) {
            try {
                updateSuccessful = index.saveObjects(jsonContacts) != null;
            } catch (AlgoliaException e) {
                logger.debug(e.getMessage());
                updateSuccessful = false;
            }
        }

        return updateSuccessful;
    }

    private Boolean batchDeleteFromSearchIndex(List<Contact> contactList) {
        initializeSearchIndex();

        Boolean deleteSuccessful = true;

        List<String> contactIDs = new ArrayList<String>();
        for (Contact contact : contactList) {
            contactIDs.add("contact_" + contact.getId() + "_" + contact.getTags());

            if (contactIDs.size() == 10000) {
                try {
                    deleteSuccessful = index.deleteObjects(contactIDs) != null;
                } catch (AlgoliaException e) {
                    logger.debug(e.getMessage());
                    deleteSuccessful = false;
                }
                contactIDs.clear();
            }
        }

        if (!contactIDs.isEmpty()) {
            try {
                deleteSuccessful = index.deleteObjects(contactIDs) != null;
            } catch (AlgoliaException e) {
                logger.debug(e.getMessage());
                deleteSuccessful = false;
            }
        }

        return deleteSuccessful;
    }

    private static class ConnectionsMapper implements RowMapper<Contact> {
        public Contact mapRow(ResultSet rs, int rowNum) throws SQLException {
            Contact contact = new Contact();

            contact.setId(rs.getInt("id"));
            contact.setVcard(rs.getString("vcard"));
            contact.setHashCode(rs.getString("hash_code"));
            contact.setUpdatedAt(rs.getTimestamp("updated_at"));
            contact.setUpdatedOffline(rs.getTimestamp("connection_updated_offline"));
            contact.setAddedAt(rs.getTimestamp("connection_added_at"));
            contact.setNotes(rs.getString("notes"));

            return contact;
        }
    }

    private static class ConnectionsSearchMapper implements RowMapper<Contact> {
        public Contact mapRow(ResultSet rs, int rowNum) throws SQLException {
            Contact contact = new Contact();

            contact.setId(rs.getInt("id"));
            contact.setVcard(rs.getString("vcard"));
            contact.setJoinedUpdatedAt(rs.getTimestamp("joined_updated_at"));
            contact.setNotes(rs.getString("notes"));
            contact.setTags(rs.getString("_tags"));

            return contact;
        }
    }

    private static class ConnectionsForDeletionMapper implements RowMapper<Contact> {
        public Contact mapRow(ResultSet rs, int rowNum) throws SQLException {
            Contact contact = new Contact();

            contact.setId(rs.getInt("contact_id"));
            contact.setTags(rs.getString("_tags"));

            return contact;
        }
    }

    private static class ContactMapper implements RowMapper<Contact> {
        public Contact mapRow(ResultSet rs, int rowNum) throws SQLException {
            Contact contact = new Contact();

            contact.setId(rs.getInt("id"));
            contact.setVcard(rs.getString("vcard"));
            contact.setHashCode(rs.getString("hash_code"));
            contact.setUpdatedAt(rs.getTimestamp("updated_at"));

            return contact;
        }
    }

    private static class CoWorkersMapper implements RowMapper<Contact> {
        public Contact mapRow(ResultSet rs, int rowNum) throws SQLException {
            Contact contact = new Contact();

            contact.setId(rs.getInt("contact_id"));
            contact.setVcard(rs.getString("vcard"));
            contact.setHashCode(rs.getString("hash_code"));
            contact.setUpdatedAt(rs.getTimestamp("updated_at"));

            return contact;
        }
    }

    private Contact prepareCoWorkerForSearchIndex(Contact contact, Integer userId, Integer organizationId) {
        Contact preparedContact = contact;

        preparedContact.setTags("coworkers_organization_" + organizationId);
        preparedContact.setCoWorkerUserId(userId);

        return preparedContact;
    }

    private Contact prepareContactForSearchIndex(Contact contact, Integer userId, Integer organizationId) {
        Contact preparedContact = contact;

        preparedContact.setTags("user_" + userId + "_organization_" + organizationId);

        return preparedContact;
    }

    public void setSkipJCard(Boolean skipJCard) {
        this.skipJCard = skipJCard;
    }

    public void setSkipSearchIndex(Boolean skipSearchIndex) {
        this.skipSearchIndex = skipSearchIndex;
    }

    public void setApplicationID(String applicationID) {
        this.applicationID = applicationID;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public Index getIndex() {
        return index;
    }
}
