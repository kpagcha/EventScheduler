package es.uca.garciachacon.eventscheduler.data.model.tournament.event.domain;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.IOException;

/**
 * Representación de un jugador en un evento o categoría de juego en el contexto de un torneo deportivo. Un jugador
 * es el sujeto que compone un partido o enfrentamiento y puede competir contra otros jugadores.
 * <p>
 * Es una representación abstracta que puede ser un jugador entendido como individuo, es decir, un deportista, una
 * persona, un competidor o un participante, así como un conjunto de individuos que, en términos del modelo
 * deportivo, representar un sujeto único que compite, siendo el ejemplo más claro el de un equipo de jugadores.
 * <p>
 * También puede representar un jugador entendido como entidad que juega, sin mayor detalle ni concreción de las
 * características de la entidad, por ejemplo, un programa o proceso informático.
 */
@JsonDeserialize(using = PlayerDeserializer.class)
public class Player extends Entity {
    public Player(String name) {
        super(name);
    }
}

class PlayerDeserializer extends JsonDeserializer<Player> {
    @Override
    public Player deserialize(JsonParser jp, DeserializationContext ctx) throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);
        JsonNode nameNode = node.get("name");
        return new Player(nameNode.isNull() ? null : nameNode.textValue());
    }
}