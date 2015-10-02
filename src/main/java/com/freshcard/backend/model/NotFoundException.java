package com.freshcard.backend.model;

/**
 * Created by willy on 18.08.14.
 */
public class NotFoundException extends Exception {
    public NotFoundException(String message) {
        super(message);
    }
}
