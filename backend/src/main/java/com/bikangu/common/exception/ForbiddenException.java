package com.bikangu.common.exception;

public class ForbiddenException extends BikanguException {

    public ForbiddenException(String message) {
        super(message, "FORBIDDEN", 403);
    }

    public ForbiddenException() {
        this("Access denied");
    }
}