package es.uca.garciachacon.eventscheduler.data.model.schedule;

import es.uca.garciachacon.eventscheduler.data.model.schedule.value.AbstractScheduleValue;
import es.uca.garciachacon.eventscheduler.data.model.schedule.value.InverseScheduleValue;
import es.uca.garciachacon.eventscheduler.data.model.schedule.value.InverseScheduleValueOccupied;
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

import static org.junit.Assert.*;

/**
 * Tests de la clase {@link InverseSchedule}, así como de los valores internos que utiliza,
 * {@link InverseScheduleValue}.
 */
public class InverseScheduleTest {

    private Tournament tournament;
    private Event single;

    @Before
    public void setUp() throws ValidationException {
        List<Localization> localizations = TournamentUtils.buildGenericLocalizations(2, "Court");
        List<Timeslot> timeslots = TournamentUtils.buildSimpleTimeslots(10);

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
        singles.addMatchup(new HashSet<>(Arrays.asList(sPlayers.get(0), sPlayers.get(7))));

        List<Player> dPlayers = doubles.getPlayers();
        doubles.addTeam(dPlayers.get(0), dPlayers.get(5));
        doubles.addTeam(dPlayers.get(7), dPlayers.get(2));
        doubles.addTeam(dPlayers.get(4), dPlayers.get(1));
        doubles.addTeam(dPlayers.get(3), dPlayers.get(6));

        tournament = new Tournament("Tennis Tournament", singles, doubles);

        tournament.addBreak(timeslots.get(5));
        tournament.addUnavailableLocalizationAtTimeslot(localizations.get(0), timeslots.get(0));
        tournament.addUnavailablePlayerAtTimeslot(sPlayers.get(0), timeslots.get(7));
        doubles.addUnavailableLocalizationAtTimeslot(localizations.get(1), timeslots.get(4));

        tournament.getSolver().setSearchStrategy(TournamentSolver.SearchStrategy.MINDOM_UB);

        tournament.solve();

        single = tournament.getEvents().get(0);
    }

    @Test(expected = NullPointerException.class)
    public void constructorEventLocalizationScheduleTest() {
        InverseSchedule schedule = new InverseSchedule(single);

        List<AbstractScheduleValue> vals =
                Stream.of(schedule.getScheduleValues()).flatMap(Arrays::stream).collect(Collectors.toList());

        assertEquals(single.getNumberOfMatches(), vals.stream().filter(AbstractScheduleValue::isOccupied).count());

        long nUnavailableTimeslots = single.getLocalizations().size() * single.getBreaks().size() +
                single.getUnavailableLocalizations().values().stream().distinct().count();

        assertTrue(vals.stream().filter(AbstractScheduleValue::isLimited).count() <= nUnavailableTimeslots);
        assertTrue(vals.stream().filter(AbstractScheduleValue::isUnavailable).count() <= nUnavailableTimeslots);

        new InverseSchedule((Event) null);
    }

    @Test(expected = NullPointerException.class)
    public void constructorTournamentLocalizationScheduleTest() {
        InverseSchedule schedule = new InverseSchedule(tournament);

        List<AbstractScheduleValue> vals =
                Stream.of(schedule.getScheduleValues()).flatMap(Arrays::stream).collect(Collectors.toList());

        assertEquals(tournament.getNumberOfMatches(), vals.stream().filter(AbstractScheduleValue::isOccupied).count());

        long nUnavailableTimeslots = 0;
        for (Event event : tournament.getEvents())
            nUnavailableTimeslots += event.getUnavailableLocalizations().values().stream().distinct().count() +
                    event.getBreaks().size() * event.getLocalizations().size();

        assertTrue(vals.stream().filter(AbstractScheduleValue::isLimited).count() <= nUnavailableTimeslots);
        assertTrue(vals.stream().filter(AbstractScheduleValue::isUnavailable).count() <= nUnavailableTimeslots);

        new InverseSchedule((Tournament) null);
    }

