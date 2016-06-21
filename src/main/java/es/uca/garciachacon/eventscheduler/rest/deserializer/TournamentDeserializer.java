package es.uca.garciachacon.eventscheduler.rest.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import es.uca.garciachacon.eventscheduler.data.model.tournament.Tournament;
import es.uca.garciachacon.eventscheduler.data.model.tournament.event.Event;
import es.uca.garciachacon.eventscheduler.data.model.tournament.event.Matchup;
import es.uca.garciachacon.eventscheduler.data.model.tournament.event.domain.Localization;
import es.uca.garciachacon.eventscheduler.data.model.tournament.event.domain.Player;
import es.uca.garciachacon.eventscheduler.data.model.tournament.event.domain.Team;
import es.uca.garciachacon.eventscheduler.data.model.tournament.event.domain.Timeslot;
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
 */
public class TournamentDeserializer extends JsonDeserializer<Tournament> {
    @Override
    public Tournament deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        ObjectMapper mapper = new ObjectMapper();

        String name = node.get("name").asText();

        List<Player> allPlayers =
                mapper.reader(TypeFactory.defaultInstance().constructCollectionType(List.class, Player.class))
                        .readValue(node.path("players"));

        List<Localization> allLocalizations =
                mapper.reader(TypeFactory.defaultInstance().constructCollectionType(List.class, Localization.class))
                        .readValue(node.path("localizations"));

        List<Timeslot> allTimeslots =
                mapper.reader(TypeFactory.defaultInstance().constructCollectionType(List.class, Timeslot.class))
                        .readValue(node.path("timeslots"));

        List<Event> events = new ArrayList<>();

        JsonNode eventsNode = node.get("events");
        if (eventsNode != null) {
            for (JsonNode eventNode : eventsNode) {
                String eventName = eventNode.get("name").asText();

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
        }

        return new Tournament(name, events);
    }

    private List<Player> parsePlayers(List<Player> allPlayers, JsonNode eventNode) {
        List<Player> players = new ArrayList<>();
        JsonNode playersNode = eventNode.get("players");
        if (playersNode != null) {
            for (JsonNode playerNode : playersNode)
                players.add(allPlayers.get(playerNode.asInt()));
        }
        return players;
    }

    private List<Localization> parseLocalizations(List<Localization> allLocalizations, JsonNode eventNode) {
        List<Localization> localizations = new ArrayList<>();
        JsonNode localizationsNode = eventNode.get("localizations");
        if (localizationsNode != null) {
            for (JsonNode localizationNode : localizationsNode)
                localizations.add(allLocalizations.get(localizationNode.asInt()));
        }
        return localizations;
    }

    private List<Timeslot> parseTimeslots(List<Timeslot> allTimeslots, JsonNode eventNode) {
        List<Timeslot> timeslots = new ArrayList<>();
        JsonNode timeslotsNode = eventNode.get("timeslots");
        if (timeslotsNode != null) {
            for (JsonNode timeslotNode : timeslotsNode)
                timeslots.add(allTimeslots.get(timeslotNode.asInt()));
        }
        return timeslots;
    }

    private void parseMatchesPerPlayer(JsonNode node, Event event) {
        JsonNode matchesPerPlayerNode = node.get("matchesPerPlayer");
        if (matchesPerPlayerNode != null)
            event.setMatchesPerPlayer(Integer.parseInt(matchesPerPlayerNode.asText()));
    }

    private void parseTimeslotsPerMatch(JsonNode node, Event event) {
        JsonNode timeslotsPerMatchNode = node.get("timeslotsPerMatch");
        if (timeslotsPerMatchNode != null)
            event.setTimeslotsPerMatch(Integer.parseInt(timeslotsPerMatchNode.asText()));
    }

    private void parsePlayersPerMatch(JsonNode node, Event event) {
        JsonNode playersPerMatchNode = node.get("playersPerMatch");
        if (playersPerMatchNode != null)
            event.setPlayersPerMatch(Integer.parseInt(playersPerMatchNode.asText()));
    }

    private void parsePlayersPerTeam(JsonNode node, Event event) {
        JsonNode playersPerTeamNode = node.get("playersPerTeam");
        if (playersPerTeamNode != null)
            event.setPlayersPerTeam(Integer.parseInt(playersPerTeamNode.asText()));
    }

    private void parseTeams(JsonNode node, List<Player> players, Event event) {
        JsonNode teamsNode = node.get("teams");
        if (teamsNode != null) {
            List<Team> teams = new ArrayList<>();

            for (JsonNode teamNode : teamsNode) {
                List<Player> playersInTeam = new ArrayList<>();

                for (JsonNode playerNode : teamNode.get("players"))
                    playersInTeam.add(players.get(playerNode.asInt()));

                JsonNode teamName = teamNode.get("name");
                if (teamName != null && teamName.isTextual())
                    teams.add(new Team(teamName.asText(), new HashSet<>(playersInTeam)));
                else
                    teams.add(new Team(new HashSet<>(playersInTeam)));
            }
            event.setTeams(teams);
        }
    }

    private void parseBreaks(JsonNode node, List<Timeslot> timeslots, Event event) {
        JsonNode breaksNode = node.get("breaks");
        if (breaksNode != null) {
            List<Timeslot> breaks = new ArrayList<>();
            for (JsonNode breakNode : breaksNode)
                breaks.add(timeslots.get(breakNode.asInt()));

            event.setBreaks(breaks);
        }
    }

