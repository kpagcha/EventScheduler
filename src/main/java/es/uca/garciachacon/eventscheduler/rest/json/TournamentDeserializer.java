package es.uca.garciachacon.eventscheduler.rest.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import es.uca.garciachacon.eventscheduler.data.model.tournament.Tournament;

import java.io.IOException;

/**
 * Deserializador personalizado de la clase {@link Tournament}.
 */
public class TournamentDeserializer extends JsonDeserializer<Tournament> {
    @Override
    public Tournament deserialize(JsonParser jp, DeserializationContext ctx) throws IOException {
        return null;
    }
}
