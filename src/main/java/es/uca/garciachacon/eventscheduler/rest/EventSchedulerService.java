package es.uca.garciachacon.eventscheduler.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("eventscheduler")
public class EventSchedulerService {
    @GET
    public String getMessage() {
        return "Hello World!";
    }
}
