package com.westminster.smartcampus.resource;

import com.westminster.smartcampus.exception.RoomNotEmptyException;
import com.westminster.smartcampus.model.ApiLink;
import com.westminster.smartcampus.model.Room;
import com.westminster.smartcampus.repository.DataStore;
import com.westminster.smartcampus.util.ResponseFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
public class RoomResource {

    @GET
    public Response getAllRooms(@Context UriInfo uriInfo) {
        List<Map<String, Object>> rooms = DataStore.rooms.values()
                .stream()
                .sorted(Comparator.comparing(Room::getId))
                .map(room -> toRoomRepresentation(room, uriInfo))
                .collect(Collectors.toList());

        List<ApiLink> links = List.of(
                new ApiLink("self", uriInfo.getRequestUri().toString(), "GET"),
                new ApiLink("create-room", uriInfo.getAbsolutePath().toString(), "POST")
        );

        return ResponseFactory.ok("All rooms retrieved successfully.", uriInfo.getPath(), rooms, links);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createRoom(Room room, @Context UriInfo uriInfo) {
        validateRoom(room);

        if (DataStore.rooms.containsKey(room.getId())) {
            return ResponseFactory.error(409, "RoomAlreadyExists", "A room with the same ID already exists.", uriInfo.getPath());
        }

        if (room.getSensorIds() == null) {
            room.setSensorIds(new ArrayList<>());
        }

        DataStore.rooms.put(room.getId(), room);

        URI location = uriInfo.getAbsolutePathBuilder().path(room.getId()).build();
        List<ApiLink> links = List.of(
                new ApiLink("self", location.toString(), "GET"),
                new ApiLink("all-rooms", uriInfo.getAbsolutePath().toString(), "GET")
        );

        return Response.created(location)
                .entity(new com.westminster.smartcampus.util.ApiResponse<>("success",
                        "Room created successfully.",
                        uriInfo.getPath(),
                        toRoomRepresentation(room, uriInfo),
                        links))
                .build();
    }

    @GET
    @Path("/{roomId}")
    public Response getRoomById(@PathParam("roomId") String roomId, @Context UriInfo uriInfo) {
        Room room = DataStore.rooms.get(roomId);
        if (room == null) {
            return ResponseFactory.error(404, "RoomNotFound", "The requested room does not exist.", uriInfo.getPath());
        }

        List<ApiLink> links = List.of(
                new ApiLink("self", uriInfo.getRequestUri().toString(), "GET"),
                new ApiLink("delete", uriInfo.getRequestUri().toString(), "DELETE"),
                new ApiLink("all-rooms", uriInfo.getBaseUriBuilder().path("rooms").build().toString(), "GET")
        );

        return ResponseFactory.ok("Room retrieved successfully.", uriInfo.getPath(), toRoomRepresentation(room, uriInfo), links);
    }

    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId, @Context UriInfo uriInfo) {
        Room room = DataStore.rooms.get(roomId);
        if (room == null) {
            return ResponseFactory.error(404, "RoomNotFound", "The requested room does not exist.", uriInfo.getPath());
        }

        if (room.getSensorIds() != null && !room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException("The room cannot be deleted because it still has sensors assigned to it.");
        }

        DataStore.rooms.remove(roomId);

        List<ApiLink> links = List.of(
                new ApiLink("all-rooms", uriInfo.getBaseUriBuilder().path("rooms").build().toString(), "GET"),
                new ApiLink("create-room", uriInfo.getBaseUriBuilder().path("rooms").build().toString(), "POST")
        );

        return ResponseFactory.ok("Room deleted successfully.", uriInfo.getPath(), Map.of("deletedRoomId", roomId), links);
    }

    private void validateRoom(Room room) {
        if (room == null || isBlank(room.getId()) || isBlank(room.getName())) {
            throw new BadRequestException("Room id and name are required.");
        }
        if (room.getCapacity() <= 0) {
            throw new BadRequestException("Room capacity must be greater than zero.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private Map<String, Object> toRoomRepresentation(Room room, UriInfo uriInfo) {
        Map<String, Object> roomData = new LinkedHashMap<>();
        roomData.put("id", room.getId());
        roomData.put("name", room.getName());
        roomData.put("capacity", room.getCapacity());
        roomData.put("sensorIds", room.getSensorIds());
        roomData.put("links", List.of(
                Map.of("rel", "self", "href", uriInfo.getBaseUriBuilder().path("rooms").path(room.getId()).build().toString(), "method", "GET"),
                Map.of("rel", "delete", "href", uriInfo.getBaseUriBuilder().path("rooms").path(room.getId()).build().toString(), "method", "DELETE"),
                Map.of("rel", "sensors-filtered-by-roomId-client-side", "href", uriInfo.getBaseUriBuilder().path("sensors").build().toString(), "method", "GET")
        ));
        return roomData;
    }
}
