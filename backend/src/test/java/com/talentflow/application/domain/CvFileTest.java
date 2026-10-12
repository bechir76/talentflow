package com.talentflow.application.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.talentflow.application.TestFiles;
import com.talentflow.shared.domain.BusinessRuleException;

class CvFileTest {

    @Test
    void acceptsPdfContent() {
        assertThat(CvFile.of(TestFiles.MINIMAL_PDF).size()).isEqualTo(TestFiles.MINIMAL_PDF.length);
    }

    @Test
    void rejectsNonPdfContentEvenWithPdfName() {
        byte[] png = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A};

        assertThatThrownBy(() -> CvFile.of(png))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("must be a PDF");
    }

    @Test
    void rejectsEmptyFile() {
        assertThatThrownBy(() -> CvFile.of(new byte[0])).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void rejectsFilesLargerThanFiveMegabytes() {
        byte[] tooLarge = Arrays.copyOf(TestFiles.MINIMAL_PDF, CvFile.MAX_SIZE_BYTES + 1);

        assertThatThrownBy(() -> CvFile.of(tooLarge))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("5 MB");
    }
}
