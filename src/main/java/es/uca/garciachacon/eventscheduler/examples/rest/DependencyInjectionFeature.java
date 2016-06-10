package es.uca.garciachacon.eventscheduler.examples.rest;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

@Provider
public class DependencyInjectionFeature implements Feature {
    @Override
    public boolean configure(FeatureContext context) {
        context.register(new Binder());
        return true;
    }
}
