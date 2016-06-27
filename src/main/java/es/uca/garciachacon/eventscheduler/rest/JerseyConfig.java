package es.uca.garciachacon.eventscheduler.rest;

import org.glassfish.jersey.server.ResourceConfig;

public class JerseyConfig extends ResourceConfig {
    public JerseyConfig() {
        register(new es.uca.garciachacon.eventscheduler.rest.Binder());
    }
}
