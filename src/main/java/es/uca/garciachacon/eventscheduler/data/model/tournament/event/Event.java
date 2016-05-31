package es.uca.garciachacon.eventscheduler.data.model.tournament.event;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import es.uca.garciachacon.eventscheduler.data.model.tournament.Tournament;
import es.uca.garciachacon.eventscheduler.data.model.tournament.event.domain.Localization;
import es.uca.garciachacon.eventscheduler.data.model.tournament.event.domain.Player;
import es.uca.garciachacon.eventscheduler.data.model.tournament.event.domain.Team;
import es.uca.garciachacon.eventscheduler.data.model.tournament.event.domain.timeslot.Timeslot;
import es.uca.garciachacon.eventscheduler.data.validation.validable.Validable;
import es.uca.garciachacon.eventscheduler.data.validation.validable.ValidationException;
import es.uca.garciachacon.eventscheduler.data.validation.validator.Validator;
import es.uca.garciachacon.eventscheduler.data.validation.validator.tournament.EventValidator;
import es.uca.garciachacon.eventscheduler.solver.TournamentSolver.MatchupMode;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Representación de un evento o categoría deportiva en el contexto de un torneo deportivo.
 * <p>
 * Un evento se compone de jugadores que componen enfrentamientos y partidos en los que participan, y que discurren
 * en determinadas localizaciones de juego asignadas al evento, y a determinadas hora de juego.
 * <p>
 * Una categoría además incluye información adicional que especifica elementos más detallados:
 * <ul>
 * <li>Número de partidos que cada jugador del evento debe jugar
 * <li>Número de horas o <i>timeslots</i> que un partido ocupa, es decir, su duración
 * <li>Número de jugadores que componen un partido
 * <li>Equipos, entendidos como asociación de jugadores individuales, que participan en el evento, si los hubiese
 * <li>Conjunto de horas del evento en las que un enfrentamiento no puede tener lugar, es decir, <i>breaks</i> o
 * descansos, como descansos para comer, la noche, etc...
 * <li>Registro de horas a las que cada jugador no está disponible para tomar parte en un enfrentamiento, si hubiese
 * <li>Registro de localizaciones de juego que no se encuentran disponibles a determinadas horas por cualquier razón,
 * si hubiese
 * <li>Registro de enfrentamientos fijos o predefinidos de antemano entre distintos jugadores en particular, si hubiese
 * <li>Registro de localizaciones de juego predefinidas de antemano donde los jugadores especificados deben jugar, si
 * hubiese
 * <li>Registro de horas de juego predefinidas de antemano cuando los jugadores especificados deben jugar, si hubiese
 * <li>Modo de enfrentamiento, que especifica el modo como se calcularán los emparejamientos
 * </ul>
 */
public class Event implements Validable {
    /**
     * Nombre del evento o la categoría
     */
    private String name;

    /**
     * Torneo al que pertenece la categoría
     */
    @JsonManagedReference
    private Tournament tournament;

    /**
     * Jugadores concretos o abstractos (equipos) que participan en el evento
     */
    private final List<Player> players;

    /**
     * Localizaciones o terrenos de juego disponibles para la categoría
     */
    private final List<Localization> localizations;

    /**
     * Horas en las que tendrá lugar esta categoría (dominio temporal del evento o categoría)
     */
    private final List<Timeslot> timeslots;

    /**
     * Número de partidos que cada jugador ha de jugar en esta categoría
     */
    private int nMatchesPerPlayer = 1;

    /**
     * Duración de un partido en timeslots u horas
     */
    private int nTimeslotsPerMatch = 2;

    /**
     * Número de jugador que componen un partido
     */
    private int nPlayersPerMatch = 2;

    /**
     * Composición de los equipos, si hay
     */
    private List<Team> teams = new ArrayList<>();

    /**
     * Lista de timeslots del evento donde no pueden tener lugar partidos
     */
    private List<Timeslot> breaks = new ArrayList<>();

    /**
     * Timeslots u horas en las que cada jugador no está disponible
     */
    private Map<Player, Set<Timeslot>> unavailablePlayers = new HashMap<>();

    /**
     * Diccionario de localizaciones de juego no disponibles en determinadas horas
     */
    private Map<Localization, Set<Timeslot>> unavailableLocalizations = new HashMap<>();

    /**
     * Emparejamientos fijos predefinidos
     */
    private Set<Matchup> predefinedMatchups = new HashSet<>();

    /**
     * Diccionario de jugadores para cada cual los enfrentamientos de los que forme parte han de tener lugar
     * en cualquiera de las localizaciones del conjunto de localizaciones de juego asociado a su entrada
     */
    private Map<Player, Set<Localization>> playersInLocalizations = new HashMap<>();

    /**
     * Diccionario de jugadores para cada cual los enfrentamientos de los que forme parte han de comenzar en
     * cualquiera de las horas del conjunto de timeslots asociado a su entrada
     */
    private Map<Player, Set<Timeslot>> playersAtTimeslots = new HashMap<>();

    /**
     * Si esta categoría define más de un partido por jugador, indica el modo de emparejamiento
     */
    private MatchupMode matchupMode = MatchupMode.ANY;

    /**
     * Validador del evento
     */
    private Validator<Event> validator = new EventValidator();

    /**
     * Construye un evento con la información esencial: nombre, jugadores, localizaciones y timeslots.
     *
     * @param name          nombre del evento o de la categoría, no nulo
     * @param players       jugadores que participan, lista no nula
     * @param localizations localizaciones de juego en las que tendrá lugar, lista no nula
     * @param timeslots     horas o timeslots en los que discurrirá, lista no nula
     * @throws IllegalArgumentException si no se cumple alguna de las siguientes precondiciones:<br>
     *                                  <h2>Jugadores:</h2>
     *                                  <ul>
     *                                  <li>La lista de jugadores no puede ser <code>null</code>
     *                                  <li>El número de jugadores no puede ser inferior a 1
     *                                  <li>El número de jugadores por partido no puede ser inferior a 1
     *                                  <li>El número de jugadores por partido no puede ser superior al número de
     *                                  jugadores
     *                                  <li>El número de jugadores debe ser un múltiplo del número de jugadores por
     *                                  partido
     *                                  <li>Un jugador no puede estar repetido
     *                                  </ul>
     *                                  <br>
     *                                  <h2>Localizaciones:</h2>
     *                                  <ul>
     *                                  <li>La lista de localizaciones no es <code>null</code>
     *                                  <li>La lista de localizaciones tiene más de un elemento
     *                                  <li>La lista de localizaciones no tiene ningún elemento repetido
     *                                  </ul>
     *                                  <br>
     *                                  <h2>Horas</h2>
     *                                  <ul>
     *                                  <li>La lista de horas no es <code>null</code>
     *                                  <li>La lista de horas tiene más de un timeslot
     *                                  <li>La duración de un partido es superior a 1 timeslot
     *                                  <li>El número de partidos por jugador es, como mínimo, 1
     *                                  <li>El número de horas en la lista debe ser, como mínimo, el producto de la
     *                                  duración de un partido y
     *                                  el número de partidos que cada jugador debe jugar, es decir,
     *                                  <code>nTimeslotsPerMatch * nMatchesPerPlayer</code>
     *                                  <li>Cada hora debe ser posterior a la anterior, es decir, en términos de
     *                                  comparación, superior estrictamente
     *                                  <li>Una hora no puede estar repetida
     *                                  </ul>
     */
    public Event(String name, List<Player> players, List<Localization> localizations, List<Timeslot> timeslots) {
        if (name == null)
            throw new IllegalArgumentException("The name cannot be null");

        checkPlayersPreconditions(players, nPlayersPerMatch);
        checkLocalizationsPreconditions(localizations);
        checkTimeslotsPreconditions(timeslots, nTimeslotsPerMatch, nMatchesPerPlayer);

        this.name = name;
        this.players = players;
        this.localizations = localizations;
        this.timeslots = timeslots;
    }

