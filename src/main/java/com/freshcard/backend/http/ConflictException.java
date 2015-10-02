package com.freshcard.backend.http;

/**
 * Created by willy on 18.08.14.
 */
public class ConflictException extends Exception {
    public ConflictException(String message) {
        super(message);
    }
}
