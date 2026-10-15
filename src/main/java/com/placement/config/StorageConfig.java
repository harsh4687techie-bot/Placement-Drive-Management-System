package com.placement.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class StorageConfig {

    private static final Logger logger = LoggerFactory.getLogger(StorageConfig.class);

    @Value("${app.storage.data-dir:./data}")
    private String dataDirConfig;

    @Value("${app.storage.backup-dir:./backups}")
    private String backupDirConfig;

    @Value("${app.storage.report-dir:./reports}")
    private String reportDirConfig;

    private Path dataDirectory;
    private Path backupDirectory;
    private Path reportDirectory;

    @PostConstruct
    public void init() {
        // Allow overriding via environment variable DATA_DIR if present
        String envDataDir = System.getenv("DATA_DIR");
        String effectiveDataDir = (envDataDir != null && !envDataDir.isBlank()) ? envDataDir : dataDirConfig;

        this.dataDirectory = Paths.get(effectiveDataDir).toAbsolutePath().normalize();
        this.backupDirectory = Paths.get(backupDirConfig).toAbsolutePath().normalize();
        this.reportDirectory = Paths.get(reportDirConfig).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.dataDirectory);
            Files.createDirectories(this.backupDirectory);
            Files.createDirectories(this.reportDirectory);
            logger.info("Storage directories initialized: DATA_DIR={}, BACKUP_DIR={}, REPORT_DIR={}",
                    dataDirectory, backupDirectory, reportDirectory);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create required storage directories", e);
        }
    }

    public Path getDataDirectory() {
        return dataDirectory;
    }

    public Path getBackupDirectory() {
        return backupDirectory;
    }

    public Path getReportDirectory() {
        return reportDirectory;
    }

    public Path resolveDataFile(String filename) {
        // Prevent path traversal
        Path resolved = dataDirectory.resolve(filename).normalize();
        if (!resolved.startsWith(dataDirectory)) {
            throw new SecurityException("Access outside data directory is prohibited: " + filename);
        }
        return resolved;
    }
}
