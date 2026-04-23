# Smart Campus Sensor & Room Management API

This coursework project implements a RESTful API for the **5COSC022W Client-Server Architectures** module using **JAX-RS only**, exactly as required by the specification. The API manages university **Rooms**, **Sensors**, and nested **Sensor Readings** using **in-memory data structures** rather than a database.

## Main Features

- Versioned API base path: `/api/v1`
- Discovery endpoint with metadata and resource links
- Room collection and single-room retrieval
- Room creation and protected room deletion
- Sensor creation with linked room validation
- Sensor retrieval with optional filtering by `type`
- Nested sub-resource for `/sensors/{sensorId}/readings`
- Reading creation updates the parent sensor `currentValue`
- Custom exception mappers for 409, 422, 403, and 500
- Request and response logging using JAX-RS filters
- JSON responses with consistent structure and HATEOAS-style links

## Project Structure

```text
smart-campus-api-90/
├── pom.xml
├── README.md
├── REPORT_ANSWERS.pdf
├── REPORT_ANSWERS.md
├── SmartCampus.postman_collection.json
├── VIDEO_SCRIPT.md
└── src/
    └── main/
        ├── java/com/westminster/smartcampus/
        │   ├── Main.java
        │   ├── config/AppConfig.java
        │   ├── exception/
        │   ├── filter/
        │   ├── mapper/
        │   ├── model/
        │   ├── repository/DataStore.java
        │   ├── resource/
        │   └── util/
        └── resources/
```

## Technology Stack

- Java 17
- Maven
- Jersey (JAX-RS implementation)
- Embedded Grizzly HTTP server
- Jackson JSON provider
- ConcurrentHashMap and CopyOnWriteArrayList for in-memory storage

## Build and Run

### 1. Open the project
Extract the coursework folder and open it in IntelliJ IDEA or VS Code.

### 2. Build the project
```bash
mvn clean package
```

### 3. Run the server
```bash
mvn exec:java
```

### 4. Base URL
```text
http://localhost:8080/api/v1
```

## API Endpoints

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/v1` | Discovery endpoint |
| GET | `/api/v1/rooms` | Get all rooms |
| POST | `/api/v1/rooms` | Create a room |
| GET | `/api/v1/rooms/{roomId}` | Get one room |
| DELETE | `/api/v1/rooms/{roomId}` | Delete a room if it has no sensors |
| GET | `/api/v1/sensors` | Get all sensors |
| GET | `/api/v1/sensors?type=CO2` | Filter sensors by type |
| POST | `/api/v1/sensors` | Create a sensor |
| GET | `/api/v1/sensors/{sensorId}` | Get one sensor |
| GET | `/api/v1/sensors/{sensorId}/readings` | Get reading history |
| GET | `/api/v1/sensors/{sensorId}/readings/{readingId}` | Get a specific reading |
| POST | `/api/v1/sensors/{sensorId}/readings` | Add a reading |

## Sample curl Commands

### 1. Discovery
```bash
curl -i http://localhost:8080/api/v1
```

### 2. Get all rooms
```bash
curl -i http://localhost:8080/api/v1/rooms
```

### 3. Create a new room
```bash
curl -i -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{
    "id": "SCI-110",
    "name": "Science Seminar Room",
    "capacity": 30
  }'
```

### 4. Get the new room
```bash
curl -i http://localhost:8080/api/v1/rooms/SCI-110
```

### 5. Create a valid sensor
```bash
curl -i -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{
    "id": "CO2-120",
    "type": "CO2",
    "status": "ACTIVE",
    "currentValue": 415.6,
    "roomId": "SCI-110"
  }'
```

### 6. Filter sensors by type
```bash
curl -i "http://localhost:8080/api/v1/sensors?type=CO2"
```

### 7. Add a reading to a valid sensor
```bash
curl -i -X POST http://localhost:8080/api/v1/sensors/CO2-120/readings \
  -H "Content-Type: application/json" \
  -d '{
    "value": 428.9,
    "timestamp": 1760000000000
  }'
```

### 8. Get reading history
```bash
curl -i http://localhost:8080/api/v1/sensors/CO2-120/readings
```

### 9. Trigger 422 using a missing roomId
```bash
curl -i -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{
    "id": "TEMP-404",
    "type": "Temperature",
    "status": "ACTIVE",
    "currentValue": 22.1,
    "roomId": "ROOM-DOES-NOT-EXIST"
  }'
