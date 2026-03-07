package com.fms.penalty.strategy;

import com.fms.penalty.model.AssetType;

/** Fallback for unknown asset types — applies car rules so nothing is silently ignored. */
@jakarta.enterprise.context.ApplicationScoped
public class DefaultPenaltyStrategy extends AbstractPenaltyStrategy {

    public DefaultPenaltyStrategy() {
        addThreshold(80.0, 5, "Severe speeding (default)");
        addThreshold(60.0, 2, "Speeding (default)");
    }

    @Override public AssetType assetType() { return AssetType.CAR; }
}
