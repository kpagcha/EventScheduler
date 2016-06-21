package es.uca.garciachacon.eventscheduler.rest.deserializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
 * Tests de la clase de deserialización de torneos, {@link TournamentDeserializer}. Además de la compatibilidad
 * bidireccional de serialización y deserialización de torneos.
 */
public class TournamentDeserializerTest {
    private Tournament tournament;
    private Event event;
    private ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setUp() {
        event = new Event(
                "Event",
                TournamentUtils.buildGenericPlayers(8, "Player"),
                TournamentUtils.buildGenericLocalizations(4, "Court"),
                TournamentUtils.buildSimpleTimeslots(6)
        );
        tournament = new Tournament("Tournament", event);
    }


    @Test
    public void deserializeWithoutExtrasTest() throws IOException {
        Tournament t = mapper.readValue(getJsonBody(), Tournament.class);

        List<Player> players = t.getAllPlayers();
        for (int i = 0; i < players.size(); i++)
            assertEquals("Player " + (i + 1), players.get(i).getName());

        List<Localization> localizations = t.getAllLocalizations();
        for (int i = 0; i < localizations.size(); i++)
            assertEquals("Court " + (i + 1), localizations.get(i).getName());

        List<Timeslot> timeslots = t.getAllTimeslots();
        for (int i = 0; i < timeslots.size(); i++)
            assertEquals(i, timeslots.get(i).getChronologicalOrder());

        Event e = t.getEvents().get(0);
        assertEquals(1, e.getMatchesPerPlayer());
        assertEquals(2, e.getTimeslotsPerMatch());
        assertEquals(2, e.getPlayersPerMatch());
        assertFalse(e.hasTeams());
        assertFalse(e.hasPredefinedTeams());
        assertFalse(e.hasPlayersInLocalizations());
        assertFalse(e.hasPlayersAtTimeslots());
        assertFalse(e.hasBreaks());
    }

    @Test
    public void deserializeWithMultipleEventsTest() throws IOException {
        List<Localization> localizations = TournamentUtils.buildGenericLocalizations(1, "Court");
        List<Timeslot> timeslots = TournamentUtils.buildSimpleTimeslots(10);
        Event firstEvent = new Event(
                "First Event",
                TournamentUtils.buildGenericPlayers(4, "PlEvt1"),
                localizations,
                timeslots.subList(0, 2),
                1,
                1,
                2
        );
        Event secondEvent =
                new Event("Second Event", TournamentUtils.buildGenericPlayers(8, "PlEvt2"), localizations, timeslots);
        tournament = new Tournament("Tournament", firstEvent, secondEvent);

        Tournament t = mapper.readValue(getJsonBody(), Tournament.class);

        assertEquals(2, t.getEvents().size());

        Event e1 = t.getEvents().get(0);
        Event e2 = t.getEvents().get(1);

        assertEquals("First Event", e1.getName());
        assertEquals("Second Event", e2.getName());

        assertEquals(4, e1.getPlayers().size());
        assertEquals(8, e2.getPlayers().size());

        assertEquals(e1.getLocalizations(), e2.getLocalizations());

        assertEquals(2, e1.getTimeslots().size());
        assertEquals(10, e2.getTimeslots().size());
        assertEquals(e1.getTimeslots(), e2.getTimeslots().subList(0, 2));
    }

    @Test
    public void deserializeWithMissingNodesTest() throws IOException {
        String json = getJsonBody();

        JsonNode root = mapper.readTree(json);
        ObjectNode eventNode = (ObjectNode) root.get("events").get(0);
        eventNode.remove("players");
        eventNode.remove("localizations");
        eventNode.remove("timeslots");

        Tournament t = mapper.readValue(root.toString(), Tournament.class);

        Event e = t.getEvents().get(0);
        assertEquals(e.getPlayers(), t.getAllPlayers());
        assertEquals(e.getLocalizations(), t.getAllLocalizations());
        assertEquals(e.getTimeslots(), t.getAllTimeslots());
    }

