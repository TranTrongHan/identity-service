package com.luketran.identity.domain.exceptions;

public class SessionInvalidException extends RuntimeException {

    public SessionInvalidException() {
        super("Session is invalid or has expired");
    }

    public SessionInvalidException(String message) {
        super(message);
    }
}
