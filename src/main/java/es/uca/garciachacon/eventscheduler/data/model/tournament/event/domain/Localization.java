package es.uca.garciachacon.eventscheduler.data.model.tournament.event.domain;

/**
 * Representación de una localización de juego en la que transcurre un partido o un enfrentamiento perteneciente a un
 * evento o categoría de juego en el contexto de un torneo deportivo.
 * <p>
 * <p>Es una representación abstracta que engloba
 * ubicaciones como pueden ser pistas, canchas, campos, rings de lucha, rinks, o incluso tableros y localizaciones
 * digitales o informáticas.</p>
 */
public class Localization extends Entity {
    public Localization(String name) {
        super(name);
    }
}
