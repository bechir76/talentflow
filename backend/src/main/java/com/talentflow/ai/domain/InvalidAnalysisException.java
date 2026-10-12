package com.talentflow.ai.domain;

/**
 * The analyzer's output did not match the expected contract (for example malformed JSON or a score of 140).
 */
public class InvalidAnalysisException extends RuntimeException {

    public InvalidAnalysisException(String message) {
        super(message);
    }

    public InvalidAnalysisException(String message, Throwable cause) {
        super(message, cause);
    }
}
