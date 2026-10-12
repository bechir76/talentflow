package com.talentflow.demo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

/**
 * Renders a simple one-page CV for demo candidates, so the PDF viewer and the AI screening have real input.
 */
final class DemoCvRenderer {

    private static final float MARGIN = 56;
    private static final float WIDTH = PDRectangle.A4.getWidth() - 2 * MARGIN;
    private static final int WRAP_AT = 95;

    private DemoCvRenderer() {
    }

    static byte[] render(DemoData.Person person) {
        PDType1Font bold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        PDType1Font regular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                float y = PDRectangle.A4.getHeight() - MARGIN;
                y = line(content, bold, 22, person.name().toUpperCase(), y, 28);
                y = line(content, regular, 11, person.headline(), y, 18);
                y = line(content, regular, 9, person.email() + "  |  " + person.phone() + "  |  " + person.city(), y, 24);
                content.setLineWidth(1.5f);
                content.moveTo(MARGIN, y + 8);
                content.lineTo(MARGIN + WIDTH, y + 8);
                content.stroke();
                y = line(content, bold, 11, "PROFILE", y - 8, 16);
                for (String text : wrap(person.summary())) {
                    y = line(content, regular, 10, text, y, 14);
                }
                y = line(content, bold, 11, "SKILLS", y - 10, 16);
                y = line(content, regular, 10, String.join(", ", person.skills()), y, 14);
                y = line(content, bold, 11, "EXPERIENCE", y - 10, 16);
                for (String job : person.experience()) {
                    for (String text : wrap(job)) {
                        y = line(content, regular, 10, text, y, 14);
                    }
                    y -= 6;
                }
                y = line(content, bold, 11, "LANGUAGES", y - 4, 16);
                line(content, regular, 10, "German, English", y, 14);
            }
            document.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static float line(PDPageContentStream content, PDType1Font font, float size, String text, float y,
            float advance) throws IOException {
        content.beginText();
        content.setFont(font, size);
        content.newLineAtOffset(MARGIN, y);
        content.showText(text);
        content.endText();
        return y - advance;
    }

    private static List<String> wrap(String text) {
        List<String> lines = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String word : text.split(" ")) {
            if (current.length() + word.length() + 1 > WRAP_AT) {
                lines.add(current.toString());
                current.setLength(0);
            }
            if (!current.isEmpty()) {
                current.append(' ');
            }
            current.append(word);
        }
        if (!current.isEmpty()) {
            lines.add(current.toString());
        }
        return lines;
    }
}
