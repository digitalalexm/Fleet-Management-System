package com.fms.fleet.model.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record Trip(
        UUID id,
        UUID assetId,
        UUID operatorId,
        UUID contractId,
        OffsetDateTime scheduledStart,
        OffsetDateTime scheduledEnd,
        OffsetDateTime actualStart,
        OffsetDateTime actualEnd,
        String originName,
        String destinationName,
        String status,
        BigDecimal distanceCoveredKm,
        BigDecimal fuelConsumedLiters,
        OffsetDateTime createdAt) {}
