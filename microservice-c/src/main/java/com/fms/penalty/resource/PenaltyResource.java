package com.fms.penalty.resource;
import com.fms.penalty.service.PenaltyService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.Map;
import java.util.UUID;

@Path("/api/v1/penalties") @Produces(MediaType.APPLICATION_JSON)
public class PenaltyResource {
    @Inject PenaltyService service;

    @GET @Path("/driver/{operatorId}")
    public Map<String,Object> getPoints(@PathParam("operatorId") UUID operatorId) {
        long pts = service.getPoints(operatorId);
        return Map.of("operatorId",operatorId.toString(),"totalPenaltyPoints",pts,"riskLevel",risk(pts));
    }

    @DELETE @Path("/driver/{operatorId}/reset")
    public Response reset(@PathParam("operatorId") UUID operatorId) {
        service.resetPoints(operatorId);
        return Response.ok(Map.of("message","Points reset","operatorId",operatorId)).build();
    }

    private String risk(long pts) {
        if (pts<10) return "LOW"; if (pts<30) return "MEDIUM"; if (pts<50) return "HIGH"; return "CRITICAL";
    }
}
