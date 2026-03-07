package com.fms.simulator.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fms.simulator.model.Heartbeat;
import com.fms.simulator.service.*;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

@ApplicationScoped
public class HeartbeatPublisher {

    private static final Logger LOG = Logger.getLogger(HeartbeatPublisher.class);
    @Inject SessionRegistry registry;
    @Inject SimulationEngine engine;
    @Inject ObjectMapper objectMapper;
    @Inject @Channel("car-heartbeats") Emitter<String> emitter;

    @Scheduled(every="5s",identity="heartbeat-tick")
    public void tick() {
        if (registry.count()==0) return;
        registry.getAll().forEach(session -> {
            try {
                Heartbeat hb=engine.nextHeartbeat(session);
                registry.update(engine.advance(session,hb));
                emitter.send(objectMapper.writeValueAsString(hb));
                LOG.debugf("HB: trip=%s type=%s speed=%.1f",hb.tripId(),hb.assetType(),hb.speedKmh());
            } catch (Exception e) { LOG.errorf(e,"HB error trip=%s",session.tripId()); }
        });
    }
}
