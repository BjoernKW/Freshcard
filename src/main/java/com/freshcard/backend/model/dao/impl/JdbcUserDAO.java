package com.freshcard.backend.model.dao.impl;

import com.algolia.search.saas.APIClient;
import com.algolia.search.saas.Index;
import com.freshcard.backend.model.Organization;
import com.freshcard.backend.model.Role;
import com.freshcard.backend.model.User;
import com.freshcard.backend.model.dao.UserDAO;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by willy on 15.08.14.
 */
public class JdbcUserDAO implements UserDAO {
    private static final Logger logger = Logger.getLogger(JdbcUserDAO.class);

    protected DataSource dataSource;

    private PasswordEncoder passwordEncoder;

    protected JdbcTemplate jdbcTemplate;

    private String applicationID;

    private String apiKey;

    private String apiKeySearch;

    private String indexName;

    private APIClient client;

    private Index index;

    private static final String USER_INFO_JOINS = "JOIN organizations_users ou ON ou.user_id = u.\"id\" JOIN organizations o ON o.\"id\" = ou.organization_id";

    protected static final String USER_AUTHENTICATION_JOINS = "JOIN organizations co ON co.\"id\" = u.current_organization_id JOIN authorities a ON a.username = u.username AND a.organization_id = co.\"id\"";

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public User find(Integer id) {
        String sql = "SELECT u.*, o.*, o.\"id\" AS organization_id, o.hash_code AS organization_hash_code FROM users u " + USER_INFO_JOINS + " WHERE u.\"id\" = ?";

        Map<Integer, User> users = new HashMap<Integer, User>();
        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.query(sql, new Object[] { id }, new UserInfoMapper(users));

        User user = null;
        if (users.size() > 0) {
            user = users.values().iterator().next();
        }

        return user;
    }

    public User findByUsername(String username) {
        String sql = "SELECT u.*, a.* FROM users u " + USER_AUTHENTICATION_JOINS + " WHERE u.username = ? AND u.temporary_user = false";

        Map<Integer, User> users = new HashMap<Integer, User>();
        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.query(sql, new Object[]{ username }, new UserAuthenticationMapper(users));

        User user = null;
        if (users.size() > 0) {
            user = users.values().iterator().next();
        }

        return user;
    }

    public User findTemporaryUserByUsername(String username) {
        String sql = "SELECT u.*, a.* FROM users u " + USER_AUTHENTICATION_JOINS + " WHERE u.username = ? AND u.temporary_user = true";

        Map<Integer, User> users = new HashMap<Integer, User>();
        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.query(sql, new Object[]{ username }, new UserAuthenticationMapper(users));

        User user = null;
        if (users.size() > 0) {
            user = users.values().iterator().next();
        }

        return user;
    }

    public User getOAuthUser(String username) {
        String sql = "SELECT u.*, a.* FROM users u " + USER_AUTHENTICATION_JOINS + " WHERE u.username = ? AND u.use_oauth = true AND u.temporary_user = false";

        Map<Integer, User> users = new HashMap<Integer, User>();
        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.query(sql, new Object[]{ username }, new UserAuthenticationMapper(users, true));

        User user = null;
        if (users.size() > 0) {
            user = users.values().iterator().next();
        }

        return user;
    }

    public User findByHashCode(String hashCode) {
        String sql = "SELECT u.*, o.*, o.\"id\" AS organization_id, o.hash_code AS organization_hash_code FROM users u " + USER_INFO_JOINS + " WHERE u.hash_code = ?";

        Map<Integer, User> users = new HashMap<Integer, User>();
        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.query(sql, new Object[] { hashCode }, new UserInfoMapper(users));

        User user = null;
        if (users.size() > 0) {
            user = users.values().iterator().next();
        }

        return user;
    }

    public User getUserInfo(String username) {
        String sql = "SELECT u.*, o.*, o.\"id\" AS organization_id, o.hash_code AS organization_hash_code FROM users u " + USER_INFO_JOINS + " WHERE u.username = ?";

        Map<Integer, User> users = new HashMap<Integer, User>();
        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.query(sql, new Object[] { username }, new UserInfoMapper(users));

        User user = null;
        if (users.size() > 0) {
            user = users.values().iterator().next();
        }

        return user;
    }

