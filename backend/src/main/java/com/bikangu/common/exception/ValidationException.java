package com.bikangu.common.exception;

public class ValidationException extends BikanguException {

    public ValidationException(String message) {
        super(message, "VALIDATION_ERROR", 400);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, "VALIDATION_ERROR", 400, cause);
    }
}