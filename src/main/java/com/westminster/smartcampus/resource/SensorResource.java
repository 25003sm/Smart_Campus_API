package com.westminster.smartcampus.resource;

import com.westminster.smartcampus.exception.LinkedResourceNotFoundException;
import com.westminster.smartcampus.model.ApiLink;
import com.westminster.smartcampus.model.Room;
import com.westminster.smartcampus.model.Sensor;
import com.westminster.smartcampus.repository.DataStore;
import com.westminster.smartcampus.util.ResponseFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
public class SensorResource {

    @GET
    public Response getSensors(@QueryParam("type") String type, @Context UriInfo uriInfo) {
        List<Map<String, Object>> sensors = DataStore.sensors.values()
                .stream()
                .filter(sensor -> type == null || sensor.getType().equalsIgnoreCase(type))
                .sorted(Comparator.comparing(Sensor::getId))
                .map(sensor -> toSensorRepresentation(sensor, uriInfo))
                .collect(Collectors.toList());

        List<ApiLink> links = List.of(
                new ApiLink("self", uriInfo.getRequestUri().toString(), "GET"),
                new ApiLink("create-sensor", uriInfo.getAbsolutePath().toString(), "POST")
        );

        String message = type == null ? "All sensors retrieved successfully." : "Sensors filtered by type retrieved successfully.";
        return ResponseFactory.ok(message, uriInfo.getPath(), sensors, links);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createSensor(Sensor sensor, @Context UriInfo uriInfo) {
        validateSensor(sensor);

        if (DataStore.sensors.containsKey(sensor.getId())) {
            return ResponseFactory.error(409, "SensorAlreadyExists", "A sensor with the same ID already exists.", uriInfo.getPath());
        }

        Room room = DataStore.rooms.get(sensor.getRoomId());
        if (room == null) {
            throw new LinkedResourceNotFoundException("The specified roomId does not exist, so the sensor cannot be linked.");
        }

        DataStore.sensors.put(sensor.getId(), sensor);
        room.getSensorIds().add(sensor.getId());
        DataStore.readings.putIfAbsent(sensor.getId(), new java.util.concurrent.CopyOnWriteArrayList<>());

        URI location = uriInfo.getAbsolutePathBuilder().path(sensor.getId()).build();
        List<ApiLink> links = List.of(
                new ApiLink("self", location.toString(), "GET"),
                new ApiLink("readings", location.toString() + "/readings", "GET"),
                new ApiLink("all-sensors", uriInfo.getAbsolutePath().toString(), "GET")
        );

        return Response.created(location)
                .entity(new com.westminster.smartcampus.util.ApiResponse<>("success",
                        "Sensor created successfully.",
                        uriInfo.getPath(),
                        toSensorRepresentation(sensor, uriInfo),
                        links))
                .build();
    }

    @GET
    @Path("/{sensorId}")
    public Response getSensorById(@PathParam("sensorId") String sensorId, @Context UriInfo uriInfo) {
        Sensor sensor = DataStore.sensors.get(sensorId);
        if (sensor == null) {
            return ResponseFactory.error(404, "SensorNotFound", "The requested sensor does not exist.", uriInfo.getPath());
        }

        List<ApiLink> links = List.of(
                new ApiLink("self", uriInfo.getRequestUri().toString(), "GET"),
                new ApiLink("readings", uriInfo.getRequestUri().toString() + "/readings", "GET"),
                new ApiLink("all-sensors", uriInfo.getBaseUriBuilder().path("sensors").build().toString(), "GET")
        );

        return ResponseFactory.ok("Sensor retrieved successfully.", uriInfo.getPath(), toSensorRepresentation(sensor, uriInfo), links);
    }

    @Path("/{sensorId}/readings")
    public SensorReadingResource sensorReadingResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }

    private void validateSensor(Sensor sensor) {
        if (sensor == null || isBlank(sensor.getId()) || isBlank(sensor.getType()) || isBlank(sensor.getStatus()) || isBlank(sensor.getRoomId())) {
            throw new BadRequestException("Sensor id, type, status, and roomId are required.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private Map<String, Object> toSensorRepresentation(Sensor sensor, UriInfo uriInfo) {
        Map<String, Object> sensorData = new LinkedHashMap<>();
        sensorData.put("id", sensor.getId());
        sensorData.put("type", sensor.getType());
        sensorData.put("status", sensor.getStatus());
        sensorData.put("currentValue", sensor.getCurrentValue());
        sensorData.put("roomId", sensor.getRoomId());
        sensorData.put("links", List.of(
                Map.of("rel", "self", "href", uriInfo.getBaseUriBuilder().path("sensors").path(sensor.getId()).build().toString(), "method", "GET"),
                Map.of("rel", "readings", "href", uriInfo.getBaseUriBuilder().path("sensors").path(sensor.getId()).path("readings").build().toString(), "method", "GET"),
                Map.of("rel", "room", "href", uriInfo.getBaseUriBuilder().path("rooms").path(sensor.getRoomId()).build().toString(), "method", "GET")
        ));
        return sensorData;
    }
}
