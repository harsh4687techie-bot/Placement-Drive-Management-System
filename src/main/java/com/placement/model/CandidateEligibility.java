package com.placement.model;

import java.util.ArrayList;
import java.util.List;

public class CandidateEligibility {
    private Student student;
    private boolean eligible;
    private List<String> reasons = new ArrayList<>();
    private boolean alreadyRegistered;
    private String currentRegistrationStatus;

    public CandidateEligibility() {
    }

    public CandidateEligibility(Student student, boolean eligible, List<String> reasons,
                                boolean alreadyRegistered, String currentRegistrationStatus) {
        this.student = student;
        this.eligible = eligible;
        this.reasons = reasons != null ? new ArrayList<>(reasons) : new ArrayList<>();
        this.alreadyRegistered = alreadyRegistered;
        this.currentRegistrationStatus = currentRegistrationStatus;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public boolean isEligible() {
        return eligible;
    }

    public void setEligible(boolean eligible) {
        this.eligible = eligible;
    }

    public List<String> getReasons() {
        if (reasons == null) {
            reasons = new ArrayList<>();
        }
        return reasons;
    }

    public void setReasons(List<String> reasons) {
        this.reasons = reasons != null ? new ArrayList<>(reasons) : new ArrayList<>();
    }

    public boolean isAlreadyRegistered() {
        return alreadyRegistered;
    }

    public void setAlreadyRegistered(boolean alreadyRegistered) {
        this.alreadyRegistered = alreadyRegistered;
    }

    public String getCurrentRegistrationStatus() {
        return currentRegistrationStatus;
    }

    public void setCurrentRegistrationStatus(String currentRegistrationStatus) {
        this.currentRegistrationStatus = currentRegistrationStatus;
    }
}
