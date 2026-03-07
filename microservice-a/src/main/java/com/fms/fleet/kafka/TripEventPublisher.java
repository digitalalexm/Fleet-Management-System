package com.fms.fleet.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fms.fleet.model.entity.Asset;
import com.fms.fleet.model.entity.Trip;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class TripEventPublisher {

    private static final Logger LOG = Logger.getLogger(TripEventPublisher.class);
    @Inject @Channel("trip-events") Emitter<String> emitter;
    @Inject ObjectMapper objectMapper;

    public void publish(Trip trip, Asset asset, String eventType) {
        try {
            Map<String,Object> event = new HashMap<>();
            event.put("eventType",       eventType);
            event.put("tripId",          trip.id().toString());
            event.put("assetId",         trip.assetId().toString());
            event.put("assetType",       asset.assetType().name());
            event.put("operatorId",      trip.operatorId().toString());
            event.put("originName",      trip.originName());
            event.put("destinationName", trip.destinationName());
            event.put("status",          trip.status());
            event.put("timestamp",       OffsetDateTime.now().toString());
            emitter.send(objectMapper.writeValueAsString(event));
            LOG.infof("Published [%s] trip=%s assetType=%s", eventType, trip.id(), asset.assetType());
        } catch (Exception e) {
            LOG.errorf(e, "Failed to publish [%s] trip=%s", eventType, trip.id());
        }
    }
}
