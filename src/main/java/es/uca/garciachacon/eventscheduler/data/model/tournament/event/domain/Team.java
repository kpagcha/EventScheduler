package es.uca.garciachacon.eventscheduler.data.model.tournament.event.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import es.uca.garciachacon.eventscheduler.data.model.tournament.event.Event;
import es.uca.garciachacon.eventscheduler.solver.TournamentSolver;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Un equipo es una composición de jugadores, como mínimo dos de ellos.
 * <p>
 * Los jugadores que pertenecen a un mismo equipo toman parte en los mismos enfrentamientos de las categorías en
 * las que participan y se enfrentan, en conjunto, a otros participantes.
 * <p>
 * Se diferencian de la clase {@link Player} en que, mientras esta última define un equipo como una única entidad
 * que juega, esta clase considera los jugadores individuales de los que está compuesto.
 * <p>
 * El principal objetivo de esta distinción es permitir a jugadores participar en potencialmente distintas
 * categorías, tanto siendo parte de equipos como individualmente, de modo que el solucionador del problema,
 * {@link TournamentSolver}, tenga en cuenta la participación del jugador como entidad individual en múltiples
 * categorías.
 */
public class Team extends Entity {
    /**
     * Evento al que pertenece el equipo. Sólo se asigna cuando el equipo es añadido a un evento en particular.
     */
    @JsonBackReference
    private Event event;

    /**
     * Conjunto de jugadores que componene el equipo
     */
    private Set<Player> players;

    /**
     * Constructor de un equipo, al que le asigna un nombre e inicializa el conjunto de jugadores de los que se compone.
     *
     * @param name    una cadena no nula
     * @param players un conjunto con más dos de jugadores, no <code>null</code>
     * @throws IllegalArgumentException si <code>players</code> es <code>null</code>, o si alguno sus elementos son
     *                                  <code>null</code> o si su tamaño es inferior a 2
     */
    public Team(String name, Set<Player> players) {
        super(name);

        if (players == null)
            throw new IllegalArgumentException("Players cannot be null");

        if (players.size() < 2)
            throw new IllegalArgumentException("A team cannot have less than two players");

        if (players.contains(null))
            throw new IllegalArgumentException("A team cannot contain a null player");

        this.players = players;
    }

    /**
     * Construye un equipo compuesto por los jugadores indicados. El nombre del equipo será la concatenación por un
     * guión ("-") de los nombres de los componentes del equipo. Este constructor invoca al constructor
     * {@link Team#Team(String, Set)}, luego se lanzarán las mismas excepciones si se dan las circunstancias.
     *
     * @param players conjunto no nulo de más de dos jugadores
     */
    public Team(Set<Player> players) {
        this(StringUtils.join(players, "-"), players);
    }

    /**
     * Construye un equipo a partir de un array de jugadores.
     *
     * @param playersArray array no nulo de jugadores únicos
     */
    public Team(Player... playersArray) {
        super(StringUtils.join(playersArray, "-"));

        for (int i = 0; i < playersArray.length - 1; i++)
            for (int j = i + 1; j < playersArray.length; j++)
                if (playersArray[i] == playersArray[j])
                    throw new IllegalArgumentException(String.format("All players must be unique; player(%s) is " +
                            "duplicated",
                            playersArray[i]
                    ));

        Set<Player> players = new HashSet<>(Arrays.asList(playersArray));

        if (players.size() < 2)
            throw new IllegalArgumentException("A team cannot have less than two players");

        if (players.contains(null))
            throw new IllegalArgumentException("A team cannot contain a null player");

        this.players = players;
    }

    public Event getEvent() { return event; }

    /**
     * Asocia un evento a este equipo. Este método es usado únicamente en la clase {@link Event}, es decir, la
     * asociación se gestiona de forma interna.
     *
     * @param event un evento no nulo
     * @throws IllegalArgumentException si el evento es <code>null</code>
     * @throws IllegalStateException    si ya existe una asociación. Es ilegal invocar a esta función externamente, no
     *                                  se puede cambiar voluntariamente el evento asociado al equipo; ésta es una
     *                                  responsabilidad de la clase {@link Event}
     */
    public void setEvent(Event event) {
        if (event == null)
            throw new IllegalArgumentException("Event cannot be null");

        if (this.event != null)
            throw new IllegalStateException("Event has already been set");

        this.event = event;
    }

    public Set<Player> getPlayers() {
        return players;
    }

    /**
     * Comprueba si este equipo contiene un jugador.
     *
     * @param player un jugador cualquier
     * @return <code>true</code> si el jugador pertenece a este equipo, de lo contrario, <code>false</code>
     */
    public boolean contains(Player player) {
        return players.contains(player);
    }

    public String toString() {
        return name;
    }
}