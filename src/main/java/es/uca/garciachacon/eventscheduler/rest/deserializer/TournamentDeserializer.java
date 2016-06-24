package es.uca.garciachacon.eventscheduler.rest.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import es.uca.garciachacon.eventscheduler.data.model.tournament.*;
import es.uca.garciachacon.eventscheduler.solver.TournamentSolver;

import java.io.IOException;
import java.util.*;

/**
 * Deserializador de un torneo, representado por {@link Tournament}. El formato del cuerpo JSON esperado contiene
 * cinco campos obligatorios.
 * <p>
 * El primer campo se trata del <strong>nombre</strong>, bajo el nombre de campo <code>"name"</code>. Debe ser
 * textual.
 * <p>
 * El segundo campo es una lista de todos los <strong>jugadores</strong> de los que el torneo se compone, es decir,
 * la suma de todos los jugadores únicos que componen los eventos o categorías del torneo. El formato de cada jugador
 * debe ser como describe el deserializador {@link PlayerDeserializer}. El nombre del campo es <code>"players"</code>.
 * <p>
 * El tercer campo es una lista de todos las <strong>localizaciones</strong> de juego del torneo. El formato de cada
 * localización debe ser el descrito en {@link LocalizationDeserializer}. El nombre del campo es
 * <code>"localizations"</code>.
 * <p>
 * El cuarto campo es una lista de todos los <strong><i>timeslots</i></strong> del torneo. Al igual que los jugadores
 * y las localizaciones, se trata de la suma de los <i>timeslots</i> únicos de cada categoría. Cada <i>timeslot</i>
 * debe tener cualquier formato de los especificados por {@link TimeslotDeserializer}. El nombre del campo es
 * <code>"timeslots"</code>.
 * <p>
 * El quinto campo, y último, es una lista de los <strong>eventos</strong> o categorías que componen el torneo. Cada
 * objeto evento en la lista tendrá un complejo formato que se describirá más adelante. Debe haber al menos un
 * evento. El nombre del campo es <code>"events"</code>.
 * <p>
 * En resumen, el formato esperado para deserializar un torneo es el siguiente:
 * <p>
 * <pre>
 * {
 *   "name" : "Tournament",
 *   "players" : [ {
 *     "name" : "Player 1"
 *   }, {
 *     "name" : "Player 2"
 *   }, {
 *     "name" : "Player 3"
 *   }, {
 *     "name" : "Player 4"
 *   }, {
 *     "name" : "Player 5"
 *   }, {
 *     "name" : "Player 6"
 *   } ],
 *   "localizations" : [ {
 *     "name" : "Court 1"
 *   }, {
 *     "name" : "Court 2"
 *   }, {
 *     "name" : "Court 3"
 *   } ],
 *   "timeslots" : [ {
 *     "name" : "Timeslot [order=0]",
 *     "chronologicalOrder" : 0
 *   }, {
 *     "name" : "Timeslot [order=1]",
 *     "chronologicalOrder" : 1
 *   }, {
 *     "name" : "Timeslot [order=2]",
 *     "chronologicalOrder" : 2
 *   }, {
 *     "name" : "Timeslot [order=3]",
 *     "chronologicalOrder" : 3
 *   }, {
 *     "name" : "Timeslot [order=4]",
 *     "chronologicalOrder" : 4
 *   } ],
 *     "events" : [ {
 *     "name" : "Event"
 *   } ]
 * }
 * </pre>
 * <p>
 * A continuación se especifica el formato requerido para cada objeto evento de la lista <code>"events"</code>.
 * <p>
 * El <strong>nombre</strong> del evento se asocia al campo <code>"name"</code> y es obligatorio.
 * <p>
 * Los <strong>jugadores, localizaciones y <i>timeslots</i></strong> del evento se especifican mediante listas cuyo
 * contenido incluyen referencias a los jugadores, localizaciones y <i>timeslots</i> de los campos
 * <code>"players"</code>, <code>"localizations"</code> y <code>"timeslots"</code> del torneo. Las referencias en las
 * listas respectivas son las posiciones de los elementos. El nombre de los atributos en el objeto evento para estos
 * tres elementos es el mismo: <code>"players"</code>, <code>"localizations"</code> y <code>"timeslots"</code>.
 * <p>
 * En el siguiente ejemplo, los jugadores del evento único del torneo se tratarían del primero y del tercero de los
 * jugadores en la lista de todos los jugadores del evento, las localizaciones serían la segunda y tercera, y los
 * <i>timeslots</i> los cuatro primeros:
 * <p>
 * <pre>
 * {
 *   "name" : "Tournament",
 *   "players" : [ {
 *     "name" : "Player 1"
 *   }, {
 *     "name" : "Player 2"
 *   }, {
 *     "name" : "Player 3"
 *   }, {
 *     "name" : "Player 4"
 *   }, {
 *     "name" : "Player 5"
 *   }, {
 *     "name" : "Player 6"
 *   } ],
 *   "localizations" : [ {
 *     "name" : "Court 1"
 *   }, {
 *     "name" : "Court 2"
 *   }, {
 *     "name" : "Court 3"
 *   } ],
 *   "timeslots" : [ {
 *     "name" : "Timeslot [order=0]",
 *     "chronologicalOrder" : 0
 *   }, {
 *     "name" : "Timeslot [order=1]",
 *     "chronologicalOrder" : 1
 *   }, {
 *     "name" : "Timeslot [order=2]",
 *     "chronologicalOrder" : 2
 *   }, {
 *     "name" : "Timeslot [order=3]",
 *     "chronologicalOrder" : 3
 *   }, {
 *     "name" : "Timeslot [order=4]",
 *     "chronologicalOrder" : 4
 *   } ],
 *     "events" : [ {
 *     "name" : "Event",
 *     "players": [ 0, 2 ],
 *     "localizations": [ 1, 2 ],
 *     "timeslots": [ 0, 1, 2, 3 ]
 *   } ]
 * }
 * </pre>
 * <p>
 * Si el evento no incluye uno o varios de estos tres campos, automáticamente se asignarán al evento todos los
 * jugadores y/o localizaciones y/o <i>timeslots</i> del torneo, como fue el caso en el primer ejemplo.
 * <p>
 * Se puede indicar el <strong>número de partidos por jugador</strong> del evento mediante el campo
 * <code>"matchesPerPlayer"</code>:
 * <pre>
 * {
 *   "matchesPerPlayer": 2
 * }
 * </pre>
 * <p>
 * El <strong>número de <i>timeslots</i> por partidos</strong> es otro campo adicional y se incluye como sigue:
 * <pre>
 * {
 *   "timeslotsPerMatch": 1
 * }
 * </pre>
 * <p>
 * Para definir el <strong>número de jugadores por partido</strong>:
 * <pre>
 * {
 *   "playersPerMatch": 2
 * }
 * </pre>
 * <p>
 * Y el <strong>número de jugadores por equipo</strong>:
 * <p>
 * <pre>
 * {
 *   "playersPerTeam": 3
 * }
 * </pre>
 * <p>
 * Para añadir <strong>equipos</strong> al evento se incluirá una lista de objetos con la propiedad opcional
 * <code>"name"</code> y el campo obligatorio <code>"players"</code> que contiene las referencias de los jugadores
 * que componen cada equipo. Las referencias funcionan de igual forma que en la lista de jugadores del evento: son
 * las posiciones de los jugadores en la lista de jugadores del torneo.
 * <p>
 * <pre>
 * "teams" : [ {
 *     "name" : "Player 3-Player 5",
 *     "players" : [ 4, 2 ]
 *   }, {
 *     "name" : "Player 2-Player 1",
 *     "players" : [ 0, 1 ]
 *   } ]
 * </pre>
 * <p>
 * Se pueden añadir <strong><i>breaks</i></strong> al evento usando el campo <code>"breaks"</code> y contendrá una
 * lista de las posiciones de los <i>timeslots</i> del evento que se corresponden con <i>breaks</i>.
 * <p>
 * <pre>
 * "breaks" : [ 3, 4 ]
 * </pre>
 * <p>
 * Las <strong>no disponibilidades de jugadores</strong> se indicarán con el campo <code>"unavailablePlayers"</code>,
 * al que se le asociará un objeto JSON, donde los campos clave son cadenas que indican la posición del jugador que
 * tiene no disponibilidades, y a cada una de estos campos clave (jugadores) se le asocian una lista de posiciones
 * que indican los <i>timeslots</i> a los que cada jugador no está disponible.
 * <p>
 * En el siguiente ejemplo, se indica que el quinto jugador no está disponible en los <i>timeslots</i> tercero y
 * cuarto, y que el tercer jugador no está disonible en el segundo.
 * <p>
 * <pre>
 * "unavailablePlayers" : {
 *   "4" : [ 2, 3 ],
 *   "2" : [ 1 ]
 * }
 * </pre>
 * <p>
 * El formato de las <strong>localizaciones no disponibles</strong> es idéntico al de los jugadores no disponibles.
 * El campo que se usa es <code>"unavailableLocalizations"</code>.
 * <p>
 * <pre>
 * "unavailableLocalizations" : {
 *   "1" : [ 1, 2, 4, 5 ]
 * }
 * </pre>
 * <p>
 * La asignación de <strong>jugadores en localizaciones</strong> se indica con el campo
 * <code>"playersInLocalizations"</code> y el formato es igual que el de las no disponibilidades.
 * <p>
 * <pre>
 * "playersInLocalizations" : {
 *   "4" : [ 0 ],
 *   "2" : [ 2, 0 ],
 *   "1" : [ 1 ]
 * }
 * </pre>
 * <p>
 * La asignación de <strong>jugadores en <i>timeslots</i></strong> se incluye bajo el campo
 * <code>"playersAtTimeslots"</code> y sigue el mismo formato que los anteriores componentes.
 * <p>
 * <pre>
 * "playersAtTimeslots" : {
 *   "5" : [ 2 ],
 *   "3" : [ 2, 1 ]
 * }
 * </pre>
 * <p>
 * Para añadir <strong>enfrentamientos predefinidos</strong> se usa el campo <code>"predefinedMatchups"</code>. Se
 * incluirá una lista con objetos con los siguientes atributos obligatorios: <code>"players"</code> que indicará las
 * posiciones de los jugadores que componen el enfrentamiento, <code>"localizations"</code> con las posiciones de las
 * posibles localizaciones del enfrentamiento, <code>"timeslots"</code> con una lista de los <i>timeslots</i> en los
 * que podrá comenzar y <code>"occurrences"</code> asociado al número de ocurrencias del enfrentamiento, un valor
 * entero y positivo.
 * <p>
 * <pre>
 * "predefinedMatchups" : [ {
 *   "players" : [ 0, 1 ],
 *   "localizations" : [ 2 ],
 *   "timeslots" : [ 2, 0, 1 ],
 *   "occurrences" : 1
 * }, {
 *   "players" : [ 4, 2 ],
 *   "localizations" : [ 2, 1, 0 ],
 *   "timeslots" : [ 2, 3 ],
 *   "occurrences" : 1
 * } ]
 * </pre>
 * <p>
 * El <strong>modo de enfrentamiento</strong> se añade con una cadena asociada al campo <code>"matchupMode"</code>.
 * El valor está limitado a los siguientes: <code>"ANY"</code>, <code>"ALL_DIFFERENT"</code>,
 * <code>"ALL_EQUAL"</code> y <code>"CUSTOM"</code>, donde se ignoran mayúsculas y minúsculas, y el guión bajo puede
 * ser sustituido por un espacio.
 * <p>
 * <pre>
 * "matchupMode" : "all_different"
 * </pre>
 * <p>
 * Si el contenido del JSON que se quiere deserializar no se ajusta al formato descrito se lanzará una
 * {@link MalformedJsonException} que describirá el problema en el formato.
 */
