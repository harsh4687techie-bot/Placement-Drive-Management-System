package com.placement.service;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.placement.config.StorageConfig;
import com.placement.exception.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class BackupService {

    private static final Logger logger = LoggerFactory.getLogger(BackupService.class);
    private static final String[] DATA_FILES = {
            "students.json",
            "drives.json",
            "registrations.json",
            "status_history.json"
    };

    private final StorageConfig storageConfig;
    private final Gson gson = new Gson();

    public BackupService(StorageConfig storageConfig) {
        this.storageConfig = storageConfig;
    }

    public synchronized String createBackup() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String backupFolderName = "backup_" + timestamp;
        Path backupDir = storageConfig.getBackupDirectory().resolve(backupFolderName).normalize();

        try {
            Files.createDirectories(backupDir);

            for (String file : DATA_FILES) {
                Path source = storageConfig.getDataDirectory().resolve(file);
                Path target = backupDir.resolve(file);
                if (Files.exists(source)) {
                    Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                } else {
                    Files.writeString(target, "[]");
                }
            }

            logger.info("Backup successfully created at: {}", backupDir);
            return backupFolderName;
        } catch (IOException e) {
            throw new StorageException("Failed to create backup: " + e.getMessage(), e);
        }
    }

    public List<String> listBackups() {
        List<String> backups = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(storageConfig.getBackupDirectory())) {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    backups.add(entry.getFileName().toString());
                }
            }
        } catch (IOException e) {
            logger.warn("Could not list backups directory", e);
        }
        backups.sort(Collections.reverseOrder());
        return backups;
    }

    public synchronized void restoreBackup(String backupName) {
        // Prevent path traversal
        if (backupName == null || backupName.contains("..") || backupName.contains("/") || backupName.contains("\\")) {
            throw new SecurityException("Invalid backup name provided: " + backupName);
        }

        Path backupDir = storageConfig.getBackupDirectory().resolve(backupName).normalize();
        if (!backupDir.startsWith(storageConfig.getBackupDirectory()) || !Files.exists(backupDir) || !Files.isDirectory(backupDir)) {
            throw new IllegalArgumentException("Backup not found or invalid: " + backupName);
        }

        // Validate all JSON files before overwriting active database
        for (String file : DATA_FILES) {
            Path backupFile = backupDir.resolve(file);
            if (Files.exists(backupFile)) {
                try {
                    String content = Files.readString(backupFile);
                    gson.fromJson(content, JsonElement.class);
                } catch (Exception e) {
                    throw new StorageException("Backup verification failed for file " + file + ": invalid JSON syntax", e);
                }
            }
        }

        // Copy files over to data directory
        try {
            for (String file : DATA_FILES) {
                Path backupFile = backupDir.resolve(file);
                Path targetFile = storageConfig.getDataDirectory().resolve(file);
                if (Files.exists(backupFile)) {
                    Files.copy(backupFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
                }
            }
            logger.info("Backup {} successfully restored to active database", backupName);
        } catch (IOException e) {
            throw new StorageException("Failed to restore backup: " + e.getMessage(), e);
        }
    }
}