    public User findByConfirmationHashCode(String hashCode) {
        String sql = "SELECT u.*, o.*, o.\"id\" AS organization_id, o.hash_code AS organization_hash_code FROM users u " + USER_INFO_JOINS + " WHERE u.confirmation_hash_code = ?";

        Map<Integer, User> users = new HashMap<Integer, User>();
        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.query(sql, new Object[] { hashCode }, new UserInfoMapper(users));

        User user = null;
        if (users.size() > 0) {
            user = users.values().iterator().next();
        }

        return user;
    }

    public User getUserInfoByUsername(String username) {
        String sql = "SELECT u.*, ou.search_public_key, ou.search_public_key_for_co_workers, o.*, o.\"id\" AS organization_id, o.hash_code AS organization_hash_code FROM users u " + USER_INFO_JOINS + " WHERE u.username = ? ORDER BY u.current_organization_id = ou.organization_id ASC";

        Map<Integer, User> users = new HashMap<Integer, User>();
        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.query(sql, new Object[]{ username }, new UserInfoMapper(users, true));

        User user = null;
        if (users.size() > 0) {
            Iterator<User> userIterator = users.values().iterator();
            while (userIterator.hasNext()) {
                user = userIterator.next();

                if (user.getOrganizations() != null && user.getOrganizations().size() > 0) {
                    for (Organization organization : user.getOrganizations()) {
                        if (organization.getSearchPublicKey() == null || organization.getSearchPublicKey().equals("") || organization.getSearchPublicKeyForCoWorkers() == null || organization.getSearchPublicKeyForCoWorkers().equals("")) {
                            initializeSearchIndex();

                            final Integer userId = user.getId();
                            final Integer organizationId = organization.getId();

                            String searchPublicKey = "";
                            String searchPublicKeyForCoWorkers = "";
                            try {
                                if (organization.getSearchPublicKey() == null || organization.getSearchPublicKey().equals("")) {
                                    searchPublicKey = client.generateSecuredApiKey(apiKeySearch, "user_" + userId + "_organization_" + organizationId);
                                    user.setSearchPublicKey(searchPublicKey);
                                }
                                if (organization.getSearchPublicKeyForCoWorkers() == null || organization.getSearchPublicKeyForCoWorkers().equals("")) {
                                    searchPublicKeyForCoWorkers = client.generateSecuredApiKey(apiKeySearch, "coworkers_organization_" + organizationId);
                                    user.setSearchPublicKeyForCoWorkers(searchPublicKeyForCoWorkers);
                                }
                            } catch (NoSuchAlgorithmException e) {
                                logger.debug(e.getMessage());
                            } catch (InvalidKeyException e) {
                                logger.debug(e.getMessage());
                            }
                            final String finalSearchPublicKey = searchPublicKey != "" ? searchPublicKey : organization.getSearchPublicKey();
                            final String finalSearchPublicKeyForCoWorkers = searchPublicKeyForCoWorkers != "" ? searchPublicKeyForCoWorkers : organization.getSearchPublicKeyForCoWorkers();

                            final String updateOrganizationSql = "UPDATE organizations_users SET search_public_key = ?, search_public_key_for_co_workers = ? WHERE organization_id = ? AND user_id = ?";

                            jdbcTemplate = new JdbcTemplate(dataSource);
                            KeyHolder keyHolder = new GeneratedKeyHolder();

                            PreparedStatementCreator preparedStatementCreator = new PreparedStatementCreator() {
                                public PreparedStatement createPreparedStatement(Connection connection)
                                        throws SQLException {
                                    PreparedStatement preparedStatement = connection.prepareStatement(updateOrganizationSql, new String[]{"id"});

                                    preparedStatement.setString(1, finalSearchPublicKey);
                                    preparedStatement.setString(2, finalSearchPublicKeyForCoWorkers);
                                    preparedStatement.setInt(3, organizationId);
                                    preparedStatement.setInt(4, userId);

                                    return preparedStatement;
                                }
                            };
                            jdbcTemplate.update(
                                    preparedStatementCreator,
                                    keyHolder
                            );
                        }
                    }
                }
            }
        }

        return user;
    }

