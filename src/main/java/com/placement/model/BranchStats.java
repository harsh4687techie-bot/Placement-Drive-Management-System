package com.placement.model;

public class BranchStats {
    private String branch;
    private long totalStudents;
    private long registeredStudents;
    private long selectedStudents;
    private double placementPercentage;
    private double averagePackage;
    private double highestPackage;

    public BranchStats() {
    }

    public BranchStats(String branch, long totalStudents, long registeredStudents,
                       long selectedStudents, double placementPercentage,
                       double averagePackage, double highestPackage) {
        this.branch = branch;
        this.totalStudents = totalStudents;
        this.registeredStudents = registeredStudents;
        this.selectedStudents = selectedStudents;
        this.placementPercentage = placementPercentage;
        this.averagePackage = averagePackage;
        this.highestPackage = highestPackage;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public long getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(long totalStudents) {
        this.totalStudents = totalStudents;
    }

    public long getRegisteredStudents() {
        return registeredStudents;
    }

    public void setRegisteredStudents(long registeredStudents) {
        this.registeredStudents = registeredStudents;
    }

    public long getSelectedStudents() {
        return selectedStudents;
    }

    public void setSelectedStudents(long selectedStudents) {
        this.selectedStudents = selectedStudents;
    }

    public double getPlacementPercentage() {
        return placementPercentage;
    }

    public void setPlacementPercentage(double placementPercentage) {
        this.placementPercentage = placementPercentage;
    }

    public double getAveragePackage() {
        return averagePackage;
    }

    public void setAveragePackage(double averagePackage) {
        this.averagePackage = averagePackage;
    }

    public double getHighestPackage() {
        return highestPackage;
    }

    public void setHighestPackage(double highestPackage) {
        this.highestPackage = highestPackage;
    }
}
