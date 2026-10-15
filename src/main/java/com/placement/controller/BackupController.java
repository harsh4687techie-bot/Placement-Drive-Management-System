package com.placement.controller;

import com.placement.dto.ApiResponse;
import com.placement.service.BackupService;
import com.placement.service.DemoDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class BackupController {

    private final BackupService backupService;
    private final DemoDataService demoDataService;

    public BackupController(BackupService backupService, DemoDataService demoDataService) {
        this.backupService = backupService;
        this.demoDataService = demoDataService;
    }

    @PostMapping("/backup/create")
    public ResponseEntity<ApiResponse<Map<String, String>>> createBackup() {
        String backupName = backupService.createBackup();
        return ResponseEntity.ok(ApiResponse.success("Backup successfully created", Map.of("backupName", backupName)));
    }

    @GetMapping("/backup/list")
    public ResponseEntity<ApiResponse<List<String>>> listBackups() {
        List<String> list = backupService.listBackups();
        return ResponseEntity.ok(ApiResponse.success("Available backups retrieved", list));
    }

    @PostMapping("/backup/restore/{backupName}")
    public ResponseEntity<ApiResponse<Void>> restoreBackup(@PathVariable String backupName) {
        backupService.restoreBackup(backupName);
        return ResponseEntity.ok(ApiResponse.success("Database restored successfully from " + backupName));
    }

    @PostMapping("/demo/load")
    public ResponseEntity<ApiResponse<Void>> loadDemoData(@RequestParam(defaultValue = "false") boolean forceReset) {
        demoDataService.loadDemoData(forceReset);
        return ResponseEntity.ok(ApiResponse.success("Demo dataset loaded successfully"));
    }

    @DeleteMapping("/data/reset")
    public ResponseEntity<ApiResponse<Void>> resetAllData() {
        demoDataService.resetAllData();
        return ResponseEntity.ok(ApiResponse.success("All placement data cleared. Database is now empty."));
    }
}
