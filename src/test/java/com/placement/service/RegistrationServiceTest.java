package com.placement.service;

import com.placement.config.StorageConfig;
import com.placement.dto.RegistrationRequest;
import com.placement.dto.StatusUpdateRequest;
import com.placement.exception.DriveNotActiveException;
import com.placement.exception.DuplicateResourceException;
import com.placement.exception.IneligibleStudentException;
import com.placement.exception.InvalidStatusTransitionException;
import com.placement.model.DriveStatus;
import com.placement.model.PlacementDrive;
import com.placement.model.PlacementStatus;
import com.placement.model.Registration;
import com.placement.model.StatusHistory;
import com.placement.model.Student;
import com.placement.repository.JsonStorageEngine;
import com.placement.repository.PlacementDriveRepository;
import com.placement.repository.RegistrationRepository;
import com.placement.repository.StatusHistoryRepository;
import com.placement.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RegistrationServiceTest {

    @TempDir
    Path tempDir;

    private StudentRepository studentRepository;
    private PlacementDriveRepository driveRepository;
    private RegistrationRepository registrationRepository;
    private StatusHistoryRepository historyRepository;
    private EligibilityService eligibilityService;
    private RegistrationService registrationService;

    private Student testStudent;
    private PlacementDrive testDrive;

    @BeforeEach
    void setUp() {
        StorageConfig storageConfig = new StorageConfig();
        ReflectionTestUtils.setField(storageConfig, "dataDirConfig", tempDir.resolve("data").toString());
        ReflectionTestUtils.setField(storageConfig, "backupDirConfig", tempDir.resolve("backups").toString());
        ReflectionTestUtils.setField(storageConfig, "reportDirConfig", tempDir.resolve("reports").toString());
        storageConfig.init();

        JsonStorageEngine engine = new JsonStorageEngine(storageConfig);
        studentRepository = new StudentRepository(engine);
        driveRepository = new PlacementDriveRepository(engine);
        registrationRepository = new RegistrationRepository(engine);
        historyRepository = new StatusHistoryRepository(engine);
        eligibilityService = new EligibilityService(studentRepository, registrationRepository);

        registrationService = new RegistrationService(
                registrationRepository,
                studentRepository,
                driveRepository,
                historyRepository,
                eligibilityService
        );

        testStudent = new Student("S1", "21CS001", "Alice Code", "CSE", 8.8,
                List.of("Java", "SQL"), "alice@test.com", "1234567890", 0, "2026-01-01", "2026-01-01");
        studentRepository.save(testStudent);

        testDrive = new PlacementDrive("D1", "TechCorp", "SWE", 15.0, 7.5,
                List.of("CSE"), List.of("Java"), 0, "2026-12-31",
                DriveStatus.ACTIVE, "Job description", "2026-01-01", "2026-01-01");
        driveRepository.save(testDrive);
    }

    @Test
    @DisplayName("Should successfully register an eligible student to an active drive")
    void testSuccessfulRegistration() {
        Registration registered = registrationService.registerStudent(new RegistrationRequest("S1", "D1"));

        assertNotNull(registered);
        assertNotNull(registered.getId());
        assertEquals(PlacementStatus.REGISTERED, registered.getStatus());
        assertEquals("TechCorp", registered.getCompanyName());
        assertEquals("Alice Code", registered.getStudentName());

        List<StatusHistory> history = historyRepository.findByRegistrationId(registered.getId());
        assertEquals(1, history.size());
        assertEquals(PlacementStatus.REGISTERED, history.get(0).getToStatus());
    }

    @Test
    @DisplayName("Should reject duplicate registration for same student and drive")
    void testDuplicateRegistrationFails() {
        registrationService.registerStudent(new RegistrationRequest("S1", "D1"));

        assertThrows(DuplicateResourceException.class, () -> {
            registrationService.registerStudent(new RegistrationRequest("S1", "D1"));
        });

        assertEquals(1, registrationRepository.findAll().size());
    }

    @Test
    @DisplayName("Should reject registration for CLOSED or COMPLETED drive")
    void testClosedDriveRegistrationFails() {
        testDrive.setStatus(DriveStatus.CLOSED);
        driveRepository.save(testDrive);

        assertThrows(DriveNotActiveException.class, () -> {
            registrationService.registerStudent(new RegistrationRequest("S1", "D1"));
        });
    }

    @Test
    @DisplayName("Should reject registration for ineligible student")
    void testIneligibleStudentRegistrationFails() {
        Student lowCgpaStudent = new Student("S2", "21CS002", "Bob Low", "CSE", 6.0,
                List.of("Java"), "bob@test.com", "123", 0, null, null);
        studentRepository.save(lowCgpaStudent);

        assertThrows(IneligibleStudentException.class, () -> {
            registrationService.registerStudent(new RegistrationRequest("S2", "D1"));
        });
    }

    @Test
    @DisplayName("Should allow valid status transitions: REGISTERED -> SHORTLISTED -> INTERVIEW -> SELECTED")
    void testValidStatusTransitions() {
        Registration registered = registrationService.registerStudent(new RegistrationRequest("S1", "D1"));

        // 1. REGISTERED -> SHORTLISTED
        Registration updated1 = registrationService.updateStatus(registered.getId(),
                new StatusUpdateRequest(PlacementStatus.SHORTLISTED, "Cleared OA"));
        assertEquals(PlacementStatus.SHORTLISTED, updated1.getStatus());

        // 2. SHORTLISTED -> INTERVIEW
        Registration updated2 = registrationService.updateStatus(registered.getId(),
                new StatusUpdateRequest(PlacementStatus.INTERVIEW, "Round 1 passed"));
        assertEquals(PlacementStatus.INTERVIEW, updated2.getStatus());

        // 3. INTERVIEW -> SELECTED
        Registration updated3 = registrationService.updateStatus(registered.getId(),
                new StatusUpdateRequest(PlacementStatus.SELECTED, "Offer letter issued"));
        assertEquals(PlacementStatus.SELECTED, updated3.getStatus());

        List<StatusHistory> history = historyRepository.findByRegistrationId(registered.getId());
        assertEquals(4, history.size()); // Initial + 3 transitions
    }

    @Test
    @DisplayName("Should reject invalid status transition directly from REGISTERED to SELECTED")
    void testInvalidStatusTransitionFails() {
        Registration registered = registrationService.registerStudent(new RegistrationRequest("S1", "D1"));

        assertThrows(InvalidStatusTransitionException.class, () -> {
            registrationService.updateStatus(registered.getId(),
                    new StatusUpdateRequest(PlacementStatus.SELECTED, "Direct jump"));
        });
    }
}
