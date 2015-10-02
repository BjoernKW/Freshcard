package com.freshcard.backend.model.dao;

import com.freshcard.backend.model.Organization;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

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
public class OrganizationDAOTest {
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    protected OrganizationDAO organizationDAO;

    @Before
    public void setup() {

    }

    @Test
    public void find() throws Exception {
        assertNotNull(organizationDAO.find(2));
    }

    @Test
    public void findByName() throws Exception {
        assertNotNull(organizationDAO.findByName("Freshcard"));
    }

    @Test
    public void findByUserName() throws Exception {
        assertTrue(organizationDAO.findByUsername("admin@freshcard.co").size() > 0);
    }

    @Test
    public void insert() throws Exception {
        Organization organization = new Organization();
        organization.setName("Just testing");
        organization.setUrl("http.//testing.com");

        Number organizationId = organizationDAO.insert(organization);
        assertNotNull(organizationId);
    }

    @Test
    public void update() throws Exception {
        Organization organization = organizationDAO.find(3);
        organization.setName("Just testing");

        organizationDAO.setTestRun(true);
        organizationDAO.update(3, organization);

        organization = organizationDAO.find(3);

        assertEquals("Just testing", organization.getName());
    }
}
