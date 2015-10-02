package com.freshcard.backend.api;

import com.freshcard.backend.model.dao.ContactDAO;
import com.freshcard.backend.model.dao.UserDAO;
import com.freshcard.backend.security.TokenTransfer;
import com.freshcard.backend.security.TokenUtil;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.jayway.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static com.jayway.restassured.module.mockmvc.RestAssuredMockMvc.standaloneSetup;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

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
public class ContactControllerTest {
    private String jCardFixture = "[\"vcard\",[[\"version\",{},\"text\",\"4.0\"],[\"n\",{},\"text\",[\"Doe\",\"John\",\"\",\"\",\"\"]],[\"fn\",{},\"text\",\"John Doe\"],[\"photo\",{\"type\":\"JPEG\"},\"uri\",\"data:image/jpeg;base64,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\"],[\"org\",{},\"text\",[[\"Example.com Inc.\",\"\"]]],[\"title\",{},\"text\",\"Imaginary test person\"],[\"email\",{\"type\":[\"INTERNET\",\"WORK\"],\"pref\":\"1\"},\"text\",\"johnDoe@example.org\"],[\"tel\",{\"type\":\"WORK\",\"pref\":\"1\"},\"text\",\"+1 617 555 1212\"],[\"tel\",{\"type\":\"WORK\"},\"text\",\"+1 (617) 555-1234\"],[\"tel\",{\"type\":\"CELL\"},\"text\",\"+1 781 555 1212\"],[\"tel\",{\"type\":\"HOME\"},\"text\",\"+1 202 555 1212\"],[\"adr\",{\"type\":\"WORK\",\"group\":\"item1\"},\"text\",[\"\",\"\",\"132 Hawthorne St\",\"San Francisco\",\"CA\",\"94107\",\"USA\"]],[\"adr\",{\"type\":\"HOME\",\"pref\":\"1\",\"group\":\"item2\"},\"text\",[\"\",\"\",\"Karen House 1-11 Bache's St\",\"London\",\"Greater London\",\"N1 6DL\",\"UK\"]],[\"x-abadr\",{\"group\":\"item1\"},\"unknown\",\"us\"],[\"x-abadr\",{\"group\":\"item2\"},\"unknown\",\"us\"],[\"x-ablabel\",{\"group\":\"item3\"},\"unknown\",\"_$!<HomePage>!$_\"],[\"x-ablabel\",{\"group\":\"item4\"},\"unknown\",\"FOAF\"],[\"x-abrelatednames\",{\"type\":\"pref\",\"group\":\"item5\"},\"unknown\",\"Jane Doe\"],[\"x-ablabel\",{\"group\":\"item5\"},\"unknown\",\"_$!<Friend>!$_\"],[\"x-abuid\",{},\"unknown\",\"5AD380FD-B2DE-4261-BA99-DE1D1DB52FBE\\\\\\\\:ABPerson\"],[\"note\",{},\"text\",\"John Doe has a long and varied history, being documented on more police files than anyone else. Reports of his death are alas numerous.\"],[\"url\",{\"type\":\"pref\",\"group\":\"item3\"},\"uri\",\"http://www.example/com/doe\"],[\"url\",{\"group\":\"item4\"},\"uri\",\"http://www.example.com/Joe/foaf.df\"],[\"categories\",{},\"text\",\"Work\",\"Test group\"],[\"prodid\",{},\"text\",\"ez-vcard 0.9.5\"]]]";
    private String hashCodeFixture = "b0a14ebcf7ab3475b2132db1b37a4a438a6acbf434daf489402442149c87cbe4a143ba2773293231";
    private String userHashCodeFixture = "4b4aeedfbbb1fc4f31ed83e9a626f6b2e15fdae310695a12d618aaba50ebf37be128700152015f12";
    private String otherUserHashCodeFixture = "15ffd80630c16d084295c2bd0110c3e26c9de7e1016b8c1b9f352e1424bbd77d5623ee22bc14f89f";
    private String temporaryUserHashCodeFixture = "1b4aeedfbbb1fc4f31ed83e9a626f6b2e15fdae310695a12d618aaba50ebf37be128700152015f18";
    private String vCardFixture = "BEGIN:VCARD\n" +
            "VERSION:3.0\n" +
            "N:Doe;John;;;\n" +
            "FN:John Doe\n" +
            "ORG:Example.com Inc.;\n" +
            "TITLE:Imaginary test person\n" +
            "EMAIL;type=INTERNET;type=WORK;type=pref:johnDoe@example.org\n" +
            "TEL;type=WORK;type=pref:+1 617 555 1212\n" +
            "TEL;type=WORK:+1 (617) 555-1234\n" +
            "TEL;type=CELL:+1 781 555 1212\n" +
            "TEL;type=HOME:+1 202 555 1212\n" +
            "item1.ADR;type=WORK:;;2 Enterprise Avenue;Worktown;NY;01111;USA\n" +
            "item1.X-ABADR:us\n" +
            "item2.ADR;type=HOME;type=pref:;;3 Acacia Avenue;Hoemtown;MA;02222;USA\n" +
            "item2.X-ABADR:us\n" +
            "NOTE:John Doe has a long and varied history\\, being documented on more police files that anyone else. Reports of his death are alas numerous.\n" +
            "item3.URL;type=pref:http\\://www.example/com/doe\n" +
            "item3.X-ABLabel:_$!<HomePage>!$_\n" +
            "item4.URL:http\\://www.example.com/Joe/foaf.df\n" +
            "item4.X-ABLabel:FOAF\n" +
            "item5.X-ABRELATEDNAMES;type=pref:Jane Doe\n" +
            "item5.X-ABLabel:_$!<Friend>!$_\n" +
            "CATEGORIES:Work,Test group\n" +
            "X-ABUID:5AD380FD-B2DE-4261-BA99-DE1D1DB52FBE\\:ABPerson\n" +
            "END:VCARD\n";
    private String usernameFixture = "admin@freshcard.co";
    private String otherUsernameFixture = "some@testuser.com";
    private String temporaryUsernameFixture = "temporary@freshcard.co";

