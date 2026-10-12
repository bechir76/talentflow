package com.talentflow.shared.domain;

/**
 * The caller is authenticated but may not access this particular resource (row-level rule).
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
