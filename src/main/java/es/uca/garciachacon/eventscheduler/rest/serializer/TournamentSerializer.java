package es.uca.garciachacon.eventscheduler.rest.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import es.uca.garciachacon.eventscheduler.data.model.tournament.Tournament;
import es.uca.garciachacon.eventscheduler.data.model.tournament.event.Event;
import es.uca.garciachacon.eventscheduler.data.model.tournament.event.Matchup;
import es.uca.garciachacon.eventscheduler.data.model.tournament.event.domain.Localization;
import es.uca.garciachacon.eventscheduler.data.model.tournament.event.domain.Player;
import es.uca.garciachacon.eventscheduler.data.model.tournament.event.domain.Team;
import es.uca.garciachacon.eventscheduler.data.model.tournament.event.domain.Timeslot;
import es.uca.garciachacon.eventscheduler.utils.TournamentUtils;

import java.io.IOException;
import java.util.*;

public class TournamentSerializer extends JsonSerializer<Tournament> {
    public static void main(String[] args) throws IOException {
        List<Player> players = TournamentUtils.buildGenericPlayers(4, "Jugador");
        List<Localization> localizations = TournamentUtils.buildGenericLocalizations(2, "Pista");
        List<Timeslot> timeslots = TournamentUtils.buildLocalTimeTimeslots(4);
        Event event = new Event("Evento", players, localizations, timeslots);

        event.addTeam(players.get(0), players.get(3));
        event.addTeam(players.get(2), players.get(1));

        event.addBreakRange(timeslots.get(0), timeslots.get(2));

        event.addUnavailablePlayerAtTimeslot(players.get(0), timeslots.get(2));
        event.addUnavailablePlayerAtTimeslot(players.get(0), timeslots.get(3));
        event.addUnavailablePlayerAtTimeslot(players.get(3), timeslots.get(1));
        event.addUnavailablePlayerAtTimeslot(players.get(3), timeslots.get(2));

        event.addUnavailableLocalizationAtTimeslot(localizations.get(0), timeslots.get(1));
        event.addUnavailableLocalizationAtTimeslot(localizations.get(0), timeslots.get(2));
        event.addUnavailableLocalizationAtTimeslot(localizations.get(1), timeslots.get(0));

        event.addPlayerInLocalization(players.get(1), localizations.get(0));

        event.addPlayerAtTimeslot(players.get(3), timeslots.get(0));
        event.addPlayerAtTimeslot(players.get(3), timeslots.get(1));
        event.addPlayerAtTimeslot(players.get(2), timeslots.get(2));
        event.addPlayerAtTimeslot(players.get(0), timeslots.get(0));

        event.addMatchup(new Matchup(
                event,
                new HashSet<>(Arrays.asList(players.get(0), players.get(2))),
                new HashSet<>(localizations),
                new HashSet<>(Arrays.asList(timeslots.get(0))),
                1
        ));

        Tournament tournament = new Tournament("Torneo", event);

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(tournament);

        System.out.println(json);

        Tournament deserializedTournament = mapper.readValue(json, Tournament.class);
        System.out.println("Deserialized tournament ==> " + deserializedTournament);
    }