    public User findEnabledUserByUsername(String username) {
        String sql = "SELECT u.*, a.* FROM users u " + USER_AUTHENTICATION_JOINS + " WHERE u.enabled = true AND u.username = ? AND u.temporary_user = false";

        User user = null;
        Map<Integer, User> users = new HashMap<Integer, User>();
        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.query(sql, new Object[] { username }, new UserAuthenticationMapper(users, true));

        if (users.size() > 0) {
            user = users.values().iterator().next();
        }

        return user;
    }

    public User insert(final User user) {
        final String sql = "INSERT INTO users (username, password, enabled, hash_code, confirmation_hash_code, preferred_language, use_oauth, most_recently_used_oauth_service, github_access_token, linkedin_access_token, twitter_access_token, xing_access_token, confirmed, temporary_user) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(true);
        user.setHashCode(passwordEncoder.encode(user.getUsername()));
        user.setConfirmationHashCode(passwordEncoder.encode(passwordEncoder.encode(user.getUsername())));

        jdbcTemplate = new JdbcTemplate(dataSource);
        KeyHolder keyHolder = new GeneratedKeyHolder();

        PreparedStatementCreator preparedStatementCreator = new PreparedStatementCreator() {
            public PreparedStatement createPreparedStatement(Connection connection)
                    throws SQLException {
                PreparedStatement preparedStatement = connection.prepareStatement(sql, new String[] { "id" });

                preparedStatement.setString(1, user.getUsername());
                preparedStatement.setString(2, user.getPassword());
                preparedStatement.setBoolean(3, user.getEnabled());
                preparedStatement.setString(4, user.getHashCode());
                preparedStatement.setString(5, user.getConfirmationHashCode());
                preparedStatement.setString(6, user.getPreferredLanguage());
                preparedStatement.setBoolean(7, user.getUseOAuth());
                preparedStatement.setString(8, user.getMostRecentlyUsedOAuthService());
                preparedStatement.setString(9, user.getGitHubAccessToken());
                preparedStatement.setString(10, user.getLinkedInAccessToken());
                preparedStatement.setString(11, user.getTwitterAccessToken());
                preparedStatement.setString(12, user.getXingAccessToken());
                preparedStatement.setBoolean(13, user.getConfirmed());
                preparedStatement.setBoolean(14, user.getTemporaryUser());

                return preparedStatement;
            }
        };
        jdbcTemplate.update(
                preparedStatementCreator,
                keyHolder
        );

        user.setId(keyHolder.getKey().intValue());

        return user;
    }

    public Number addUserToOrganization(final Integer userId, final Integer organizationId) {
        initializeSearchIndex();

        final String sql = "INSERT INTO organizations_users (organization_id, user_id, search_public_key, search_public_key_for_co_workers) VALUES (?, ?, ?, ?)";

        jdbcTemplate = new JdbcTemplate(dataSource);
        KeyHolder keyHolder = new GeneratedKeyHolder();

        PreparedStatementCreator preparedStatementCreator = new PreparedStatementCreator() {
            public PreparedStatement createPreparedStatement(Connection connection)
                    throws SQLException {
                PreparedStatement preparedStatement = connection.prepareStatement(sql, new String[] { "id" });

                preparedStatement.setInt(1, organizationId);
                preparedStatement.setInt(2, userId);
                try {
                    preparedStatement.setString(3, client.generateSecuredApiKey(apiKeySearch, "user_" + userId + "_organization_" + organizationId));
                } catch (NoSuchAlgorithmException e) {
                    logger.debug(e.getMessage());
                    preparedStatement.setString(3, "");
                } catch (InvalidKeyException e) {
                    logger.debug(e.getMessage());
                    preparedStatement.setString(3, "");
                }
                try {
                    preparedStatement.setString(4, client.generateSecuredApiKey(apiKeySearch, "coworkers_organization_" + organizationId));
                } catch (NoSuchAlgorithmException e) {
                    logger.debug(e.getMessage());
                    preparedStatement.setString(4, "");
                } catch (InvalidKeyException e) {
                    logger.debug(e.getMessage());
                    preparedStatement.setString(4, "");
                }

                return preparedStatement;
            }
        };
        jdbcTemplate.update(
                preparedStatementCreator,
                keyHolder
        );

        return keyHolder.getKey();
    }

