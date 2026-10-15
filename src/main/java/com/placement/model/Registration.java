package com.placement.model;

public class Registration {
    private String id;
    private String studentId;
    private String studentRollNumber;
    private String studentName;
    private String driveId;
    private String companyName;
    private String jobRole;
    private Double packageLpa;
    private PlacementStatus status = PlacementStatus.REGISTERED;
    private String registeredAt;
    private String updatedAt;
    private String remarks;

    public Registration() {
    }

    public Registration(String id, String studentId, String studentRollNumber, String studentName,
                        String driveId, String companyName, String jobRole, Double packageLpa,
                        PlacementStatus status, String registeredAt, String updatedAt, String remarks) {
        this.id = id;
        this.studentId = studentId;
        this.studentRollNumber = studentRollNumber;
        this.studentName = studentName;
        this.driveId = driveId;
        this.companyName = companyName;
        this.jobRole = jobRole;
        this.packageLpa = packageLpa;
        this.status = status != null ? status : PlacementStatus.REGISTERED;
        this.registeredAt = registeredAt;
        this.updatedAt = updatedAt;
        this.remarks = remarks;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getStudentRollNumber() {
        return studentRollNumber;
    }

    public void setStudentRollNumber(String studentRollNumber) {
        this.studentRollNumber = studentRollNumber;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
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

    public PlacementStatus getStatus() {
        return status != null ? status : PlacementStatus.REGISTERED;
    }

    public void setStatus(PlacementStatus status) {
        this.status = status != null ? status : PlacementStatus.REGISTERED;
    }

    public String getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(String registeredAt) {
        this.registeredAt = registeredAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
