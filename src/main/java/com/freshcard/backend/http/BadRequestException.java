package com.freshcard.backend.http;

/**
 * Created by willy on 18.08.14.
 */
public class BadRequestException extends Exception {
    public BadRequestException(String message) {
        super(message);
    }
}
