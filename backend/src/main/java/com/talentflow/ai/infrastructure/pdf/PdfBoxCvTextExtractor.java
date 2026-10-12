package com.talentflow.ai.infrastructure.pdf;

import java.io.IOException;
import java.io.UncheckedIOException;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import com.talentflow.ai.application.CvTextExtractor;

@Component
public class PdfBoxCvTextExtractor implements CvTextExtractor {

    @Override
    public String extract(byte[] pdf) {
        try (PDDocument document = Loader.loadPDF(pdf)) {
            return new PDFTextStripper().getText(document).strip();
        } catch (IOException e) {
            throw new UncheckedIOException("The CV could not be read as a PDF", e);
        }
    }
}
