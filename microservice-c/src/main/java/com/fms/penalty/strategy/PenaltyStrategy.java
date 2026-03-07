package com.fms.penalty.strategy;

import com.fms.penalty.model.AssetType;
import com.fms.penalty.model.HeartbeatEvent;

/**
 * Strategy for calculating penalty points per asset type.
 * Add a new @ApplicationScoped implementation to support a new asset type —
 * PenaltyStrategyRegistry discovers it automatically via CDI.
 */
public interface PenaltyStrategy {

    /** The asset type this strategy handles. */
    AssetType assetType();

    /** Points to add for this heartbeat (0 = no infraction). */
    int calculate(HeartbeatEvent event);

    /** Human-readable description of the infraction. */
    String describe(double speedKmh, int points);
}
