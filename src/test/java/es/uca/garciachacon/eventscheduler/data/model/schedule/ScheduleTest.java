package es.uca.garciachacon.eventscheduler.data.model.schedule;

import es.uca.garciachacon.eventscheduler.data.model.schedule.value.AbstractScheduleValue;
import es.uca.garciachacon.eventscheduler.data.model.schedule.value.ScheduleValue;
import es.uca.garciachacon.eventscheduler.data.model.schedule.value.ScheduleValueOccupied;
import es.uca.garciachacon.eventscheduler.data.model.schedule.value.Value;
import es.uca.garciachacon.eventscheduler.data.model.tournament.*;
import es.uca.garciachacon.eventscheduler.data.validation.validable.ValidationException;
import es.uca.garciachacon.eventscheduler.solver.TournamentSolver;
import es.uca.garciachacon.eventscheduler.utils.TournamentUtils;
import org.hamcrest.core.StringContains;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;

/**
 * Tests de las clases {@link Schedule}, {@link EventSchedule} y {@link TournamentSchedule}.
 * <p>
 * Include además, test de {@link AbstractScheduleValue} y {@linkplain Value}.
 */
public class ScheduleTest {

    private Tournament tournament;

    @Before
    public void setUp() {
        List<Localization> localizations = TournamentUtils.buildGenericLocalizations(2, "Court");
        List<Timeslot> timeslots = TournamentUtils.buildSimpleTimeslots(8);

        Event singles = new Event("Singles", TournamentUtils.buildGenericPlayers(8, "SPl"), localizations, timeslots);
        Event doubles = new Event("Doubles",
                TournamentUtils.buildGenericPlayers(8, "DPl"),
                localizations,
                timeslots.subList(3, 8),
                1,
                2,
                4
        );

        List<Player> sPlayers = singles.getPlayers();
        singles.addMatchup(sPlayers.get(0), sPlayers.get(7));

        List<Player> dPlayers = doubles.getPlayers();
        doubles.addTeam(dPlayers.get(0), dPlayers.get(5));
        doubles.addTeam(dPlayers.get(7), dPlayers.get(2));
        doubles.addTeam(dPlayers.get(4), dPlayers.get(1));
        doubles.addTeam(dPlayers.get(3), dPlayers.get(6));

        tournament = new Tournament("Tennis Tournament", singles, doubles);

        tournament.addBreak(timeslots.get(5));
        tournament.addUnavailableLocalizationAtTimeslot(localizations.get(0), timeslots.get(0));
        tournament.addUnavailablePlayerAtTimeslot(sPlayers.get(0), timeslots.get(7));

        tournament.getSolver().setSearchStrategy(TournamentSolver.SearchStrategy.MINDOM_UB);
    }

