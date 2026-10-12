package com.talentflow.application.application;

import com.talentflow.application.domain.CvFile;

/**
 * Data a candidate submits with an application.
 *
 * @param consentGiven whether the candidate ticked the data processing consent box
 */
public record ApplyCommand(String phone, String location, String coverLetter, boolean consentGiven, CvFile cv) {
}
