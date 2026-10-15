package com.placement.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.placement.config.StorageConfig;
import com.placement.exception.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Component
public class JsonStorageEngine {

    private static final Logger logger = LoggerFactory.getLogger(JsonStorageEngine.class);

    private final StorageConfig storageConfig;
    private final Gson gson;
    private final ConcurrentHashMap<String, ReentrantReadWriteLock> fileLocks = new ConcurrentHashMap<>();

    public JsonStorageEngine(StorageConfig storageConfig) {
        this.storageConfig = storageConfig;
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .create();
    }

    private ReentrantReadWriteLock getLockFor(String filename) {
        return fileLocks.computeIfAbsent(filename, k -> new ReentrantReadWriteLock(true));
    }

    /**
     * Reads a list of entities from a JSON file.
     * If the file does not exist, it is safely created with empty array '[]'.
     * If the file is corrupt/invalid JSON, throws StorageException without wiping the file.
     */
    public <T> List<T> readList(String filename, Type listType) {
        ReentrantReadWriteLock lock = getLockFor(filename);
        lock.readLock().lock();
        try {
            Path targetPath = storageConfig.resolveDataFile(filename);

            if (!Files.exists(targetPath)) {
                // Initialize missing file under write lock
                lock.readLock().unlock();
                lock.writeLock().lock();
                try {
                    if (!Files.exists(targetPath)) {
                        writeRaw(targetPath, "[]");
                        logger.info("Initialized empty storage file: {}", targetPath);
                    }
                } finally {
                    lock.readLock().lock();
                    lock.writeLock().unlock();
                }
            }

            // Read file content
            String content;
            try (BufferedReader reader = Files.newBufferedReader(targetPath, StandardCharsets.UTF_8)) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append(System.lineSeparator());
                }
                content = sb.toString().trim();
            }

            if (content.isEmpty() || content.equals("[]")) {
                return new ArrayList<>();
            }

            try {
                List<T> list = gson.fromJson(content, listType);
                return list != null ? list : new ArrayList<>();
            } catch (JsonParseException e) {
                String errMsg = "Storage corruption detected in file '" + filename + "'. The file contains invalid JSON. Data will NOT be overwritten to prevent loss.";
                logger.error(errMsg, e);
                throw new StorageException(errMsg, e);
            }

        } catch (IOException e) {
            throw new StorageException("Failed to read storage file: " + filename, e);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Writes a list of entities into a JSON file atomically using a temporary file.
     */
    public <T> void writeList(String filename, List<T> data) {
        ReentrantReadWriteLock lock = getLockFor(filename);
        lock.writeLock().lock();
        try {
            Path targetPath = storageConfig.resolveDataFile(filename);
            String json = gson.toJson(data != null ? data : new ArrayList<>());

            Path tempFile = Files.createTempFile(storageConfig.getDataDirectory(), filename + ".tmp.", ".json");
            try {
                try (BufferedWriter writer = Files.newBufferedWriter(tempFile, StandardCharsets.UTF_8)) {
                    writer.write(json);
                    writer.flush();
                }

                // Perform atomic move if supported, otherwise replace existing
                try {
                    Files.move(tempFile, targetPath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
                } catch (AtomicMoveNotSupportedException e) {
                    Files.move(tempFile, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
            } finally {
                // Ensure temp file is cleaned up if move failed
                if (Files.exists(tempFile)) {
                    try {
                        Files.delete(tempFile);
                    } catch (IOException ignored) {
                    }
                }
            }

        } catch (IOException e) {
            throw new StorageException("Failed to persist data to storage file: " + filename, e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void writeRaw(Path targetPath, String content) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(targetPath, StandardCharsets.UTF_8)) {
            writer.write(content);
            writer.flush();
        }
    }

    public Gson getGson() {
        return gson;
    }
}
