package es.uca.garciachacon.eventscheduler.rest.serializer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.uca.garciachacon.eventscheduler.data.model.tournament.Tournament;
import es.uca.garciachacon.eventscheduler.data.model.tournament.event.Event;
import es.uca.garciachacon.eventscheduler.data.model.tournament.event.Matchup;
import es.uca.garciachacon.eventscheduler.data.model.tournament.event.domain.Localization;
import es.uca.garciachacon.eventscheduler.data.model.tournament.event.domain.Player;
import es.uca.garciachacon.eventscheduler.data.model.tournament.event.domain.Timeslot;
import es.uca.garciachacon.eventscheduler.solver.TournamentSolver;
import es.uca.garciachacon.eventscheduler.utils.TournamentUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Tests de la clase de serialización de un torneo, {@link TournamentSerializer}.
 */
public class TournamentSerializerTest {
    private Tournament tournament;
    private Event event;
    private ObjectMapper mapper;

    @Before
    public void setUp() {
        event = new Event(
                "Event",
                TournamentUtils.buildGenericPlayers(8, "Player"),
                TournamentUtils.buildGenericLocalizations(3, "Court"),
                TournamentUtils.buildSimpleTimeslots(10)
        );
        tournament = new Tournament("Tournament", event);

        mapper = new ObjectMapper();
    }

    @Test
    public void serializeWithoutExtrasTest() throws IOException {
        JsonNode root = mapper.readTree(mapper.writeValueAsString(tournament));

        assertEquals("Tournament", root.get("name").asText());

        JsonNode playersNode = root.get("players");
        for (int i = 0; i < playersNode.size(); i++)
            assertEquals("Player " + (i + 1), playersNode.get(i).get("name").asText());

        JsonNode localizationsNode = root.get("localizations");
        for (int i = 0; i < localizationsNode.size(); i++)
            assertEquals("Court " + (i + 1), localizationsNode.get(i).get("name").asText());

        JsonNode timeslotsNode = root.get("timeslots");
        for (int i = 0; i < timeslotsNode.size(); i++)
            assertEquals(i, timeslotsNode.get(i).get("chronologicalOrder").asInt());

        JsonNode eventsNode = root.get("events");
        assertEquals(1, eventsNode.size());

        JsonNode eventNode = eventsNode.get(0);

        assertEquals("Event", eventNode.get("name").asText());

        JsonNode eventPlayersNode = eventNode.get("players");
        for (int i = 0; i < eventPlayersNode.size(); i++)
            assertEquals(i, eventPlayersNode.get(i).asInt());

        JsonNode eventLocalizationsNode = eventNode.get("localizations");
        for (int i = 0; i < eventLocalizationsNode.size(); i++)
            assertEquals(i, eventLocalizationsNode.get(i).asInt());

        JsonNode eventTimeslotsNode = eventNode.get("timeslots");
        for (int i = 0; i < eventTimeslotsNode.size(); i++)
            assertEquals(i, eventTimeslotsNode.get(i).asInt());

        assertEquals(1, eventNode.get("matchesPerPlayer").asInt());
        assertEquals(2, eventNode.get("timeslotsPerMatch").asInt());
        assertEquals(2, eventNode.get("playersPerMatch").asInt());

        assertTrue(eventNode.path("playersPerTeam").isMissingNode());
        assertTrue(eventNode.path("teams").isMissingNode());
        assertTrue(eventNode.path("breaks").isMissingNode());
        assertTrue(eventNode.path("unavailablePlayers").isMissingNode());
        assertTrue(eventNode.path("unavailableLocalizations").isMissingNode());
        assertTrue(eventNode.path("playersInLocalizations").isMissingNode());
        assertTrue(eventNode.path("playersAtTimeslots").isMissingNode());
        assertTrue(eventNode.path("predefinedMatchups").isMissingNode());

        assertEquals("ANY", eventNode.path("matchupMode").asText());
    }

    @Test
    public void serializeWithPlayersPerTeamTest() throws IOException {
        event.setPlayersPerTeam(2);

        assertEquals(2, getMainEventNode().get("playersPerTeam").asInt());
    }

