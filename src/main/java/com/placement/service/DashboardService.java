package com.placement.service;

import com.placement.model.DashboardStats;
import com.placement.model.DriveStatus;
import com.placement.model.PlacementStatus;
import com.placement.repository.PlacementDriveRepository;
import com.placement.repository.RegistrationRepository;
import com.placement.repository.StatusHistoryRepository;
import com.placement.repository.StudentRepository;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    private final StudentRepository studentRepository;
    private final PlacementDriveRepository driveRepository;
    private final RegistrationRepository registrationRepository;
    private final StatusHistoryRepository historyRepository;

    public DashboardService(StudentRepository studentRepository,
                            PlacementDriveRepository driveRepository,
                            RegistrationRepository registrationRepository,
                            StatusHistoryRepository historyRepository) {
        this.studentRepository = studentRepository;
        this.driveRepository = driveRepository;
        this.registrationRepository = registrationRepository;
        this.historyRepository = historyRepository;
    }

    public DashboardStats getStats() {
        long totalStudents = studentRepository.findAll().size();
        var drives = driveRepository.findAll();
        long totalDrives = drives.size();
        long activeDrives = drives.stream().filter(d -> d.getStatus() == DriveStatus.ACTIVE).count();

        var registrations = registrationRepository.findAll();
        long totalRegistrations = registrations.size();
        long shortlisted = registrations.stream().filter(r -> r.getStatus() == PlacementStatus.SHORTLISTED).count();
        long interview = registrations.stream().filter(r -> r.getStatus() == PlacementStatus.INTERVIEW).count();
        long selected = registrations.stream().filter(r -> r.getStatus() == PlacementStatus.SELECTED).count();
        long rejected = registrations.stream().filter(r -> r.getStatus() == PlacementStatus.REJECTED).count();

        var recentActivity = historyRepository.findRecent(10);

        return new DashboardStats(
                totalStudents,
                totalDrives,
                activeDrives,
                totalRegistrations,
                shortlisted,
                interview,
                selected,
                rejected,
                recentActivity
        );
    }
}
