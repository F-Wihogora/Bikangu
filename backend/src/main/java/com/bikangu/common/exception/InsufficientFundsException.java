package com.bikangu.common.exception;

public class InsufficientFundsException extends BikanguException {

    public InsufficientFundsException(String message) {
        super(message, "INSUFFICIENT_FUNDS", 400);
    }

    public InsufficientFundsException() {
        this("Insufficient funds to complete the transaction");
    }
}