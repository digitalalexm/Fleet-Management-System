package com.fms.fleet.model.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

public record Operator(
        UUID id, String operatorType, String firstName, String lastName,
        String employeeId, String status, String contactInfo, OffsetDateTime createdAt) {

    public String fullName() {
        return firstName + " " + lastName;
    }
}