    private void parseUnavailablePlayers(JsonNode node, List<Player> players, List<Timeslot> timeslots, Event event) {

        JsonNode unavailablePlayersNode = node.get("unavailablePlayers");

        if (unavailablePlayersNode != null) {
            Map<Player, Set<Timeslot>> unavailablePlayers = new HashMap<>();

            Iterator<Map.Entry<String, JsonNode>> nodeIterator = unavailablePlayersNode.fields();
            while (nodeIterator.hasNext()) {
                Map.Entry<String, JsonNode> entry = nodeIterator.next();

                Set<Timeslot> unavailablePlayerTimeslots = new HashSet<>();
                for (JsonNode unavailableTimeslotNode : entry.getValue())
                    unavailablePlayerTimeslots.add(timeslots.get(unavailableTimeslotNode.asInt()));

                unavailablePlayers.put(players.get(Integer.valueOf(entry.getKey())), unavailablePlayerTimeslots);
            }

            event.setUnavailablePlayers(unavailablePlayers);
        }
    }

    private void parseUnavailableLocalizations(JsonNode node, List<Localization> localizations,
            List<Timeslot> timeslots, Event event) {

        JsonNode unavailableLocalizationsNode = node.get("unavailableLocalizations");

        if (unavailableLocalizationsNode != null) {
            Map<Localization, Set<Timeslot>> unavailableLocalizations = new HashMap<>();

            Iterator<Map.Entry<String, JsonNode>> nodeIterator = unavailableLocalizationsNode.fields();
            while (nodeIterator.hasNext()) {
                Map.Entry<String, JsonNode> entry = nodeIterator.next();

                Set<Timeslot> unavailableLocalizationTimeslots = new HashSet<>();
                for (JsonNode unavailableTimeslotNode : entry.getValue())
                    unavailableLocalizationTimeslots.add(timeslots.get(unavailableTimeslotNode.asInt()));

                unavailableLocalizations.put(localizations.get(Integer.valueOf(entry.getKey())),
                        unavailableLocalizationTimeslots
                );
            }

            event.setUnavailableLocalizations(unavailableLocalizations);
        }
    }

    private void parsePredefinedMatchups(JsonNode node, List<Player> players, List<Localization> localizations,
            List<Timeslot> timeslots, Event event) {
        JsonNode predefinedMatchupsNode = node.get("predefinedMatchups");
        if (predefinedMatchupsNode != null) {
            Set<Matchup> matchups = new HashSet<>();

            for (JsonNode predefinedMatchupNode : predefinedMatchupsNode) {
                Set<Player> matchupPlayers = new HashSet<>();
                for (JsonNode playerNode : predefinedMatchupNode.get("players"))
                    matchupPlayers.add(players.get(playerNode.asInt()));

                Set<Localization> matchupLocalizations = new HashSet<>();
                for (JsonNode localizationNode : predefinedMatchupNode.get("localizations"))
                    matchupLocalizations.add(localizations.get(localizationNode.asInt()));

                Set<Timeslot> matchupTimeslots = new HashSet<>();
                for (JsonNode timeslotNode : predefinedMatchupNode.get("timeslots"))
                    matchupTimeslots.add(timeslots.get(timeslotNode.asInt()));

                int occurrences = predefinedMatchupNode.get("occurrences").asInt();

                matchups.add(new Matchup(event, matchupPlayers, matchupLocalizations, matchupTimeslots, occurrences));
            }

            event.setPredefinedMatchups(matchups);
        }
    }

    private void parsePlayersInLocalizations(JsonNode node, List<Player> players, List<Localization> localizations,
            Event event) {
        JsonNode playersInLocalizationsNode = node.get("playersInLocalizations");
        if (playersInLocalizationsNode != null) {
            Iterator<Map.Entry<String, JsonNode>> nodeIterator = playersInLocalizationsNode.fields();
            while (nodeIterator.hasNext()) {
                Map.Entry<String, JsonNode> entry = nodeIterator.next();
                Player player = players.get(Integer.valueOf(entry.getKey()));

                for (JsonNode localizationNode : entry.getValue())
                    event.addPlayerInLocalization(player, localizations.get(localizationNode.asInt()));
            }
        }
    }

    private void parsePlayersAtTimeslots(JsonNode node, List<Player> players, List<Timeslot> timeslots, Event event) {
        JsonNode playersAtTimeslotsNode = node.get("playersAtTimeslots");
        if (playersAtTimeslotsNode != null) {
            Iterator<Map.Entry<String, JsonNode>> nodeIterator = playersAtTimeslotsNode.fields();
            while (nodeIterator.hasNext()) {
                Map.Entry<String, JsonNode> entry = nodeIterator.next();
                Player player = players.get(Integer.valueOf(entry.getKey()));

                for (JsonNode timeslotNode : entry.getValue())
                    event.addPlayerAtTimeslot(player, timeslots.get(timeslotNode.asInt()));
            }
        }
    }

    private void parseMatchupMode(JsonNode node, Event event) {
        JsonNode matchupModeNode = node.get("matchupMode");
        if (matchupModeNode != null) {
            TournamentSolver.MatchupMode matchupMode = null;
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
            }

            event.setMatchupMode(matchupMode);
        }
    }
}