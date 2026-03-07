package com.fms.fleet.resource;
import com.fms.fleet.model.dto.Dtos.*;
import com.fms.fleet.service.TripService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.*;
@Path("/api/v1/trips") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
public class TripResource {
    @Inject TripService service;
    @GET public List<TripResponse> listAll() { return service.getAll(); }
    @GET @Path("/asset/{assetId}") public List<TripResponse> byAsset(@PathParam("assetId") UUID assetId) { return service.getByAsset(assetId); }
    @GET @Path("/{id}") public TripResponse getById(@PathParam("id") UUID id) { return service.getById(id); }
    @POST public Response schedule(@Valid TripRequest req) { return Response.status(201).entity(service.schedule(req)).build(); }
    @POST @Path("/{id}/start") public TripResponse start(@PathParam("id") UUID id) { return service.start(id); }
    @POST @Path("/{id}/complete") public TripResponse complete(@PathParam("id") UUID id) { return service.complete(id); }
    @POST @Path("/{id}/cancel") public TripResponse cancel(@PathParam("id") UUID id) { return service.cancel(id); }
}