    @Test
    public void eventScheduleTest() throws ValidationException {
        assertNull(tournament.getCurrentSchedules());

        tournament.solve();
        Map<Event, EventSchedule> schedules = tournament.getCurrentSchedules();

        assertNotNull(schedules);

        for (Event event : tournament.getEvents()) {
            EventSchedule schedule = schedules.get(event);
            List<Match> matches = schedule.getMatches();

            assertEquals(event, schedule.getEvent());
            assertEquals(event.getNumberOfMatches(), matches.size());

            List<AbstractScheduleValue> vals =
                    Stream.of(schedule.getScheduleValues()).flatMap(Arrays::stream).collect(Collectors.toList());

            assertEquals(event.getNumberOfOccupiedTimeslots(),
                    vals.stream().filter(AbstractScheduleValue::isOccupied).count()
            );

            assertTrue(vals.stream().filter(AbstractScheduleValue::isLimited).count() <=
                    event.getUnavailableLocalizations().values().stream().distinct().count() *
                            event.getPlayers().size());

            assertEquals(event.getBreaks().size() * event.getPlayers().size(),
                    vals.stream().filter(AbstractScheduleValue::isBreak).count()
            );

            assertTrue(vals.stream().filter(AbstractScheduleValue::isUnavailable).count() <=
                    event.getUnavailablePlayers().values().stream().count());
        }

        try {
            new EventSchedule(null, new int[][][]{});
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        }

        try {
            new EventSchedule(tournament.getEvents().get(0), null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        }
    }

    @Test(expected = NullPointerException.class)
    public void tournamentScheduleTest() throws ValidationException {
        assertNull(tournament.getCurrentSchedules());

        try {
            new TournamentSchedule(tournament);
            fail("IllegalStateException expected");
        } catch (IllegalStateException e) {
            assertEquals("Tournament schedules not calculated", e.getMessage());
        }

        tournament.solve();
        TournamentSchedule schedule = tournament.getSchedule();

        List<AbstractScheduleValue> vals =
                Stream.of(schedule.getScheduleValues()).flatMap(Arrays::stream).collect(Collectors.toList());

        assertEquals(tournament.getNumberOfOccupiedTimeslots(),
                vals.stream().filter(AbstractScheduleValue::isOccupied).count()
        );

        int nUnavailableLocalizationTimeslots = 0;
        int nBreakTimeslots = 0;
        for (Event event : tournament.getEvents()) {
            nUnavailableLocalizationTimeslots +=
                    event.getUnavailableLocalizations().values().stream().distinct().count() *
                            event.getPlayers().size();

            nBreakTimeslots += event.getBreaks().size() * event.getPlayers().size();
        }
        assertTrue(vals.stream().filter(AbstractScheduleValue::isLimited).count() <= nUnavailableLocalizationTimeslots);

        assertEquals(nBreakTimeslots, vals.stream().filter(AbstractScheduleValue::isBreak).count());

        assertTrue(vals.stream().filter(AbstractScheduleValue::isUnavailable).count() <= tournament.getEvents()
                .stream()
                .map(Event::getUnavailablePlayers)
                .map(Map::values)
                .flatMap(Collection::stream)
                .count());

        assertNotNull(schedule);
        assertEquals(tournament.getNumberOfMatches(), schedule.getMatches().size());

        new TournamentSchedule(null);
    }

    @Test
    public void filterMatchesByTest() throws ValidationException {
        tournament.solve();

        List<Event> events = tournament.getEvents();
        List<Player> sPlayers = tournament.getEvents().get(0).getPlayers();
        List<Player> dPlayers = tournament.getEvents().get(1).getPlayers();
        List<Timeslot> timeslots = tournament.getAllTimeslots();
        Map<Event, EventSchedule> schedules = tournament.getCurrentSchedules();
        TournamentSchedule schedule = tournament.getSchedule();

        List<Match> matches = schedules.get(events.get(0)).filterMatchesByPlayer(sPlayers.get(3));
        assertEquals(1, matches.size());

        matches = schedule.filterMatchesByPlayers(new ArrayList<>(Arrays.asList(sPlayers.get(4),
                sPlayers.get(6),
                dPlayers.get(1)
        )));
        assertEquals(0, matches.size());

        matches = schedule.filterMatchesByPlayers(new ArrayList<>(Arrays.asList(dPlayers.get(7), dPlayers.get(2))));
        assertEquals(1, matches.size());

        matches = schedule.filterMatchesByLocalization(tournament.getAllLocalizations().get(0));
        assertEquals(3, matches.size());

        matches = schedules.get(events.get(1)).filterMatchesByLocalization(tournament.getAllLocalizations().get(1));
        assertEquals(2, matches.size());

        matches = schedule.filterMatchesByStartTimeslot(timeslots.get(0));
        assertEquals(1, matches.size());

        matches = schedule.filterMatchesByStartTimeslot(timeslots.get(2));
        assertEquals(0, matches.size());

        matches = schedule.filterMatchesByEndTimeslot(timeslots.get(1));
        assertEquals(1, matches.size());

        matches = schedule.filterMatchesDuringTimeslot(timeslots.get(4));
        assertEquals(2, matches.size());

        matches = schedule.filterMatchesDuringTimeslot(timeslots.get(7));
        assertEquals(2, matches.size());

        matches = schedule.filterMatchesDuringTimeslots(new ArrayList<>(Arrays.asList(timeslots.get(4),
                timeslots.get(7)
        )));
        assertEquals(4, matches.size());

        matches = schedule.filterMatchesDuringTimeslotRange(timeslots.get(3), timeslots.get(4));
        assertEquals(2, matches.size());

        matches = schedule.filterMatchesInTimeslotRange(timeslots.get(1), timeslots.get(2));
        assertEquals(1, matches.size());

        assertEquals(0, schedule.filterMatchesByPlayer(new Player("New Player")).size());
        assertEquals(0, schedule.filterMatchesByLocalization(new Localization("New Localization")).size());
        assertEquals(0,
                schedule.filterMatchesByPlayers(new ArrayList<>(Arrays.asList(sPlayers.get(0), sPlayers.get(1)))).size()
        );
        assertEquals(0, schedule.filterMatchesByStartTimeslot(timeslots.get(2)).size());
        assertEquals(0, schedule.filterMatchesByEndTimeslot(timeslots.get(3)).size());
        assertEquals(0, schedule.filterMatchesDuringTimeslot(timeslots.get(5)).size());
        assertEquals(0, schedule.filterMatchesInTimeslotRange(timeslots.get(4), timeslots.get(5)).size());
        assertEquals(0, schedule.filterMatchesInTimeslotRange(timeslots.get(1), timeslots.get(3)).size());
        assertEquals(0, schedule.filterMatchesInTimeslotRange(timeslots.get(0), timeslots.get(2)).size());
    }

    @Test
    public void toStringTest() throws ValidationException {
        tournament.solve();

        Map<Event, EventSchedule> schedules = tournament.getCurrentSchedules();
        TournamentSchedule schedule = tournament.getSchedule();

        assertThat(schedule.toString(), StringContains.containsString("x"));
        assertThat(schedules.get(tournament.getEvents().get(0)).toString(), not(StringContains.containsString("x")));

        assertThat(schedules.get(tournament.getEvents().get(0)).toString(), StringContains.containsString("~"));
        assertThat(schedules.get(tournament.getEvents().get(1)).toString(), not(StringContains.containsString("~")));

        int i = 1;
        for (Player p : tournament.getEvents().get(0).getPlayers())
            p.setName("SinglePlayer" + (i++));

        assertThat(schedules.get(tournament.getEvents().get(0)).toString(), StringContains.containsString("SinglePl"));
        assertThat(schedules.get(tournament.getEvents().get(1)).toString(),
                not(StringContains.containsString("SinglePl"))
        );
    }

    @Test
    public void playerScheduleValueTest() {
        ScheduleValue v = new ScheduleValue(ScheduleValue.FREE);
        assertEquals(v.getValue(), ScheduleValue.FREE);
        assertTrue(v.isFree());
        assertFalse(v.isOccupied());

        assertNotNull(v);

        v = new ScheduleValueOccupied(2);
        assertEquals(ScheduleValue.OCCUPIED, v.getValue());
        assertEquals(2, ((ScheduleValueOccupied) v).getLocalization());
        assertTrue(v.isOccupied());
        assertFalse(v.isFree());
        assertFalse(v.isContinuation());
        assertEquals("2", v.toString());
        assertTrue(v.equals(new ScheduleValueOccupied(2)));
        assertFalse(v.equals(new ScheduleValueOccupied(3)));
        assertFalse(v.equals(new ScheduleValue(ScheduleValue.FREE)));
        assertNotNull(v);

        v = new ScheduleValue(ScheduleValue.UNAVAILABLE);
        assertTrue(v.isUnavailable());
        assertEquals("~", v.toString());
        assertTrue(v.equals(new ScheduleValue(ScheduleValue.UNAVAILABLE)));

        v = new ScheduleValue(ScheduleValue.BREAK);
        assertTrue(v.isBreak());
        assertEquals("*", v.toString());
        assertTrue(v.equals(new ScheduleValue(ScheduleValue.BREAK)));

        v = new ScheduleValue(ScheduleValue.LIMITED);
        assertTrue(v.isLimited());
        assertEquals("¬", v.toString());
        assertTrue(v.equals(new ScheduleValue(ScheduleValue.LIMITED)));

        v = new ScheduleValue(ScheduleValue.NOT_IN_DOMAIN);
        assertTrue(v.isNotInDomain());
        assertEquals("x", v.toString());
        assertTrue(v.equals(new ScheduleValue(ScheduleValue.NOT_IN_DOMAIN)));

        v = new ScheduleValue(ScheduleValue.FREE);
        assertTrue(v.isFree());
        assertEquals("-", v.toString());
        assertTrue(v.equals(new ScheduleValue(ScheduleValue.FREE)));

        try {
            new ScheduleValue(new Value("UNKNOWN"));
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), "Illegal value");
        }
    }

    @Test
    public void abstractScheduleValueTest() {
        AbstractScheduleValue v1 = new ScheduleValue(ScheduleValue.LIMITED);
        AbstractScheduleValue v2 = new ScheduleValue(ScheduleValue.UNAVAILABLE);
        AbstractScheduleValue v3 = new ScheduleValue(ScheduleValue.UNAVAILABLE);

        assertTrue(v1.isLimited());
        assertTrue(v2.isUnavailable());
        assertFalse(v1.isUnavailable());
        assertFalse(v2.isOccupied());
        assertFalse(v1.isFree());
        assertFalse(v2.isNotInDomain());
        assertFalse(v2.isContinuation());
        assertFalse(v1.isBreak());

        assertTrue(v2.equals(v3));
        assertTrue(v3.equals(v2));
        assertNotNull(v2);
        assertFalse(v1.equals(v2));
        assertFalse(v3.equals(v1));
    }

    @Test
    public void scheduleValueTest() {
        Value v = new Value("STATE_1");
        assertEquals("STATE_1", v.getName());
        assertTrue(v.is("STATE_1"));
        assertTrue(v.is("state_1"));
        assertFalse(v.is("STATE_2"));
        assertTrue(v.equals(new Value("State_1")));
        assertFalse(v.equals(new Value("STATE_2")));
        assertNotNull(v);
        assertFalse(v.equals(new Object()));
    }
}
