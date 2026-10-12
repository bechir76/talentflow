package com.talentflow.application;

import java.nio.charset.StandardCharsets;

/**
 * Sample files shared by the application tests.
 */
public final class TestFiles {

    /** The smallest content accepted as a PDF: just the signature and a comment line. */
    public static final byte[] MINIMAL_PDF = "%PDF-1.7\n%test\n".getBytes(StandardCharsets.US_ASCII);

    private TestFiles() {
    }
}
