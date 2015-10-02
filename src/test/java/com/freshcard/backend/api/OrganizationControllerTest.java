package com.freshcard.backend.api;

import com.freshcard.backend.model.dao.UserDAO;
import com.freshcard.backend.security.TokenTransfer;
import com.freshcard.backend.security.TokenUtil;
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
public class OrganizationControllerTest {
    private String[] hashCodeFixtures = { "65ba737bda318d7423bc1a7223bb1c431bc2d9c5e52ac1134a3fc355e05ae2e79fd7c03d8e6ebb86", "85b51dbcd144c5757178160f262ccb68ed319c8b36a6d5401732427fbe459efeb694a009422f5582" };
    private String usernameFixture = "admin@freshcard.co";

    @Autowired
    private OrganizationController organizationController;

    @Autowired
    private UserDAO userDAO;

    @Before
    public void setup() {
        standaloneSetup(organizationController);
    }

    @Test
    public void find() throws Exception {
        UserDetails userDetails = userDAO.loadUserByUsername(usernameFixture);
        TokenTransfer tokenTransfer = new TokenTransfer(TokenUtil.createToken(userDetails));

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                when().
                get("/api/v1/organizations/2").
                then().
                statusCode(200).
                body("id", equalTo(2)).
                body("name", equalTo("Freshcard")).
                body("hashCode", equalTo(hashCodeFixtures[0]));
    }
}