    @Autowired
    private ContactController contactController;

    @Autowired
    private UserDAO userDAO;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    protected ContactDAO contactDAO;

    @Before
    public void setup() {
        contactController.setSkipJCard(true);
        standaloneSetup(contactController);
    }

    @Test
    public void getUserContacts() throws Exception {
        UserDetails userDetails = userDAO.loadUserByUsername(usernameFixture);
        TokenTransfer tokenTransfer = new TokenTransfer(TokenUtil.createToken(userDetails));

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                param("userId", 2).
                param("organizationId", 2).
                when().
                post("/api/v1/contacts/getUserContacts").
                then().
                statusCode(200).
                body("id[0]", equalTo(2)).
                body("vcard[0]", equalTo(jCardFixture)).
                body("hashCode[0]", equalTo(hashCodeFixture));
    }

    @Test
    public void getUserConnections() throws Exception {
        UserDetails userDetails = userDAO.loadUserByUsername(usernameFixture);
        TokenTransfer tokenTransfer = new TokenTransfer(TokenUtil.createToken(userDetails));

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                param("userId", 2).
                param("organizationId", 2).
                param("offset", 0).
                when().
                post("/api/v1/contacts/getUserConnections").
                then().
                statusCode(200);
    }

    @Test
    public void getCoWorkers() throws Exception {
        UserDetails userDetails = userDAO.loadUserByUsername(usernameFixture);
        TokenTransfer tokenTransfer = new TokenTransfer(TokenUtil.createToken(userDetails));

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                param("userId", 2).
                param("organizationId", 2).
                param("offset", 0).
                when().
                post("/api/v1/contacts/getCoWorkers").
                then().
                statusCode(200);
    }

