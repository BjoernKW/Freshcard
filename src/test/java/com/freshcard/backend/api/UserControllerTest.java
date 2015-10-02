package com.freshcard.backend.api;

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
public class UserControllerTest {
    private String usernameFixture = "admin@freshcard.co";
    private String newUsernameFixture = "test@test.de";
    private String yetAnotherUsernameFixture = "yetanother@testuser.com";
    private String hashCodeFixture = "4b4aeedfbbb1fc4f31ed83e9a626f6b2e15fdae310695a12d618aaba50ebf37be128700152015f12";
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

    private String jCardFixture = "[\"vcard\",[[\"version\",{},\"text\",\"4.0\"],[\"n\",{},\"text\",[\"Doe\",\"John\",\"\",\"\",\"\"]],[\"fn\",{},\"text\",\"John Doe\"],[\"org\",{},\"text\",[[\"Example.com Inc.\",\"\"]]],[\"title\",{},\"text\",\"Imaginary test person\"],[\"email\",{\"type\":[\"INTERNET\",\"WORK\"],\"pref\":\"1\"},\"text\",\"johnDoe@example.org\"],[\"tel\",{\"type\":\"WORK\",\"pref\":\"1\"},\"text\",\"+1 617 555 1212\"],[\"tel\",{\"type\":\"WORK\"},\"text\",\"+1 (617) 555-1234\"],[\"tel\",{\"type\":\"CELL\"},\"text\",\"+1 781 555 1212\"],[\"tel\",{\"type\":\"HOME\"},\"text\",\"+1 202 555 1212\"],[\"adr\",{\"type\":\"WORK\",\"group\":\"item1\"},\"text\",[\"\",\"\",\"2 Enterprise Avenue\",\"Worktown\",\"NY\",\"01111\",\"USA\"]],[\"adr\",{\"type\":\"HOME\",\"pref\":\"1\",\"group\":\"item2\"},\"text\",[\"\",\"\",\"3 Acacia Avenue\",\"Hoemtown\",\"MA\",\"02222\",\"USA\"]],[\"x-abadr\",{\"group\":\"item1\"},\"unknown\",\"us\"],[\"x-abadr\",{\"group\":\"item2\"},\"unknown\",\"us\"],[\"x-ablabel\",{\"group\":\"item3\"},\"unknown\",\"_$!<HomePage>!$_\"],[\"x-ablabel\",{\"group\":\"item4\"},\"unknown\",\"FOAF\"],[\"x-abrelatednames\",{\"type\":\"pref\",\"group\":\"item5\"},\"unknown\",\"Jane Doe\"],[\"x-ablabel\",{\"group\":\"item5\"},\"unknown\",\"_$!<Friend>!$_\"],[\"x-abuid\",{},\"unknown\",\"5AD380FD-B2DE-4261-BA99-DE1D1DB52FBE\\\\:ABPerson\"],[\"note\",{},\"text\",\"John Doe has a long and varied history, being documented on more police files that anyone else. Reports of his death are alas numerous.\"],[\"url\",{\"type\":\"pref\",\"group\":\"item3\"},\"uri\",\"http://www.example/com/doe\"],[\"url\",{\"group\":\"item4\"},\"uri\",\"http://www.example.com/Joe/foaf.df\"],[\"categories\",{},\"text\",\"Work\",\"Test group\"],[\"prodid\",{},\"text\",\"ez-vcard 0.9.5\"]]]\n";

    @Autowired
    private UserController userController;

    @Autowired
    private UserDAO userDAO;

    @Before
    public void setup() {
        userController.setSendMail(false);
        userController.setSkipJCard(true);
        standaloneSetup(userController);
    }

