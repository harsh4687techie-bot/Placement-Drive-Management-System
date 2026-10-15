package com.placement.dto;

import com.placement.model.PlacementStatus;
import jakarta.validation.constraints.NotNull;

public class StatusUpdateRequest {
    @NotNull(message = "New status is required")
    private PlacementStatus status;

    private String remarks;

    public StatusUpdateRequest() {
    }

    public StatusUpdateRequest(PlacementStatus status, String remarks) {
        this.status = status;
        this.remarks = remarks;
    }

    public PlacementStatus getStatus() {
        return status;
    }

    public void setStatus(PlacementStatus status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
