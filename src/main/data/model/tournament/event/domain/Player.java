package data.model.tournament.event.domain;

/**
 * Representaci�n de un jugador en un evento o categor�a de juego en el contexto de un torneo deportivo.
 * Un jugador es el sujeto que compone un partido o enfrentamiento y puede competir contra otros jugadores.
 * <p>
 * Es una representaci�n abstracta que puede ser un jugador entendido como individuo, es decir, un deportista,
 * una persona, un competidor o un participante, as� como un conjunto de individuos que, en t�rminos del modelo
 * deportivo, representar un sujeto �nico que compite, siendo el ejemplo m�s claro el de un equipo de jugadores.
 * <p>
 * Tambi�n puede representar un jugador entendido como entidad que juega, sin mayor detalle ni concreci�n de
 * las caracter�sticas de la entidad, por ejemplo, un programa o proceso inform�tico.
 *
 */
public class Player extends Entity {
	public Player(String name) {
		super(name);
	}
}