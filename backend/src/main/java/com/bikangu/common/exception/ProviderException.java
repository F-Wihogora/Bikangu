package com.bikangu.common.exception;

public class ProviderException extends BikanguException {

    private final String providerCode;
    private final String providerMessage;

    public ProviderException(String message, String providerCode, String providerMessage) {
        super(message, "PROVIDER_ERROR", 502);
        this.providerCode = providerCode;
        this.providerMessage = providerMessage;
    }

    public String getProviderCode() {
        return providerCode;
    }

    public String getProviderMessage() {
        return providerMessage;
    }
}