    /**
     * Construye un evento con la información esencial y configuración básica adicional.
     *
     * @param name              nombre no null del evento
     * @param players           lista no vacía de jugadores únicos
     * @param localizations     lista no vacía de localizaciones de juego no repetidas
     * @param timeslots         lista de no vacía horas de juego, no null y con más de un elemento
     * @param matchesPerPlayer  número de partidos por jugador, superior a 1
     * @param timeslotsPerMatch número de timeslots por partido, superior a 1
     * @param playersPerMatch   número de jugadores por partido, igual o superior a 1 y divisor del tamaño de la lista
     *                          de jugadores
     */
    public Event(String name, List<Player> players, List<Localization> localizations, List<Timeslot> timeslots,
            int matchesPerPlayer, int timeslotsPerMatch, int playersPerMatch) {

        if (name == null)
            throw new IllegalArgumentException("The name cannot be null");

        checkPlayersPreconditions(players, playersPerMatch);
        checkLocalizationsPreconditions(localizations);
        checkTimeslotsPreconditions(timeslots, timeslotsPerMatch, matchesPerPlayer);

        this.name = name;
        this.players = players;
        this.localizations = localizations;
        this.timeslots = timeslots;

        nMatchesPerPlayer = matchesPerPlayer;
        nTimeslotsPerMatch = timeslotsPerMatch;
        nPlayersPerMatch = playersPerMatch;
    }

    /**
     * Comprueba las precondiciones de jugadores y número de jugadores por partido.
     *
     * @param players          lista de jugadores únicos
     * @param nPlayersPerMatch número de jugadores por partido
     * @throws IllegalArgumentException si no se cumple alguna de las precondiciones de jugadores, ver
     *                                  {@link #Event(String, List, List, List)})
     */
    private void checkPlayersPreconditions(List<Player> players, int nPlayersPerMatch) {
        if (players == null)
            throw new IllegalArgumentException("The list of players cannot be null");

        if (players.size() < 1)
            throw new IllegalArgumentException("The number of players (" + players.size() + ") cannot be less than 1");

        if (players.contains(null))
            throw new IllegalArgumentException("A player cannot be null");

        if (nPlayersPerMatch < 1)
            throw new IllegalArgumentException(
                    "The number of players per match (" + nPlayersPerMatch + ") cannot be less than 1");

        if (players.size() % nPlayersPerMatch != 0)
            throw new IllegalArgumentException("The number of players (" + players.size() +
                    ") must be a multiple of the number of players per match specified in this event (" +
                    nPlayersPerMatch + ")");

        for (int i = 0; i < players.size() - 1; i++)
            for (int j = i + 1; j < players.size(); j++)
                if (players.get(i).equals(players.get(j)))
                    throw new IllegalArgumentException("All players must be unique");
    }

    /**
     * Comprueba las precondiciones para las localizaciones de juego del evento
     *
     * @param localizations lista de localizaciones de juego
     * @throws IllegalArgumentException si no se cumple alguna de las precondiciones
     */
    private void checkLocalizationsPreconditions(List<Localization> localizations) {
        if (localizations == null)
            throw new IllegalArgumentException("The list of localizations cannot be null");

        if (localizations.size() < 1)
            throw new IllegalArgumentException(
                    "The number of localizations (" + localizations.size() + ") cannot be less than 1");

        if (localizations.contains(null))
            throw new IllegalArgumentException("A localization cannot be null");

        for (int i = 0; i < localizations.size() - 1; i++)
            for (int j = i + 1; j < localizations.size(); j++)
                if (localizations.get(i).equals(localizations.get(j)))
                    throw new IllegalArgumentException("All localizations must be unique");
    }

    /**
     * Comprueba las precondiciones para las horas de juego, el número de partidos por jugador y la duración de un
     * partido
     *
     * @param timeslots          lista de horas de juego, no null y con más de un elemento
     * @param nTimeslotsPerMatch número de timeslots por partido, superior a 1
     * @param nMatchesPerPlayer  número de partidos por jugador, superior a 1
     * @throws IllegalArgumentException si alguna precondición falla (ver {@link #Event(String, List, List, List)})
     */
    private void checkTimeslotsPreconditions(List<Timeslot> timeslots, int nTimeslotsPerMatch, int nMatchesPerPlayer) {
        if (timeslots == null)
            throw new IllegalArgumentException("The list of timeslots cannot be null");

        if (timeslots.size() < 1)
            throw new IllegalArgumentException(
                    "The number of timeslots (" + timeslots.size() + ") cannot be less than 1");

        if (timeslots.contains(null))
            throw new IllegalArgumentException("A timeslot cannot be null");

        if (nTimeslotsPerMatch < 1)
            throw new IllegalArgumentException(
                    "The number of timeslots per match (" + nTimeslotsPerMatch + ") cannot be less than 1");

        if (nMatchesPerPlayer < 1)
            throw new IllegalArgumentException(
                    "The number of matches per player (" + nMatchesPerPlayer + ") cannot be less than 1");

        if (timeslots.size() < nMatchesPerPlayer * nTimeslotsPerMatch)
            throw new IllegalArgumentException("The number of timeslots (" + timeslots.size() +
                    ") cannot be less than the product of the number of matches per player and the duration of a " +
                    "match");

        for (int i = 0; i < timeslots.size() - 1; i++)
            for (int j = i + 1; j < timeslots.size(); j++)
                if (timeslots.get(i).equals(timeslots.get(j)))
                    throw new IllegalArgumentException("All timeslots must be unique");

        for (int i = 0; i < timeslots.size() - 1; i++)
            if (timeslots.get(i).compareTo(timeslots.get(i + 1)) < 1)
                throw new IllegalArgumentException(
                        "Timeslot (" + timeslots.get(i) + ") must be greater than the next one (" +
                                timeslots.get(i + 1) + ")");
    }

    @SuppressWarnings("unchecked")
    public <T> void setValidator(Validator<T> validator) {
        if (validator == null)
            throw new IllegalArgumentException("The parameter cannot be null");

        this.validator = (Validator<Event>) validator;
    }

    public List<String> getMessages() {
        return validator.getValidationMessages();
    }

    public void validate() throws ValidationException {
        if (!validator.validate(this))
            throw new ValidationException(String.format("Validation has failed for this event (%s)", name));
    }

