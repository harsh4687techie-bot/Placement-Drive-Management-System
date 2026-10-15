package com.placement.service;

import com.placement.config.StorageConfig;
import com.placement.model.CandidateEligibility;
import com.placement.model.DriveStatus;
import com.placement.model.PlacementDrive;
import com.placement.model.Student;
import com.placement.repository.JsonStorageEngine;
import com.placement.repository.RegistrationRepository;
import com.placement.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EligibilityServiceTest {

    @TempDir
    Path tempDir;

    private StudentRepository studentRepository;
    private RegistrationRepository registrationRepository;
    private EligibilityService eligibilityService;

    @BeforeEach
    void setUp() {
        StorageConfig storageConfig = new StorageConfig();
        ReflectionTestUtils.setField(storageConfig, "dataDirConfig", tempDir.resolve("data").toString());
        ReflectionTestUtils.setField(storageConfig, "backupDirConfig", tempDir.resolve("backups").toString());
        ReflectionTestUtils.setField(storageConfig, "reportDirConfig", tempDir.resolve("reports").toString());
        storageConfig.init();

        JsonStorageEngine engine = new JsonStorageEngine(storageConfig);
        studentRepository = new StudentRepository(engine);
        registrationRepository = new RegistrationRepository(engine);
        eligibilityService = new EligibilityService(studentRepository, registrationRepository);
    }

    @Test
    @DisplayName("Eligible student meeting CGPA, branch, backlogs, and skills should pass")
    void testEligibleStudentPasses() {
        Student student = new Student("S1", "21CS001", "John Doe", "CSE", 8.5,
                Arrays.asList("Java", "Spring Boot", "SQL"), "john@test.com", "1234567890", 0, "2026-01-01", "2026-01-01");

        PlacementDrive drive = new PlacementDrive("D1", "TechCorp", "Software Engineer", 12.0, 7.5,
                Arrays.asList("CSE", "IT"), Arrays.asList("Java", "SQL"), 0, "2026-12-31",
                DriveStatus.ACTIVE, "Role", "2026-01-01", "2026-01-01");

        CandidateEligibility result = eligibilityService.evaluateEligibility(student, drive);

        assertTrue(result.isEligible(), "Student should be eligible");
        assertTrue(result.getReasons().isEmpty(), "No ineligibility reasons should exist");
        assertFalse(result.isAlreadyRegistered());
    }

    @Test
    @DisplayName("Student with CGPA below minimum requirement should fail eligibility")
    void testCgpaBelowThresholdFails() {
        Student student = new Student("S1", "21CS001", "John Doe", "CSE", 6.8,
                Arrays.asList("Java", "SQL"), null, null, 0, null, null);

        PlacementDrive drive = new PlacementDrive("D1", "TechCorp", "Software Engineer", 12.0, 7.5,
                List.of("CSE"), List.of("Java"), 0, "2026-12-31",
                DriveStatus.ACTIVE, "Role", null, null);

        CandidateEligibility result = eligibilityService.evaluateEligibility(student, drive);

        assertFalse(result.isEligible());
        assertTrue(result.getReasons().stream().anyMatch(r -> r.contains("CGPA 6.80 is below minimum requirement of 7.50")));
    }

    @Test
    @DisplayName("Student from non-eligible branch should fail eligibility")
    void testBranchMismatchFails() {
        Student student = new Student("S1", "21ME001", "Mike Mechanix", "ME", 8.8,
                Arrays.asList("Java", "SQL"), null, null, 0, null, null);

        PlacementDrive drive = new PlacementDrive("D1", "TechCorp", "Software Engineer", 12.0, 7.5,
                Arrays.asList("CSE", "IT"), List.of("Java"), 0, "2026-12-31",
                DriveStatus.ACTIVE, "Role", null, null);

        CandidateEligibility result = eligibilityService.evaluateEligibility(student, drive);

        assertFalse(result.isEligible());
        assertTrue(result.getReasons().stream().anyMatch(r -> r.contains("Branch 'ME' is not in eligible branches")));
    }

    @Test
    @DisplayName("Mandatory skill matching must be case-insensitive and ignore whitespace")
    void testSkillMatchingCaseAndWhitespaceInsensitive() {
        Student student = new Student("S1", "21CS001", "Alice Code", "CSE", 8.0,
                Arrays.asList("  java  ", "SPRING BOOT", "sql"), null, null, 0, null, null);

        PlacementDrive drive = new PlacementDrive("D1", "TechCorp", "Backend Dev", 10.0, 7.0,
                List.of("CSE"), Arrays.asList("JAVA", " spring boot ", "SQL"), 0, "2026-12-31",
                DriveStatus.ACTIVE, "Role", null, null);

        CandidateEligibility result = eligibilityService.evaluateEligibility(student, drive);

        assertTrue(result.isEligible(), "Student should be eligible with case-insensitive whitespace-trimmed skill match");
        assertTrue(result.getReasons().isEmpty());
    }

    @Test
    @DisplayName("Missing mandatory skills should fail eligibility with exact details")
    void testMissingMandatorySkillsFails() {
        Student student = new Student("S1", "21CS001", "Alice Code", "CSE", 8.0,
                List.of("Java"), null, null, 0, null, null);

        PlacementDrive drive = new PlacementDrive("D1", "TechCorp", "Backend Dev", 10.0, 7.0,
                List.of("CSE"), Arrays.asList("Java", "Docker", "Kubernetes"), 0, "2026-12-31",
                DriveStatus.ACTIVE, "Role", null, null);

        CandidateEligibility result = eligibilityService.evaluateEligibility(student, drive);

        assertFalse(result.isEligible());
        assertTrue(result.getReasons().stream().anyMatch(r -> r.contains("Missing mandatory skills") && r.contains("Docker")));
    }

    @Test
    @DisplayName("Backlogs exceeding maximum allowed should fail eligibility")
    void testBacklogsExceedingLimitFails() {
        Student student = new Student("S1", "21CS001", "Bob Backlog", "CSE", 8.0,
                List.of("Java"), null, null, 2, null, null);

        PlacementDrive drive = new PlacementDrive("D1", "TechCorp", "Dev", 10.0, 7.0,
                List.of("CSE"), List.of("Java"), 0, "2026-12-31",
                DriveStatus.ACTIVE, "Role", null, null);

        CandidateEligibility result = eligibilityService.evaluateEligibility(student, drive);

        assertFalse(result.isEligible());
        assertTrue(result.getReasons().stream().anyMatch(r -> r.contains("Backlogs count (2) exceeds maximum allowed (0)")));
    }
}
