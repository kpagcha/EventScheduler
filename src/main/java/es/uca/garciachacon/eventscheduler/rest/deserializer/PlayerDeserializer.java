package es.uca.garciachacon.eventscheduler.rest.deserializer;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import es.uca.garciachacon.eventscheduler.data.model.tournament.event.domain.Player;

import java.io.IOException;

public class PlayerDeserializer extends JsonDeserializer<Player> {
    @Override
    public Player deserialize(JsonParser jp, DeserializationContext ctx) throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);
        JsonNode nameNode = node.get("name");
        return new Player(nameNode.isTextual() ? nameNode.textValue() : null);
    }
}