    @Test
    public void deserializeMatchesPerPlayerTest() throws IOException {
        event.setMatchesPerPlayer(2);

        Tournament t = mapper.readValue(getJsonBody(), Tournament.class);
        assertEquals(2, t.getEvents().get(0).getMatchesPerPlayer());
    }

    @Test
    public void deserializeTimeslotsPerMatchTest() throws IOException {
        event.setTimeslotsPerMatch(1);

        Tournament t = mapper.readValue(getJsonBody(), Tournament.class);
        assertEquals(1, t.getEvents().get(0).getTimeslotsPerMatch());
    }

    @Test
    public void deserializePlayersPerMatchTest() throws IOException {
        event.setPlayersPerMatch(4);

        Tournament t = mapper.readValue(getJsonBody(), Tournament.class);
        assertEquals(4, t.getEvents().get(0).getPlayersPerMatch());
    }

    @Test
    public void deserializePlayersPerTeamTest() throws IOException {
        event.setPlayersPerTeam(2);

        Tournament t = mapper.readValue(getJsonBody(), Tournament.class);
        assertEquals(2, t.getEvents().get(0).getPlayersPerTeam());
    }

    @Test
    public void deserializeTeamsTest() throws IOException {
        List<Player> players = event.getPlayers();
        event.addTeam(players.get(0), players.get(3));
        event.addTeam(players.get(4), players.get(5));

        Tournament t = mapper.readValue(getJsonBody(), Tournament.class);
        Event e = t.getEvents().get(0);
        players = e.getPlayers();

        assertEquals(2, e.getTeams().size());

        assertNotNull(e.filterTeamByPlayer(players.get(0)));
        assertNotNull(e.filterTeamByPlayer(players.get(3)));
        assertNotNull(e.filterTeamByPlayer(players.get(4)));
        assertNotNull(e.filterTeamByPlayer(players.get(5)));

        assertEquals(e.filterTeamByPlayer(players.get(0)), e.filterTeamByPlayer(players.get(3)));
        assertEquals(e.filterTeamByPlayer(players.get(4)), e.filterTeamByPlayer(players.get(5)));
    }

    @Test
    public void deserializeBreaksTest() throws IOException {
        List<Timeslot> timeslots = event.getTimeslots();
        event.addBreakRange(timeslots.get(3), timeslots.get(5));

        Tournament t = mapper.readValue(getJsonBody(), Tournament.class);
        Event e = t.getEvents().get(0);
        timeslots = e.getTimeslots();

        assertEquals(3, e.getBreaks().size());
        assertTrue(e.getBreaks().contains(timeslots.get(3)));
        assertTrue(e.getBreaks().contains(timeslots.get(4)));
        assertTrue(e.getBreaks().contains(timeslots.get(5)));
    }

    @Test
    public void deserializeUnavailablePlayersTest() throws IOException {
        List<Player> players = event.getPlayers();
        List<Timeslot> timeslots = event.getTimeslots();
        event.addUnavailablePlayerAtTimeslotRange(players.get(2), timeslots.get(2), timeslots.get(5));
        event.addUnavailablePlayerAtTimeslotRange(players.get(5), timeslots.get(0), timeslots.get(4));
        event.addUnavailablePlayerAtTimeslot(players.get(0), timeslots.get(3));

        Tournament t = mapper.readValue(getJsonBody(), Tournament.class);
        Event e = t.getEvents().get(0);
        players = e.getPlayers();
        timeslots = e.getTimeslots();

        Map<Player, Set<Timeslot>> unavailablePlayers = e.getUnavailablePlayers();
        assertEquals(3, unavailablePlayers.size());
        assertTrue(unavailablePlayers.containsKey(players.get(2)));
        assertTrue(unavailablePlayers.containsKey(players.get(5)));
        assertTrue(unavailablePlayers.containsKey(players.get(0)));
        for (int i = 2; i <= 5; i++)
            assertTrue(unavailablePlayers.get(players.get(2)).contains(timeslots.get(i)));
        for (int i = 0; i <= 4; i++)
            assertTrue(unavailablePlayers.get(players.get(5)).contains(timeslots.get(i)));
        assertTrue(unavailablePlayers.get(players.get(0)).contains(timeslots.get(3)));
    }

