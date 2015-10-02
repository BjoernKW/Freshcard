package com.freshcard.backend.model.dao;

import com.freshcard.backend.model.Role;
import com.freshcard.backend.model.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(
        locations = {
                "classpath:applicationContext.xml",
                "classpath:dispatcher-servlet.xml",
                "file:src/main/webapp/WEB-INF/spring-security.xml"
        }
)
public class RoleDAOTest {
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    protected RoleDAO roleDAO;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    protected UserDAO userDAO;

    @Before
    public void setup() {

    }

    @Test
    public void insert() throws Exception {
        Role role = new Role();
        role.setAuthority("ROLE_ADMIN");
        role.setUsername("some@testuser.com");
        role.setOrganizationId(3);

        Number roleId = roleDAO.insert(role);

        assertNotNull(roleId);
    }

    @Test
    public void deleteByUsername() throws Exception {
        String username = "role@testuser.com";

        User user = userDAO.findByUsername(username);
        assertNotNull(user.getAuthorities());

        roleDAO.deleteByUsername(username);

        user = userDAO.findByUsername(username);
        assertNull(user);

        Role role = new Role();
        role.setAuthority("ROLE_USER");
        role.setUsername("role@testuser.com");
        role.setOrganizationId(3);

        Number roleId = roleDAO.insert(role);

        assertNotNull(roleId);

        user = userDAO.findByUsername(username);
        assertNotNull(user.getAuthorities());
    }
}
