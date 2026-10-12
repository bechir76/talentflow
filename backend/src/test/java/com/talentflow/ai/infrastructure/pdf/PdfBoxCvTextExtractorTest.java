package com.talentflow.ai.infrastructure.pdf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.UncheckedIOException;

import org.junit.jupiter.api.Test;

import com.talentflow.application.TestFiles;

class PdfBoxCvTextExtractorTest {

    private final PdfBoxCvTextExtractor extractor = new PdfBoxCvTextExtractor();

    @Test
    void extractsTextFromPdf() {
        byte[] pdf = TestFiles.pdfWithText("Senior Java Developer", "Spring Boot, PostgreSQL");

        assertThat(extractor.extract(pdf)).contains("Senior Java Developer", "Spring Boot, PostgreSQL");
    }

    @Test
    void failsForBrokenPdf() {
        assertThatThrownBy(() -> extractor.extract("%PDF-1.7 not really".getBytes()))
                .isInstanceOf(UncheckedIOException.class);
    }
}
