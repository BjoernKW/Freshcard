package com.freshcard.backend.model.dao.impl;

import com.freshcard.backend.model.Contact;
import com.freshcard.backend.model.Organization;
import com.freshcard.backend.model.User;
import com.freshcard.backend.model.dao.OrganizationDAO;
import org.apache.log4j.Logger;
import org.postgresql.util.PGobject;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by willy on 15.08.14.
 */
public class JdbcOrganizationDAO implements OrganizationDAO {
    private static final Logger logger = Logger.getLogger(JdbcOrganizationDAO.class);

    private Boolean testRun = false;

    private DataSource dataSource;

    private PasswordEncoder passwordEncoder;

    private JdbcTemplate jdbcTemplate;

    private static final String ORGANIZATION_INFO_JOINS = "JOIN organizations_users ou ON ou.organization_id = o.\"id\" JOIN users u ON u.\"id\" = ou.user_id";

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public Organization find(Integer id) {
        String sql = "SELECT o.*, ou.search_public_key, ou.search_public_key_for_co_workers, u.*, u.\"id\" AS user_id, u.hash_code AS user_hash_code, c.vcard FROM organizations o " + ORGANIZATION_INFO_JOINS + " LEFT OUTER JOIN contacts_users cu ON u.\"id\" = cu.user_id AND cu.organization_id = o.\"id\" AND cu.user_is_owner = true LEFT OUTER JOIN contacts c ON c.\"id\" = cu.contact_id WHERE o.\"id\" = ?";

        Map<Integer, Organization> organizations = new HashMap<Integer, Organization>();
        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.query(sql, new Object[] { id }, new OrganizationInfoMapper(organizations));

        Organization organization = null;
        if (organizations.size() > 0) {
            organization = organizations.values().iterator().next();
        }

        return organization;
    }

    public Organization findByName(String name) {
        String sql = "SELECT o.*, ou.search_public_key, ou.search_public_key_for_co_workers, u.*, u.\"id\" AS user_id, u.hash_code AS user_hash_code, c.vcard FROM organizations o " + ORGANIZATION_INFO_JOINS + " LEFT OUTER JOIN contacts_users cu ON u.\"id\" = cu.user_id  AND cu.organization_id = o.\"id\" AND cu.user_is_owner = true LEFT OUTER JOIN contacts c ON c.\"id\" = cu.contact_id WHERE o.name = ?";

        Map<Integer, Organization> organizations = new HashMap<Integer, Organization>();
        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.query(sql, new Object[]{ name }, new OrganizationInfoMapper(organizations));

        Organization organization = null;
        if (organizations.size() > 0) {
            organization = organizations.values().iterator().next();
        }

        return organization;
    }

    public List<Organization> findByUsername(String username) {
        String sql = "SELECT o.* FROM organizations o JOIN organizations_users ou ON ou.organization_id = o.\"id\" JOIN users u ON u.\"id\" = ou.user_id WHERE u.username = ?";

        jdbcTemplate = new JdbcTemplate(dataSource);
        List<Organization> organizations =
                jdbcTemplate.query(sql, new Object[]{ username }, new SimpleOrganizationMapper());

        return organizations;
    }

    public Number insert(final Organization organization) {
        final String sql = "INSERT INTO organizations (name, url, hash_code) VALUES (?, ?, ?)";

        jdbcTemplate = new JdbcTemplate(dataSource);
        KeyHolder keyHolder = new GeneratedKeyHolder();

        PreparedStatementCreator preparedStatementCreator = new PreparedStatementCreator() {
            public PreparedStatement createPreparedStatement(Connection connection)
                    throws SQLException {
                PreparedStatement preparedStatement = connection.prepareStatement(sql, new String[] { "id" });

                preparedStatement.setString(1, organization.getName());
                preparedStatement.setString(2, organization.getUrl());
                preparedStatement.setString(3, passwordEncoder.encode(organization.getName()));

                return preparedStatement;
            }
        };
        jdbcTemplate.update(
                preparedStatementCreator,
                keyHolder
        );

        return keyHolder.getKey();
    }

