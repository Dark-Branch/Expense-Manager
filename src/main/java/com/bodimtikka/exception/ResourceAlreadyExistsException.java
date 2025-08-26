package com.bodimtikka.exception;

public class ResourceAlreadyExistsException extends RuntimeException {
    private final String resourceName;

    public ResourceAlreadyExistsException(String resourceName) {
        super(resourceName + " already exists");
        this.resourceName = resourceName;
    }

    public String getResourceName() {
        return resourceName;
    }
}
