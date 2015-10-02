package com.freshcard.backend;

import com.freshcard.backend.http.InternalServerErrorException;
import com.freshcard.backend.model.User;
import com.freshcard.backend.model.dao.UserDAO;
import com.freshcard.backend.security.oAuth.GitHubApi;
import com.freshcard.backend.util.UserManagementHelper;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.LinkedInApi;
import org.scribe.builder.api.TwitterApi;
import org.scribe.builder.api.XingApi;
import org.scribe.model.*;
import org.scribe.oauth.OAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Properties;

@Api(value = "oAuth", description = "oAuth")
@Controller
@RequestMapping("/oAuth")
public class OAuthController {
    private static final Logger logger = Logger.getLogger(OAuthController.class);

    private static final String GITHUB_PROFILE_INFO = "https://api.github.com/user/emails";
    private static final String LINKED_IN_PROFILE_INFO = "https://api.linkedin.com/v1/people/~:(first-name,last-name,email-address)?format=json";
    private static final String TWITTER_PROFILE_INFO = "https://api.twitter.com/1.1/account/verify_credentials.json";
    private static final String XING_PROFILE_INFO = "https://api.xing.com/v1/users/me";

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private UserManagementHelper userManagementHelper;

    @Autowired
    @Qualifier("authenticationProperties")
    private Properties authenticationProperties;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    private String serverAddress;

    private String uiAddress;

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler( {InternalServerErrorException.class } )
    public void handleInternalServerError() {
    }

    @ApiOperation(value = "Authenticate via GitHub")
    @RequestMapping(value = "/authenticate/GitHub", method = RequestMethod.GET)
    public String authenticateViaGitHub(HttpServletRequest request) {
        OAuthService service = new ServiceBuilder()
                .provider(GitHubApi.class)
                .apiKey(authenticationProperties.getProperty("GitHub.apiKey"))
                .apiSecret(authenticationProperties.getProperty("GitHub.secretKey"))
                .callback(getServerAddress() + "/oAuth/callback/GitHub")
                .scope("user")
                .build();
        String state = passwordEncoder.encode(authenticationProperties.getProperty("GitHub.apiKey") + System.currentTimeMillis());
        request.getSession().setAttribute("requestToken", null);

        return "redirect:" + service.getAuthorizationUrl(null) + "&state=" + state;
    }

