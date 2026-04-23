package com.westminster.smartcampus.mapper;

import com.westminster.smartcampus.util.ErrorResponse;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {
    @Context
    private ContainerRequestContext requestContext;

    @Override
    public Response toResponse(Throwable exception) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse(
                        "InternalServerError",
                        "An unexpected server error occurred. Please contact the API administrator.",
                        500,
                        requestContext.getUriInfo().getPath()))
                .build();
    }
}
