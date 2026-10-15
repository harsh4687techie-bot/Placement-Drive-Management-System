package com.placement.controller;

import com.placement.config.StorageConfig;
import com.placement.dto.ApiResponse;
import com.placement.repository.PlacementDriveRepository;
import com.placement.repository.StudentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final StorageConfig storageConfig;
    private final StudentRepository studentRepository;
    private final PlacementDriveRepository driveRepository;

    public HealthController(StorageConfig storageConfig,
                            StudentRepository studentRepository,
                            PlacementDriveRepository driveRepository) {
        this.storageConfig = storageConfig;
        this.studentRepository = studentRepository;
        this.driveRepository = driveRepository;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "Placement Drive Management System");
        health.put("dataDirectory", storageConfig.getDataDirectory().toString());
        health.put("totalStudents", studentRepository.findAll().size());
        health.put("totalDrives", driveRepository.findAll().size());
        health.put("systemTime", java.time.Instant.now().toString());

        return ResponseEntity.ok(ApiResponse.success("System is healthy and operational", health));
    }
}
