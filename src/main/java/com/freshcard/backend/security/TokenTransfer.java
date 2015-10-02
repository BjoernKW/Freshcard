package com.freshcard.backend.security;

/**
 * Created by willy on 16.08.14.
 */
public class TokenTransfer {
    private final String token;

    public TokenTransfer(String token) {
        this.token = token;
    }

    public String getToken() {
        return this.token;
    }
}
