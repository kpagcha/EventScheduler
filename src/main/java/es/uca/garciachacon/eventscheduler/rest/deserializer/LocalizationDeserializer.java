package es.uca.garciachacon.eventscheduler.rest.deserializer;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import es.uca.garciachacon.eventscheduler.data.model.tournament.event.domain.Localization;

import java.io.IOException;

public class LocalizationDeserializer extends JsonDeserializer<Localization> {
    @Override
    public Localization deserialize(JsonParser jp, DeserializationContext ctx) throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);
        JsonNode nameNode = node.get("name");
        return new Localization(nameNode.isTextual() ? nameNode.textValue() : null);
    }
}