    @ApiOperation(value = "GitHub callback")
    @RequestMapping(value = "/callback/GitHub", method = RequestMethod.GET)
    public String callbackFromGitHub(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws InternalServerErrorException {
        OAuthService service = new ServiceBuilder()
                .provider(GitHubApi.class)
                .apiKey(authenticationProperties.getProperty("GitHub.apiKey"))
                .apiSecret(authenticationProperties.getProperty("GitHub.secretKey"))
                .callback(getUiAddress() + "/public/oAuth/callback")
                .scope("user")
                .build();

        Verifier verifier = new Verifier(request.getParameter("code"));
        Token requestToken = (Token)request.getSession().getAttribute("requestToken");
        Token accessToken = service.getAccessToken(requestToken, verifier);

        OAuthRequest oAuthRequest = new OAuthRequest(Verb.GET, GITHUB_PROFILE_INFO);
        service.signRequest(accessToken, oAuthRequest);
        Response serviceResponse = oAuthRequest.send();

        String eMailAddress = "";
        try {
            JSONArray eMailAddresses = (JSONArray)new JSONParser().parse(serviceResponse.getBody());
            eMailAddress = ((JSONObject)eMailAddresses.get(0)).get("email").toString();
        } catch (ParseException e) {
            logger.debug(e.getMessage());
        }

        User user = userDAO.findByUsername(eMailAddress);
        if (user == null) {
            user = new User();
            user.setUsername(eMailAddress);
            user.setGitHubAccessToken(accessToken.getToken());
            user.setMostRecentlyUsedOAuthService(User.GITHUB_SERVICE_NAME);
            user.setPassword(accessToken.getToken());

            createUser(user);
        } else {
            user.setGitHubAccessToken(accessToken.getToken());
            user.setMostRecentlyUsedOAuthService(User.GITHUB_SERVICE_NAME);
            user.setPassword(user.getGitHubAccessToken());
            user.setUseOAuth(true);
            user.setConfirmed(true);

            userDAO.update(user.getId(), user);
        }

        Cookie serviceCookie = new Cookie("oAuthService", User.GITHUB_SERVICE_NAME);
        serviceCookie.setPath("/");
        serviceCookie.setMaxAge(-1);
        response.addCookie(serviceCookie);

        Cookie eMailAddressCookie = new Cookie("eMailAddress", eMailAddress);
        eMailAddressCookie.setPath("/");
        eMailAddressCookie.setMaxAge(-1);
        response.addCookie(eMailAddressCookie);

        Cookie accessTokenCookie = new Cookie("accessToken", accessToken.getToken());
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(-1);
        response.addCookie(accessTokenCookie);

        return "redirect:" + getUiAddress() + "/public/oAuth/callback";
    }

    @ApiOperation(value = "Authenticate via LinkedIn")
    @RequestMapping(value = "/authenticate/LinkedIn", method = RequestMethod.GET)
    public String authenticateViaLinkedIn(HttpServletRequest request) {
        OAuthService service = new ServiceBuilder()
                .provider(LinkedInApi.class)
                .apiKey(authenticationProperties.getProperty("LinkedIn.apiKey"))
                .apiSecret(authenticationProperties.getProperty("LinkedIn.secretKey"))
                .callback(getServerAddress() + "/oAuth/callback/LinkedIn")
                .scope("r_emailaddress r_fullprofile")
                .build();

        Token requestToken = service.getRequestToken();
        request.getSession().setAttribute("requestToken", requestToken);

        return "redirect:" + service.getAuthorizationUrl(requestToken);
    }

    @ApiOperation(value = "LinkedIn callback")
    @RequestMapping(value = "/callback/LinkedIn", method = RequestMethod.GET)
    public String callbackFromLinkedIn(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws InternalServerErrorException {
        OAuthService service = new ServiceBuilder()
                .provider(LinkedInApi.class)
                .apiKey(authenticationProperties.getProperty("LinkedIn.apiKey"))
                .apiSecret(authenticationProperties.getProperty("LinkedIn.secretKey"))
                .scope("r_emailaddress r_fullprofile")
                .build();

        Verifier verifier = new Verifier(request.getParameter("oauth_verifier"));
        Token requestToken = (Token)request.getSession().getAttribute("requestToken");
        Token accessToken = service.getAccessToken(requestToken, verifier);

        OAuthRequest oAuthRequest = new OAuthRequest(Verb.GET, LINKED_IN_PROFILE_INFO);
        service.signRequest(accessToken, oAuthRequest);
        Response serviceResponse = oAuthRequest.send();

        String eMailAddress = "";
        try {
            JSONObject profile = (JSONObject)new JSONParser().parse(serviceResponse.getBody());
            eMailAddress = profile.get("emailAddress").toString();
        } catch (ParseException e) {
            logger.debug(e.getMessage());
        }

        User user = userDAO.findByUsername(eMailAddress);
        if (user == null) {
            user = new User();
            user.setUsername(eMailAddress);
            user.setLinkedInAccessToken(accessToken.getToken());
            user.setMostRecentlyUsedOAuthService(User.LINKEDIN_SERVICE_NAME);
            user.setPassword(accessToken.getToken());

            createUser(user);
        } else {
            user.setLinkedInAccessToken(accessToken.getToken());
            user.setMostRecentlyUsedOAuthService(User.LINKEDIN_SERVICE_NAME);
            user.setPassword(user.getLinkedInAccessToken());
            user.setUseOAuth(true);
            user.setConfirmed(true);

            userDAO.update(user.getId(), user);
        }

        Cookie serviceCookie = new Cookie("oAuthService", User.LINKEDIN_SERVICE_NAME);
        serviceCookie.setPath("/");
        serviceCookie.setMaxAge(-1);
        response.addCookie(serviceCookie);

        Cookie eMailAddressCookie = new Cookie("eMailAddress", eMailAddress);
        eMailAddressCookie.setPath("/");
        eMailAddressCookie.setMaxAge(-1);
        response.addCookie(eMailAddressCookie);

        Cookie accessTokenCookie = new Cookie("accessToken", accessToken.getToken());
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(-1);
        response.addCookie(accessTokenCookie);

        return "redirect:" + getUiAddress() + "/public/oAuth/callback";
    }

    @ApiOperation(value = "Authenticate via Twitter")
    @RequestMapping(value = "/authenticate/Twitter", method = RequestMethod.GET)
    public String authenticateViaTwitter(HttpServletRequest request) {
        OAuthService service = new ServiceBuilder()
                .provider(TwitterApi.Authenticate.class)
                .apiKey(authenticationProperties.getProperty("Twitter.apiKey"))
                .apiSecret(authenticationProperties.getProperty("Twitter.secretKey"))
                .callback(getServerAddress() + "/oAuth/callback/Twitter")
                .build();

        Token requestToken = service.getRequestToken();
        request.getSession().setAttribute("requestToken", requestToken);

        return "redirect:" + service.getAuthorizationUrl(requestToken);
    }

    @ApiOperation(value = "Twitter callback")
    @RequestMapping(value = "/callback/Twitter", method = RequestMethod.GET)
    public String callbackFromTwitter(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws InternalServerErrorException {
        OAuthService service = new ServiceBuilder()
                .provider(TwitterApi.Authenticate.class)
                .apiKey(authenticationProperties.getProperty("Twitter.apiKey"))
                .apiSecret(authenticationProperties.getProperty("Twitter.secretKey"))
                .build();

        Verifier verifier = new Verifier(request.getParameter("oauth_verifier"));
        Token requestToken = (Token)request.getSession().getAttribute("requestToken");
        Token accessToken = service.getAccessToken(requestToken, verifier);

        OAuthRequest oAuthRequest = new OAuthRequest(Verb.GET, TWITTER_PROFILE_INFO);
        service.signRequest(accessToken, oAuthRequest);
        Response serviceResponse = oAuthRequest.send();

        String screenName = "";
        try {
            JSONObject profile = (JSONObject)new JSONParser().parse(serviceResponse.getBody());
            screenName = profile.get("screen_name").toString();
        } catch (ParseException e) {
            logger.debug(e.getMessage());
        }

        User user = userDAO.findByUsername(screenName);
        if (user == null) {
            user = new User();
            user.setUsername(screenName);
            user.setTwitterAccessToken(accessToken.getToken());
            user.setMostRecentlyUsedOAuthService(User.TWITTER_SERVICE_NAME);
            user.setPassword(accessToken.getToken());

            createUser(user);
        } else {
            user.setTwitterAccessToken(accessToken.getToken());
            user.setMostRecentlyUsedOAuthService(User.TWITTER_SERVICE_NAME);
            user.setPassword(user.getTwitterAccessToken());
            user.setUseOAuth(true);
            user.setConfirmed(true);

            userDAO.update(user.getId(), user);
        }

        Cookie serviceCookie = new Cookie("oAuthService", User.TWITTER_SERVICE_NAME);
        serviceCookie.setPath("/");
        serviceCookie.setMaxAge(-1);
        response.addCookie(serviceCookie);

        Cookie eMailAddressCookie = new Cookie("eMailAddress", screenName);
        eMailAddressCookie.setPath("/");
        eMailAddressCookie.setMaxAge(-1);
        response.addCookie(eMailAddressCookie);

        Cookie accessTokenCookie = new Cookie("accessToken", accessToken.getToken());
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(-1);
        response.addCookie(accessTokenCookie);

        return "redirect:" + getUiAddress() + "/public/oAuth/callback";
    }

    @ApiOperation(value = "Authenticate via XING")
    @RequestMapping(value = "/authenticate/XING", method = RequestMethod.GET)
    public String authenticateViaXing(HttpServletRequest request) {
        OAuthService service = new ServiceBuilder()
                .provider(XingApi.class)
                .apiKey(authenticationProperties.getProperty("XING.apiKey"))
                .apiSecret(authenticationProperties.getProperty("XING.secretKey"))
                .callback(getServerAddress() + "/oAuth/callback/XING")
                .build();

        Token requestToken = service.getRequestToken();
        request.getSession().setAttribute("requestToken", requestToken);

        return "redirect:" + service.getAuthorizationUrl(requestToken);
    }

    @ApiOperation(value = "XING callback")
    @RequestMapping(value = "/callback/XING", method = RequestMethod.GET)
    public String callbackFromXing(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws InternalServerErrorException {
        OAuthService service = new ServiceBuilder()
                .provider(XingApi.class)
                .apiKey(authenticationProperties.getProperty("XING.apiKey"))
                .apiSecret(authenticationProperties.getProperty("XING.secretKey"))
                .build();

        Verifier verifier = new Verifier(request.getParameter("oauth_verifier"));
        Token requestToken = (Token)request.getSession().getAttribute("requestToken");
        Token accessToken = service.getAccessToken(requestToken, verifier);

        OAuthRequest oAuthRequest = new OAuthRequest(Verb.GET, XING_PROFILE_INFO);
        service.signRequest(accessToken, oAuthRequest);
        Response serviceResponse = oAuthRequest.send();

        String eMailAddress = "";
        try {
            JSONObject profile = (JSONObject)new JSONParser().parse(serviceResponse.getBody());
            eMailAddress = ((JSONObject)((JSONArray)profile.get("users")).get(0)).get("active_email").toString();
        } catch (ParseException e) {
            logger.debug(e.getMessage());
        }

        User user = userDAO.findByUsername(eMailAddress);
        if (user == null) {
            user = new User();
            user.setUsername(eMailAddress);
            user.setXingAccessToken(accessToken.getToken());
            user.setMostRecentlyUsedOAuthService(User.XING_SERVICE_NAME);
            user.setPassword(accessToken.getToken());

            createUser(user);
        } else {
            user.setXingAccessToken(accessToken.getToken());
            user.setMostRecentlyUsedOAuthService(User.XING_SERVICE_NAME);
            user.setPassword(user.getXingAccessToken());
            user.setUseOAuth(true);
            user.setConfirmed(true);

            userDAO.update(user.getId(), user);
        }

        Cookie serviceCookie = new Cookie("oAuthService", User.XING_SERVICE_NAME);
        serviceCookie.setPath("/");
        serviceCookie.setMaxAge(-1);
        response.addCookie(serviceCookie);

        Cookie eMailAddressCookie = new Cookie("eMailAddress", eMailAddress);
        eMailAddressCookie.setPath("/");
        eMailAddressCookie.setMaxAge(-1);
        response.addCookie(eMailAddressCookie);

        Cookie accessTokenCookie = new Cookie("accessToken", accessToken.getToken());
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(-1);
        response.addCookie(accessTokenCookie);

        return "redirect:" + getUiAddress() + "/public/oAuth/callback";
    }
    
    private String getServerAddress() {
        if (serverAddress == null) {
            serverAddress = System.getProperty("serverAddress") != null ? System.getProperty("serverAddress") : authenticationProperties.getProperty("serverAddress");
        }
        
        return serverAddress;
    }

    private String getUiAddress() {
        if (uiAddress == null) {
            uiAddress = System.getProperty("uiAddress") != null ? System.getProperty("uiAddress") : authenticationProperties.getProperty("uiAddress");
        }

        return uiAddress;
    }

    private void createUser(User user) throws InternalServerErrorException {
        user.setUseOAuth(true);
        user.setConfirmed(true);

        userManagementHelper.createUser(user);
    }
}