package com.dentalcloud.dentalcloudbackend.exceptions;

public class ConflictException extends RuntimeException {
    private final String code;

    public ConflictException(String message) {
        this("APPOINTMENT_SLOT_TAKEN", message);
    }

    public ConflictException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
