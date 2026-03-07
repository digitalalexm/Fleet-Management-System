package com.fms.penalty.strategy;

import com.fms.penalty.model.AssetType;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class TruckPenaltyStrategy extends AbstractPenaltyStrategy {

    @ConfigProperty(name = "penalty.truck.severe.speed",  defaultValue = "70.0") double severeSpeed;
    @ConfigProperty(name = "penalty.truck.severe.points", defaultValue = "8")    int    severePoints;
    @ConfigProperty(name = "penalty.truck.mild.speed",    defaultValue = "50.0") double mildSpeed;
    @ConfigProperty(name = "penalty.truck.mild.points",   defaultValue = "3")    int    mildPoints;

    @PostConstruct
    void init() {
        addThreshold(severeSpeed, severePoints, "Severe speeding");
        addThreshold(mildSpeed,   mildPoints,   "Speeding");
    }

    @Override public AssetType assetType() { return AssetType.TRUCK; }
}
