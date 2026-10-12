package com.talentflow.application;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

/**
 * Sample files shared by the application tests.
 */
public final class TestFiles {

    /** The smallest content accepted as a PDF: just the signature and a comment line. */
    public static final byte[] MINIMAL_PDF = "%PDF-1.7\n%test\n".getBytes(StandardCharsets.US_ASCII);

    /** A real one-page PDF containing the given lines of text. */
    public static byte[] pdfWithText(String... lines) {
        try (PDDocument document = new PDDocument(); var out = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                content.beginText();
                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 11);
                content.setLeading(14);
                content.newLineAtOffset(50, 740);
                for (String line : lines) {
                    content.showText(line);
                    content.newLine();
                }
                content.endText();
            }
            document.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private TestFiles() {
    }
}
