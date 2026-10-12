package com.talentflow.shared.domain;

/**
 * A requested resource does not exist or is not visible to the caller.
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String resource, Object id) {
        super(resource + " " + id + " was not found.");
    }
}
