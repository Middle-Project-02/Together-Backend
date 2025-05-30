package com.together.server.application.auth.exception;

public class MemberNotFoundException extends RuntimeException {

    public MemberNotFoundException(String message) {
        super(message);
    }
}
