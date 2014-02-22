package com.sqlcipherexample.app.exception;

public class EncryptionException extends RuntimeException {
    public EncryptionException(String detailMessage) {
        super(detailMessage);
    }
}
