package com.westminster.smartcampus.config;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("/api/v1")
public class AppConfig extends ResourceConfig {
    public AppConfig() {
        packages("com.westminster.smartcampus.resource",
                "com.westminster.smartcampus.mapper",
                "com.westminster.smartcampus.filter");
        register(JacksonFeature.class);
    }
}
