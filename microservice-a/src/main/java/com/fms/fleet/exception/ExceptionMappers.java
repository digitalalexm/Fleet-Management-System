package com.fms.fleet.exception;

import com.fms.fleet.model.dto.Dtos.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.time.OffsetDateTime;

public final class ExceptionMappers {

    private ExceptionMappers() {}

    @Provider
    public static class NotFoundMapper implements ExceptionMapper<ResourceNotFoundException> {
        public Response toResponse(ResourceNotFoundException e) {
            return Response.status(404).entity(new ErrorResponse(404,"NOT_FOUND",e.getMessage(),OffsetDateTime.now())).build();
        }
    }
    @Provider
    public static class ConflictMapper implements ExceptionMapper<BusinessRuleException> {
        public Response toResponse(BusinessRuleException e) {
            return Response.status(409).entity(new ErrorResponse(409,"CONFLICT",e.getMessage(),OffsetDateTime.now())).build();
        }
    }
    @Provider
    public static class ValidationMapper implements ExceptionMapper<ConstraintViolationException> {
        public Response toResponse(ConstraintViolationException e) {
            String msg = e.getConstraintViolations().stream()
                    .map(cv -> cv.getPropertyPath()+": "+cv.getMessage())
                    .reduce((a,b)->a+"; "+b).orElse("Validation failed");
            return Response.status(400).entity(new ErrorResponse(400,"VALIDATION_ERROR",msg,OffsetDateTime.now())).build();
        }
    }
}
