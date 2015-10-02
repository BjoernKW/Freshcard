package com.freshcard.backend.model.dao;

import com.freshcard.backend.model.Organization;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;

/**
 * Created by willy on 15.08.14.
 */
public interface OrganizationDAO {
    @Cacheable("sql.organizations")
    public Organization find(Integer id);

    @Cacheable("sql.organizations")
    public Organization findByName(String name);

    @Cacheable("sql.organizations.lists")
    public List<Organization> findByUsername(String username);

    @CacheEvict(
            value = {
                    "sql.organizations",
                    "sql.organizations.lists"
            },
            allEntries = true
    )
    public Number insert(Organization Organization);

    @CacheEvict(
            value = {
                    "sql.organizations",
                    "sql.organizations.lists"
            },
            allEntries = true
    )
    public Number insertBatch(List<Organization> organizations);

    @CacheEvict(
            value = {
                    "sql.organizations",
                    "sql.organizations.lists"
            },
            allEntries = true)
    public void update(Integer id, Organization Organization);

    @CacheEvict(
            value = {
                    "sql.organizations",
                    "sql.organizations.lists"
            },
            allEntries = true)
    public void delete(Integer id);

    public void setTestRun(Boolean testRun);
}