    @Test
    public void serializeWithTeamsTest() throws IOException {
        List<Player> players = event.getPlayers();
        event.addTeam(players.get(3), players.get(5));
        event.addTeam(players.get(2), players.get(7));
        event.addTeam(players.get(1), players.get(4));

        JsonNode teamsNode = getMainEventNode().get("teams");

        assertEquals(3, teamsNode.size());

        assertEquals(
                event.filterTeamByPlayer(players.get(teamsNode.get(0).get("players").get(0).asInt())),
                event.filterTeamByPlayer(players.get(teamsNode.get(0).get("players").get(1).asInt()))
        );

        assertEquals(
                event.filterTeamByPlayer(players.get(teamsNode.get(1).get("players").get(0).asInt())),
                event.filterTeamByPlayer(players.get(teamsNode.get(1).get("players").get(1).asInt()))
        );

        assertEquals(
                event.filterTeamByPlayer(players.get(teamsNode.get(2).get("players").get(0).asInt())),
                event.filterTeamByPlayer(players.get(teamsNode.get(2).get("players").get(1).asInt()))
        );
    }

    @Test
    public void serializeWithBreaksTest() throws IOException {
        event.addBreak(event.getTimeslots().get(5));

        JsonNode breaksNode = getMainEventNode().get("breaks");

        assertEquals(1, breaksNode.size());

        assertEquals(5, breaksNode.get(0).asInt());
    }

    @Test
    public void serializeWithUnavailablePlayers() throws IOException {
        List<Player> players = event.getPlayers();
        List<Timeslot> timeslots = event.getTimeslots();
        event.addUnavailablePlayerAtTimeslotRange(players.get(3), timeslots.get(2), timeslots.get(4));
        event.addUnavailablePlayerAtTimeslot(players.get(6), timeslots.get(0));

        JsonNode unavailablePlayersNode = getMainEventNode().get("unavailablePlayers");

        assertEquals(2, unavailablePlayersNode.size());

        List<Integer> timeslotsReferences = new ArrayList<>(3);
        for (int i = 0; i < 3; i++)
            timeslotsReferences.add(unavailablePlayersNode.get("3").get(i).asInt());
        Collections.sort(timeslotsReferences);

        assertEquals(2, timeslotsReferences.get(0).intValue());
        assertEquals(3, timeslotsReferences.get(1).intValue());
        assertEquals(4, timeslotsReferences.get(2).intValue());

        assertEquals(0, unavailablePlayersNode.get("6").get(0).asInt());
    }

    @Test
    public void serializeWithUnavailableLocalizations() throws IOException {
        List<Localization> localizations = event.getLocalizations();
        List<Timeslot> timeslots = event.getTimeslots();
        event.addUnavailableLocalizationAtTimeslotRange(localizations.get(1), timeslots.get(3), timeslots.get(7));
        event.addUnavailableLocalizationAtTimeslotRange(localizations.get(0), timeslots.get(0), timeslots.get(3));

        JsonNode unavailableLocalizationsNode = getMainEventNode().get("unavailableLocalizations");

        assertEquals(2, unavailableLocalizationsNode.size());

        List<Integer> timeslotsReferences = new ArrayList<>(5);
        for (int i = 0; i < 5; i++)
            timeslotsReferences.add(unavailableLocalizationsNode.get("1").get(i).asInt());
        Collections.sort(timeslotsReferences);

        assertEquals(3, timeslotsReferences.get(0).intValue());
        assertEquals(4, timeslotsReferences.get(1).intValue());
        assertEquals(5, timeslotsReferences.get(2).intValue());
        assertEquals(6, timeslotsReferences.get(3).intValue());
        assertEquals(7, timeslotsReferences.get(4).intValue());

        timeslotsReferences = new ArrayList<>(4);
        for (int i = 0; i < 4; i++)
            timeslotsReferences.add(unavailableLocalizationsNode.get("0").get(i).asInt());
        Collections.sort(timeslotsReferences);

        assertEquals(0, timeslotsReferences.get(0).intValue());
        assertEquals(1, timeslotsReferences.get(1).intValue());
        assertEquals(2, timeslotsReferences.get(2).intValue());
        assertEquals(3, timeslotsReferences.get(3).intValue());
    }

