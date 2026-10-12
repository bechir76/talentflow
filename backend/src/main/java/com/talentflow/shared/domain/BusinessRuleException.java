package com.talentflow.shared.domain;

/**
 * A request is well-formed but breaks a domain rule (for example an invalid status change).
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
