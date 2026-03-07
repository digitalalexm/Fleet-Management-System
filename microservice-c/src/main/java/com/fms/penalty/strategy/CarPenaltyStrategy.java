package com.fms.penalty.strategy;

import com.fms.penalty.model.AssetType;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class CarPenaltyStrategy extends AbstractPenaltyStrategy {

    @ConfigProperty(name = "penalty.car.severe.speed",  defaultValue = "80.0") double severeSpeed;
    @ConfigProperty(name = "penalty.car.severe.points", defaultValue = "5")    int    severePoints;
    @ConfigProperty(name = "penalty.car.mild.speed",    defaultValue = "60.0") double mildSpeed;
    @ConfigProperty(name = "penalty.car.mild.points",   defaultValue = "2")    int    mildPoints;

    @PostConstruct
    void init() {
        addThreshold(severeSpeed, severePoints, "Severe speeding");
        addThreshold(mildSpeed,   mildPoints,   "Speeding");
    }

    @Override public AssetType assetType() { return AssetType.CAR; }
}
