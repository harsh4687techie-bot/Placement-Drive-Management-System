package com.placement.model;

import java.util.ArrayList;
import java.util.List;

public class PlacementDrive {
    private String id;
    private String companyName;
    private String jobRole;
    private Double packageLpa;
    private Double minCgpa;
    private List<String> eligibleBranches = new ArrayList<>();
    private List<String> requiredSkills = new ArrayList<>();
    private Integer maxBacklogs = 0;
    private String applicationDeadline;
    private DriveStatus status = DriveStatus.ACTIVE;
    private String jobDescription;
    private String createdAt;
    private String updatedAt;

    public PlacementDrive() {
    }

    public PlacementDrive(String id, String companyName, String jobRole, Double packageLpa,
                          Double minCgpa, List<String> eligibleBranches, List<String> requiredSkills,
                          Integer maxBacklogs, String applicationDeadline, DriveStatus status,
                          String jobDescription, String createdAt, String updatedAt) {
        this.id = id;
        this.companyName = companyName;
        this.jobRole = jobRole;
        this.packageLpa = packageLpa;
        this.minCgpa = minCgpa;
        this.eligibleBranches = eligibleBranches != null ? new ArrayList<>(eligibleBranches) : new ArrayList<>();
        this.requiredSkills = requiredSkills != null ? new ArrayList<>(requiredSkills) : new ArrayList<>();
        this.maxBacklogs = maxBacklogs != null ? maxBacklogs : 0;
        this.applicationDeadline = applicationDeadline;
        this.status = status != null ? status : DriveStatus.ACTIVE;
        this.jobDescription = jobDescription;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
