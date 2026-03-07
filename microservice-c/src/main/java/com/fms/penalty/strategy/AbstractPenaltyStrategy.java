package com.fms.penalty.strategy;

import com.fms.penalty.model.HeartbeatEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class with configurable threshold arithmetic.
 * Subclasses register thresholds via addThreshold() in @PostConstruct.
 * The highest matching threshold wins.
 */
public abstract class AbstractPenaltyStrategy implements PenaltyStrategy {

    /** 5-second heartbeat interval in hours. */
    protected static final double INTERVAL_HOURS = 5.0 / 3600.0;

    protected record Threshold(double limitKmh, int pointsPerKm, String label) {}

    private final List<Threshold> thresholds = new ArrayList<>();

    protected void addThreshold(double limitKmh, int pointsPerKm, String label) {
        thresholds.add(new Threshold(limitKmh, pointsPerKm, label));
        thresholds.sort((a, b) -> Double.compare(b.limitKmh(), a.limitKmh()));
    }

    @Override
    public int calculate(HeartbeatEvent event) {
        return calculateFromSpeed(event.speedKmh());
    }

    protected int calculateFromSpeed(double speedKmh) {
        for (Threshold t : thresholds) {
            if (speedKmh > t.limitKmh()) {
                double excessKm = (speedKmh - t.limitKmh()) * INTERVAL_HOURS;
                return (int) Math.ceil(excessKm * t.pointsPerKm());
            }
        }
        return 0;
    }

    @Override
    public String describe(double speedKmh, int points) {
        for (Threshold t : thresholds) {
            if (speedKmh > t.limitKmh()) {
                return String.format("%s [%s]: %.1f km/h (limit %.0f). +%d pts",
                        t.label(), assetType().name(), speedKmh, t.limitKmh(), points);
            }
        }
        return String.format("No infraction [%s]: %.1f km/h", assetType().name(), speedKmh);
    }
}
