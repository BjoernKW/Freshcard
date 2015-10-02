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
public class ConfigurationControllerTest {
    private String usernameFixture = "admin@freshcard.co";

    @Autowired
    private ConfigurationController configurationController;

    @Autowired
    private UserDAO userDAO;

    @Before
    public void setup() {
        standaloneSetup(configurationController);
    }

    @Test
    public void getAlgoliaConfiguration() throws Exception {
        UserDetails userDetails = userDAO.loadUserByUsername(usernameFixture);
        TokenTransfer tokenTransfer = new TokenTransfer(TokenUtil.createToken(userDetails));

        given().
                header("X-Auth-Token", tokenTransfer.getToken()).
                contentType("application/json").
                when().
                get("/api/v1/configuration/getAlgoliaConfiguration").
                then().
                statusCode(200);
    }
}
