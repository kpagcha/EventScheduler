package es.uca.garciachacon.eventscheduler.data.model.tournament;

import org.apache.commons.lang3.StringUtils;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Representa un enfrentamiento y define cuatro componentes: al menos dos jugadores que compondrán el
 * enfrentamiento (un partido), una lista de posibles localizaciones de juego donde tendrá lugar el enfrentamiento,
 * una lista de horas de juego o <i>timeslots</i> donde comenzará el enfrentamiento y el número de ocurrencias que
 * habrá de tener. El número de ocurrencias únicamente será relevante en eventos que definan un modo de
 * enfrentamiento personalizado ({@link es.uca.garciachacon.eventscheduler.solver.TournamentSolver.MatchupMode#CUSTOM
 * }), de lo contrario, este valor será absolutamente ignorado y no tendrá consecuencias en el cálculo de los horarios.
 * <p>
 * Nótese que las horas de juego indicadas especifican los posibles comienzos de enfrentamientos, al contrario
 * que la propiedad de un evento que asigna horas de juego a los jugadores, donde se especifica el conjunto de horas
 * donde sus enfrentamientos deben transcurrir, es decir, considerando <i>timeslots</i> intermedios o finales.
 * <p>
 * Los enfrentamientos se publicarán sobre el <i>solver</i> del torneo para aplicar las debidas restricciones que
 * aseguren que en el horario se refleje efectivamente el enfrentamiento definido entre los jugadores especificados,
 * en una de las localizaciones especificadas, en un determinado rango de las horas especificadas y con el número de
 * ocurrencias especificado.
 */
public class Matchup {

    /**
     * Jugadores que componen el enfrentamiento
     */
    private Set<Player> players;

    /**
     * Posibles localizaciones de juego donde el enfrentamiento tendrá lugar
     */
    private Set<Localization> localizations;

    /**
     * Posibles <i>timeslots</i> donde el enfrentamiento comenzará
     */
    private Set<Timeslot> timeslots;

    /**
     * Número de ocurrencias del enfrentamiento
     */
    private int occurrences;

    /**
     * Construye un enfrentamiento (o tantos como se especifique en el número de ocurrencias) perteneciente a un
     * evento, entre jugadores del mismo, en una de las posibles localizaciones de juego indicadas y <i>timeslots</i>
     * indicados.
     * <p>
     * Si el conjunto de localizaciones está vacío, quiere decir que el enfrentamiento puede ocurrir en cualquiera de
     * las disponibles. Lo mismo para el conjunto de <i>timeslots</i>.
     *
     * @param players       conjunto de al menos dos jugadores y pertenecientes al evento
     * @param localizations conjunto de al menos una localización y perteneciente al evento
     * @param timeslots     conjunto de al menos un <i>timeslot</i>
     * @param occurrences   número de veces que el enfrentamiento tendrá lugar, el valor estará entre 1 y el número de
     *                      partidos por jugador que defina el evento (ver {@link Event#getMatchesPerPlayer()}
     * @throws NullPointerException     si alguno de los argumentos es <code>null</code>
     * @throws IllegalArgumentException si el conjunto de jugadores está vacío o contiene <code>null</code>
     * @throws IllegalArgumentException si el conjunto de localizaciones contiene <code>null</code>
     * @throws IllegalArgumentException si el conjunto de <i>timeslots</i> contiene <code>null</code>
     * @throws IllegalArgumentException si el número de ocurrencias es menor que 1
     */
    public Matchup(Set<Player> players, Set<Localization> localizations, Set<Timeslot> timeslots, int occurrences) {
        Objects.requireNonNull(players);
        Objects.requireNonNull(localizations);
        Objects.requireNonNull(timeslots);

        if (players.isEmpty())
            throw new IllegalArgumentException("Players cannot be empty");

        if (players.contains(null))
            throw new IllegalArgumentException("Players cannot contain null");

        if (localizations.contains(null))
            throw new IllegalArgumentException("Localizations cannot contain null");

        if (timeslots.contains(null))
            throw new IllegalArgumentException("Timeslots cannot contain null");

        if (occurrences < 1)
            throw new IllegalArgumentException("Occurrences cannot be less than 1");

        this.players = players;
        this.localizations = localizations;
        this.timeslots = timeslots;
        this.occurrences = occurrences;
    }

    /**
     * Constructor de un enfrentamiento predeterminado que invoca al constructor
     * {@link Matchup#Matchup(Set, Set, Set, int)} con el valor de ocurrencias igual a 1.
     *
     * @param players       conjunto de al menos dos jugadores y pertenecientes al evento
     * @param localizations conjunto de al menos una localización y perteneciente al evento
     * @param timeslots     conjunto de al menos un <i>timeslot</i>
     */
    public Matchup(Set<Player> players, Set<Localization> localizations, Set<Timeslot> timeslots) {
        this(players, localizations, timeslots, 1);
    }

    /**
     * Constructor de un enfrentamiento predeterminado que invoca al constructor
     * {@link Matchup#Matchup(Set, Set, Set, int)} con el valor de ocurrencias igual a 1, y con conjuntos de
     * localizaciones y <i>timeslots</i> vacíos.
     *
     * @param players conjunto de al menos dos jugadores y pertenecientes al evento
     */
    public Matchup(Set<Player> players) {
        this(players, new HashSet<>(), new HashSet<>(), 1);
    }

    public Set<Player> getPlayers() {
        return players;
    }

    public Set<Localization> getLocalizations() {
        return localizations;
    }

    public Set<Timeslot> getTimeslots() {
        return timeslots;
    }

    public int getOccurrences() {
        return occurrences;
    }

    public String toString() {
        return String.format(
                "Matchup {Players=[%s], Localizations=[%s], Timeslots=[%s], Occurrences=%d}",
                StringUtils.join(players, ","),
                StringUtils.join(localizations, ","),
                StringUtils.join(timeslots.stream().sorted(Comparator.reverseOrder()).collect(Collectors
                        .toList()),
                        ","),
                occurrences
        );
    }
}
