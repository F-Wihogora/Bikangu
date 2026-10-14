package com.bikangu.common.exception;

public class ResourceNotFoundException extends BikanguException {

    public ResourceNotFoundException(String resource, String identifier) {
        super(String.format("%s not found with identifier: %s", resource, identifier),
              "RESOURCE_NOT_FOUND", 404);
    }

    public ResourceNotFoundException(String resource, Object identifier) {
        this(resource, identifier.toString());
    }
}