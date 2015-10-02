package com.freshcard.backend.util;

import com.freshcard.backend.model.*;
import com.freshcard.backend.model.dao.ContactDAO;
import com.freshcard.backend.model.dao.OrganizationDAO;
import com.freshcard.backend.model.dao.RoleDAO;
import com.freshcard.backend.model.dao.UserDAO;
import com.freshcard.backend.security.ForbiddenException;
import com.fullcontact.api.libs.fullcontact4j.FullContact;
import com.fullcontact.api.libs.fullcontact4j.FullContactException;
import com.fullcontact.api.libs.fullcontact4j.entity.person.ContactInfo;
import com.fullcontact.api.libs.fullcontact4j.entity.person.PersonEntity;
import com.fullcontact.api.libs.fullcontact4j.handlers.PersonHandler;
import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.parameter.EmailType;
import ezvcard.property.Email;
import ezvcard.property.FormattedName;
import org.apache.log4j.Logger;
import org.springframework.dao.DuplicateKeyException;

import java.sql.Timestamp;
import java.util.List;
import java.util.Properties;

/**
 * Created by willy on 07.09.14.
 */
public class UserManagementHelper {
    private static final Logger logger = Logger.getLogger(UserManagementHelper.class);

    private UserDAO userDAO;

    private ContactDAO contactDAO;

    private OrganizationDAO organizationDAO;

    private RoleDAO roleDAO;

    private Boolean isTestRun = false;

    private Properties fullContactProperties;

    public User createUser(User user) {
        Organization organization = new Organization();
        organization.setName(user.getUsername());
        Number organizationId = organizationDAO.insert(organization);
        organization.setId(organizationId.intValue());
        user.setCurrentOrganization(organization);

        User newUser = userDAO.insert(user);

        userDAO.updateCurrentOrganization(newUser.getId(), newUser);

        Role userRole = new Role();
        userRole.setUsername(newUser.getUsername());
        userRole.setOrganizationId(organizationId.intValue());
        userRole.setAuthority(Role.ROLE_USER);
        roleDAO.insert(userRole);

        Role adminRole = new Role();
        adminRole.setUsername(newUser.getUsername());
        adminRole.setOrganizationId(organizationId.intValue());
        adminRole.setAuthority(Role.ROLE_ADMIN);
        roleDAO.insert(adminRole);

        userDAO.addUserToOrganization(newUser.getId(), organizationId.intValue());

        if (!isTestRun) {
            createContact(newUser, false);
        }

        return newUser;
    }

    public Boolean deleteUser(User user) {
        roleDAO.deleteByUsername(user.getUsername());

        user.setCurrentOrganization(null);
        userDAO.updateCurrentOrganization(user.getId(), user);
        for (Organization organization : user.getOrganizations()) {
            userDAO.removeUserFromOrganization(user.getId(), organization.getId());

            Organization remainingOrganization = organizationDAO.find(organization.getId());
            List<User> remainingUsers = null;
            if (remainingOrganization != null) {
                remainingUsers = remainingOrganization.getUsers();
            }
            if (remainingUsers == null || remainingUsers.size() <= 0) {
                organizationDAO.delete(organization.getId());
            }
        }

        userDAO.removeContactsForUser(user.getId());
        userDAO.delete(user.getId());

        return true;
    }

    public User updateUsername(User user, String newUsername) {
        User updatedUser = user;

        List<Role> roles = userDAO.getAuthorities(user.getUsername(), user.getCurrentOrganization().getId());
        roleDAO.deleteByUsername(user.getUsername());

        user.setUsername(newUsername);
        userDAO.updateUsername(user.getId(), user);
        updatedUser.setUsername(newUsername);

        for (Role role : roles) {
            Role newRole = new Role();
            newRole.setUsername(newUsername);
            newRole.setOrganizationId(role.getOrganizationId());
            newRole.setAuthority(role.getAuthority());
            roleDAO.insert(newRole);
        }

        return updatedUser;
    }

    public Number createContact(User user, Boolean skipJCard) {
        contactDAO.setSkipJCard(skipJCard);

        Contact primaryContact = new Contact();
        primaryContact.setVcard(createVCard(user).writeJson());
        Number contactId = contactDAO.insert(primaryContact);

        userDAO.addContactToUser(contactId.intValue(), user.getId(), user.getCurrentOrganization().getId());

        return contactId;
    }

    public Contact addConnection(Integer userId, String otherUsername, Integer organizationId, Boolean isTemporaryUser, String notes, Long updatedOffline, Long addedAt, Boolean skipJCard) throws NotFoundException, ForbiddenException {
        contactDAO.setSkipJCard(skipJCard);

        User user = userDAO.find(userId);
        if (user == null) {
            throw new NotFoundException("User not found");
        }

        User otherUser = getUser(otherUsername, isTemporaryUser);

        Organization organization = organizationDAO.find(organizationId);
        if (organization == null) {
            throw new NotFoundException("Organization not found");
        }

        List<Contact> contactList = contactDAO.getUserContacts(otherUser.getId(), otherUser.getCurrentOrganization().getId());
        if (contactList.size() <= 0) {
            createContact(otherUser, skipJCard);
        }

        Contact contact;

        Timestamp updatedOfflineTimestamp = new Timestamp(System.currentTimeMillis());
        if (updatedOffline != null) {
            updatedOfflineTimestamp = new Timestamp(updatedOffline);
        }
        Timestamp addedAtTimestamp = new Timestamp(System.currentTimeMillis());
        if (addedAt != null) {
            addedAtTimestamp = new Timestamp(addedAt);
        }
        try {
            contact = contactDAO.addConnection(user.getId(), otherUser, organization.getId(), notes, updatedOfflineTimestamp, addedAtTimestamp);
            if (contact == null) {
                throw new NotFoundException("Contact data not found");
            }
            userDAO.updateConnectionSynchronizationTimestamp(user.getId(), updatedOfflineTimestamp);
        } catch (DuplicateKeyException e) {
            logger.info(e.getMessage());

            throw new ForbiddenException("Can't add yourself as contact");
        }

        return contact;
    }