    @Test
    public void serializeWithPlayersInLocalizationsTest() throws IOException {
        List<Player> players = event.getPlayers();
        List<Localization> localizations = event.getLocalizations();
        event.addPlayerInLocalization(players.get(4), localizations.get(0));
        event.addPlayerInLocalization(players.get(4), localizations.get(1));
        event.addPlayerInLocalization(players.get(0), localizations.get(1));
        event.addPlayerInLocalization(players.get(7), localizations.get(0));

        JsonNode playersInLocalizationsNode = getMainEventNode().get("playersInLocalizations");

        assertEquals(3, playersInLocalizationsNode.size());

        List<Integer> localizationsReferences = new ArrayList<>(2);
        localizationsReferences.add(playersInLocalizationsNode.get("4").get(0).asInt());
        localizationsReferences.add(playersInLocalizationsNode.get("4").get(1).asInt());
        Collections.sort(localizationsReferences);

        assertEquals(0, localizationsReferences.get(0).intValue());
        assertEquals(1, localizationsReferences.get(1).intValue());

        assertEquals(1, playersInLocalizationsNode.get("0").get(0).asInt());

        assertEquals(0, playersInLocalizationsNode.get("7").get(0).asInt());
    }

    @Test
    public void serializeWithPlayersAtTimeslotsTest() throws IOException {
        List<Player> players = event.getPlayers();
        List<Timeslot> timeslots = event.getTimeslots();
        event.addPlayerAtTimeslot(players.get(2), timeslots.get(4));
        event.addPlayerAtTimeslot(players.get(5), timeslots.get(3));
        event.addPlayerAtTimeslot(players.get(5), timeslots.get(6));
        event.addPlayerAtTimeslot(players.get(5), timeslots.get(7));

        JsonNode playersAtTimeslotsNode = getMainEventNode().get("playersAtTimeslots");

        assertEquals(2, playersAtTimeslotsNode.size());

        assertEquals(4, playersAtTimeslotsNode.get("2").get(0).asInt());

        List<Integer> timeslotsReferences = new ArrayList<>(3);
        for (int i = 0; i < 3; i++)
            timeslotsReferences.add(playersAtTimeslotsNode.get("5").get(i).asInt());
        Collections.sort(timeslotsReferences);

        assertEquals(3, timeslotsReferences.get(0).intValue());
        assertEquals(6, timeslotsReferences.get(1).intValue());
        assertEquals(7, timeslotsReferences.get(2).intValue());
    }

    @Test
    public void serializeWithPredefinedMatchupsTest() throws IOException {
        List<Player> players = event.getPlayers();
        List<Localization> localizations = event.getLocalizations();
        List<Timeslot> timeslots = event.getTimeslots();
        event.addMatchup(players.get(0), players.get(1));
        event.addMatchup(new Matchup(
                event,
                new HashSet<>(Arrays.asList(players.get(2), players.get(3))),
                new HashSet<>(Arrays.asList(localizations.get(0))),
                new HashSet<>(Arrays.asList(timeslots.get(3), timeslots.get(4))),
                1
        ));

        JsonNode predefinedMatchupsNode = getMainEventNode().get("predefinedMatchups");

        assertEquals(2, predefinedMatchupsNode.size());

        List<Integer> playersReferences = new ArrayList<>(2);
        playersReferences.add(predefinedMatchupsNode.get(0).get("players").get(0).asInt());
        playersReferences.add(predefinedMatchupsNode.get(0).get("players").get(1).asInt());
        Collections.sort(playersReferences);

        assertEquals(0, playersReferences.get(0).intValue());
        assertEquals(1, playersReferences.get(1).intValue());

        List<Integer> localizationsReferences = new ArrayList<>(3);
        for (int i = 0; i < 3; i++)
            localizationsReferences.add(predefinedMatchupsNode.get(0).get("localizations").get(i).asInt());
        Collections.sort(localizationsReferences);

        for (int i = 0; i < 3; i++)
            assertEquals(i, localizationsReferences.get(i).intValue());

        List<Integer> timeslotsReferences = new ArrayList<>(timeslots.size());
        for (int i = 0; i < timeslots.size() - event.getTimeslotsPerMatch() + 1; i++)
            timeslotsReferences.add(predefinedMatchupsNode.get(0).get("timeslots").get(i).asInt());
        Collections.sort(timeslotsReferences);

        for (int i = 0; i < timeslots.size() - event.getTimeslotsPerMatch() + 1; i++)
            assertEquals(i, timeslotsReferences.get(i).intValue());

        assertEquals(1, predefinedMatchupsNode.get(0).get("occurrences").asInt());


        playersReferences = new ArrayList<>(2);
        playersReferences.add(predefinedMatchupsNode.get(1).get("players").get(0).asInt());
        playersReferences.add(predefinedMatchupsNode.get(1).get("players").get(1).asInt());
        Collections.sort(playersReferences);

        assertEquals(2, playersReferences.get(0).intValue());
        assertEquals(3, playersReferences.get(1).intValue());

        assertEquals(0, predefinedMatchupsNode.get(1).get("localizations").get(0).asInt());

        timeslotsReferences = new ArrayList<>(2);
        timeslotsReferences.add(predefinedMatchupsNode.get(1).get("timeslots").get(0).asInt());
        timeslotsReferences.add(predefinedMatchupsNode.get(1).get("timeslots").get(1).asInt());
        Collections.sort(timeslotsReferences);

        assertEquals(3, timeslotsReferences.get(0).intValue());
        assertEquals(4, timeslotsReferences.get(1).intValue());

        assertEquals(1, predefinedMatchupsNode.get(1).get("occurrences").asInt());
    }

