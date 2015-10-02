package com.freshcard.backend.util;

import com.freshcard.backend.model.User;
import com.freshcard.backend.model.dao.OrganizationDAO;
import com.freshcard.backend.model.dao.UserDAO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

/**
 * Created by willy on 02.09.14.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {
                "classpath:applicationContext.xml",
                "classpath:dispatcher-servlet.xml",
                "file:src/main/webapp/WEB-INF/spring-security.xml"
        }
)
public class UserManagementHelperTest {
    private String adminUsernameFixture = "admin@freshcard.co";
    private String yetAnotherUsernameFixture = "newuser@testuser.com";

    @Autowired
    private UserManagementHelper userManagementHelper;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    protected UserDAO userDAO;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    protected OrganizationDAO organizationDAO;

    @Before
    public void setup() {

    }

    @Test
    public void createUser() {
        User user = new User();
        user.setUsername(yetAnotherUsernameFixture);
        user.setPassword(yetAnotherUsernameFixture);

        User newUser = userManagementHelper.createUser(user);

        assertEquals(yetAnotherUsernameFixture, newUser.getUsername());
        assertNotNull(userDAO.findByUsername(yetAnotherUsernameFixture));
    }

    @Test
    public void deleteUser() {
        User user = new User();
        user.setUsername(yetAnotherUsernameFixture);

        assertTrue(yetAnotherUsernameFixture, userManagementHelper.deleteUser(user));
        assertNull(userDAO.findByUsername(yetAnotherUsernameFixture));
    }

    @Test
    public void isAdmin() {
        assertTrue(userManagementHelper.isAdmin(adminUsernameFixture, 2));
    }

    @Test
    public void isAuthorizedForContact() {
        assertTrue(userManagementHelper.isAuthorizedForContact(2, 2));
    }

    @Test
    public void isUserAuthorizedForEMailAddress() {
        assertTrue(userManagementHelper.isUserAuthorizedForEMailAddress(userDAO.findByUsername(adminUsernameFixture), "johnDoe@example.org"));
    }

    @Test
    public void isOrganizationAuthenticated() {
        User authenticatedUser = userDAO.findByUsername(adminUsernameFixture);
        authenticatedUser.setOrganizations(organizationDAO.findByUsername(adminUsernameFixture));

        User user = userDAO.find(2);
        user.setOrganizations(organizationDAO.findByUsername(user.getUsername()));

        assertTrue(userManagementHelper.isOrganizationAuthenticated(authenticatedUser, user));
    }
}
