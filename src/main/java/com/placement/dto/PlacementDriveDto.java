package com.placement.dto;

import com.placement.model.DriveStatus;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

public class PlacementDriveDto {
    @NotBlank(message = "Company name is required")
    private String companyName;

    @NotBlank(message = "Job role is required")
    private String jobRole;

    @NotNull(message = "Annual package is required")
    @DecimalMin(value = "0.01", message = "Annual package must be greater than 0")
    private Double packageLpa;

    @NotNull(message = "Minimum CGPA is required")
    @DecimalMin(value = "0.0", message = "Minimum CGPA must be at least 0.0")
    @DecimalMax(value = "10.0", message = "Minimum CGPA cannot exceed 10.0")
    private Double minCgpa;

    @NotEmpty(message = "At least one eligible branch is required")
    private List<String> eligibleBranches = new ArrayList<>();

    private List<String> requiredSkills = new ArrayList<>();

    @Min(value = 0, message = "Maximum backlogs cannot be negative")
    private Integer maxBacklogs = 0;

    @NotBlank(message = "Application deadline is required")
    private String applicationDeadline;

    private DriveStatus status = DriveStatus.ACTIVE;

    private String jobDescription;

    public PlacementDriveDto() {
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getJobRole() {
        return jobRole;
    }

    public void setJobRole(String jobRole) {
        this.jobRole = jobRole;
    }

    public Double getPackageLpa() {
        return packageLpa;
    }

    public void setPackageLpa(Double packageLpa) {
        this.packageLpa = packageLpa;
    }

    public Double getMinCgpa() {
        return minCgpa;
    }

    public void setMinCgpa(Double minCgpa) {
        this.minCgpa = minCgpa;
    }

    public List<String> getEligibleBranches() {
        if (eligibleBranches == null) {
            eligibleBranches = new ArrayList<>();
        }
        return eligibleBranches;
    }

    public void setEligibleBranches(List<String> eligibleBranches) {
        this.eligibleBranches = eligibleBranches != null ? new ArrayList<>(eligibleBranches) : new ArrayList<>();
    }

    public List<String> getRequiredSkills() {
        if (requiredSkills == null) {
            requiredSkills = new ArrayList<>();
        }
        return requiredSkills;
    }

    public void setRequiredSkills(List<String> requiredSkills) {
        this.requiredSkills = requiredSkills != null ? new ArrayList<>(requiredSkills) : new ArrayList<>();
    }

    public Integer getMaxBacklogs() {
        return maxBacklogs != null ? maxBacklogs : 0;
    }

    public void setMaxBacklogs(Integer maxBacklogs) {
        this.maxBacklogs = maxBacklogs != null ? maxBacklogs : 0;
    }

    public String getApplicationDeadline() {
        return applicationDeadline;
    }

    public void setApplicationDeadline(String applicationDeadline) {
        this.applicationDeadline = applicationDeadline;
    }

    public DriveStatus getStatus() {
        return status != null ? status : DriveStatus.ACTIVE;
    }

    public void setStatus(DriveStatus status) {
        this.status = status != null ? status : DriveStatus.ACTIVE;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }
}
