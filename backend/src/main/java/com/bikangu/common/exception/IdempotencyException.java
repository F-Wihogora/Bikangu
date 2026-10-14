package com.bikangu.common.exception;

public class IdempotencyException extends BikanguException {

    public IdempotencyException(String message) {
        super(message, "IDEMPOTENCY_VIOLATION", 409);
    }

    public IdempotencyException() {
        this("Duplicate request detected. Operation already processed.");
    }
}