    @Test
    public void getCoWorker() throws Exception {
        UserDetails userDetails = userDAO.loadUserByUsername(usernameFixture);
        TokenTransfer tokenTransfer = new TokenTransfer(TokenUtil.createToken(userDetails));

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                body("{\"userId\":5,\"organizationId\":4,\"contactId\":7}").
                when().
                post("/api/v1/contacts/getCoWorker").
                then().
                statusCode(200).
                body("id", equalTo(7));
    }

    @Test
    public void getUserContactsFromJSON() throws Exception {
        UserDetails userDetails = userDAO.loadUserByUsername(usernameFixture);
        TokenTransfer tokenTransfer = new TokenTransfer(TokenUtil.createToken(userDetails));

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                body("{\"userId\":2,\"organizationId\":2}").
                when().
                post("/api/v1/contacts/getUserContactsFromJSON").
                then().
                statusCode(200).
                body("id[0]", equalTo(2)).
                body("vcard[0]", equalTo(jCardFixture)).
                body("hashCode[0]", equalTo(hashCodeFixture));
    }

    @Test
    public void getUserConnectionsFromJSON() throws Exception {
        UserDetails userDetails = userDAO.loadUserByUsername(usernameFixture);
        TokenTransfer tokenTransfer = new TokenTransfer(TokenUtil.createToken(userDetails));

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                body("{\"userId\":2,\"organizationId\":2,\"offset\":0}").
                when().
                post("/api/v1/contacts/getUserConnectionsFromJSON").
                then().
                statusCode(200);
    }

    @Test
    public void getCoWorkersFromJSON() throws Exception {
        UserDetails userDetails = userDAO.loadUserByUsername(usernameFixture);
        TokenTransfer tokenTransfer = new TokenTransfer(TokenUtil.createToken(userDetails));

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                body("{\"userId\":2,\"organizationId\":2,\"offset\":0}").
                when().
                post("/api/v1/contacts/getCoWorkersFromJSON").
                then().
                statusCode(200);
    }

    @Test
    public void find() throws Exception {
        given().
                contentType("application/json").
                when().
                get("/api/v1/contacts/2").
                then().
                statusCode(200).
                body("id", equalTo(2)).
                body("vcard", equalTo(jCardFixture)).
                body("hashCode", equalTo(hashCodeFixture));
    }

    @Test
    public void findByHashCode() throws Exception {
        given().
                contentType("application/json").
                body("b0a14ebcf7ab3475b2132db1b37a4a438a6acbf434daf489402442149c87cbe4a143ba2773293231").
                when().
                post("/api/v1/contacts/findByHashCode").
                then().
                statusCode(200).
                body("id", equalTo(2)).
                body("vcard", equalTo(jCardFixture)).
                body("hashCode", equalTo(hashCodeFixture));
    }

    @Test
    public void getVCardsByHashCode() throws Exception {
        given().
                contentType("application/json").
                when().
                get("/api/v1/contacts/getVCardsByHashCode/" + hashCodeFixture).
                then().
                statusCode(200).
                contentType("text/vcard");
    }

    @Test
    public void addConnection() throws Exception {
        UserDetails userDetails = userDAO.loadUserByUsername(usernameFixture);
        TokenTransfer tokenTransfer = new TokenTransfer(TokenUtil.createToken(userDetails));

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                param("userId", 2).
                param("otherUsername", "some@testuser.com").
                param("organizationId", 2).
                when().
                post("/api/v1/contacts/addConnection").
                then().
                statusCode(200);

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                param("userId", 2).
                param("organizationId", 2).
                param("offset", 0).
                when().
                post("/api/v1/contacts/getUserConnections").
                then().
                statusCode(200).
                body("id[0]", equalTo(3));
    }

