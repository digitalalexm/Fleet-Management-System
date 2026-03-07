package com.fms.fleet.resource;
import com.fms.fleet.model.dto.Dtos.*;
import com.fms.fleet.service.OperatorService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.*;
@Path("/api/v1/operators") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
public class OperatorResource {
    @Inject OperatorService service;
    @GET public List<OperatorResponse> listAll() { return service.getAll(); }
    @GET @Path("/available") public List<OperatorResponse> available() { return service.getAvailable(); }
    @GET @Path("/{id}") public OperatorResponse getById(@PathParam("id") UUID id) { return service.getById(id); }
    @POST public Response create(@Valid OperatorRequest req) { return Response.status(201).entity(service.create(req)).build(); }
    @PUT @Path("/{id}") public OperatorResponse update(@PathParam("id") UUID id, @Valid OperatorRequest req) { return service.update(id,req); }
    @DELETE @Path("/{id}") public Response delete(@PathParam("id") UUID id) { service.delete(id); return Response.noContent().build(); }
}
