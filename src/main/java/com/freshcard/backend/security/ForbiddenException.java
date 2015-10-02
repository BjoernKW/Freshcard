package com.freshcard.backend.security;

/**
 * Created by willy on 18.08.14.
 */
public class ForbiddenException extends Exception {
    public ForbiddenException(String message) {
        super(message);
    }
}