public class TournamentDeserializer extends JsonDeserializer<Tournament> {
    @Override
    public Tournament deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        ObjectMapper mapper = new ObjectMapper();

        JsonNode nameNode = node.path("name");
        if (!nameNode.isTextual())
            throw new MalformedJsonException("Expected \"name\" textual field");

        String name = nameNode.asText();

        JsonNode playersNode = node.path("players");
        if (!playersNode.isArray())
            throw new MalformedJsonException("Expected \"players\" array field");
        List<Player> allPlayers =
                mapper.reader(TypeFactory.defaultInstance().constructCollectionType(List.class, Player.class))
                        .readValue(playersNode);

        JsonNode localizationsNode = node.path("localizations");
        if (!localizationsNode.isArray())
            throw new MalformedJsonException("Expected \"localizations\" array field");
        List<Localization> allLocalizations =
                mapper.reader(TypeFactory.defaultInstance().constructCollectionType(List.class, Localization.class))
                        .readValue(localizationsNode);

        JsonNode timeslotsNode = node.path("timeslots");
        if (!timeslotsNode.isArray())
            throw new MalformedJsonException("Expected \"timeslots\" array field");
        List<Timeslot> allTimeslots =
                mapper.reader(TypeFactory.defaultInstance().constructCollectionType(List.class, Timeslot.class))
                        .readValue(timeslotsNode);

