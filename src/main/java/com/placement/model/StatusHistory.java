package com.placement.model;

public class StatusHistory {
    private String id;
    private String registrationId;
    private String studentId;
    private String studentRollNumber;
    private String studentName;
    private String driveId;
    private String companyName;
    private PlacementStatus fromStatus;
    private PlacementStatus toStatus;
    private String changedAt;
    private String remarks;

    public StatusHistory() {
    }

    public StatusHistory(String id, String registrationId, String studentId, String studentRollNumber,
                         String studentName, String driveId, String companyName,
                         PlacementStatus fromStatus, PlacementStatus toStatus,
                         String changedAt, String remarks) {
        this.id = id;
        this.registrationId = registrationId;
        this.studentId = studentId;
        this.studentRollNumber = studentRollNumber;
        this.studentName = studentName;
        this.driveId = driveId;
        this.companyName = companyName;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.changedAt = changedAt;
        this.remarks = remarks;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(String registrationId) {
        this.registrationId = registrationId;
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

    public PlacementStatus getFromStatus() {
        return fromStatus;
    }

    public void setFromStatus(PlacementStatus fromStatus) {
        this.fromStatus = fromStatus;
    }

    public PlacementStatus getToStatus() {
        return toStatus;
    }

    public void setToStatus(PlacementStatus toStatus) {
        this.toStatus = toStatus;
    }

    public String getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(String changedAt) {
        this.changedAt = changedAt;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
