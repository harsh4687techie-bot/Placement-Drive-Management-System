package com.placement.controller;

import com.placement.dto.ApiResponse;
import com.placement.model.BranchStats;
import com.placement.model.CandidateEligibility;
import com.placement.model.CompanyStats;
import com.placement.model.Registration;
import com.placement.model.Student;
import com.placement.service.ReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/students")
    public ResponseEntity<ApiResponse<List<Student>>> getStudentDirectory() {
        return ResponseEntity.ok(ApiResponse.success("Student directory retrieved", reportService.getStudentDirectory()));
    }

    @GetMapping("/eligible-candidates")
    public ResponseEntity<ApiResponse<List<CandidateEligibility>>> getEligibleCandidates(@RequestParam String driveId) {
        return ResponseEntity.ok(ApiResponse.success("Eligible candidates for drive retrieved",
                reportService.getEligibleCandidatesByDrive(driveId)));
    }

    @GetMapping("/shortlisted")
    public ResponseEntity<ApiResponse<List<Registration>>> getShortlisted() {
        return ResponseEntity.ok(ApiResponse.success("Shortlisted candidates retrieved", reportService.getShortlistedCandidates()));
    }

    @GetMapping("/interview")
    public ResponseEntity<ApiResponse<List<Registration>>> getInterview() {
        return ResponseEntity.ok(ApiResponse.success("Interview candidates retrieved", reportService.getInterviewCandidates()));
    }

    @GetMapping("/selected")
    public ResponseEntity<ApiResponse<List<Registration>>> getSelected() {
        return ResponseEntity.ok(ApiResponse.success("Selected candidates retrieved", reportService.getSelectedCandidates()));
    }

    @GetMapping("/company-wise")
    public ResponseEntity<ApiResponse<List<CompanyStats>>> getCompanyWise() {
        return ResponseEntity.ok(ApiResponse.success("Company-wise statistics retrieved", reportService.getCompanyWiseStats()));
    }

    @GetMapping("/branch-wise")
    public ResponseEntity<ApiResponse<List<BranchStats>>> getBranchWise() {
        return ResponseEntity.ok(ApiResponse.success("Branch-wise statistics retrieved", reportService.getBranchWiseStats()));
    }

    @GetMapping("/export-csv")
    public ResponseEntity<byte[]> exportCsv(
            @RequestParam(defaultValue = "students") String type,
            @RequestParam(required = false) String driveId) {

        String csvContent = reportService.generateCsv(type, driveId);
        String filename = "placement_report_" + type.toLowerCase() + "_" + System.currentTimeMillis() + ".csv";

        byte[] bytes = csvContent.getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(bytes);
    }
}