    @Test
    public void addConnectionFromJSON() throws Exception {
        UserDetails userDetails = userDAO.loadUserByUsername(usernameFixture);
        TokenTransfer tokenTransfer = new TokenTransfer(TokenUtil.createToken(userDetails));

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                body("{\"userId\":2,\"otherUsername\":\"some@testuser.com\",\"organizationId\":2}").
                when().
                post("/api/v1/contacts/addConnectionFromJSON").
                then().
                statusCode(200);

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                param("userId", 2).
                param("organizationId", 2).
                param("offset", 0).
                when().
                post("/api/v1/contacts/getUserConnections").
                then().
                statusCode(200).
                body("id[0]", equalTo(3));

        Long addedAtTimestamp = System.currentTimeMillis() + 300000;
        Long updatedAtTimestamp = System.currentTimeMillis() + 600000;

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                body("{\"userId\":2,\"otherUsername\":\"another@testuser.com\",\"organizationId\":2,\"notes\":\"Test note\",\"updatedOffline\":" + updatedAtTimestamp + ",\"addedAt\":" + addedAtTimestamp + "}").
                when().
                post("/api/v1/contacts/addConnectionFromJSON").
                then().
                statusCode(200);

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                param("userId", 2).
                param("organizationId", 2).
                param("offset", 0).
                when().
                post("/api/v1/contacts/getUserConnections").
                then().
                statusCode(200).
                body("id[0]", equalTo(3)).
                body("id[1]", equalTo(5)).
                body("notes[1]", equalTo("Test note")).
                body("updatedOffline[1]", equalTo(updatedAtTimestamp)).
                body("addedAt[1]", equalTo(addedAtTimestamp));

        updatedAtTimestamp = System.currentTimeMillis() + 1200000;

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                body("{\"userId\":2,\"otherContactId\":5,\"organizationId\":2,\"notes\":\"Updated another test note\",\"updatedOffline\":" + updatedAtTimestamp + "}").
                when().
                post("/api/v1/contacts/updateConnection").
                then().
                statusCode(200);

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                param("userId", 2).
                param("organizationId", 2).
                param("offset", 0).
                when().
                post("/api/v1/contacts/getUserConnections").
                then().
                statusCode(200).
                body("id[0]", equalTo(3)).
                body("id[1]", equalTo(5)).
                body("notes[1]", equalTo("Updated another test note")).
                body("updatedOffline[1]", equalTo(updatedAtTimestamp));
    }

    @Test
    public void addMutualConnection() throws Exception {
        UserDetails userDetails = userDAO.loadUserByUsername(usernameFixture);
        TokenTransfer tokenTransfer = new TokenTransfer(TokenUtil.createToken(userDetails));

        UserDetails otherUserDetails = userDAO.loadUserByUsername(otherUsernameFixture);
        TokenTransfer otherTokenTransfer = new TokenTransfer(TokenUtil.createToken(otherUserDetails));

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                param("userId", 2).
                param("organizationId", 2).
                param("offset", 0).
                when().
                post("/api/v1/contacts/getUserConnections").
                then().
                statusCode(200).
                body("id[0]", not(equalTo(3)));

        given().
                header("X-Auth-Token", otherTokenTransfer.getToken()).
                contentType("application/json").
                param("userId", 3).
                param("organizationId", 3).
                param("offset", 0).
                when().
                post("/api/v1/contacts/getUserConnections").
                then().
                statusCode(200).
                body("id[0]", not(equalTo(2)));

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                body("{\"hashCode\":\"" + userHashCodeFixture + "\",\"connectionHashCode\":\"" + otherUserHashCodeFixture + "\"}").
                when().
                post("/api/v1/contacts/addMutualConnection").
                then().
                statusCode(200);

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                param("userId", 2).
                param("organizationId", 2).
                param("offset", 0).
                when().
                post("/api/v1/contacts/getUserConnections").
                then().
                statusCode(200).
                body("id[0]", equalTo(3));

        given().
                header("X-Auth-Token", otherTokenTransfer.getToken()).
                contentType("application/json").
                param("userId", 3).
                param("organizationId", 3).
                param("offset", 0).
                when().
                post("/api/v1/contacts/getUserConnections").
                then().
                statusCode(200).
                body("id[0]", equalTo(2));

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                param("userId", 2).
                param("otherContactId", 3).
                param("organizationId", 2).
                when().
                post("/api/v1/contacts/removeConnection").
                then().
                statusCode(200);

        given().
                header("X-Auth-Token", otherTokenTransfer.getToken()).
                contentType("application/json").
                param("userId", 3).
                param("otherContactId", 2).
                param("organizationId", 3).
                when().
                post("/api/v1/contacts/removeConnection").
                then().
                statusCode(200);

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                param("userId", 2).
                param("organizationId", 2).
                param("offset", 0).
                when().
                post("/api/v1/contacts/getUserConnections").
                then().
                statusCode(200).
                body("id[0]", not(equalTo(3)));

        given().
                header("X-Auth-Token", otherTokenTransfer.getToken()).
                contentType("application/json").
                param("userId", 3).
                param("organizationId", 3).
                param("offset", 0).
                when().
                post("/api/v1/contacts/getUserConnections").
                then().
                statusCode(200).
                body("id[0]", not(equalTo(2)));
    }

