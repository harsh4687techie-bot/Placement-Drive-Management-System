package com.placement.service;

import com.placement.dto.RegistrationRequest;
import com.placement.dto.StatusUpdateRequest;
import com.placement.exception.DriveNotActiveException;
import com.placement.exception.DuplicateResourceException;
import com.placement.exception.IneligibleStudentException;
import com.placement.exception.InvalidStatusTransitionException;
import com.placement.exception.ResourceNotFoundException;
import com.placement.model.CandidateEligibility;
import com.placement.model.DriveStatus;
import com.placement.model.PlacementDrive;
import com.placement.model.PlacementStatus;
import com.placement.model.Registration;
import com.placement.model.StatusHistory;
import com.placement.model.Student;
import com.placement.repository.PlacementDriveRepository;
import com.placement.repository.RegistrationRepository;
import com.placement.repository.StatusHistoryRepository;
import com.placement.repository.StudentRepository;
import com.placement.util.DateTimeUtil;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final StudentRepository studentRepository;
    private final PlacementDriveRepository driveRepository;
    private final StatusHistoryRepository historyRepository;
    private final EligibilityService eligibilityService;

    // Concurrency lock object for registrations
    private final Object registrationLock = new Object();

    public RegistrationService(RegistrationRepository registrationRepository,
                               StudentRepository studentRepository,
                               PlacementDriveRepository driveRepository,
                               StatusHistoryRepository historyRepository,
                               EligibilityService eligibilityService) {
        this.registrationRepository = registrationRepository;
        this.studentRepository = studentRepository;
        this.driveRepository = driveRepository;
        this.historyRepository = historyRepository;
        this.eligibilityService = eligibilityService;
    }

    public List<Registration> getAllRegistrations(String driveId, String studentId, PlacementStatus status, String search) {
        return registrationRepository.findAll().stream()
                .filter(r -> {
                    if (driveId == null || driveId.isBlank() || driveId.equalsIgnoreCase("ALL")) return true;
                    return r.getDriveId().equalsIgnoreCase(driveId.trim());
                })
                .filter(r -> {
                    if (studentId == null || studentId.isBlank()) return true;
                    return r.getStudentId().equalsIgnoreCase(studentId.trim());
                })
                .filter(r -> {
                    if (status == null) return true;
                    return r.getStatus() == status;
                })
                .filter(r -> {
                    if (search == null || search.isBlank()) return true;
                    String q = search.trim().toLowerCase();
                    boolean sName = r.getStudentName() != null && r.getStudentName().toLowerCase().contains(q);
                    boolean sRoll = r.getStudentRollNumber() != null && r.getStudentRollNumber().toLowerCase().contains(q);
                    boolean comp = r.getCompanyName() != null && r.getCompanyName().toLowerCase().contains(q);
                    boolean role = r.getJobRole() != null && r.getJobRole().toLowerCase().contains(q);
                    return sName || sRoll || comp || role;
                })
                .sorted(Comparator.comparing(Registration::getRegisteredAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    public Registration getRegistrationById(String id) {
        return registrationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registration not found with ID: " + id));
    }

    public Registration registerStudent(RegistrationRequest request) {
        synchronized (registrationLock) {
            Student student = studentRepository.findById(request.getStudentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + request.getStudentId()));

            PlacementDrive drive = driveRepository.findById(request.getDriveId())
                    .orElseThrow(() -> new ResourceNotFoundException("Placement drive not found with ID: " + request.getDriveId()));

            // Verify drive is active
            if (drive.getStatus() != DriveStatus.ACTIVE) {
                throw new DriveNotActiveException("Registration failed: Drive '" + drive.getCompanyName() +
                        "' is " + drive.getStatus() + ". Only ACTIVE drives accept registrations.");
            }

            // Check if already registered
            Optional<Registration> existing = registrationRepository.findByStudentIdAndDriveId(student.getId(), drive.getId());
            if (existing.isPresent()) {
                throw new DuplicateResourceException("Student " + student.getFullName() + " (" + student.getRollNumber() +
                        ") is already registered for " + drive.getCompanyName() + " (" + drive.getJobRole() + ").");
            }

            // Check eligibility
            CandidateEligibility eligibility = eligibilityService.evaluateEligibility(student, drive);
            if (!eligibility.isEligible()) {
                throw new IneligibleStudentException("Student " + student.getFullName() +
                        " does not satisfy the eligibility criteria for this drive.", eligibility.getReasons());
            }

            // Create registration
            String now = DateTimeUtil.nowIso();
            Registration registration = new Registration();
            registration.setStudentId(student.getId());
            registration.setStudentRollNumber(student.getRollNumber());
            registration.setStudentName(student.getFullName());
            registration.setDriveId(drive.getId());
            registration.setCompanyName(drive.getCompanyName());
            registration.setJobRole(drive.getJobRole());
            registration.setPackageLpa(drive.getPackageLpa());
            registration.setStatus(PlacementStatus.REGISTERED);
            registration.setRegisteredAt(now);
            registration.setUpdatedAt(now);
            registration.setRemarks("Initial candidate registration");

            Registration saved = registrationRepository.save(registration);

            // Record initial status in history
            StatusHistory history = new StatusHistory();
            history.setRegistrationId(saved.getId());
            history.setStudentId(student.getId());
            history.setStudentRollNumber(student.getRollNumber());
            history.setStudentName(student.getFullName());
            history.setDriveId(drive.getId());
            history.setCompanyName(drive.getCompanyName());
            history.setFromStatus(null);
            history.setToStatus(PlacementStatus.REGISTERED);
            history.setChangedAt(now);
            history.setRemarks("Application submitted successfully");
            historyRepository.save(history);

            return saved;
        }
    }

    public Registration updateStatus(String registrationId, StatusUpdateRequest request) {
        synchronized (registrationLock) {
            Registration registration = getRegistrationById(registrationId);
            PlacementStatus oldStatus = registration.getStatus();
            PlacementStatus newStatus = request.getStatus();

            if (newStatus == null) {
                throw new IllegalArgumentException("Target status cannot be null");
            }

            if (oldStatus == newStatus) {
                return registration;
            }

            if (!oldStatus.canTransitionTo(newStatus)) {
                throw new InvalidStatusTransitionException(
                        String.format("Invalid status transition from '%s' to '%s'. Allowed transitions: %s",
                                oldStatus, newStatus, oldStatus.getAllowedTransitions())
                );
            }

            String now = DateTimeUtil.nowIso();
            String remarks = request.getRemarks() != null && !request.getRemarks().isBlank()
                    ? request.getRemarks().trim()
                    : "Status updated from " + oldStatus + " to " + newStatus;

            registration.setStatus(newStatus);
            registration.setUpdatedAt(now);
            registration.setRemarks(remarks);

            Registration updated = registrationRepository.save(registration);

            // Add history entry
            StatusHistory history = new StatusHistory();
            history.setRegistrationId(registration.getId());
            history.setStudentId(registration.getStudentId());
            history.setStudentRollNumber(registration.getStudentRollNumber());
            history.setStudentName(registration.getStudentName());
            history.setDriveId(registration.getDriveId());
            history.setCompanyName(registration.getCompanyName());
            history.setFromStatus(oldStatus);
            history.setToStatus(newStatus);
            history.setChangedAt(now);
            history.setRemarks(remarks);
            historyRepository.save(history);

            return updated;
        }
    }

    public List<StatusHistory> getRegistrationHistory(String registrationId) {
        return historyRepository.findByRegistrationId(registrationId);
    }
}
