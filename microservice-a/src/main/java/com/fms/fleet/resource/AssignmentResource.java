package com.fms.fleet.resource;
import com.fms.fleet.model.dto.Dtos.*;
import com.fms.fleet.service.AssignmentService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.*;
@Path("/api/v1/assignments") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
public class AssignmentResource {
    @Inject AssignmentService service;
    @POST public Response assign(@Valid AssignmentRequest req) { return Response.status(201).entity(service.assign(req)).build(); }
    @GET @Path("/asset/{assetId}") public List<AssignmentResponse> byAsset(@PathParam("assetId") UUID assetId) { return service.getByAsset(assetId); }
    @DELETE @Path("/{id}/release") public Response release(@PathParam("id") UUID id) { service.release(id); return Response.noContent().build(); }
}
