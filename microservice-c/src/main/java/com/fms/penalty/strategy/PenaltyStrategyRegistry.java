package com.fms.penalty.strategy;

import com.fms.penalty.model.AssetType;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.StreamSupport;

/**
 * Auto-discovers all PenaltyStrategy CDI beans and maps them by AssetType enum.
 * To add support for a new asset type: create a new @ApplicationScoped bean
 * implementing PenaltyStrategy — nothing else needs to change (OCP).
 */
@ApplicationScoped
public class PenaltyStrategyRegistry {

    private static final Logger LOG = Logger.getLogger(PenaltyStrategyRegistry.class);

    @Inject Instance<PenaltyStrategy> allStrategies;
    @Inject DefaultPenaltyStrategy    defaultStrategy;

    private final Map<AssetType, PenaltyStrategy> strategyMap = new EnumMap<>(AssetType.class);

    @PostConstruct
    void init() {
        StreamSupport.stream(allStrategies.spliterator(), false)
                .filter(s -> s.assetType() != AssetType.CAR || !(s instanceof DefaultPenaltyStrategy))
                .forEach(s -> strategyMap.put(s.assetType(), s));
        LOG.infof("Loaded penalty strategies: %s", strategyMap.keySet());
    }

    /**
     * Returns the strategy for the given AssetType enum.
     * Falls back to DefaultPenaltyStrategy for null or unmapped types.
     */
    public PenaltyStrategy resolve(AssetType assetType) {
        if (assetType == null) return defaultStrategy;
        return strategyMap.getOrDefault(assetType, defaultStrategy);
    }
}
