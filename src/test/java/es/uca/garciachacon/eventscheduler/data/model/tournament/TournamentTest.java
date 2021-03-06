package es.uca.garciachacon.eventscheduler.data.model.tournament;

import es.uca.garciachacon.eventscheduler.data.model.schedule.EventSchedule;
import es.uca.garciachacon.eventscheduler.data.model.schedule.TournamentSchedule;
import es.uca.garciachacon.eventscheduler.data.validation.validable.ValidationException;
import es.uca.garciachacon.eventscheduler.data.validation.validator.TournamentValidator;
import es.uca.garciachacon.eventscheduler.solver.TournamentSolver.SearchStrategy;
import es.uca.garciachacon.eventscheduler.utils.TournamentUtils;
import org.hamcrest.core.StringContains;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;

/**
 * Tests de la clase {@link Tournament}.
 */
public class TournamentTest {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    private Tournament tournament;

    @Before
    public void setUp() {
        tournament = new Tournament("Tournament", new Event("Event",
                TournamentUtils.buildGenericPlayers(16, "Player"),
                TournamentUtils.buildGenericLocalizations(2, "Localization"),
                TournamentUtils.buildSimpleTimeslots(10)
        ));
    }

    @Test
    public void constructorTest() {
        List<Localization> localizations = TournamentUtils.buildGenericLocalizations(2, "Localization");
        List<Timeslot> timeslots = TournamentUtils.buildSimpleTimeslots(10);

        Event primaryEvent =
                new Event("Primary Event", TournamentUtils.buildGenericPlayers(16, "Player"), localizations, timeslots);

        Event secondaryEvent = new Event("Secondary Event",
                TournamentUtils.buildGenericPlayers(8, "Player"),
                localizations,
                timeslots
        );

        assertNull(primaryEvent.getTournament());
        assertNull(secondaryEvent.getTournament());

        Tournament tournament =
                new Tournament("Tournament", new ArrayList<>(Arrays.asList(primaryEvent, secondaryEvent)));

        assertEquals("Tournament", tournament.getName());
        assertEquals(2, tournament.getEvents().size());
        assertEquals(24, tournament.getAllPlayers().size());
        assertEquals(2, tournament.getAllLocalizations().size());
        assertEquals(10, tournament.getAllTimeslots().size());

        assertEquals(tournament, primaryEvent.getTournament());
        assertEquals(tournament, secondaryEvent.getTournament());

        for (Event event : tournament.getEvents())
            assertEquals(tournament, event.getTournament());

        localizations = TournamentUtils.buildGenericLocalizations(3, "Localization");
        timeslots = TournamentUtils.buildSimpleTimeslots(15);

        primaryEvent =
                new Event("Primary Event", TournamentUtils.buildGenericPlayers(32, "Player"), localizations, timeslots);

        secondaryEvent = new Event("Secondary Event",
                TournamentUtils.buildGenericPlayers(9, "Player"),
                localizations,
                timeslots,
                2,
                4,
                3
        );

        tournament = new Tournament("Tournament", primaryEvent, secondaryEvent);

        assertEquals("Tournament", tournament.getName());
        assertEquals(2, tournament.getEvents().size());
        assertEquals(41, tournament.getAllPlayers().size());
        assertEquals(3, tournament.getAllLocalizations().size());
        assertEquals(15, tournament.getAllTimeslots().size());

        List<Player> players = TournamentUtils.buildGenericPlayers(24, "Player");

        primaryEvent = new Event("Primary Event", players, localizations, timeslots);

        Collections.shuffle(players);
        secondaryEvent = new Event("Secondary Event",
                new Random().ints(0, players.size())
                        .distinct()
                        .limit(6)
                        .mapToObj(players::get)
                        .collect(Collectors.toList()),
                localizations,
                timeslots
        );

        tournament = new Tournament("Tournament", primaryEvent, secondaryEvent);

        assertEquals(24, tournament.getAllPlayers().size());
    }

