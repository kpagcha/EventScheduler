package es.uca.garciachacon.eventscheduler.data.model.schedule;

import com.fasterxml.jackson.annotation.JsonInclude;
import es.uca.garciachacon.eventscheduler.data.model.tournament.Localization;
import es.uca.garciachacon.eventscheduler.data.model.tournament.Player;
import es.uca.garciachacon.eventscheduler.data.model.tournament.Team;
import es.uca.garciachacon.eventscheduler.data.model.tournament.Timeslot;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * Representa un partido o enfrentamiento. Un partido normalmente se compone de dos o más jugadores que se enfrentan
 * o compiten entre sí, aunque también puede ser compuesto por un solo jugador, para deportes o juegos individuales.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Match {
    /**
     * Lista de jugadores que componen el enfrentamiento
     */
    private final List<Player> players;

    /**
     * Localización de juego donde tiene lugar el enfrentamiento
     */
    private final Localization localization;

    /**
     * Período en el que el enfrentamiento comienza
     */
    private final Timeslot startTimeslot;

    /**
     * Último período en el que el enfrentamiento tiene lugar
     */
    private final Timeslot endTimeslot;

    /**
     * Número de timeslots que ocupa
     */
    private final int duration;

    /**
     * Lista de equipos, si hubiera
     */
    private List<Team> teams;

    /**
     * Construye un enfrentamiento
     *
     * @param players      lista de jugadores que componen el partido no nula y con al menos un jugador
     * @param localization localización de juego donde tiene lugar el enfrentamiento
     * @param start        hora o <i>timeslot</i> de comienzo del partido
     * @param end          hora o <i>timeslot</i> de fin del partido
     * @param duration     número de timeslots que dura el partido. Mayor que 0
     * @throws NullPointerException     si algún argumento es <code>null</code>
     * @throws IllegalArgumentException si la lista de jugadores está vacía
     * @throws IllegalArgumentException si la lista de jugadores tiene duplicados
     * @throws IllegalArgumentException si la duración del partido es menor que 1
     * @throws IllegalArgumentException si el <i>timeslot</i> final precede al comienzo
     * @throws IllegalArgumentException si la duración del partido es 1 y los <i>timeslots</i> comienzo y fin son
     *                                  distintos
     */
    public Match(List<Player> players, Localization localization, Timeslot start, Timeslot end, int duration) {
        Objects.requireNonNull(players);
        Objects.requireNonNull(localization);
        Objects.requireNonNull(start);
        Objects.requireNonNull(end);

        if (players.isEmpty())
            throw new IllegalArgumentException("Players cannot be empty");

        if (duration < 1)
            throw new IllegalArgumentException(
                    "The duration of the match cannot amount to a timeslot span lesser than 1");

        if (end.compareTo(start) > 0)
            throw new IllegalArgumentException("The end timeslot cannot precede the start timeslot");

        if (duration > 1 && start.compareTo(end) == 0)
            throw new IllegalArgumentException(
                    "The duration of the match cannot amount to a timeslot span greater than 1 if the start timeslot " +
                            "is the same than the end timeslot");

        for (int i = 0; i < players.size() - 1; i++)
            for (int j = i + 1; j < players.size(); j++)
                if (players.get(i) == players.get(j))
                    throw new IllegalArgumentException("Players cannot be duplicated");

        this.players = players;
        this.localization = localization;
        startTimeslot = start;
        endTimeslot = end;
        this.duration = duration;
        teams = new ArrayList<>();
    }

    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    public Localization getLocalization() {
        return localization;
    }

    public Timeslot getStartTimeslot() {
        return startTimeslot;
    }

    public Timeslot getEndTimeslot() {
        return endTimeslot;
    }

    public int getDuration() {
        return duration;
    }

    public List<Team> getTeams() {
        return Collections.unmodifiableList(teams);
    }

    /**
     * Establece el conjunto de equipos que se enfrentan en este partido
     *
     * @param teams una lista de equipos que cumple las precondiciones especificadas
     * @throws NullPointerException     si la lista de equipos es <code>null</code>
     * @throws IllegalArgumentException si los equipos contienen un equipo <code>null</code>
     * @throws IllegalArgumentException si los equipos contienen equipos repetidos
     * @throws IllegalArgumentException si hay equipos con diferentes números de jugadores
     * @throws IllegalArgumentException si hay equipos con jugadores que no pertenecen a los jugadores de este partido
     * @throws IllegalArgumentException si el número de jugadores de algún equipo no es coherente con el número de
     *                                  jugadores del partido (debe ser divisor de este último)
     */
    public void setTeams(List<Team> teams) {
        Objects.requireNonNull(teams);

        if (teams.isEmpty())
            throw new IllegalArgumentException("Teams cannot be empty");

        if (teams.contains(null))
            throw new IllegalArgumentException("Teams cannot contain a null team");

        if (new HashSet<>(teams).size() != teams.size())
            throw new IllegalArgumentException("Teams cannot be duplicated");

        int nPlayersPerTeam = teams.get(0).getPlayers().size();
        for (int i = 1; i < teams.size(); i++)
            if (teams.get(i).getPlayers().size() != nPlayersPerTeam)
                throw new IllegalArgumentException("Teams cannot have different number of players");

        if (players.size() % nPlayersPerTeam != 0)
            throw new IllegalArgumentException(String.format(
                    "The number of players in a team (%d) must be a divisor of the number of players of the match (%d)",
                    nPlayersPerTeam,
                    players.size()
            ));

        for (Team team : teams)
            if (!players.containsAll(team.getPlayers()))
                throw new IllegalArgumentException(
                        "All players in a team must be contained in the list of players of this match");

        this.teams = teams;
    }

    /**
     * Comprueba si el transcurso de un partido sucede, parcial o totalmente, durante el rango indicado
     *
     * @param t1 un extremo del rango
     * @param t2 el otro extremo
     * @return <code>true</code> si el partido o parte del mismo se juega dentro del rango, <code>false</code> si no
     */
    public boolean during(Timeslot t1, Timeslot t2) {
        Timeslot start, end;
        if (t1.compareTo(t2) >= 0) {
            start = t1;
            end = t2;
        } else {
            start = t2;
            end = t1;
        }

        return !(startTimeslot.compareTo(end) < 0 || endTimeslot.compareTo(start) > 0);
    }

    /**
     * Comprueba si el transcurso de un partido sucede dentro el rango indicado, es decir, el comienzo del partido es
     * igual o posterior al extremo mayor y el fin del partido es igual o anterior al extremo menor.
     * <p>
     * Los extremos también se consideran parte del rango.
     *
     * @param t1 un extremo del rango
     * @param t2 el otro extremo
     * @return <code>true</code> si el partidose juega dentro del rango, <code>false</code> si no
     */
    public boolean within(Timeslot t1, Timeslot t2) {
        Timeslot start, end;
        if (t1.compareTo(t2) >= 0) {
            start = t1;
            end = t2;
        } else {
            start = t2;
            end = t1;
        }

        return startTimeslot.compareTo(start) <= 0 && endTimeslot.compareTo(end) >= 0;
    }

    /**
     * Comprueba si el transcurso de un partido sucede durante el <i>timeslot</i>
     *
     * @param t <i>timeslot</i> no <code>null</code>
     * @return <code>true</code> si el partido o parte del mismo se juega durante el <i>timeslot</i>,
     * <code>false</code> si no
     */
    public boolean during(Timeslot t) {
        return t.within(startTimeslot, endTimeslot);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("At %s in %s: ", startTimeslot, localization));

        if (teams.isEmpty())
            sb.append(StringUtils.join(players, " vs "));
        else
            sb.append(StringUtils.join(teams, " vs "));

        return sb.toString();
    }
}
