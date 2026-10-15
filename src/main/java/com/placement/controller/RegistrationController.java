package com.placement.controller;

import com.placement.dto.ApiResponse;
import com.placement.dto.RegistrationRequest;
import com.placement.dto.StatusUpdateRequest;
import com.placement.model.PlacementStatus;
import com.placement.model.Registration;
import com.placement.model.StatusHistory;
import com.placement.service.RegistrationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/registrations")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Registration>>> getAllRegistrations(
            @RequestParam(required = false) String driveId,
            @RequestParam(required = false) String studentId,
            @RequestParam(required = false) PlacementStatus status,
            @RequestParam(required = false) String search) {

        List<Registration> list = registrationService.getAllRegistrations(driveId, studentId, status, search);
        return ResponseEntity.ok(ApiResponse.success("Registrations retrieved successfully", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Registration>> getRegistrationById(@PathVariable String id) {
        Registration registration = registrationService.getRegistrationById(id);
        return ResponseEntity.ok(ApiResponse.success("Registration retrieved", registration));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Registration>> registerStudent(@Valid @RequestBody RegistrationRequest request) {
        Registration created = registrationService.registerStudent(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Student successfully registered for the placement drive", created));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Registration>> updateStatus(
            @PathVariable String id,
            @Valid @RequestBody StatusUpdateRequest request) {

        Registration updated = registrationService.updateStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Registration status updated successfully", updated));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<ApiResponse<List<StatusHistory>>> getRegistrationHistory(@PathVariable String id) {
        List<StatusHistory> history = registrationService.getRegistrationHistory(id);
        return ResponseEntity.ok(ApiResponse.success("Status history retrieved", history));
    }
}
