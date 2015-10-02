package com.freshcard.backend.security;

/**
 * Created by willy on 18.08.14.
 */
public class UnauthorizedException extends Exception {
    public UnauthorizedException(String message) {
        super(message);
    }
}