    @Test
    public void find() throws Exception {
        UserDetails userDetails = userDAO.loadUserByUsername(usernameFixture);
        TokenTransfer tokenTransfer = new TokenTransfer(TokenUtil.createToken(userDetails));

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                when().
                get("/api/v1/users/2").
                then().
                statusCode(200).
                body("id", equalTo(2)).
                body("username", equalTo(usernameFixture)).
                body("currentOrganization.id", equalTo(2)).
                body("hashCode", equalTo(hashCodeFixture));
    }

    @Test
    public void findByHashCode() throws Exception {
        given().
                contentType("application/json").
                body(hashCodeFixture).
                when().
                post("/api/v1/users/findByHashCode").
                then().
                statusCode(200).
                body("id", equalTo(2)).
                body("username", equalTo(usernameFixture)).
                body("hashCode", equalTo(hashCodeFixture));
    }

    @Test
    public void findByUsername() throws Exception {
        UserDetails userDetails = userDAO.loadUserByUsername(usernameFixture);
        TokenTransfer tokenTransfer = new TokenTransfer(TokenUtil.createToken(userDetails));

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                body(usernameFixture).
                when().
                post("/api/v1/users/findByUsername").
                then().
                statusCode(200).
                body("id", equalTo(2)).
                body("username", equalTo(usernameFixture)).
                body("hashCode", equalTo(hashCodeFixture));
    }

    @Test
    public void getUserInfo() throws Exception {
        UserDetails userDetails = userDAO.loadUserByUsername(usernameFixture);
        TokenTransfer tokenTransfer = new TokenTransfer(TokenUtil.createToken(userDetails));

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                body("{\"username\":\"admin@freshcard.co\"}").
                when().
                post("/api/v1/users/getUserInfo").
                then().
                statusCode(200).
                body("id", equalTo(2)).
                body("username", equalTo(usernameFixture)).
                body("hashCode", equalTo(hashCodeFixture));
    }

    @Test
    public void getVCardsByHashCode() throws Exception {
        given().
                contentType("application/json").
                when().
                get("/api/v1/users/getVCardsByHashCode/" + hashCodeFixture).
                then().
                statusCode(200).
                contentType("text/vcard");
    }

    @Test
    public void updateCurrentOrganization() throws Exception {
        UserDetails userDetails = userDAO.loadUserByUsername(usernameFixture);
        TokenTransfer tokenTransfer = new TokenTransfer(TokenUtil.createToken(userDetails));

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                body("{\"id\":2,\"username\":\"admin@freshcard.co\",\"currentOrganization\":{\"id\":3}}").
                when().
                post("/api/v1/users/updateCurrentOrganization").
                then().
                statusCode(200);

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                when().
                get("/api/v1/users/2").
                then().
                statusCode(200).
                body("id", equalTo(2)).
                body("username", equalTo(usernameFixture)).
                body("currentOrganization.id", equalTo(3)).
                body("hashCode", equalTo(hashCodeFixture));

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                body("{\"id\":2,\"username\":\"admin@freshcard.co\",\"currentOrganization\":{\"id\":2}}").
                when().
                post("/api/v1/users/updateCurrentOrganization").
                then().
                statusCode(200);

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                when().
                get("/api/v1/users/2").
                then().
                statusCode(200).
                body("id", equalTo(2)).
                body("username", equalTo(usernameFixture)).
                body("currentOrganization.id", equalTo(2)).
                body("hashCode", equalTo(hashCodeFixture));
    }

    @Test
    public void sendPasswordResetLink() throws Exception {
        given().
                contentType("application/json").
                body("{\"id\":2,\"username\":\"admin@freshcard.co\",\"currentOrganization\":{\"id\":2}}").
                when().
                post("/api/v1/users/sendPasswordResetLink/").
                then().
                statusCode(200);
    }

    @Test
    public void sendEMailConnectionRequest() throws Exception {
        UserDetails userDetails = userDAO.loadUserByUsername(usernameFixture);
        TokenTransfer tokenTransfer = new TokenTransfer(TokenUtil.createToken(userDetails));

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                param("username", "admin@freshcard.co").
                param("recipientEMailAddress", "test@freshcard.co").
                when().
                post("/api/v1/users/sendEMailConnectionRequest/").
                then().
                statusCode(200);
    }

