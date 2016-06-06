package es.uca.garciachacon.eventscheduler.rest.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import es.uca.garciachacon.eventscheduler.data.model.tournament.event.Event;

import java.io.IOException;

/**
 * Deserializador personalizado de la clase {@link Event}.
 */
public class EventDeserializer extends JsonDeserializer<Event> {
    @Override
    public Event deserialize(JsonParser jp, DeserializationContext ctx) throws IOException {
        return null;
    }
}
