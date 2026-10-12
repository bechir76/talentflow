package com.talentflow.shared.domain;

/**
 * The request conflicts with existing data (for example a duplicate application).
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
