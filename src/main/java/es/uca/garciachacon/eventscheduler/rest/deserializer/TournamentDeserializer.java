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
                    matchupMode = null;
            }

            event.setMatchupMode(matchupMode);
        }
    }
}