    @Test(expected = NullPointerException.class)
    public void constructorNullNameTest() {
        Event event = new Event("Event",
                TournamentUtils.buildGenericPlayers(16, "Player"),
                TournamentUtils.buildGenericLocalizations(2, "Localization"),
                TournamentUtils.buildSimpleTimeslots(10)
        );

        new Tournament(null, event);
    }

    @Test(expected = NullPointerException.class)
    public void constructorNullEventsTest() {
        new Tournament("Tournament", (List<Event>) null);
    }

    @Test
    public void constructorEmtpyEventsTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("list of categories cannot be empty");
        new Tournament("Tournament", new ArrayList<>());
    }

    @Test
    public void constructorNullEventTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("category cannot be null");
        new Tournament("Tournament", Collections.singletonList(null));
    }

    @Test(expected = NullPointerException.class)
    public void setNameTest() {
        Event event = new Event("Event",
                TournamentUtils.buildGenericPlayers(16, "Player"),
                TournamentUtils.buildGenericLocalizations(2, "Localization"),
                TournamentUtils.buildSimpleTimeslots(10)
        );

        Tournament tournament = new Tournament("Tournament", event);

        tournament.setName("Modified Tournament");

        assertEquals("Modified Tournament", tournament.getName());

        tournament.setName(null);
    }

    @Test
    public void getEventsTest() {
        expectedEx.expect(UnsupportedOperationException.class);
        tournament.getEvents().clear();
    }

    @Test
    public void getAllPlayersTest() {
        expectedEx.expect(UnsupportedOperationException.class);
        tournament.getAllPlayers().add(new Player("New Player"));
    }

    @Test
    public void getAllLocalizationsTest() {
        expectedEx.expect(UnsupportedOperationException.class);
        tournament.getAllLocalizations().remove(0);
    }

    @Test
    public void getAllTimeslotsTest() {
        expectedEx.expect(UnsupportedOperationException.class);
        tournament.getAllTimeslots().set(3, new Timeslot(3));
    }

    @Test
    public void getNumberOfMatches() {
        List<Localization> localizations = TournamentUtils.buildGenericLocalizations(2, "Localization");
        List<Timeslot> timeslots = TournamentUtils.buildSimpleTimeslots(10);

        Event primaryEvent =
                new Event("Primary Event", TournamentUtils.buildGenericPlayers(16, "Player"), localizations, timeslots);

        Event secondaryEvent = new Event("Secondary Event",
                TournamentUtils.buildGenericPlayers(8, "Player"),
                localizations,
                timeslots
        );

        Tournament tournament =
                new Tournament("Tournament", new ArrayList<>(Arrays.asList(primaryEvent, secondaryEvent)));

        assertEquals(
                tournament.getEvents().get(0).getNumberOfMatches() + tournament.getEvents().get(1).getNumberOfMatches(),
                tournament.getNumberOfMatches()
        );
    }

    @Test
    public void validatorTest() {
        tournament.setValidator(new TournamentValidator());

        assertTrue(tournament.getMessages().isEmpty());

        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("parameter cannot be null");
        tournament.setValidator(null);
    }

    @Test
    public void solveTest() {
        tournament = new Tournament("Tournament with 2 solutions", new Event("Event",
                TournamentUtils.buildGenericPlayers(2, "Player"),
                TournamentUtils.buildGenericLocalizations(1, "Court"),
                TournamentUtils.buildDayOfWeekTimeslots(2),
                1,
                1,
                2
        ));

        try {
            assertNull(tournament.getSchedule());
            assertNull(tournament.getEventSchedules());

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            System.setOut(new PrintStream(out));

            tournament.printCurrentSchedules();

            assertThat(out.toString(), StringContains.containsString("Empty schedule"));

            tournament.getSolver().setSearchStrategy(SearchStrategy.MINDOM_UB);
            assertTrue(tournament.solve());

            TournamentSchedule tournamentSchedule = tournament.getSchedule();
            Map<Event, EventSchedule> schedules = new HashMap<>(tournament.getEventSchedules());

            assertNotNull(tournamentSchedule);
            assertNotNull(schedules);
            assertTrue(tournament.hasSchedule());

            out.reset();
            tournament.printCurrentSchedules();
            assertThat(out.toString(), StringContains.containsString("start=MONDAY"));

            assertTrue(tournament.nextSchedules());

            out.reset();
            tournament.printCurrentSchedules();
            assertThat(out.toString(), StringContains.containsString("start=TUESDAY"));

            out.reset();
            tournament.printCurrentSchedules(false);
            assertThat(out.toString(), not(StringContains.containsString("start=MONDAY")));
            assertThat(out.toString(), not(StringContains.containsString("start=MONDAY")));

            assertNotEquals(tournamentSchedule, tournament.getSchedule());
            for (Event e : tournament.getEventSchedules().keySet())
                assertNotEquals(schedules.get(e).getMatches().get(0).getStartTimeslot(),
                        tournament.getEventSchedules().get(e).getMatches().get(0).getStartTimeslot()
                );

            assertFalse(tournament.nextSchedules());

            assertNull(tournament.getSchedule());
            assertNull(tournament.getEventSchedules());
            assertFalse(tournament.hasSchedule());

            out.reset();
            tournament.printCurrentSchedules();
            assertThat(out.toString(), StringContains.containsString("Empty schedule"));
        } catch (ValidationException e) {
            fail("Unexpected exception thrown; tournament is valid");
        }
    }

    @Test
    public void groupEventsByNumberOfPlayersPerMatchTest() {
        List<Player> players = TournamentUtils.buildGenericPlayers(64, "Player");
        List<Localization> localizations = TournamentUtils.buildGenericLocalizations(10, "Court");
        List<Timeslot> timeslots = TournamentUtils.buildSimpleTimeslots(20);

        Tournament tournament = new Tournament("Tournament", new Event("E1",
                new Random().ints(0, players.size())
                        .distinct()
                        .limit(16)
                        .mapToObj(players::get)
                        .collect(Collectors.toList()),
                localizations,
                timeslots,
                1,
                2,
                2
        ), new Event("E2",
                new Random().ints(0, players.size())
                        .distinct()
                        .limit(16)
                        .mapToObj(players::get)
                        .collect(Collectors.toList()),
                localizations,
                timeslots,
                2,
                2,
                2
        ), new Event("E3",
                new Random().ints(0, players.size())
                        .distinct()
                        .limit(32)
                        .mapToObj(players::get)
                        .collect(Collectors.toList()),
                localizations,
                timeslots,
                1,
                2,
                4
        ), new Event("E3",
                new Random().ints(0, players.size())
                        .distinct()
                        .limit(36)
                        .mapToObj(players::get)
                        .collect(Collectors.toList()),
                localizations,
                timeslots,
                1,
                2,
                6
        ));

        Map<Integer, Set<Event>> eventsByPlayersPerMatch = tournament.groupEventsByNumberOfPlayersPerMatch();

        assertEquals(3, eventsByPlayersPerMatch.size());
        assertEquals(2, eventsByPlayersPerMatch.get(2).size());
        assertEquals(1, eventsByPlayersPerMatch.get(4).size());
        assertEquals(1, eventsByPlayersPerMatch.get(6).size());
    }

    @Test
    public void playerUnavailableTimeslotsTest() {
        List<Player> players = TournamentUtils.buildGenericPlayers(16, "Player");
        List<Localization> localizations = TournamentUtils.buildGenericLocalizations(1, "Court");
        List<Timeslot> timeslots = TournamentUtils.buildSimpleTimeslots(20);

        Event e1 = new Event("E1", players.subList(0, 4), localizations, timeslots.subList(0, 10));
        Event e2 = new Event("E2", players.subList(2, 10), localizations, timeslots.subList(5, 15));
        Event e3 = new Event("E3", players.subList(8, 16), localizations, timeslots.subList(11, 20));

        Tournament t = new Tournament("Tournament", e1, e2, e3);

        Player player2 = players.get(2);
        Timeslot timeslot7 = timeslots.get(7);
        t.addUnavailablePlayerAtTimeslot(player2, timeslot7);

        assertTrue(e1.hasUnavailablePlayers());
        assertTrue(e1.getUnavailablePlayers().get(player2).contains(timeslot7));
        assertTrue(e2.getUnavailablePlayers().get(player2).contains(timeslot7));
        assertNull(e3.getUnavailablePlayers().get(player2));

        Player player8 = players.get(8);
        Timeslot timeslot10 = timeslots.get(10);
        Timeslot timeslot12 = timeslots.get(12);
        t.addUnavailablePlayerAtTimeslots(player8, new HashSet<>(Arrays.asList(timeslot10, timeslot12)));

        assertEquals(1, e1.getUnavailablePlayers().size());
        assertEquals(2, e2.getUnavailablePlayers().size());
        assertEquals(1, e3.getUnavailablePlayers().size());

        assertEquals(2, e2.getUnavailablePlayers().get(player8).size());
        assertTrue(e2.getUnavailablePlayers().get(player8).contains(timeslot10));
        assertTrue(e3.getUnavailablePlayers().get(player8).contains(timeslot12));

        try {
            t.addUnavailablePlayerAtTimeslot(null, timeslots.get(1));
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        }

        try {
            t.addUnavailablePlayerAtTimeslot(players.get(3), null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        }

        try {
            t.addUnavailablePlayerAtTimeslots(null, new HashSet<>(Collections.singletonList(timeslots.get(1))));
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        }

        try {
            t.addUnavailablePlayerAtTimeslots(players.get(15), null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        }

        t.addUnavailablePlayerAtTimeslot(player2, timeslot7);
        assertEquals(1, e1.getUnavailablePlayers().get(player2).size());

        t.removeUnavailablePlayerAtTimeslot(player2, timeslots.get(3));
        assertEquals(1, e1.getUnavailablePlayers().get(player2).size());

        t.removeUnavailablePlayerAtTimeslot(players.get(1), timeslot7);
        assertEquals(1, e1.getUnavailablePlayers().get(player2).size());

        t.removeUnavailablePlayerAtTimeslot(player2, timeslot7);
        assertNull(e1.getUnavailablePlayers().get(player2));

        t.addUnavailablePlayerAtTimeslotRange(player2, timeslots.get(3), timeslots.get(6));
        assertEquals(4, e1.getUnavailablePlayers().get(player2).size());
        assertEquals(2, e2.getUnavailablePlayers().get(player2).size());
        assertNull(e3.getUnavailablePlayers().get(player2));

        Player player9 = players.get(9);
        t.addUnavailablePlayerAtTimeslotRange(player9, timeslots.get(16), timeslots.get(14));
        assertNull(e1.getUnavailablePlayers().get(player9));
        assertEquals(1, e2.getUnavailablePlayers().get(player9).size());
        assertEquals(3, e3.getUnavailablePlayers().get(player9).size());

        try {
            t.addUnavailablePlayerAtTimeslotRange(new Player("Unknown Player"), timeslots.get(9), timeslots.get(13));
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), StringContains.containsString("does not exist in the list of players"));
        }

        try {
            t.addUnavailablePlayerAtTimeslotRange(player9, new Timeslot(2), timeslots.get(13));
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), StringContains.containsString("does not exist in the list of timeslots"));
        }

        try {
            t.addUnavailablePlayerAtTimeslotRange(players.get(5), timeslots.get(9), new Timeslot(4));
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), StringContains.containsString("does not exist in the list of timeslots"));
        }
    }

    @Test
    public void breaksTest() {
        List<Player> players = TournamentUtils.buildGenericPlayers(16, "Player");
        List<Localization> localizations = TournamentUtils.buildGenericLocalizations(1, "Court");
        List<Timeslot> timeslots = TournamentUtils.buildSimpleTimeslots(20);

        Event e1 = new Event("E1", players, localizations, timeslots.subList(0, 10));
        Event e2 = new Event("E2", players, localizations, timeslots.subList(5, 15));
        Event e3 = new Event("E3", players, localizations, timeslots.subList(11, 20));

        Tournament t = new Tournament("Tournament", e1, e2, e3);

        assertFalse(t.getEvents().get(0).hasBreaks());
        assertFalse(t.getEvents().get(1).hasBreaks());
        assertFalse(t.getEvents().get(2).hasBreaks());

        t.addBreak(timeslots.get(6));

        assertTrue(e1.hasBreaks());
        assertTrue(e2.hasBreaks());
        assertFalse(e3.hasBreaks());

        assertTrue(e1.getBreaks().contains(timeslots.get(6)));
        assertTrue(e2.getBreaks().contains(timeslots.get(6)));

        t.addBreaks(new HashSet<>(Arrays.asList(timeslots.get(10), timeslots.get(11))));

        assertTrue(e3.hasBreaks());
        assertEquals(1, e1.getBreaks().size());
        assertEquals(3, e2.getBreaks().size());
        assertEquals(1, e3.getBreaks().size());

        t.removeBreak(timeslots.get(11));

        assertFalse(e3.hasBreaks());
        assertEquals(0, e3.getBreaks().size());
        assertEquals(2, e2.getBreaks().size());
        assertFalse(e2.getBreaks().contains(timeslots.get(11)));

        assertEquals(1, e1.getBreaks().size());
        t.addBreak(timeslots.get(6));
        assertEquals(1, e1.getBreaks().size());
        t.addBreaks(new HashSet<>(Collections.singletonList(timeslots.get(6))));
        assertEquals(1, e1.getBreaks().size());

        t.removeBreak(timeslots.get(5));
        assertEquals(1, e1.getBreaks().size());
        assertEquals(2, e2.getBreaks().size());
        assertEquals(0, e3.getBreaks().size());

        try {
            t.addBreaks(null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        }

        try {
            t.addBreak(null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        }
    }

    @Test
    public void unavailableLocalizationTest() {
        List<Player> players = TournamentUtils.buildGenericPlayers(16, "Player");
        List<Localization> localizations = TournamentUtils.buildGenericLocalizations(10, "Court");
        List<Timeslot> timeslots = TournamentUtils.buildSimpleTimeslots(20);

        Event e1 = new Event("E1", players, localizations.subList(0, 4), timeslots.subList(0, 10));
        Event e2 = new Event("E2", players, localizations.subList(2, 8), timeslots.subList(5, 15));
        Event e3 = new Event("E3", players, localizations.subList(6, 10), timeslots.subList(11, 20));

        Tournament t = new Tournament("Tournament", e1, e2, e3);

        assertFalse(t.getEvents().get(0).hasUnavailableLocalizations());
        assertFalse(t.getEvents().get(1).hasUnavailableLocalizations());
        assertFalse(t.getEvents().get(2).hasUnavailableLocalizations());

        t.addUnavailableLocalizationAtTimeslot(localizations.get(6), timeslots.get(13));

        assertFalse(e1.hasUnavailableLocalizations());
        assertTrue(e2.hasUnavailableLocalizations());
        assertTrue(e3.hasUnavailableLocalizations());

        assertEquals(1, e2.getUnavailableLocalizations().get(localizations.get(6)).size());
        t.addUnavailableLocalizationAtTimeslot(localizations.get(6), timeslots.get(13));
        assertEquals(1, e2.getUnavailableLocalizations().get(localizations.get(6)).size());

        t.addUnavailableLocalizationAtTimeslots(localizations.get(3),
                new HashSet<>(Arrays.asList(timeslots.get(8), timeslots.get(11)))
        );

        assertTrue(e1.hasUnavailableLocalizations());
        assertEquals(1, e1.getUnavailableLocalizations().size());
        assertEquals(2, e2.getUnavailableLocalizations().size());

        t.removeUnavailableLocalization(localizations.get(6));
        assertFalse(e3.hasUnavailableLocalizations());
        assertEquals(1, e2.getUnavailableLocalizations().size());

        t.removeUnavailableLocalizationAtTimeslot(localizations.get(3), timeslots.get(8));
        assertFalse(e1.hasUnavailableLocalizations());
        assertEquals(1, e2.getUnavailableLocalizations().size());

        t.removeUnavailableLocalizationAtTimeslot(localizations.get(0), timeslots.get(0));
        assertEquals(1, e2.getUnavailableLocalizations().size());
        t.removeUnavailableLocalizationAtTimeslot(localizations.get(0), timeslots.get(8));
        assertEquals(1, e2.getUnavailableLocalizations().size());
        t.removeUnavailableLocalizationAtTimeslot(localizations.get(3), timeslots.get(3));
        assertEquals(1, e2.getUnavailableLocalizations().size());

        t.addUnavailableLocalizationAtTimeslotRange(localizations.get(3), timeslots.get(2), timeslots.get(6));
        assertEquals(5, e1.getUnavailableLocalizations().get(localizations.get(3)).size());
        assertEquals(3, e2.getUnavailableLocalizations().get(localizations.get(3)).size());
        assertNull(e3.getUnavailableLocalizations().get(localizations.get(3)));

        t.addUnavailableLocalizationAtTimeslotRange(localizations.get(6), timeslots.get(13), timeslots.get(17));
        assertNull(e1.getUnavailableLocalizations().get(localizations.get(6)));
        assertEquals(2, e2.getUnavailableLocalizations().get(localizations.get(6)).size());
        assertEquals(5, e3.getUnavailableLocalizations().get(localizations.get(6)).size());

        try {
            t.addUnavailableLocalizationAtTimeslot(null, timeslots.get(0));
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        }

        try {
            t.addUnavailableLocalizationAtTimeslot(localizations.get(4), null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        }

        try {
            t.addUnavailableLocalizationAtTimeslots(null,
                    new HashSet<>(Arrays.asList(timeslots.get(8), timeslots.get(11)))
            );
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        }

        try {
            t.addUnavailableLocalizationAtTimeslots(localizations.get(4), null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        }

        try {
            t.addUnavailableLocalizationAtTimeslotRange(new Localization("Unknown Localization"),
                    timeslots.get(4),
                    timeslots.get(10)
            );
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), StringContains.containsString("does not exist in the list of localizations"));
        }

        try {
            t.addUnavailableLocalizationAtTimeslotRange(localizations.get(4),
                    new Timeslot(3),
                    timeslots.get(10)
            );
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), StringContains.containsString("does not exist in the list of timeslots"));
        }

        try {
            t.addUnavailableLocalizationAtTimeslotRange(localizations.get(4),
                    timeslots.get(10),
                    new Timeslot(3)
            );
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), StringContains.containsString("does not exist in the list of timeslots"));
        }
    }

    @Test
    public void toStringTest() {
        assertEquals("Tournament", tournament.toString());
    }

    @Test
    public void modifiedEventAndNextSchedules() throws ValidationException {
        List<Player> players = TournamentUtils.buildGenericPlayers(4, "Player");
        List<Localization> localizations = TournamentUtils.buildGenericLocalizations(1, "Court");
        List<Timeslot> timeslots = TournamentUtils.buildSimpleTimeslots(2);

        Event event = new Event("Event", players, localizations, timeslots);
        event.setTimeslotsPerMatch(1);
        event.addMatchup(players.get(0), players.get(1));

        tournament = new Tournament("Tournament", event);
        tournament.getSolver().setSearchStrategy(SearchStrategy.MINDOM_UB);

        tournament.solve();

        event.addUnavailablePlayerAtTimeslot(players.get(0), timeslots.get(1));

        try {
            tournament.nextSchedules();
            fail("IllegalStateException expected");
        } catch (IllegalStateException e) {
            assertEquals("An event has an inconsistent state", e.getMessage());
        }

        tournament.solve();
    }
}