    @Test
    public void addMutualConnectionWithTemporaryUser() throws Exception {
        UserDetails userDetails = userDAO.loadUserByUsername(usernameFixture);
        TokenTransfer tokenTransfer = new TokenTransfer(TokenUtil.createToken(userDetails));

        UserDetails otherUserDetails = userDAO.findTemporaryUserByUsername(temporaryUsernameFixture);
        TokenTransfer otherTokenTransfer = new TokenTransfer(TokenUtil.createToken(otherUserDetails));

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                param("userId", 2).
                param("organizationId", 2).
                param("offset", 0).
                when().
                post("/api/v1/contacts/getUserConnections").
                then().
                statusCode(200).
                body("id[0]", not(equalTo(3)));

        given().
                header("X-Auth-Token", otherTokenTransfer.getToken()).
                contentType("application/json").
                param("userId", 4).
                param("organizationId", 3).
                param("offset", 0).
                when().
                post("/api/v1/contacts/getUserConnections").
                then().
                statusCode(200).
                body("id[0]", not(equalTo(2)));

        given().
                contentType("application/json").
                body("{\"hashCode\":\"" + userHashCodeFixture + "\",\"connectionHashCode\":\"" + temporaryUserHashCodeFixture + "\"}").
                when().
                post("/api/v1/contacts/addMutualConnectionWithTemporaryUser").
                then().
                statusCode(200);

        given().
                header("X-Auth-Token", otherTokenTransfer.getToken()).
                contentType("application/json").
                param("userId", 4).
                param("organizationId", 3).
                param("offset", 0).
                when().
                post("/api/v1/contacts/getUserConnections").
                then().
                statusCode(200).
                body("id[0]", equalTo(2));

        given().
                header("X-Auth-Token", otherTokenTransfer.getToken()).
                contentType("application/json").
                param("userId", 4).
                param("otherContactId", 2).
                param("organizationId", 3).
                when().
                post("/api/v1/contacts/removeConnection").
                then().
                statusCode(200);

        given().
                header("X-Auth-Token", otherTokenTransfer.getToken()).
                contentType("application/json").
                param("userId", 4).
                param("organizationId", 3).
                param("offset", 0).
                when().
                post("/api/v1/contacts/getUserConnections").
                then().
                statusCode(200).
                body("id[0]", not(equalTo(2)));
    }

    @Test
    public void addCoWorker() throws Exception {
        UserDetails userDetails = userDAO.loadUserByUsername(usernameFixture);
        TokenTransfer tokenTransfer = new TokenTransfer(TokenUtil.createToken(userDetails));

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                param("userId", 2).
                param("otherUsername", "some@testuser.com").
                param("organizationId", 2).
                when().
                post("/api/v1/contacts/addCoWorker").
                then().
                statusCode(200);

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                param("userId", 2).
                param("organizationId", 2).
                param("offset", 0).
                when().
                post("/api/v1/contacts/getCoWorkers").
                then().
                statusCode(200).
                body("id[0]", equalTo(3));
    }

