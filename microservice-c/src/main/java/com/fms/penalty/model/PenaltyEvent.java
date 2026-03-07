package com.fms.penalty.model;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Represents an assessed penalty against a driver. */
public record PenaltyEvent(
        UUID      operatorId,
        UUID      tripId,
        UUID      heartbeatId,
        AssetType assetType,
        double    speedKmh,
        int       pointsAwarded,
        long      totalPoints,
        String    reason,
        OffsetDateTime occurredAt) {}
