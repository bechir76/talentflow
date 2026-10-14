package com.talentflow.ai.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PersonalDataRedactorTest {

    private final PersonalDataRedactor redactor = new PersonalDataRedactor();

    @Test
    void removesKnownNameEmailAndPhone() {
        String cv = """
                LENA SCHMIDT
                lena.schmidt@example.org | +49 151 234 5678 | Berlin
                Lena led a team of five engineers. Contact Ms. Schmidt for references.
                """;

        String redacted = redactor.redact(cv, "Lena Schmidt", "lena.schmidt@example.org", "+49 151 234 5678");

        assertThat(redacted).doesNotContainIgnoringCase("lena").doesNotContainIgnoringCase("schmidt")
                .doesNotContain("@").doesNotContain("234 5678")
                .contains("[name]", "[email]", "[phone]", "Berlin", "team of five engineers");
    }

    @Test
    void removesUnknownEmailsPhonesAndProfileLinks() {
        String cv = "Reach me at jo@example.org, (030) 1234 5678 or 0151-2345678. linkedin.com/in/jo-dev https://jo.dev";

        String redacted = redactor.redact(cv, null, null, null);

        assertThat(redacted).isEqualTo("Reach me at [email], [phone] or [phone]. [link] [link]");
    }

    @Test
    void keepsYearRangesAndTechnicalTermsNeededForTheAnalysis() {
        String cv = "Backend Engineer, 2016 - 2021. Java 21, Spring Boot 3, PostgreSQL 16. 8+ years of experience.";

        assertThat(redactor.redact(cv, "Max Mustermann", null, null)).isEqualTo(cv);
    }

    @Test
    void doesNotReplaceNamePartsInsideOtherWords() {
        assertThat(redactor.redact("Annabelle used Ansible.", "Ann Lee", null, null))
                .isEqualTo("Annabelle used Ansible.");
    }
}
