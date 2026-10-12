package com.talentflow.application.application;

/**
 * Port for storing CV files outside the database.
 */
public interface CvStorage {

    /**
     * @return an opaque key that identifies the stored file
     */
    String store(byte[] content);

    byte[] load(String key);

    /** Deletes the file; does nothing if it does not exist. */
    void delete(String key);
}