    @Test
    public void constructorTournamentLocalizationScheduleSingleTournamentTest() throws ValidationException {
        Event event = new Event("Event",
                TournamentUtils.buildGenericPlayers(8, "Player"),
                TournamentUtils.buildGenericLocalizations(3, "Court"),
                TournamentUtils.buildSimpleTimeslots(6)
        );
        List<Localization> localizations = event.getLocalizations();
        List<Timeslot> timeslots = event.getTimeslots();
        event.addUnavailableLocalizationAtTimeslots(localizations.get(0),
                new HashSet<>(Arrays.asList(timeslots.get(0), timeslots.get(1), timeslots.get(2), timeslots.get(3)))
        );
        event.addUnavailableLocalizationAtTimeslots(localizations.get(1),
                new HashSet<>(Arrays.asList(timeslots.get(4), timeslots.get(5)))
        );
        event.addUnavailableLocalizationAtTimeslots(localizations.get(2),
                new HashSet<>(Arrays.asList(timeslots.get(2), timeslots.get(3)))
        );

        tournament = new Tournament("Tournament", event);

        try {
            new InverseSchedule(tournament);
            fail("IllegalStateException expected for not calculated schedules");
        } catch (IllegalStateException e) {
            assertEquals("Tournament schedule not calculated", e.getMessage());
        }

        tournament.solve();

        InverseSchedule schedule = new InverseSchedule(tournament);

        assertEquals(tournament.getEvents()
                        .get(0)
                        .getUnavailableLocalizations()
                        .values()
                        .stream()
                        .flatMap(Collection::stream)
                        .count(),
                Stream.of(schedule.getScheduleValues())
                        .flatMap(Arrays::stream)
                        .filter(AbstractScheduleValue::isUnavailable)
                        .count()
        );
    }

    @Test
    public void getTotalTimeslotsTest() {
        InverseSchedule schedule = new InverseSchedule(tournament);

        assertEquals(tournament.getAllLocalizations().size() * tournament.getAllTimeslots().size(),
                schedule.getTotalTimeslots()
        );
    }

    @Test
    public void getAvailableTimeslotsTest() {
        InverseSchedule schedule = new InverseSchedule(tournament);
        List<AbstractScheduleValue> vals =
                Stream.of(schedule.getScheduleValues()).flatMap(Arrays::stream).collect(Collectors.toList());

        assertEquals(vals.stream().filter(v -> v.isOccupied() || v.isFree() || v.isContinuation()).count(),
                schedule.getAvailableTimeslots()
        );
    }

    @Test
    public void getOccupationTest() {
        InverseSchedule schedule = new InverseSchedule(tournament);
        List<AbstractScheduleValue> vals =
                Stream.of(schedule.getScheduleValues()).flatMap(Arrays::stream).collect(Collectors.toList());

        assertEquals(vals.stream().filter(v -> v.isOccupied() || v.isContinuation()).count(), schedule.getOccupation());
    }

    @Test
    public void toStringTest() {
        InverseSchedule schedule = new InverseSchedule(tournament);
        String scheduleStr = schedule.toString();

        assertThat(scheduleStr, StringContains.containsString("0,7"));
        assertThat(scheduleStr, StringContains.containsString("¬"));
    }

    @Test
    public void localizationScheduleValueTest() {
        InverseScheduleValue v = new InverseScheduleValue(InverseScheduleValue.FREE);
        assertTrue(v.isFree());
        assertEquals("-", v.toString());

        v = new InverseScheduleValue(InverseScheduleValue.LIMITED);
        assertTrue(v.isLimited());
        assertEquals("¬", v.toString());

        v = new InverseScheduleValue(InverseScheduleValue.UNAVAILABLE);
        assertTrue(v.isUnavailable());
        assertEquals("*", v.toString());

        v = new InverseScheduleValue(InverseScheduleValue.CONTINUATION);
        assertTrue(v.isContinuation());
        assertEquals("<", v.toString());

        v = new InverseScheduleValueOccupied(new ArrayList<>(Arrays.asList(3, 4)));
        assertTrue(v.isOccupied());
        assertTrue(((InverseScheduleValueOccupied) v).getPlayers()
                .containsAll(new ArrayList<>(Arrays.asList(3, 4))));
        assertEquals("3,4", v.toString());

        v = new InverseScheduleValue(InverseScheduleValue.LIMITED);
        assertTrue(v.equals(new InverseScheduleValue(InverseScheduleValue.LIMITED)));
        assertFalse(v.equals(new InverseScheduleValue(InverseScheduleValue.CONTINUATION)));
        assertNotNull(v);

        v = new InverseScheduleValueOccupied(new ArrayList<>(Arrays.asList(0, 2, 5, 6)));
        assertTrue(v.equals(new InverseScheduleValueOccupied(new ArrayList<>(Arrays.asList(0, 2, 5, 6)))));
        assertFalse(v.equals(new InverseScheduleValueOccupied(new ArrayList<>(Arrays.asList(0, 2, 5)))));
        assertFalse(v.equals(new InverseScheduleValue(InverseScheduleValue.OCCUPIED)));
        assertNotNull(v);

        try {
            new InverseScheduleValue(new Value("UNKNOWN"));
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), "Illegal value");
        }

        try {
            new InverseScheduleValueOccupied(null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        }

        try {
            new InverseScheduleValueOccupied(new ArrayList<>(Arrays.asList(5, null, 6)));
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), "Player index cannot be null");
        }

        try {
            new InverseScheduleValueOccupied(new ArrayList<>(Arrays.asList(5, 5, 6)));
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), StringContains.containsString("Player indices cannot be duplicated"));
        }
    }
}
