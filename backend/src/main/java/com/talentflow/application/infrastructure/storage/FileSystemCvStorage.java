package com.talentflow.application.infrastructure.storage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.talentflow.application.application.CvStorage;

/**
 * Stores CVs as files in a local directory (a Docker volume in compose).
 * Keys are random, so file names never contain personal data.
 */
@Component
public class FileSystemCvStorage implements CvStorage {

    private static final Pattern KEY_FORMAT = Pattern.compile("[0-9a-f-]{36}\\.pdf");

    private final Path directory;

    public FileSystemCvStorage(@Value("${talentflow.storage.cv-directory}") Path directory) {
        this.directory = directory.toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.directory);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot create CV directory " + this.directory, e);
        }
    }

    @Override
    public String store(byte[] content) {
        String key = UUID.randomUUID() + ".pdf";
        try {
            Files.write(resolve(key), content);
            return key;
        } catch (IOException e) {
            throw new UncheckedIOException("Could not store CV", e);
        }
    }

    @Override
    public byte[] load(String key) {
        try {
            return Files.readAllBytes(resolve(key));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read CV " + key, e);
        }
    }

    @Override
    public void delete(String key) {
        try {
            Files.deleteIfExists(resolve(key));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not delete CV " + key, e);
        }
    }

    /** Only accepts keys this class generated, so a key can never point outside the directory. */
    private Path resolve(String key) {
        if (key == null || !KEY_FORMAT.matcher(key).matches()) {
            throw new IllegalArgumentException("Invalid CV key");
        }
        return directory.resolve(key);
    }
}
