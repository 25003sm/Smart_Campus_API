package com.westminster.smartcampus.resource;

import com.westminster.smartcampus.model.ApiLink;
import com.westminster.smartcampus.util.ResponseFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Context;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {
    @GET
    public javax.ws.rs.core.Response discover(@Context UriInfo uriInfo) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("name", "Smart Campus Sensor & Room Management API");
        metadata.put("version", "v1");
        metadata.put("contact", "admin@campus.com");

        Map<String, String> resources = new LinkedHashMap<>();
        resources.put("rooms", uriInfo.getBaseUriBuilder().path("rooms").build().toString());
        resources.put("sensors", uriInfo.getBaseUriBuilder().path("sensors").build().toString());
        metadata.put("resources", resources);

        List<ApiLink> links = List.of(
                new ApiLink("rooms", uriInfo.getBaseUriBuilder().path("rooms").build().toString(), "GET"),
                new ApiLink("sensors", uriInfo.getBaseUriBuilder().path("sensors").build().toString(), "GET")
        );

        return ResponseFactory.ok("Discovery document returned successfully.", uriInfo.getPath(), metadata, links);
    }
}
