package com.freshcard.backend.security.oAuth;

import org.scribe.builder.api.DefaultApi20;
import org.scribe.model.OAuthConfig;

/**
 * Created by willy on 14.09.14.
 */
public class GitHubApi extends DefaultApi20 {
    private static final String ACCESS_TOKEN_URL = "https://github.com/login/oauth/access_token";
    private static final String AUTHORIZATION_URL = "https://github.com/login/oauth/authorize?client_id=%s&scope=%s&redirect_uri=%s";

    public String getAccessTokenEndpoint() {
        return ACCESS_TOKEN_URL;
    }

    public String getAuthorizationUrl(OAuthConfig config) {
        return String.format(
                AUTHORIZATION_URL,
                config.getApiKey(),
                config.getScope(),
                config.getCallback()
        );
    }
}
