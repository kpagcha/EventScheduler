package es.uca.garciachacon.eventscheduler.restexample;

import org.glassfish.jersey.server.ResourceConfig;

public class JerseyConfig extends ResourceConfig {
    public JerseyConfig() {
        register(new Binder());
    }
}
