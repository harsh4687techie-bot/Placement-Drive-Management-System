package com.placement.service;

import com.placement.model.CandidateEligibility;
import com.placement.model.PlacementDrive;
import com.placement.model.Registration;
import com.placement.model.Student;
import com.placement.repository.RegistrationRepository;
import com.placement.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EligibilityService {

    private final StudentRepository studentRepository;
    private final RegistrationRepository registrationRepository;

    public EligibilityService(StudentRepository studentRepository, RegistrationRepository registrationRepository) {
        this.studentRepository = studentRepository;
        this.registrationRepository = registrationRepository;
    }

    /**
     * Evaluates whether a student is eligible for a placement drive.
     * Returns CandidateEligibility with boolean flag and detailed reasons if ineligible.
     */
    public CandidateEligibility evaluateEligibility(Student student, PlacementDrive drive) {
        List<String> reasons = new ArrayList<>();

        if (student == null) {
            reasons.add("Student profile does not exist or is null");
            return new CandidateEligibility(null, false, reasons, false, null);
        }

        if (drive == null) {
            reasons.add("Placement drive does not exist or is null");
            return new CandidateEligibility(student, false, reasons, false, null);
        }

        // 1. CGPA Criterion
        if (student.getCgpa() == null) {
            reasons.add("Student CGPA is missing");
        } else if (drive.getMinCgpa() != null && student.getCgpa() < drive.getMinCgpa()) {
            reasons.add(String.format("CGPA %.2f is below minimum requirement of %.2f", student.getCgpa(), drive.getMinCgpa()));
        }

        // 2. Branch Criterion (case-insensitive & trimmed)
        if (student.getBranch() == null || student.getBranch().isBlank()) {
            reasons.add("Student branch is not specified");
        } else if (drive.getEligibleBranches() != null && !drive.getEligibleBranches().isEmpty()) {
            String studentBranchClean = student.getBranch().trim().toUpperCase();
            boolean branchMatch = drive.getEligibleBranches().stream()
                    .filter(b -> b != null && !b.isBlank())
                    .map(b -> b.trim().toUpperCase())
                    .anyMatch(b -> b.equalsIgnoreCase(studentBranchClean));
            if (!branchMatch) {
                reasons.add(String.format("Branch '%s' is not in eligible branches: %s",
                        student.getBranch(), drive.getEligibleBranches()));
            }
        }

        // 3. Backlog Criterion
        int studentBacklogs = student.getBacklogs() != null ? student.getBacklogs() : 0;
        int maxAllowedBacklogs = drive.getMaxBacklogs() != null ? drive.getMaxBacklogs() : 0;
        if (studentBacklogs > maxAllowedBacklogs) {
            reasons.add(String.format("Backlogs count (%d) exceeds maximum allowed (%d)",
                    studentBacklogs, maxAllowedBacklogs));
        }

        // 4. Mandatory Skills Matching (case-insensitive & trimmed)
        if (drive.getRequiredSkills() != null && !drive.getRequiredSkills().isEmpty()) {
            Set<String> studentSkillSet = student.getSkills() != null
                    ? student.getSkills().stream()
                        .filter(s -> s != null && !s.isBlank())
                        .map(s -> s.trim().toLowerCase())
                        .collect(Collectors.toSet())
                    : Collections.emptySet();

            List<String> missingSkills = new ArrayList<>();
            for (String required : drive.getRequiredSkills()) {
                if (required != null && !required.isBlank()) {
                    String cleanReq = required.trim().toLowerCase();
                    if (!studentSkillSet.contains(cleanReq)) {
                        missingSkills.add(required.trim());
                    }
                }
            }

            if (!missingSkills.isEmpty()) {
                reasons.add(String.format("Missing mandatory skills: %s", missingSkills));
            }
        }

        boolean eligible = reasons.isEmpty();

        // Check if student has already registered for this drive
        Optional<Registration> existingReg = registrationRepository.findByStudentIdAndDriveId(student.getId(), drive.getId());
        boolean alreadyRegistered = existingReg.isPresent();
        String currentStatus = existingReg.map(r -> r.getStatus().name()).orElse(null);

        return new CandidateEligibility(student, eligible, reasons, alreadyRegistered, currentStatus);
    }

    /**
     * Evaluates all students against a drive and returns lists of eligible and ineligible candidates.
     */
    public List<CandidateEligibility> evaluateAllCandidatesForDrive(PlacementDrive drive) {
        List<Student> students = studentRepository.findAll();
        List<CandidateEligibility> results = new ArrayList<>();
        for (Student student : students) {
            results.add(evaluateEligibility(student, drive));
        }
        return results;
    }
}
