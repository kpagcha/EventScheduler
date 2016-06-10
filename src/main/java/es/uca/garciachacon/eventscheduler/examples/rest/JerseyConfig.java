package es.uca.garciachacon.eventscheduler.examples.rest;

import org.glassfish.jersey.server.ResourceConfig;

public class JerseyConfig extends ResourceConfig {
    public JerseyConfig() {
        register(new Binder());
    }
}
