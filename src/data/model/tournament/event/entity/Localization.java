package data.model.tournament.event.entity;

/**
 * Representaci�n de una localizaci�n de juego en la que transcurre un partido o un enfrentamiento
 * perteneciente a un evento o categor�a de juego en el contexto de un torneo deportivo.
 * <p>
 * Es una representaci�n abstracta que engloba ubicaciones como pueden ser pistas, canchas, campos,
 * rings de lucha, rinks, o incluso tableros y localizaciones digitales o inform�ticas.
 *
 */
public class Localization extends Entity {
	public Localization(String name) {
		super(name);
	}
}
