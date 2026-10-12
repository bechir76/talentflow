package com.talentflow.shared.application;

/**
 * Port that gives use cases access to the authenticated user without depending on Spring Security.
 */
public interface CurrentUserProvider {

    /**
     * @return the authenticated user
     * @throws IllegalStateException if the request is not authenticated
     */
    CurrentUser currentUser();
}
