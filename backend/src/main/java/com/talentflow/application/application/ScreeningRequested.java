package com.talentflow.application.application;

import java.util.UUID;

/**
 * Published when an application needs (another) AI screening. Handled after the transaction commits.
 */
public record ScreeningRequested(UUID applicationId) {
}