    @Test
    public void deserializeUnavailableLocalizationsTest() throws IOException {
        List<Localization> localizations = event.getLocalizations();
        List<Timeslot> timeslots = event.getTimeslots();
        event.addUnavailableLocalizationAtTimeslotRange(localizations.get(0), timeslots.get(0), timeslots.get(3));
        event.addUnavailableLocalizationAtTimeslotRange(localizations.get(2), timeslots.get(2), timeslots.get(5));
        event.addUnavailableLocalizationAtTimeslotRange(localizations.get(3), timeslots.get(1), timeslots.get(3));

        Tournament t = mapper.readValue(getJsonBody(), Tournament.class);
        Event e = t.getEvents().get(0);
        localizations = e.getLocalizations();
        timeslots = e.getTimeslots();

        Map<Localization, Set<Timeslot>> unavailableLocalizations = e.getUnavailableLocalizations();
        assertEquals(3, unavailableLocalizations.size());
        assertTrue(unavailableLocalizations.containsKey(localizations.get(0)));
        assertTrue(unavailableLocalizations.containsKey(localizations.get(2)));
        assertTrue(unavailableLocalizations.containsKey(localizations.get(3)));
        for (int i = 0; i <= 3; i++)
            assertTrue(unavailableLocalizations.get(localizations.get(0)).contains(timeslots.get(i)));
        for (int i = 2; i <= 5; i++)
            assertTrue(unavailableLocalizations.get(localizations.get(2)).contains(timeslots.get(i)));
        for (int i = 1; i <= 3; i++)
            assertTrue(unavailableLocalizations.get(localizations.get(3)).contains(timeslots.get(i)));
    }

    @Test
    public void deserializePredefinedMatchupsTest() throws IOException {
        List<Player> players = event.getPlayers();
        List<Localization> localizations = event.getLocalizations();
        List<Timeslot> timeslots = event.getTimeslots();
        event.addMatchup(players.get(2), players.get(5));
        event.addMatchup(new Matchup(
                event,
                new HashSet<>(Arrays.asList(players.get(4), players.get(1))),
                new HashSet<>(Arrays.asList(localizations.get(0), localizations.get(2))),
                new HashSet<>(Arrays.asList(timeslots.get(1), timeslots.get(4))),
                1
        ));

        Tournament t = mapper.readValue(getJsonBody(), Tournament.class);
        Event e = t.getEvents().get(0);
        players = e.getPlayers();
        localizations = e.getLocalizations();
        timeslots = e.getTimeslots();

        assertEquals(2, e.getPredefinedMatchups().size());

        Iterator<Matchup> iterator = e.getPredefinedMatchups().iterator();
        Matchup firstMatchup = iterator.next();
        Matchup secondMatchup;
        if (!firstMatchup.getPlayers().contains(players.get(2))) {
            secondMatchup = firstMatchup;
            firstMatchup = iterator.next();
        } else
            secondMatchup = iterator.next();

        assertTrue(firstMatchup.getPlayers().contains(players.get(2)));
        assertTrue(firstMatchup.getPlayers().contains(players.get(5)));
        assertEquals(new HashSet<>(localizations), new HashSet<>(firstMatchup.getLocalizations()));
        assertEquals(
                new HashSet<>(timeslots.subList(0, timeslots.size() - e.getTimeslotsPerMatch() + 1)),
                new HashSet<>(firstMatchup.getTimeslots())
        );

        assertTrue(secondMatchup.getPlayers().contains(players.get(4)));
        assertTrue(secondMatchup.getPlayers().contains(players.get(1)));
        assertTrue(secondMatchup.getLocalizations().contains(localizations.get(0)));
        assertTrue(secondMatchup.getLocalizations().contains(localizations.get(2)));
        assertTrue(secondMatchup.getTimeslots().contains(timeslots.get(1)));
        assertTrue(secondMatchup.getTimeslots().contains(timeslots.get(4)));
    }

