package com.fms.simulator.model;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Active simulation session — created on TRIP_STARTED, removed on TRIP_COMPLETED/CANCELLED. */
public record SimulationSession(
        UUID      tripId,
        UUID      assetId,
        UUID      operatorId,
        AssetType assetType,        // enum internally
        double    currentLat,
        double    currentLon,
        OffsetDateTime startedAt) {

    public static SimulationSession start(UUID tripId, UUID assetId, UUID operatorId,
                                          AssetType assetType, double lat, double lon) {
        return new SimulationSession(tripId, assetId, operatorId, assetType, lat, lon, OffsetDateTime.now());
    }

    public SimulationSession withPosition(double lat, double lon) {
        return new SimulationSession(tripId, assetId, operatorId, assetType, lat, lon, startedAt);
    }
}