    public void removeUserFromOrganization(final Integer userId, final Integer organizationId) {
        final String sql = "DELETE FROM organizations_users WHERE organization_id = ? AND user_id = ?";

        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.update(sql, new Object[] { organizationId, userId });
    }

    public Number addContactToUser(final Integer contactId, final Integer userId, final Integer organizationId) {
        final String sql = "INSERT INTO contacts_users (contact_id, user_id, organization_id, user_is_owner) VALUES (?, ?, ?, true)";

        jdbcTemplate = new JdbcTemplate(dataSource);
        KeyHolder keyHolder = new GeneratedKeyHolder();

        PreparedStatementCreator preparedStatementCreator = new PreparedStatementCreator() {
            public PreparedStatement createPreparedStatement(Connection connection)
                    throws SQLException {
                PreparedStatement preparedStatement = connection.prepareStatement(sql, new String[] { "id" });

                preparedStatement.setInt(1, contactId);
                preparedStatement.setInt(2, userId);
                preparedStatement.setInt(3, organizationId);

                return preparedStatement;
            }
        };
        jdbcTemplate.update(
                preparedStatementCreator,
                keyHolder
        );

        return keyHolder.getKey();
    }

    public void removeContactsForUser(final Integer userId) {
        final String sql = "DELETE FROM contacts_users WHERE user_id = ?";

        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.update(sql, new Object[] { userId });
    }

    public Number insertBatch(final List<User> users) {
        final String sql = "INSERT INTO users (username, password, enabled, hash_code, confirmation_hash_code, preferred_language, use_oauth, most_recently_used_oauth_service, github_access_token, linkedin_access_token, twitter_access_token, xing_access_token, confirmed) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        jdbcTemplate = new JdbcTemplate(dataSource);
        int[] rowsAffected = jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
                User user = users.get(i);

                preparedStatement.setString(1, user.getUsername());
                preparedStatement.setString(2, passwordEncoder.encode(user.getPassword()));
                preparedStatement.setBoolean(3, true);
                preparedStatement.setString(4, passwordEncoder.encode(user.getUsername()));
                preparedStatement.setString(5, passwordEncoder.encode(passwordEncoder.encode(user.getUsername())));
                preparedStatement.setString(6, user.getPreferredLanguage());
                preparedStatement.setBoolean(7, user.getUseOAuth());
                preparedStatement.setString(8, user.getMostRecentlyUsedOAuthService());
                preparedStatement.setString(9, user.getGitHubAccessToken());
                preparedStatement.setString(10, user.getLinkedInAccessToken());
                preparedStatement.setString(11, user.getTwitterAccessToken());
                preparedStatement.setString(12, user.getXingAccessToken());
                preparedStatement.setBoolean(13, user.getConfirmed());
            }

