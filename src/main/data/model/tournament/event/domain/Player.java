package data.model.tournament.event.domain;

/**
 * Representación de un jugador en un evento o categoría de juego en el contexto de un torneo deportivo.
 * Un jugador es el sujeto que compone un partido o enfrentamiento y puede competir contra otros jugadores.
 * <p>
 * Es una representación abstracta que puede ser un jugador entendido como individuo, es decir, un deportista,
 * una persona, un competidor o un participante, así como un conjunto de individuos que, en términos del modelo
 * deportivo, representar un sujeto único que compite, siendo el ejemplo más claro el de un equipo de jugadores.
 * <p>
 * También puede representar un jugador entendido como entidad que juega, sin mayor detalle ni concreción de
 * las características de la entidad, por ejemplo, un programa o proceso informático.
 *
 */
public class Player extends Entity {
	public Player(String name) {
		super(name);
	}
}