    public Number insertBatch(final List<Organization> organizations) {
        final String sql = "INSERT INTO organizations (name, url, hash_code) VALUES (?, ?, ?)";

        jdbcTemplate = new JdbcTemplate(dataSource);

        int[] rowsAffected = jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
                Organization organization = organizations.get(i);

                preparedStatement.setString(1, organization.getName());
                preparedStatement.setString(2, organization.getUrl());
                preparedStatement.setString(3, passwordEncoder.encode(organization.getName()));
            }

            public int getBatchSize() {
                return organizations.size();
            }
        });

        return rowsAffected.length;
    }

    public void update(Integer id, Organization organization) {
        String sql = "UPDATE organizations SET name = COALESCE(?, name), url = COALESCE(?, url), template_image_path = COALESCE(?, template_image_path), template_layout = COALESCE(?, template_layout), template_as_svg = COALESCE(?, template_as_svg), logo_image_path = COALESCE(?, logo_image_path), updated_at = CURRENT_TIMESTAMP WHERE \"id\" = ?";

        final PGobject dataObject = new PGobject();
        dataObject.setType("json");

        try {
            dataObject.setValue(organization.getTemplateLayout());

            jdbcTemplate = new JdbcTemplate(dataSource);
            jdbcTemplate.update(
                    sql,
                    new Object[] {
                            organization.getName(),
                            organization.getUrl(),
                            organization.getTemplateImagePath(),
                            testRun ? organization.getTemplateLayout() : dataObject,
                            organization.getTemplateAsSVG(),
                            organization.getLogoImagePath(),
                            id
                    }
            );
        } catch (SQLException e) {
            logger.debug(e.getMessage());
        }
    }

    public void delete(Integer id) {
        String contactsSql = "DELETE FROM contacts_users WHERE \"organization_id\" = ?";

        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.update(contactsSql, new Object[] { id });

        String usersSql = "DELETE FROM organizations_users WHERE \"organization_id\" = ?";

        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.update(usersSql, new Object[] { id });

        String sql = "DELETE FROM organizations WHERE \"id\" = ?";

        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.update(sql, new Object[] { id });
    }

    public void setTestRun(Boolean testRun) {
        this.testRun = testRun;
    }

    private static class SimpleOrganizationMapper implements RowMapper<Organization> {
        public Organization mapRow(ResultSet rs, int rowNum) throws SQLException {
            Organization organization = new Organization();

            organization.setId(rs.getInt("id"));
            organization.setName(rs.getString("name"));
            organization.setUrl(rs.getString("url"));
            organization.setHashCode(rs.getString("hash_code"));
            organization.setUpdatedAt(rs.getTimestamp("updated_at"));

            return organization;
        }
    }

    private static class OrganizationInfoMapper implements RowMapper<Organization> {
        Map<Integer, Organization> organizations;

        public OrganizationInfoMapper(Map<Integer, Organization> organizations) {
            this.organizations = organizations;
        }

        public Organization mapRow(ResultSet rs, int rowNum) throws SQLException {
            Integer id = rs.getInt("id");
            Organization organization = organizations.get(id);
            if (organization == null) {
                organization = new Organization();

                organization.setId(id);
                organization.setName(rs.getString("name"));
                organization.setUrl(rs.getString("url"));
                organization.setHashCode(rs.getString("hash_code"));
                organization.setUpdatedAt(rs.getTimestamp("updated_at"));
                organization.setTemplateImagePath(rs.getString("template_image_path"));
                organization.setTemplateLayout(rs.getString("template_layout"));
                organization.setTemplateAsSVG(rs.getString("template_as_svg"));
                organization.setLogoImagePath(rs.getString("logo_image_path"));

                organizations.put(id, organization);
            }

            User user = new User();
            user.setId(rs.getInt("user_id"));
            user.setUsername(rs.getString("username"));
            user.setSearchPublicKey(rs.getString("search_public_key"));
            user.setSearchPublicKeyForCoWorkers(rs.getString("search_public_key_for_co_workers"));
            user.setHashCode(rs.getString("user_hash_code"));
            List<Contact> ownContacts = new ArrayList<Contact>();
            Contact contact = new Contact();
            contact.setVcard(rs.getString("vcard"));
            ownContacts.add(contact);
            user.setOwnContacts(ownContacts);
            organization.getUsers().add(user);

            return organization;
        }
    }
}
