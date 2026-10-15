package com.placement.service;

import com.placement.model.BranchStats;
import com.placement.model.CandidateEligibility;
import com.placement.model.CompanyStats;
import com.placement.model.PlacementDrive;
import com.placement.model.PlacementStatus;
import com.placement.model.Registration;
import com.placement.model.Student;
import com.placement.repository.PlacementDriveRepository;
import com.placement.repository.RegistrationRepository;
import com.placement.repository.StudentRepository;
import com.placement.util.CsvHelper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final StudentRepository studentRepository;
    private final PlacementDriveRepository driveRepository;
    private final RegistrationRepository registrationRepository;
    private final EligibilityService eligibilityService;

    public ReportService(StudentRepository studentRepository,
                         PlacementDriveRepository driveRepository,
                         RegistrationRepository registrationRepository,
                         EligibilityService eligibilityService) {
        this.studentRepository = studentRepository;
        this.driveRepository = driveRepository;
        this.registrationRepository = registrationRepository;
        this.eligibilityService = eligibilityService;
    }

    public List<Student> getStudentDirectory() {
        return studentRepository.findAll().stream()
                .sorted(Comparator.comparing(Student::getRollNumber, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .collect(Collectors.toList());
    }

    public List<CandidateEligibility> getEligibleCandidatesByDrive(String driveId) {
        PlacementDrive drive = driveRepository.findById(driveId)
                .orElseThrow(() -> new IllegalArgumentException("Placement drive not found with ID: " + driveId));
        return eligibilityService.evaluateAllCandidatesForDrive(drive).stream()
                .filter(CandidateEligibility::isEligible)
                .collect(Collectors.toList());
    }

    public List<Registration> getShortlistedCandidates() {
        return registrationRepository.findByStatus(PlacementStatus.SHORTLISTED);
    }

    public List<Registration> getInterviewCandidates() {
        return registrationRepository.findByStatus(PlacementStatus.INTERVIEW);
    }

    public List<Registration> getSelectedCandidates() {
        return registrationRepository.findByStatus(PlacementStatus.SELECTED);
    }

    public List<CompanyStats> getCompanyWiseStats() {
        List<PlacementDrive> drives = driveRepository.findAll();
        List<Registration> registrations = registrationRepository.findAll();

        List<CompanyStats> results = new ArrayList<>();
        for (PlacementDrive drive : drives) {
            List<Registration> driveRegs = registrations.stream()
                    .filter(r -> drive.getId().equalsIgnoreCase(r.getDriveId()))
                    .collect(Collectors.toList());

            long totalReg = driveRegs.size();
            long shortlisted = driveRegs.stream().filter(r -> r.getStatus() == PlacementStatus.SHORTLISTED).count();
            long interview = driveRegs.stream().filter(r -> r.getStatus() == PlacementStatus.INTERVIEW).count();
            long selected = driveRegs.stream().filter(r -> r.getStatus() == PlacementStatus.SELECTED).count();
            long rejected = driveRegs.stream().filter(r -> r.getStatus() == PlacementStatus.REJECTED).count();

            results.add(new CompanyStats(
                    drive.getId(),
                    drive.getCompanyName(),
                    drive.getJobRole(),
                    drive.getPackageLpa(),
                    drive.getStatus(),
                    totalReg,
                    shortlisted,
                    interview,
                    selected,
                    rejected
            ));
        }

        results.sort((a, b) -> {
            Double p1 = a.getPackageLpa() != null ? a.getPackageLpa() : 0.0;
            Double p2 = b.getPackageLpa() != null ? b.getPackageLpa() : 0.0;
            return p2.compareTo(p1);
        });

        return results;
    }

    public List<BranchStats> getBranchWiseStats() {
        List<Student> students = studentRepository.findAll();
        List<Registration> registrations = registrationRepository.findAll();

        // Unique branches
        Set<String> branches = students.stream()
                .map(Student::getBranch)
                .filter(b -> b != null && !b.isBlank())
                .map(String::toUpperCase)
                .collect(Collectors.toCollection(HashSet::new));

        List<BranchStats> statsList = new ArrayList<>();

        for (String branch : branches) {
            List<Student> branchStudents = students.stream()
                    .filter(s -> branch.equalsIgnoreCase(s.getBranch()))
                    .collect(Collectors.toList());

            Set<String> branchStudentIds = branchStudents.stream()
                    .map(Student::getId)
                    .collect(Collectors.toSet());

            long totalStudents = branchStudents.size();

            // Registered students from this branch (unique students who registered for at least one drive)
            long registeredCount = registrations.stream()
                    .filter(r -> branchStudentIds.contains(r.getStudentId()))
                    .map(Registration::getStudentId)
                    .distinct()
                    .count();

            // Selected students from this branch (unique students with SELECTED status)
            List<Registration> selectedRegs = registrations.stream()
                    .filter(r -> branchStudentIds.contains(r.getStudentId()) && r.getStatus() == PlacementStatus.SELECTED)
                    .collect(Collectors.toList());

            long selectedCount = selectedRegs.stream()
                    .map(Registration::getStudentId)
                    .distinct()
                    .count();

            double placementPercentage = totalStudents > 0
                    ? Math.round(((double) selectedCount / totalStudents) * 10000.0) / 100.0
                    : 0.0;

            double avgPackage = 0.0;
            double maxPackage = 0.0;
            if (!selectedRegs.isEmpty()) {
                double totalPkg = 0.0;
                for (Registration r : selectedRegs) {
                    double pkg = r.getPackageLpa() != null ? r.getPackageLpa() : 0.0;
                    totalPkg += pkg;
                    if (pkg > maxPackage) {
                        maxPackage = pkg;
                    }
                }
                avgPackage = Math.round((totalPkg / selectedRegs.size()) * 100.0) / 100.0;
            }

            statsList.add(new BranchStats(
                    branch,
                    totalStudents,
                    registeredCount,
                    selectedCount,
                    placementPercentage,
                    avgPackage,
                    maxPackage
            ));
        }

        statsList.sort(Comparator.comparing(BranchStats::getBranch));
        return statsList;
    }

    /**
     * Generates CSV string for any supported report type.
     */
    public String generateCsv(String reportType, String driveId) {
        StringBuilder csv = new StringBuilder();

        switch (reportType.toLowerCase()) {
            case "students" -> {
                csv.append("Roll Number,Full Name,Branch,CGPA,Skills,Backlogs,Email,Phone\n");
                for (Student s : getStudentDirectory()) {
                    csv.append(CsvHelper.toCsvRow(Arrays.asList(
                            s.getRollNumber(),
                            s.getFullName(),
                            s.getBranch(),
                            s.getCgpa(),
                            String.join("; ", s.getSkills()),
                            s.getBacklogs(),
                            s.getEmail(),
                            s.getPhone()
                    ))).append("\n");
                }
            }
            case "drives" -> {
                csv.append("Company Name,Job Role,Package (LPA),Min CGPA,Eligible Branches,Required Skills,Max Backlogs,Deadline,Status\n");
                for (PlacementDrive d : driveRepository.findAll()) {
                    csv.append(CsvHelper.toCsvRow(Arrays.asList(
                            d.getCompanyName(),
                            d.getJobRole(),
                            d.getPackageLpa(),
                            d.getMinCgpa(),
                            String.join("; ", d.getEligibleBranches()),
                            String.join("; ", d.getRequiredSkills()),
                            d.getMaxBacklogs(),
                            d.getApplicationDeadline(),
                            d.getStatus()
                    ))).append("\n");
                }
            }
            case "registrations" -> {
                csv.append("Registration ID,Roll Number,Student Name,Company Name,Job Role,Package (LPA),Status,Registration Date\n");
                for (Registration r : registrationRepository.findAll()) {
                    csv.append(CsvHelper.toCsvRow(Arrays.asList(
                            r.getId(),
                            r.getStudentRollNumber(),
                            r.getStudentName(),
                            r.getCompanyName(),
                            r.getJobRole(),
                            r.getPackageLpa(),
                            r.getStatus(),
                            r.getRegisteredAt()
                    ))).append("\n");
                }
            }
            case "shortlisted" -> {
                csv.append("Roll Number,Student Name,Company Name,Job Role,Package (LPA),Status,Date\n");
                for (Registration r : getShortlistedCandidates()) {
                    csv.append(CsvHelper.toCsvRow(Arrays.asList(
                            r.getStudentRollNumber(),
                            r.getStudentName(),
                            r.getCompanyName(),
                            r.getJobRole(),
                            r.getPackageLpa(),
                            r.getStatus(),
                            r.getUpdatedAt()
                    ))).append("\n");
                }
            }
            case "interview" -> {
                csv.append("Roll Number,Student Name,Company Name,Job Role,Package (LPA),Status,Date\n");
                for (Registration r : getInterviewCandidates()) {
                    csv.append(CsvHelper.toCsvRow(Arrays.asList(
                            r.getStudentRollNumber(),
                            r.getStudentName(),
                            r.getCompanyName(),
                            r.getJobRole(),
                            r.getPackageLpa(),
                            r.getStatus(),
                            r.getUpdatedAt()
                    ))).append("\n");
                }
            }
            case "selected" -> {
                csv.append("Roll Number,Student Name,Company Name,Job Role,Package (LPA),Status,Selection Date\n");
                for (Registration r : getSelectedCandidates()) {
                    csv.append(CsvHelper.toCsvRow(Arrays.asList(
                            r.getStudentRollNumber(),
                            r.getStudentName(),
                            r.getCompanyName(),
                            r.getJobRole(),
                            r.getPackageLpa(),
                            r.getStatus(),
                            r.getUpdatedAt()
                    ))).append("\n");
                }
            }
            case "company-wise" -> {
                csv.append("Company Name,Job Role,Package (LPA),Drive Status,Total Registrations,Shortlisted,Interview,Selected,Rejected\n");
                for (CompanyStats cs : getCompanyWiseStats()) {
                    csv.append(CsvHelper.toCsvRow(Arrays.asList(
                            cs.getCompanyName(),
                            cs.getJobRole(),
                            cs.getPackageLpa(),
                            cs.getStatus(),
                            cs.getTotalRegistrations(),
                            cs.getShortlistedCount(),
                            cs.getInterviewCount(),
                            cs.getSelectedCount(),
                            cs.getRejectedCount()
                    ))).append("\n");
                }
            }
            case "branch-wise" -> {
                csv.append("Branch,Total Students,Registered Candidates,Placed Candidates,Placement Rate (%),Average Package (LPA),Highest Package (LPA)\n");
                for (BranchStats bs : getBranchWiseStats()) {
                    csv.append(CsvHelper.toCsvRow(Arrays.asList(
                            bs.getBranch(),
                            bs.getTotalStudents(),
                            bs.getRegisteredStudents(),
                            bs.getSelectedStudents(),
                            bs.getPlacementPercentage() + "%",
                            bs.getAveragePackage(),
                            bs.getHighestPackage()
                    ))).append("\n");
                }
            }
            case "eligible" -> {
                if (driveId == null || driveId.isBlank()) {
                    throw new IllegalArgumentException("Drive ID is required for eligible candidates export");
                }
                PlacementDrive drive = driveRepository.findById(driveId)
                        .orElseThrow(() -> new IllegalArgumentException("Drive not found: " + driveId));
                csv.append("Drive: ").append(drive.getCompanyName()).append(" - ").append(drive.getJobRole()).append("\n");
                csv.append("Roll Number,Full Name,Branch,CGPA,Skills,Backlogs,Registration Status\n");
                for (CandidateEligibility ce : getEligibleCandidatesByDrive(driveId)) {
                    Student s = ce.getStudent();
                    csv.append(CsvHelper.toCsvRow(Arrays.asList(
                            s.getRollNumber(),
                            s.getFullName(),
                            s.getBranch(),
                            s.getCgpa(),
                            String.join("; ", s.getSkills()),
                            s.getBacklogs(),
                            ce.isAlreadyRegistered() ? ce.getCurrentRegistrationStatus() : "Not Registered"
                    ))).append("\n");
                }
            }
            default -> throw new IllegalArgumentException("Unsupported report type: " + reportType);
        }

        return csv.toString();
    }
}
