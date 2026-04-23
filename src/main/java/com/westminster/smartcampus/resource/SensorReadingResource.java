package com.westminster.smartcampus.resource;

import com.westminster.smartcampus.exception.SensorUnavailableException;
import com.westminster.smartcampus.model.ApiLink;
import com.westminster.smartcampus.model.Sensor;
import com.westminster.smartcampus.model.SensorReading;
import com.westminster.smartcampus.repository.DataStore;
import com.westminster.smartcampus.util.ResponseFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Produces(MediaType.APPLICATION_JSON)
public class SensorReadingResource {
    private final String sensorId;

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    @GET
    public Response getReadings(@Context UriInfo uriInfo) {
        Sensor sensor = DataStore.sensors.get(sensorId);
        if (sensor == null) {
            return ResponseFactory.error(404, "SensorNotFound", "The parent sensor does not exist.", uriInfo.getPath());
        }

        List<SensorReading> readingList = DataStore.readings.getOrDefault(sensorId, new CopyOnWriteArrayList<>());

        List<ApiLink> links = List.of(
                new ApiLink("self", uriInfo.getRequestUri().toString(), "GET"),
                new ApiLink("parent-sensor", uriInfo.getBaseUriBuilder().path("sensors").path(sensorId).build().toString(), "GET"),
                new ApiLink("add-reading", uriInfo.getRequestUri().toString(), "POST")
        );

        return ResponseFactory.ok("Sensor readings retrieved successfully.", uriInfo.getPath(), readingList, links);
    }


    @GET
    @Path("/{readingId}")
    public Response getReadingById(@PathParam("readingId") String readingId, @Context UriInfo uriInfo) {
        Sensor sensor = DataStore.sensors.get(sensorId);
        if (sensor == null) {
            return ResponseFactory.error(404, "SensorNotFound", "The parent sensor does not exist.", uriInfo.getPath());
        }

        List<SensorReading> readingList = DataStore.readings.getOrDefault(sensorId, new CopyOnWriteArrayList<>());
        for (SensorReading reading : readingList) {
            if (reading.getId().equals(readingId)) {
                List<ApiLink> links = List.of(
                        new ApiLink("self", uriInfo.getRequestUri().toString(), "GET"),
                        new ApiLink("sensor-readings", uriInfo.getBaseUriBuilder().path("sensors").path(sensorId).path("readings").build().toString(), "GET"),
                        new ApiLink("parent-sensor", uriInfo.getBaseUriBuilder().path("sensors").path(sensorId).build().toString(), "GET")
                );
                return ResponseFactory.ok("Reading retrieved successfully.", uriInfo.getPath(), reading, links);
            }
        }
        return ResponseFactory.error(404, "ReadingNotFound", "The requested reading does not exist for this sensor.", uriInfo.getPath());
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addReading(SensorReading reading, @Context UriInfo uriInfo) {
        Sensor sensor = DataStore.sensors.get(sensorId);
        if (sensor == null) {
            return ResponseFactory.error(404, "SensorNotFound", "The parent sensor does not exist.", uriInfo.getPath());
        }

        if (reading == null) {
            throw new BadRequestException("Reading payload is required.");
        }

        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException("This sensor is under maintenance and cannot accept new readings.");
        }

        if (reading.getId() == null || reading.getId().isBlank()) {
            reading.setId(UUID.randomUUID().toString());
        }
        if (reading.getTimestamp() <= 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        DataStore.readings.computeIfAbsent(sensorId, key -> new CopyOnWriteArrayList<>()).add(reading);
        sensor.setCurrentValue(reading.getValue());

        URI location = uriInfo.getAbsolutePathBuilder().path(reading.getId()).build();
        List<ApiLink> links = List.of(
                new ApiLink("self", location.toString(), "GET"),
                new ApiLink("sensor-readings", uriInfo.getAbsolutePath().toString(), "GET"),
                new ApiLink("parent-sensor", uriInfo.getBaseUriBuilder().path("sensors").path(sensorId).build().toString(), "GET")
        );

        return Response.created(location)
                .entity(new com.westminster.smartcampus.util.ApiResponse<>("success",
                        "Reading created successfully and sensor currentValue updated.",
                        uriInfo.getPath(),
                        reading,
                        links))
                .build();
    }
}
