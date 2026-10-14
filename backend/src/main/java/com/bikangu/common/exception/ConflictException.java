package com.bikangu.common.exception;

public class ConflictException extends BikanguException {

    public ConflictException(String message) {
        super(message, "CONFLICT", 409);
    }
}