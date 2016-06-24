package es.uca.garciachacon.eventscheduler.rest.deserializer;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import es.uca.garciachacon.eventscheduler.data.model.tournament.Player;

import java.io.IOException;

/**
 * Deserializador de un jugador ({@link Player}). Espera JSON con el siguiente formato:
 * <code>{"name": John Doe"}</code>.
 * <p>
 * Si el nombre del jugador no es texto, se lanzará una excepción al
 * intentar construir el objeto.
 */
public class PlayerDeserializer extends JsonDeserializer<Player> {
    @Override
    public Player deserialize(JsonParser jp, DeserializationContext ctx) throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);
        JsonNode nameNode = node.get("name");
        return new Player(nameNode.isTextual() ? nameNode.textValue() : null);
    }
}