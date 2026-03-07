package com.fms.penalty.strategy;

import com.fms.penalty.model.AssetType;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class ShipPenaltyStrategy extends AbstractPenaltyStrategy {

    @ConfigProperty(name = "penalty.ship.severe.speed",  defaultValue = "30.0") double severeSpeed;
    @ConfigProperty(name = "penalty.ship.severe.points", defaultValue = "6")    int    severePoints;
    @ConfigProperty(name = "penalty.ship.mild.speed",    defaultValue = "20.0") double mildSpeed;
    @ConfigProperty(name = "penalty.ship.mild.points",   defaultValue = "2")    int    mildPoints;

    @PostConstruct
    void init() {
        addThreshold(severeSpeed, severePoints, "Dangerous harbour speed");
        addThreshold(mildSpeed,   mildPoints,   "Above harbour limit");
    }

    @Override public AssetType assetType() { return AssetType.SHIP; }
}
