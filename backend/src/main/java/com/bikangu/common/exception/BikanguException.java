package com.bikangu.common.exception;

public class BikanguException extends RuntimeException {

    private final String errorCode;
    private final int httpStatus;

    public BikanguException(String message, String errorCode, int httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public BikanguException(String message, String errorCode, int httpStatus, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}