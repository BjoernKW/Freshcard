package com.freshcard.backend.model.dao;

import com.freshcard.backend.model.Contact;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.sql.Timestamp;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(
        locations = {
                "classpath:applicationContext.xml",
                "classpath:dispatcher-servlet.xml",
                "file:src/main/webapp/WEB-INF/spring-security.xml"
        }
)
public class ContactDAOTest {
    private String hashCodeFixture = "b0a14ebcf7ab3475b2132db1b37a4a438a6acbf434daf489402442149c87cbe4a143ba2773293231";

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    protected ContactDAO contactDAO;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    protected UserDAO userDAO;

    @Before
    public void setup() {
        contactDAO.setSkipJCard(true);
    }

    @Test
    public void find() throws Exception {
        assertNotNull(contactDAO.find(2));
    }

    @Test
    public void getUserConnection() throws Exception {
        assertNotNull(contactDAO.getUserConnection(5, 7, 3));
    }

    @Test
    public void getCoWorker() throws Exception {
        assertNotNull(contactDAO.getCoWorker(5, 7, 4));
    }

    @Test
    public void findByHashCode() throws Exception {
        assertNotNull(contactDAO.findByHashCode(hashCodeFixture));
    }

    @Test
    public void findByUserId() throws Exception {
        assertTrue(contactDAO.findByUserId(2).size() > 0);
    }

    @Test
    public void getUserContacts() throws Exception {
        assertTrue(contactDAO.getUserContacts(2, 2).size() > 0);
    }

    @Test
    public void manageUserConnections() throws Exception {
        Integer before = contactDAO.getAllConnections().size();

        Contact contact = contactDAO.addConnection(2, userDAO.find(3), 2);
        assertNotNull(contact);

        assertTrue(contactDAO.getUserConnections(2, 2).size() > 0);

        Integer afterAdd = contactDAO.getAllConnections().size();
        assertTrue(afterAdd > before);
        assertTrue(contactDAO.getConnectionsByContactId(3).size() == 1);

        Boolean removed = contactDAO.removeConnection(2, 3, 2);
        assertTrue(removed);

        Integer afterRemove = contactDAO.getAllConnections().size();
        assertTrue(afterRemove < afterAdd);

        contact = contactDAO.addConnection(2, userDAO.find(3), 2);
        assertNotNull(contact);

        Integer afterAddForUser = contactDAO.getUserConnections(2, 2).size();
        assertTrue(afterAddForUser > 0);

        removed = contactDAO.removeConnectionByUserId(2, 3, 2);
        assertTrue(removed);

        Integer afterRemoveForUser = contactDAO.getUserConnections(2, 2).size();
        assertTrue(afterRemoveForUser < afterAddForUser);

        contact = contactDAO.addConnection(2, userDAO.find(3), 2);
        assertNotNull(contact);

        afterAddForUser = contactDAO.getUserConnections(2, 2).size();
        assertTrue(afterAddForUser > 0);

        removed = contactDAO.removePrimaryContactConnectionByUserId(2, 3, 2);
        assertTrue(removed);

        afterRemoveForUser = contactDAO.getUserConnections(2, 2).size();
        assertTrue(afterRemoveForUser < afterAddForUser);
    }

