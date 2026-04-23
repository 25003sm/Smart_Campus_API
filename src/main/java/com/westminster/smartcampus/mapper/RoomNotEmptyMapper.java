package com.westminster.smartcampus.mapper;

import com.westminster.smartcampus.exception.RoomNotEmptyException;
import com.westminster.smartcampus.util.ErrorResponse;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.container.ContainerRequestContext;

@Provider
public class RoomNotEmptyMapper implements ExceptionMapper<RoomNotEmptyException> {
    @Context
    private ContainerRequestContext requestContext;

    @Override
    public Response toResponse(RoomNotEmptyException exception) {
        return Response.status(Response.Status.CONFLICT)
                .entity(new ErrorResponse("RoomNotEmpty", exception.getMessage(), 409, requestContext.getUriInfo().getPath()))
                .build();
    }
}
