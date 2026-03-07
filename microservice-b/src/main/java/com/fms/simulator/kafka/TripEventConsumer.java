package com.fms.simulator.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fms.simulator.model.AssetType;
import com.fms.simulator.model.SimulationSession;
import com.fms.simulator.service.SessionRegistry;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;
import java.util.UUID;

@ApplicationScoped
public class TripEventConsumer {

    private static final Logger LOG = Logger.getLogger(TripEventConsumer.class);

    @Inject SessionRegistry registry;
    @Inject ObjectMapper    objectMapper;

    @ConfigProperty(name = "simulator.city.lat.min", defaultValue = "37.95") double latMin;
    @ConfigProperty(name = "simulator.city.lat.max", defaultValue = "38.05") double latMax;
    @ConfigProperty(name = "simulator.city.lon.min", defaultValue = "23.70") double lonMin;
    @ConfigProperty(name = "simulator.city.lon.max", defaultValue = "23.80") double lonMax;

    @Incoming("trip-events")
    public void onTripEvent(String message) {
        try {
            JsonNode e         = objectMapper.readTree(message);
            String   eventType = e.get("eventType").asText();
            UUID     tripId    = UUID.fromString(e.get("tripId").asText());

            switch (eventType) {
                case "TRIP_STARTED" -> {
                    UUID      assetId    = UUID.fromString(e.get("assetId").asText());
                    UUID      operatorId = UUID.fromString(e.get("operatorId").asText());
                    // Deserialise assetType String → enum
                    AssetType assetType  = AssetType.fromJson(
                            e.has("assetType") ? e.get("assetType").asText() : null);
                    double lat = latMin + Math.random() * (latMax - latMin);
                    double lon = lonMin + Math.random() * (lonMax - lonMin);
                    registry.register(SimulationSession.start(tripId, assetId, operatorId, assetType, lat, lon));
                }
                case "TRIP_COMPLETED", "TRIP_CANCELLED" -> registry.deactivate(tripId);
                default -> LOG.debugf("Ignored event: %s", eventType);
            }
        } catch (Exception ex) {
            LOG.errorf(ex, "Error processing trip event: %s", message);
        }
    }
}