        List<Event> events = new ArrayList<>();

        JsonNode eventsNode = node.path("events");
        if (!eventsNode.isArray())
            throw new MalformedJsonException("Expected \"events\" array field");

        for (JsonNode eventNode : eventsNode) {
            JsonNode eventNameNode = eventNode.path("name");
            if (!eventNameNode.isTextual())
                throw new MalformedJsonException("Expected \"name\" textual field for the event");

            String eventName = eventNameNode.asText();

            List<Player> players = parsePlayers(allPlayers, eventNode);
            List<Localization> localizations = parseLocalizations(allLocalizations, eventNode);
            List<Timeslot> timeslots = parseTimeslots(allTimeslots, eventNode);

            if (players.isEmpty())
                players = allPlayers;

            if (localizations.isEmpty())
                localizations = allLocalizations;

            if (timeslots.isEmpty())
                timeslots = allTimeslots;

            Event event = new Event(eventName, players, localizations, timeslots);

            parseMatchesPerPlayer(eventNode, event);

            parseTimeslotsPerMatch(eventNode, event);

            parsePlayersPerMatch(eventNode, event);

            parsePlayersPerTeam(eventNode, event);

            parseTeams(eventNode, players, event);

            parseBreaks(eventNode, timeslots, event);

            parseUnavailablePlayers(eventNode, players, timeslots, event);

            parseUnavailableLocalizations(eventNode, localizations, timeslots, event);

            parsePlayersInLocalizations(eventNode, players, localizations, event);

            parsePlayersAtTimeslots(eventNode, players, timeslots, event);

            // Se aplica después de configurar las localizaciones y horas asignadas para evitar comportamiento
            // indeseado debido a la asignación automática de localizaciones y horas del método que establece
            // los enfrentamientos predefinidos del evento
            parsePredefinedMatchups(eventNode, players, localizations, timeslots, event);

            parseMatchupMode(eventNode, event);

            events.add(event);
        }

