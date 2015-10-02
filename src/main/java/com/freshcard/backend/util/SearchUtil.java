package com.freshcard.backend.util;

import com.algolia.search.saas.APIClient;
import com.algolia.search.saas.AlgoliaException;
import com.algolia.search.saas.Index;
import com.freshcard.backend.model.Contact;
import com.freshcard.backend.model.dao.ContactDAO;
import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.property.Photo;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by willy on 16.05.14.
 */
public class SearchUtil {
    private static final Logger logger = Logger.getLogger(SearchUtil.class);

    private ContactDAO contactDAO;

    private Index index;

    public SearchUtil() {

    }

    public void initialize() {
        contactDAO.initializeSearchIndex();
        index = contactDAO.getIndex();

        List<Contact> contactList = contactDAO.getAllConnections();
        List<JSONObject> jsonContacts = new ArrayList<JSONObject>();
        for (Contact contact : contactList) {
            jsonContacts.add(contactDAO.convertContactToJsonObject(contact));

            if (jsonContacts.size() == 10000) {
                try {
                    index.saveObjects(jsonContacts);
                } catch (AlgoliaException e) {
                    logger.debug(e.getMessage());
                }
                jsonContacts.clear();
            }
        }

        if (!jsonContacts.isEmpty()) {
            try {
                index.saveObjects(jsonContacts);
            } catch (AlgoliaException e) {
                logger.debug(e.getMessage());
            }
        }
    }

    public void setContactDAO(ContactDAO contactDAO) {
        this.contactDAO = contactDAO;
    }
}
