package com.marketplace.api.shared.exception;

public class ResourceNotFoundException extends DomainException {

    public ResourceNotFoundException(String resourceName, Object id) {
        super(String.format("%s not found with identifier: %s", resourceName, id));
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
