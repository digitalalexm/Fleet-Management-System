package com.fms.penalty.model;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Heartbeat consumed from fms.car.heartbeats.
 * assetType is an enum — Jackson handles String↔enum via @JsonCreator/@JsonValue.
 */
public record HeartbeatEvent(
        UUID      heartbeatId,
        UUID      tripId,
        UUID      assetId,
        UUID      operatorId,
        AssetType assetType,
        double    latitude,
        double    longitude,
        double    speedKmh,
        double    heading,
        int       engineRpm,
        double    fuelLevel,
        OffsetDateTime timestamp) {}
