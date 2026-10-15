package com.placement.model;

import java.util.ArrayList;
import java.util.List;

public class DashboardStats {
    private long totalStudents;
    private long totalDrives;
    private long activeDrives;
    private long totalRegistrations;
    private long shortlistedCount;
    private long interviewCount;
    private long selectedCount;
    private long rejectedCount;
    private List<StatusHistory> recentActivity = new ArrayList<>();

    public DashboardStats() {
    }

    public DashboardStats(long totalStudents, long totalDrives, long activeDrives,
                          long totalRegistrations, long shortlistedCount, long interviewCount,
                          long selectedCount, long rejectedCount, List<StatusHistory> recentActivity) {
        this.totalStudents = totalStudents;
        this.totalDrives = totalDrives;
        this.activeDrives = activeDrives;
        this.totalRegistrations = totalRegistrations;
        this.shortlistedCount = shortlistedCount;
        this.interviewCount = interviewCount;
        this.selectedCount = selectedCount;
        this.rejectedCount = rejectedCount;
        this.recentActivity = recentActivity != null ? new ArrayList<>(recentActivity) : new ArrayList<>();
    }

    public long getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(long totalStudents) {
        this.totalStudents = totalStudents;
    }

    public long getTotalDrives() {
        return totalDrives;
    }

    public void setTotalDrives(long totalDrives) {
        this.totalDrives = totalDrives;
    }

    public long getActiveDrives() {
        return activeDrives;
    }

    public void setActiveDrives(long activeDrives) {
        this.activeDrives = activeDrives;
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

    public List<StatusHistory> getRecentActivity() {
        if (recentActivity == null) {
            recentActivity = new ArrayList<>();
        }
        return recentActivity;
    }

    public void setRecentActivity(List<StatusHistory> recentActivity) {
        this.recentActivity = recentActivity != null ? new ArrayList<>(recentActivity) : new ArrayList<>();
    }
}