            public int getBatchSize() {
                return users.size();
            }
        });

        return rowsAffected.length;
    }

    public void updateCurrentOrganization(Integer id, User user) {
        String sql = "UPDATE users SET current_organization_id = CASE WHEN ? IS NULL THEN current_organization_id ELSE ? END, updated_at = CURRENT_TIMESTAMP WHERE \"id\" = ?";

        Integer organizationId = user.getCurrentOrganization() != null ? user.getCurrentOrganization().getId() : null;

        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.update(
                sql,
                new Object[] {
                        organizationId,
                        organizationId,
                        id
                }
        );
    }

    public void updateSynchronizationTimestamp(Integer id) {
        String sql = "UPDATE users SET synchronized_at = CURRENT_TIMESTAMP WHERE \"id\" = ?";

        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.update(
                sql,
                new Object[] {
                        id
                }
        );
    }

    public void updateConnectionSynchronizationTimestamp(Integer id, Timestamp connectionsLastUpdated) {
        String sql = "UPDATE users SET connections_last_updated = ? WHERE \"id\" = ?";

        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.update(
                sql,
                new Object[] {
                        connectionsLastUpdated,
                        id
                }
        );
    }

    public void updatePassword(Integer id, User user) {
        String sql = "UPDATE users SET password = ?, confirmed = ?, confirmation_hash_code = ?, updated_at = CURRENT_TIMESTAMP WHERE \"id\" = ?";

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setConfirmationHashCode(passwordEncoder.encode(passwordEncoder.encode(user.getUsername())));

        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.update(
                sql,
                new Object[] {
                        user.getPassword(),
                        user.getConfirmed(),
                        user.getConfirmationHashCode(),
                        id
                }
        );
    }

    public void updateUsername(Integer id, User user) {
        String sql = "UPDATE users SET username = ?, confirmed = false, confirmation_hash_code = ?, updated_at = CURRENT_TIMESTAMP WHERE \"id\" = ?";

        user.setConfirmationHashCode(passwordEncoder.encode(passwordEncoder.encode(user.getUsername())));

        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.update(
                sql,
                new Object[] {
                        user.getUsername(),
                        user.getConfirmationHashCode(),
                        id
                }
        );
    }

    public User update(Integer id, User user) {
        String sql = "UPDATE users SET " +
                "confirmed = CASE WHEN ? IS NULL THEN confirmed ELSE ? END, " +
                "confirmation_hash_code = COALESCE(?, confirmation_hash_code), " +
                "use_oauth = CASE WHEN ? IS NULL THEN confirmed ELSE ? END, " +
                "most_recently_used_oauth_service = COALESCE(?, most_recently_used_oauth_service), " +
                "github_access_token = COALESCE(?, github_access_token), " +
                "linkedin_access_token = COALESCE(?, linkedin_access_token), " +
                "twitter_access_token = COALESCE(?, twitter_access_token), " +
                "xing_access_token = COALESCE(?, xing_access_token), " +
                "temporary_user = CASE WHEN ? IS NULL THEN confirmed ELSE ? END, " +
                "preferred_language = COALESCE(?, preferred_language), " +
                "custom_signature = COALESCE(?, custom_signature), " +
                "profile_image_path = COALESCE(?, profile_image_path), " +
                "updated_at = CURRENT_TIMESTAMP " +
                "WHERE \"id\" = ?";

        if (user.getUsername() != null && user.getConfirmationHashCode() == null) {
            user.setConfirmationHashCode(passwordEncoder.encode(passwordEncoder.encode(user.getUsername())));
        }

        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.update(
                sql,
                new Object[] {
                        user.getConfirmed(),
                        user.getConfirmed(),
                        user.getConfirmationHashCode(),
                        user.getUseOAuth(),
                        user.getUseOAuth(),
                        user.getMostRecentlyUsedOAuthService(),
                        user.getGitHubAccessToken(),
                        user.getLinkedInAccessToken(),
                        user.getTwitterAccessToken(),
                        user.getXingAccessToken(),
                        user.getTemporaryUser(),
                        user.getTemporaryUser(),
                        user.getPreferredLanguage(),
                        user.getCustomSignature(),
                        user.getProfilePicturePath(),
                        id
                }
        );

        return user;
    }

    public void delete(Integer id) {
        String sql = "DELETE FROM users WHERE \"id\" = ?";

        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.update(sql, new Object[] { id });
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = findEnabledUserByUsername(username);

        if (user == null) {
            throw new UsernameNotFoundException("No user was found for username '" + username + "'.");
        }

        return user;
    }

    public List<Role> getAuthorities(String username, Integer organizationId) {
        String sql = "SELECT * FROM authorities WHERE username = ? AND organization_id = ?";

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        List<Role> roles =
                jdbcTemplate.query(
                        sql,
                        new Object[] {
                                username,
                                organizationId
                        },
                        new RoleMapper()
                );

        return roles;
    }

    public void initializeSearchIndex() {
        if (index == null) {
            client = new APIClient(applicationID, apiKey);
            index = client.initIndex(indexName);
        }
    }

    public void setApplicationID(String applicationID) {
        this.applicationID = applicationID;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public void setApiKeySearch(String apiKeySearch) {
        this.apiKeySearch = apiKeySearch;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    private class UserAuthenticationMapper implements RowMapper<User> {
        Map<Integer, User> users;
        Boolean withPassword = false;

        public UserAuthenticationMapper(Map<Integer, User> users) {
            this.users = users;
            this.withPassword = false;
        }

        public UserAuthenticationMapper(Map<Integer, User> users, Boolean withPassword) {
            this.users = users;
            this.withPassword = withPassword;
        }

        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            Integer id = rs.getInt("id");
            User user = users.get(id);
            if (user == null) {
                user = new User();

                user.setId(id);
                user.setUsername(rs.getString("username"));
                user.setUseOAuth(rs.getBoolean("use_oauth"));
                user.setMostRecentlyUsedOAuthService("most_recently_used_oauth_service");
                if (withPassword) {
                    user.setPassword(rs.getString("password"));
                }
                user.setEnabled(rs.getBoolean("enabled"));
                user.setHashCode(rs.getString("hash_code"));
                user.setUpdatedAt(rs.getTimestamp("updated_at"));
                user.setSynchronizedAt(rs.getTimestamp("synchronized_at"));
                user.setTemporaryUser((rs.getBoolean("temporary_user")));
                user.setCustomSignature(rs.getString("custom_signature"));
                user.setProfilePicturePath(rs.getString("profile_image_path"));

                Organization currentOrganization = new Organization();
                currentOrganization.setId(rs.getInt("current_organization_id"));
                user.setCurrentOrganization(currentOrganization);

                users.put(id, user);
            }

            Role role = new Role();
            role.setUsername(rs.getString("username"));
            role.setAuthority(rs.getString("authority"));
            role.setOrganizationId(rs.getInt("organization_id"));
            user.getRoles().add(role.getAuthority());

            return user;
        }
    }

    private class UserInfoMapper implements RowMapper<User> {
        Map<Integer, User> users;
        Boolean isForTransfer = false;

        public UserInfoMapper(Map<Integer, User> users) {
            this.users = users;
            this.isForTransfer = false;
        }

        public UserInfoMapper(Map<Integer, User> users, Boolean isForTransfer) {
            this.users = users;
            this.isForTransfer = isForTransfer;
        }

        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            Integer id = rs.getInt("id");
            User user = users.get(id);
            if (user == null) {
                user = new User();

                user.setId(id);
                user.setUsername(rs.getString("username"));
                user.setHashCode(rs.getString("hash_code"));
                user.setConfirmed(rs.getBoolean("confirmed"));
                user.setConfirmationHashCode(rs.getString("confirmation_hash_code"));
                user.setPreferredLanguage(rs.getString("preferred_language"));
                user.setUpdatedAt(rs.getTimestamp("updated_at"));
                user.setSynchronizedAt(rs.getTimestamp("synchronized_at"));
                user.setTemporaryUser((rs.getBoolean("temporary_user")));
                user.setCustomSignature(rs.getString("custom_signature"));
                user.setProfilePicturePath(rs.getString("profile_image_path"));
                user.setConnectionsLastUpdated(rs.getTimestamp("connections_last_updated"));

                Organization currentOrganization = new Organization();
                currentOrganization.setId(rs.getInt("current_organization_id"));
                currentOrganization.setTemplateAsSVG(rs.getString("template_as_svg"));
                currentOrganization.setHashCode(rs.getString("organization_hash_code"));
                user.setCurrentOrganization(currentOrganization);

                users.put(id, user);
            }

            Organization organization = new Organization();
            organization.setId(rs.getInt("organization_id"));
            organization.setName(rs.getString("name"));
            organization.setUrl(rs.getString("url"));
            organization.setHashCode(rs.getString("organization_hash_code"));
            if (isForTransfer) {
                organization.setSearchPublicKey(rs.getString("search_public_key"));
                organization.setSearchPublicKeyForCoWorkers(rs.getString("search_public_key_for_co_workers"));
            }
            user.getOrganizations().add(organization);

            return user;
        }
    }

    private class RoleMapper implements RowMapper<Role> {
        public Role mapRow(ResultSet rs, int rowNum) throws SQLException {
            Role role = new Role();

            role.setUsername(rs.getString("username"));
            role.setAuthority(rs.getString("authority"));
            role.setOrganizationId(rs.getInt("organization_id"));

            return role;
        }
    }
}