    @Test
    public void sendEMailConnectionRequestFromJSON() throws Exception {
        UserDetails userDetails = userDAO.loadUserByUsername(usernameFixture);
        TokenTransfer tokenTransfer = new TokenTransfer(TokenUtil.createToken(userDetails));

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                body("{\"username\":\"admin@freshcard.co\",\"recipientEMailAddress\":\"test@freshcard.co\"}").
                when().
                post("/api/v1/users/sendEMailConnectionRequestFromJSON/").
                then().
                statusCode(200);
    }

    @Test
    public void addCoWorkerViaEMail() throws Exception {
        UserDetails userDetails = userDAO.loadUserByUsername(usernameFixture);
        TokenTransfer tokenTransfer = new TokenTransfer(TokenUtil.createToken(userDetails));

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                body("{\"username\":\"admin@freshcard.co\",\"recipientEMailAddress\":\"test@freshcard.co\",\"organizationId\":4}").
                when().
                post("/api/v1/users/addCoWorkerViaEMail/").
                then().
                statusCode(200);
    }

    @Test
    public void changePassword() throws Exception {
        given().
                contentType("application/json").
                body("{\"id\":2,\"username\":\"admin@freshcard.co\",\"currentOrganization\":{\"id\":2}}").
                when().
                post("/api/v1/users/changePassword/").
                then().
                statusCode(200);
    }

    @Test
    public void checkByUsername() throws Exception {
        given().
                contentType("application/json").
                body(usernameFixture).
                when().
                post("/api/v1/users/checkByUsername").
                then().
                statusCode(403);

        given().
                contentType("application/json").
                body("ADMIN@freshcard.co").
                when().
                post("/api/v1/users/checkByUsername").
                then().
                statusCode(403);

        given().
                contentType("application/json").
                body("non-existing-username").
                when().
                post("/api/v1/users/checkByUsername").
                then().
                statusCode(200);
    }

    @Test
    public void checkIfUserExists() throws Exception {
        given().
                contentType("application/json").
                body("{\"username\":\"admin@freshcard.co\"}").
                when().
                post("/api/v1/users/checkIfUserExists").
                then().
                statusCode(403);

        given().
                contentType("application/json").
                body("{\"username\":\"ADMIN@freshcard.co\"}").
                when().
                post("/api/v1/users/checkIfUserExists").
                then().
                statusCode(403);

        given().
                contentType("application/json").
                body("{\"username\":\"something@non-existing-username.co\"}").
                when().
                post("/api/v1/users/checkIfUserExists").
                then().
                statusCode(200);
    }

    @Test
    public void signUp() throws Exception {
        given().
                contentType("application/json").
                body(newUsernameFixture).
                when().
                post("/api/v1/users/checkByUsername").
                then().
                statusCode(200);

        given().
                contentType("application/json").
                body("{\"username\":\"" + newUsernameFixture + "\",\"password\":\"test\"}").
                when().
                post("/api/v1/users/signUp").
                then().
                statusCode(200);

        given().
                contentType("application/json").
                body(newUsernameFixture).
                when().
                post("/api/v1/users/checkByUsername").
                then().
                statusCode(403);

        given().
                contentType("application/json").
                body("test2@test.de").
                when().
                post("/api/v1/users/checkByUsername").
                then().
                statusCode(200);

        given().
                contentType("application/json").
                body("{\"username\":\"TEST2@test.de\",\"password\":\"test\"}").
                when().
                post("/api/v1/users/signUp").
                then().
                statusCode(200);

        given().
                contentType("application/json").
                body("test2@test.de").
                when().
                post("/api/v1/users/checkByUsername").
                then().
                statusCode(403);
    }

