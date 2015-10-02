package com.freshcard.backend.model.dao;

import com.freshcard.backend.model.Role;
import org.springframework.cache.annotation.CacheEvict;

/**
 * Created by willy on 15.08.14.
 */
public interface RoleDAO {
    @CacheEvict(
            value = {
                    "sql.roles",
                    "sql.roles.lists"
            },
            allEntries = true
    )
    public Number insert(Role role);

    @CacheEvict(
            value = {
                    "sql.roles",
                    "sql.roles.lists"
            },
            allEntries = true)
    public void delete(Integer id);

    @CacheEvict(
            value = {
                    "sql.roles",
                    "sql.roles.lists"
            },
            allEntries = true)
    public void deleteByUsername(String username);
}
