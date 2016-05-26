package data.model.schedule;

import data.model.schedule.value.AbstractScheduleValue;
import data.model.schedule.value.LocalizationScheduleValue;
import data.model.schedule.value.LocalizationScheduleValueOccupied;
import data.model.schedule.value.ScheduleValue;
import data.model.tournament.Tournament;
import data.model.tournament.event.Event;
import data.model.tournament.event.domain.Localization;
import data.model.tournament.event.domain.Player;
import data.model.tournament.event.domain.timeslot.Timeslot;
import data.validation.validable.ValidationException;
import org.hamcrest.core.StringContains;
import org.junit.Before;
import org.junit.Test;
import solver.TournamentSolver.SearchStrategy;
import utils.TournamentUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;

/**
 * Tests de la clase {@link LocalizationSchedule}, así como de los valores internos que utiliza,
 * {@link LocalizationScheduleValue}.
 */
public class LocalizationScheduleTest {

    private Tournament tournament;
    private Event single;
    private List<Match> singleMatches;

    @Before
    public void setUp() throws ValidationException {
        List<Localization> localizations = TournamentUtils.buildGenericLocalizations(2, "Court");
        List<Timeslot> timeslots = TournamentUtils.buildAbstractTimeslots(10);

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

        tournament.getSolver().setSearchStrategy(SearchStrategy.MINDOM_UB);

        tournament.solve();

        single = tournament.getEvents().get(0);
        singleMatches = tournament.getCurrentSchedules().get(single).getMatches();
    }

    @Test
    public void constructorEventLocalizationScheduleTest() {
        LocalizationSchedule schedule = new LocalizationSchedule(single, singleMatches);

        List<AbstractScheduleValue> vals =
                Stream.of(schedule.getScheduleValues()).flatMap(Arrays::stream).collect(Collectors.toList());

        assertEquals(single.getNumberOfMatches(), vals.stream().filter(AbstractScheduleValue::isOccupied).count());

        long nUnavailableTimeslots = single.getLocalizations().size() * single.getBreaks().size() +
                single.getUnavailableLocalizations().values().stream().distinct().count();

        assertTrue(vals.stream().filter(AbstractScheduleValue::isLimited).count() <= nUnavailableTimeslots);
        assertTrue(vals.stream().filter(AbstractScheduleValue::isUnavailable).count() <= nUnavailableTimeslots);

        try {
            new LocalizationSchedule(null, singleMatches);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            assertEquals("The parameters cannot be null", e.getMessage());
        }

        try {
            new LocalizationSchedule(single, null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            assertEquals("The parameters cannot be null", e.getMessage());
        }

        try {
            singleMatches.remove(singleMatches.size() - 1);
            new LocalizationSchedule(single, singleMatches);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(),
                    StringContains.containsString("equal to the expected number of matches for the event")
            );
        }
    }