    @Test
    public void addCoWorkerFromJSON() throws Exception {
        UserDetails userDetails = userDAO.loadUserByUsername(usernameFixture);
        TokenTransfer tokenTransfer = new TokenTransfer(TokenUtil.createToken(userDetails));

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                body("{\"userId\":2,\"otherUsername\":\"some@testuser.com\",\"organizationId\":2}").
                when().
                post("/api/v1/contacts/addCoWorkerFromJSON").
                then().
                statusCode(200);

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                param("userId", 2).
                param("organizationId", 2).
                param("offset", 0).
                when().
                post("/api/v1/contacts/getCoWorkers").
                then().
                statusCode(200).
                body("id[0]", equalTo(3));
    }

    @Test
    public void removeConnection() throws Exception {
        UserDetails userDetails = userDAO.loadUserByUsername(usernameFixture);
        TokenTransfer tokenTransfer = new TokenTransfer(TokenUtil.createToken(userDetails));

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                param("userId", 2).
                param("otherUsername", "some@testuser.com").
                param("organizationId", 2).
                when().
                post("/api/v1/contacts/addConnection").
                then().
                statusCode(200);

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                param("userId", 2).
                param("organizationId", 2).
                param("offset", 0).
                when().
                post("/api/v1/contacts/getUserConnections").
                then().
                statusCode(200).
                body("id[0]", equalTo(3));

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                param("userId", 2).
                param("otherContactId", 3).
                param("organizationId", 2).
                when().
                post("/api/v1/contacts/removeConnection").
                then().
                statusCode(200);

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                param("userId", 2).
                param("organizationId", 2).
                param("offset", 0).
                when().
                post("/api/v1/contacts/getUserConnections").
                then().
                statusCode(200).
                body("id[0]", not(equalTo(3)));
    }

    @Test
    public void removeConnectionFromJSON() throws Exception {
        UserDetails userDetails = userDAO.loadUserByUsername(usernameFixture);
        TokenTransfer tokenTransfer = new TokenTransfer(TokenUtil.createToken(userDetails));

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                param("userId", 2).
                param("otherUsername", "some@testuser.com").
                param("organizationId", 2).
                when().
                post("/api/v1/contacts/addConnection").
                then().
                statusCode(200);

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                param("userId", 2).
                param("organizationId", 2).
                param("offset", 0).
                when().
                post("/api/v1/contacts/getUserConnections").
                then().
                statusCode(200).
                body("id[0]", equalTo(3));

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                body("{\"userId\":2,\"otherContactId\":3,\"organizationId\":2}").
                when().
                post("/api/v1/contacts/removeConnectionFromJSON").
                then().
                statusCode(200);

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                param("userId", 2).
                param("organizationId", 2).
                param("offset", 0).
                when().
                post("/api/v1/contacts/getUserConnections").
                then().
                statusCode(200).
                body("id[0]", not(equalTo(3)));
    }

    @Test
    public void removeConnectionByUsername() throws Exception {
        UserDetails userDetails = userDAO.loadUserByUsername(usernameFixture);
        TokenTransfer tokenTransfer = new TokenTransfer(TokenUtil.createToken(userDetails));

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                param("userId", 2).
                param("otherUsername", "some@testuser.com").
                param("organizationId", 2).
                when().
                post("/api/v1/contacts/addConnection").
                then().
                statusCode(200);

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                param("userId", 2).
                param("organizationId", 2).
                param("offset", 0).
                when().
                post("/api/v1/contacts/getUserConnections").
                then().
                statusCode(200).
                body("id[0]", equalTo(3));

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                param("userId", 2).
                param("otherUsername", "some@testuser.com").
                param("organizationId", 2).
                when().
                post("/api/v1/contacts/removeConnectionByUsername").
                then().
                statusCode(200);

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                param("userId", 2).
                param("organizationId", 2).
                param("offset", 0).
                when().
                post("/api/v1/contacts/getUserConnections").
                then().
                statusCode(200).
                body("id[0]", not(equalTo(3)));
    }

