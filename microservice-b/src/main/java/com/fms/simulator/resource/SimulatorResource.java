package com.fms.simulator.resource;
import com.fms.simulator.model.SimulationSession;
import com.fms.simulator.service.SessionRegistry;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.*;
@Path("/api/v1/simulator") @Produces(MediaType.APPLICATION_JSON)
public class SimulatorResource {
    @Inject SessionRegistry registry;
    @GET @Path("/sessions") public Collection<SimulationSession> sessions() { return registry.getAll(); }
    @GET @Path("/sessions/count") public Map<String,Integer> count() { return Map.of("activeSessions",registry.count()); }
    @POST @Path("/sessions/{tripId}/stop") public Response stop(@PathParam("tripId") UUID tripId) {
        return registry.deactivate(tripId).map(s->Response.ok(Map.of("stopped",tripId.toString())).build())
                .orElse(Response.status(404).entity(Map.of("error","Session not found")).build());
    }
    @GET @Path("/health") public Map<String,Object> health() { return Map.of("status","UP","activeSessions",registry.count()); }
}