    @Test
    public void deserializePlayersInLocalizationsTest() throws IOException {
        List<Player> players = event.getPlayers();
        List<Localization> localizations = event.getLocalizations();
        event.addPlayerInLocalization(players.get(0), localizations.get(2));
        event.addPlayerInLocalization(players.get(0), localizations.get(3));
        event.addPlayerInLocalization(players.get(2), localizations.get(3));
        event.addPlayerInLocalization(players.get(5), localizations.get(0));
        event.addPlayerInLocalization(players.get(3), localizations.get(1));
        event.addPlayerInLocalization(players.get(3), localizations.get(2));

        Tournament t = mapper.readValue(getJsonBody(), Tournament.class);
        Event e = t.getEvents().get(0);
        players = e.getPlayers();
        localizations = e.getLocalizations();

        Map<Player, Set<Localization>> playersInLocalizations = e.getPlayersInLocalizations();
        assertEquals(4, playersInLocalizations.size());
        assertTrue(playersInLocalizations.containsKey(players.get(0)));
        assertTrue(playersInLocalizations.containsKey(players.get(2)));
        assertTrue(playersInLocalizations.containsKey(players.get(3)));
        assertTrue(playersInLocalizations.containsKey(players.get(5)));
        assertTrue(playersInLocalizations.get(players.get(0)).contains(localizations.get(2)));
        assertTrue(playersInLocalizations.get(players.get(0)).contains(localizations.get(3)));
        assertTrue(playersInLocalizations.get(players.get(2)).contains(localizations.get(3)));
        assertTrue(playersInLocalizations.get(players.get(5)).contains(localizations.get(0)));
        assertTrue(playersInLocalizations.get(players.get(3)).contains(localizations.get(1)));
        assertTrue(playersInLocalizations.get(players.get(3)).contains(localizations.get(2)));
    }

    @Test
    public void deserializePlayersAtTimeslotsTest() throws IOException {
        List<Player> players = event.getPlayers();
        List<Timeslot> timeslots = event.getTimeslots();
        event.addPlayerAtTimeslot(players.get(3), timeslots.get(3));
        event.addPlayerAtTimeslot(players.get(3), timeslots.get(4));
        event.addPlayerAtTimeslot(players.get(5), timeslots.get(0));
        event.addPlayerAtTimeslot(players.get(5), timeslots.get(1));
        event.addPlayerAtTimeslot(players.get(4), timeslots.get(4));

        Tournament t = mapper.readValue(getJsonBody(), Tournament.class);
        Event e = t.getEvents().get(0);
        players = e.getPlayers();
        timeslots = e.getTimeslots();

        Map<Player, Set<Timeslot>> playersAtTimeslots = e.getPlayersAtTimeslots();
        assertEquals(3, playersAtTimeslots.size());
        assertTrue(playersAtTimeslots.containsKey(players.get(3)));
        assertTrue(playersAtTimeslots.containsKey(players.get(5)));
        assertTrue(playersAtTimeslots.containsKey(players.get(4)));
        assertTrue(playersAtTimeslots.get(players.get(3)).contains(timeslots.get(3)));
        assertTrue(playersAtTimeslots.get(players.get(3)).contains(timeslots.get(4)));
        assertTrue(playersAtTimeslots.get(players.get(5)).contains(timeslots.get(0)));
        assertTrue(playersAtTimeslots.get(players.get(5)).contains(timeslots.get(1)));
        assertTrue(playersAtTimeslots.get(players.get(4)).contains(timeslots.get(4)));
    }

    @Test
    public void deserializeMatchupModeTest() throws IOException {
        event.setMatchesPerPlayer(2);
        event.setMatchupMode(TournamentSolver.MatchupMode.ALL_EQUAL);

        Tournament t = mapper.readValue(getJsonBody(), Tournament.class);
        Event e = t.getEvents().get(0);

        assertEquals(TournamentSolver.MatchupMode.ALL_EQUAL, e.getMatchupMode());
    }

    private String getJsonBody() throws JsonProcessingException {
        return mapper.writeValueAsString(tournament);
    }
}