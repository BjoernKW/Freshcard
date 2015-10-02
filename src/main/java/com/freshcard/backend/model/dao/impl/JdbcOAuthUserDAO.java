package com.freshcard.backend.model.dao.impl;

import com.freshcard.backend.model.Role;
import com.freshcard.backend.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by willy on 15.08.14.
 */
public class JdbcOAuthUserDAO extends JdbcUserDAO {
    public User findEnabledUserByUsername(String username) {
        String sql = "SELECT u.*, a.* FROM users u " + USER_AUTHENTICATION_JOINS + " WHERE u.enabled = true AND u.username = ?";

        User user = null;
        Map<Integer, User> users = new HashMap<Integer, User>();
        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.query(sql, new Object[] { username }, new UserOAuthAuthenticationMapper(users));

        if (users.size() > 0) {
            user = users.values().iterator().next();
        }

        return user;
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = findEnabledUserByUsername(username);

        if (user == null) {
            throw new UsernameNotFoundException("No user was found for username '" + username + "'.");
        }

        return user;
    }

    private class UserOAuthAuthenticationMapper implements RowMapper<User> {
        Map<Integer, User> users;

        public UserOAuthAuthenticationMapper(Map<Integer, User> users) {
            this.users = users;
        }

        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            Integer id = rs.getInt("id");
            User user = users.get(id);
            if (user == null) {
                user = new User();

                user.setId(id);
                user.setUsername(rs.getString("username"));
                user.setUseOAuth(rs.getBoolean("use_oauth"));
                user.setMostRecentlyUsedOAuthService(rs.getString("most_recently_used_oauth_service"));
                if (user.getMostRecentlyUsedOAuthService().equals(User.GITHUB_SERVICE_NAME)) {
                    user.setPassword(rs.getString("github_access_token"));
                }
                if (user.getMostRecentlyUsedOAuthService().equals(User.LINKEDIN_SERVICE_NAME)) {
                    user.setPassword(rs.getString("linkedin_access_token"));
                }
                if (user.getMostRecentlyUsedOAuthService().equals(User.TWITTER_SERVICE_NAME)) {
                    user.setPassword(rs.getString("twitter_access_token"));
                }
                if (user.getMostRecentlyUsedOAuthService().equals(User.XING_SERVICE_NAME)) {
                    user.setPassword(rs.getString("xing_access_token"));
                }
                user.setEnabled(rs.getBoolean("enabled"));
                user.setHashCode(rs.getString("hash_code"));

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
}
