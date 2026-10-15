package com.placement.exception;

import java.util.ArrayList;
import java.util.List;

public class IneligibleStudentException extends RuntimeException {
    private final List<String> reasons;

    public IneligibleStudentException(String message, List<String> reasons) {
        super(message);
        this.reasons = reasons != null ? new ArrayList<>(reasons) : new ArrayList<>();
    }

    public List<String> getReasons() {
        return reasons;
    }
}