    @Test
    public void serializeWithMatchupModeTest() throws IOException {
        event.setMatchesPerPlayer(2);
        event.setMatchupMode(TournamentSolver.MatchupMode.ALL_DIFFERENT);

        assertEquals("ALL_DIFFERENT", getMainEventNode().get("matchupMode").asText());
    }

    @Test
    public void serializeWithEventsWithPartiallySharedDomainsTest() throws IOException {
        List<Player> players = TournamentUtils.buildGenericPlayers(16, "Player");
        List<Localization> localizations = TournamentUtils.buildGenericLocalizations(6, "Court");
        List<Timeslot> timeslots = TournamentUtils.buildDayOfWeekTimeslots(8);

        Event firstEvent = new Event(
                "First Event",
                players.subList(0, players.size() / 2),
                localizations.subList(0, localizations.size() / 2),
                timeslots.subList(0, timeslots.size() / 2)
        );
        Event secondEvent = new Event(
                "Second Event",
                players.subList(players.size() / 2, players.size()),
                localizations.subList(localizations.size() / 2, localizations.size()),
                timeslots.subList(timeslots.size() / 2, timeslots.size())
        );

        tournament = new Tournament("Tournament", firstEvent, secondEvent);

        JsonNode root = mapper.readTree(mapper.writeValueAsString(tournament));
        JsonNode firstEventNode = root.get("events").get(0);
        JsonNode secondEventNode = root.get("events").get(1);

        for (int i = 0; i < players.size() / 2; i++) {
            Player player = players.get(firstEventNode.get("players").get(i).asInt());
            assertTrue(firstEvent.getPlayers().contains(player));
            assertFalse(secondEvent.getPlayers().contains(player));

            player = players.get(secondEventNode.get("players").get(i).asInt());
            assertFalse(firstEvent.getPlayers().contains(player));
            assertTrue(secondEvent.getPlayers().contains(player));
        }

        for (int i = 0; i < localizations.size() / 2; i++) {
            Localization localization = localizations.get(firstEventNode.get("localizations").get(i).asInt());
            assertTrue(firstEvent.getLocalizations().contains(localization));
            assertFalse(secondEvent.getLocalizations().contains(localization));

            localization = localizations.get(secondEventNode.get("localizations").get(i).asInt());
            assertFalse(firstEvent.getLocalizations().contains(localization));
            assertTrue(secondEvent.getLocalizations().contains(localization));
        }

        for (int i = 0; i < timeslots.size() / 2; i++) {
            Timeslot timeslot = timeslots.get(firstEventNode.get("timeslots").get(i).asInt());
            assertTrue(firstEvent.getTimeslots().contains(timeslot));
            assertFalse(secondEvent.getTimeslots().contains(timeslot));

            timeslot = timeslots.get(secondEventNode.get("timeslots").get(i).asInt());
            assertFalse(firstEvent.getTimeslots().contains(timeslot));
            assertTrue(secondEvent.getTimeslots().contains(timeslot));
        }
    }

    private JsonNode getMainEventNode() throws IOException {
        JsonNode root = mapper.readTree(mapper.writeValueAsString(tournament));
        return root.get("events").get(0);
    }
}
