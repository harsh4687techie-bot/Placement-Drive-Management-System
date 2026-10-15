package com.placement.model;

import java.util.ArrayList;
import java.util.List;

public class Student {
    private String id;
    private String rollNumber;
    private String fullName;
    private String branch;
    private Double cgpa;
    private List<String> skills = new ArrayList<>();
    private String email;
    private String phone;
    private Integer backlogs = 0;
    private String createdAt;
    private String updatedAt;

    public Student() {
    }

    public Student(String id, String rollNumber, String fullName, String branch, Double cgpa,
                   List<String> skills, String email, String phone, Integer backlogs,
                   String createdAt, String updatedAt) {
        this.id = id;
        this.rollNumber = rollNumber;
        this.fullName = fullName;
        this.branch = branch;
        this.cgpa = cgpa;
        this.skills = skills != null ? new ArrayList<>(skills) : new ArrayList<>();
        this.email = email;
        this.phone = phone;
        this.backlogs = backlogs != null ? backlogs : 0;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public Double getCgpa() {
        return cgpa;
    }

    public void setCgpa(Double cgpa) {
        this.cgpa = cgpa;
    }

    public List<String> getSkills() {
        if (skills == null) {
            skills = new ArrayList<>();
        }
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills != null ? new ArrayList<>(skills) : new ArrayList<>();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getBacklogs() {
        return backlogs != null ? backlogs : 0;
    }

    public void setBacklogs(Integer backlogs) {
        this.backlogs = backlogs != null ? backlogs : 0;
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
