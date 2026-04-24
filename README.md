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
├── SmartCampus.postman_collection.json
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
| GET | `/api/v1/rooms/ROOM1` | Get one room |
| DELETE | `/api/v1/rooms/ROOM1` | Room Deletion |
| GET | `/api/v1/sensors` | Get all Sensors |
| POST | `/api/v1/sensors` | Create Sensors |
| GET | `/api/v1/sensors?type=CO2` | Filter Sensors |
| GET | `/api/v1/sensors/S1` | Get Sensor Readings |
| POST | `/api/v1/sensors/S1/readings` | Add Sensor Reading |
| DELETE | `/api/v1/rooms/ROOM1` | Delete Romm with Sensor |
| POST | `/api/v1/sensors` | Invalid Room |
| POST | `/api/v1/sensors//SENSOR-M1/readings` | SensorUnavailableException |
| GET | `/api/v1/rooms/%$#@!` | GlobalExceptionMapper |
| GET | `/api/v1/rooms` | LoggingFilter |


## Sample curl Commands

### 1. Discovery Endpoint
```bash
curl -i http://localhost:8080/api/v1
```

### 2. Create a Room
```bash
curl -X POST http://localhost:8080/api/v1/rooms \
-H "Content-Type: application/json" \
-d '{
  "id": "ROOM1",
  "name": "Lecture Hall",
  "capacity": 100
}'
```

### 3. Get all Rooms
```bash
curl -X GET http://localhost:8080/api/v1/rooms
```

### 4. Create a Sensor
```bash
curl -X POST http://localhost:8080/api/v1/sensors \
-H "Content-Type: application/json" \
-d '{
  "id": "S1",
  "type": "CO2",
  "status": "ACTIVE",
  "currentValue": 0,
  "roomId": "ROOM1"
}'
```

### 5. Get all Sensors
```bash
curl -X GET http://localhost:8080/api/v1/sensors
```

### 6. Filter Sensors
```bash
curl -X GET "http://localhost:8080/api/v1/sensors?type=CO2"
```

### 7. Add Sensor reading
```bash
curl -X POST http://localhost:8080/api/v1/sensors/S1/readings \
-H "Content-Type: application/json" \
-d '{
  "id": "R1",
  "timestamp": 1713950000000,
  "value": 45.5
}'
```

### 8. Get Sensor reading
```bash
curl -X GET http://localhost:8080/api/v1/sensors/S1/readings
```
