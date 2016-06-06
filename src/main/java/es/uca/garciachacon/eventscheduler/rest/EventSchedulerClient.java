package es.uca.garciachacon.eventscheduler.rest;

import org.glassfish.jersey.client.ClientConfig;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class EventSchedulerClient {
    public static void main(String[] args) {
        ClientConfig config = new ClientConfig();
        Client client = ClientBuilder.newClient(config);
        WebTarget target = client.target(getBaseUri());

        String response =
                target.path("eventscheduler").request().accept(MediaType.TEXT_PLAIN).get(Response.class).toString();
        String plain = target.path("eventscheduler").request().accept(MediaType.TEXT_PLAIN).get(String.class);

        System.out.println(response);
        System.out.println(plain);
    }

    private static URI getBaseUri() {
        return UriBuilder.fromUri("http://localhost:8080/").build();
    }
}
