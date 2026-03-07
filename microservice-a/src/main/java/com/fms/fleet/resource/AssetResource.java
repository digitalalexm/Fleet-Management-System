package com.fms.fleet.resource;
import com.fms.fleet.model.dto.Dtos.*;
import com.fms.fleet.service.AssetService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.*;
@Path("/api/v1/assets") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
public class AssetResource {
    @Inject AssetService service;
    @GET public List<AssetResponse> listAll() { return service.getAll(); }
    @GET @Path("/land-vehicles") public List<AssetResponse> landVehicles() { return service.getLandVehicles(); }
    @GET @Path("/{id}") public AssetResponse getById(@PathParam("id") UUID id) { return service.getById(id); }
    @POST public Response create(@Valid AssetRequest req) { return Response.status(201).entity(service.create(req)).build(); }
    @PUT @Path("/{id}") public AssetResponse update(@PathParam("id") UUID id, @Valid AssetRequest req) { return service.update(id,req); }
    @DELETE @Path("/{id}") public Response delete(@PathParam("id") UUID id) { service.delete(id); return Response.noContent().build(); }
}