```

### 10. Trigger 403 using a maintenance sensor
```bash
curl -i -X POST http://localhost:8080/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d '{
    "value": 25.2
  }'
```

### 11. Trigger 409 by deleting a room that still has sensors
```bash
curl -i -X DELETE http://localhost:8080/api/v1/rooms/ENG-201
```

### 12. Demonstrate 415 Unsupported Media Type
```bash
curl -i -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: text/plain" \
  -d 'not-json'
```

## Conceptual Report Answers

The module specification says the report answers must be written in the GitHub `README.md`, while another line mentions PDF format. To stay safe, this coursework includes the report in **both** formats:
- `README.md`
- `REPORT_ANSWERS.pdf`

### Part 1 - Service Architecture & Setup

#### 1. Project and Application Configuration
The API is structured as a Maven project using Jersey as the JAX-RS implementation and Grizzly as the embedded HTTP server. The class `AppConfig` extends `ResourceConfig`, which is a subclass of `Application`, and uses `@ApplicationPath("/api/v1")` to declare a versioned API entry point. This improves maintainability because future versions such as `/api/v2` can be introduced without breaking existing clients.

**Question: What is the default lifecycle of a JAX-RS resource class, and how does this affect in-memory data management?**  
By default, JAX-RS resource classes are created **per request**. This means the runtime usually instantiates a new resource object for each incoming HTTP request rather than reusing one singleton object for all requests. This behaviour is useful because it reduces accidental sharing of request-specific state across users. However, it also means that application data cannot safely be stored inside normal instance fields of resource classes if the data must survive across multiple requests. For that reason, this coursework stores shared state in the dedicated `DataStore` class, where thread-safe structures such as `ConcurrentHashMap` and `CopyOnWriteArrayList` are used. These data structures reduce race conditions and prevent data loss when multiple clients interact with the API at the same time.

#### 2. Discovery Endpoint
The discovery endpoint is available at `GET /api/v1`. It returns version information, administrative contact details, and links to the main resource collections. This creates a useful entry point for clients and mirrors the idea that APIs should be discoverable, not just documented externally.

**Question: Why is hypermedia considered an advanced RESTful practice?**  
Hypermedia, often described through HATEOAS, is considered a hallmark of mature REST design because the server does not only send back raw data; it also tells the client what actions are possible next through links. This benefits client developers because they can follow links dynamically instead of hard-coding all possible paths into the client. As an API evolves, clients become less tightly coupled to static documentation because the server can advertise the current valid navigation routes directly in each response. In this coursework, HATEOAS-style links are included so that a client can move from discovery to rooms, from a sensor to its readings, and from a reading back to the parent sensor.

### Part 2 - Room Management

#### 1. Room Resource Implementation
The `RoomResource` class manages the `/rooms` collection. `GET /rooms` returns all room objects, `POST /rooms` creates a new room, and `GET /rooms/{roomId}` returns one specific room.

**Question: What are the implications of returning only room IDs versus full room objects?**  
Returning only IDs makes the response smaller, which saves bandwidth and is useful when collections are very large. However, it increases client-side work because the client must make additional requests to fetch each room's full details. Returning the full room objects gives the client richer information immediately, which improves usability and reduces follow-up calls, but the payload becomes larger. In this coursework the API returns full room objects because the dataset is small, it improves clarity during testing, and it demonstrates the complete resource model more effectively.

#### 2. Room Deletion and Safety Logic
`DELETE /rooms/{roomId}` removes a room only if that room has no linked sensors. If sensors are still assigned, the request is blocked with a `409 Conflict` response.

**Question: Is DELETE idempotent in this implementation?**  
Yes. A DELETE request is idempotent because repeating the same request does not keep changing the system state after the first successful deletion. In this API, the first valid DELETE removes the room. If the same request is repeated again, the room is already missing, so the final state of the system remains unchanged. The second request therefore returns `404 Not Found`, but the operation is still idempotent because no new side effects are produced.

### Part 3 - Sensor Operations and Linking

#### 1. Sensor Resource and Integrity
The `SensorResource` class manages the `/sensors` collection. When a client submits a new sensor, the API checks that the provided `roomId` already exists in the system before saving the sensor.

**Question: What happens if a client sends a format other than JSON?**  
The `POST /sensors` method declares `@Consumes(MediaType.APPLICATION_JSON)`. This means the endpoint explicitly states that it accepts only JSON request bodies. If a client sends `text/plain`, `application/xml`, or another unsupported media type, the JAX-RS runtime performs content negotiation and fails to find a suitable message body reader for that content. The practical result is an HTTP `415 Unsupported Media Type` response because the request format does not match what the endpoint is willing to consume.

#### 2. Filtered Retrieval and Search
The endpoint `GET /sensors` supports an optional `type` query parameter, such as `/sensors?type=CO2`.

**Question: Why is a query parameter better than a path segment for filtering?**  
Using a query parameter is better because the main resource remains the same sensor collection, and the client is simply asking for a filtered view of that collection. A path such as `/sensors/type/CO2` makes the filter look like a different hierarchical resource rather than a search condition. Query parameters are more flexible, more conventional for filtering and searching, and easier to extend later with additional options such as `status`, `roomId`, or value ranges.

### Part 4 - Deep Nesting with Sub-Resources

#### 1. Sub-Resource Locator Pattern
The endpoint `/sensors/{sensorId}/readings` is implemented using a sub-resource locator in `SensorResource`, which returns a dedicated `SensorReadingResource`.

**Question: What are the benefits of the Sub-Resource Locator pattern?**  
The Sub-Resource Locator pattern improves modularity by separating nested resource logic into its own class. Without this pattern, one large controller would need to manage rooms, sensors, and deeply nested reading routes, which would quickly become difficult to maintain. Delegating the reading logic to `SensorReadingResource` makes the code easier to read, test, extend, and debug. In larger APIs, this pattern helps control complexity because each resource class focuses on one level of responsibility.

#### 2. Historical Data Management
Inside `SensorReadingResource`, `GET /` returns reading history and `POST /` appends new readings to the selected sensor. Every successful reading insertion also updates the parent sensor's `currentValue` field. This guarantees consistency between the detailed history and the summary value shown on the parent sensor resource.

### Part 5 - Advanced Error Handling, Exception Mapping, and Logging

#### 1. Resource Conflict (409)
When a client attempts to delete a room that still contains linked sensors, the API throws `RoomNotEmptyException`. The mapper converts that into a `409 Conflict` response with a clean JSON error payload. This is more appropriate than allowing a generic server error because the failure is caused by a business-rule conflict, not an internal crash.

#### 2. Dependency Validation (422)
When a client tries to create a sensor with a `roomId` that does not exist, the API throws `LinkedResourceNotFoundException`, which is mapped to `422 Unprocessable Entity`.

**Question: Why is 422 more accurate than 404 in this situation?**  
HTTP `422 Unprocessable Entity` is more semantically accurate because the endpoint itself exists and the JSON request body is syntactically valid, but the server cannot process it because one field refers to invalid linked data. A normal `404 Not Found` is usually used when the client requests a missing URL or resource directly. Here the path is correct and the payload is structurally correct, but the internal reference is invalid, which is exactly the kind of semantic problem that `422` describes.

#### 3. State Constraint (403)
When a client attempts to post a reading to a sensor whose status is `MAINTENANCE`, the API throws `SensorUnavailableException`, which is mapped to `403 Forbidden`. This reflects the fact that the client is authenticated to reach the endpoint but the current state of the target resource forbids the requested action.

#### 4. Global Safety Net (500)
A catch-all `ExceptionMapper<Throwable>` is implemented to convert unexpected runtime failures into a generic `500 Internal Server Error` response.

**Question: Why should internal stack traces not be exposed?**  
Exposing raw Java stack traces creates a security risk because they may reveal internal package names, class names, source file names, line numbers, server libraries, framework versions, and details about how validation and business logic are structured. An attacker can use this information to map the internals of the application and look for weak points more efficiently. A production-grade API should therefore return a clean generic error response to the client while keeping full technical details only in server-side logs.

#### 5. Logging Filters
The API includes a filter class that implements both `ContainerRequestFilter` and `ContainerResponseFilter`. It logs the HTTP method and URI for incoming requests, and it logs the final status code for outgoing responses.

**Question: Why are JAX-RS filters better than manual logging inside every method?**  
Filters are better for cross-cutting concerns such as logging because they centralise the behaviour in one reusable place. If logging statements were inserted manually into every resource method, the project would become repetitive, harder to maintain, and easier to break accidentally. Filters keep the resource methods focused on business logic while ensuring that all requests and responses are logged consistently across the application.
