package com.fms.penalty.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fms.penalty.model.HeartbeatEvent;
import com.fms.penalty.service.PenaltyService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

@ApplicationScoped
public class HeartbeatConsumer {

    private static final Logger LOG = Logger.getLogger(HeartbeatConsumer.class);
    @Inject PenaltyService penaltyService;
    @Inject ObjectMapper objectMapper;

    @Incoming("car-heartbeats")
    public void onHeartbeat(String message) {
        try {
            HeartbeatEvent event = objectMapper.readValue(message, HeartbeatEvent.class);
            penaltyService.evaluate(event);
        } catch (Exception e) { LOG.errorf(e,"Failed to process heartbeat: %s",message); }
    }
}
