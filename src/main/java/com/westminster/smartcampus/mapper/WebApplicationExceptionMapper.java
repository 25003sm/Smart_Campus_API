package com.westminster.smartcampus.mapper;

import com.westminster.smartcampus.util.ErrorResponse;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {
    @Context
    private ContainerRequestContext requestContext;

    @Override
    public Response toResponse(WebApplicationException exception) {
        int status = exception.getResponse() != null ? exception.getResponse().getStatus() : 500;
        String error;
        switch (status) {
            case 404:
                error = "NotFound";
                break;
            case 405:
                error = "MethodNotAllowed";
                break;
            case 415:
                error = "UnsupportedMediaType";
                break;
            case 400:
                error = "BadRequest";
                break;
            default:
                error = "RequestError";
                break;
        }
        String message = exception.getMessage() == null || exception.getMessage().isBlank()
                ? "The request could not be processed."
                : exception.getMessage();
        return Response.status(status)
                .entity(new ErrorResponse(error, message, status, requestContext.getUriInfo().getPath()))
                .build();
    }
}
