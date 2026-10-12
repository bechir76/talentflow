package com.talentflow.ai.application;

/**
 * Port that turns a CV file into plain text.
 */
public interface CvTextExtractor {

    /**
     * @return the text of the document; empty if it contains none (for example a scanned image)
     */
    String extract(byte[] pdf);
}