    /**
     * Asigna el nombre del torneo. Si es null se lanza IllegalArgumentException
     *
     * @param name una cadena no nula que representa el nombre del torneo. Se lanzará excepción si la cadena es null
     */
    public void setName(String name) {
        if (name == null)
            throw new IllegalArgumentException("Name cannot be null");

        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setTournament(Tournament tournament) {
        this.tournament = tournament;
    }

    public Tournament getTournament() {
        return tournament;
    }

    /**
     * Devuelve la lista de jugadores como una lista no modificable
     *
     * @return la lista de jugadores envuelta en un wrapper que la hace no modificable
     */
    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    /**
     * Devuelve la lista de localizaciones como una lista no modificable
     *
     * @return la lista de localizaciones envuelta en un wrapper que la hace no modificable
     */
    public List<Localization> getLocalizations() {
        return Collections.unmodifiableList(localizations);
    }

    /**
     * Devuelve la lista de timeslots como una lista no modificable
     *
     * @return la lista de timeslots envuelta en un wrapper que la hace no modificable
     */
    public List<Timeslot> getTimeslots() {
        return Collections.unmodifiableList(timeslots);
    }

    /**
     * Asigna el número de partidos por jugador. Además, elimina todos los emparejamientos predefinidos, si hubiese.
     *
     * @param matchesPerPlayer número de partidos por jugador, mínimo 1
     * @throws IllegalArgumentException si no se cumple alguna de las precondiciones de los timeslots (ver
     *                                  {@link #Event(String, List, List, List)})
     */
    public void setMatchesPerPlayer(int matchesPerPlayer) {
        checkTimeslotsPreconditions(timeslots, nTimeslotsPerMatch, matchesPerPlayer);

        predefinedMatchups.clear();

        nMatchesPerPlayer = matchesPerPlayer;
        if (matchesPerPlayer == 1)
            matchupMode = MatchupMode.ANY;
    }

    public int getMatchesPerPlayer() {
        return nMatchesPerPlayer;
    }

    /**
     * Asigna la duración de un partido, es decir, el número de <i>timeslots</i> sobre los que los partidos del
     * envento transcurren. Además, elimina todos los emparejamientos predefinidos, si hubiese.
     *
     * @param timeslotsPerMatch número de timeslots por partidos, superior a 1
     * @throws IllegalArgumentException si falla alguna precondición de timeslots (ver
     *                                  {@link #Event(String, List, List, List)})
     */
    public void setTimeslotsPerMatch(int timeslotsPerMatch) {
        checkTimeslotsPreconditions(timeslots, timeslotsPerMatch, nMatchesPerPlayer);

        predefinedMatchups.clear();

        nTimeslotsPerMatch = timeslotsPerMatch;
    }

    public int getTimeslotsPerMatch() {
        return nTimeslotsPerMatch;
    }

    /**
     * Asigna el número de jugadores por partido. Además, elimina todos los equipos existentes así como los
     * emparejamientos predefinidos, si hubiese.
     *
     * @param playersPerMatch número de jugadores por partido, superior a 1
     * @throws IllegalArgumentException si falla alguna de las precondiciones de jugadores (ver
     *                                  {@link #Event(String, List, List, List)})
     */
    public void setPlayersPerMatch(int playersPerMatch) {
        checkPlayersPreconditions(players, playersPerMatch);

        nPlayersPerMatch = playersPerMatch;

        teams.clear();
        predefinedMatchups.clear();

        if (playersPerMatch == 1)
            matchupMode = MatchupMode.ANY;
    }

    public int getPlayersPerMatch() {
        return nPlayersPerMatch;
    }

    /**
     * Asigna la lista de equipos que componen el evento.
     *
     * @param teams lista de equipos no nula, con jugadores pertenecientes a este evento, sin jugadores repetidos en
     *              distintos equipos y todos los equipos del mismo tamaño
     * @throws IllegalArgumentException si no se cumplen algunas de las siguientes precondiciones:
     *                                  <ul>
     *                                  <li>La lista de equipos no es nula
     *                                  <li>La lista de equipos tiene más de 2 elementos
     *                                  <li>Todos los jugadores pertenecen a este evento
     *                                  <li>Ningún jugador se encuentra repetido en distintos equipos y, por ende, no
     *                                  hay enfrentamientos repetidor
     *                                  <li>Todos los equipos tienen el mismo número de jugadores
     *                                  <li>El número total de jugadores que componen todos los equipos debe ser igual
     *                                  al número de jugadores del evento
     *                                  </ul>
     */
    public void setTeams(List<Team> teams) {
        checkTeamsPreconditions(teams);

        this.teams = teams;
    }

    /**
     * Comprueba las precondiciones de los equipos
     *
     * @param teams lista de equipos
     * @throws IllegalArgumentException si alguna de las precondiciones no se cumple, ver {@link #setTeams(List)}
     */
    private void checkTeamsPreconditions(List<Team> teams) {
        if (teams == null)
            throw new IllegalArgumentException("Teams cannot be null");

        if (teams.size() < 2)
            throw new IllegalArgumentException("There must be at least two teams");

        int playersPerTeam = teams.get(0).getPlayers().size();

        for (Team team : teams) {
            if (team == null)
                throw new IllegalArgumentException("A team cannot be null");

            int teamSize = team.getPlayers().size();
            if (teamSize != playersPerTeam)
                throw new IllegalArgumentException(
                        "All teams must have the same number of players (" + playersPerTeam + ", and this team has " +
                                teamSize + ")");

            for (Player player : team.getPlayers())
                if (!players.contains(player))
                    throw new IllegalArgumentException(
                            "The player (" + player + ") does not exist in the list of players of this event");
        }

        if (nPlayersPerMatch % playersPerTeam != 0)
            throw new IllegalArgumentException(String.format(
                    "Number of players in a team (%d) must be a divisor of the number of players per match (%d)",
                    playersPerTeam,
                    nPlayersPerMatch
            ));

        for (int i = 0; i < teams.size() - 1; i++)
            for (Player player : teams.get(i).getPlayers())
                for (int j = i + 1; j < teams.size(); j++)
                    if (teams.get(j).getPlayers().contains(player))
                        throw new IllegalArgumentException("A player (" + player + ") can only be present " +
                                "in one team (player is in " + teams.get(i) + " and " + teams.get(j) + ")");
    }

    /**
     * Comprueba las precondiciones de un equipo
     *
     * @param team un equipo de jugadores
     * @throws IllegalArgumentException si alguna de las precondiciones no se cumple, ver {@link #setTeams(List)},
     *                                  aquellas reglas concernientes a un único equipo, y derivables:
     *                                  <ul>
     *                                  <li>El número de jugadores del equipo es igual al número de equipos que
     *                                  conforman la lista existente (si hay alguno)
     *                                  <li>El equipo no puede existir ya, o ninguno de los jugadores que lo componen
     *                                  puede estar ya asignados a otro equipo
     *                                  </ul>
     */
    private void checkTeamPreconditions(Team team) {
        if (team == null)
            throw new IllegalArgumentException("Team cannot be null");

        for (Player player : team.getPlayers())
            if (!players.contains(player))
                throw new IllegalArgumentException(
                        "The player (" + player + ") does not exist in the list of players of this event");

        int playersInTeam = teams.isEmpty() ? team.getPlayers().size() : teams.get(0).getPlayers().size();

        if (!teams.isEmpty()) {
            if (team.getPlayers().size() != playersInTeam)
                throw new IllegalArgumentException(
                        "The number of players in the team (" + team.getPlayers().size() + ") must be the same");

            for (Player player : team.getPlayers())
                for (Team t : teams)
                    if (t.getPlayers().contains(player))
                        throw new IllegalArgumentException(
                                "A player (" + player + ") can only be present in one team (player is already in " + t +
                                        ")");
        }

        if (nPlayersPerMatch % playersInTeam != 0)
            throw new IllegalArgumentException(String.format(
                    "Number of players in a team (%d) must be a divisor of the number of players per match (%d)",
                    playersInTeam,
                    nPlayersPerMatch
            ));
    }

    /**
     * Devuelve la lista de equipos del torneo envuelta en un wrapper que la hace no modificable
     *
     * @return la lista de equipos del torneo, no modificable
     */
    public List<Team> getTeams() {
        return Collections.unmodifiableList(teams);
    }

    /**
     * Añade un equipo, si la composición del mismo no es nula, de dos jugadores o más y pertenecientes al evento.
     *
     * @param teamPlayers jugadores que compondrán el nuevo equipo a añadir y pertenecientes a este evento
     * @throws IllegalArgumentException si no se cumplen las precondiciones
     */
    public void addTeam(Player... teamPlayers) {
        if (teamPlayers == null)
            throw new IllegalArgumentException("Players cannot be null");

        Team team = new Team(teamPlayers);

        checkTeamPreconditions(team);

        teams.add(team);
    }

    /**
     * Añade un equipo
     *
     * @param team equipo no nulo
     * @throws IllegalArgumentException si no se cumplen las precondiciones
     */
    public void addTeam(Team team) {
        checkTeamPreconditions(team);

        teams.add(team);
    }

    /**
     * Elimina un equipo de la lista de equipos, si existe. Si no, no habrá modificaciones.
     *
     * @param team un equipo de jugadores
     */
    public void removeTeam(Team team) {
        teams.remove(team);
    }

    /**
     * Vacía el conjunto de equipos definidos sobre el evento.
     */
    public void clearTeams() {
        teams.clear();
    }

    /**
     * @return true si sobre la categoría se definen equipos explícitos, y false si no
     */
    public boolean hasTeams() {
        return !teams.isEmpty();
    }

    /**
     * Asigna el diccionario que define las horas en las que los jugadores no están disponibles. Es opcional, y no
     * todos los jugadores tienen por qué definir un conjunto de horas en las que no están disponibles.
     *
     * @param unavailability un diccionario que define sobre cada jugador un conjunto de horas en las que no está
     *                       disponible
     * @throws IllegalArgumentException si no se cumplen las precondiciones:
     *                                  <ul>
     *                                  <li>El diccionario no puede ser nulo
     *                                  <li>Todos los jugadores deben existir en la lista de jugadores del evento
     *                                  <li>El conjunto de horas no disponibles asociada a un jugador no puede ser
     *                                  null ni estar vacío
     *                                  <li>Cada hora del conjunto de horas no disponibles asociada a un jugador debe
     *                                  existir en el evento
     *                                  </ul>
     */
    public void setUnavailablePlayers(Map<Player, Set<Timeslot>> unavailability) {
        checkUnavailablePlayersPreconditions(unavailability);

        unavailablePlayers = unavailability;
    }

    /**
     * Comprueba las precondiciones del diccionario de horas en las que los jugadores no están disponibles
     *
     * @param unavailability diccionario no nulo
     * @throws IllegalArgumentException si no se cumple alguna precondición, ver
     *                                  {@linkplain #setUnavailableLocalizations(Map)}
     */
    private void checkUnavailablePlayersPreconditions(Map<Player, Set<Timeslot>> unavailability) {
        if (unavailability == null)
            throw new IllegalArgumentException("Map cannot be null");

        if (unavailability.containsKey(null))
            throw new IllegalArgumentException("A player cannot be null");

        for (Player player : unavailability.keySet()) {
            if (!players.contains(player))
                throw new IllegalArgumentException(
                        "The player (" + player + ") does not exist in the list of players of this event");

            Set<Timeslot> playerUnavaible = unavailability.get(player);
            if (playerUnavaible == null)
                throw new IllegalArgumentException("The set of unavailable timeslots for a player cannot be null");

            if (playerUnavaible.isEmpty())
                throw new IllegalArgumentException("The set of unavailable timeslots for a player cannot be empty");

            for (Timeslot timeslot : playerUnavaible)
                if (timeslot == null)
                    throw new IllegalArgumentException("The timeslot cannot be null");
                else if (!timeslots.contains(timeslot))
                    throw new IllegalArgumentException("The timeslot (" + timeslot + ") does not exist in this event");
        }
    }

    /**
     * Devuelve el diccionario de jugadores y horas no disponibles en un wrapper que la hace no modificable
     *
     * @return el diccionario de jugadores y sus horas no disponibles, no modificable
     */
    public Map<Player, Set<Timeslot>> getUnavailablePlayers() {
        return Collections.unmodifiableMap(unavailablePlayers);
    }

    /**
     * Marca al jugador como no disponible a una hora determinada.
     *
     * @param player   jugador que pertenece a este evento
     * @param timeslot hora perteneciente al dominio de este evento y no existente en el conjunto de horas no
     *                 disponibles del jugador
     * @throws IllegalArgumentException si no se cumple alguna precondición
     */
    public void addUnavailablePlayerAtTimeslot(Player player, Timeslot timeslot) {
        if (player == null || timeslot == null)
            throw new IllegalArgumentException("The parameters cannot be null");

        if (!players.contains(player))
            throw new IllegalArgumentException(
                    "The player (" + player + ") does not exist in the list of players of the event");

        if (!timeslots.contains(timeslot))
            throw new IllegalArgumentException(
                    "The timeslot (" + timeslot + ") doest not exist in the list of timeslots of the event");

        unavailablePlayers.computeIfAbsent(player, t -> new HashSet<>()).add(timeslot);
    }

    /**
     * Marca al jugador como no disponible en una serie de horas.
     *
     * @param player    jugador que pertenece a este evento
     * @param timeslots conjunto no vacío de horas, y todas ellas pertenecientes al dominio del evento
     * @throws IllegalArgumentException si no se cumple alguna precondición, ver
     *                                  {@link #addUnavailablePlayerAtTimeslot(Player, Timeslot)}
     */
    public void addUnavailablePlayerAtTimeslots(Player player, Set<Timeslot> timeslots) {
        if (player == null || timeslots == null)
            throw new IllegalArgumentException("The parameters cannot be null");

        for (Timeslot timeslot : timeslots)
            addUnavailablePlayerAtTimeslot(player, timeslot);
    }

    /**
     * Marca al jugador como no disponible en el rango de horas indicado, extremos incluidos.
     *
     * @param player jugador perteneciente al dominio del evento
     * @param t1     un extremo del rango de <i>timeslots</i>
     * @param t2     el otro extremo
     * @throws IllegalArgumentException si alguno de los argumentos son <code>null</code> o no existen en el dominio
     *                                  del evento
     */
    public void addUnavailablePlayerAtTimeslotRange(Player player, Timeslot t1, Timeslot t2) {
        if (player == null || t1 == null || t2 == null)
            throw new IllegalArgumentException("The parameters cannot be null");

        if (!players.contains(player))
            throw new IllegalArgumentException(String.format("Player (%s) does not exist in the list of players of "
                    + "this event",
                    player
            ));

        if (!timeslots.contains(t1))
            throw new IllegalArgumentException(String.format("Timeslot (%s) does not exist in the list of timeslots "
                    + "of this event",
                    t1
            ));

        if (!timeslots.contains(t2))
            throw new IllegalArgumentException(String.format("Timeslot (%s) does not exist in the list of timeslots "
                    + "of this event",
                    t2
            ));

        Timeslot start, end;
        if (t1.compareTo(t2) >= 0) {
            start = t1;
            end = t2;
        } else {
            start = t2;
            end = t1;
        }

        for (int t = timeslots.indexOf(start); t <= timeslots.indexOf(end); t++)
            addUnavailablePlayerAtTimeslot(player, timeslots.get(t));
    }

    /**
     * Si el jugador no está disponible a la hora timeslot, se elimina de la lista y vuelve a estar disponible a esa
     * hora.
     *
     * @param player   jugador que pertenece a este evento
     * @param timeslot hora perteneciente al dominio del evento
     * @throws IllegalArgumentException si no se cumple alguna precondición
     */
    public void removeUnavailablePlayerAtTimeslot(Player player, Timeslot timeslot) {
        if (player == null || timeslot == null)
            throw new IllegalArgumentException("The parameters cannot be null");

        if (!players.contains(player))
            throw new IllegalArgumentException(
                    "The player (" + player + ") does not exist in the list of players of the event");

        if (!timeslots.contains(timeslot))
            throw new IllegalArgumentException(
                    "The timeslot (" + timeslot + ") does not exist in the list of timeslots of the event");

        // Elimina el timeslot asociado al jugador, y si el conjunto queda vacío, se elimina la entrada del diccionario
        unavailablePlayers.computeIfPresent(player, (p, t) -> t.remove(timeslot) && t.isEmpty() ? null : t);
    }

    /**
     * Vacía el diccionario de jugadores no disponibles en <i>timeslots</i>
     */
    public void clearUnavailablePlayers() {
        unavailablePlayers.clear();
    }

    /**
     * Comprueba si hay jugadores no disponibles a ciertas horas
     *
     * @return <code>true</code> si hay algún jugador no disponible a algunas horas, de lo contrario <code>false</code>
     */
    public boolean hasUnavailablePlayers() {
        return !unavailablePlayers.isEmpty();
    }

    /**
     * Asigna el conjunto de enfrentamientos predefinidos entre jugadores del evento. Además, actualiza las horas de
     * juego asignadas a los jugadores y las localizaciones de juego asginadas a los mismos.
     * <p>
     * <p>Si el evento especifica más de un partido por jugador, para cada jugador que compone cada enfrentamiento, si
     * al jugador no se le han asignado localizaciones de juego u horas de juego concretas donde sus partidos deban
     * tener lugar, se le asignarán todas las localizaciones y/u horas de juego existentes en los dominios del evento
     * .</p>
     *
     * @param matchups una lista de múltiples enfrentamientos no repetidos entre jugadores del evento
     * @throws IllegalArgumentException si <code>matchups</code> es <code>null</code>
     * @throws IllegalArgumentException si el número de enfrentamientos de un jugador en particular supera el máximo
     *                                  posible, es decir, el número de partidos por jugador que este evento define
     */
    public void setPredefinedMatchups(Set<Matchup> matchups) {
        if (matchups == null)
            throw new IllegalArgumentException("Matchups cannot be null");

        for (Matchup matchup : matchups) {
            for (Player player : matchup.getPlayers()) {
                long count = matchups.stream()
                        .filter(m -> m.getPlayers().contains(player))
                        .mapToInt(Matchup::getOccurences)
                        .sum();

                if (count > nMatchesPerPlayer)
                    throw new IllegalArgumentException(String.format(
                            "Player's (%s) number of predefined matchups (%d) exceeds the limit (%d)",
                            player,
                            count,
                            nMatchesPerPlayer
                    ));

                Set<Localization> assignedLocalizations = matchup.getLocalizations();
                Set<Timeslot> assignedTimeslots = matchup.getTimeslots();

                if (nMatchesPerPlayer > 1) {
                    if (!playersInLocalizations.containsKey(player))
                        assignedLocalizations = new HashSet<>(localizations);
                    if (!playersAtTimeslots.containsKey(player))
                        assignedTimeslots =
                                new HashSet<>(timeslots.subList(0, timeslots.size() - nTimeslotsPerMatch + 1));
                }

                playersInLocalizations.computeIfAbsent(player, l -> new HashSet<>()).addAll(assignedLocalizations);
                playersAtTimeslots.computeIfAbsent(player, l -> new HashSet<>()).addAll(assignedTimeslots);
            }
        }

        this.predefinedMatchups = matchups;
    }

    /**
     * Devuelve la lista de emparejamientos fijos envuelta en un wrapper que la hace no modificable
     *
     * @return la lista no modificable de emparejamientos fijos del evento
     */
    public Set<Matchup> getPredefinedMatchups() {
        return Collections.unmodifiableSet(predefinedMatchups);
    }

    /**
     * Añade un enfrentamiento predefinido.
     * <p>
     * <p>Actualiza la asignación de localizaciones y horas de juego para los jugadores implicados en el
     * enfrentamiento, asociando a cada uno de ellos con dichas localizaciones y horas que el enfrentamiento
     * especifica como posibles.</p>
     * <p>
     * <p>Si el evento especifica más de un partido por jugador, para cada jugador que compone el enfrentamiento, si
     * al jugador no se le han asignado localizaciones de juego u horas de juego concretas donde sus partidos deban
     * tener lugar, se le asignarán todas las localizaciones y/u horas de juego existentes en los dominios del evento
     * .</p>
     *
     * @param matchup emparejamiento no <code>null</code> a añadir a los emparejamientos predefinidos del evento
     * @throws IllegalArgumentException si <code>matchup</code> es <code>null</code>
     */
    public void addMatchup(Matchup matchup) {
        if (matchup == null)
            throw new IllegalArgumentException("Matchup cannot be null");

        predefinedMatchups.add(matchup);

        matchup.getPlayers().forEach(player -> {
            Set<Localization> assignedLocalizations = matchup.getLocalizations();
            Set<Timeslot> assignedTimeslots = matchup.getTimeslots();

            if (nMatchesPerPlayer > 1) {
                if (!playersInLocalizations.containsKey(player))
                    assignedLocalizations = new HashSet<>(localizations);
                if (!playersAtTimeslots.containsKey(player))
                    assignedTimeslots = new HashSet<>(timeslots.subList(0, timeslots.size() - nTimeslotsPerMatch + 1));
            }

            playersInLocalizations.computeIfAbsent(player, l -> new HashSet<>()).addAll(assignedLocalizations);
            playersAtTimeslots.computeIfAbsent(player, l -> new HashSet<>()).addAll(assignedTimeslots);
        });
    }

    /**
     * Añade un enfrentamiento predefinido que ocurrirá una vez, entre los jugadores indicados, en cualquiera de las
     * localizaciones y cualquiera de los <i>timeslots</i> del evento si no tiene ninguna localización y/u hora
     * asignada, o en alguna de las asignadas, si tiene.
     * <p>
     * <p>Actualiza la asignación a jugadores de localizaciones y horas de juego del mismo modo que describe el
     * método {@link Event#addMatchup(Matchup)}.</p>
     *
     * @param players conjunto de jugadores entre los que ocurrirá un enfrentamiento predefinido
     */
    public void addMatchup(Set<Player> players) {
        Set<Localization> assignedLocalizations = new HashSet<>();
        Set<Timeslot> assignedTimeslots = new HashSet<>();

        players.forEach(player -> {
            if (playersInLocalizations.containsKey(player))
                assignedLocalizations.addAll(playersInLocalizations.get(player));

            if (playersAtTimeslots.containsKey(player))
                assignedTimeslots.addAll(playersAtTimeslots.get(player));
        });

        if (assignedLocalizations.isEmpty())
            assignedLocalizations.addAll(localizations);

        if (assignedTimeslots.isEmpty())
            assignedTimeslots.addAll(timeslots);

        addMatchup(new Matchup(this, players, assignedLocalizations, new HashSet<>(assignedTimeslots), 1));
    }

    /**
     * Añade un enfrentamiento predefinido que ocurrirá una vez, entre los jugadores indicados, en cualquiera de las
     * localizaciones y cualquier de los <i>timeslots</i> del evento si no tiene ninguna localización y/u hora
     * asignada, o en alguna de las asignadas, si tiene.
     * <p>
     * <p>Actualiza la asignación a jugadores de localizaciones y horas de juego del mismo modo que describe el
     * método {@link Event#addMatchup(Matchup)}.</p>
     *
     * @param players jugadores entre los que tendrá lugar el enfrentamiento
     */
    public void addMatchup(Player... players) {
        addMatchup(new HashSet<>(Arrays.asList(players)));
    }

    /**
     * Añade un enfrentamiento predefinido que ocurrirá una vez, entre los jugadores de los equipos indicados, en
     * cualquiera de las localizaciones y cualquiera de los <i>timeslots</i> del evento si no tiene ninguna
     * localización y/u hora asignada, o en alguna de las asignadas, si tiene.
     * <p>
     * <p>Actualiza la asignación a jugadores de localizaciones y horas de juego del mismo modo que describe el
     * método {@link Event#addMatchup(Matchup)}.</p>
     *
     * @param teams conjunto de equipos entre los que ocurrirá un enfrentamiento predefinido
     */
    public void addTeamMatchup(Set<Team> teams) {
        addMatchup(new Matchup(
                this,
                teams.stream().map(Team::getPlayers).flatMap(Collection::stream).collect(Collectors.toSet()),
                new HashSet<>(localizations),
                new HashSet<>(timeslots),
                1
        ));
    }

    /**
     * Elimina un enfrentamiento predefinido. Si el enfrentamiento no existe, no se produce ninguna modificación. Las
     * asginaciones de localizaciones y horas de juego permanecerán, así que su eliminación debe ser ejecutada
     * explícitamente.
     *
     * @param matchup un conjunto de jugadores a eliminar de la lista, si existe
     */
    public void removeMatchup(Matchup matchup) {
        predefinedMatchups.remove(matchup);
    }

    /**
     * Define todos los enfrentamientos predefinidos entre los jugadores indicados, si existe alguno. Las
     * asginaciones de localizaciones y horas de juego permanecerán, así que su eliminación debe ser ejecutada
     * explícitamente.
     *
     * @param players jugadores para los que se borrarán los enfrentamientos predefinidos entre sí
     */
    public void removeMatchup(Set<Player> players) {
        predefinedMatchups.removeIf(m -> m.getPlayers().equals(players));
    }

    /**
     * Elimina un enfrentamiento fijo entre equipos. Si el enfrentamiento no existe, no se produce ninguna
     * modificación. Las asginaciones de localizaciones y horas de juego permanecerán, así que su eliminación debe
     * ser ejecutada explícitamente.
     *
     * @param teams un conjunto de equipos cuyo enfrentamiento se eliminará de la lista, si existe
     */
    public void removeTeamsMatchup(Set<Team> teams) {
        if (teams != null)
            removeMatchup(teams.stream().map(Team::getPlayers).flatMap(Collection::stream).collect(Collectors.toSet()));
    }

    /**
     * Limpia el conjunto de enfrentamientos predefinidos.
     */
    public void clearPredefinedMatchups() {
        predefinedMatchups.clear();
    }

    /**
     * Comprueba si este evento tiene emparejamientos predefinidos
     *
     * @return true si sobre el evento se han definido enfrentamientos fijos predefinidos, y false si no
     */
    public boolean hasPredefinedMatchups() {
        return !predefinedMatchups.isEmpty();
    }

    /**
     * Asigna la lista de horas que son descansos o <i>breaks</i>. Se ignoran elementos repetidos.
     *
     * @param breaks lista no nula de horas existentes en el torneo que serán interpretadas como breaks
     * @throws IllegalArgumentException si la lista es nula o contiene horas que no existen en este evento
     */
    public void setBreaks(List<Timeslot> breaks) {
        if (breaks == null)
            throw new IllegalArgumentException("The list of breaks cannot be null");

        if (breaks.contains(null))
            throw new IllegalArgumentException("A break cannot be null");

        for (int i = 0; i < breaks.size() - 1; i++)
            for (int j = i + 1; j < breaks.size(); j++)
                if (breaks.get(i) == breaks.get(j))
                    throw new IllegalArgumentException("Break cannot be repeated");

        if (!timeslots.containsAll(breaks))
            throw new IllegalArgumentException("All break timeslots must exist in the list of timeslots of this event");

        this.breaks = new ArrayList<>(new HashSet<>(breaks));
    }

    /**
     * Devuelve la lista no modificable de horas del evento que representan un break o descanso
     *
     * @return lista de horas del evento envuelta en un wrapper que la hace no modificable
     */
    public List<Timeslot> getBreaks() {
        return Collections.unmodifiableList(breaks);
    }


    /**
     * Añade una hora (timeslot) a la lista de <i>breaks</i>. Si ya existe, no habrá modificaciones.
     *
     * @param timeslotBreak una hora del evento que no exista ya en la lista de breaks
     * @throws IllegalArgumentException si no se cumplen todas las precondiciones
     */
    public void addBreak(Timeslot timeslotBreak) {
        if (timeslotBreak == null)
            throw new IllegalArgumentException("The timeslot break cannot be null");

        if (!timeslots.contains(timeslotBreak))
            throw new IllegalArgumentException("The timeslot (" + timeslotBreak + ") does not exist in this event");

        breaks.add(timeslotBreak);
    }

    /**
     * Añade <i>breaks</i> en el rango indicado, extremos incluidos.
     *
     * @param t1 un extremo del rango
     * @param t2 el otro extremo del rango
     */
    public void addBreakRange(Timeslot t1, Timeslot t2) {
        if (t1 == null || t2 == null)
            throw new IllegalArgumentException("The parameters cannot be null");

        if (!timeslots.contains(t1))
            throw new IllegalArgumentException(String.format("Timeslot (%s) does not exist in the list of timeslots "
                    + "of this event",
                    t1
            ));

        if (!timeslots.contains(t2))
            throw new IllegalArgumentException(String.format("Timeslot (%s) does not exist in the list of timeslots "
                    + "of this event",
                    t2
            ));

        Timeslot start, end;
        if (t1.compareTo(t2) >= 0) {
            start = t1;
            end = t2;
        } else {
            start = t2;
            end = t1;
        }

        for (int t = timeslots.indexOf(start); t <= timeslots.indexOf(end); t++)
            addBreak(timeslots.get(t));
    }


    /**
     * Elimina un break, es decir, la hora se considerará como una hora regular de juego. Si la hora no existe en la
     * lista de breaks, no habrá modificaciones.
     *
     * @param timeslotBreak una hora del evento
     */
    public void removeBreak(Timeslot timeslotBreak) {
        breaks.remove(timeslotBreak);
    }

    /**
     * Limpia la lista de <i>timeslots</i> que son descansos o <i>breaks</i>.
     */
    public void clearBreaks() {
        breaks.clear();
    }

    /**
     * Comprueba si un timeslot es un break.
     *
     * @param timeslot hora perteneciente al conjunto del evento
     * @return <code>true</code> si es break, <code>false</code> si no
     * @throws IllegalArgumentException si no se cumplen las precondiciones
     */
    public boolean isBreak(Timeslot timeslot) {
        if (timeslot == null)
            throw new IllegalArgumentException("The timeslot cannot be null");

        if (!timeslots.contains(timeslot))
            throw new IllegalArgumentException("The timeslot (" + timeslot + ") does not exist in this event");

        return breaks.contains(timeslot);
    }

    /**
     * Comprueba si este evento tiene breaks
     *
     * @return true si tiene breaks o false si no
     */
    public boolean hasBreaks() {
        return !breaks.isEmpty();
    }

    /**
     * Asigna las horas a las que determinadas localizaciones de juego no están disponibles para que un partido
     * discurra sobre ellas.
     *
     * @param unavailableLocalizations diccionario no nulo de localizaciones y las horas a las que no están disponibles
     * @throws IllegalArgumentException si no se cumplen las siguientes precondiciones:
     *                                  <ul>
     *                                  <li>El diccionario no es null
     *                                  <li>Todas las localizaciones existen en el dominio del evento
     *                                  <li>Todas las horas asignadas a cada localización pertenecen al dominio del
     *                                  evento
     *                                  </ul>
     */
    public void setUnavailableLocalizations(Map<Localization, Set<Timeslot>> unavailableLocalizations) {
        if (unavailableLocalizations == null)
            throw new IllegalArgumentException("The dictionary of unavailable localizations cannot be null");

        if (unavailableLocalizations.containsKey(null))
            throw new IllegalArgumentException("A localization cannot be null");

        for (Localization localization : unavailableLocalizations.keySet()) {
            if (!localizations.contains(localization))
                throw new IllegalArgumentException(
                        "The localization (" + localization + ") does not exist in this event");

            Set<Timeslot> unavailableTimeslot = unavailableLocalizations.get(localization);
            if (unavailableTimeslot == null)
                throw new IllegalArgumentException(
                        "The unavailable timeslots set for the localization (" + localization + ") cannot be null");

            for (Timeslot timeslot : unavailableTimeslot)
                if (!timeslots.contains(timeslot))
                    throw new IllegalArgumentException("The timeslot (" + timeslot + ") does not exist in this event");
        }

        this.unavailableLocalizations = unavailableLocalizations;
    }

    /**
     * Devuelve el mapa de localizaciones no disponibles a las horas especificadas.
     *
     * @return diccionario no modificable
     */
    public Map<Localization, Set<Timeslot>> getUnavailableLocalizations() {
        return Collections.unmodifiableMap(unavailableLocalizations);
    }

    /**
     * Marca como inválida o no disponible una localización de juego a una hora determinada.
     *
     * @param localization localización perteneciente al conjunto de localizaciones del evento
     * @param timeslot     hora perteneciente al conjunto de horas en las que el evento discurre
     * @throws IllegalArgumentException si alguno de los parámetros es <code>null</code>
     * @throws IllegalArgumentException si la localización no existe en el evento
     * @throws IllegalArgumentException si el <i>timeslot</i> no existe en el evento
     */
    public void addUnavailableLocalizationAtTimeslot(Localization localization, Timeslot timeslot) {
        if (localization == null || timeslot == null)
            throw new IllegalArgumentException("The parameters cannot be null");

        if (!localizations.contains(localization))
            throw new IllegalArgumentException("The localization (" + localization + ") does not exist in this event");

        if (!timeslots.contains(timeslot))
            throw new IllegalArgumentException("The timeslot (" + timeslot + ") does not exist in this event");

        unavailableLocalizations.computeIfAbsent(localization, t -> new HashSet<>()).add(timeslot);
    }

    /**
     * Marca como inválida o no disponible una localización de juego a un conjunto de horas determinado.
     *
     * @param localization localización perteneciente al conjunto de localizaciones del evento
     * @param timeslots    conjunto de horas pertenecientes al dominio del evento y no existentes en el conjunto de
     *                     horas no
     *                     disponibles de la localización
     * @throws IllegalArgumentException si alguno de los parámetros es <code>null</code>
     * @throws IllegalArgumentException si se da alguno de los casos ilegales que se describen en
     *                                  {@link Event#addUnavailableLocalizationAtTimeslot(Localization, Timeslot)}
     */
    public void addUnavailableLocalizationAtTimeslots(Localization localization, Set<Timeslot> timeslots) {
        if (localization == null || timeslots == null)
            throw new IllegalArgumentException("The parameters cannot be null");

        for (Timeslot timeslot : timeslots)
            addUnavailableLocalizationAtTimeslot(localization, timeslot);
    }

    /**
     * Marca como no disponibles las localizaciones en el rango de horas de juego indicadas, extremos incluidos.
     *
     * @param localization localización de juego del evento
     * @param t1           un extremo del rango de horas
     * @param t2           el otro extremo del rango de horas
     * @throws IllegalArgumentException si los parámetros son <code>null</code>
     * @throws IllegalArgumentException si se da alguno de los casos ilegales que se describen en
     *                                  {@link Event#addUnavailableLocalizationAtTimeslot(Localization, Timeslot)}
     */
    public void addUnavailableLocalizationAtTimeslotRange(Localization localization, Timeslot t1, Timeslot t2) {
        if (localization == null || t1 == null || t2 == null)
            throw new IllegalArgumentException("The parameters cannot be null");

        if (!localizations.contains(localization))
            throw new IllegalArgumentException(String.format("Localization (%s) does not exist in the list of " +
                    "localizations of this event",
                    localization
            ));

        if (!timeslots.contains(t1))
            throw new IllegalArgumentException(String.format("Timeslot (%s) does not exist in the list of timeslots "
                    + "of this event",
                    t1
            ));

        if (!timeslots.contains(t2))
            throw new IllegalArgumentException(String.format("Timeslot (%s) does not exist in the list of timeslots "
                    + "of this event",
                    t2
            ));

        Timeslot start, end;
        if (t1.compareTo(t2) >= 0) {
            start = t1;
            end = t2;
        } else {
            start = t2;
            end = t1;
        }

        for (int t = timeslots.indexOf(start); t <= timeslots.indexOf(end); t++)
            addUnavailableLocalizationAtTimeslot(localization, timeslots.get(t));
    }

    /**
     * Elimina la invalidez de una localización, si la hubiese, volviendo a estar disponible a cualquier hora. Si la
     * localización no estuviese no disponible a ninguna hora, no habrá modificaciones.
     *
     * @param localization localización perteneciente al conjunto de localizaciones del evento
     */
    public void removeUnavailableLocalization(Localization localization) {
        unavailableLocalizations.remove(localization);
    }

    /**
     * Elimina la invalidez de una localización a una hora, si la hubiese, volviendo a estar disponible a esa hora.
     * Si la hora no estaba disponible para esa pista, no habrá modificaciones.
     *
     * @param localization localización perteneciente al conjunto de localizaciones del evento
     * @param timeslot     hora perteneciente al conjunto de horas en las que el evento discurre
     */
    public void removeUnavailableLocalizationTimeslot(Localization localization, Timeslot timeslot) {
        if (localization == null || timeslot == null)
            throw new IllegalArgumentException("The parameters cannot be null");

        if (!localizations.contains(localization))
            throw new IllegalArgumentException("The localization (" + localization + ") does not exist in this event");

        if (!timeslots.contains(timeslot))
            throw new IllegalArgumentException("The timeslot (" + timeslot + ") does not exist in this event");

        unavailableLocalizations.computeIfPresent(localization, (l, t) -> t.remove(timeslot) && t.isEmpty() ? null : t);
    }

    /**
     * Vacía el diccionario de localizaciones de juego no disponibles en <i>timeslots</i>.
     */
    public void clearUnavailableLocalizations() {
        unavailableLocalizations.clear();
    }

    /**
     * Comprueba si hay localizaciones de juego no disponibles.
     *
     * @return <code>true</code> si hay alguna localización de juego no disponible, y <code>false</code> si no hay
     * ninguna
     */
    public boolean hasUnavailableLocalizations() {
        return !unavailableLocalizations.isEmpty();
    }

    /**
     * Asigna las localizaciones de juego donde los partidos de los jugadores indicados han de tener lugar.
     *
     * @param playersInLocalizations diccionario no nulo de jugadores pertenecientes al evento y localizaciones donde
     *                               jugarán,
     *                               existentes en este evento
     * @throws IllegalArgumentException si alguno de los parámetros es <code>null</code>
     * @throws IllegalArgumentException si alguno de los jugadores es <code>null</code>
     * @throws IllegalArgumentException si alguno de los jugadores no existe en el evento
     * @throws IllegalArgumentException si alguna de las localizacioens es <code>null</code>
     * @throws IllegalArgumentException si alguna de las localizaciones no existe en el evento
     */
    public void setPlayersInLocalizations(Map<Player, Set<Localization>> playersInLocalizations) {
        if (playersInLocalizations == null)
            throw new IllegalArgumentException("The parameter cannot be null");

        if (playersInLocalizations.containsKey(null))
            throw new IllegalArgumentException("A player cannot be null");

        for (Player player : playersInLocalizations.keySet()) {
            if (!players.contains(player))
                throw new IllegalArgumentException(
                        "The player (" + player + ") does not exist in the list of players of this event");

            Set<Localization> playerInLocalizations = playersInLocalizations.get(player);
            if (playerInLocalizations == null)
                throw new IllegalArgumentException("The localizations assigned to the player cannot be null");

            for (Localization localization : playerInLocalizations)
                if (!localizations.contains(localization))
                    throw new IllegalArgumentException(
                            "The localization (" + localization + ") does not exist in this event");
        }

        this.playersInLocalizations = playersInLocalizations;
    }

    /**
     * Devuelve el diccionario de jugadores a los que se les ha asignado localizaciones de juego donde sus partidos
     * deban tener lugar.
     *
     * @return diccionario no modificable
     */
    public Map<Player, Set<Localization>> getPlayersInLocalizations() {
        return Collections.unmodifiableMap(playersInLocalizations);
    }

    /**
     * Asigna al jugador una localización explícita donde ha de jugar. Si ya se ha asignado, no hay modificaciones.
     *
     * @param player       jugador perteneciente al conjunto de jugadores del evento
     * @param localization localización perteneciente al conjunto de localizaciones del evento
     * @throws IllegalArgumentException si alguno de los parámetros es <code>null</code>
     * @throws IllegalArgumentException si alguno de los jugadores no existe en el evento
     * @throws IllegalArgumentException si alguna de las localizaciones no existe en el evento
     */
    public void addPlayerInLocalization(Player player, Localization localization) {
        if (player == null || localization == null)
            throw new IllegalArgumentException("The parameters cannot be null");

        if (!players.contains(player))
            throw new IllegalArgumentException("The player (" + player + ") does not exist in the list of players of " +
                    "this event");

        if (!localizations.contains(localization))
            throw new IllegalArgumentException("The localization (" + localization + ") does not exist in this event");

        playersInLocalizations.computeIfAbsent(player, l -> new HashSet<>()).add(localization);
    }

    /**
     * Elimina de la configuración que el jugador deba jugar en la localización, si existe. Si no, no habrá
     * modificaciones.
     *
     * @param player       jugador perteneciente al conjunto de jugadores del evento
     * @param localization localización perteneciente al conjunto de localizaciones del evento
     */
    public void removePlayerInLocalization(Player player, Localization localization) {
        playersInLocalizations.computeIfPresent(player, (p, l) -> l.remove(localization) && l.isEmpty() ? null : l);
    }

    /**
     * Vacía el diccionario de jugadores en localizaciones de juego.
     */
    public void clearPlayersInLocalizations() {
        playersInLocalizations.clear();
    }

    /**
     * Comprueba si hay jugadores a los que se les han asignado las localizaciones donde jugar.
     *
     * @return true si sobre el evento hay definidas asignaciones fijas de jugadores sobre localizaciones
     */
    public boolean hasPlayersInLocalizations() {
        return !playersInLocalizations.isEmpty();
    }

    /**
     * Define las posibles horas a las que cada jugador (si se incluye en el diccionario) empezará su partido o
     * partidos. Se desecharán <i>timeslots</i> que pretendan indicar un comienzo de partido inválido, es decir, una
     * hora de juego situada al final del evento que produjese un partido fuera de rango del dominio.
     *
     * @param playersAtTimeslots diccionario de jugadores del evento a los que se les asigna las horas, existentes en
     *                           el evento, a las que sus partidos deben tener lugar
     * @throws IllegalArgumentException si no se cumplen las precondiciones
     */
    public void setPlayersAtTimeslots(Map<Player, Set<Timeslot>> playersAtTimeslots) {
        if (playersAtTimeslots == null)
            throw new IllegalArgumentException("The parameter cannot be null");

        if (playersAtTimeslots.containsKey(null))
            throw new IllegalArgumentException("A player cannot be null");

        for (Player player : playersAtTimeslots.keySet()) {
            if (!players.contains(player))
                throw new IllegalArgumentException(
                        "The player (" + player + ") does not exist in the list of players of this event");

            Set<Timeslot> playerAtTimeslots = playersAtTimeslots.get(player);
            if (playerAtTimeslots == null)
                throw new IllegalArgumentException("The timeslots assigned to the player cannot be null");

            for (Timeslot timeslot : playerAtTimeslots)
                if (!timeslots.contains(timeslot))
                    throw new IllegalArgumentException("The timeslot (" + timeslot + ") does not exist in this event");
        }

        playersAtTimeslots.values()
                .forEach(playerTimeslots -> playerTimeslots.removeIf(t -> timeslots.indexOf(t) + nTimeslotsPerMatch >
                        timeslots.size()));

        this.playersAtTimeslots = playersAtTimeslots;
    }

    /**
     * Devuelve el diccionario de jugadores a los que se les ha asignado horas donde sus partidos deban tener lugar.
     *
     * @return diccionario no modificable
     */
    public Map<Player, Set<Timeslot>> getPlayersAtTimeslots() {
        return Collections.unmodifiableMap(playersAtTimeslots);
    }

    /**
     * Comprueba las precondiciones para la asignación de una hora a la que un jugador debe jugar.
     *
     * @param player   jugador del evento
     * @param timeslot hora del evento, no asignada aún
     * @throws IllegalArgumentException si no se cumplen las precondiciones
     */
    private void checkPlayerAtTimeslotPreconditions(Player player, Timeslot timeslot) {
        if (player == null)
            throw new IllegalArgumentException("Player cannot be null");

        if (timeslot == null)
            throw new IllegalArgumentException("Timeslot cannot be null");

        if (!players.contains(player))
            throw new IllegalArgumentException("The player (" + player + ") does not exist in the list of players of " +
                    "this event");

        if (!timeslots.contains(timeslot))
            throw new IllegalArgumentException("The timeslot (" + timeslot + ") does not exist in this event");
    }

    /**
     * Asigna al jugador al timeslot explícito donde han de comenzar sus partidos. Si es un <i>timeslot</i> donde no
     * puede comenzar un partido, o si ya ha sido asignado al jugador, no habrá modificaciones.
     *
     * @param player   jugador perteneciente al conjunto de jugadores del evento
     * @param timeslot hora perteneciente al conjunto de horas en las que el evento discurre
     * @throws IllegalArgumentException si no se cumplen las precondiciones
     */
    public void addPlayerAtTimeslot(Player player, Timeslot timeslot) {
        checkPlayerAtTimeslotPreconditions(player, timeslot);

        if (timeslots.indexOf(timeslot) + nTimeslotsPerMatch <= timeslots.size())
            playersAtTimeslots.computeIfAbsent(player, t -> new HashSet<>()).add(timeslot);
    }

    /**
     * Asigna al jugador los timeslots explícitos donde sus partidos han de comenzar.
     *
     * @param player    jugador perteneciente al conjunto de jugadores del evento
     * @param timeslots conjunto de horas no asignadas pertenecientes al conjunto de horas en las que el evento discurre
     * @throws IllegalArgumentException si no se cumplen las precondiciones
     */
    public void addPlayerAtTimeslots(Player player, Set<Timeslot> timeslots) {
        if (timeslots == null)
            throw new IllegalArgumentException("Timeslots cannot be null");

        timeslots.forEach(t -> addPlayerAtTimeslot(player, t));
    }

    /**
     * Asigna al jugador un rango de <i>timeslot</i> donde deberán comenzar sus partidos.
     *
     * @param player un jugador del evento
     * @param t1     un extremo del rango de horas
     * @param t2     el otro extremo
     * @throws IllegalArgumentException si alguno de los parámetros son <code>null</code> o no pertenecen al dominio
     *                                  del evento
     */
    public void addPlayerAtTimeslotRange(Player player, Timeslot t1, Timeslot t2) {
        if (player == null || t1 == null || t2 == null)
            throw new IllegalArgumentException("The parameters cannot be null");

        if (!players.contains(player))
            throw new IllegalArgumentException(String.format("Player (%s) does not exist in the list of players of "
                    + "this event",
                    player
            ));

        if (!timeslots.contains(t1))
            throw new IllegalArgumentException(String.format("Timeslot (%s) does not exist in the list of timeslots "
                    + "of this event",
                    t1
            ));

        if (!timeslots.contains(t2))
            throw new IllegalArgumentException(String.format("Timeslot (%s) does not exist in the list of timeslots "
                    + "of this event",
                    t2
            ));

        Timeslot start, end;
        if (t1.compareTo(t2) >= 0) {
            start = t1;
            end = t2;
        } else {
            start = t2;
            end = t1;
        }

        for (int t = timeslots.indexOf(start); t <= timeslots.indexOf(end); t++)
            addPlayerAtTimeslot(player, timeslots.get(t));
    }

    /**
     * Asigna a los jugadores los timeslots explícitos donde han de comenzar sus partidos.
     *
     * @param players   jugadores pertenecientes al evento
     * @param timeslots conjunto de horas no asignadas pertenecientes al conjunto de horas en las que el evento discurre
     * @throws IllegalArgumentException si no se cumplen las precondiciones
     */
    public void addPlayersAtTimeslots(Set<Player> players, Set<Timeslot> timeslots) {
        if (players == null || timeslots == null)
            throw new IllegalArgumentException("The parameters cannot be null");

        players.forEach(p -> addPlayerAtTimeslots(p, timeslots));
    }


    /**
     * Elimina de la configuración que el jugador deba jugar a la hora indicada, si ha sido asignada.
     *
     * @param player   jugador perteneciente al conjunto de jugadores del evento
     * @param timeslot hora perteneciente al conjunto de horas en las que el evento discurre
     */
    public void removePlayerAtTimeslot(Player player, Timeslot timeslot) {
        playersAtTimeslots.computeIfPresent(player, (p, t) -> t.remove(timeslot) && t.isEmpty() ? null : t);
    }

    /**
     * Elimina de la configuración que el jugador deba jugar a las horas indicadas, si han sido asignadas.
     *
     * @param player    jugador perteneciente al conjunto de jugadores del evento
     * @param timeslots conjunto de horas pertenecientes al conjunto de horas en las que el evento discurre
     * @throws IllegalArgumentException si no se cumplen las precondiciones
     */
    public void removePlayerAtTimeslots(Player player, Set<Timeslot> timeslots) {
        if (timeslots == null)
            throw new IllegalArgumentException("Timeslots cannot be null");

        timeslots.forEach(t -> removePlayerAtTimeslot(player, t));
    }

    /**
     * Comprueba si hay jugadores a los que se les han asignado horas de juego fijas.
     *
     * @return true si sobre el evento hay definidas asignaciones fijas de jugadores sobre timeslots
     */
    public boolean hasPlayersAtTimeslots() {
        return !playersAtTimeslots.isEmpty();
    }

    /**
     * Vacía el diccionario de jugadores en <i>timeslots</i>.
     */
    public void clearPlayersAtTimeslots() {
        playersAtTimeslots.clear();
    }

    /**
     * Asigna el modo de emparejamiento de este evento, sólo si el número de partidos por jugador es superior a uno y
     * el número de jugadores por partido es superior a uno.
     *
     * @param matchupMode modo de emparejamiento, no null
     */
    public void setMatchupMode(MatchupMode matchupMode) {
        if (matchupMode == null)
            throw new IllegalArgumentException("Matchup mode cannot be null");

        if (nMatchesPerPlayer > 1 && nPlayersPerMatch > 1)
            this.matchupMode = matchupMode;
        else
            this.matchupMode = MatchupMode.ANY;
    }

    public MatchupMode getMatchupMode() {
        return matchupMode;
    }


    /**
     * Devuelve el equipo al que el jugador pertenece, si el jugador existe en el evento y si pertenece a algún equipo.
     *
     * @param player jugador no nulo perteneciente al evento
     * @return equipo al que pertenece el jugador, o <code>null</code> si no pertenece a ningún equipo o el jugador
     * no existe en este evento
     */
    public Team filterTeamByPlayer(Player player) {
        return teams.stream().filter(team -> team.getPlayers().contains(player)).limit(1).findFirst().orElse(null);
    }

    /**
     * Comprueba si el jugador está disponible a una hora determinada.
     *
     * @param player   jugador del evento
     * @param timeslot hora perteneciente al evento
     * @return <code>true</code> si el jugador está disponible a esa hora, <code>false</code> si no
     */
    public boolean isPlayerUnavailable(Player player, Timeslot timeslot) {
        return unavailablePlayers.containsKey(player) && unavailablePlayers.get(player).contains(timeslot);
    }

    /**
     * Comprueba si la localización está invalidada a una hora determinada.
     *
     * @param localization localización de juego de este evento
     * @param timeslot     hora perteneciente al evento
     * @return <code>true</code> si la localización no está disponible a la hora indicada, <code>false</code> si está
     * disponible
     */
    public boolean isLocalizationUnavailable(Localization localization, Timeslot timeslot) {
        return unavailableLocalizations.containsKey(localization) &&
                unavailableLocalizations.get(localization).contains(timeslot);
    }

    /**
     * Devuelve el número total de partidos que se jugarán en este evento.
     *
     * @return número de partidos del evento
     */
    public int getNumberOfMatches() {
        return players.size() / nPlayersPerMatch * nMatchesPerPlayer;
    }

    /**
     * Devuelve el número total de <i>timeslots</i> que se ocuparán con partidos.
     *
     * @return número de horas ocupadas, mayor que 0
     */
    public int getNumberOfOccupiedTimeslots() {
        return players.size() * nMatchesPerPlayer * nTimeslotsPerMatch;
    }

    public String toString() {
        return name;
    }
}
