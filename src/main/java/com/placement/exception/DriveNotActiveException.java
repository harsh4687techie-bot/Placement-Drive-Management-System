package com.placement.exception;

public class DriveNotActiveException extends RuntimeException {
    public DriveNotActiveException(String message) {
        super(message);
    }
}