    @Override
    public void serialize(Tournament tournament, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();

        List<Player> allPlayers = tournament.getAllPlayers();
        List<Localization> allLocalizations = tournament.getAllLocalizations();
        List<Timeslot> allTimeslots = tournament.getAllTimeslots();

        gen.writeStringField("name", tournament.getName());

        gen.writeArrayFieldStart("players");
        for (Player player : allPlayers)
            gen.writeObject(player);
        gen.writeEndArray();

        gen.writeArrayFieldStart("localizations");
        for (Localization localization : allLocalizations)
            gen.writeObject(localization);
        gen.writeEndArray();

        gen.writeArrayFieldStart("timeslots");
        for (Timeslot timeslot : allTimeslots)
            gen.writeObject(timeslot);
        gen.writeEndArray();

        gen.writeArrayFieldStart("events");
        for (Event event : tournament.getEvents()) {
            gen.writeStartObject();

            gen.writeStringField("name", event.getName());

            gen.writeArrayFieldStart("players");
            for (Player player : event.getPlayers())
                gen.writeNumber(allPlayers.indexOf(player));
            gen.writeEndArray();

            gen.writeArrayFieldStart("localizations");
            for (Localization localization : event.getLocalizations())
                gen.writeNumber(allLocalizations.indexOf(localization));
            gen.writeEndArray();

            gen.writeArrayFieldStart("timeslots");
            for (Timeslot timeslot : event.getTimeslots())
                gen.writeNumber(allTimeslots.indexOf(timeslot));
            gen.writeEndArray();

            gen.writeNumberField("matchesPerPlayer", event.getMatchesPerPlayer());
            gen.writeNumberField("timeslotsPerMatch", event.getTimeslotsPerMatch());
            gen.writeNumberField("playersPerMatch", event.getPlayersPerMatch());
            if (event.getPlayersPerTeam() > 0)
                gen.writeNumberField("playersPerTeam", event.getPlayersPerTeam());

            List<Team> teams = event.getTeams();
            if (!teams.isEmpty()) {
                gen.writeArrayFieldStart("teams");

                for (Team team : teams) {
                    gen.writeStartObject();

                    gen.writeStringField("name", team.getName());
                    gen.writeArrayFieldStart("players");
                    for (Player player : team.getPlayers())
                        gen.writeNumber(allPlayers.indexOf(player));
                    gen.writeEndArray();

                    gen.writeEndObject();
                }
                gen.writeEndArray();
            }

            List<Timeslot> breaks = event.getBreaks();
            if (!breaks.isEmpty()) {
                gen.writeArrayFieldStart("breaks");
                for (Timeslot breakTimeslot : breaks)
                    gen.writeNumber(allTimeslots.indexOf(breakTimeslot));
                gen.writeEndArray();
            }

            Map<Player, Set<Timeslot>> unavailablePlayers = event.getUnavailablePlayers();
            if (!unavailablePlayers.isEmpty()) {
                gen.writeObjectFieldStart("unavailablePlayers");

                for (Map.Entry<Player, Set<Timeslot>> entry : unavailablePlayers.entrySet()) {
                    gen.writeFieldName(String.valueOf(allPlayers.indexOf(entry.getKey())));

                    gen.writeStartArray();
                    for (Timeslot timeslot : entry.getValue())
                        gen.writeNumber(allTimeslots.indexOf(timeslot));
                    gen.writeEndArray();
                }

                gen.writeEndObject();
            }

            Map<Localization, Set<Timeslot>> unavailableLocalizations = event.getUnavailableLocalizations();
            if (!unavailableLocalizations.isEmpty()) {
                gen.writeObjectFieldStart("unavailableLocalizations");

                for (Map.Entry<Localization, Set<Timeslot>> entry : unavailableLocalizations.entrySet()) {
                    gen.writeFieldName(String.valueOf(allLocalizations.indexOf(entry.getKey())));

                    gen.writeStartArray();
                    for (Timeslot timeslot : entry.getValue())
                        gen.writeNumber(allTimeslots.indexOf(timeslot));
                    gen.writeEndArray();
                }

                gen.writeEndObject();
            }

            Map<Player, Set<Localization>> playersInLocalizations = event.getPlayersInLocalizations();
            if (!playersInLocalizations.isEmpty()) {
                gen.writeObjectFieldStart("playersInLocalizations");

                for (Map.Entry<Player, Set<Localization>> entry : playersInLocalizations.entrySet()) {
                    gen.writeFieldName(String.valueOf(allPlayers.indexOf(entry.getKey())));

                    gen.writeStartArray();
                    for (Localization localization : entry.getValue())
                        gen.writeNumber(allLocalizations.indexOf(localization));
                    gen.writeEndArray();
                }

                gen.writeEndObject();
            }

            Map<Player, Set<Timeslot>> playersAtTimeslots = event.getPlayersAtTimeslots();
            if (!playersAtTimeslots.isEmpty()) {
                gen.writeObjectFieldStart("playersAtTimeslots");

                for (Map.Entry<Player, Set<Timeslot>> entry : playersAtTimeslots.entrySet()) {
                    gen.writeFieldName(String.valueOf(allPlayers.indexOf(entry.getKey())));

                    gen.writeStartArray();
                    for (Timeslot timeslot : entry.getValue())
                        gen.writeNumber(allTimeslots.indexOf(timeslot));
                    gen.writeEndArray();
                }

                gen.writeEndObject();
            }

            Set<Matchup> predefinedMatchups = event.getPredefinedMatchups();
            if (!predefinedMatchups.isEmpty()) {
                gen.writeArrayFieldStart("predefinedMatchups");

                for (Matchup predefinedMatchup : predefinedMatchups) {
                    gen.writeStartObject();

                    gen.writeArrayFieldStart("players");
                    for (Player player : predefinedMatchup.getPlayers())
                        gen.writeNumber(allPlayers.indexOf(player));
                    gen.writeEndArray();

                    gen.writeArrayFieldStart("localizations");
                    for (Localization localization : predefinedMatchup.getLocalizations())
                        gen.writeNumber(allLocalizations.indexOf(localization));
                    gen.writeEndArray();

                    gen.writeArrayFieldStart("timeslots");
                    for (Timeslot timeslot : predefinedMatchup.getTimeslots())
                        gen.writeNumber(allTimeslots.indexOf(timeslot));
                    gen.writeEndArray();

                    gen.writeNumberField("occurrences", predefinedMatchup.getOccurrences());

                    gen.writeEndObject();
                }

                gen.writeEndArray();
            }

            gen.writeStringField("matchupMode", event.getMatchupMode().toString());

            gen.writeEndObject();
        }
        gen.writeEndArray();

        gen.writeEndObject();
    }
}
