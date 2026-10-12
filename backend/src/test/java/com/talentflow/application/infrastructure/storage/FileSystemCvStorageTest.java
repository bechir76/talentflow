package com.talentflow.application.infrastructure.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileSystemCvStorageTest {

    @TempDir
    private Path directory;

    @Test
    void storesLoadsAndDeletesFiles() {
        FileSystemCvStorage storage = new FileSystemCvStorage(directory);
        byte[] content = {1, 2, 3};

        String key = storage.store(content);

        assertThat(key).matches("[0-9a-f-]{36}\\.pdf");
        assertThat(storage.load(key)).containsExactly(1, 2, 3);
        storage.delete(key);
        assertThat(Files.exists(directory.resolve(key))).isFalse();
    }

    @Test
    void ignoresDeletingMissingFile() {
        FileSystemCvStorage storage = new FileSystemCvStorage(directory);

        storage.delete("00000000-0000-0000-0000-000000000000.pdf");
    }

    @Test
    void rejectsKeysThatCouldEscapeTheDirectory() {
        FileSystemCvStorage storage = new FileSystemCvStorage(directory);

        assertThatThrownBy(() -> storage.load("../secrets.pdf")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> storage.delete("/etc/passwd")).isInstanceOf(IllegalArgumentException.class);
    }
}
