package com.placement.service;

import com.placement.dto.PlacementDriveDto;
import com.placement.exception.ResourceNotFoundException;
import com.placement.model.DriveStatus;
import com.placement.model.PlacementDrive;
import com.placement.repository.PlacementDriveRepository;
import com.placement.repository.RegistrationRepository;
import com.placement.util.DateTimeUtil;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DriveService {

    private final PlacementDriveRepository driveRepository;
    private final RegistrationRepository registrationRepository;

    public DriveService(PlacementDriveRepository driveRepository, RegistrationRepository registrationRepository) {
        this.driveRepository = driveRepository;
        this.registrationRepository = registrationRepository;
    }

    public List<PlacementDrive> getAllDrives(String search, DriveStatus status, Double minPackage, String sortBy, String sortOrder) {
        List<PlacementDrive> drives = driveRepository.findAll();

        return drives.stream()
                .filter(d -> {
                    if (search == null || search.isBlank()) return true;
                    String q = search.trim().toLowerCase();
                    boolean compMatch = d.getCompanyName() != null && d.getCompanyName().toLowerCase().contains(q);
                    boolean roleMatch = d.getJobRole() != null && d.getJobRole().toLowerCase().contains(q);
                    return compMatch || roleMatch;
                })
                .filter(d -> {
                    if (status == null) return true;
                    return d.getStatus() == status;
                })
                .filter(d -> {
                    if (minPackage == null) return true;
                    return d.getPackageLpa() != null && d.getPackageLpa() >= minPackage;
                })
                .sorted((d1, d2) -> {
                    int result = 0;
                    if ("package".equalsIgnoreCase(sortBy)) {
                        Double p1 = d1.getPackageLpa() != null ? d1.getPackageLpa() : 0.0;
                        Double p2 = d2.getPackageLpa() != null ? d2.getPackageLpa() : 0.0;
                        result = p1.compareTo(p2);
                    } else if ("deadline".equalsIgnoreCase(sortBy)) {
                        String dl1 = d1.getApplicationDeadline() != null ? d1.getApplicationDeadline() : "";
                        String dl2 = d2.getApplicationDeadline() != null ? d2.getApplicationDeadline() : "";
                        result = dl1.compareToIgnoreCase(dl2);
                    } else { // default sort by company
                        String c1 = d1.getCompanyName() != null ? d1.getCompanyName() : "";
                        String c2 = d2.getCompanyName() != null ? d2.getCompanyName() : "";
                        result = c1.compareToIgnoreCase(c2);
                    }
                    return "desc".equalsIgnoreCase(sortOrder) ? -result : result;
                })
                .collect(Collectors.toList());
    }

    public PlacementDrive getDriveById(String id) {
        return driveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Placement drive not found with ID: " + id));
    }

    public synchronized PlacementDrive createDrive(PlacementDriveDto dto) {
        PlacementDrive drive = new PlacementDrive();
        drive.setCompanyName(dto.getCompanyName().trim());
        drive.setJobRole(dto.getJobRole().trim());
        drive.setPackageLpa(dto.getPackageLpa());
        drive.setMinCgpa(dto.getMinCgpa());
        drive.setEligibleBranches(dto.getEligibleBranches() != null ? dto.getEligibleBranches() : List.of());
        drive.setRequiredSkills(dto.getRequiredSkills() != null ? dto.getRequiredSkills() : List.of());
        drive.setMaxBacklogs(dto.getMaxBacklogs() != null ? dto.getMaxBacklogs() : 0);
        drive.setApplicationDeadline(dto.getApplicationDeadline());
        drive.setStatus(dto.getStatus() != null ? dto.getStatus() : DriveStatus.ACTIVE);
        drive.setJobDescription(dto.getJobDescription() != null ? dto.getJobDescription().trim() : null);
        drive.setCreatedAt(DateTimeUtil.nowIso());
        drive.setUpdatedAt(DateTimeUtil.nowIso());

        return driveRepository.save(drive);
    }

    public synchronized PlacementDrive updateDrive(String id, PlacementDriveDto dto) {
        PlacementDrive existing = getDriveById(id);

        existing.setCompanyName(dto.getCompanyName().trim());
        existing.setJobRole(dto.getJobRole().trim());
        existing.setPackageLpa(dto.getPackageLpa());
        existing.setMinCgpa(dto.getMinCgpa());
        existing.setEligibleBranches(dto.getEligibleBranches() != null ? dto.getEligibleBranches() : List.of());
        existing.setRequiredSkills(dto.getRequiredSkills() != null ? dto.getRequiredSkills() : List.of());
        existing.setMaxBacklogs(dto.getMaxBacklogs() != null ? dto.getMaxBacklogs() : 0);
        existing.setApplicationDeadline(dto.getApplicationDeadline());
        existing.setStatus(dto.getStatus() != null ? dto.getStatus() : DriveStatus.ACTIVE);
        existing.setJobDescription(dto.getJobDescription() != null ? dto.getJobDescription().trim() : null);
        existing.setUpdatedAt(DateTimeUtil.nowIso());

        return driveRepository.save(existing);
    }

    public synchronized boolean deleteDrive(String id) {
        getDriveById(id);
        // Cascade delete registrations for this drive
        registrationRepository.findByDriveId(id).forEach(r -> {
            registrationRepository.deleteById(r.getId());
        });
        return driveRepository.deleteById(id);
    }
}
