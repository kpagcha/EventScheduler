package data.model.tournament.event.entity;

/**
 * Representación de una localización de juego en la que transcurre un partido o un enfrentamiento
 * perteneciente a un evento o categoría de juego en el contexto de un torneo deportivo.
 * <p>
 * Es una representación abstracta que engloba ubicaciones como pueden ser pistas, canchas, campos,
 * rings de lucha, rinks, o incluso tableros y localizaciones digitales o informáticas.
 *
 */
public class Localization extends Entity {
	public Localization(String name) {
		super(name);
	}
}
