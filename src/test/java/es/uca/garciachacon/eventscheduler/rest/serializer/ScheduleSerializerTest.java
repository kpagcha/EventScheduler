package es.uca.garciachacon.eventscheduler.rest.serializer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.uca.garciachacon.eventscheduler.data.model.schedule.InverseSchedule;
import es.uca.garciachacon.eventscheduler.data.model.schedule.Schedule;
import es.uca.garciachacon.eventscheduler.data.model.tournament.*;
import es.uca.garciachacon.eventscheduler.data.validation.validable.ValidationException;
import es.uca.garciachacon.eventscheduler.utils.TournamentUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Tests del serializador de un horario, {@link ScheduleSerializer}.
 */
public class ScheduleSerializerTest {
    private Schedule schedule;
    private Tournament tournament;
    private Event event;
    private Event smallEvent;
    private ObjectMapper mapper;

    @Before
    public void setUp() throws ValidationException {
        mapper = new ObjectMapper();

        List<Player> players = TournamentUtils.buildGenericPlayers(8, "Player");
        List<Localization> localizations = TournamentUtils.buildGenericLocalizations(3, "Court");
        List<Timeslot> timeslots = TournamentUtils.buildSimpleTimeslots(8);

        event = new Event("Main Event", players, localizations.subList(1, 3), timeslots);
        smallEvent =
                new Event("Small Event", players.subList(2, 6), localizations.subList(0, 2), timeslots.subList(3, 8));

        smallEvent.addMatchup(new Matchup(
                smallEvent,
                new HashSet<>(Arrays.asList(players.get(2), players.get(3))),
                new HashSet<>(Arrays.asList(localizations.get(0))),
                new HashSet<>(Arrays.asList(timeslots.get(3))),
                1
        ));

        tournament = new Tournament("Tournament", event, smallEvent);
        tournament.solve();
    }

    @Test
    public void serializeTournamentSchedule() throws IOException {
        schedule = tournament.getSchedule();

        JsonNode root = mapper.readTree(schedule.toJson());

        assertEquals("Tournament", root.get("name").asText());
        assertEquals("Tournament", root.get("tournament").asText());
        assertEquals(8, root.get("players").size());
        assertEquals(3, root.get("localizations").size());
        assertEquals(8, root.get("timeslots").size());
        assertEquals(8, root.get("scheduleValues").size());
        for (int i = 0; i < 8; i++)
            assertEquals(8, root.get("scheduleValues").get(i).size());
        assertEquals(6, root.get("matches").size());
        assertEquals(24, root.get("totalTimeslots").asInt());
        assertEquals(24, root.get("availableTimeslots").asInt());
        assertEquals(8, root.get("occupation").asInt());
        assertEquals(0.333, root.get("occupationRatio").asDouble(), 0.001);
    }

    @Test
    public void serializeMainEventSchedule() throws IOException {
        schedule = tournament.getEventSchedules().get(event);

        JsonNode root = mapper.readTree(schedule.toJson());

        assertEquals("Main Event", root.get("name").asText());
        assertEquals("Tournament", root.get("tournament").asText());
        assertEquals(8, root.get("players").size());
        assertEquals(2, root.get("localizations").size());
        assertEquals(8, root.get("timeslots").size());
        assertEquals(4, root.get("matches").size());
        assertEquals(8, root.get("scheduleValues").size());
        for (int i = 0; i < 8; i++)
            assertEquals(8, root.get("scheduleValues").get(i).size());
        assertEquals(16, root.get("totalTimeslots").asInt());
        assertEquals(16, root.get("availableTimeslots").asInt());
        assertEquals(4, root.get("occupation").asInt());
        assertEquals(0.25, root.get("occupationRatio").asDouble(), 0.001);
    }

