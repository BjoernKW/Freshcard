package com.freshcard.backend.model.dao.impl;

import com.freshcard.backend.model.Role;
import com.freshcard.backend.model.dao.RoleDAO;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by willy on 15.08.14.
 */
public class JdbcRoleDAO implements RoleDAO {
    private static final Logger logger = Logger.getLogger(JdbcRoleDAO.class);

    private DataSource dataSource;

    private JdbcTemplate jdbcTemplate;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Number insert(final Role role) {
        final String sql = "INSERT INTO authorities (username, authority, organization_id) VALUES (?, ?, ?)";

        jdbcTemplate = new JdbcTemplate(dataSource);
        KeyHolder keyHolder = new GeneratedKeyHolder();

        PreparedStatementCreator preparedStatementCreator = new PreparedStatementCreator() {
            public PreparedStatement createPreparedStatement(Connection connection)
                    throws SQLException {
                PreparedStatement preparedStatement = connection.prepareStatement(sql, new String[] { "id" });

                preparedStatement.setString(1, role.getUsername());
                preparedStatement.setString(2, role.getAuthority());
                preparedStatement.setInt(3, role.getOrganizationId());

                return preparedStatement;
            }
        };
        jdbcTemplate.update(
                preparedStatementCreator,
                keyHolder
        );

        return keyHolder.getKey();
    }

    public void delete(Integer id) {
        String sql = "DELETE FROM authorities WHERE id = ?";

        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.update(sql, new Object[] { id });
    }

    public void deleteByUsername(String username) {
        String sql = "DELETE FROM authorities WHERE username = ?";

        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.update(sql, new Object[] { username });
    }
}
