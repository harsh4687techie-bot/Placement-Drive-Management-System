package com.placement.dto;

import jakarta.validation.constraints.NotBlank;

public class RegistrationRequest {
    @NotBlank(message = "Student ID is required")
    private String studentId;

    @NotBlank(message = "Drive ID is required")
    private String driveId;

    public RegistrationRequest() {
    }

    public RegistrationRequest(String studentId, String driveId) {
        this.studentId = studentId;
        this.driveId = driveId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getDriveId() {
        return driveId;
    }

    public void setDriveId(String driveId) {
        this.driveId = driveId;
    }
}
