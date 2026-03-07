package com.fms.simulator.service;

import com.fms.simulator.model.*;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import java.time.OffsetDateTime;
import java.util.Random;
import java.util.UUID;

@ApplicationScoped
public class SimulationEngine {

    private static final Random RNG = new Random();

    @ConfigProperty(name = "simulator.city.lat.min", defaultValue = "37.95") double latMin;
    @ConfigProperty(name = "simulator.city.lat.max", defaultValue = "38.05") double latMax;
    @ConfigProperty(name = "simulator.city.lon.min", defaultValue = "23.70") double lonMin;
    @ConfigProperty(name = "simulator.city.lon.max", defaultValue = "23.80") double lonMax;

    public Heartbeat nextHeartbeat(SimulationSession session) {
        double speed   = generateSpeed(session.assetType());
        double heading = RNG.nextDouble() * 360.0;
        double newLat  = clamp(session.currentLat() + Math.cos(Math.toRadians(heading)) * speed * 0.000001, latMin, latMax);
        double newLon  = clamp(session.currentLon() + Math.sin(Math.toRadians(heading)) * speed * 0.000001, lonMin, lonMax);
        return new Heartbeat(UUID.randomUUID(), session.tripId(), session.assetId(), session.operatorId(),
                session.assetType(),            // enum — Jackson writes "CAR" etc. to Kafka JSON
                newLat, newLon, speed, heading,
                (int)(800 + speed * 40 + RNG.nextInt(200)),
                Math.max(0, 100.0 - RNG.nextDouble() * 0.1),
                OffsetDateTime.now());
    }

    public SimulationSession advance(SimulationSession session, Heartbeat hb) {
        return session.withPosition(hb.latitude(), hb.longitude());
    }

    /** Package-private for unit tests. */
    double generateSpeed(AssetType assetType) {
        if (assetType == null) return generateCarSpeed();
        return switch (assetType) {
            case CAR      -> generateCarSpeed();
            case TRUCK    -> generateTruckSpeed();
            case AIRCRAFT -> generateAircraftSpeed();
            case SHIP     -> generateShipSpeed();
        };
    }

    // CAR: 70% city (10-60), 20% mild over (60-80), 10% severe (80-120)
    private double generateCarSpeed() {
        double r = RNG.nextDouble();
        if (r < 0.70) return 10 + RNG.nextDouble() * 50;
        if (r < 0.90) return 60 + RNG.nextDouble() * 20;
        return 80 + RNG.nextDouble() * 40;
    }
    // TRUCK: 60% legal (20-50), 25% mild over (50-70), 15% severe (70-90)
    private double generateTruckSpeed() {
        double r = RNG.nextDouble();
        if (r < 0.60) return 20 + RNG.nextDouble() * 30;
        if (r < 0.85) return 50 + RNG.nextDouble() * 20;
        return 70 + RNG.nextDouble() * 20;
    }
    // AIRCRAFT: cruise 180-250 knots stored as km/h equivalent
    private double generateAircraftSpeed() {
        double r = RNG.nextDouble();
        if (r < 0.80) return (180 + RNG.nextDouble() * 70) * 1.852;
        return (250 + RNG.nextDouble() * 50) * 1.852;
    }
    // SHIP: 75% harbour (10-20), 15% fast (20-30), 10% dangerous (30-40)
    private double generateShipSpeed() {
        double r = RNG.nextDouble();
        if (r < 0.75) return 10 + RNG.nextDouble() * 10;
        if (r < 0.90) return 20 + RNG.nextDouble() * 10;
        return 30 + RNG.nextDouble() * 10;
    }

    private double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}