    public Boolean addCoWorker(String otherUsername, Integer organizationId, Boolean isTemporaryUser, Boolean skipJCard) throws NotFoundException {
        contactDAO.setSkipJCard(skipJCard);

        User otherUser = getUser(otherUsername, isTemporaryUser);

        List<Contact> contactList = contactDAO.getUserContacts(otherUser.getId(), organizationId);
        if (contactList.size() <= 0) {
            createContact(otherUser, skipJCard);
        }

        Boolean coWorkerAdded = contactDAO.addCoWorker(otherUser.getId(), organizationId);

        return coWorkerAdded;
    }

    private User getUser(String otherUsername, Boolean isTemporaryUser) throws NotFoundException {
        User otherUser;
        if (isTemporaryUser) {
            otherUser = userDAO.findTemporaryUserByUsername(otherUsername);
            if (otherUser == null) {
                otherUser = userDAO.findByUsername(otherUsername);
            }
        } else {
            otherUser = userDAO.findByUsername(otherUsername);
        }
        if (otherUser == null) {
            throw new NotFoundException("Other user not found");
        }
        return otherUser;
    }

    public void setUserDAO(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public void setOrganizationDAO(OrganizationDAO organizationDAO) {
        this.organizationDAO = organizationDAO;
    }

    public void setRoleDAO(RoleDAO roleDAO) {
        this.roleDAO = roleDAO;
    }

    public void setContactDAO(ContactDAO contactDAO) {
        this.contactDAO = contactDAO;
    }

    public VCard createVCard(User user) {
        VCard vcard = new VCard();

        vcard.addEmail(user.getUsername(), EmailType.WORK);

        if (user.getUsername() != null) {
            FullContact fullContact = new FullContact(fullContactProperties.getProperty("fullContact.apiKey"));
            PersonHandler personHandler = fullContact.getPersonHandler();
            try {
                PersonEntity person = personHandler.getPersonInformation(user.getUsername());

                if (person != null) {
                    ContactInfo contactInfo = person.getContactInfo();
                    if (contactInfo != null) {
                        vcard.addFormattedName(new FormattedName(contactInfo.getFullName()));
                    }
                }
            } catch (FullContactException e) {
                logger.debug(e.getMessage());
            }
        }

        return vcard;
    }

    public void setIsTestRun(Boolean isTestRun) {
        this.isTestRun = isTestRun;
    }

    public void setFullContactProperties(Properties fullContactProperties) {
        this.fullContactProperties = fullContactProperties;
    }

    public Boolean isAuthorizedForOrganization(String username, Integer organizationId) {
        Boolean isAuthorized = false;
        List<Organization> organizations = organizationDAO.findByUsername(username);
        for (Organization organization : organizations) {
            if (organization.getId() == organizationId) {
                isAuthorized = true;
            }
        }

        return isAuthorized;
    }

    public Boolean isAdmin(String username, Integer organizationId) {
        Boolean isAdmin = false;

        for (Role role : userDAO.getAuthorities(username, organizationId)) {
            if (role.getAuthority().equals(Role.ROLE_ADMIN)) {
                isAdmin = true;
            }
        }

        return isAdmin;
    }

    public Boolean isUserAuthorizedForEMailAddress(User user, String fromAddress) {
        Boolean authorized = false;

        if (user != null) {
            user.setOwnContacts(contactDAO.getUserContacts(user.getId(), user.getCurrentOrganization().getId()));
            List<Contact> contacts = user.getOwnContacts();
            if (contacts != null) {
                for (Contact contact : contacts) {
                    List<VCard> vCards = Ezvcard.parseJson(contact.getVcard()).all();
                    if (vCards != null) {
                        for (VCard vCard : vCards) {
                            List<Email> eMailAddresses = vCard.getEmails();
                            if (eMailAddresses != null) {
                                for (Email eMailAddress : eMailAddresses) {
                                    if (eMailAddress.getValue().equals(fromAddress)) {
                                        authorized = true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return authorized;
    }

    public Boolean isOrganizationAuthenticated(User authenticatedUser, User user) {
        Boolean organizationAuthenticated = false;

        for (Organization organization : user.getOrganizations()) {
            for (Organization authenticatedOrganization : authenticatedUser.getOrganizations()) {
                if (organization.getId() == authenticatedOrganization.getId()
                        && organization.getHashCode() == authenticatedOrganization.getHashCode()) {
                    organizationAuthenticated = true;
                }
            }
        }

        return organizationAuthenticated;
    }

    public Boolean isAuthorizedForContact(Integer userId, Integer contactId) {
        Boolean isAuthorized = false;
        List<Contact> contacts = contactDAO.findByUserId(userId);
        for (Contact contact : contacts) {
            if (contactId == contact.getId()) {
                isAuthorized = true;
            }
        }

        return isAuthorized;
    }
}
