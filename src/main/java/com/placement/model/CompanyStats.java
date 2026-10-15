package com.placement.model;

public class CompanyStats {
    private String driveId;
    private String companyName;
    private String jobRole;
    private Double packageLpa;
    private DriveStatus status;
    private long totalRegistrations;
    private long shortlistedCount;
    private long interviewCount;
    private long selectedCount;
    private long rejectedCount;

    public CompanyStats() {
    }

    public CompanyStats(String driveId, String companyName, String jobRole, Double packageLpa,
                        DriveStatus status, long totalRegistrations, long shortlistedCount,
                        long interviewCount, long selectedCount, long rejectedCount) {
        this.driveId = driveId;
        this.companyName = companyName;
        this.jobRole = jobRole;
        this.packageLpa = packageLpa;
        this.status = status;
        this.totalRegistrations = totalRegistrations;
        this.shortlistedCount = shortlistedCount;
        this.interviewCount = interviewCount;
        this.selectedCount = selectedCount;
        this.rejectedCount = rejectedCount;
    }

    public String getDriveId() {
        return driveId;
    }

    public void setDriveId(String driveId) {
        this.driveId = driveId;
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

    public DriveStatus getStatus() {
        return status;
    }

    public void setStatus(DriveStatus status) {
        this.status = status;
    }

    public long getTotalRegistrations() {
        return totalRegistrations;
    }

    public void setTotalRegistrations(long totalRegistrations) {
        this.totalRegistrations = totalRegistrations;
    }

    public long getShortlistedCount() {
        return shortlistedCount;
    }

    public void setShortlistedCount(long shortlistedCount) {
        this.shortlistedCount = shortlistedCount;
    }

    public long getInterviewCount() {
        return interviewCount;
    }

    public void setInterviewCount(long interviewCount) {
        this.interviewCount = interviewCount;
    }

    public long getSelectedCount() {
        return selectedCount;
    }

    public void setSelectedCount(long selectedCount) {
        this.selectedCount = selectedCount;
    }

    public long getRejectedCount() {
        return rejectedCount;
    }

    public void setRejectedCount(long rejectedCount) {
        this.rejectedCount = rejectedCount;
    }
}
