package com.placement.controller;

import com.placement.dto.ApiResponse;
import com.placement.dto.PlacementDriveDto;
import com.placement.model.CandidateEligibility;
import com.placement.model.DriveStatus;
import com.placement.model.PlacementDrive;
import com.placement.service.DriveService;
import com.placement.service.EligibilityService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/drives")
public class DriveController {

    private final DriveService driveService;
    private final EligibilityService eligibilityService;

    public DriveController(DriveService driveService, EligibilityService eligibilityService) {
        this.driveService = driveService;
        this.eligibilityService = eligibilityService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PlacementDrive>>> getAllDrives(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) DriveStatus status,
            @RequestParam(required = false) Double minPackage,
            @RequestParam(defaultValue = "company") String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder) {

        List<PlacementDrive> drives = driveService.getAllDrives(search, status, minPackage, sortBy, sortOrder);
        return ResponseEntity.ok(ApiResponse.success("Placement drives retrieved successfully", drives));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PlacementDrive>> getDriveById(@PathVariable String id) {
        PlacementDrive drive = driveService.getDriveById(id);
        return ResponseEntity.ok(ApiResponse.success("Placement drive retrieved", drive));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PlacementDrive>> createDrive(@Valid @RequestBody PlacementDriveDto dto) {
        PlacementDrive created = driveService.createDrive(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Placement drive created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PlacementDrive>> updateDrive(@PathVariable String id,
                                                                  @Valid @RequestBody PlacementDriveDto dto) {
        PlacementDrive updated = driveService.updateDrive(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Placement drive updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDrive(@PathVariable String id) {
        driveService.deleteDrive(id);
        return ResponseEntity.ok(ApiResponse.success("Placement drive deleted successfully"));
    }

    @GetMapping("/{id}/eligible-students")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getEligibleStudents(@PathVariable String id) {
        PlacementDrive drive = driveService.getDriveById(id);
        List<CandidateEligibility> allEvaluations = eligibilityService.evaluateAllCandidatesForDrive(drive);

        List<CandidateEligibility> eligible = allEvaluations.stream()
                .filter(CandidateEligibility::isEligible)
                .collect(Collectors.toList());

        List<CandidateEligibility> ineligible = allEvaluations.stream()
                .filter(c -> !c.isEligible())
                .collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("drive", drive);
        data.put("eligible", eligible);
        data.put("ineligible", ineligible);
        data.put("totalEligible", eligible.size());
        data.put("totalIneligible", ineligible.size());

        return ResponseEntity.ok(ApiResponse.success("Candidate eligibility evaluated successfully", data));
    }
}
