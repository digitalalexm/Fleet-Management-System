package com.fms.penalty.repository;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import java.util.UUID;
/** Redis store for driver penalty points. Key: fms:penalty:driver:<operatorId> */
@ApplicationScoped
public class PenaltyStore {

    private static final Logger LOG = Logger.getLogger(PenaltyStore.class);
    private static final String PREFIX = "fms:penalty:driver:";
    @Inject RedisDataSource redis;
    private ValueCommands<String,Long> cmds() { return redis.value(Long.class); }

    public long addPoints(UUID operatorId, int points) {
        Long total = cmds().incrby(PREFIX+operatorId, points);
        LOG.debugf("Driver %s: +%d pts => total %d", operatorId, points, total);
        return total != null ? total : points;
    }

    public long getPoints(UUID operatorId) {
        try { Long v=cmds().get(PREFIX+operatorId); return v!=null?v:0L; } catch(Exception e) { return 0L; }
    }

    public void resetPoints(UUID operatorId) {
        cmds().set(PREFIX+operatorId, 0L);
        LOG.infof("Points reset for driver %s", operatorId);
    }
}