    @Test
    public void constructorTournamentLocalizationScheduleTest() {
        LocalizationSchedule schedule = new LocalizationSchedule(tournament);

        List<AbstractScheduleValue> vals =
                Stream.of(schedule.getScheduleValues()).flatMap(Arrays::stream).collect(Collectors.toList());

        assertEquals(tournament.getNumberOfMatches(), vals.stream().filter(AbstractScheduleValue::isOccupied).count());

        long nUnavailableTimeslots = 0;
        for (Event event : tournament.getEvents())
            nUnavailableTimeslots += event.getUnavailableLocalizations().values().stream().distinct().count() +
                    event.getBreaks().size() * event.getLocalizations().size();

        assertTrue(vals.stream().filter(AbstractScheduleValue::isLimited).count() <= nUnavailableTimeslots);
        assertTrue(vals.stream().filter(AbstractScheduleValue::isUnavailable).count() <= nUnavailableTimeslots);

        try {
            new LocalizationSchedule(null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            assertEquals("Tournament cannot be null", e.getMessage());
        }
    }

    @Test
    public void constructorTournamentLocalizationScheduleSingleTournamentTest() throws ValidationException {
        Event event = new Event("Event",
                TournamentUtils.buildGenericPlayers(8, "Player"),
                TournamentUtils.buildGenericLocalizations(3, "Court"),
                TournamentUtils.buildAbstractTimeslots(6)
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
            new LocalizationSchedule(tournament);
            fail("IllegalStateException expected for not calculated schedules");
        } catch (IllegalStateException e) {
            assertEquals("Tournament schedule not calculated", e.getMessage());
        }

        tournament.solve();

        LocalizationSchedule schedule = new LocalizationSchedule(tournament);

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
        LocalizationSchedule schedule = new LocalizationSchedule(tournament);

        assertEquals(tournament.getAllLocalizations().size() * tournament.getAllTimeslots().size(),
                schedule.getTotalTimeslots()
        );
    }

    @Test
    public void getAvailableTimeslotsTest() {
        LocalizationSchedule schedule = new LocalizationSchedule(tournament);
        List<AbstractScheduleValue> vals =
                Stream.of(schedule.getScheduleValues()).flatMap(Arrays::stream).collect(Collectors.toList());

        assertEquals(vals.stream().filter(v -> v.isOccupied() || v.isFree() || v.isContinuation()).count(),
                schedule.getAvailableTimeslots()
        );
    }

    @Test
    public void getOccupationTest() {
        LocalizationSchedule schedule = new LocalizationSchedule(tournament);
        List<AbstractScheduleValue> vals =
                Stream.of(schedule.getScheduleValues()).flatMap(Arrays::stream).collect(Collectors.toList());

        assertEquals(vals.stream().filter(v -> v.isOccupied() || v.isContinuation()).count(), schedule.getOccupation());
    }

    @Test
    public void toStringTest() {
        LocalizationSchedule schedule = new LocalizationSchedule(tournament);
        String scheduleStr = schedule.toString();

        assertThat(scheduleStr, StringContains.containsString("5,6"));
        assertThat(scheduleStr, StringContains.containsString("8,9,12,13"));
        assertThat(scheduleStr, StringContains.containsString("¬"));
    }

    @Test
    public void localizationScheduleValueTest() {
        LocalizationScheduleValue v = new LocalizationScheduleValue(LocalizationScheduleValue.FREE);
        assertTrue(v.isFree());
        assertEquals("-", v.toString());

        v = new LocalizationScheduleValue(LocalizationScheduleValue.LIMITED);
        assertTrue(v.isLimited());
        assertEquals("¬", v.toString());

        v = new LocalizationScheduleValue(LocalizationScheduleValue.UNAVAILABLE);
        assertTrue(v.isUnavailable());
        assertEquals("*", v.toString());

        v = new LocalizationScheduleValue(LocalizationScheduleValue.CONTINUATION);
        assertTrue(v.isContinuation());
        assertEquals("<", v.toString());

        v = new LocalizationScheduleValueOccupied(new ArrayList<>(Arrays.asList(3, 4)));
        assertTrue(v.isOccupied());
        assertTrue(((LocalizationScheduleValueOccupied) v).getPlayers()
                .containsAll(new ArrayList<>(Arrays.asList(3, 4))));
        assertEquals("3,4", v.toString());

        v = new LocalizationScheduleValue(LocalizationScheduleValue.LIMITED);
        assertTrue(v.equals(new LocalizationScheduleValue(LocalizationScheduleValue.LIMITED)));
        assertFalse(v.equals(new LocalizationScheduleValue(LocalizationScheduleValue.CONTINUATION)));
        assertNotNull(v);

        v = new LocalizationScheduleValueOccupied(new ArrayList<>(Arrays.asList(0, 2, 5, 6)));
        assertTrue(v.equals(new LocalizationScheduleValueOccupied(new ArrayList<>(Arrays.asList(0, 2, 5, 6)))));
        assertFalse(v.equals(new LocalizationScheduleValueOccupied(new ArrayList<>(Arrays.asList(0, 2, 5)))));
        assertFalse(v.equals(new LocalizationScheduleValue(LocalizationScheduleValue.OCCUPIED)));
        assertNotNull(v);

        try {
            new LocalizationScheduleValue(new ScheduleValue("UNKNOWN"));
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), "Illegal value");
        }

        try {
            new LocalizationScheduleValueOccupied(null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), "Players indices cannot be null");
        }

        try {
            new LocalizationScheduleValueOccupied(new ArrayList<>(Arrays.asList(5, null, 6)));
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), "Player index cannot be null");
        }

        try {
            new LocalizationScheduleValueOccupied(new ArrayList<>(Arrays.asList(5, 5, 6)));
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), StringContains.containsString("Player indices cannot be duplicated"));
        }
    }
}