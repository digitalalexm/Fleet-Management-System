package com.fms.simulator.model;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Telemetry snapshot emitted every 5s per active trip.
 * assetType is an enum; Jackson serialises it to String ("CAR", "TRUCK" …)
 * when the record is written to Kafka — so Microservice C reads a plain String
 * and deserialises back to its own AssetType enum.
 */
public record Heartbeat(
        UUID      heartbeatId,
        UUID      tripId,
        UUID      assetId,
        UUID      operatorId,
        AssetType assetType,        // enum internally, String in JSON
        double    latitude,
        double    longitude,
        double    speedKmh,
        double    heading,
        int       engineRpm,
        double    fuelLevel,
        OffsetDateTime timestamp) {

    /** Convenience: speed in knots for telemetry_logs compatibility (1 knot = 1.852 km/h). */
    public double speedKnots() { return speedKmh / 1.852; }
}
