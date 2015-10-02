package com.freshcard.backend.model.dao;

import com.freshcard.backend.model.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.sql.Timestamp;
import java.util.Date;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(
        locations = {
                "classpath:applicationContext.xml",
                "classpath:dispatcher-servlet.xml",
                "file:src/main/webapp/WEB-INF/spring-security.xml"
        }
)
public class UserDAOTest {
    private String hashCodeFixture = "4b4aeedfbbb1fc4f31ed83e9a626f6b2e15fdae310695a12d618aaba50ebf37be128700152015f12";

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    protected UserDAO userDAO;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    protected OrganizationDAO organizationDAO;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    @Qualifier("authenticationManager")
    private AuthenticationManager authenticationManager;

    @Before
    public void setup() {

    }

    @Test
    public void find() throws Exception {
        User user = userDAO.find(2);

        assertNotNull(user);
        assertNull(user.getPassword());
    }

    @Test
    public void findByHashCode() throws Exception {
        User user = userDAO.findByHashCode(hashCodeFixture);

        assertNotNull(user);
        assertNull(user.getPassword());
    }

    @Test
    public void findByUsername() throws Exception {
        User user = userDAO.findByUsername("admin@freshcard.co");

        assertNotNull(user);
        assertNull(user.getPassword());
    }

    @Test
    public void findTemporaryUserByUsername() throws Exception {
        User user = userDAO.findTemporaryUserByUsername("temporary@freshcard.co");

        assertNotNull(user);
        assertNull(user.getPassword());
    }

    @Test
    public void getUserInfo() throws Exception {
        User user = userDAO.getUserInfo("admin@freshcard.co");

        assertNotNull(user);
        assertNull(user.getPassword());
    }

    @Test
    public void findEnabledUserByUsername() throws Exception {
        User user = userDAO.findEnabledUserByUsername("admin@freshcard.co");

        assertNotNull(user);
        assertNotNull(user.getPassword());
    }

    @Test
    public void getOAuthUser() throws Exception {
        User user = userDAO.getOAuthUser("some@testuser.com");

        assertNotNull(user);
        assertNotNull(user.getPassword());
    }

    @Test
    public void getUserInfoByUsername() throws Exception {
        User user = userDAO.getUserInfoByUsername("admin@freshcard.co");

        assertNotNull(user);
        assertNull(user.getPassword());
    }

    @Test
    public void insert() throws Exception {
        User user = new User();
        user.setUsername("test_username");
        user.setPassword("test");

        User newUser = userDAO.insert(user);
        assertNotNull(newUser);
    }

    @Test
    public void addUserToOrganization() throws Exception {
        Number id = userDAO.addUserToOrganization(2, 3);
        assertNotNull(id);
    }

    @Test
    public void addContactToUser() throws Exception {
        Number id = userDAO.addContactToUser(2, 2, 3);
        assertNotNull(id);
    }

    @Test
    public void update() throws Exception {
        User user = userDAO.find(3);
        user.setUsername("something");
        user = userDAO.update(3, user);

        assertEquals("something", user.getUsername());

        assertEquals("en", user.getPreferredLanguage());

        user.setPreferredLanguage(null);
        userDAO.update(3, user);
        user = userDAO.find(3);

        assertEquals("en", user.getPreferredLanguage());

        user.setPreferredLanguage("de");
        userDAO.update(3, user);
        user = userDAO.find(3);

        assertEquals("de", user.getPreferredLanguage());
    }

    @Test
    public void updatePassword() throws Exception {
        String username = "admin@freshcard.co";
        String newPassword = "new password";

        User user = userDAO.findEnabledUserByUsername(username);
        String oldPassword = user.getPassword();
        user.setPassword(newPassword);
        user.setConfirmed(true);
        user.setConfirmationHashCode("#test");

        userDAO.updatePassword(2, user);

        user = userDAO.findEnabledUserByUsername(username);
        assertNotEquals(oldPassword, user.getPassword());

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(username, newPassword);
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetails userDetails = userDAO.loadUserByUsername(username);

        assertNotNull(userDetails);
    }

    @Test
    public void updateCurrentOrganization() throws Exception {
        User user = userDAO.find(2);
        assertTrue(user.getCurrentOrganization().getId() == 2);
        user.setCurrentOrganization(organizationDAO.find(3));

        userDAO.updateCurrentOrganization(2, user);

        user = userDAO.find(2);

        assertTrue(user.getCurrentOrganization().getId() == 3);
    }

    @Test
    public void updateSynchronizationTimestamp() throws Exception {
        User user = userDAO.find(2);
        Timestamp lastSynchronizationAt = user.getSynchronizedAt();

        assertTrue(lastSynchronizationAt.before(new Timestamp(new Date().getTime())));

        userDAO.updateSynchronizationTimestamp(user.getId());

        user = userDAO.find(2);
        Timestamp nextSynchronizationAt = user.getSynchronizedAt();

        assertTrue(lastSynchronizationAt.before(nextSynchronizationAt));
        assertTrue(nextSynchronizationAt.before(new Timestamp(new Date().getTime())));
    }

    @Test
    public void updateConnectionSynchronizationTimestamp() throws Exception {
        User user = userDAO.getUserInfo("admin@freshcard.co");
        Timestamp connectionsLastUpdated = user.getConnectionsLastUpdated();

        assertTrue(connectionsLastUpdated.before(new Timestamp(new Date().getTime())));

        userDAO.updateConnectionSynchronizationTimestamp(user.getId(), new Timestamp(System.currentTimeMillis()));

        user = userDAO.getUserInfo("admin@freshcard.co");
        Timestamp nextConnectionsLastUpdated = user.getConnectionsLastUpdated();

        assertTrue(connectionsLastUpdated.before(nextConnectionsLastUpdated));
        assertTrue(nextConnectionsLastUpdated.before(new Timestamp(new Date().getTime())));
    }
}
