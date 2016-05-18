package data.model.tournament.event.domain;

import org.apache.commons.lang3.StringUtils;
import solver.TournamentSolver;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Un equipo es una composición de jugadores, como mínimo dos de ellos.
 * <p>
 * <p>Los jugadores que pertenecen a un mismo equipo toman parte en los mismos enfrentamientos de las categorías en
 * las que participan y se enfrentan, en conjunto, a otros participantes.</p>
 * <p>
 * <p>Se diferencian de la clase {@link Player} en que, mientras esta última define un equipo como una única entidad
 * que juega, esta clase considera los jugadores individuales de los que está compuesto.</p>
 * <p>
 * <p>El principal objetivo de esta distinción es permitir a jugadores participar en potencialmente distintas
 * categorías, tanto siendo parte de equipos como individualmente, de modo que el solucionador del problema,
 * {@link TournamentSolver}, tenga en cuenta la participación del jugador como entidad individual en múltiples
 * categorías.</p>
 */
public class Team extends Entity {
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
