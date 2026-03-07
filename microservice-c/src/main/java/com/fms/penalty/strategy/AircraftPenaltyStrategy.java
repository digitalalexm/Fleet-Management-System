package com.fms.penalty.strategy;

import com.fms.penalty.model.AssetType;
import com.fms.penalty.model.HeartbeatEvent;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/** Aircraft thresholds are in knots; heartbeat carries km/h and is converted internally. */
@ApplicationScoped
public class AircraftPenaltyStrategy extends AbstractPenaltyStrategy {

    private static final double KMH_TO_KNOTS = 1.0 / 1.852;

    @ConfigProperty(name = "penalty.aircraft.severe.knots",  defaultValue = "250.0") double severeKnots;
    @ConfigProperty(name = "penalty.aircraft.severe.points", defaultValue = "10")    int    severePoints;
    @ConfigProperty(name = "penalty.aircraft.mild.knots",    defaultValue = "200.0") double mildKnots;
    @ConfigProperty(name = "penalty.aircraft.mild.points",   defaultValue = "4")     int    mildPoints;

    @PostConstruct
    void init() {
        addThreshold(severeKnots, severePoints, "Exceeds cruise limit");
        addThreshold(mildKnots,   mildPoints,   "Above advisory speed");
    }

    @Override public AssetType assetType() { return AssetType.AIRCRAFT; }

    @Override
    public int calculate(HeartbeatEvent event) {
        return calculateFromSpeed(event.speedKmh() * KMH_TO_KNOTS);
    }

    @Override
    public String describe(double speedKmh, int points) {
        return super.describe(speedKmh * KMH_TO_KNOTS, points);
    }
}
