package com.fms.fleet.model.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record Asset(
        UUID id,
        AssetType assetType,        // enum internally
        String internalName,
        String manufacturer,
        String modelName,
        String status,
        LocalDate purchaseDate,
        OffsetDateTime createdAt,
        String licensePlate,
        BigDecimal distanceCounter,
        String fuelType,
        String tailNumber,
        BigDecimal totalFlightHours,
        LocalDate nextInspectionDate,
        String shipName,
        String vesselType) {

    public boolean isLandVehicle() {
        return assetType == AssetType.CAR || assetType == AssetType.TRUCK;
    }
}
