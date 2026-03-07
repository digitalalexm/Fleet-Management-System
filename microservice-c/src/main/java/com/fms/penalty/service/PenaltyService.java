package com.fms.penalty.service;

import com.fms.penalty.model.*;
import com.fms.penalty.repository.PenaltyStore;
import com.fms.penalty.strategy.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class PenaltyService {

    private static final Logger LOG = Logger.getLogger(PenaltyService.class);

    @Inject PenaltyStrategyRegistry registry;
    @Inject PenaltyStore            store;

    public Optional<PenaltyEvent> evaluate(HeartbeatEvent event) {
        // registry.resolve() accepts AssetType enum directly
        PenaltyStrategy strategy = registry.resolve(event.assetType());
        int    points = strategy.calculate(event);
        if (points <= 0) return Optional.empty();

        long   total  = store.addPoints(event.operatorId(), points);
        String reason = strategy.describe(event.speedKmh(), points);

        LOG.warnf("PENALTY assetType=%s driver=%s speed=%.1f km/h +%d pts (total=%d)",
                event.assetType().name(), event.operatorId(), event.speedKmh(), points, total);

        return Optional.of(new PenaltyEvent(
                event.operatorId(), event.tripId(), event.heartbeatId(),
                event.assetType(),  // enum preserved in the event record
                event.speedKmh(), points, total, reason, OffsetDateTime.now()));
    }

    public long getPoints(UUID operatorId)  { return store.getPoints(operatorId); }
    public void resetPoints(UUID operatorId){ store.resetPoints(operatorId); }
}