        return new Tournament(name, events);
    }

    private List<Player> parsePlayers(List<Player> allPlayers, JsonNode eventNode) throws MalformedJsonException {
        List<Player> players = new ArrayList<>();

        JsonNode playersNode = eventNode.path("players");
        if (!playersNode.isMissingNode()) {
            if (!playersNode.isArray())
                throw new MalformedJsonException("Field \"players\" expected to be an array");

            for (JsonNode playerNode : playersNode) {
                if (!playerNode.isInt())
                    throw new MalformedJsonException("Each item in \"players\" expected to be an integer");

                try {
                    players.add(allPlayers.get(playerNode.asInt()));
                } catch (IndexOutOfBoundsException e) {
                    throw new MalformedJsonException("IndexOutOfBoundsException at players element: " + e.getMessage());
                }
            }
        }

        return players;
    }

    private List<Localization> parseLocalizations(List<Localization> allLocalizations, JsonNode eventNode)
            throws MalformedJsonException {
        List<Localization> localizations = new ArrayList<>();

        JsonNode localizationsNode = eventNode.path("localizations");
        if (!localizationsNode.isMissingNode()) {
            if (!localizationsNode.isArray())
                throw new MalformedJsonException("Field \"localizations\" expected to be an array");

            for (JsonNode localizationNode : localizationsNode) {
                if (!localizationNode.isInt())
                    throw new MalformedJsonException("Each item in \"localizations\" expected to be an integer");

                try {
                    localizations.add(allLocalizations.get(localizationNode.asInt()));
                } catch (IndexOutOfBoundsException e) {
                    throw new MalformedJsonException(
                            "IndexOutOfBoundsException at localizations element: " + e.getMessage());
                }
            }
        }

        return localizations;
    }

    private List<Timeslot> parseTimeslots(List<Timeslot> allTimeslots, JsonNode eventNode)
            throws MalformedJsonException {
        List<Timeslot> timeslots = new ArrayList<>();

        JsonNode timeslotsNode = eventNode.path("timeslots");
        if (!timeslotsNode.isMissingNode()) {
            if (!timeslotsNode.isArray())
                throw new MalformedJsonException("Field \"timeslots\" expected to be an array");

            for (JsonNode timeslotNode : timeslotsNode) {
                if (!timeslotNode.isInt())
                    throw new MalformedJsonException("Each item in \"timeslots\" expected to be an integer");

                try {
                    timeslots.add(allTimeslots.get(timeslotNode.asInt()));
                } catch (IndexOutOfBoundsException e) {
                    throw new MalformedJsonException(
                            "IndexOutOfBoundsException at timeslots element: " + e.getMessage());
                }
            }
        }

        return timeslots;
    }

    private void parseMatchesPerPlayer(JsonNode node, Event event) throws MalformedJsonException {
        JsonNode matchesPerPlayerNode = node.path("matchesPerPlayer");
        if (!matchesPerPlayerNode.isMissingNode()) {
            if (!matchesPerPlayerNode.isInt())
                throw new MalformedJsonException("Field \"matchesPerPlayer\" expected to be an integer");
            event.setMatchesPerPlayer(matchesPerPlayerNode.asInt());
        }

    }

    private void parseTimeslotsPerMatch(JsonNode node, Event event) throws MalformedJsonException {
        JsonNode timeslotsPerMatchNode = node.path("timeslotsPerMatch");
        if (!timeslotsPerMatchNode.isMissingNode()) {
            if (!timeslotsPerMatchNode.isInt())
                throw new MalformedJsonException("Field \"timeslotsPerMatch\" expected to be an integer");
            event.setTimeslotsPerMatch(timeslotsPerMatchNode.asInt());
        }
    }

    private void parsePlayersPerMatch(JsonNode node, Event event) throws MalformedJsonException {
        JsonNode playersPerMatchNode = node.path("playersPerMatch");
        if (!playersPerMatchNode.isMissingNode()) {
            if (!playersPerMatchNode.isInt())
                throw new MalformedJsonException("Field \"playersPerMatch\" expected to be an integer");
            event.setPlayersPerMatch(playersPerMatchNode.asInt());
        }
    }

    private void parsePlayersPerTeam(JsonNode node, Event event) throws MalformedJsonException {
        JsonNode playersPerTeamNode = node.path("playersPerTeam");
        if (!playersPerTeamNode.isMissingNode()) {
            if (!playersPerTeamNode.isInt())
                throw new MalformedJsonException("Field \"playersPerTeam\" expected to be an integer");
            event.setPlayersPerTeam(playersPerTeamNode.asInt());
        }
    }

    private void parseTeams(JsonNode node, List<Player> players, Event event) throws MalformedJsonException {
        JsonNode teamsNode = node.path("teams");
        if (!teamsNode.isMissingNode()) {
            if (!teamsNode.isArray())
                throw new MalformedJsonException("Field \"teams\" expected to be an array");

            List<Team> teams = new ArrayList<>();

            for (JsonNode teamNode : teamsNode) {
                if (!teamNode.isObject())
                    throw new MalformedJsonException("Each item in \"teams\" array field expected to be an object");

                List<Player> playersInTeam = new ArrayList<>();

                JsonNode playersNode = teamNode.path("players");
                if (!playersNode.isArray())
                    throw new MalformedJsonException("Expected \"players\" array field in the team");

                for (JsonNode playerNode : playersNode) {
                    if (!playerNode.isInt())
                        throw new MalformedJsonException(
                                "Each item in \"players\" array field expected to be an integer");

                    try {
                        playersInTeam.add(players.get(playerNode.asInt()));
                    } catch (IndexOutOfBoundsException e) {
                        throw new MalformedJsonException(
                                "IndexOutOfBoundsException at team players element: " + e.getMessage());
                    }
                }

                JsonNode teamNameNode = teamNode.path("name");
                if (teamNameNode.isMissingNode())
                    teams.add(new Team(new HashSet<>(playersInTeam)));
                else {
                    if (!teamNameNode.isTextual())
                        throw new MalformedJsonException("Field \"name\" expected to be textual for the team");
                    teams.add(new Team(teamNameNode.asText(), new HashSet<>(playersInTeam)));
                }
            }
            event.setTeams(teams);
        }
    }

    private void parseBreaks(JsonNode node, List<Timeslot> timeslots, Event event) throws MalformedJsonException {
        JsonNode breaksNode = node.path("breaks");

        if (!breaksNode.isMissingNode()) {
            if (!breaksNode.isArray())
                throw new MalformedJsonException("Field \"breaks\" expected to be an array");

            List<Timeslot> breaks = new ArrayList<>();
            for (JsonNode breakNode : breaksNode) {
                if (!breakNode.isInt())
                    throw new MalformedJsonException("Each item in \"breaks\" array field expected to be an integer");

                try {
                    breaks.add(timeslots.get(breakNode.asInt()));
                } catch (IndexOutOfBoundsException e) {
                    throw new MalformedJsonException("IndexOutOfBoundsException at breaks element: " + e.getMessage());
                }
            }

            event.setBreaks(breaks);
        }
    }

    private void parseUnavailablePlayers(JsonNode node, List<Player> players, List<Timeslot> timeslots, Event event)
            throws MalformedJsonException {
        JsonNode unavailablePlayersNode = node.path("unavailablePlayers");

        if (!unavailablePlayersNode.isMissingNode()) {
            if (!unavailablePlayersNode.isObject())
                throw new MalformedJsonException("Field \"unavailablePlayers\" expected to be an object");

            Map<Player, Set<Timeslot>> unavailablePlayers = new HashMap<>();

            Iterator<Map.Entry<String, JsonNode>> nodeIterator = unavailablePlayersNode.fields();
            while (nodeIterator.hasNext()) {
                Map.Entry<String, JsonNode> entry = nodeIterator.next();

                Set<Timeslot> unavailablePlayerTimeslots = new HashSet<>();
                JsonNode timeslotsNode = entry.getValue();

                if (!timeslotsNode.isArray())
                    throw new MalformedJsonException("Unavailable player timeslots expected to be an array");

                for (JsonNode unavailableTimeslotNode : timeslotsNode) {
                    if (!unavailableTimeslotNode.isInt())
                        throw new MalformedJsonException(
                                "Each item in unavailable player timeslots expected to be an integer");

                    try {
                        unavailablePlayerTimeslots.add(timeslots.get(unavailableTimeslotNode.asInt()));
                    } catch (IndexOutOfBoundsException e) {
                        throw new MalformedJsonException(
                                "IndexOutOfBoundsException at unavailable player timeslots element: " + e.getMessage());
                    }
                }

                try {
                    unavailablePlayers.put(players.get(Integer.valueOf(entry.getKey())), unavailablePlayerTimeslots);
                } catch (NumberFormatException e) {
                    throw new MalformedJsonException("Unavailable player expected to be a textual integer");
                }
            }

            event.setUnavailablePlayers(unavailablePlayers);
        }
    }

    private void parseUnavailableLocalizations(JsonNode node, List<Localization> localizations,
            List<Timeslot> timeslots, Event event) throws MalformedJsonException {
        JsonNode unavailableLocalizationsNode = node.path("unavailableLocalizations");

        if (!unavailableLocalizationsNode.isMissingNode()) {
            if (!unavailableLocalizationsNode.isObject())
                throw new MalformedJsonException("Field \"unavailableLocalizations\" expected to be an object");

            Map<Localization, Set<Timeslot>> unavailableLocalizations = new HashMap<>();

            Iterator<Map.Entry<String, JsonNode>> nodeIterator = unavailableLocalizationsNode.fields();
            while (nodeIterator.hasNext()) {
                Map.Entry<String, JsonNode> entry = nodeIterator.next();

                Set<Timeslot> unavailableLocalizationTimeslots = new HashSet<>();
                JsonNode timeslotsNode = entry.getValue();

                if (!timeslotsNode.isArray())
                    throw new MalformedJsonException("Unavailable localization timeslots expected to be an array");

                for (JsonNode unavailableTimeslotNode : timeslotsNode) {
                    if (!unavailableTimeslotNode.isInt())
                        throw new MalformedJsonException(
                                "Each item in unavailable localization timeslots expected to be an integer");

                    try {
                        unavailableLocalizationTimeslots.add(timeslots.get(unavailableTimeslotNode.asInt()));
                    } catch (IndexOutOfBoundsException e) {
                        throw new MalformedJsonException(
                                "IndexOutOfBoundsException at unavailable localization timeslots element: " +
                                        e.getMessage());
                    }
                }

                try {
                    unavailableLocalizations.put(localizations.get(Integer.valueOf(entry.getKey())),
                            unavailableLocalizationTimeslots
                    );
                } catch (NumberFormatException e) {
                    throw new MalformedJsonException("Unavailable localization expected to be a textual integer");
                }
            }

            event.setUnavailableLocalizations(unavailableLocalizations);
        }
    }

    private void parsePredefinedMatchups(JsonNode node, List<Player> players, List<Localization> localizations,
            List<Timeslot> timeslots, Event event) throws MalformedJsonException {
        JsonNode predefinedMatchupsNode = node.path("predefinedMatchups");

        if (!predefinedMatchupsNode.isMissingNode()) {
            if (!predefinedMatchupsNode.isArray())
                throw new MalformedJsonException("Field \"predefinedMatchups\" expected to be an array");

            Set<Matchup> matchups = new HashSet<>();

            for (JsonNode predefinedMatchupNode : predefinedMatchupsNode) {
                if (!predefinedMatchupNode.isObject())
                    throw new MalformedJsonException("Each item in \"predefinedMatchups\" expected to be an object");

                Set<Player> matchupPlayers = new HashSet<>();
                JsonNode playersNode = predefinedMatchupNode.path("players");
                if (!playersNode.isArray())
                    throw new MalformedJsonException("Expected \"players\" array field in matchup");

                for (JsonNode playerNode : playersNode) {
                    if (!playerNode.isInt())
                        throw new MalformedJsonException("Each item in matchup players expected to be an integer");

                    try {
                        matchupPlayers.add(players.get(playerNode.asInt()));
                    } catch (IndexOutOfBoundsException e) {
                        throw new MalformedJsonException(
                                "IndexOutOfBoundsException at matchup players element: " + e.getMessage());
                    }
                }

                Set<Localization> matchupLocalizations = new HashSet<>();
                JsonNode localizationsNode = predefinedMatchupNode.path("localizations");
                if (!localizationsNode.isArray())
                    throw new MalformedJsonException("Expected \"localizations\" array field in matchup");

                for (JsonNode localizationNode : localizationsNode) {
                    if (!localizationNode.isInt())
                        throw new MalformedJsonException(
                                "Each item in matchup localizations expected to be an " + "integer");

                    try {
                        matchupLocalizations.add(localizations.get(localizationNode.asInt()));
                    } catch (IndexOutOfBoundsException e) {
                        throw new MalformedJsonException(
                                "IndexOutOfBoundsException at matchup localizations element: " + e.getMessage());
                    }
                }

                Set<Timeslot> matchupTimeslots = new HashSet<>();
                JsonNode timeslotsNode = predefinedMatchupNode.path("timeslots");
                if (!timeslotsNode.isArray())
                    throw new MalformedJsonException("Expected \"timeslots\" array field in matchup");

                for (JsonNode timeslotNode : timeslotsNode) {
                    try {
                        if (!timeslotNode.isInt())
                            throw new MalformedJsonException(
                                    "Each item in matchup timeslots expected to be an " + "integer");

                        matchupTimeslots.add(timeslots.get(timeslotNode.asInt()));
                    } catch (IndexOutOfBoundsException e) {
                        throw new MalformedJsonException(
                                "IndexOutOfBoundsException at matchup timeslots element: " + e.getMessage());
                    }
                }

                JsonNode ocurrencesNode = predefinedMatchupNode.path("occurrences");
                if (!ocurrencesNode.isInt())
                    throw new MalformedJsonException("Expected \"occurrences\" integer node in matchup");

                int occurrences = ocurrencesNode.asInt();

                matchups.add(new Matchup(event, matchupPlayers, matchupLocalizations, matchupTimeslots, occurrences));
            }

            event.setPredefinedMatchups(matchups);
        }
    }

    private void parsePlayersInLocalizations(JsonNode node, List<Player> players, List<Localization> localizations,
            Event event) throws MalformedJsonException {
        JsonNode playersInLocalizationsNode = node.path("playersInLocalizations");

        if (!playersInLocalizationsNode.isMissingNode()) {
            if (!playersInLocalizationsNode.isObject())
                throw new MalformedJsonException("Field \"playersInLocalizations\" expected to be an object");

            Iterator<Map.Entry<String, JsonNode>> nodeIterator = playersInLocalizationsNode.fields();
            while (nodeIterator.hasNext()) {
                Map.Entry<String, JsonNode> entry = nodeIterator.next();

                Player player;
                try {
                    player = players.get(Integer.valueOf(entry.getKey()));
                } catch (NumberFormatException e) {
                    throw new MalformedJsonException("Player expected to be a textual integer");
                }

                JsonNode localizationsNode = entry.getValue();
                if (!localizationsNode.isArray())
                    throw new MalformedJsonException("Assigned localizations expected to be an array");

                for (JsonNode localizationNode : localizationsNode) {
                    if (!localizationNode.isInt())
                        throw new MalformedJsonException(
                                "Each item in assigned localizations expected to be an " + "integer");

                    try {
                        event.addPlayerInLocalization(player, localizations.get(localizationNode.asInt()));
                    } catch (IndexOutOfBoundsException e) {
                        throw new MalformedJsonException(
                                "IndexOutOfBoundsException at assigned localizations element: " + e.getMessage());
                    }
                }
            }
        }
    }

    private void parsePlayersAtTimeslots(JsonNode node, List<Player> players, List<Timeslot> timeslots, Event event)
            throws MalformedJsonException {
        JsonNode playersAtTimeslotsNode = node.path("playersAtTimeslots");

        if (!playersAtTimeslotsNode.isMissingNode()) {
            if (!playersAtTimeslotsNode.isObject())
                throw new MalformedJsonException("Field \"playersAtTimeslots\" expected to be an object");

            Iterator<Map.Entry<String, JsonNode>> nodeIterator = playersAtTimeslotsNode.fields();
            while (nodeIterator.hasNext()) {
                Map.Entry<String, JsonNode> entry = nodeIterator.next();
                Player player;
                try {
                    player = players.get(Integer.valueOf(entry.getKey()));
                } catch (NumberFormatException e) {
                    throw new MalformedJsonException("Player expected to be a textual integer");
                }

                JsonNode timeslotsNode = entry.getValue();
                if (!timeslotsNode.isArray())
                    throw new MalformedJsonException("Assigned timeslots expected to be an array");

                for (JsonNode timeslotNode : timeslotsNode) {
                    if (!timeslotNode.isInt())
                        throw new MalformedJsonException("Each item in assigned timeslots expected to be an integer");

                    try {
                        event.addPlayerAtTimeslot(player, timeslots.get(timeslotNode.asInt()));
                    } catch (IndexOutOfBoundsException e) {
                        throw new MalformedJsonException(
                                "IndexOutOfBoundsException at assigned timeslots element: " + e.getMessage());
                    }
                }
            }
        }
    }

    private void parseMatchupMode(JsonNode node, Event event) throws MalformedJsonException {
        JsonNode matchupModeNode = node.path("matchupMode");

        if (!matchupModeNode.isMissingNode()) {
            if (!matchupModeNode.isTextual())
                throw new MalformedJsonException("Field \"matchupMode\" expected to be textual");

            TournamentSolver.MatchupMode matchupMode;
            switch (matchupModeNode.asText().trim().toLowerCase()) {
                case "all_different":
                case "all different":
                    matchupMode = TournamentSolver.MatchupMode.ALL_DIFFERENT;
                    break;
                case "all_equal":
                case "all equal":
                    matchupMode = TournamentSolver.MatchupMode.ALL_EQUAL;
                    break;
                case "any":
                    matchupMode = TournamentSolver.MatchupMode.ANY;
                    break;
                case "custom":
                    matchupMode = TournamentSolver.MatchupMode.CUSTOM;
                    break;
                default:
                    throw new MalformedJsonException("Unknown matchup mode value");
            }

            event.setMatchupMode(matchupMode);
        }
    }
}