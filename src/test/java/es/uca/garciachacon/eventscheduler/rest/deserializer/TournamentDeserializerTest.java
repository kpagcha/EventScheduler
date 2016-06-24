package es.uca.garciachacon.eventscheduler.rest.deserializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import es.uca.garciachacon.eventscheduler.data.model.tournament.*;
import es.uca.garciachacon.eventscheduler.solver.TournamentSolver;
import es.uca.garciachacon.eventscheduler.utils.TournamentUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Tests de la clase de deserialización de torneos, {@link TournamentDeserializer}. Además de la compatibilidad
 * bidireccional de serialización y deserialización de torneos.
 */
public class TournamentDeserializerTest {
    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

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
        JsonNode root = mapper.readTree(getJsonBody());

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
    public void deserializeWithMissingNameTest() throws IOException {
        String json = "{\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\"localizations\":[{\"name\":\"Court 1\"}," +
                "{\"name\":\"Court 2\"}],\"timeslots\":[{\"name\":\"Timeslot [order=0]\",\"chronologicalOrder\":0}," +
                "{\"name\":\"Timeslot [order=1]\",\"chronologicalOrder\":1},{\"name\":\"Timeslot [order=2]\"," +
                "\"chronologicalOrder\":2},{\"name\":\"Timeslot [order=3]\",\"chronologicalOrder\":3}]," +
                "\"events\":[{\"name\":\"Event\",\"players\":[0,1,2,3],\"localizations\":[0,1],\"timeslots\":[0,1,2," +
                "3],\"matchesPerPlayer\":1,\"timeslotsPerMatch\":2,\"playersPerMatch\":2,\"matchupMode\":\"ANY\"}]}";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Expected \"name\" textual field");
        mapper.readValue(json, Tournament.class);
    }

    @Test
    public void deserializeWithNonTextualNameTest() throws IOException {
        String json = "{\"name\":20,\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\"localizations\":[{\"name\":\"Court 1\"}," +
                "{\"name\":\"Court 2\"}],\"timeslots\":[{\"name\":\"Timeslot [order=0]\",\"chronologicalOrder\":0}," +
                "{\"name\":\"Timeslot [order=1]\",\"chronologicalOrder\":1},{\"name\":\"Timeslot [order=2]\"," +
                "\"chronologicalOrder\":2},{\"name\":\"Timeslot [order=3]\",\"chronologicalOrder\":3}]," +
                "\"events\":[{\"name\":\"Event\",\"players\":[0,1,2,3],\"localizations\":[0,1],\"timeslots\":[0,1,2," +
                "3],\"matchesPerPlayer\":1,\"timeslotsPerMatch\":2,\"playersPerMatch\":2,\"matchupMode\":\"ANY\"}]}\n";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Expected \"name\" textual field");
        mapper.readValue(json, Tournament.class);
    }

    @Test
    public void deserializeWithMissingPlayersTest() throws IOException {
        String json = "{\"name\":\"Tournament\",\"localizations\":[{\"name\":\"Court 1\"},{\"name\":\"Court 2\"}]," +
                "\"timeslots\":[{\"name\":\"Timeslot [order=0]\",\"chronologicalOrder\":0},{\"name\":\"Timeslot " +
                "[order=1]\",\"chronologicalOrder\":1},{\"name\":\"Timeslot [order=2]\",\"chronologicalOrder\":2}," +
                "{\"name\":\"Timeslot [order=3]\",\"chronologicalOrder\":3}],\"events\":[{\"name\":\"Event\"," +
                "\"players\":[0,1,2,3],\"localizations\":[0,1],\"timeslots\":[0,1,2,3],\"matchesPerPlayer\":1," +
                "\"timeslotsPerMatch\":2,\"playersPerMatch\":2,\"matchupMode\":\"ANY\"}]}";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Expected \"players\" array field");
        mapper.readValue(json, Tournament.class);
    }

    @Test
    public void deserializeWithNonArrayPlayersTest() throws IOException {
        String json = "{\"name\":\"Tournament\",\"players\":\"not an array\",\"localizations\":[{\"name\":\"Court " +
                "1\"},{\"name\":\"Court 2\"}],\"timeslots\":[{\"name\":\"Timeslot [order=0]\"," +
                "\"chronologicalOrder\":0},{\"name\":\"Timeslot [order=1]\",\"chronologicalOrder\":1}," +
                "{\"name\":\"Timeslot [order=2]\",\"chronologicalOrder\":2},{\"name\":\"Timeslot [order=3]\"," +
                "\"chronologicalOrder\":3}],\"events\":[{\"name\":\"Event\",\"players\":[0,1,2,3]," +
                "\"localizations\":[0,1],\"timeslots\":[0,1,2,3],\"matchesPerPlayer\":1,\"timeslotsPerMatch\":2," +
                "\"playersPerMatch\":2,\"matchupMode\":\"ANY\"}]}";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Expected \"players\" array field");
        mapper.readValue(json, Tournament.class);
    }

    @Test
    public void deserializeWithMissingLocalizationsTest() throws IOException {
        String json = "{\"name\":\"Tournament\",\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\"timeslots\":[{\"name\":\"Timeslot [order=0]\"," +
                "\"chronologicalOrder\":0},{\"name\":\"Timeslot [order=1]\",\"chronologicalOrder\":1}," +
                "{\"name\":\"Timeslot [order=2]\",\"chronologicalOrder\":2},{\"name\":\"Timeslot [order=3]\"," +
                "\"chronologicalOrder\":3}],\"events\":[{\"name\":\"Event\",\"players\":[0,1,2,3]," +
                "\"localizations\":[0,1],\"timeslots\":[0,1,2,3],\"matchesPerPlayer\":1,\"timeslotsPerMatch\":2," +
                "\"playersPerMatch\":2,\"matchupMode\":\"ANY\"}]}";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Expected \"localizations\" array field");
        mapper.readValue(json, Tournament.class);
    }

    @Test
    public void deserializeWithNonArrayLocalizationsTest() throws IOException {
        String json = "{\"name\":\"Tournament\",\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\"localizations\":109," +
                "\"timeslots\":[{\"name\":\"Timeslot [order=0]\",\"chronologicalOrder\":0},{\"name\":\"Timeslot " +
                "[order=1]\",\"chronologicalOrder\":1},{\"name\":\"Timeslot [order=2]\",\"chronologicalOrder\":2}," +
                "{\"name\":\"Timeslot [order=3]\",\"chronologicalOrder\":3}],\"events\":[{\"name\":\"Event\"," +
                "\"players\":[0,1,2,3],\"localizations\":[0,1],\"timeslots\":[0,1,2,3],\"matchesPerPlayer\":1," +
                "\"timeslotsPerMatch\":2,\"playersPerMatch\":2,\"matchupMode\":\"ANY\"}]}";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Expected \"localizations\" array field");
        mapper.readValue(json, Tournament.class);
    }

    @Test
    public void deserializeWithMissingTimeslotsTest() throws IOException {
        String json = "{\"name\":\"Tournament\",\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\"localizations\":[{\"name\":\"Court 1\"}," +
                "{\"name\":\"Court 2\"}]}";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Expected \"timeslots\" array field");
        mapper.readValue(json, Tournament.class);
    }

    @Test
    public void deserializeWithNonArrayTimeslotsTest() throws IOException {
        String json = "{\"name\":\"Tournament\",\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\"localizations\":[{\"name\":\"Court 1\"}," +
                "{\"name\":\"Court 2\"}],\"timeslots\":{}}";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Expected \"timeslots\" array field");
        mapper.readValue(json, Tournament.class);
    }

    @Test
    public void deserializeWithMissingEventsTest() throws IOException {
        String json = "{\"name\":\"Tournament\",\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\"localizations\":[{\"name\":\"Court 1\"}," +
                "{\"name\":\"Court 2\"}],\"timeslots\":[{\"name\":\"Timeslot [order=0]\",\"chronologicalOrder\":0}," +
                "{\"name\":\"Timeslot [order=1]\",\"chronologicalOrder\":1},{\"name\":\"Timeslot [order=2]\"," +
                "\"chronologicalOrder\":2},{\"name\":\"Timeslot [order=3]\",\"chronologicalOrder\":3}]}";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Expected \"events\" array field");
        mapper.readValue(json, Tournament.class);
    }

    @Test
    public void deserializeWithNonArrayEventsTest() throws IOException {
        String json = "{\"name\":\"Tournament\",\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\"localizations\":[{\"name\":\"Court 1\"}," +
                "{\"name\":\"Court 2\"}],\"timeslots\":[{\"name\":\"Timeslot [order=0]\",\"chronologicalOrder\":0}," +
                "{\"name\":\"Timeslot [order=1]\",\"chronologicalOrder\":1},{\"name\":\"Timeslot [order=2]\"," +
                "\"chronologicalOrder\":2},{\"name\":\"Timeslot [order=3]\",\"chronologicalOrder\":3}]," +
                "\"events\":null}";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Expected \"events\" array field");
        mapper.readValue(json, Tournament.class);
    }

    @Test
    public void deserializeWithNonArrayPlayersEventTest() throws IOException {
        String json = "{\"name\":\"Tournament\",\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\"localizations\":[{\"name\":\"Court 1\"}," +
                "{\"name\":\"Court 2\"}],\"timeslots\":[{\"name\":\"Timeslot [order=0]\",\"chronologicalOrder\":0}," +
                "{\"name\":\"Timeslot [order=1]\",\"chronologicalOrder\":1},{\"name\":\"Timeslot [order=2]\"," +
                "\"chronologicalOrder\":2},{\"name\":\"Timeslot [order=3]\",\"chronologicalOrder\":3}]," +
                "\"events\":[{\"name\":\"Event\",\"players\":null,\"localizations\":[0,1],\"timeslots\":[0,1,2,3]," +
                "\"matchesPerPlayer\":1,\"timeslotsPerMatch\":2,\"playersPerMatch\":2,\"matchupMode\":\"ANY\"}]}";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Field \"players\" expected to be an array");
        mapper.readValue(json, Tournament.class);
    }

    @Test
    public void deserializeWithNonIntegerPlayersElementEventTest() throws IOException {
        String json = "{\"name\":\"Tournament\",\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\"localizations\":[{\"name\":\"Court 1\"}," +
                "{\"name\":\"Court 2\"}],\"timeslots\":[{\"name\":\"Timeslot [order=0]\",\"chronologicalOrder\":0}," +
                "{\"name\":\"Timeslot [order=1]\",\"chronologicalOrder\":1},{\"name\":\"Timeslot [order=2]\"," +
                "\"chronologicalOrder\":2},{\"name\":\"Timeslot [order=3]\",\"chronologicalOrder\":3}]," +
                "\"events\":[{\"name\":\"Event\",\"players\":[0,1,{}],\"localizations\":[0,1],\"timeslots\":[0,1,2," +
                "3]," +
                "\"matchesPerPlayer\":1,\"timeslotsPerMatch\":2,\"playersPerMatch\":2,\"matchupMode\":\"ANY\"}]}";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Each item in \"players\" expected to be an integer");
        mapper.readValue(json, Tournament.class);
    }

    @Test
    public void deserializeWithPlayersOutOfBoundsEventTest() throws IOException {
        String json = "{\"name\":\"Tournament\",\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\"localizations\":[{\"name\":\"Court 1\"}," +
                "{\"name\":\"Court 2\"}],\"timeslots\":[{\"name\":\"Timeslot [order=0]\",\"chronologicalOrder\":0}," +
                "{\"name\":\"Timeslot [order=1]\",\"chronologicalOrder\":1},{\"name\":\"Timeslot [order=2]\"," +
                "\"chronologicalOrder\":2},{\"name\":\"Timeslot [order=3]\",\"chronologicalOrder\":3}]," +
                "\"events\":[{\"name\":\"Event\",\"players\":[0,1,8],\"localizations\":[0,1],\"timeslots\":[0,1,2," +
                "3]," +
                "\"matchesPerPlayer\":1,\"timeslotsPerMatch\":2,\"playersPerMatch\":2,\"matchupMode\":\"ANY\"}]}";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("IndexOutOfBoundsException at players element: Index: 8, Size: 4");
        mapper.readValue(json, Tournament.class);
    }

    @Test
    public void deserializeWithNonArrayLocalizationsEventTest() throws IOException {
        String json = "{\"name\":\"Tournament\",\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\"localizations\":[{\"name\":\"Court 1\"}," +
                "{\"name\":\"Court 2\"}],\"timeslots\":[{\"name\":\"Timeslot [order=0]\",\"chronologicalOrder\":0}," +
                "{\"name\":\"Timeslot [order=1]\",\"chronologicalOrder\":1},{\"name\":\"Timeslot [order=2]\"," +
                "\"chronologicalOrder\":2},{\"name\":\"Timeslot [order=3]\",\"chronologicalOrder\":3}]," +
                "\"events\":[{\"name\":\"Event\",\"players\":[0,1,2,3],\"localizations\":{},\"timeslots\":[0,1,2,3]," +
                "\"matchesPerPlayer\":1,\"timeslotsPerMatch\":2,\"playersPerMatch\":2,\"matchupMode\":\"ANY\"}]}";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Field \"localizations\" expected to be an array");
        mapper.readValue(json, Tournament.class);
    }

    @Test
    public void deserializeWithNonIntegerLocalizationsElementEventTest() throws IOException {
        String json = "{\"name\":\"Tournament\",\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\"localizations\":[{\"name\":\"Court 1\"}," +
                "{\"name\":\"Court 2\"}],\"timeslots\":[{\"name\":\"Timeslot [order=0]\",\"chronologicalOrder\":0}," +
                "{\"name\":\"Timeslot [order=1]\",\"chronologicalOrder\":1},{\"name\":\"Timeslot [order=2]\"," +
                "\"chronologicalOrder\":2},{\"name\":\"Timeslot [order=3]\",\"chronologicalOrder\":3}]," +
                "\"events\":[{\"name\":\"Event\",\"players\":[0,1,2,3],\"localizations\":[0,1,\"loc\"]," +
                "\"timeslots\":[0,1,2,3],\"matchesPerPlayer\":1,\"timeslotsPerMatch\":2,\"playersPerMatch\":2," +
                "\"matchupMode\":\"ANY\"}]}";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Each item in \"localizations\" expected to be an integer");
        mapper.readValue(json, Tournament.class);
    }

    @Test
    public void deserializeWithLocalizationsOutOfBoundsEventTest() throws IOException {
        String json = "{\"name\":\"Tournament\",\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\"localizations\":[{\"name\":\"Court 1\"}," +
                "{\"name\":\"Court 2\"}],\"timeslots\":[{\"name\":\"Timeslot [order=0]\",\"chronologicalOrder\":0}," +
                "{\"name\":\"Timeslot [order=1]\",\"chronologicalOrder\":1},{\"name\":\"Timeslot [order=2]\"," +
                "\"chronologicalOrder\":2},{\"name\":\"Timeslot [order=3]\",\"chronologicalOrder\":3}]," +
                "\"events\":[{\"name\":\"Event\",\"players\":[0,1,2,3],\"localizations\":[0,1,2],\"timeslots\":[0,1," +
                "2,3],\"matchesPerPlayer\":1,\"timeslotsPerMatch\":2,\"playersPerMatch\":2,\"matchupMode\":\"ANY\"}]}";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("IndexOutOfBoundsException at localizations element: Index: 2, Size: 2");
        mapper.readValue(json, Tournament.class);
    }

    @Test
    public void deserializeWithNonArrayTimeslotsEventTest() throws IOException {
        String json = "{\"name\":\"Tournament\",\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\"localizations\":[{\"name\":\"Court 1\"}," +
                "{\"name\":\"Court 2\"}],\"timeslots\":[{\"name\":\"Timeslot [order=0]\",\"chronologicalOrder\":0}," +
                "{\"name\":\"Timeslot [order=1]\",\"chronologicalOrder\":1},{\"name\":\"Timeslot [order=2]\"," +
                "\"chronologicalOrder\":2},{\"name\":\"Timeslot [order=3]\",\"chronologicalOrder\":3}]," +
                "\"events\":[{\"name\":\"Event\",\"players\":[0,1,2,3],\"localizations\":[0,1]," +
                "\"timeslots\":\"timeslots\",\"matchesPerPlayer\":1,\"timeslotsPerMatch\":2,\"playersPerMatch\":2," +
                "\"matchupMode\":\"ANY\"}]}";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Field \"timeslots\" expected to be an array");
        mapper.readValue(json, Tournament.class);
    }

    @Test
    public void deserializeWithNonIntegerTimeslotsElementEventTest() throws IOException {
        String json = "{\"name\":\"Tournament\",\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\"localizations\":[{\"name\":\"Court 1\"}," +
                "{\"name\":\"Court 2\"}],\"timeslots\":[{\"name\":\"Timeslot [order=0]\",\"chronologicalOrder\":0}," +
                "{\"name\":\"Timeslot [order=1]\",\"chronologicalOrder\":1},{\"name\":\"Timeslot [order=2]\"," +
                "\"chronologicalOrder\":2},{\"name\":\"Timeslot [order=3]\",\"chronologicalOrder\":3}]," +
                "\"events\":[{\"name\":\"Event\",\"players\":[0,1,2,3],\"localizations\":[0,1],\"timeslots\":[0,1,2," +
                "\"3\"],\"matchesPerPlayer\":1,\"timeslotsPerMatch\":2,\"playersPerMatch\":2," +
                "\"matchupMode\":\"ANY\"}]}";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Each item in \"timeslots\" expected to be an integer");
        mapper.readValue(json, Tournament.class);
    }

    @Test
    public void deserializeWithTimeslotsOutOfBoundsEventTest() throws IOException {
        String json = "{\"name\":\"Tournament\",\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\"localizations\":[{\"name\":\"Court 1\"}," +
                "{\"name\":\"Court 2\"}],\"timeslots\":[{\"name\":\"Timeslot [order=0]\",\"chronologicalOrder\":0}," +
                "{\"name\":\"Timeslot [order=1]\",\"chronologicalOrder\":1},{\"name\":\"Timeslot [order=2]\"," +
                "\"chronologicalOrder\":2},{\"name\":\"Timeslot [order=3]\",\"chronologicalOrder\":3}]," +
                "\"events\":[{\"name\":\"Event\",\"players\":[0,1,2,3],\"localizations\":[0,1],\"timeslots\":[0,10]," +
                "\"matchesPerPlayer\":1,\"timeslotsPerMatch\":2,\"playersPerMatch\":2,\"matchupMode\":\"ANY\"}]}";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("IndexOutOfBoundsException at timeslots element: Index: 10, Size: 4");
        mapper.readValue(json, Tournament.class);
    }

    @Test
    public void deserializeWithNonTextualNameEventTest() throws IOException {
        String json = "{\"name\":\"Tournament\",\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\"localizations\":[{\"name\":\"Court 1\"}," +
                "{\"name\":\"Court 2\"}],\"timeslots\":[{\"name\":\"Timeslot [order=0]\",\"chronologicalOrder\":0}," +
                "{\"name\":\"Timeslot [order=1]\",\"chronologicalOrder\":1},{\"name\":\"Timeslot [order=2]\"," +
                "\"chronologicalOrder\":2},{\"name\":\"Timeslot [order=3]\",\"chronologicalOrder\":3}]," +
                "\"events\":[{\"name\":null,\"players\":[0,1,2,3],\"localizations\":[0,1],\"timeslots\":[0,1," +
                "2,3],\"matchesPerPlayer\":1,\"timeslotsPerMatch\":2,\"playersPerMatch\":2,\"matchupMode\":\"ANY\"}]}";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Expected \"name\" textual field for the event");
        mapper.readValue(json, Tournament.class);
    }

    @Test
    public void deserializeMatchesPerPlayerTest() throws IOException {
        event.setMatchesPerPlayer(2);

        Tournament t = mapper.readValue(getJsonBody(), Tournament.class);
        assertEquals(2, t.getEvents().get(0).getMatchesPerPlayer());
    }

    @Test
    public void deserializeMatchesPerPlayerNonIntegerTest() throws IOException {
        String json = "{\"name\":\"Tournament\",\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\"localizations\":[{\"name\":\"Court 1\"}," +
                "{\"name\":\"Court 2\"}],\"timeslots\":[{\"name\":\"Timeslot [order=0]\",\"chronologicalOrder\":0}," +
                "{\"name\":\"Timeslot [order=1]\",\"chronologicalOrder\":1},{\"name\":\"Timeslot [order=2]\"," +
                "\"chronologicalOrder\":2},{\"name\":\"Timeslot [order=3]\",\"chronologicalOrder\":3}]," +
                "\"events\":[{\"name\":\"Event\",\"players\":[0,1,2,3],\"localizations\":[0,1],\"timeslots\":[0,1,2," +
                "3],\"matchesPerPlayer\":\"1\",\"timeslotsPerMatch\":2,\"playersPerMatch\":2," +
                "\"matchupMode\":\"ANY\"}]}";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Field \"matchesPerPlayer\" expected to be an integer");
        mapper.readValue(json, Tournament.class);
    }

    @Test
    public void deserializeTimeslotsPerMatchTest() throws IOException {
        event.setTimeslotsPerMatch(1);

        Tournament t = mapper.readValue(getJsonBody(), Tournament.class);
        assertEquals(1, t.getEvents().get(0).getTimeslotsPerMatch());
    }

    @Test
    public void deserializeTimeslotsPerMatchNonIntegerTest() throws IOException {
        String json = "{\"name\":\"Tournament\",\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\"localizations\":[{\"name\":\"Court 1\"}," +
                "{\"name\":\"Court 2\"}],\"timeslots\":[{\"name\":\"Timeslot [order=0]\",\"chronologicalOrder\":0}," +
                "{\"name\":\"Timeslot [order=1]\",\"chronologicalOrder\":1},{\"name\":\"Timeslot [order=2]\"," +
                "\"chronologicalOrder\":2},{\"name\":\"Timeslot [order=3]\",\"chronologicalOrder\":3}]," +
                "\"events\":[{\"name\":\"Event\",\"players\":[0,1,2,3],\"localizations\":[0,1],\"timeslots\":[0,1,2," +
                "3],\"matchesPerPlayer\":1,\"timeslotsPerMatch\":null,\"playersPerMatch\":2," +
                "\"matchupMode\":\"ANY\"}]}";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Field \"timeslotsPerMatch\" expected to be an integer");
        mapper.readValue(json, Tournament.class);
    }

    @Test
    public void deserializePlayersPerMatchTest() throws IOException {
        event.setPlayersPerMatch(4);

        Tournament t = mapper.readValue(getJsonBody(), Tournament.class);
        assertEquals(4, t.getEvents().get(0).getPlayersPerMatch());
    }

    @Test
    public void deserializePlayersPerMatchNonIntegerTest() throws IOException {
        String json = "{\"name\":\"Tournament\",\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\"localizations\":[{\"name\":\"Court 1\"}," +
                "{\"name\":\"Court 2\"}],\"timeslots\":[{\"name\":\"Timeslot [order=0]\",\"chronologicalOrder\":0}," +
                "{\"name\":\"Timeslot [order=1]\",\"chronologicalOrder\":1},{\"name\":\"Timeslot [order=2]\"," +
                "\"chronologicalOrder\":2},{\"name\":\"Timeslot [order=3]\",\"chronologicalOrder\":3}]," +
                "\"events\":[{\"name\":\"Event\",\"players\":[0,1,2,3],\"localizations\":[0,1],\"timeslots\":[0,1,2," +
                "3],\"matchesPerPlayer\":1,\"timeslotsPerMatch\":1,\"playersPerMatch\":null," +
                "\"matchupMode\":\"ANY\"}]}";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Field \"playersPerMatch\" expected to be an integer");
        mapper.readValue(json, Tournament.class);
    }

    @Test
    public void deserializePlayersPerTeamTest() throws IOException {
        event.setPlayersPerTeam(2);

        Tournament t = mapper.readValue(getJsonBody(), Tournament.class);
        assertEquals(2, t.getEvents().get(0).getPlayersPerTeam());
    }

    @Test
    public void deserializePlayersPerTeamNonIntegerTest() throws IOException {
        String json = "{\"name\":\"Tournament\",\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\"localizations\":[{\"name\":\"Court 1\"}," +
                "{\"name\":\"Court 2\"}],\"timeslots\":[{\"name\":\"Timeslot [order=0]\",\"chronologicalOrder\":0}," +
                "{\"name\":\"Timeslot [order=1]\",\"chronologicalOrder\":1},{\"name\":\"Timeslot [order=2]\"," +
                "\"chronologicalOrder\":2},{\"name\":\"Timeslot [order=3]\",\"chronologicalOrder\":3}]," +
                "\"events\":[{\"name\":\"Event\",\"players\":[0,1,2,3],\"localizations\":[0,1],\"timeslots\":[0,1,2," +
                "3],\"matchesPerPlayer\":1,\"timeslotsPerMatch\":1,\"playersPerMatch\":4,\"playersPerTeam\":[]," +
                "\"matchupMode\":\"ANY\"}]}";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Field \"playersPerTeam\" expected to be an integer");
        mapper.readValue(json, Tournament.class);
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


        String json = "{\"name\":\"Tournament\",\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"},{\"name\":\"Player 5\"},{\"name\":\"Player 6\"}," +
                "{\"name\":\"Player 7\"},{\"name\":\"Player 8\"}],\"localizations\":[{\"name\":\"Court 1\"}," +
                "{\"name\":\"Court 2\"},{\"name\":\"Court 3\"},{\"name\":\"Court 4\"}]," +
                "\"timeslots\":[{\"name\":\"Timeslot [order=0]\",\"chronologicalOrder\":0},{\"name\":\"Timeslot " +
                "[order=1]\",\"chronologicalOrder\":1},{\"name\":\"Timeslot [order=2]\",\"chronologicalOrder\":2}," +
                "{\"name\":\"Timeslot [order=3]\",\"chronologicalOrder\":3},{\"name\":\"Timeslot [order=4]\"," +
                "\"chronologicalOrder\":4},{\"name\":\"Timeslot [order=5]\",\"chronologicalOrder\":5}]," +
                "\"events\":[{\"name\":\"Event\",\"players\":[0,1,2,3,4,5,6,7],\"localizations\":[0,1,2,3]," +
                "\"timeslots\":[0,1,2,3,4,5],\"matchesPerPlayer\":1,\"timeslotsPerMatch\":2,\"playersPerMatch\":2," +
                "\"playersPerTeam\":2,\"teams\":[{\"players\":[0,3]},{\"players\":[4,5]}],\"matchupMode\":\"ANY\"}]}";

        t = mapper.readValue(json, Tournament.class);
        assertEquals(2, t.getEvents().get(0).getTeams().size());
    }

    @Test
    public void deserializeTeamsNonArrayTest() throws IOException {
        String json = "{\"name\":\"Tournament\",\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\"localizations\":[{\"name\":\"Court 1\"}]," +
                "\"timeslots\":[{\"name\":\"Timeslot [order=0]\",\"chronologicalOrder\":0}]," +
                "\"events\":[{\"name\":\"Event\",\"players\":[0,1,2,3],\"localizations\":[0],\"timeslots\":[0]," +
                "\"matchesPerPlayer\":1,\"timeslotsPerMatch\":2,\"playersPerMatch\":2,\"playersPerTeam\":2," +
                "\"teams\":{},\"matchupMode\":\"ANY\"}]}";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Field \"teams\" expected to be an array");
        mapper.readValue(json, Tournament.class);
    }

    @Test
    public void deserializeTeamNonObjectTest() throws IOException {
        String json = "{\"name\":\"Tournament\",\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\n" +
                "\"localizations\":[{\"name\":\"Court 1\"}],\"timeslots\":[{\"name\":\"Timeslot [order=0]\"," +
                "\"chronologicalOrder\":0}],\n" +
                "\"events\":[{\"name\":\"Event\",\"players\":[0,1,2,3],\"localizations\":[0],\n" +
                "\"timeslots\":[0],\"matchesPerPlayer\":1,\"timeslotsPerMatch\":2,\"playersPerMatch\":2," +
                "\"playersPerTeam\":2,\n" +
                "\"teams\":[\"team1\"],\"matchupMode\":\"ANY\"}]}";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Each item in \"teams\" array field expected to be an object");
        mapper.readValue(json, Tournament.class);
    }

    @Test
    public void deserializeTeamPlayersMissingTest() throws IOException {
        String json = "{\"name\":\"Tournament\",\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\n" +
                "\"localizations\":[{\"name\":\"Court 1\"}],\"timeslots\":[{\"name\":\"Timeslot [order=0]\"," +
                "\"chronologicalOrder\":0}],\n" +
                "\"events\":[{\"name\":\"Event\",\"players\":[0,1,2,3],\"localizations\":[0],\n" +
                "\"timeslots\":[0],\"matchesPerPlayer\":1,\"timeslotsPerMatch\":2,\"playersPerMatch\":2," +
                "\"playersPerTeam\":2,\n" +
                "\"teams\":[{\"name\":\"Player 1-Player 2\"}],\"matchupMode\":\"ANY\"}]}";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Expected \"players\" array field in the team");
        mapper.readValue(json, Tournament.class);
    }

    @Test
    public void deserializeTeamsPlayersNonArrayTest() throws IOException {
        String json = "\n" +
                "{\"name\":\"Tournament\",\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\n" +
                "\"localizations\":[{\"name\":\"Court 1\"}],\"timeslots\":[{\"name\":\"Timeslot [order=0]\"," +
                "\"chronologicalOrder\":0}],\n" +
                "\"events\":[{\"name\":\"Event\",\"players\":[0,1,2,3],\"localizations\":[0],\n" +
                "\"timeslots\":[0],\"matchesPerPlayer\":1,\"timeslotsPerMatch\":2,\"playersPerMatch\":2," +
                "\"playersPerTeam\":2,\n" +
                "\"teams\":[{\"name\":\"Player 1-Player 2\",\"players\":null}],\"matchupMode\":\"ANY\"}]}\n";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Expected \"players\" array field in the team");
        mapper.readValue(json, Tournament.class);
    }

    @Test
    public void deserializeTeamsPlayerNonIntegerTest() throws IOException {
        String json = "{\"name\":\"Tournament\",\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\n" +
                "\"localizations\":[{\"name\":\"Court 1\"}],\"timeslots\":[{\"name\":\"Timeslot [order=0]\"," +
                "\"chronologicalOrder\":0}],\n" +
                "\"events\":[{\"name\":\"Event\",\"players\":[0,1,2,3],\"localizations\":[0],\n" +
                "\"timeslots\":[0],\"matchesPerPlayer\":1,\"timeslotsPerMatch\":2,\"playersPerMatch\":2," +
                "\"playersPerTeam\":2,\n" +
                "\"teams\":[{\"name\":\"Player 1-Player 2\",\"players\":[{},0]}],\"matchupMode\":\"ANY\"}]}";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Each item in \"players\" array field expected to be an integer");
        mapper.readValue(json, Tournament.class);
    }

    @Test
    public void deserializeTeamsPlayerOutOfBoundsTest() throws IOException {
        String json = "\n" +
                "{\"name\":\"Tournament\",\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\n" +
                "\"localizations\":[{\"name\":\"Court 1\"}],\"timeslots\":[{\"name\":\"Timeslot [order=0]\"," +
                "\"chronologicalOrder\":0}],\n" +
                "\"events\":[{\"name\":\"Event\",\"players\":[0,1,2,3],\"localizations\":[0],\n" +
                "\"timeslots\":[0],\"matchesPerPlayer\":1,\"timeslotsPerMatch\":2,\"playersPerMatch\":2," +
                "\"playersPerTeam\":2,\n" +
                "\"teams\":[{\"name\":\"Player 1-Player 2\",\"players\":[4,0]}],\"matchupMode\":\"ANY\"}]}";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("IndexOutOfBoundsException at team players element");
        mapper.readValue(json, Tournament.class);
    }

    @Test
    public void deserializeTeamsNameNonTextualTest() throws IOException {
        String json = "{\"name\":\"Tournament\",\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\n" +
                "\"localizations\":[{\"name\":\"Court 1\"}],\"timeslots\":[{\"name\":\"Timeslot [order=0]\"," +
                "\"chronologicalOrder\":0}],\n" +
                "\"events\":[{\"name\":\"Event\",\"players\":[0,1,2,3],\"localizations\":[0],\n" +
                "\"timeslots\":[0],\"matchesPerPlayer\":1,\"timeslotsPerMatch\":2,\"playersPerMatch\":2," +
                "\"playersPerTeam\":2,\n" +
                "\"teams\":[{\"name\":10,\"players\":[1,0]}],\"matchupMode\":\"ANY\"}]}";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Field \"name\" expected to be textual for the team");
        mapper.readValue(json, Tournament.class);
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
    public void deserializeBreaksNonArrayTest() throws IOException {
        String json = "{\"name\":\"Tournament\",\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\n" +
                "\"localizations\":[{\"name\":\"Court 1\"}],\"timeslots\":[{\"name\":\"Timeslot [order=0]\"," +
                "\"chronologicalOrder\":0},{\"name\":\"Timeslot [order=1]\",\n" +
                "\"chronologicalOrder\":1},{\"name\":\"Timeslot [order=2]\",\"chronologicalOrder\":2}]," +
                "\"events\":[{\"name\":\"Event\",\"players\":[0,1,2,3],\n" +
                "\"localizations\":[0],\"timeslots\":[0,1,2],\"matchesPerPlayer\":1,\"timeslotsPerMatch\":2," +
                "\"playersPerMatch\":2,\"breaks\":null,\"matchupMode\":\"ANY\"}]}\n";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Field \"breaks\" expected to be an array");
        mapper.readValue(json, Tournament.class);
    }

    @Test
    public void deserializeBreakNonIntegerTest() throws IOException {
        String json = "{\"name\":\"Tournament\",\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\n" +
                "\"localizations\":[{\"name\":\"Court 1\"}],\"timeslots\":[{\"name\":\"Timeslot [order=0]\"," +
                "\"chronologicalOrder\":0},{\"name\":\"Timeslot [order=1]\",\n" +
                "\"chronologicalOrder\":1},{\"name\":\"Timeslot [order=2]\",\"chronologicalOrder\":2}]," +
                "\"events\":[{\"name\":\"Event\",\"players\":[0,1,2,3],\n" +
                "\"localizations\":[0],\"timeslots\":[0,1,2],\"matchesPerPlayer\":1,\"timeslotsPerMatch\":2," +
                "\"playersPerMatch\":2,\"breaks\":[null],\"matchupMode\":\"ANY\"}]}";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Each item in \"breaks\" array field expected to be an integer");
        mapper.readValue(json, Tournament.class);
    }

    @Test
    public void deserializeBreakOutOfBoundsTest() throws IOException {
        String json = "{\"name\":\"Tournament\",\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\n" +
                "\"localizations\":[{\"name\":\"Court 1\"}],\"timeslots\":[{\"name\":\"Timeslot [order=0]\"," +
                "\"chronologicalOrder\":0},{\"name\":\"Timeslot [order=1]\",\n" +
                "\"chronologicalOrder\":1},{\"name\":\"Timeslot [order=2]\",\"chronologicalOrder\":2}]," +
                "\"events\":[{\"name\":\"Event\",\"players\":[0,1,2,3],\n" +
                "\"localizations\":[0],\"timeslots\":[0,1,2],\"matchesPerPlayer\":1,\"timeslotsPerMatch\":2," +
                "\"playersPerMatch\":2,\"breaks\":[6],\"matchupMode\":\"ANY\"}]}";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("IndexOutOfBoundsException at breaks element");
        mapper.readValue(json, Tournament.class);
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
    public void deserializeUnavailablePlayersNonObjectTest() throws IOException {
        String json = "{\"name\":\"Tournament\",\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\n" +
                "\"localizations\":[{\"name\":\"Court 1\"},{\"name\":\"Court 2\"}]," +
                "\"timeslots\":[{\"name\":\"Timeslot [order=0]\",\"chronologicalOrder\":0},\n" +
                "{\"name\":\"Timeslot [order=1]\",\"chronologicalOrder\":1},{\"name\":\"Timeslot [order=2]\"," +
                "\"chronologicalOrder\":2},\n" +
                "{\"name\":\"Timeslot [order=3]\",\"chronologicalOrder\":3}],\"events\":[{\"name\":\"Event\"," +
                "\"players\":[0,1,2,3],\n" +
                "\"localizations\":[0,1],\"timeslots\":[0,1,2,3],\"matchesPerPlayer\":1,\"timeslotsPerMatch\":2," +
                "\"playersPerMatch\":2,\n" +
                "\"unavailablePlayers\":[0,3],\"matchupMode\":\"ANY\"}]}\n";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Field \"unavailablePlayers\" expected to be an object");
        mapper.readValue(json, Tournament.class);
    }

    @Test
    public void deserializeUnavailablePlayersTimeslotsNonArrayTest() throws IOException {
        String json = "{\"name\":\"Tournament\",\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\n" +
                "\"localizations\":[{\"name\":\"Court 1\"},{\"name\":\"Court 2\"}]," +
                "\"timeslots\":[{\"name\":\"Timeslot [order=0]\",\"chronologicalOrder\":0},\n" +
                "{\"name\":\"Timeslot [order=1]\",\"chronologicalOrder\":1},{\"name\":\"Timeslot [order=2]\"," +
                "\"chronologicalOrder\":2},\n" +
                "{\"name\":\"Timeslot [order=3]\",\"chronologicalOrder\":3}],\"events\":[{\"name\":\"Event\"," +
                "\"players\":[0,1,2,3],\n" +
                "\"localizations\":[0,1],\"timeslots\":[0,1,2,3],\"matchesPerPlayer\":1,\"timeslotsPerMatch\":2," +
                "\"playersPerMatch\":2,\n" +
                "\"unavailablePlayers\":{\"1\":0},\"matchupMode\":\"ANY\"}]}\n" +
                "\n";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Unavailable player timeslots expected to be an array");
        mapper.readValue(json, Tournament.class);
    }

    @Test
    public void deserializeUnavailablePlayersTimeslotNonIntegerTest() throws IOException {
        String json = "{\"name\":\"Tournament\",\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\n" +
                "\"localizations\":[{\"name\":\"Court 1\"},{\"name\":\"Court 2\"}]," +
                "\"timeslots\":[{\"name\":\"Timeslot [order=0]\",\"chronologicalOrder\":0},\n" +
                "{\"name\":\"Timeslot [order=1]\",\"chronologicalOrder\":1},{\"name\":\"Timeslot [order=2]\"," +
                "\"chronologicalOrder\":2},\n" +
                "{\"name\":\"Timeslot [order=3]\",\"chronologicalOrder\":3}],\"events\":[{\"name\":\"Event\"," +
                "\"players\":[0,1,2,3],\n" +
                "\"localizations\":[0,1],\"timeslots\":[0,1,2,3],\"matchesPerPlayer\":1,\"timeslotsPerMatch\":2," +
                "\"playersPerMatch\":2,\n" +
                "\"unavailablePlayers\":{\"1\":[\"3\"]},\"matchupMode\":\"ANY\"}]}\n";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Each item in unavailable player timeslots expected to be an integer");
        mapper.readValue(json, Tournament.class);
    }

    @Test
    public void deserializeUnavailablePlayersTimeslotOutOfBoundsTest() throws IOException {
        String json = "{\"name\":\"Tournament\",\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\n" +
                "\"localizations\":[{\"name\":\"Court 1\"},{\"name\":\"Court 2\"}]," +
                "\"timeslots\":[{\"name\":\"Timeslot [order=0]\",\"chronologicalOrder\":0},\n" +
                "{\"name\":\"Timeslot [order=1]\",\"chronologicalOrder\":1},{\"name\":\"Timeslot [order=2]\"," +
                "\"chronologicalOrder\":2},\n" +
                "{\"name\":\"Timeslot [order=3]\",\"chronologicalOrder\":3}],\"events\":[{\"name\":\"Event\"," +
                "\"players\":[0,1,2,3],\n" +
                "\"localizations\":[0,1],\"timeslots\":[0,1,2,3],\"matchesPerPlayer\":1,\"timeslotsPerMatch\":2," +
                "\"playersPerMatch\":2,\n" +
                "\"unavailablePlayers\":{\"1\":[5]},\"matchupMode\":\"ANY\"}]}";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("IndexOutOfBoundsException at unavailable player timeslots element");
        mapper.readValue(json, Tournament.class);
    }

    @Test
    public void deserializeUnavailablePlayersPlayerNonTextualIntegerTest() throws IOException {
        String json = "{\"name\":\"Tournament\",\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\n" +
                "\"localizations\":[{\"name\":\"Court 1\"},{\"name\":\"Court 2\"}]," +
                "\"timeslots\":[{\"name\":\"Timeslot [order=0]\",\"chronologicalOrder\":0},\n" +
                "{\"name\":\"Timeslot [order=1]\",\"chronologicalOrder\":1},{\"name\":\"Timeslot [order=2]\"," +
                "\"chronologicalOrder\":2},\n" +
                "{\"name\":\"Timeslot [order=3]\",\"chronologicalOrder\":3}],\"events\":[{\"name\":\"Event\"," +
                "\"players\":[0,1,2,3],\n" +
                "\"localizations\":[0,1],\"timeslots\":[0,1,2,3],\"matchesPerPlayer\":1,\"timeslotsPerMatch\":2," +
                "\"playersPerMatch\":2,\n" +
                "\"unavailablePlayers\":{\"abc\":[3]},\"matchupMode\":\"ANY\"}]}";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Unavailable player expected to be a textual integer");
        mapper.readValue(json, Tournament.class);
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
    public void deserializeUnavailableLocalizationsNonObjectTest() throws IOException {
        String json = "{\"name\":\"Tournament\",\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\n" +
                "\"localizations\":[{\"name\":\"Court 1\"},{\"name\":\"Court 2\"}]," +
                "\"timeslots\":[{\"name\":\"Timeslot [order=0]\",\"chronologicalOrder\":0},\n" +
                "{\"name\":\"Timeslot [order=1]\",\"chronologicalOrder\":1},{\"name\":\"Timeslot [order=2]\"," +
                "\"chronologicalOrder\":2},\n" +
                "{\"name\":\"Timeslot [order=3]\",\"chronologicalOrder\":3}],\"events\":[{\"name\":\"Event\"," +
                "\"players\":[0,1,2,3],\n" +
                "\"localizations\":[0,1],\"timeslots\":[0,1,2,3],\"matchesPerPlayer\":1,\"timeslotsPerMatch\":2," +
                "\"playersPerMatch\":2,\n" +
                "\"unavailableLocalizations\":[0,3],\"matchupMode\":\"ANY\"}]}\n";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Field \"unavailableLocalizations\" expected to be an object");
        mapper.readValue(json, Tournament.class);
    }

    @Test
    public void deserializeUnavailableLocalizationsTimeslotsNonArrayTest() throws IOException {
        String json = "{\"name\":\"Tournament\",\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\n" +
                "\"localizations\":[{\"name\":\"Court 1\"},{\"name\":\"Court 2\"}]," +
                "\"timeslots\":[{\"name\":\"Timeslot [order=0]\",\"chronologicalOrder\":0},\n" +
                "{\"name\":\"Timeslot [order=1]\",\"chronologicalOrder\":1},{\"name\":\"Timeslot [order=2]\"," +
                "\"chronologicalOrder\":2},\n" +
                "{\"name\":\"Timeslot [order=3]\",\"chronologicalOrder\":3}],\"events\":[{\"name\":\"Event\"," +
                "\"players\":[0,1,2,3],\n" +
                "\"localizations\":[0,1],\"timeslots\":[0,1,2,3],\"matchesPerPlayer\":1,\"timeslotsPerMatch\":2," +
                "\"playersPerMatch\":2,\n" +
                "\"unavailableLocalizations\":{\"1\":0},\"matchupMode\":\"ANY\"}]}\n" +
                "\n";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Unavailable localization timeslots expected to be an array");
        mapper.readValue(json, Tournament.class);
    }

    @Test
    public void deserializeUnavailableLocalizationsTimeslotNonIntegerTest() throws IOException {
        String json = "{\"name\":\"Tournament\",\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\n" +
                "\"localizations\":[{\"name\":\"Court 1\"},{\"name\":\"Court 2\"}]," +
                "\"timeslots\":[{\"name\":\"Timeslot [order=0]\",\"chronologicalOrder\":0},\n" +
                "{\"name\":\"Timeslot [order=1]\",\"chronologicalOrder\":1},{\"name\":\"Timeslot [order=2]\"," +
                "\"chronologicalOrder\":2},\n" +
                "{\"name\":\"Timeslot [order=3]\",\"chronologicalOrder\":3}],\"events\":[{\"name\":\"Event\"," +
                "\"players\":[0,1,2,3],\n" +
                "\"localizations\":[0,1],\"timeslots\":[0,1,2,3],\"matchesPerPlayer\":1,\"timeslotsPerMatch\":2," +
                "\"playersPerMatch\":2,\n" +
                "\"unavailableLocalizations\":{\"1\":[\"3\"]},\"matchupMode\":\"ANY\"}]}\n";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Each item in unavailable localization timeslots expected to be an integer");
        mapper.readValue(json, Tournament.class);
    }

    @Test
    public void deserializeUnavailableLocalizationsTimeslotOutOfBoundsTest() throws IOException {
        String json = "{\"name\":\"Tournament\",\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\n" +
                "\"localizations\":[{\"name\":\"Court 1\"},{\"name\":\"Court 2\"}]," +
                "\"timeslots\":[{\"name\":\"Timeslot [order=0]\",\"chronologicalOrder\":0},\n" +
                "{\"name\":\"Timeslot [order=1]\",\"chronologicalOrder\":1},{\"name\":\"Timeslot [order=2]\"," +
                "\"chronologicalOrder\":2},\n" +
                "{\"name\":\"Timeslot [order=3]\",\"chronologicalOrder\":3}],\"events\":[{\"name\":\"Event\"," +
                "\"players\":[0,1,2,3],\n" +
                "\"localizations\":[0,1],\"timeslots\":[0,1,2,3],\"matchesPerPlayer\":1,\"timeslotsPerMatch\":2," +
                "\"playersPerMatch\":2,\n" +
                "\"unavailableLocalizations\":{\"1\":[5]},\"matchupMode\":\"ANY\"}]}";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("IndexOutOfBoundsException at unavailable localization timeslots element");
        mapper.readValue(json, Tournament.class);
    }

    @Test
    public void deserializeUnavailableLocalizationsPlayerNonTextualIntegerTest() throws IOException {
        String json = "{\"name\":\"Tournament\",\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\n" +
                "\"localizations\":[{\"name\":\"Court 1\"},{\"name\":\"Court 2\"}]," +
                "\"timeslots\":[{\"name\":\"Timeslot [order=0]\",\"chronologicalOrder\":0},\n" +
                "{\"name\":\"Timeslot [order=1]\",\"chronologicalOrder\":1},{\"name\":\"Timeslot [order=2]\"," +
                "\"chronologicalOrder\":2},\n" +
                "{\"name\":\"Timeslot [order=3]\",\"chronologicalOrder\":3}],\"events\":[{\"name\":\"Event\"," +
                "\"players\":[0,1,2,3],\n" +
                "\"localizations\":[0,1],\"timeslots\":[0,1,2,3],\"matchesPerPlayer\":1,\"timeslotsPerMatch\":2," +
                "\"playersPerMatch\":2,\n" +
                "\"unavailableLocalizations\":{\"abc\":[3]},\"matchupMode\":\"ANY\"}]}";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Unavailable localization expected to be a textual integer");
        mapper.readValue(json, Tournament.class);
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
    public void deserializePredefinedMatchupsNonArrayTest() throws IOException {
        String json = "{\"name\":\"Tournament\",\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\n" +
                "\"localizations\":[{\"name\":\"Court 1\"},{\"name\":\"Court 2\"}]," +
                "\"timeslots\":[{\"name\":\"Timeslot [order=0]\",\"chronologicalOrder\":0},\n" +
                "{\"name\":\"Timeslot [order=1]\",\"chronologicalOrder\":1},{\"name\":\"Timeslot [order=2]\"," +
                "\"chronologicalOrder\":2},\n" +
                "{\"name\":\"Timeslot [order=3]\",\"chronologicalOrder\":3}],\"events\":[{\"name\":\"Event\"," +
                "\"players\":[0,1,2,3],\"localizations\":[0,1],\n" +
                "\"timeslots\":[0,1,2,3],\"matchesPerPlayer\":1,\"timeslotsPerMatch\":2,\"playersPerMatch\":2,\n" +
                "\"playersInLocalizations\":{\"3\":[1,0],\"1\":[1,0]},\"playersAtTimeslots\":{\"3\":[0,1,2],\"1\":[0," +
                "1,2]},\n" +
                "\"predefinedMatchups\":null,\"matchupMode\":\"ANY\"}]}\n";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Field \"predefinedMatchups\" expected to be an array");
        mapper.readValue(json, Tournament.class);
    }

    @Test
    public void deserializePredefinedMatchupsMatchupNonObjectTest() throws IOException {
        String json = "{\"name\":\"Tournament\",\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\n" +
                "\"localizations\":[{\"name\":\"Court 1\"},{\"name\":\"Court 2\"}]," +
                "\"timeslots\":[{\"name\":\"Timeslot [order=0]\",\"chronologicalOrder\":0},\n" +
                "{\"name\":\"Timeslot [order=1]\",\"chronologicalOrder\":1},{\"name\":\"Timeslot [order=2]\"," +
                "\"chronologicalOrder\":2},\n" +
                "{\"name\":\"Timeslot [order=3]\",\"chronologicalOrder\":3}],\"events\":[{\"name\":\"Event\"," +
                "\"players\":[0,1,2,3],\"localizations\":[0,1],\n" +
                "\"timeslots\":[0,1,2,3],\"matchesPerPlayer\":1,\"timeslotsPerMatch\":2,\"playersPerMatch\":2,\n" +
                "\"playersInLocalizations\":{\"3\":[1,0],\"1\":[1,0]},\"playersAtTimeslots\":{\"3\":[0,1,2],\"1\":[0," +
                "1,2]},\n" +
                "\"predefinedMatchups\":[0],\"matchupMode\":\"ANY\"}]}";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Each item in \"predefinedMatchups\" expected to be an object");
        mapper.readValue(json, Tournament.class);
    }

    @Test
    public void deserializePredefinedMatchupsMissingPlayersTest() throws IOException {
        String json = "{\"name\":\"Tournament\",\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\n" +
                "\"localizations\":[{\"name\":\"Court 1\"},{\"name\":\"Court 2\"}]," +
                "\"timeslots\":[{\"name\":\"Timeslot [order=0]\",\"chronologicalOrder\":0},\n" +
                "{\"name\":\"Timeslot [order=1]\",\"chronologicalOrder\":1},{\"name\":\"Timeslot [order=2]\"," +
                "\"chronologicalOrder\":2},\n" +
                "{\"name\":\"Timeslot [order=3]\",\"chronologicalOrder\":3}],\"events\":[{\"name\":\"Event\"," +
                "\"players\":[0,1,2,3],\"localizations\":[0,1],\n" +
                "\"timeslots\":[0,1,2,3],\"matchesPerPlayer\":1,\"timeslotsPerMatch\":2,\"playersPerMatch\":2,\n" +
                "\"playersInLocalizations\":{\"3\":[1,0],\"1\":[1,0]},\"playersAtTimeslots\":{\"3\":[0,1,2],\"1\":[0," +
                "1,2]},\n" +
                "\"predefinedMatchups\":[{\"localizations\":[1,0],\"timeslots\":[0,1,2],\"occurrences\":1}]," +
                "\"matchupMode\":\"ANY\"}]}\n";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Expected \"players\" array field in matchup");
        mapper.readValue(json, Tournament.class);
    }

    @Test
    public void deserializePredefinedMatchupsPlayersNonArrayTest() throws IOException {
        String json = "{\"name\":\"Tournament\",\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\n" +
                "\"localizations\":[{\"name\":\"Court 1\"},{\"name\":\"Court 2\"}]," +
                "\"timeslots\":[{\"name\":\"Timeslot [order=0]\",\"chronologicalOrder\":0},\n" +
                "{\"name\":\"Timeslot [order=1]\",\"chronologicalOrder\":1},{\"name\":\"Timeslot [order=2]\"," +
                "\"chronologicalOrder\":2},\n" +
                "{\"name\":\"Timeslot [order=3]\",\"chronologicalOrder\":3}],\"events\":[{\"name\":\"Event\"," +
                "\"players\":[0,1,2,3],\"localizations\":[0,1],\n" +
                "\"timeslots\":[0,1,2,3],\"matchesPerPlayer\":1,\"timeslotsPerMatch\":2,\"playersPerMatch\":2,\n" +
                "\"playersInLocalizations\":{\"3\":[1,0],\"1\":[1,0]},\"playersAtTimeslots\":{\"3\":[0,1,2],\"1\":[0," +
                "1,2]},\n" +
                "\"predefinedMatchups\":[{\"players\":{},\"localizations\":[1,0],\"timeslots\":[0,1,2]," +
                "\"occurrences\":1}],\"matchupMode\":\"ANY\"}]}";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Expected \"players\" array field in matchup");
        mapper.readValue(json, Tournament.class);
    }

    @Test
    public void deserializePredefinedMatchupsPlayerNonIntegerTest() throws IOException {
        String json = "{\"name\":\"Tournament\",\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\n" +
                "\"localizations\":[{\"name\":\"Court 1\"},{\"name\":\"Court 2\"}]," +
                "\"timeslots\":[{\"name\":\"Timeslot [order=0]\",\"chronologicalOrder\":0},\n" +
                "{\"name\":\"Timeslot [order=1]\",\"chronologicalOrder\":1},{\"name\":\"Timeslot [order=2]\"," +
                "\"chronologicalOrder\":2},\n" +
                "{\"name\":\"Timeslot [order=3]\",\"chronologicalOrder\":3}],\"events\":[{\"name\":\"Event\"," +
                "\"players\":[0,1,2,3],\"localizations\":[0,1],\n" +
                "\"timeslots\":[0,1,2,3],\"matchesPerPlayer\":1,\"timeslotsPerMatch\":2,\"playersPerMatch\":2,\n" +
                "\"playersInLocalizations\":{\"3\":[1,0],\"1\":[1,0]},\"playersAtTimeslots\":{\"3\":[0,1,2],\"1\":[0," +
                "1,2]},\n" +
                "\"predefinedMatchups\":[{\"players\":[3,null],\"localizations\":[1,0],\"timeslots\":[0,1,2]," +
                "\"occurrences\":1}],\"matchupMode\":\"ANY\"}]}";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Each item in matchup players expected to be an integer");
        mapper.readValue(json, Tournament.class);
    }

    @Test
    public void deserializePredefinedMatchupsPlayerOutOfBoundsTest() throws IOException {
        String json = "{\"name\":\"Tournament\",\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\n" +
                "\"localizations\":[{\"name\":\"Court 1\"},{\"name\":\"Court 2\"}]," +
                "\"timeslots\":[{\"name\":\"Timeslot [order=0]\",\"chronologicalOrder\":0},\n" +
                "{\"name\":\"Timeslot [order=1]\",\"chronologicalOrder\":1},{\"name\":\"Timeslot [order=2]\"," +
                "\"chronologicalOrder\":2},\n" +
                "{\"name\":\"Timeslot [order=3]\",\"chronologicalOrder\":3}],\"events\":[{\"name\":\"Event\"," +
                "\"players\":[0,1,2,3],\"localizations\":[0,1],\n" +
                "\"timeslots\":[0,1,2,3],\"matchesPerPlayer\":1,\"timeslotsPerMatch\":2,\"playersPerMatch\":2,\n" +
                "\"playersInLocalizations\":{\"3\":[1,0],\"1\":[1,0]},\"playersAtTimeslots\":{\"3\":[0,1,2],\"1\":[0," +
                "1,2]},\n" +
                "\"predefinedMatchups\":[{\"players\":[3,9],\"localizations\":[1,0],\"timeslots\":[0,1,2]," +
                "\"occurrences\":1}],\"matchupMode\":\"ANY\"}]}";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("IndexOutOfBoundsException at matchup players element");
        mapper.readValue(json, Tournament.class);
    }

    @Test
    public void deserializePredefinedMatchupsMissingLocalizationsTest() throws IOException {
        String json = "{\"name\":\"Tournament\",\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\n" +
                "\"localizations\":[{\"name\":\"Court 1\"},{\"name\":\"Court 2\"}]," +
                "\"timeslots\":[{\"name\":\"Timeslot [order=0]\",\"chronologicalOrder\":0},\n" +
                "{\"name\":\"Timeslot [order=1]\",\"chronologicalOrder\":1},{\"name\":\"Timeslot [order=2]\"," +
                "\"chronologicalOrder\":2},\n" +
                "{\"name\":\"Timeslot [order=3]\",\"chronologicalOrder\":3}],\"events\":[{\"name\":\"Event\"," +
                "\"players\":[0,1,2,3],\"localizations\":[0,1],\n" +
                "\"timeslots\":[0,1,2,3],\"matchesPerPlayer\":1,\"timeslotsPerMatch\":2,\"playersPerMatch\":2,\n" +
                "\"playersInLocalizations\":{\"3\":[1,0],\"1\":[1,0]},\"playersAtTimeslots\":{\"3\":[0,1,2],\"1\":[0," +
                "1,2]},\n" +
                "\"predefinedMatchups\":[{\"players\":[3,1],\"timeslots\":[0,1,2],\"occurrences\":1}]," +
                "\"matchupMode\":\"ANY\"}]}\n" +
                "\n";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Expected \"localizations\" array field in matchup");
        mapper.readValue(json, Tournament.class);
    }

    @Test
    public void deserializePredefinedMatchupsLocalizationsNonArrayTest() throws IOException {
        String json = "{\"name\":\"Tournament\",\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\n" +
                "\"localizations\":[{\"name\":\"Court 1\"},{\"name\":\"Court 2\"}]," +
                "\"timeslots\":[{\"name\":\"Timeslot [order=0]\",\"chronologicalOrder\":0},\n" +
                "{\"name\":\"Timeslot [order=1]\",\"chronologicalOrder\":1},{\"name\":\"Timeslot [order=2]\"," +
                "\"chronologicalOrder\":2},\n" +
                "{\"name\":\"Timeslot [order=3]\",\"chronologicalOrder\":3}],\"events\":[{\"name\":\"Event\"," +
                "\"players\":[0,1,2,3],\"localizations\":[0,1],\n" +
                "\"timeslots\":[0,1,2,3],\"matchesPerPlayer\":1,\"timeslotsPerMatch\":2,\"playersPerMatch\":2,\n" +
                "\"playersInLocalizations\":{\"3\":[1,0],\"1\":[1,0]},\"playersAtTimeslots\":{\"3\":[0,1,2],\"1\":[0," +
                "1,2]},\n" +
                "\"predefinedMatchups\":[{\"players\":[3,1],\"localizations\":null,\"timeslots\":[0,1,2]," +
                "\"occurrences\":1}],\"matchupMode\":\"ANY\"}]}";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Expected \"localizations\" array field in matchup");
        mapper.readValue(json, Tournament.class);
    }

    @Test
    public void deserializePredefinedMatchupsLocalizationNonIntegerTest() throws IOException {
        String json = "{\"name\":\"Tournament\",\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\n" +
                "\"localizations\":[{\"name\":\"Court 1\"},{\"name\":\"Court 2\"}]," +
                "\"timeslots\":[{\"name\":\"Timeslot [order=0]\",\"chronologicalOrder\":0},\n" +
                "{\"name\":\"Timeslot [order=1]\",\"chronologicalOrder\":1},{\"name\":\"Timeslot [order=2]\"," +
                "\"chronologicalOrder\":2},\n" +
                "{\"name\":\"Timeslot [order=3]\",\"chronologicalOrder\":3}],\"events\":[{\"name\":\"Event\"," +
                "\"players\":[0,1,2,3],\"localizations\":[0,1],\n" +
                "\"timeslots\":[0,1,2,3],\"matchesPerPlayer\":1,\"timeslotsPerMatch\":2,\"playersPerMatch\":2,\n" +
                "\"playersInLocalizations\":{\"3\":[1,0],\"1\":[1,0]},\"playersAtTimeslots\":{\"3\":[0,1,2],\"1\":[0," +
                "1,2]},\n" +
                "\"predefinedMatchups\":[{\"players\":[3,1],\"localizations\":[{},0],\"timeslots\":[0,1,2]," +
                "\"occurrences\":1}],\"matchupMode\":\"ANY\"}]}\n";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Each item in matchup localizations expected to be an integer");
        mapper.readValue(json, Tournament.class);
    }

    @Test
    public void deserializePredefinedMatchupsLocalizationOutOfBoundsTest() throws IOException {
        String json = "{\"name\":\"Tournament\",\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\n" +
                "\"localizations\":[{\"name\":\"Court 1\"},{\"name\":\"Court 2\"}]," +
                "\"timeslots\":[{\"name\":\"Timeslot [order=0]\",\"chronologicalOrder\":0},\n" +
                "{\"name\":\"Timeslot [order=1]\",\"chronologicalOrder\":1},{\"name\":\"Timeslot [order=2]\"," +
                "\"chronologicalOrder\":2},\n" +
                "{\"name\":\"Timeslot [order=3]\",\"chronologicalOrder\":3}],\"events\":[{\"name\":\"Event\"," +
                "\"players\":[0,1,2,3],\"localizations\":[0,1],\n" +
                "\"timeslots\":[0,1,2,3],\"matchesPerPlayer\":1,\"timeslotsPerMatch\":2,\"playersPerMatch\":2,\n" +
                "\"playersInLocalizations\":{\"3\":[1,0],\"1\":[1,0]},\"playersAtTimeslots\":{\"3\":[0,1,2],\"1\":[0," +
                "1,2]},\n" +
                "\"predefinedMatchups\":[{\"players\":[3,1],\"localizations\":[3,0],\"timeslots\":[0,1,2]," +
                "\"occurrences\":1}],\"matchupMode\":\"ANY\"}]}";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("IndexOutOfBoundsException at matchup localizations element");
        mapper.readValue(json, Tournament.class);
    }

    @Test
    public void deserializePredefinedMatchupsMissingTimeslotsTest() throws IOException {
        String json = "{\"name\":\"Tournament\",\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\n" +
                "\"localizations\":[{\"name\":\"Court 1\"},{\"name\":\"Court 2\"}]," +
                "\"timeslots\":[{\"name\":\"Timeslot [order=0]\",\"chronologicalOrder\":0},\n" +
                "{\"name\":\"Timeslot [order=1]\",\"chronologicalOrder\":1},{\"name\":\"Timeslot [order=2]\"," +
                "\"chronologicalOrder\":2},\n" +
                "{\"name\":\"Timeslot [order=3]\",\"chronologicalOrder\":3}],\"events\":[{\"name\":\"Event\"," +
                "\"players\":[0,1,2,3],\"localizations\":[0,1],\n" +
                "\"timeslots\":[0,1,2,3],\"matchesPerPlayer\":1,\"timeslotsPerMatch\":2,\"playersPerMatch\":2,\n" +
                "\"playersInLocalizations\":{\"3\":[1,0],\"1\":[1,0]},\"playersAtTimeslots\":{\"3\":[0,1,2],\"1\":[0," +
                "1,2]},\n" +
                "\"predefinedMatchups\":[{\"players\":[3,1],\"localizations\":[1,0],\"occurrences\":1}]," +
                "\"matchupMode\":\"ANY\"}]}";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Expected \"timeslots\" array field in matchup");
        mapper.readValue(json, Tournament.class);
    }

    @Test
    public void deserializePredefinedMatchupsTimeslotsNonArrayTest() throws IOException {
        String json = "{\"name\":\"Tournament\",\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\n" +
                "\"localizations\":[{\"name\":\"Court 1\"},{\"name\":\"Court 2\"}]," +
                "\"timeslots\":[{\"name\":\"Timeslot [order=0]\",\"chronologicalOrder\":0},\n" +
                "{\"name\":\"Timeslot [order=1]\",\"chronologicalOrder\":1},{\"name\":\"Timeslot [order=2]\"," +
                "\"chronologicalOrder\":2},\n" +
                "{\"name\":\"Timeslot [order=3]\",\"chronologicalOrder\":3}],\"events\":[{\"name\":\"Event\"," +
                "\"players\":[0,1,2,3],\"localizations\":[0,1],\n" +
                "\"timeslots\":[0,1,2,3],\"matchesPerPlayer\":1,\"timeslotsPerMatch\":2,\"playersPerMatch\":2,\n" +
                "\"playersInLocalizations\":{\"3\":[1,0],\"1\":[1,0]},\"playersAtTimeslots\":{\"3\":[0,1,2],\"1\":[0," +
                "1,2]},\n" +
                "\"predefinedMatchups\":[{\"players\":[3,1],\"localizations\":[1,0],\"timeslots\":10," +
                "\"occurrences\":1}],\"matchupMode\":\"ANY\"}]}\n";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Expected \"timeslots\" array field in matchup");
        mapper.readValue(json, Tournament.class);
    }

    @Test
    public void deserializePredefinedMatchupsTimeslotNonIntegerTest() throws IOException {
        String json = "{\"name\":\"Tournament\",\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\n" +
                "\"localizations\":[{\"name\":\"Court 1\"},{\"name\":\"Court 2\"}]," +
                "\"timeslots\":[{\"name\":\"Timeslot [order=0]\",\"chronologicalOrder\":0},\n" +
                "{\"name\":\"Timeslot [order=1]\",\"chronologicalOrder\":1},{\"name\":\"Timeslot [order=2]\"," +
                "\"chronologicalOrder\":2},\n" +
                "{\"name\":\"Timeslot [order=3]\",\"chronologicalOrder\":3}],\"events\":[{\"name\":\"Event\"," +
                "\"players\":[0,1,2,3],\"localizations\":[0,1],\n" +
                "\"timeslots\":[0,1,2,3],\"matchesPerPlayer\":1,\"timeslotsPerMatch\":2,\"playersPerMatch\":2,\n" +
                "\"playersInLocalizations\":{\"3\":[1,0],\"1\":[1,0]},\"playersAtTimeslots\":{\"3\":[0,1,2],\"1\":[0," +
                "1,2]},\n" +
                "\"predefinedMatchups\":[{\"players\":[3,1],\"localizations\":[1,0],\"timeslots\":[0,\"a\",2]," +
                "\"occurrences\":1}],\"matchupMode\":\"ANY\"}]}";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Each item in matchup timeslots expected to be an integer");
        mapper.readValue(json, Tournament.class);
    }

    @Test
    public void deserializePredefinedMatchupsTimeslotOutOfBoundsTest() throws IOException {
        String json = "{\"name\":\"Tournament\",\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\n" +
                "\"localizations\":[{\"name\":\"Court 1\"},{\"name\":\"Court 2\"}]," +
                "\"timeslots\":[{\"name\":\"Timeslot [order=0]\",\"chronologicalOrder\":0},\n" +
                "{\"name\":\"Timeslot [order=1]\",\"chronologicalOrder\":1},{\"name\":\"Timeslot [order=2]\"," +
                "\"chronologicalOrder\":2},\n" +
                "{\"name\":\"Timeslot [order=3]\",\"chronologicalOrder\":3}],\"events\":[{\"name\":\"Event\"," +
                "\"players\":[0,1,2,3],\"localizations\":[0,1],\n" +
                "\"timeslots\":[0,1,2,3],\"matchesPerPlayer\":1,\"timeslotsPerMatch\":2,\"playersPerMatch\":2,\n" +
                "\"playersInLocalizations\":{\"3\":[1,0],\"1\":[1,0]},\"playersAtTimeslots\":{\"3\":[0,1,2],\"1\":[0," +
                "1,2]},\n" +
                "\"predefinedMatchups\":[{\"players\":[3,1],\"localizations\":[1,0],\"timeslots\":[0,6,2]," +
                "\"occurrences\":1}],\"matchupMode\":\"ANY\"}]}";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("IndexOutOfBoundsException at matchup timeslots element");
        mapper.readValue(json, Tournament.class);
    }

    @Test
    public void deserializePredefinedMatchupsMissingOccurrencesTest() throws IOException {
        String json = "{\"name\":\"Tournament\",\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\n" +
                "\"localizations\":[{\"name\":\"Court 1\"},{\"name\":\"Court 2\"}]," +
                "\"timeslots\":[{\"name\":\"Timeslot [order=0]\",\"chronologicalOrder\":0},\n" +
                "{\"name\":\"Timeslot [order=1]\",\"chronologicalOrder\":1},{\"name\":\"Timeslot [order=2]\"," +
                "\"chronologicalOrder\":2},\n" +
                "{\"name\":\"Timeslot [order=3]\",\"chronologicalOrder\":3}],\"events\":[{\"name\":\"Event\"," +
                "\"players\":[0,1,2,3],\"localizations\":[0,1],\n" +
                "\"timeslots\":[0,1,2,3],\"matchesPerPlayer\":1,\"timeslotsPerMatch\":2,\"playersPerMatch\":2,\n" +
                "\"playersInLocalizations\":{\"3\":[1,0],\"1\":[1,0]},\"playersAtTimeslots\":{\"3\":[0,1,2],\"1\":[0," +
                "1,2]},\n" +
                "\"predefinedMatchups\":[{\"players\":[3,1],\"localizations\":[1,0],\"timeslots\":[0,1,2]}]," +
                "\"matchupMode\":\"ANY\"}]}";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Expected \"occurrences\" integer node in matchup");
        mapper.readValue(json, Tournament.class);
    }

    @Test
    public void deserializePredefinedMatchupsOccurrencesNonIntegerTest() throws IOException {
        String json = "{\"name\":\"Tournament\",\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\n" +
                "\"localizations\":[{\"name\":\"Court 1\"},{\"name\":\"Court 2\"}]," +
                "\"timeslots\":[{\"name\":\"Timeslot [order=0]\",\"chronologicalOrder\":0},\n" +
                "{\"name\":\"Timeslot [order=1]\",\"chronologicalOrder\":1},{\"name\":\"Timeslot [order=2]\"," +
                "\"chronologicalOrder\":2},\n" +
                "{\"name\":\"Timeslot [order=3]\",\"chronologicalOrder\":3}],\"events\":[{\"name\":\"Event\"," +
                "\"players\":[0,1,2,3],\"localizations\":[0,1],\n" +
                "\"timeslots\":[0,1,2,3],\"matchesPerPlayer\":1,\"timeslotsPerMatch\":2,\"playersPerMatch\":2,\n" +
                "\"playersInLocalizations\":{\"3\":[1,0],\"1\":[1,0]},\"playersAtTimeslots\":{\"3\":[0,1,2],\"1\":[0," +
                "1,2]},\n" +
                "\"predefinedMatchups\":[{\"players\":[3,1],\"localizations\":[1,0],\"timeslots\":[0,1,2]," +
                "\"occurrences\":{}}],\"matchupMode\":\"ANY\"}]}";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Expected \"occurrences\" integer node in matchup");
        mapper.readValue(json, Tournament.class);
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
    public void deserializePlayersInLocalizationsNonObjectTest() throws IOException {
        String json = "{\"name\":\"Tournament\",\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\n" +
                "\"localizations\":[{\"name\":\"Court 1\"},{\"name\":\"Court 2\"}]," +
                "\"timeslots\":[{\"name\":\"Timeslot [order=0]\",\"chronologicalOrder\":0},\n" +
                "{\"name\":\"Timeslot [order=1]\",\"chronologicalOrder\":1},{\"name\":\"Timeslot [order=2]\"," +
                "\"chronologicalOrder\":2},\n" +
                "{\"name\":\"Timeslot [order=3]\",\"chronologicalOrder\":3}],\"events\":[{\"name\":\"Event\"," +
                "\"players\":[0,1,2,3],\n" +
                "\"localizations\":[0,1],\"timeslots\":[0,1,2,3],\"matchesPerPlayer\":1,\"timeslotsPerMatch\":2," +
                "\"playersPerMatch\":2,\n" +
                "\"playersInLocalizations\":[],\"matchupMode\":\"ANY\"}]}\n";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Field \"playersInLocalizations\" expected to be an object");
        mapper.readValue(json, Tournament.class);
    }

    @Test
    public void deserializePlayersInLocalizationsPlayerNonTextualIntegerTest() throws IOException {
        String json = "{\"name\":\"Tournament\",\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\n" +
                "\"localizations\":[{\"name\":\"Court 1\"},{\"name\":\"Court 2\"}]," +
                "\"timeslots\":[{\"name\":\"Timeslot [order=0]\",\"chronologicalOrder\":0},\n" +
                "{\"name\":\"Timeslot [order=1]\",\"chronologicalOrder\":1},{\"name\":\"Timeslot [order=2]\"," +
                "\"chronologicalOrder\":2},\n" +
                "{\"name\":\"Timeslot [order=3]\",\"chronologicalOrder\":3}],\"events\":[{\"name\":\"Event\"," +
                "\"players\":[0,1,2,3],\n" +
                "\"localizations\":[0,1],\"timeslots\":[0,1,2,3],\"matchesPerPlayer\":1,\"timeslotsPerMatch\":2," +
                "\"playersPerMatch\":2,\n" +
                "\"playersInLocalizations\":{\"abc\":[0]},\"matchupMode\":\"ANY\"}]}";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Player expected to be a textual integer");
        mapper.readValue(json, Tournament.class);
    }

    @Test
    public void deserializePlayersInLocalizationsLocalizationsNonArrayTest() throws IOException {
        String json = "{\"name\":\"Tournament\",\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\n" +
                "\"localizations\":[{\"name\":\"Court 1\"},{\"name\":\"Court 2\"}]," +
                "\"timeslots\":[{\"name\":\"Timeslot [order=0]\",\"chronologicalOrder\":0},\n" +
                "{\"name\":\"Timeslot [order=1]\",\"chronologicalOrder\":1},{\"name\":\"Timeslot [order=2]\"," +
                "\"chronologicalOrder\":2},\n" +
                "{\"name\":\"Timeslot [order=3]\",\"chronologicalOrder\":3}],\"events\":[{\"name\":\"Event\"," +
                "\"players\":[0,1,2,3],\n" +
                "\"localizations\":[0,1],\"timeslots\":[0,1,2,3],\"matchesPerPlayer\":1,\"timeslotsPerMatch\":2," +
                "\"playersPerMatch\":2,\n" +
                "\"playersInLocalizations\":{\"2\":{}},\"matchupMode\":\"ANY\"}]}\n";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Assigned localizations expected to be an array");
        mapper.readValue(json, Tournament.class);
    }

    @Test
    public void deserializePlayersInLocalizationsLocalizationNonIntegerTest() throws IOException {
        String json = "{\"name\":\"Tournament\",\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\n" +
                "\"localizations\":[{\"name\":\"Court 1\"},{\"name\":\"Court 2\"}]," +
                "\"timeslots\":[{\"name\":\"Timeslot [order=0]\",\"chronologicalOrder\":0},\n" +
                "{\"name\":\"Timeslot [order=1]\",\"chronologicalOrder\":1},{\"name\":\"Timeslot [order=2]\"," +
                "\"chronologicalOrder\":2},\n" +
                "{\"name\":\"Timeslot [order=3]\",\"chronologicalOrder\":3}],\"events\":[{\"name\":\"Event\"," +
                "\"players\":[0,1,2,3],\n" +
                "\"localizations\":[0,1],\"timeslots\":[0,1,2,3],\"matchesPerPlayer\":1,\"timeslotsPerMatch\":2," +
                "\"playersPerMatch\":2,\n" +
                "\"playersInLocalizations\":{\"2\":[[]]},\"matchupMode\":\"ANY\"}]}";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Each item in assigned localizations expected to be an integer");
        mapper.readValue(json, Tournament.class);
    }

    @Test
    public void deserializePlayersInLocalizationsLocalizationOutOfBoundsTest() throws IOException {
        String json = "{\"name\":\"Tournament\",\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\n" +
                "\"localizations\":[{\"name\":\"Court 1\"},{\"name\":\"Court 2\"}]," +
                "\"timeslots\":[{\"name\":\"Timeslot [order=0]\",\"chronologicalOrder\":0},\n" +
                "{\"name\":\"Timeslot [order=1]\",\"chronologicalOrder\":1},{\"name\":\"Timeslot [order=2]\"," +
                "\"chronologicalOrder\":2},\n" +
                "{\"name\":\"Timeslot [order=3]\",\"chronologicalOrder\":3}],\"events\":[{\"name\":\"Event\"," +
                "\"players\":[0,1,2,3],\n" +
                "\"localizations\":[0,1],\"timeslots\":[0,1,2,3],\"matchesPerPlayer\":1,\"timeslotsPerMatch\":2," +
                "\"playersPerMatch\":2,\n" +
                "\"playersInLocalizations\":{\"2\":[9]},\"matchupMode\":\"ANY\"}]}";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("IndexOutOfBoundsException at assigned localizations element");
        mapper.readValue(json, Tournament.class);
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
    public void deserializePlayersAtTimeslotsNonObjectTest() throws IOException {
        String json = "{\"name\":\"Tournament\",\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\n" +
                "\"localizations\":[{\"name\":\"Court 1\"},{\"name\":\"Court 2\"}]," +
                "\"timeslots\":[{\"name\":\"Timeslot [order=0]\",\"chronologicalOrder\":0},\n" +
                "{\"name\":\"Timeslot [order=1]\",\"chronologicalOrder\":1},{\"name\":\"Timeslot [order=2]\"," +
                "\"chronologicalOrder\":2},\n" +
                "{\"name\":\"Timeslot [order=3]\",\"chronologicalOrder\":3}],\"events\":[{\"name\":\"Event\"," +
                "\"players\":[0,1,2,3],\n" +
                "\"localizations\":[0,1],\"timeslots\":[0,1,2,3],\"matchesPerPlayer\":1,\"timeslotsPerMatch\":2," +
                "\"playersPerMatch\":2,\n" +
                "\"playersAtTimeslots\":[],\"matchupMode\":\"ANY\"}]}\n";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Field \"playersAtTimeslots\" expected to be an object");
        mapper.readValue(json, Tournament.class);
    }

    @Test
    public void deserializePlayersAtTimeslotsPlayerNonTextualIntegerTest() throws IOException {
        String json = "{\"name\":\"Tournament\",\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\n" +
                "\"localizations\":[{\"name\":\"Court 1\"},{\"name\":\"Court 2\"}]," +
                "\"timeslots\":[{\"name\":\"Timeslot [order=0]\",\"chronologicalOrder\":0},\n" +
                "{\"name\":\"Timeslot [order=1]\",\"chronologicalOrder\":1},{\"name\":\"Timeslot [order=2]\"," +
                "\"chronologicalOrder\":2},\n" +
                "{\"name\":\"Timeslot [order=3]\",\"chronologicalOrder\":3}],\"events\":[{\"name\":\"Event\"," +
                "\"players\":[0,1,2,3],\n" +
                "\"localizations\":[0,1],\"timeslots\":[0,1,2,3],\"matchesPerPlayer\":1,\"timeslotsPerMatch\":2," +
                "\"playersPerMatch\":2,\n" +
                "\"playersAtTimeslots\":{\"abc\":[0]},\"matchupMode\":\"ANY\"}]}";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Player expected to be a textual integer");
        mapper.readValue(json, Tournament.class);
    }

    @Test
    public void deserializePlayersAtTimeslotsTimeslotsNonArrayTest() throws IOException {
        String json = "{\"name\":\"Tournament\",\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\n" +
                "\"localizations\":[{\"name\":\"Court 1\"},{\"name\":\"Court 2\"}]," +
                "\"timeslots\":[{\"name\":\"Timeslot [order=0]\",\"chronologicalOrder\":0},\n" +
                "{\"name\":\"Timeslot [order=1]\",\"chronologicalOrder\":1},{\"name\":\"Timeslot [order=2]\"," +
                "\"chronologicalOrder\":2},\n" +
                "{\"name\":\"Timeslot [order=3]\",\"chronologicalOrder\":3}],\"events\":[{\"name\":\"Event\"," +
                "\"players\":[0,1,2,3],\n" +
                "\"localizations\":[0,1],\"timeslots\":[0,1,2,3],\"matchesPerPlayer\":1,\"timeslotsPerMatch\":2," +
                "\"playersPerMatch\":2,\n" +
                "\"playersAtTimeslots\":{\"2\":{}},\"matchupMode\":\"ANY\"}]}\n";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Assigned timeslots expected to be an array");
        mapper.readValue(json, Tournament.class);
    }

    @Test
    public void deserializePlayersAtTimeslotsTimeslotNonIntegerTest() throws IOException {
        String json = "{\"name\":\"Tournament\",\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\n" +
                "\"localizations\":[{\"name\":\"Court 1\"},{\"name\":\"Court 2\"}]," +
                "\"timeslots\":[{\"name\":\"Timeslot [order=0]\",\"chronologicalOrder\":0},\n" +
                "{\"name\":\"Timeslot [order=1]\",\"chronologicalOrder\":1},{\"name\":\"Timeslot [order=2]\"," +
                "\"chronologicalOrder\":2},\n" +
                "{\"name\":\"Timeslot [order=3]\",\"chronologicalOrder\":3}],\"events\":[{\"name\":\"Event\"," +
                "\"players\":[0,1,2,3],\n" +
                "\"localizations\":[0,1],\"timeslots\":[0,1,2,3],\"matchesPerPlayer\":1,\"timeslotsPerMatch\":2," +
                "\"playersPerMatch\":2,\n" +
                "\"playersAtTimeslots\":{\"2\":[[]]},\"matchupMode\":\"ANY\"}]}";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Each item in assigned timeslots expected to be an integer");
        mapper.readValue(json, Tournament.class);
    }

    @Test
    public void deserializePlayersAtTimeslotsTimeslotOutOfBoundsTest() throws IOException {
        String json = "{\"name\":\"Tournament\",\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\n" +
                "\"localizations\":[{\"name\":\"Court 1\"},{\"name\":\"Court 2\"}]," +
                "\"timeslots\":[{\"name\":\"Timeslot [order=0]\",\"chronologicalOrder\":0},\n" +
                "{\"name\":\"Timeslot [order=1]\",\"chronologicalOrder\":1},{\"name\":\"Timeslot [order=2]\"," +
                "\"chronologicalOrder\":2},\n" +
                "{\"name\":\"Timeslot [order=3]\",\"chronologicalOrder\":3}],\"events\":[{\"name\":\"Event\"," +
                "\"players\":[0,1,2,3],\n" +
                "\"localizations\":[0,1],\"timeslots\":[0,1,2,3],\"matchesPerPlayer\":1,\"timeslotsPerMatch\":2," +
                "\"playersPerMatch\":2,\n" +
                "\"playersAtTimeslots\":{\"2\":[9]},\"matchupMode\":\"ANY\"}]}";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("IndexOutOfBoundsException at assigned timeslots element");
        mapper.readValue(json, Tournament.class);
    }

    @Test
    public void deserializeMatchupModeTest() throws IOException {
        event.setMatchesPerPlayer(2);
        event.setMatchupMode(TournamentSolver.MatchupMode.ALL_EQUAL);

        Tournament t = mapper.readValue(getJsonBody(), Tournament.class);
        Event e = t.getEvents().get(0);

        assertEquals(TournamentSolver.MatchupMode.ALL_EQUAL, e.getMatchupMode());


        event.setMatchupMode(TournamentSolver.MatchupMode.ALL_DIFFERENT);

        t = mapper.readValue(getJsonBody(), Tournament.class);
        e = t.getEvents().get(0);

        assertEquals(TournamentSolver.MatchupMode.ALL_DIFFERENT, e.getMatchupMode());


        event.setMatchupMode(TournamentSolver.MatchupMode.CUSTOM);

        t = mapper.readValue(getJsonBody(), Tournament.class);
        e = t.getEvents().get(0);

        assertEquals(TournamentSolver.MatchupMode.CUSTOM, e.getMatchupMode());
    }

    @Test
    public void deserializeMatchupModeNonTextualTest() throws IOException {
        String json = "{\"name\":\"Tournament\",\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\"localizations\":[{\"name\":\"Court 1\"}," +
                "{\"name\":\"Court 2\"}],\"timeslots\":[{\"name\":\"Timeslot [order=0]\",\"chronologicalOrder\":0}," +
                "{\"name\":\"Timeslot [order=1]\",\"chronologicalOrder\":1},{\"name\":\"Timeslot [order=2]\"," +
                "\"chronologicalOrder\":2},{\"name\":\"Timeslot [order=3]\",\"chronologicalOrder\":3}]," +
                "\"events\":[{\"name\":\"Event\",\"players\":[0,1,2,3],\"localizations\":[0,1],\"timeslots\":[0,1,2," +
                "3],\"matchesPerPlayer\":1,\"timeslotsPerMatch\":2,\"playersPerMatch\":2,\"matchupMode\":1}]}\n";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Field \"matchupMode\" expected to be textual");
        mapper.readValue(json, Tournament.class);
    }

    @Test
    public void deserializeMatchupModeUnknownTest() throws IOException {
        String json = "{\"name\":\"Tournament\",\"players\":[{\"name\":\"Player 1\"},{\"name\":\"Player 2\"}," +
                "{\"name\":\"Player 3\"},{\"name\":\"Player 4\"}],\"localizations\":[{\"name\":\"Court 1\"}," +
                "{\"name\":\"Court 2\"}],\"timeslots\":[{\"name\":\"Timeslot [order=0]\",\"chronologicalOrder\":0}," +
                "{\"name\":\"Timeslot [order=1]\",\"chronologicalOrder\":1},{\"name\":\"Timeslot [order=2]\"," +
                "\"chronologicalOrder\":2},{\"name\":\"Timeslot [order=3]\",\"chronologicalOrder\":3}]," +
                "\"events\":[{\"name\":\"Event\",\"players\":[0,1,2,3],\"localizations\":[0,1],\"timeslots\":[0,1,2," +
                "3],\"matchesPerPlayer\":1,\"timeslotsPerMatch\":2,\"playersPerMatch\":2," +
                "\"matchupMode\":\"unknown\"}]}\n";

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Unknown matchup mode value");
        mapper.readValue(json, Tournament.class);
    }

    private String getJsonBody() throws JsonProcessingException {
        return mapper.writeValueAsString(tournament);
    }
}