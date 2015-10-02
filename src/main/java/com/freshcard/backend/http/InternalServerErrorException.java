package com.freshcard.backend.http;

/**
 * Created by willy on 18.08.14.
 */
public class InternalServerErrorException extends Exception {
    public InternalServerErrorException(String message) {
        super(message);
    }
}
