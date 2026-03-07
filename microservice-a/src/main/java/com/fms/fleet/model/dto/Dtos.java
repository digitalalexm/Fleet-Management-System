package com.fms.fleet.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public final class Dtos {

    private Dtos() {}

    /**
     * Asset request — assetType arrives as a plain String from the HTTP client.
     * It is validated and converted to AssetType enum inside DtoMapper.
     */
    public record AssetRequest(
            @NotBlank String assetType,
            @NotBlank @Size(max = 100) String internalName,
            String manufacturer, String modelName, String status, LocalDate purchaseDate,
            String licensePlate, BigDecimal distanceCounter, String fuelType,
            String tailNumber, BigDecimal totalFlightHours, LocalDate nextInspectionDate,
            String shipName, String vesselType) {}

    /**
     * Asset response — assetType is serialised to String via AssetType.toJson() (@JsonValue).
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record AssetResponse(
            UUID id,
            String assetType,           // String in JSON: "CAR", "TRUCK", etc.
            String internalName, String manufacturer, String modelName,
            String status, LocalDate purchaseDate, OffsetDateTime createdAt,
            String licensePlate, BigDecimal distanceCounter, String fuelType,
            String tailNumber, BigDecimal totalFlightHours, LocalDate nextInspectionDate,
            String shipName, String vesselType) {}

    public record OperatorRequest(
            @NotBlank String operatorType,
            @NotBlank @Size(max = 100) String firstName,
            @NotBlank @Size(max = 100) String lastName,
            @NotBlank @Size(max = 50)  String employeeId,
            String status, String contactInfo) {}

    public record OperatorResponse(
            UUID id, String operatorType, String firstName, String lastName,
            String employeeId, String status, String contactInfo, OffsetDateTime createdAt) {}

    public record TripRequest(
            @NotNull UUID assetId, @NotNull UUID operatorId, @NotNull UUID contractId,
            OffsetDateTime scheduledStart, OffsetDateTime scheduledEnd,
            @NotBlank String originName, @NotBlank String destinationName) {}

    public record TripResponse(
            UUID id, UUID assetId, UUID operatorId, UUID contractId,
            OffsetDateTime scheduledStart, OffsetDateTime scheduledEnd,
            OffsetDateTime actualStart, OffsetDateTime actualEnd,
            String originName, String destinationName, String status,
            BigDecimal distanceCoveredKm, BigDecimal fuelConsumedLiters, OffsetDateTime createdAt) {}

    public record AssignmentRequest(
            @NotNull UUID assetId, @NotNull UUID operatorId, String notes) {}

    public record AssignmentResponse(
            UUID id, UUID assetId, UUID operatorId,
            OffsetDateTime assignedAt, OffsetDateTime releasedAt, String notes) {}

    public record ErrorResponse(
            int status, String error, String message, OffsetDateTime timestamp) {}
}
