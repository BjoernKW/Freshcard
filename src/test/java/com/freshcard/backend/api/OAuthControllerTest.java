package com.freshcard.backend.api;

import com.freshcard.backend.OAuthController;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.jayway.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static com.jayway.restassured.module.mockmvc.RestAssuredMockMvc.standaloneSetup;

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
public class OAuthControllerTest {
    @Autowired
    private OAuthController oAuthController;

    @Before
    public void setup() {
        standaloneSetup(oAuthController);
    }

    @Test
    public void authenticateViaGitHub() throws Exception {
        given().
                contentType("application/json").
                when().
                get("/oAuth/authenticate/GitHub").
                then().
                statusCode(302);
    }

    @Test
    public void authenticateViaLinkedIn() throws Exception {
        given().
                contentType("application/json").
                when().
                get("/oAuth/authenticate/LinkedIn").
                then().
                statusCode(302);
    }

    @Test
    public void authenticateViaTwitter() throws Exception {
        given().
                contentType("application/json").
                when().
                get("/oAuth/authenticate/Twitter").
                then().
                statusCode(302);
    }

    @Test
    public void authenticateViaXING() throws Exception {
        given().
                contentType("application/json").
                when().
                get("/oAuth/authenticate/XING").
                then().
                statusCode(302);
    }
}
