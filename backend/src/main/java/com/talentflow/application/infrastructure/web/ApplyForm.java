package com.talentflow.application.infrastructure.web;

import jakarta.validation.constraints.Size;

import com.talentflow.application.application.ApplyCommand;
import com.talentflow.application.domain.CvFile;
import com.talentflow.application.domain.JobApplication;

/**
 * Text fields of the multipart application form. The CV is sent as the {@code cv} part.
 *
 * @param consent must be {@code true}; checked by the use case so the error message is explicit
 */
public record ApplyForm(
        @Size(max = 40) String phone,
        @Size(max = 150) String location,
        @Size(max = JobApplication.MAX_COVER_LETTER_LENGTH) String coverLetter,
        boolean consent) {

    ApplyCommand toCommand(CvFile cv) {
        return new ApplyCommand(phone, location, coverLetter, consent, cv);
    }
}
