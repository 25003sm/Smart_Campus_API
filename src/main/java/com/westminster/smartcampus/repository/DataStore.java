package com.westminster.smartcampus.repository;

import com.westminster.smartcampus.model.Room;
import com.westminster.smartcampus.model.Sensor;
import com.westminster.smartcampus.model.SensorReading;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class DataStore {
    public static final Map<String, Room> rooms = new ConcurrentHashMap<>();
    public static final Map<String, Sensor> sensors = new ConcurrentHashMap<>();
    public static final Map<String, List<SensorReading>> readings = new ConcurrentHashMap<>();

    static {
        seed();
    }

    private DataStore() {
    }

    private static void seed() {
        Room library = new Room("LIB-301", "Library Quiet Study", 45);
        Room engineering = new Room("ENG-201", "Engineering Lab", 35);
        rooms.put(library.getId(), library);
        rooms.put(engineering.getId(), engineering);

        Sensor co2Sensor = new Sensor("CO2-001", "CO2", "ACTIVE", 412.5, "ENG-201");
        Sensor tempSensor = new Sensor("TEMP-001", "Temperature", "MAINTENANCE", 23.4, "LIB-301");
        sensors.put(co2Sensor.getId(), co2Sensor);
        sensors.put(tempSensor.getId(), tempSensor);

        engineering.getSensorIds().add(co2Sensor.getId());
        library.getSensorIds().add(tempSensor.getId());

        List<SensorReading> co2Readings = new CopyOnWriteArrayList<>();
        co2Readings.add(new SensorReading("READ-1001", System.currentTimeMillis() - 60000, 405.3));
        co2Readings.add(new SensorReading("READ-1002", System.currentTimeMillis() - 30000, 412.5));
        readings.put(co2Sensor.getId(), co2Readings);

        List<SensorReading> tempReadings = new CopyOnWriteArrayList<>();
        tempReadings.add(new SensorReading("READ-2001", System.currentTimeMillis() - 120000, 23.4));
        readings.put(tempSensor.getId(), tempReadings);
    }
}