    @Test
    public void updatePassword() throws Exception {
        UserDetails userDetails = userDAO.loadUserByUsername(usernameFixture);
        TokenTransfer tokenTransfer = new TokenTransfer(TokenUtil.createToken(userDetails));

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                body("{\"id\":2,\"password\":\"test_new\"}").
                when().
                post("/api/v1/users/updatePassword/").
                then().
                statusCode(200);

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                body("{\"id\":3,\"password\":\"test_new\"}").
                when().
                post("/api/v1/users/updatePassword/").
                then().
                statusCode(401);
    }

    @Test
    public void updateUsername() throws Exception {
        String newUsername = "yetanother@new-testuser.com";

        UserDetails userDetails = userDAO.loadUserByUsername(yetAnotherUsernameFixture);
        TokenTransfer tokenTransfer = new TokenTransfer(TokenUtil.createToken(userDetails));

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                when().
                get("/api/v1/users/6").
                then().
                statusCode(200).
                body("id", equalTo(6)).
                body("username", equalTo(yetAnotherUsernameFixture));

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                body("{\"id\":6,\"newUsername\":\"" + newUsername + "\"}").
                when().
                post("/api/v1/users/updateUsername/").
                then().
                statusCode(200);

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                when().
                get("/api/v1/users/6").
                then().
                statusCode(401);

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                body("{\"id\":5,\"newUsername\":\"" + newUsername + "\"}").
                when().
                post("/api/v1/users/updateUsername/").
                then().
                statusCode(401);

        userDetails = userDAO.loadUserByUsername(newUsername);
        tokenTransfer = new TokenTransfer(TokenUtil.createToken(userDetails));

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                when().
                get("/api/v1/users/6").
                then().
                statusCode(200).
                body("id", equalTo(6)).
                body("username", equalTo(newUsername));

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                body("{\"id\":5,\"newUsername\":\"" + newUsername + "\"}").
                when().
                post("/api/v1/users/updateUsername/").
                then().
                statusCode(401);
    }

    @Test
    public void finishSynchronization() throws Exception {
        UserDetails userDetails = userDAO.loadUserByUsername(usernameFixture);
        TokenTransfer tokenTransfer = new TokenTransfer(TokenUtil.createToken(userDetails));

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                when().
                get("/api/v1/users/finishSynchronization/2").
                then().
                statusCode(200);
    }

    @Test
    public void createVCardsFromString() throws Exception {
        UserDetails userDetails = userDAO.loadUserByUsername(usernameFixture);
        TokenTransfer tokenTransfer = new TokenTransfer(TokenUtil.createToken(userDetails));

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                body("{\"vcard\":\"" + JSONObject.escape(vCardFixture) + "\",\"isTestRun\":true}").
                when().
                post("/api/v1/users/createVCardsFromString/2").
                then().
                statusCode(200).
                body("numberOfImportedVCards", equalTo(1));
    }

    @Test
    public void updateVCards() throws Exception {
        UserDetails userDetails = userDAO.loadUserByUsername(usernameFixture);
        TokenTransfer tokenTransfer = new TokenTransfer(TokenUtil.createToken(userDetails));

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                body("{\"userId\":2,\"vcards\":[\"" + JSONObject.escape(jCardFixture) + "\"],\"isTestRun\":true}").
                when().
                post("/api/v1/users/updateVCards").
                then().
                statusCode(200);
    }

    @Test
    public void createVCardsFromJSONString() throws Exception {
        UserDetails userDetails = userDAO.loadUserByUsername(usernameFixture);
        TokenTransfer tokenTransfer = new TokenTransfer(TokenUtil.createToken(userDetails));

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                body("{\"vcard\":\"" + JSONObject.escape(jCardFixture) + "\",\"format\":\"jcard\",\"isTestRun\":true}").
                when().
                post("/api/v1/users/createVCardsFromString/2").
                then().
                statusCode(200).
                body("numberOfImportedVCards", equalTo(1));
    }
}
