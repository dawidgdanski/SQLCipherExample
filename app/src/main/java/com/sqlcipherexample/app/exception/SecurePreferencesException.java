package com.sqlcipherexample.app.exception;

public class SecurePreferencesException extends RuntimeException {
    public SecurePreferencesException(String detailMessage) {
        super(detailMessage);
    }
}
