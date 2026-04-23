package com.westminster.smartcampus.util;

import com.westminster.smartcampus.model.ApiLink;

import javax.ws.rs.core.Response;
import java.util.List;

public final class ResponseFactory {
    private ResponseFactory() {
    }

    public static <T> Response ok(String message, String path, T data, List<ApiLink> links) {
        return Response.ok(new ApiResponse<>("success", message, path, data, links)).build();
    }

    public static <T> Response created(String message, String path, T data, List<ApiLink> links) {
        return Response.status(Response.Status.CREATED)
                .entity(new ApiResponse<>("success", message, path, data, links))
                .build();
    }

    public static Response error(int status, String error, String message, String path) {
        return Response.status(status)
                .entity(new ErrorResponse(error, message, status, path))
                .build();
    }
}
