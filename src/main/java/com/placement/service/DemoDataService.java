package com.placement.service;

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
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class DemoDataService {

    private static final Logger logger = LoggerFactory.getLogger(DemoDataService.class);

    private final StudentRepository studentRepository;
    private final PlacementDriveRepository driveRepository;
    private final RegistrationRepository registrationRepository;
    private final StatusHistoryRepository historyRepository;

    @Value("${app.demo.auto-load-if-empty:true}")
    private boolean autoLoadIfEmpty;

    public DemoDataService(StudentRepository studentRepository,
                           PlacementDriveRepository driveRepository,
                           RegistrationRepository registrationRepository,
                           StatusHistoryRepository historyRepository) {
        this.studentRepository = studentRepository;
        this.driveRepository = driveRepository;
        this.registrationRepository = registrationRepository;
        this.historyRepository = historyRepository;
    }

    @PostConstruct
    public void init() {
        if (autoLoadIfEmpty && studentRepository.findAll().isEmpty() && driveRepository.findAll().isEmpty()) {
            logger.info("Storage is empty. Automatically initializing demo data for college placement portal...");
            loadDemoData(false);
        }
    }

    public synchronized void resetAllData() {
        studentRepository.saveAll(new ArrayList<>());
        driveRepository.saveAll(new ArrayList<>());
        registrationRepository.saveAll(new ArrayList<>());
        historyRepository.saveAll(new ArrayList<>());
        logger.info("All data cleared successfully.");
    }

    public synchronized void loadDemoData(boolean forceReset) {
        if (forceReset) {
            resetAllData();
        } else if (!studentRepository.findAll().isEmpty() || !driveRepository.findAll().isEmpty()) {
            logger.info("Demo data load skipped: Database already contains records.");
            return;
        }

        String now = DateTimeUtil.nowIso();

        // 12 Fictional Students
        List<Student> students = Arrays.asList(
                new Student("STU-001", "21CS001", "Aarav Sharma", "CSE", 9.42,
                        Arrays.asList("Java", "Algorithms", "System Design", "Spring Boot", "SQL"),
                        "aarav.sharma@college.edu", "9876543210", 0, now, now),
                new Student("STU-002", "21CS002", "Ananya Iyer", "CSE", 8.85,
                        Arrays.asList("Java", "Algorithms", "React", "SQL"),
                        "ananya.iyer@college.edu", "9876543211", 0, now, now),
                new Student("STU-003", "21IT001", "Rohan Mehta", "IT", 8.35,
                        Arrays.asList("Java", "Algorithms", "System Design", "Docker"),
                        "rohan.mehta@college.edu", "9876543212", 0, now, now),
                new Student("STU-004", "21IT002", "Pooja Verma", "IT", 7.90,
                        Arrays.asList("Python", "Linux", "Networking", "AWS"),
                        "pooja.verma@college.edu", "9876543213", 0, now, now),
                new Student("STU-005", "21EC001", "Kunal Singhania", "ECE", 7.45,
                        Arrays.asList("Linux", "Networking", "Python", "Embedded C"),
                        "kunal.s@college.edu", "9876543214", 0, now, now),
                new Student("STU-006", "21EC002", "Sneha Kulkarni", "ECE", 7.10,
                        Arrays.asList("Java", "SQL", "HTML", "CSS"),
                        "sneha.k@college.edu", "9876543215", 1, now, now),
                new Student("STU-007", "21ME001", "Vikram Rathore", "ME", 8.60,
                        Arrays.asList("CAD", "Thermodynamics", "ANSYS", "Matlab"),
                        "vikram.r@college.edu", "9876543216", 0, now, now),
                new Student("STU-008", "21ME002", "Aditya Patel", "ME", 6.80,
                        Arrays.asList("CAD", "Manufacturing", "Java"),
                        "aditya.p@college.edu", "9876543217", 0, now, now),
                new Student("STU-009", "21CE001", "Divya Nair", "CE", 7.75,
                        Arrays.asList("AutoCAD", "STAAD Pro", "Surveying"),
                        "divya.nair@college.edu", "9876543218", 0, now, now),
                new Student("STU-010", "21CS003", "Sameer Deshmukh", "CSE", 6.40,
                        Arrays.asList("Java", "SQL"),
                        "sameer.d@college.edu", "9876543219", 2, now, now),
                new Student("STU-011", "21IT003", "Tanvi Joshi", "IT", 9.15,
                        Arrays.asList("Java", "Algorithms", "System Design", "Python", "Cloud"),
                        "tanvi.j@college.edu", "9876543220", 0, now, now),
                new Student("STU-012", "21EC003", "Rahul Sundaram", "ECE", 6.95,
                        Arrays.asList("Python", "Linux", "Networking"),
                        "rahul.s@college.edu", "9876543221", 0, now, now)
        );
        studentRepository.saveAll(students);

        // 4 Fictional Placement Drives
        List<PlacementDrive> drives = Arrays.asList(
                new PlacementDrive("DRV-101", "Google Cloud", "Software Development Engineer", 24.5, 8.50,
                        Arrays.asList("CSE", "IT"),
                        Arrays.asList("Java", "Algorithms", "System Design"),
                        0, "2026-11-30", DriveStatus.ACTIVE,
                        "Core backend development role working on distributed cloud services.", now, now),

                new PlacementDrive("DRV-102", "Amazon AWS", "Cloud Support Associate", 14.0, 7.20,
                        Arrays.asList("CSE", "IT", "ECE"),
                        Arrays.asList("Linux", "Networking", "Python"),
                        0, "2026-12-15", DriveStatus.ACTIVE,
                        "Consulting enterprise customers on AWS infrastructure and operations.", now, now),

                new PlacementDrive("DRV-103", "Infosys Limited", "Specialist Programmer", 9.5, 6.50,
                        Arrays.asList("CSE", "IT", "ECE", "ME", "CE"),
                        Arrays.asList("Java", "SQL"),
                        1, "2026-11-20", DriveStatus.ACTIVE,
                        "Full stack software engineer delivering digital transformation projects.", now, now),

                new PlacementDrive("DRV-104", "Tata Motors", "Graduate Engineer Trainee", 7.5, 6.50,
                        Arrays.asList("ME", "ECE"),
                        Arrays.asList("CAD", "Thermodynamics"),
                        0, "2026-10-01", DriveStatus.COMPLETED,
                        "Automotive design and powertrain testing engineer.", now, now)
        );
        driveRepository.saveAll(drives);

        // Sample Registrations & Status Tracking
        List<Registration> registrations = new ArrayList<>();
        List<StatusHistory> history = new ArrayList<>();

        // Helper to add registration and status history
        addSampleRegistration(registrations, history, "REG-501", students.get(0), drives.get(0), PlacementStatus.SELECTED, "Selected after Final Technical Round");
        addSampleRegistration(registrations, history, "REG-502", students.get(1), drives.get(0), PlacementStatus.INTERVIEW, "Cleared Technical Round 1");
        addSampleRegistration(registrations, history, "REG-503", students.get(2), drives.get(0), PlacementStatus.SHORTLISTED, "Online Assessment Cleared");
        addSampleRegistration(registrations, history, "REG-504", students.get(10), drives.get(0), PlacementStatus.REGISTERED, "Application under review");

        addSampleRegistration(registrations, history, "REG-505", students.get(3), drives.get(1), PlacementStatus.SELECTED, "Offered role in Cloud Operations");
        addSampleRegistration(registrations, history, "REG-506", students.get(4), drives.get(1), PlacementStatus.INTERVIEW, "Scheduled for Managerial Round");
        addSampleRegistration(registrations, history, "REG-507", students.get(11), drives.get(1), PlacementStatus.SHORTLISTED, "Resume screening passed");

        addSampleRegistration(registrations, history, "REG-508", students.get(5), drives.get(2), PlacementStatus.SHORTLISTED, "Aptitude round cleared");
        addSampleRegistration(registrations, history, "REG-509", students.get(0), drives.get(2), PlacementStatus.WITHDRAWN, "Withdrawn by candidate due to higher offer");

        addSampleRegistration(registrations, history, "REG-510", students.get(6), drives.get(3), PlacementStatus.SELECTED, "Drive completed. Placed in Powertrain division.");

        registrationRepository.saveAll(registrations);
        historyRepository.saveAll(history);

        logger.info("Demo data successfully loaded: {} students, {} drives, {} registrations, {} history records.",
                students.size(), drives.size(), registrations.size(), history.size());
    }

    private void addSampleRegistration(List<Registration> regs, List<StatusHistory> histories,
                                       String regId, Student student, PlacementDrive drive,
                                       PlacementStatus status, String remarks) {
        String now = DateTimeUtil.nowIso();
        Registration reg = new Registration(
                regId,
                student.getId(),
                student.getRollNumber(),
                student.getFullName(),
                drive.getId(),
                drive.getCompanyName(),
                drive.getJobRole(),
                drive.getPackageLpa(),
                status,
                now,
                now,
                remarks
        );
        regs.add(reg);

        StatusHistory h = new StatusHistory(
                UUID.randomUUID().toString(),
                regId,
                student.getId(),
                student.getRollNumber(),
                student.getFullName(),
                drive.getId(),
                drive.getCompanyName(),
                PlacementStatus.REGISTERED,
                status,
                now,
                remarks
        );
        histories.add(h);
    }
}