    @Test
    public void addUserConnectionWithNote() throws Exception {
        Long time = System.currentTimeMillis() - 100000;
        Long futureTime = System.currentTimeMillis() + 100000;

        Contact newContact = contactDAO.addConnection(2, userDAO.find(3), 2, "Test note", new Timestamp(time), new Timestamp(futureTime));
        assertNotNull(newContact);

        Boolean notePresent = false;
        Integer contactId = 0;

        List<Contact> contactList = contactDAO.getUserConnections(2, 2);
        for (Contact contact : contactList) {
            if (contact.getNotes() != null && contact.getNotes().equals("Test note")) {
                notePresent = true;
                contactId = contact.getId();
                assertTrue(contact.getUpdatedOffline().before(new Timestamp(System.currentTimeMillis())));
                assertTrue(contact.getUpdatedOffline().equals(new Timestamp(time)));
                assertTrue(contact.getAddedAt().after(new Timestamp(System.currentTimeMillis())));
                assertTrue(contact.getAddedAt().equals(new Timestamp(futureTime)));
            }
        }

        assertTrue(notePresent);

        Boolean successful = contactDAO.updateConnection(2, contactId, 2, "Updated test note", new Timestamp(time));
        assertTrue(successful);

        Boolean noteUpdated = false;

        contactList = contactDAO.getUserConnections(2, 2);
        for (Contact contact : contactList) {
            if (contact.getNotes() != null && contact.getNotes().equals("Updated test note")) {
                noteUpdated = true;
                assertTrue(contact.getUpdatedOffline().before(new Timestamp(System.currentTimeMillis())));
                assertTrue(contact.getUpdatedOffline().equals(new Timestamp(time)));
            }
        }

        assertTrue(noteUpdated);
    }

    @Test
    public void addContact() throws Exception {
        Long time = System.currentTimeMillis() - 100000;
        Long futureTime = System.currentTimeMillis() + 100000;

        Number contactID = contactDAO.addContact(2, 2, contactDAO.find(5), new Timestamp(time), new Timestamp(futureTime));
        assertNotNull(contactID);

        List<Contact> contactList = contactDAO.getUserContacts(2, 2);
        for (Contact contact : contactList) {
            if (contact.getId() == 3) {
                assertTrue(contact.getUpdatedOffline().before(new Timestamp(System.currentTimeMillis())));
                assertTrue(contact.getUpdatedOffline().equals(new Timestamp(time)));
                assertTrue(contact.getAddedAt().after(new Timestamp(System.currentTimeMillis())));
                assertTrue(contact.getAddedAt().equals(new Timestamp(futureTime)));
            }
        }

        Long nextTime = System.currentTimeMillis() - 200000;
        Long nextFutureTime = System.currentTimeMillis() + 200000;

        contactDAO.updateConnectionTimestamps(2, 2, 3, new Timestamp(nextTime), new Timestamp(nextFutureTime));

        contactList = contactDAO.getUserContacts(2, 2);
        for (Contact contact : contactList) {
            if (contact.getId() == 3) {
                assertTrue(contact.getUpdatedOffline().before(new Timestamp(System.currentTimeMillis())));
                assertTrue(contact.getUpdatedOffline().before(new Timestamp(time)));
                assertTrue(contact.getUpdatedOffline().equals(new Timestamp(nextTime)));
                assertTrue(contact.getAddedAt().after(new Timestamp(System.currentTimeMillis())));
                assertTrue(contact.getAddedAt().after(new Timestamp(futureTime)));
                assertTrue(contact.getAddedAt().equals(new Timestamp(nextFutureTime)));
            }
        }
    }

    @Test
    public void manageUserConnectionsWithOffset() throws Exception {
        Contact contact = contactDAO.addConnection(2, userDAO.find(3), 2);
        assertNotNull(contact);

        Integer afterAdd = contactDAO.getUserConnections(2, 2, 0).size();
        assertTrue(afterAdd > 0);

        Boolean removed = contactDAO.removeConnection(2, 3, 2);
        assertTrue(removed);
        Integer afterRemove = contactDAO.getUserConnections(2, 2, 0).size();
        assertTrue(afterAdd > afterRemove);

        contactDAO.removeConnection(2, 5, 2);

        Integer afterSecondRemove= contactDAO.getUserConnections(2, 2, 0).size();
        assertTrue(afterSecondRemove < afterRemove);
    }

    @Test
    public void manageCoWorkers() throws Exception {
        Boolean added = contactDAO.addCoWorker(3, 2);
        assertTrue(added);

        Integer numberOfCoWorkers = contactDAO.getCoWorkers(2, 2, 0).size();
        assertTrue(numberOfCoWorkers > 0);

        Boolean removed = contactDAO.removeCoWorker(3, 2);
        assertTrue(removed);

        assertTrue(contactDAO.getCoWorkers(2, 2, 0).size() < numberOfCoWorkers);
    }
}