    @Test
    public void serializeSmallEventSchedule() throws IOException {
        schedule = tournament.getEventSchedules().get(smallEvent);

        JsonNode root = mapper.readTree(schedule.toJson());

        assertEquals("Small Event", root.get("name").asText());
        assertEquals("Tournament", root.get("tournament").asText());
        assertEquals(4, root.get("players").size());
        assertEquals(2, root.get("localizations").size());
        assertEquals(5, root.get("timeslots").size());
        assertEquals(4, root.get("scheduleValues").size());
        for (int i = 0; i < 4; i++)
            assertEquals(5, root.get("scheduleValues").get(i).size());
        assertEquals(2, root.get("matches").size());
        assertEquals(10, root.get("totalTimeslots").asInt());
        assertEquals(10, root.get("availableTimeslots").asInt());
        assertEquals(4, root.get("occupation").asInt());
        assertEquals(0.4, root.get("occupationRatio").asDouble(), 0.001);

        JsonNode matchNode = root.get("matches").get(0);

        JsonNode playersNode = matchNode.get("players");
        assertEquals("Player 3", playersNode.get(0).get("name").asText());
        assertEquals("Player 4", playersNode.get(1).get("name").asText());

        assertEquals("Court 1", matchNode.get("localization").get("name").asText());

        assertEquals(3, matchNode.get("startTimeslot").get("chronologicalOrder").asInt());
        assertEquals(4, matchNode.get("endTimeslot").get("chronologicalOrder").asInt());

        assertEquals(2, matchNode.get("duration").asInt());

        // Las posiciones del horario [0,0], [0,1], [1,0] y [1,1] son con las que se corresponden al enfrentamiento
        // predeterminado que se definió
        JsonNode matchInScheduleNode;
        for (int i = 0; i <= 1; i++) {
            for (int j = 0; j <= 1; j++) {
                matchInScheduleNode = root.get("scheduleValues").get(i).get(j);

                assertEquals("OCCUPIED", matchInScheduleNode.get("value").asText());
                assertEquals(0, matchInScheduleNode.get("localization").asInt());
            }
        }
    }

    @Test
    public void serializeInverseScheduleTest() throws IOException {
        schedule = new InverseSchedule(smallEvent);

        JsonNode root = mapper.readTree(schedule.toJson());

        System.out.println(schedule.toJson());

        assertEquals("Small Event", root.get("name").asText());
        assertEquals("Tournament", root.get("tournament").asText());
        assertEquals(4, root.get("players").size());
        assertEquals(2, root.get("localizations").size());
        assertEquals(5, root.get("timeslots").size());
        assertEquals(2, root.get("scheduleValues").size());
        for (int i = 0; i < 2; i++)
            assertEquals(5, root.get("scheduleValues").get(i).size());
        assertEquals(2, root.get("matches").size());
        assertEquals(10, root.get("totalTimeslots").asInt());
        assertEquals(10, root.get("availableTimeslots").asInt());
        assertEquals(4, root.get("occupation").asInt());
        assertEquals(0.4, root.get("occupationRatio").asDouble(), 0.001);

        JsonNode matchNode = root.get("matches").get(0);

        JsonNode playersNode = matchNode.get("players");
        assertEquals("Player 3", playersNode.get(0).get("name").asText());
        assertEquals("Player 4", playersNode.get(1).get("name").asText());

        assertEquals("Court 1", matchNode.get("localization").get("name").asText());

        assertEquals(3, matchNode.get("startTimeslot").get("chronologicalOrder").asInt());
        assertEquals(4, matchNode.get("endTimeslot").get("chronologicalOrder").asInt());

        assertEquals(2, matchNode.get("duration").asInt());

        JsonNode matchInScheduleNode = root.get("scheduleValues").get(0).get(0);
        assertEquals("OCCUPIED", matchInScheduleNode.get("value").asText());
        assertEquals(0, matchInScheduleNode.get("players").get(0).asInt());
        assertEquals(1, matchInScheduleNode.get("players").get(1).asInt());

        matchInScheduleNode = root.get("scheduleValues").get(0).get(1);
        assertEquals("CONTINUATION", matchInScheduleNode.get("value").asText());
    }
}