    @Test
    public void removeConnectionByUsernameFromJSON() throws Exception {
        UserDetails userDetails = userDAO.loadUserByUsername(usernameFixture);
        TokenTransfer tokenTransfer = new TokenTransfer(TokenUtil.createToken(userDetails));

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                param("userId", 2).
                param("otherUsername", "some@testuser.com").
                param("organizationId", 2).
                when().
                post("/api/v1/contacts/addConnection").
                then().
                statusCode(200);

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                param("userId", 2).
                param("organizationId", 2).
                param("offset", 0).
                when().
                post("/api/v1/contacts/getUserConnections").
                then().
                statusCode(200).
                body("id[0]", equalTo(3));

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                body("{\"userId\":2,\"otherUsername\":\"some@testuser.com\",\"organizationId\":2}").
                when().
                post("/api/v1/contacts/removeConnectionByUsernameFromJSON").
                then().
                statusCode(200);

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                param("userId", 2).
                param("organizationId", 2).
                param("offset", 0).
                when().
                post("/api/v1/contacts/getUserConnections").
                then().
                statusCode(200).
                body("id[0]", not(equalTo(3)));
    }

    @Test
    public void removeCoWorker() throws Exception {
        UserDetails userDetails = userDAO.loadUserByUsername(usernameFixture);
        TokenTransfer tokenTransfer = new TokenTransfer(TokenUtil.createToken(userDetails));

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                param("userId", 2).
                param("otherUsername", "some@testuser.com").
                param("organizationId", 2).
                when().
                post("/api/v1/contacts/addCoWorker").
                then().
                statusCode(200);

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                param("userId", 2).
                param("organizationId", 2).
                param("offset", 0).
                when().
                post("/api/v1/contacts/getCoWorkers").
                then().
                statusCode(200).
                body("id[0]", equalTo(3));

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                param("userId", 3).
                param("organizationId", 2).
                when().
                post("/api/v1/contacts/removeCoWorker").
                then().
                statusCode(200);

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                param("userId", 2).
                param("organizationId", 2).
                param("offset", 0).
                when().
                post("/api/v1/contacts/getCoWorkers").
                then().
                statusCode(200).
                body("id[0]", not(equalTo(3)));
    }

    @Test
    public void removeCoWorkerFromJSON() throws Exception {
        UserDetails userDetails = userDAO.loadUserByUsername(usernameFixture);
        TokenTransfer tokenTransfer = new TokenTransfer(TokenUtil.createToken(userDetails));

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                param("userId", 2).
                param("otherUsername", "some@testuser.com").
                param("organizationId", 2).
                when().
                post("/api/v1/contacts/addCoWorker").
                then().
                statusCode(200);

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                param("userId", 2).
                param("organizationId", 2).
                param("offset", 0).
                when().
                post("/api/v1/contacts/getCoWorkers").
                then().
                statusCode(200).
                body("id[0]", equalTo(3));

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                body("{\"userId\":3,\"organizationId\":2}").
                when().
                post("/api/v1/contacts/removeCoWorkerFromJSON").
                then().
                statusCode(200);

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                param("userId", 2).
                param("organizationId", 2).
                param("offset", 0).
                when().
                post("/api/v1/contacts/getCoWorkers").
                then().
                statusCode(200).
                body("id[0]", not(equalTo(3)));
    }

    @Test
    public void createFromVCardString() throws Exception {
        UserDetails userDetails = userDAO.loadUserByUsername(usernameFixture);
        TokenTransfer tokenTransfer = new TokenTransfer(TokenUtil.createToken(userDetails));

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                body("{\"vcard\":\"" + JSONObject.escape(vCardFixture) + "\",\"isTestRun\":true}").
                when().
                post("/api/v1/contacts/createFromVCardString/2").
                then().
                statusCode(200).
                body("numberOfImportedVCards", equalTo(1));
    }
}
