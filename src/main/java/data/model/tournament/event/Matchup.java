package data.model.tournament.event;

import data.model.tournament.event.domain.Localization;
import data.model.tournament.event.domain.Player;
import data.model.tournament.event.domain.timeslot.Timeslot;
import org.apache.commons.lang3.StringUtils;
import org.chocosolver.solver.constraints.ternary.Times;

import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>Representa un enfrentamiento y define cuatro componentes: al menos dos jugadores que compondrán el
 * enfrentamiento (un partido), una lista de posibles localizaciones de juego donde tendrá lugar el enfrentamiento,
 * una lista de horas de juego o <i>timeslots</i> donde comenzará el enfrentamiento y el número de ocurrencias que
 * habrá de tener.</p>
 * <p>Nótese que las horas de juego indicadas especifican los posibles comienzos de enfrentamientos, al contrario
 * que la propiedad de un evento que asigna horas de juego a los jugadores, donde se especifica el conjunto de horas
 * donde sus enfrentamientos deben transcurrir, es decir, considerando <i>timeslots</i> intermedios o finales.
 * </p>
 * <p>Los enfrentamientos se publicarán sobre el <i>solver</i> del torneo para aplicar las debidas restricciones que
 * aseguren que en el horario se refleje efectivamente el enfrentamiento definido entre los jugadores especificados,
 * en una de las localizaciones especificadas, en un determinado rango de las horas especificadas y con el número de
 * ocurrencias especificado. </p>
 */
public class Matchup {

    /**
     * Evento al que pertenece el enfrentamiento
     */
    private Event event;

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
    private int occurences;

    /**
     * Construye un enfrentamiento (o tantos como se especifique en el número de ocurrencias) perteneciente a un
     * evento, entre jugadores del mismo, en una de las posibles localizaciones de juego indicadas y <i>timeslots</i>
     * indicados.
     * <p>Se filtrará el agumento <i>timeslots</i> eliminando comienzos de partidos imposibles, por ejemplo, intentar
     * definir el comienzo de un enfrentamiento en el último <i>timeslot</i> de un evento con partidos de una
     * duración de dos timeslots, lo cual sería inválido porque el encuentro nunca podría tener lugar.</p>
     *
     * @param event         event al que pertenece el enfrentamiento
     * @param players       conjunto de al menos dos jugadores y pertenecientes al evento
     * @param localizations conjunto de al menos una localización y perteneciente al evento
     * @param timeslots     conjunto de al menos un <i>timeslot</i>
     * @param occurences    número de veces que el enfrentamiento tendrá lugar, el valor estará entre 1 y el número de
     *                      partidos por jugador que defina el evento (ver {@link Event#getMatchesPerPlayer()}
     * @throws IllegalArgumentException si <code>event</code> es <code>null</code>
     * @throws IllegalArgumentException si <code>players</code> es <code>null</code> o tiene un número de jugadoers
     *                                  distinto del número de jugadores por partido que especifica el evento, o si
     *                                  alguno de éstos es <code>null</code> o no pertenece al evento
     * @throws IllegalArgumentException si <code>localizations</code> es <code>null</code> o está vacío, o si
     *                                  alguna de las localizaciones es <code>null</code> o no pertenece al evento
     * @throws IllegalArgumentException si <code>timeslots</code> es <code>null</code> o está vacío, o si alguno de
     *                                  estos <i>timeslots</i> es <code>null</code> o no pertenece al evento
     * @throws IllegalArgumentException si <code>occurences</code> es menor que 1 o mayor que el número de partidos
     *                                  por jugador que el evento define
     * @throws IllegalArgumentException si se superase el número máximo de partidos que un jugar en particular pueda
     *                                  jugar
     */
    public Matchup(Event event, Set<Player> players, Set<Localization> localizations, Set<Timeslot> timeslots,
            int occurences) throws IllegalArgumentException {
        if (event == null)
            throw new IllegalArgumentException("Event cannot be null");

        if (players == null)
            throw new IllegalArgumentException("Players cannot be null");

        if (localizations == null)
            throw new IllegalArgumentException("Localizations cannot be null");

        if (timeslots == null)
            throw new IllegalArgumentException("Timeslots cannot be null");

        if (players.size() != event.getPlayersPerMatch())
            throw new IllegalArgumentException(String.format("Players cannot contain a number of players (%d) " +
                            "different than the number of players per match the event specifies (%d)",
                    players.size(),
                    event.getPlayersPerMatch()
            ));

        if (players.contains(null))
            throw new IllegalArgumentException("Players cannot contain null");

        if (!event.getPlayers().containsAll(players))
            throw new IllegalArgumentException("Not all players belong to the domain of the event");

        if (localizations.isEmpty())
            throw new IllegalArgumentException("Localizations cannot be empty");

        if (localizations.contains(null))
            throw new IllegalArgumentException("Localizations cannot contain null");

        if (!event.getLocalizations().containsAll(localizations))
            throw new IllegalArgumentException("Not all localizations belong to the domain of the event");

        timeslots.removeIf(t -> event.getTimeslots().indexOf(t) + event.getTimeslotsPerMatch() >
                event.getTimeslots().size());

        if (timeslots.isEmpty())
            throw new IllegalArgumentException("Timeslots cannot be empty");

        if (timeslots.contains(null))
            throw new IllegalArgumentException("Timeslots cannot contain null");

        if (!event.getTimeslots().containsAll(timeslots))
            throw new IllegalArgumentException("Not all timeslots belong to the domain of the event");

        for (Player player : players) {
            long resultingCount = event.getPredefinedMatchups()
                    .stream()
                    .filter(m -> m.getPlayers().contains(player))
                    .mapToInt(Matchup::getOccurences)
                    .sum() + occurences;

            if (resultingCount > event.getMatchesPerPlayer())
                throw new IllegalArgumentException(String.format(
                        "Player's (%s) number of predefined matchups (%d) would exceed the limit (%d)",
                        player,
                        resultingCount,
                        event.getMatchesPerPlayer()
                ));
        }

        this.event = event;
        this.players = players;
        this.localizations = localizations;
        this.timeslots = timeslots;
        this.occurences = occurences;
    }

    public Event getEvent() {
        return event;
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

    public int getOccurences() {
        return occurences;
    }

    public String toString() {
        return String.format("[Players = [%s], Localizations = [%s], Timeslots = [%s], Occurences = %d]",
                StringUtils.join(players, ","),
                StringUtils.join(localizations, ","),
                StringUtils.join(timeslots.stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList()),
                        ","
                ),
                occurences
        );
    }
}