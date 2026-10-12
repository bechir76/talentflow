package com.talentflow.application.domain;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import com.talentflow.shared.domain.BusinessRuleException;

/**
 * An uploaded CV. Only PDF files up to {@link #MAX_SIZE_BYTES} are accepted.
 * The content is checked by its PDF signature, not only by the declared content type.
 */
public final class CvFile {

    public static final int MAX_SIZE_BYTES = 5 * 1024 * 1024;

    private static final byte[] PDF_SIGNATURE = "%PDF-".getBytes(StandardCharsets.US_ASCII);

    private final byte[] content;

    private CvFile(byte[] content) {
        this.content = content;
    }

    public static CvFile of(byte[] content) {
        if (content == null || content.length == 0) {
            throw new BusinessRuleException("Please attach your CV as a PDF file.");
        }
        if (content.length > MAX_SIZE_BYTES) {
            throw new BusinessRuleException("The CV must not be larger than 5 MB.");
        }
        if (!startsWithPdfSignature(content)) {
            throw new BusinessRuleException("The CV must be a PDF file.");
        }
        return new CvFile(content);
    }

    public byte[] content() {
        return content.clone();
    }

    public int size() {
        return content.length;
    }

    private static boolean startsWithPdfSignature(byte[] content) {
        return content.length >= PDF_SIGNATURE.length
                && Arrays.equals(content, 0, PDF_SIGNATURE.length, PDF_SIGNATURE, 0, PDF_SIGNATURE.length);
    }
}
