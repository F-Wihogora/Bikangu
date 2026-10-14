package com.bikangu.common.exception;

public class UnauthorizedException extends BikanguException {

    public UnauthorizedException(String message) {
        super(message, "UNAUTHORIZED", 401);
    }

    public UnauthorizedException() {
        this("Authentication required");
    }
}