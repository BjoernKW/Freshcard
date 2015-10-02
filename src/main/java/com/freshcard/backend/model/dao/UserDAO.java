package com.freshcard.backend.model.dao;

import com.freshcard.backend.model.Role;
import com.freshcard.backend.model.User;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.sql.Timestamp;
import java.util.List;

/**
 * Created by willy on 15.08.14.
 */
public interface UserDAO extends UserDetailsService {
    @Cacheable("sql.users")
    public User find(Integer id);

    @Cacheable("sql.users")
    public User findByUsername(String username);

    public User findTemporaryUserByUsername(String username);

    public User getOAuthUser(String username);

    public User getUserInfo(String username);

    @Cacheable("sql.users")
    public User findByHashCode(String hashCode);

    @Cacheable("sql.users")
    public User findByConfirmationHashCode(String hashCode);

    @Cacheable("sql.users")
    public User getUserInfoByUsername(String username);

    @Cacheable("sql.users")
    public User findEnabledUserByUsername(String username);

    @CacheEvict(
            value = {
                    "sql.users",
                    "sql.users.lists"
            },
            allEntries = true
    )
    public User insert(User user);

    @CacheEvict(
            value = {
                    "sql.users",
                    "sql.organizations",
            },
            allEntries = true)
    public Number addUserToOrganization(Integer userId, Integer organizationId);

    @CacheEvict(
            value = {
                    "sql.users"
            },
            allEntries = true)
    public Number addContactToUser(final Integer contactId, final Integer userId, final Integer organizationId);

    @CacheEvict(
            value = {
                    "sql.users",
                    "sql.users.lists"
            },
            allEntries = true
    )
    public Number insertBatch(List<User> users);

    @CacheEvict(
            value = {
                    "sql.users",
                    "sql.users.lists"
            },
            allEntries = true)
    public User update(Integer id, User User);

    @CacheEvict(
            value = {
                    "sql.users"
            },
            allEntries = true)
    public void updateCurrentOrganization(Integer id, User User);

    @CacheEvict(
            value = {
                    "sql.users"
            },
            allEntries = true)
    public void updateSynchronizationTimestamp(Integer id);

    @CacheEvict(
            value = {
                    "sql.users"
            },
            allEntries = true)
    public void updateConnectionSynchronizationTimestamp(Integer id, Timestamp connectionsLastUpdated);

    @CacheEvict(
            value = {
                    "sql.users"
            },
            allEntries = true)
    public void updatePassword(Integer id, User User);

    @CacheEvict(
            value = {
                    "sql.users"
            },
            allEntries = true)
    public void updateUsername(Integer id, User User);

    @CacheEvict(
            value = {
                    "sql.users",
                    "sql.users.lists"
            },
            allEntries = true)
    public void delete(Integer id);

    @CacheEvict(
            value = {
                    "sql.users",
                    "sql.organizations"
            },
            allEntries = true)
    public void removeUserFromOrganization(final Integer userId, final Integer organizationId);

    @CacheEvict(
            value = {
                    "sql.users",
                    "sql.contacts"
            },
            allEntries = true)
    public void removeContactsForUser(final Integer userId);

    public List<Role> getAuthorities(String username, Integer organizationId);

    public void initializeSearchIndex();
}
