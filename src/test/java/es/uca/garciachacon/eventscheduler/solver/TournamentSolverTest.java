package es.uca.garciachacon.eventscheduler.solver;

import com.fasterxml.jackson.core.JsonProcessingException;
import es.uca.garciachacon.eventscheduler.data.model.schedule.EventSchedule;
import es.uca.garciachacon.eventscheduler.data.model.schedule.InverseSchedule;
import es.uca.garciachacon.eventscheduler.data.model.schedule.Match;
import es.uca.garciachacon.eventscheduler.data.model.schedule.TournamentSchedule;
import es.uca.garciachacon.eventscheduler.data.model.schedule.value.AbstractScheduleValue;
import es.uca.garciachacon.eventscheduler.data.model.tournament.*;
import es.uca.garciachacon.eventscheduler.data.validation.validable.ValidationException;
import es.uca.garciachacon.eventscheduler.solver.TournamentSolver.MatchupMode;
import es.uca.garciachacon.eventscheduler.solver.TournamentSolver.SearchStrategy;
import es.uca.garciachacon.eventscheduler.utils.TournamentUtils;
import org.hamcrest.core.StringContains;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;

/**
 * Tests de la clase {@link TournamentSolver} y la clase de estadísticas {@link ResolutionData}.
 */
public class TournamentSolverTest {

    private Tournament tournament;

    @Test
    public void constructorTest() {
        Event event = new Event("Event",
                TournamentUtils.buildGenericPlayers(8, "Player"),
                TournamentUtils.buildGenericLocalizations(3, "Court"),
                TournamentUtils.buildDayOfWeekTimeslots(14)
        );
        tournament = new Tournament("Tournament", event);

        TournamentSolver solver = new TournamentSolver(tournament);
        solver.setSearchStrategy(SearchStrategy.DOMOVERWDEG);
        solver.prioritizeTimeslots(true);
        solver.setResolutionTimeLimit(0);

        assertNull(solver.getInternalSolver());
        assertEquals("Tournament", solver.getTournament().getName());

        assertEquals(1, solver.getMatchesModel().length);
        assertEquals(8, solver.getMatchesModel()[0].length);
        assertEquals(3, solver.getMatchesModel()[0][0].length);
        assertEquals(14, solver.getMatchesModel()[0][0][0].length);

        assertEquals(solver.getMatchesBeginningsModel().length, solver.getMatchesModel().length);
        assertEquals(solver.getMatchesBeginningsModel()[0].length, solver.getMatchesModel()[0].length);
        assertEquals(solver.getMatchesBeginningsModel()[0][0].length, solver.getMatchesModel()[0][0].length);
        assertEquals(solver.getMatchesBeginningsModel()[0][0][0].length, solver.getMatchesModel()[0][0][0].length);

        assertEquals(SearchStrategy.DOMOVERWDEG, solver.getSearchStrategy());
        assertNull(solver.getResolutionData());
        assertTrue(solver.getPrioritizeTimeslots());

        assertEquals(0, solver.getResolutionTimeLimit());

        try {
            solver.setResolutionTimeLimit(-5);
            fail("IllegalArgumentException expected for invalid resolution time limit value");
        } catch (IllegalArgumentException e) {
            assertEquals("Resolution time limit cannot be less than zero", e.getMessage());
        }
    }

    @Test
    public void tournamentCaseTest() throws ValidationException {
        Event event = new Event("Event",
                TournamentUtils.buildGenericPlayers(8, "Player"),
                TournamentUtils.buildGenericLocalizations(1, "Court"),
                TournamentUtils.buildDayOfWeekTimeslots(8)
        );
        tournament = new Tournament("Tournament", event);

        TournamentSolver solver = tournament.getSolver();
        solver.setSearchStrategy(SearchStrategy.MINDOM_UB);

        assertFalse(solver.hasResolutionProcessStarted());
        assertFalse(solver.hasResolutionProcessFinished());
        assertFalse(solver.hasSolutions());
        assertEquals(0, solver.getFoundSolutions());

        assertTrue(tournament.solve());

        assertTrue(solver.hasResolutionProcessStarted());
        assertFalse(solver.hasResolutionProcessFinished());
        assertTrue(solver.hasSolutions());

        assertEquals(1, solver.getFoundSolutions());

        TournamentSchedule schedule = tournament.getSchedule();
        for (Player player : tournament.getAllPlayers())
            assertEquals(1, schedule.filterMatchesByPlayer(player).size());

        assertEquals(4, schedule.filterMatchesByLocalization(tournament.getAllLocalizations().get(0)).size());

        assertEquals(tournament.getNumberOfOccupiedTimeslots(),
                Stream.of(schedule.getScheduleValues())
                        .flatMap(Arrays::stream)
                        .filter(AbstractScheduleValue::isOccupied)
                        .count()
        );

        assertEquals(100, new Double(new InverseSchedule(tournament).getOccupationRatio() * 100).intValue());
    }

    @Test
    public void resolutionTimeLimitTest() throws ValidationException {
        Event event = new Event("Event",
                TournamentUtils.buildGenericPlayers(32, "Player"),
                TournamentUtils.buildGenericLocalizations(6, "Court"),
                TournamentUtils.buildSimpleTimeslots(10)
        );
        event.setMatchesPerPlayer(2);

        tournament = new Tournament("Tournament", event);
        TournamentSolver solver = tournament.getSolver();

        solver.setResolutionTimeLimit(1);

        assertFalse(tournament.solve());
        assertEquals(TournamentSolver.ResolutionState.INCOMPLETE, solver.getResolutionState());
    }

    @Test
    public void stopResolutionProcessTest() throws InterruptedException {
        Event event = new Event("Event",
                TournamentUtils.buildGenericPlayers(32, "Player"),
                TournamentUtils.buildGenericLocalizations(6, "Court"),
                TournamentUtils.buildSimpleTimeslots(10)
        );
        event.setMatchesPerPlayer(2);

        tournament = new Tournament("Tournament", event);

        TournamentSolver solver = tournament.getSolver();
        solver.setSearchStrategy(SearchStrategy.MINDOM_UB);

        Thread solveThread = new Thread(() -> {
            try {
                assertFalse(tournament.solve());
            } catch (ValidationException e) {
                fail("Unexpected ValidationException");
            }
        });
        solveThread.start();

        while (solver.getResolutionState() != TournamentSolver.ResolutionState.COMPUTING) {
            // por si el cómputo de la solución aún no ha empezado
        }

        Thread stopThread = new Thread(solver::stopResolutionProcess);
        stopThread.start();

        solveThread.join();
        stopThread.join();

        assertEquals(TournamentSolver.ResolutionState.INCOMPLETE, solver.getResolutionState());
    }

    @Test
    public void launchMultipleResolutionProcessesTest() throws InterruptedException, ValidationException {
        Event event = new Event("Event",
                TournamentUtils.buildGenericPlayers(64, "Player"),
                TournamentUtils.buildGenericLocalizations(1, "Court"),
                TournamentUtils.buildDayOfWeekTimeslots(14)
        );
        tournament = new Tournament("Tournament", event);
        TournamentSolver solver = tournament.getSolver();
        solver.setSearchStrategy(SearchStrategy.MINDOM_UB);

        Thread firstSolveThread = new Thread(() -> {
            try {
                tournament.solve();
            } catch (ValidationException e) {
                fail("Unexpected ValidationException");
            }
        });

        Thread secondSolveThread = new Thread(() -> {
            try {
                tournament.solve();
                while (solver.getResolutionState() == TournamentSolver.ResolutionState.COMPUTING) {
                    // espera hasta que deje de este corriendo el proceso de resolución del otro hilo
                }
                fail("Expected IllegalStateException");
            } catch (ValidationException e) {
                fail("Unexpected ValidationException");
            } catch (IllegalStateException e) {
                assertEquals("Solver is already computing the solution", e.getMessage());
            }
        });

        firstSolveThread.start();

        while (solver.getResolutionState() != TournamentSolver.ResolutionState.COMPUTING) {
            // por si el cómputo de la solución aún no ha empezado
        }

        secondSolveThread.start();

        Thread stopThread = new Thread(solver::stopResolutionProcess);

        stopThread.start();

        firstSolveThread.join();
        secondSolveThread.join();
        stopThread.join();
    }

    @Test
    public void tournamentWithOneSolutionCastTest() throws ValidationException {
        Event event = new Event("Event",
                TournamentUtils.buildGenericPlayers(2, "Player"),
                TournamentUtils.buildGenericLocalizations(1, "Court"),
                TournamentUtils.buildDayOfWeekTimeslots(2)
        );
        tournament = new Tournament("Tournament", event);

        TournamentSolver solver = tournament.getSolver();
        solver.setSearchStrategy(SearchStrategy.MINDOM_UB);

        assertEquals(TournamentSolver.ResolutionState.READY, solver.getResolutionState());

        assertTrue(tournament.solve());
        assertNotNull(tournament.getSchedule());
        assertEquals(1, solver.getFoundSolutions());
        assertEquals(TournamentSolver.ResolutionState.STARTED, solver.getResolutionState());

        assertFalse(tournament.nextSchedules());
        assertNull(tournament.getSchedule());
        assertEquals(1, solver.getFoundSolutions());
        assertTrue(solver.hasResolutionProcessFinished());
        assertEquals(TournamentSolver.ResolutionState.FINISHED, solver.getResolutionState());
    }

    @Test
    public void tournamentWithFewSolutionsCaseTest() throws ValidationException {
        Event event = new Event("Event",
                TournamentUtils.buildGenericPlayers(2, "Player"),
                TournamentUtils.buildGenericLocalizations(1, "Court"),
                TournamentUtils.buildDayOfWeekTimeslots(6)
        );
        tournament = new Tournament("Tournament", event);
        TournamentSolver solver = tournament.getSolver();
        solver.setSearchStrategy(SearchStrategy.MINDOM_UB);

        assertTrue(tournament.solve());
        assertNotNull(tournament.getSchedule());
        assertEquals(1, solver.getFoundSolutions());

        assertTrue(tournament.nextSchedules());
        assertNotNull(tournament.getSchedule());
        assertEquals(2, solver.getFoundSolutions());

        assertEquals(TournamentSolver.ResolutionState.STARTED, solver.getResolutionState());

        while (tournament.nextSchedules()) {
            // bloque vacío
        }

        assertFalse(tournament.nextSchedules());
        assertNull(tournament.getSchedule());
        assertEquals(5, solver.getFoundSolutions());
    }

    @Test
    public void tournamentInfeasibleCaseTest() throws ValidationException {
        Event event = new Event("Event",
                TournamentUtils.buildGenericPlayers(8, "Player"),
                TournamentUtils.buildGenericLocalizations(1, "Court"),
                TournamentUtils.buildDayOfWeekTimeslots(7)
        );
        tournament = new Tournament("Tournament", event);
        TournamentSolver solver = tournament.getSolver();
        solver.setSearchStrategy(SearchStrategy.MINDOM_UB);

        assertFalse(tournament.solve());
        assertNull(tournament.getSchedule());
        assertEquals(0, solver.getFoundSolutions());
        assertEquals(TournamentSolver.ResolutionState.UNFEASIBLE, solver.getResolutionState());
    }

    @Test
    public void tournamentPlayersPerMatchCaseTest() throws ValidationException {
        Event event = new Event("Event",
                TournamentUtils.buildGenericPlayers(24, "Player"),
                TournamentUtils.buildGenericLocalizations(2, "Court"),
                TournamentUtils.buildSimpleTimeslots(6),
                1,
                2,
                4
        );
        tournament = new Tournament("Tournament", event);
        tournament.getSolver().setSearchStrategy(SearchStrategy.MINDOM_UB);

        assertTrue(tournament.solve());

        List<Match> matches = tournament.getSchedule().getMatches();
        assertEquals(6, matches.size());

        for (Match match : matches)
            assertEquals(4, match.getPlayers().size());

        assertEquals(100, new Double(new InverseSchedule(tournament).getOccupationRatio() * 100).intValue());
    }

    @Test
    public void tournamentWithOnePlayerPerMatchCaseTest() throws ValidationException {
        Event event = new Event("Event",
                TournamentUtils.buildGenericPlayers(12, "Pl"),
                TournamentUtils.buildGenericLocalizations(12, "Court"),
                TournamentUtils.buildSimpleTimeslots(2),
                1,
                2,
                1
        );
        tournament = new Tournament("Tournament", event);
        tournament.getSolver().setSearchStrategy(SearchStrategy.MINDOM_UB);

        assertTrue(tournament.solve());

        List<Match> matches = tournament.getSchedule().getMatches();
        assertEquals(12, matches.size());

        for (Match match : matches)
            assertEquals(1, match.getPlayers().size());

        assertEquals(100, new Double(new InverseSchedule(tournament).getOccupationRatio() * 100).intValue());
    }

    @Test
    public void tournamentMatchesPerPlayerCaseTest() throws ValidationException {
        Event event = new Event("Event",
                TournamentUtils.buildGenericPlayers(8, "Player"),
                TournamentUtils.buildGenericLocalizations(2, "Court"),
                TournamentUtils.buildSimpleTimeslots(10),
                2,
                2,
                2
        );
        tournament = new Tournament("Tournament", event);

        assertTrue(tournament.solve());

        TournamentSchedule schedule = tournament.getSchedule();
        for (Player player : tournament.getAllPlayers())
            assertEquals(2, schedule.filterMatchesByPlayer(player).size());

        assertEquals(80, new Double(new InverseSchedule(tournament).getOccupationRatio() * 100).intValue());
    }

    @Test
    public void tournamentTimeslotsPerMatchCaseTest() throws ValidationException {
        Event event = new Event("Event",
                TournamentUtils.buildGenericPlayers(8, "Player"),
                TournamentUtils.buildGenericLocalizations(1, "Court"),
                TournamentUtils.buildSimpleTimeslots(12),
                1,
                3,
                2
        );
        tournament = new Tournament("Tournament", event);

        assertTrue(tournament.solve());

        for (Match match : tournament.getSchedule().getMatches())
            assertEquals(3, match.getDuration());
    }

    @Test
    public void tournamentWithBreaksCaseTest() throws ValidationException {
        Event event = new Event("Event",
                TournamentUtils.buildGenericPlayers(8, "Player"),
                TournamentUtils.buildGenericLocalizations(1, "Court"),
                TournamentUtils.buildSimpleTimeslots(10)
        );
        event.addBreak(event.getTimeslots().get(2));
        event.addBreak(event.getTimeslots().get(5));

        tournament = new Tournament("Tournament", event);

        assertTrue(tournament.solve());

        TournamentSchedule schedule = tournament.getSchedule();
        assertTrue(schedule.filterMatchesDuringTimeslot(tournament.getAllTimeslots().get(2)).isEmpty());
        assertTrue(schedule.filterMatchesDuringTimeslot(tournament.getAllTimeslots().get(5)).isEmpty());
    }

    @Test
    public void tournamentWithTeamsCaseTest() throws ValidationException {
        Event event = new Event("Event",
                TournamentUtils.buildGenericPlayers(18, "Player"),
                TournamentUtils.buildGenericLocalizations(1, "Court"),
                TournamentUtils.buildSimpleTimeslots(6)
        );
        event.setPlayersPerMatch(6);
        event.setPlayersPerTeam(3);

        tournament = new Tournament("Tournament", event);

        assertTrue(tournament.solve());

        List<Match> matches = tournament.getSchedule().getMatches();

        for (Match match : matches) {
            List<Team> matchTeams = match.getTeams();

            assertEquals(2, matchTeams.size());

            for (Team team : matchTeams)
                assertEquals(3, team.getPlayers().size());
        }
    }

    @Test
    public void tournamentWithPredefinedTeamsCaseTest() throws ValidationException {
        Event event = new Event("Event",
                TournamentUtils.buildGenericPlayers(8, "Player"),
                TournamentUtils.buildGenericLocalizations(1, "Court"),
                TournamentUtils.buildSimpleTimeslots(4),
                1,
                2,
                4
        );
        List<Player> players = event.getPlayers();
        event.addTeam(players.get(0), players.get(3));
        event.addTeam(players.get(6), players.get(7));
        event.addTeam(players.get(4), players.get(1));
        event.addTeam(players.get(2), players.get(5));

        tournament = new Tournament("Tournament", event);

        assertTrue(tournament.solve());

        TournamentSchedule schedule = tournament.getSchedule();
        for (Match match : schedule.getMatches())
            assertEquals(2, match.getTeams().size());

        assertEquals(1,
                schedule.filterMatchesByPlayers(new ArrayList<>(Arrays.asList(players.get(0), players.get(3)))).size()
        );
        assertEquals(1,
                schedule.filterMatchesByPlayers(new ArrayList<>(Arrays.asList(players.get(6), players.get(7)))).size()
        );
        assertEquals(1,
                schedule.filterMatchesByPlayers(new ArrayList<>(Arrays.asList(players.get(4), players.get(1)))).size()
        );
        assertEquals(1,
                schedule.filterMatchesByPlayers(new ArrayList<>(Arrays.asList(players.get(2), players.get(5)))).size()
        );
    }

    @Test
    public void tournamentWithUnavailablePlayersCaseTest() throws ValidationException {
        Event event = new Event("Event",
                TournamentUtils.buildGenericPlayers(8, "Player"),
                TournamentUtils.buildGenericLocalizations(1, "Court"),
                TournamentUtils.buildSimpleTimeslots(10)
        );
        List<Player> players = event.getPlayers();
        List<Timeslot> timeslots = event.getTimeslots();
        event.addUnavailablePlayerAtTimeslots(players.get(0),
                new HashSet<>(Arrays.asList(timeslots.get(0), timeslots.get(1), timeslots.get(2), timeslots.get(3)))
        );
        event.addUnavailablePlayerAtTimeslots(players.get(1),
                new HashSet<>(Arrays.asList(timeslots.get(5), timeslots.get(6)))
        );
        event.addUnavailablePlayerAtTimeslots(players.get(4),
                new HashSet<>(Arrays.asList(timeslots.get(4), timeslots.get(5), timeslots.get(6)))
        );
        event.addUnavailablePlayerAtTimeslots(players.get(5),
                new HashSet<>(Arrays.asList(timeslots.get(5), timeslots.get(6), timeslots.get(7), timeslots.get(8)))
        );

        tournament = new Tournament("Tournament", event);

        assertTrue(tournament.solve());

        Event e = tournament.getEvents().get(0);
        Map<Player, Set<Timeslot>> unavailablePlayers = e.getUnavailablePlayers();
        assertEquals(4, unavailablePlayers.get(players.get(0)).size());
        assertEquals(2, unavailablePlayers.get(players.get(1)).size());
        assertEquals(3, unavailablePlayers.get(players.get(4)).size());
        assertEquals(4, unavailablePlayers.get(players.get(5)).size());
        assertNull(unavailablePlayers.get(players.get(2)));
        assertNull(unavailablePlayers.get(players.get(3)));
        assertNull(unavailablePlayers.get(players.get(6)));
        assertNull(unavailablePlayers.get(players.get(7)));

        TournamentSchedule schedule = tournament.getSchedule();

        for (Match match : schedule.filterMatchesByPlayer(players.get(0))) {
            assertFalse(match.getStartTimeslot().within(timeslots.get(0), timeslots.get(3)));
            assertFalse(match.getEndTimeslot().within(timeslots.get(0), timeslots.get(3)));
        }

        for (Match match : schedule.filterMatchesByPlayer(players.get(1))) {
            assertFalse(match.getStartTimeslot().within(timeslots.get(5), timeslots.get(6)));
            assertFalse(match.getEndTimeslot().within(timeslots.get(5), timeslots.get(6)));
        }

        for (Match match : schedule.filterMatchesByPlayer(players.get(4))) {
            assertFalse(match.getStartTimeslot().within(timeslots.get(4), timeslots.get(6)));
            assertFalse(match.getEndTimeslot().within(timeslots.get(4), timeslots.get(6)));
        }

        for (Match match : schedule.filterMatchesByPlayer(players.get(5))) {
            assertFalse(match.getStartTimeslot().within(timeslots.get(5), timeslots.get(8)));
            assertFalse(match.getEndTimeslot().within(timeslots.get(5), timeslots.get(8)));
        }
    }

    @Test
    public void tournamentWithUnavailableLocalizationsCaseTest() throws ValidationException {
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

        assertTrue(tournament.solve());

        TournamentSchedule schedule = tournament.getSchedule();

        for (Match match : schedule.filterMatchesByLocalization(localizations.get(0)))
            assertFalse(match.during(timeslots.get(0), timeslots.get(3)));

        for (Match match : schedule.filterMatchesByLocalization(localizations.get(1)))
            assertFalse(match.during(timeslots.get(4), timeslots.get(5)));

        for (Match match : schedule.filterMatchesByLocalization(localizations.get(2)))
            assertFalse(match.during(timeslots.get(2), timeslots.get(3)));
    }

    @Test
    public void tournamentWithPredefinedMatchupsCaseTest() throws ValidationException {
        Player djokovic = new Player("Djokovic");
        Player robert = new Player("Robert");
        Player mahut = new Player("Mahut");
        Player belucci = new Player("Belucci");
        Player raonic = new Player("Raonic");
        Player kyrgios = new Player("Kyrgios");
        Player kohlschreiber = new Player("Kohlschreiber");
        Player nadal = new Player("Nadal");
        Player federer = new Player("Federer");
        Player zverev = new Player("Zverev");

        List<Player> players = new ArrayList<>(Arrays.asList(djokovic,
                robert,
                mahut,
                belucci,
                raonic,
                kyrgios,
                kohlschreiber,
                nadal,
                federer,
                zverev
        ));
        List<Localization> localizations = TournamentUtils.buildGenericLocalizations(3, "Court");
        List<Timeslot> timeslots = TournamentUtils.buildSimpleTimeslots(15);
        Event event = new Event("Event", players, localizations, timeslots, 1, 3, 2);

        event.addMatchup(new Matchup(event,
                new HashSet<>(Arrays.asList(djokovic, robert)),
                new HashSet<>(Arrays.asList(localizations.get(0))),
                new HashSet<>(timeslots.subList(0, 6)),
                1
        ));
        event.addMatchup(new Matchup(event,
                new HashSet<>(Arrays.asList(mahut, belucci)),
                new HashSet<>(Arrays.asList(localizations.get(1), localizations.get(2))),
                new HashSet<>(timeslots.subList(3, 10)),
                1
        ));
        event.addMatchup(new Matchup(event,
                new HashSet<>(Arrays.asList(raonic, kyrgios)),
                new HashSet<>(Arrays.asList(localizations.get(1))),
                new HashSet<>(timeslots.subList(12, 15)),
                1
        ));
        event.addMatchup(kohlschreiber, nadal);
        event.addMatchup(federer, zverev);

        tournament = new Tournament("Tournament", event);

        assertTrue(tournament.solve());

        TournamentSchedule schedule = tournament.getSchedule();

        Match match = schedule.filterMatchesByPlayer(djokovic).get(0);
        assertTrue(match.getPlayers().contains(robert));
        Assert.assertEquals(match.getLocalization(), localizations.get(0));
        assertTrue(match.within(timeslots.get(0), timeslots.get(7)));

        match = schedule.filterMatchesByPlayer(mahut).get(0);
        assertTrue(match.getPlayers().contains(belucci));
        assertTrue(match.getLocalization().equals(localizations.get(1)) ||
                match.getLocalization().equals(localizations.get(2)));
        assertTrue(match.within(timeslots.get(3), timeslots.get(11)));

        match = schedule.filterMatchesByPlayer(raonic).get(0);
        assertTrue(match.getPlayers().contains(kyrgios));
        Assert.assertEquals(match.getLocalization(), localizations.get(1));
        Assert.assertEquals(timeslots.get(12), match.getStartTimeslot());

        assertTrue(schedule.filterMatchesByPlayer(kohlschreiber).get(0).getPlayers().contains(nadal));
        assertTrue(schedule.filterMatchesByPlayer(federer).get(0).getPlayers().contains(zverev));

        InverseSchedule groupedSchedule = new InverseSchedule(tournament);
        assertEquals(tournament.getNumberOfOccupiedTimeslots(), 2 * groupedSchedule.getOccupation());
    }

    @Test
    public void tournamentWithPredefinedMatchupsAndMultipleMatchesPerPlayerCaseTest() throws ValidationException {
        Player federer = new Player("Federer");
        Player nadal = new Player("Nadal");
        Player djokovic = new Player("Djokovic");
        Player murray = new Player("Murray");
        Player ferrer = new Player("Ferrer");
        Player berdych = new Player("Berdych");

        Localization mainCourt = new Localization("Main Court");
        Localization court1 = new Localization("Court 1");
        Localization court2 = new Localization("Court 2");

        List<Timeslot> timeslots = TournamentUtils.buildSimpleTimeslots(20);

        Event event = new Event("Event",
                new ArrayList<>(Arrays.asList(federer, nadal, djokovic, murray, ferrer, berdych)),
                new ArrayList<>(Arrays.asList(mainCourt, court1, court2)),
                timeslots,
                3,
                3,
                2
        );
        event.setMatchupMode(MatchupMode.CUSTOM);

        event.addMatchup(new Matchup(event,
                new HashSet<>(Arrays.asList(djokovic, nadal)),
                new HashSet<>(Arrays.asList(mainCourt)),
                new HashSet<>(Arrays.asList(timeslots.get(0))),
                1
        ));

        event.addMatchup(new Matchup(event,
                new HashSet<>(Arrays.asList(federer, nadal)),
                new HashSet<>(Arrays.asList(mainCourt)),
                new HashSet<>(Arrays.asList(timeslots.get(3), timeslots.get(10))),
                2
        ));

        event.addMatchup(new Matchup(event,
                new HashSet<>(Arrays.asList(ferrer, berdych)),
                new HashSet<>(Arrays.asList(court1, court2)),
                new HashSet<>(Arrays.asList(timeslots.get(0), timeslots.get(3), timeslots.get(6))),
                2
        ));

        event.addMatchup(berdych, murray);

        tournament = new Tournament("Tournament", event);

        assertTrue(tournament.solve());

        TournamentSchedule schedule = tournament.getSchedule();

        List<Match> matches = schedule.filterMatchesByPlayer(nadal);
        assertEquals(3, matches.size());
        assertEquals(new HashSet<>(Arrays.asList(nadal, federer, djokovic)),
                matches.stream().map(Match::getPlayers).flatMap(Collection::stream).collect(Collectors.toSet())
        );

        matches = schedule.filterMatchesByPlayers(new ArrayList<>(Arrays.asList(nadal, djokovic)));
        assertEquals(1, matches.size());
        Match match = matches.get(0);
        assertEquals(new HashSet<>(Arrays.asList(nadal, djokovic)), new HashSet<>(match.getPlayers()));
        Assert.assertEquals(mainCourt, match.getLocalization());
        Assert.assertEquals(timeslots.get(0), match.getStartTimeslot());

        matches = schedule.filterMatchesByPlayers(new ArrayList<>(Arrays.asList(nadal, federer)));
        assertEquals(2, matches.size());
        match = matches.get(0);
        assertEquals(new HashSet<>(Arrays.asList(nadal, federer)), new HashSet<>(match.getPlayers()));
        Assert.assertEquals(mainCourt, match.getLocalization());
        Timeslot matchStart = null;
        if (match.getStartTimeslot() == timeslots.get(3))
            matchStart = timeslots.get(3);
        else if (match.getStartTimeslot() == timeslots.get(10))
            matchStart = timeslots.get(10);
        else
            fail("Match start timeslot should be 3 or 10");
        Assert.assertEquals(matchStart, match.getStartTimeslot());

        match = matches.get(1);
        assertEquals(new HashSet<>(Arrays.asList(nadal, federer)), new HashSet<>(match.getPlayers()));
        Assert.assertEquals(mainCourt, match.getLocalization());
        Assert.assertEquals(matchStart == timeslots.get(3) ? timeslots.get(10) : timeslots.get(3),
                match.getStartTimeslot()
        );

        matches = schedule.filterMatchesByPlayers(new ArrayList<>(Arrays.asList(ferrer, berdych)));
        assertEquals(2, matches.size());
        matches.forEach(m -> {
            assertEquals(new HashSet<>(Arrays.asList(ferrer, berdych)), new HashSet<>(m.getPlayers()));
            assertTrue(new HashSet<>(Arrays.asList(court1, court2)).contains(m.getLocalization()));
            assertTrue(new HashSet<>(Arrays.asList(timeslots.get(0),
                    timeslots.get(3),
                    timeslots.get(6)
            )).contains(m.getStartTimeslot()));
        });
    }

    @Test
    public void tournamentWithPredefinedMatchupsAndAssignedTimesotsInfeasibleTest() throws ValidationException {
        Event event = new Event("Event",
                TournamentUtils.buildGenericPlayers(8, "Player"),
                TournamentUtils.buildGenericLocalizations(3, "Court"),
                TournamentUtils.buildSimpleTimeslots(20),
                2,
                2,
                2
        );
        event.setMatchupMode(MatchupMode.CUSTOM);

        Player player = event.getPlayers().get(4);
        Timeslot timeslot = event.getTimeslots().get(3);
        event.addPlayerAtTimeslot(player, timeslot);
        event.addMatchup(new Matchup(event,
                new HashSet<>(Arrays.asList(player, event.getPlayers().get(2))),
                new HashSet<>(Arrays.asList(event.getLocalizations().get(1))),
                new HashSet<>(Arrays.asList(timeslot)),
                1
        ));

        tournament = new Tournament("Tournament", event);
        tournament.getSolver().setSearchStrategy(SearchStrategy.MINDOM_UB);

        assertFalse(tournament.solve());
    }

    @Test
    public void tournamentWithPredefinedMatchupsAndAssignedLocalizationInfeasibleTest() throws ValidationException {
        Event event = new Event("Event",
                TournamentUtils.buildGenericPlayers(4, "Player"),
                TournamentUtils.buildGenericLocalizations(2, "Court"),
                TournamentUtils.buildSimpleTimeslots(2)
        );
        event.addMatchup(new Matchup(event,
                new HashSet<>(Arrays.asList(event.getPlayers().get(0), event.getPlayers().get(1))),
                new HashSet<>(Arrays.asList(event.getLocalizations().get(0))),
                new HashSet<>(event.getTimeslots()),
                1
        ));
        event.addPlayerInLocalization(event.getPlayers().get(2), event.getLocalizations().get(0));

        tournament = new Tournament("Tournament", event);
        tournament.getSolver().setSearchStrategy(SearchStrategy.MINDOM_UB);

        assertFalse(tournament.solve());
    }

    @Test
    public void tournamentWithAssignedLocalizationsToPlayersCaseTest() throws ValidationException {
        Event event = new Event("Event",
                TournamentUtils.buildGenericPlayers(12, "Pl"),
                TournamentUtils.buildGenericLocalizations(5, "Court"),
                TournamentUtils.buildOneHourTimeslots(4)
        );
        List<Player> players = event.getPlayers();
        List<Localization> localizations = event.getLocalizations();
        event.addPlayerInLocalization(players.get(0), localizations.get(0));
        event.addPlayerInLocalization(players.get(0), localizations.get(1));
        event.addPlayerInLocalization(players.get(2), localizations.get(4));
        event.addPlayerInLocalization(players.get(6), localizations.get(3));
        event.addPlayerInLocalization(players.get(6), localizations.get(4));
        event.addPlayerInLocalization(players.get(7), localizations.get(4));
        event.addPlayerInLocalization(players.get(10), localizations.get(2));
        event.addPlayerInLocalization(players.get(11), localizations.get(2));

        tournament = new Tournament("Tournament", event);

        assertTrue(tournament.solve());

        TournamentSchedule schedule = tournament.getSchedule();

        Localization localization = schedule.filterMatchesByPlayer(players.get(0)).get(0).getLocalization();
        assertTrue(localization.equals(localizations.get(0)) || localization.equals(localizations.get(1)));

        localization = schedule.filterMatchesByPlayer(players.get(6)).get(0).getLocalization();
        assertTrue(localization.equals(localizations.get(3)) || localization.equals(localizations.get(4)));

        assertTrue(schedule.filterMatchesByLocalization(localizations.get(4))
                .stream()
                .map(Match::getPlayers)
                .flatMap(Collection::stream)
                .collect(Collectors.toList())
                .containsAll(new ArrayList<>(Arrays.asList(players.get(2), players.get(7)))));

        assertTrue(schedule.filterMatchesByLocalization(localizations.get(2))
                .stream()
                .map(Match::getPlayers)
                .flatMap(Collection::stream)
                .collect(Collectors.toList())
                .containsAll(new ArrayList<>(Arrays.asList(players.get(10), players.get(11)))));
    }

    @Test
    public void tournamentWithAssignedTimeslotsToPlayersCaseTest() throws ValidationException {
        Event event = new Event("Event",
                TournamentUtils.buildGenericPlayers(8, "Player"),
                TournamentUtils.buildGenericLocalizations(1, "Court"),
                TournamentUtils.buildOneHourTimeslots(10)
        );
        List<Player> players = event.getPlayers();
        List<Timeslot> timeslots = event.getTimeslots();
        event.addPlayerAtTimeslot(players.get(3), timeslots.get(0));
        event.addPlayerAtTimeslot(players.get(2), timeslots.get(0));
        event.addPlayerAtTimeslot(players.get(0), timeslots.get(8));
        event.addPlayerAtTimeslots(players.get(6),
                new HashSet<>(Arrays.asList(timeslots.get(2), timeslots.get(3), timeslots.get(4), timeslots.get(5)))
        );

        tournament = new Tournament("Tournament", event);

        assertTrue(tournament.solve());

        TournamentSchedule schedule = tournament.getSchedule();

        assertTrue(schedule.filterMatchesByPlayers(new ArrayList<>(Arrays.asList(players.get(2), players.get(3))))
                .get(0)
                .getStartTimeslot()
                .equals(timeslots.get(0)));
        assertTrue(schedule.filterMatchesByPlayer(players.get(0)).get(0).getStartTimeslot().equals(timeslots.get(8)));
        assertTrue(schedule.filterMatchesByPlayer(players.get(6)).get(0).within(timeslots.get(2), timeslots.get(7)));
    }

    @Test
    public void tournamentWithAllDifferentMatchupModeCaseTest() throws ValidationException {
        Event event = new Event("Event",
                TournamentUtils.buildGenericPlayers(8, "Player"),
                TournamentUtils.buildGenericLocalizations(1, "Court"),
                TournamentUtils.buildOneHourTimeslots(12),
                3,
                1,
                2
        );
        event.setMatchupMode(MatchupMode.ALL_DIFFERENT);

        tournament = new Tournament("Tournament", event);
        tournament.getSolver().setSearchStrategy(SearchStrategy.MINDOM_UB);

        assertTrue(tournament.solve());

        TournamentSchedule schedule = tournament.getSchedule();

        List<Player> players = tournament.getAllPlayers();
        for (Player player : players)
            assertEquals(3, schedule.filterMatchesByPlayer(player).size());

        List<Match> matches = schedule.getMatches();
        for (int i = 0; i < matches.size() - 1; i++)
            for (int j = i + 1; j < matches.size(); j++)
                assertFalse(matches.get(i).getPlayers().equals(matches.get(j).getPlayers()));
    }

    @Test
    public void tournamentWithAllEqualMatchupModeCaseTest() throws ValidationException {
        Event event = new Event("Event",
                TournamentUtils.buildGenericPlayers(8, "Player"),
                TournamentUtils.buildGenericLocalizations(1, "Court"),
                TournamentUtils.buildOneHourTimeslots(12),
                3,
                1,
                2
        );
        event.setMatchupMode(MatchupMode.ALL_EQUAL);

        tournament = new Tournament("Tournament", event);

        assertTrue(tournament.solve());

        TournamentSchedule schedule = tournament.getSchedule();

        List<Player> players = tournament.getAllPlayers();
        for (Player player : players)
            assertEquals(3, schedule.filterMatchesByPlayer(player).size());

        List<Match> matches = schedule.getMatches();
        for (int i = 0; i < matches.size(); i++) {
            int count = 0;
            for (int j = 0; j < matches.size(); j++)
                if (matches.get(i).getPlayers().equals(matches.get(j).getPlayers()))
                    count++;
            assertEquals(3, count);
        }
    }

    @Test
    public void tournamentWithAllEqualMatchupModeAndPredefinedMatchupInfeasibleCaseTest() throws ValidationException {
        List<Player> players = TournamentUtils.buildGenericPlayers(4, "Player");
        Event event = new Event("Event",
                players,
                TournamentUtils.buildGenericLocalizations(1, "Court"),
                TournamentUtils.buildOneHourTimeslots(4),
                2,
                1,
                2
        );
        event.setMatchupMode(MatchupMode.ALL_EQUAL);
        event.addMatchup(players.get(0), players.get(1));
        event.addMatchup(players.get(0), players.get(2));

        tournament = new Tournament("Tournament", event);

        assertFalse(tournament.solve());
    }

    @Test
    public void tournamentWithAnyMatchupModeCaseTest() throws ValidationException {
        Event event = new Event("Event",
                TournamentUtils.buildGenericPlayers(8, "Player"),
                TournamentUtils.buildGenericLocalizations(2, "Court"),
                TournamentUtils.buildOneHourTimeslots(12),
                3,
                1,
                2
        );
        event.setMatchupMode(MatchupMode.ANY);

        tournament = new Tournament("Tournament", event);

        assertTrue(tournament.solve());

        TournamentSchedule schedule = tournament.getSchedule();

        List<Player> players = tournament.getAllPlayers();
        for (Player player : players)
            assertEquals(3, schedule.filterMatchesByPlayer(player).size());

        List<Match> matches = schedule.getMatches();
        for (int i = 0; i < matches.size(); i++) {
            int count = 0;
            for (int j = 0; j < matches.size(); j++)
                if (matches.get(i).getPlayers().equals(matches.get(j).getPlayers()))
                    count++;
            assertTrue(count >= 1 && count <= 3);
        }
    }

    @Test
    public void tournamentWithAllDifferentMatchupModeAndTeamsCaseTest() throws ValidationException {
        List<Player> players = TournamentUtils.buildGenericPlayers(16, "Player");
        Event event = new Event("Event",
                players,
                TournamentUtils.buildGenericLocalizations(2, "Court"),
                TournamentUtils.buildSimpleTimeslots(8)
        );
        event.setMatchesPerPlayer(2);
        event.setTimeslotsPerMatch(2);
        event.setPlayersPerMatch(4);
        event.setMatchupMode(MatchupMode.ALL_DIFFERENT);

        event.addTeam(players.get(0), players.get(1));
        event.addTeam(players.get(2), players.get(3));

        tournament = new Tournament("Tournament", event);
        tournament.getSolver().setSearchStrategy(SearchStrategy.MINDOM_UB);

        assertTrue(tournament.solve());

        TournamentSchedule schedule = tournament.getSchedule();

        for (Player player : players)
            assertEquals(2, schedule.filterMatchesByPlayer(player).size());

        List<Match> matches = schedule.getMatches();
        for (int i = 0; i < matches.size() - 1; i++)
            for (int j = i + 1; j < matches.size(); j++)
                assertFalse(matches.get(i).getPlayers().equals(matches.get(j).getPlayers()));

        for (Match match : schedule.getMatches())
            assertEquals(2, match.getTeams().size());

        assertEquals(2,
                schedule.filterMatchesByPlayers(new ArrayList<>(Arrays.asList(players.get(0), players.get(1)))).size()
        );
        assertEquals(2,
                schedule.filterMatchesByPlayers(new ArrayList<>(Arrays.asList(players.get(2), players.get(3)))).size()
        );
    }

    @Test
    public void tournamentWithAllEqualMatchupModeAndTeamsCaseTest() throws ValidationException {
        List<Player> players = TournamentUtils.buildGenericPlayers(16, "Player");
        Event event = new Event("Event",
                players,
                TournamentUtils.buildGenericLocalizations(2, "Court"),
                TournamentUtils.buildSimpleTimeslots(8)
        );
        event.setMatchesPerPlayer(2);
        event.setTimeslotsPerMatch(2);
        event.setPlayersPerMatch(4);
        event.setMatchupMode(MatchupMode.ALL_EQUAL);

        event.addTeam(players.get(0), players.get(1));
        event.addTeam(players.get(2), players.get(3));
        event.addTeam(players.get(10), players.get(13));

        tournament = new Tournament("Tournament", event);
        tournament.getSolver().setSearchStrategy(SearchStrategy.MINDOM_UB);

        assertTrue(tournament.solve());

        TournamentSchedule schedule = tournament.getSchedule();

        for (Player player : players)
            assertEquals(2, schedule.filterMatchesByPlayer(player).size());

        List<Match> matches = schedule.getMatches();
        for (int i = 0; i < matches.size(); i++) {
            int count = 0;
            for (int j = 0; j < matches.size(); j++)
                if (matches.get(i).getPlayers().equals(matches.get(j).getPlayers()))
                    count++;
            assertEquals(2, count);
        }

        for (Match match : schedule.getMatches())
            assertEquals(2, match.getTeams().size());

        assertEquals(2,
                schedule.filterMatchesByPlayers(new ArrayList<>(Arrays.asList(players.get(0), players.get(1)))).size()
        );
        assertEquals(2,
                schedule.filterMatchesByPlayers(new ArrayList<>(Arrays.asList(players.get(2), players.get(3)))).size()
        );
        assertEquals(2,
                schedule.filterMatchesByPlayers(new ArrayList<>(Arrays.asList(players.get(10), players.get(13)))).size()
        );
    }

    @Test
    public void tournamentWithAnyMatchupModeAndTeamsCaseTest() throws ValidationException {
        List<Player> players = TournamentUtils.buildGenericPlayers(16, "Player");
        Event event = new Event("Event",
                players,
                TournamentUtils.buildGenericLocalizations(2, "Court"),
                TournamentUtils.buildSimpleTimeslots(8)
        );
        event.setMatchesPerPlayer(2);
        event.setTimeslotsPerMatch(2);
        event.setPlayersPerMatch(4);
        event.setMatchupMode(MatchupMode.ANY);

        event.addTeam(players.get(0), players.get(1));
        event.addTeam(players.get(2), players.get(3));
        event.addTeam(players.get(6), players.get(9));

        tournament = new Tournament("Tournament", event);
        tournament.getSolver().setSearchStrategy(SearchStrategy.MINDOM_UB);

        assertTrue(tournament.solve());

        TournamentSchedule schedule = tournament.getSchedule();

        for (Player player : players)
            assertEquals(2, schedule.filterMatchesByPlayer(player).size());

        List<Match> matches = schedule.getMatches();
        for (int i = 0; i < matches.size(); i++) {
            int count = 0;
            for (int j = 0; j < matches.size(); j++)
                if (matches.get(i).getPlayers().equals(matches.get(j).getPlayers()))
                    count++;
            assertTrue(count == 1 || count == 2);
        }

        for (Match match : schedule.getMatches())
            assertEquals(2, match.getTeams().size());

        assertEquals(2,
                schedule.filterMatchesByPlayers(new ArrayList<>(Arrays.asList(players.get(0), players.get(1)))).size()
        );
        assertEquals(2,
                schedule.filterMatchesByPlayers(new ArrayList<>(Arrays.asList(players.get(2), players.get(3)))).size()
        );
        assertEquals(2,
                schedule.filterMatchesByPlayers(new ArrayList<>(Arrays.asList(players.get(6), players.get(9)))).size()
        );
    }

    @Test
    public void tournamentWithCustomMatchupModeAndTeamsCaseTest() throws ValidationException {
        List<Player> players = TournamentUtils.buildGenericPlayers(16, "Pl");
        Event event = new Event("Event",
                players,
                TournamentUtils.buildGenericLocalizations(2, "Court"),
                TournamentUtils.buildSimpleTimeslots(8)
        );
        event.setMatchesPerPlayer(2);
        event.setTimeslotsPerMatch(2);
        event.setPlayersPerMatch(4);
        event.setMatchupMode(MatchupMode.CUSTOM);

        Team team1 = new Team(players.get(0), players.get(1));
        Team team2 = new Team(players.get(3), players.get(5));
        Team team3 = new Team(players.get(7), players.get(11));
        Team team4 = new Team(players.get(10), players.get(14));
        event.addTeam(team1);
        event.addTeam(team2);
        event.addTeam(team3);
        event.addTeam(team4);

        Set<Player> matchup1Players =
                Stream.concat(team1.getPlayers().stream(), team2.getPlayers().stream()).collect(Collectors.toSet());
        event.addMatchup(new Matchup(event,
                matchup1Players,
                new HashSet<>(event.getLocalizations()),
                new HashSet<>(event.getTimeslots()),
                1
        ));
        Set<Player> matchup2Players =
                Stream.concat(team3.getPlayers().stream(), team4.getPlayers().stream()).collect(Collectors.toSet());
        event.addMatchup(new Matchup(event,
                matchup2Players,
                new HashSet<>(event.getLocalizations()),
                new HashSet<>(event.getTimeslots()),
                2
        ));

        tournament = new Tournament("Tournament", event);
        tournament.getSolver().setSearchStrategy(SearchStrategy.MINDOM_UB);

        assertTrue(tournament.solve());

        TournamentSchedule schedule = tournament.getSchedule();

        for (Player player : players)
            assertEquals(2, schedule.filterMatchesByPlayer(player).size());

        for (Match match : schedule.getMatches())
            assertEquals(2, match.getTeams().size());

        assertEquals(1, schedule.filterMatchesByPlayers(new ArrayList<>(matchup1Players)).size());
        assertEquals(2, schedule.filterMatchesByPlayers(new ArrayList<>(matchup2Players)).size());
    }

    @Test
    public void tournamentWithPlayerUnavailableAtAssignedTimeslotInfeasibleCaseTest() throws ValidationException {
        tournament = new Tournament("Tournament", new Event("Event",
                TournamentUtils.buildGenericPlayers(8, "Player"),
                TournamentUtils.buildGenericLocalizations(1, "Court"),
                TournamentUtils.buildSimpleTimeslots(4),
                1,
                1,
                2
        ));
        Player player = tournament.getAllPlayers().get(0);
        Timeslot timeslot = tournament.getAllTimeslots().get(0);
        tournament.addUnavailablePlayerAtTimeslot(player, timeslot);
        tournament.getEvents().get(0).addPlayerAtTimeslot(player, timeslot);
        tournament.getSolver().setSearchStrategy(SearchStrategy.MINDOM_UB);

        assertFalse(tournament.solve());
        assertNull(tournament.getSchedule());
        assertNull(tournament.getEventSchedules());
        assertEquals(0, tournament.getSolver().getFoundSolutions());
    }

    @Test
    public void tournamentWithPlayerAssignedInUnavailableLocalizationInfeasibleCaseTest() throws ValidationException {
        tournament = new Tournament("Tournament", new Event("Event",
                TournamentUtils.buildGenericPlayers(8, "Player"),
                TournamentUtils.buildGenericLocalizations(2, "Court"),
                TournamentUtils.buildSimpleTimeslots(4),
                1,
                1,
                2
        ));
        Event event = tournament.getEvents().get(0);
        Player player = tournament.getAllPlayers().get(0);
        Timeslot timeslot = tournament.getAllTimeslots().get(3);
        Localization court = tournament.getAllLocalizations().get(1);

        event.addPlayerAtTimeslot(player, timeslot);
        event.addPlayerInLocalization(player, court);
        event.addUnavailableLocalizationAtTimeslot(court, timeslot);
        tournament.getSolver().setSearchStrategy(SearchStrategy.MINDOM_UB);

        tournament.solve();

        assertFalse(tournament.solve());
        assertNull(tournament.getSchedule());
        assertNull(tournament.getEventSchedules());
        assertEquals(0, tournament.getSolver().getFoundSolutions());
    }

    @Test
    public void tournamentWhereAssignedTimeslotIsBreakInfeasibleCaseTest() throws ValidationException {
        tournament = new Tournament("Tournament", new Event("Event",
                TournamentUtils.buildGenericPlayers(8, "Player"),
                TournamentUtils.buildGenericLocalizations(1, "Court"),
                TournamentUtils.buildSimpleTimeslots(5),
                1,
                1,
                2
        ));
        Event event = tournament.getEvents().get(0);
        Timeslot timeslot = tournament.getAllTimeslots().get(2);

        event.addPlayerAtTimeslot(tournament.getAllPlayers().get(6), timeslot);
        event.addBreak(timeslot);
        tournament.getSolver().setSearchStrategy(SearchStrategy.MINDOM_UB);

        assertFalse(tournament.solve());
        assertNull(tournament.getSchedule());
        assertNull(tournament.getEventSchedules());
        assertEquals(0, tournament.getSolver().getFoundSolutions());
    }

    @Test
    public void tournamentWithPlayerUnavailableAndPredefinedMatchupInfeasibleCaseTest() throws ValidationException {
        tournament = new Tournament("Tournament", new Event("Event",
                TournamentUtils.buildGenericPlayers(8, "Player"),
                TournamentUtils.buildGenericLocalizations(1, "Court"),
                TournamentUtils.buildSimpleTimeslots(4),
                1,
                1,
                2
        ));
        Event event = tournament.getEvents().get(0);
        List<Player> players = tournament.getAllPlayers();
        List<Timeslot> timeslots = tournament.getAllTimeslots();
        Player player1 = players.get(5);
        Player player2 = players.get(6);

        event.addMatchup(player1, player2);
        event.addUnavailablePlayerAtTimeslots(player1,
                new HashSet<>(Arrays.asList(timeslots.get(0), timeslots.get(1)))
        );
        event.addUnavailablePlayerAtTimeslots(player2,
                new HashSet<>(Arrays.asList(timeslots.get(2), timeslots.get(3)))
        );
        tournament.getSolver().setSearchStrategy(SearchStrategy.MINDOM_UB);

        assertFalse(tournament.solve());
        assertNull(tournament.getSchedule());
        assertNull(tournament.getEventSchedules());
        assertEquals(0, tournament.getSolver().getFoundSolutions());
    }

    @Test
    public void tournamentWithLocalizationAndPlayerUnavailableAndPredefinedMatchupInfeasibleCaseTest()
            throws ValidationException {
        tournament = new Tournament("Tournament", new Event("Event",
                TournamentUtils.buildGenericPlayers(8, "Player"),
                TournamentUtils.buildGenericLocalizations(2, "Court"),
                TournamentUtils.buildSimpleTimeslots(4),
                1,
                1,
                2
        ));
        Event event = tournament.getEvents().get(0);
        List<Player> players = tournament.getAllPlayers();
        List<Timeslot> timeslots = tournament.getAllTimeslots();
        Player player1 = players.get(5);
        Player player2 = players.get(6);
        Localization court = tournament.getAllLocalizations().get(1);

        event.addPlayerInLocalization(player1, court);
        event.addMatchup(player1, player2);
        event.addUnavailablePlayerAtTimeslots(player1,
                new HashSet<>(Arrays.asList(timeslots.get(0), timeslots.get(1)))
        );
        event.addUnavailablePlayerAtTimeslots(player2, new HashSet<>(Arrays.asList(timeslots.get(2))));
        event.addUnavailableLocalizationAtTimeslot(court, timeslots.get(3));
        tournament.getSolver().setSearchStrategy(SearchStrategy.MINDOM_UB);

        assertFalse(tournament.solve());
        assertNull(tournament.getSchedule());
        assertNull(tournament.getEventSchedules());
        assertEquals(0, tournament.getSolver().getFoundSolutions());
    }

    @Test
    public void tournamentFillTimeslotsFirstCaseTest() throws ValidationException {
        tournament = new Tournament("Tournament", new Event("Event",
                TournamentUtils.buildGenericPlayers(8, "Player"),
                TournamentUtils.buildGenericLocalizations(2, "Court"),
                TournamentUtils.buildSimpleTimeslots(4),
                1,
                1,
                2
        ));
        tournament.getSolver().setSearchStrategy(SearchStrategy.MINDOM_UB);
        tournament.getSolver().prioritizeTimeslots(true);

        assertTrue(tournament.solve());

        List<Timeslot> timeslots = tournament.getAllTimeslots();
        List<Localization> courts = tournament.getAllLocalizations();

        Localization court1 = courts.get(0);
        TournamentSchedule schedule = tournament.getSchedule();
        for (Timeslot timeslot : timeslots) {
            List<Match> matches = schedule.filterMatchesDuringTimeslot(timeslot);
            assertEquals(1, matches.size());
            Assert.assertEquals(court1, matches.get(0).getLocalization());
        }

        tournament.getSolver().prioritizeTimeslots(false);

        assertTrue(tournament.solve());

        schedule = tournament.getSchedule();
        assertEquals(2, schedule.filterMatchesDuringTimeslot(timeslots.get(0)).size());
        assertEquals(2, schedule.filterMatchesDuringTimeslot(timeslots.get(1)).size());
        assertEquals(0, schedule.filterMatchesDuringTimeslot(timeslots.get(2)).size());
        assertEquals(0, schedule.filterMatchesDuringTimeslot(timeslots.get(3)).size());
        assertEquals(2, schedule.filterMatchesByLocalization(court1).size());
        assertEquals(2, schedule.filterMatchesByLocalization(courts.get(1)).size());
    }

    @Test
    public void multiTournamentCaseTest() throws ValidationException {
        List<Localization> courts = TournamentUtils.buildGenericLocalizations(3, "Court");
        List<Timeslot> timeslots = TournamentUtils.buildSimpleTimeslots(8);

        Event cat1 = new Event("Category 1", TournamentUtils.buildGenericPlayers(8, "PCat1"), courts, timeslots);
        Event cat2 = new Event("Category 2", TournamentUtils.buildGenericPlayers(8, "PCat2"), courts, timeslots);
        Event cat3 = new Event("Category 3", TournamentUtils.buildGenericPlayers(8, "PCat3"), courts, timeslots);

        Tournament tournament = new Tournament("Tournament", cat1, cat2, cat3);
        tournament.getSolver().setSearchStrategy(SearchStrategy.MINDOM_UB);

        assertEquals(0, tournament.getSolver().getFoundSolutions());

        assertTrue(tournament.solve());

        assertEquals(1, tournament.getSolver().getFoundSolutions());

        Map<Event, EventSchedule> schedules = tournament.getEventSchedules();
        assertEquals(4, schedules.get(cat1).getMatches().size());
        assertEquals(4, schedules.get(cat2).getMatches().size());
        assertEquals(4, schedules.get(cat3).getMatches().size());

        assertEquals(4, schedules.get(cat1).filterMatchesByLocalization(courts.get(0)).size());
        assertTrue(schedules.get(cat3).filterMatchesByLocalization(courts.get(0)).isEmpty());

        TournamentSchedule schedule = tournament.getSchedule();
        for (int i = 0; i < timeslots.size(); i += 2) {
            List<Match> matches = schedule.filterMatchesByStartTimeslot(timeslots.get(i));
            assertEquals(3, matches.size());
            for (int j = 0; j < matches.size() - 1; j++)
                for (int k = j + 1; k < matches.size(); k++)
                    Assert.assertNotEquals(matches.get(j).getLocalization(), matches.get(k).getLocalization());
        }

        assertEquals(100, new Double(new InverseSchedule(tournament).getOccupationRatio() * 100).intValue());
    }

    @Test
    public void multiTournamentWithFewSolutionsCaseTest() throws ValidationException {
        List<Localization> courts = TournamentUtils.buildGenericLocalizations(1, "Court");
        List<Localization> extraCourts = new ArrayList<>();
        extraCourts.add(courts.get(0));
        extraCourts.add(new Localization("Court 2"));
        List<Timeslot> timeslots = TournamentUtils.buildLocalTimeTimeslots(4);

        Event cat1 = new Event("Category 1", TournamentUtils.buildGenericPlayers(2, "PCat1"), courts, timeslots);
        Event cat2 = new Event("Category 2", TournamentUtils.buildGenericPlayers(2, "PCat2"), extraCourts, timeslots);

        Tournament tournament = new Tournament("Tournament", cat1, cat2);
        tournament.getSolver().setSearchStrategy(SearchStrategy.MINDOM_UB);

        assertTrue(tournament.solve());
        assertEquals(1, tournament.getSolver().getFoundSolutions());

        while (tournament.nextSchedules()) {
            // bloque vacío
        }
        assertEquals(11, tournament.getSolver().getFoundSolutions());

        assertFalse(tournament.nextSchedules());
        assertNull(tournament.getEventSchedules());
        assertNull(tournament.getSchedule());
    }

    @Test
    public void multiTournamentWithoutSharedDomainsCaseTest() throws ValidationException {
        List<Timeslot> timeslots = TournamentUtils.buildDayOfWeekTimeslots(14);
        Event cat1 = new Event("Category 2",
                TournamentUtils.buildGenericPlayers(8, "PCat1"),
                TournamentUtils.buildGenericLocalizations(2, "LCat1"),
                timeslots.subList(0, 7)
        );
        Event cat2 = new Event("Category 2",
                TournamentUtils.buildGenericPlayers(8, "PCat2"),
                TournamentUtils.buildGenericLocalizations(2, "LCat2"),
                timeslots.subList(7, 14)
        );

        tournament = new Tournament("Tournament", cat1, cat2);
        tournament.getSolver().setSearchStrategy(SearchStrategy.MINDOM_UB);

        assertTrue(tournament.solve());

        Map<Event, EventSchedule> schedules = tournament.getEventSchedules();
        for (Localization court : cat1.getLocalizations())
            assertTrue(schedules.get(cat2).filterMatchesByLocalization(court).isEmpty());

        for (Localization court : cat2.getLocalizations())
            assertTrue(schedules.get(cat1).filterMatchesByLocalization(court).isEmpty());

        for (Timeslot timeslot : cat1.getTimeslots())
            assertTrue(schedules.get(cat2).filterMatchesDuringTimeslot(timeslot).isEmpty());

        for (Timeslot timeslot : cat2.getTimeslots())
            assertTrue(schedules.get(cat1).filterMatchesDuringTimeslot(timeslot).isEmpty());
    }

    @Test
    public void multiTournamentWithSharedPlayersCaseTest() throws ValidationException {
        List<Player> players = TournamentUtils.buildGenericPlayers(8, "Player");
        List<Localization> courts = TournamentUtils.buildGenericLocalizations(2, "Court");
        List<Timeslot> timeslots = TournamentUtils.buildDayOfWeekTimeslots(10);

        Event cat1 = new Event("Category 1", players, courts, timeslots, 1, 1, 2);
        Event cat2 = new Event("Category 2", players, courts, timeslots, 1, 1, 2);
        Event cat3 = new Event("Category 3", players, courts, timeslots, 1, 2, 2);

        Tournament tournament = new Tournament("Tournament", cat1, cat2, cat3);
        tournament.getSolver().setSearchStrategy(SearchStrategy.MINDOM_UB);

        assertTrue(tournament.solve());

        List<Event> events = tournament.getEvents();
        Map<Event, EventSchedule> schedules = tournament.getEventSchedules();
        for (Player player : players) {
            for (Event event : events) {
                List<Match> playerMatches = schedules.get(event).filterMatchesByPlayer(player);
                Match playerMatch = playerMatches.get(0);

                assertEquals(1, playerMatches.size());

                events.stream().filter(otherEvent -> otherEvent != event).forEach(otherEvent -> {
                    List<Match> playerOtherMatches = schedules.get(otherEvent).filterMatchesByPlayer(player);
                    assertEquals(1, playerOtherMatches.size());

                    assertFalse(playerOtherMatches.get(0)
                            .during(playerMatch.getStartTimeslot(), playerMatch.getEndTimeslot()));
                });
            }
        }
    }

    @Test
    public void multiTournamentWithPartiallySharedPlayersCaseTest() throws ValidationException {
        List<Player> players = TournamentUtils.buildGenericPlayers(8, "Pl");
        List<Localization> courts = TournamentUtils.buildGenericLocalizations(1, "Court");
        List<Timeslot> timeslots = TournamentUtils.buildDayOfWeekTimeslots(6);

        Event cat1 = new Event("Category 1", players.subList(0, 6), courts, timeslots, 1, 1, 2);
        Event cat2 = new Event("Category 2", players.subList(2, 8), courts, timeslots, 1, 1, 2);

        Tournament tournament = new Tournament("Tournament", cat1, cat2);
        tournament.getSolver().setSearchStrategy(SearchStrategy.MINDOM_UB);

        assertTrue(tournament.solve());

        TournamentSchedule schedule = tournament.getSchedule();

        for (int i : new int[]{ 0, 1, 6, 7 })
            assertEquals(1, schedule.filterMatchesByPlayer(players.get(i)).size());

        for (int i = 2; i < 6; i++)
            assertEquals(2, schedule.filterMatchesByPlayer(players.get(i)).size());
    }

    @Test
    public void multiTournamentWithPartiallySharedLocalizationsCasteTest() throws ValidationException {
        List<Player> players = TournamentUtils.buildGenericPlayers(8, "Player");
        List<Localization> courts = TournamentUtils.buildGenericLocalizations(3, "Court");
        List<Timeslot> timeslots = TournamentUtils.buildDayOfWeekTimeslots(3);

        Event cat1 = new Event("Category 1", players, courts.subList(0, 2), timeslots, 1, 1, 2);
        Event cat2 = new Event("Category 2", players, courts.subList(1, 3), timeslots, 1, 1, 2);

        Tournament tournament = new Tournament("Tournament", cat1, cat2);
        tournament.getSolver().setSearchStrategy(SearchStrategy.MINDOM_UB);

        assertTrue(tournament.solve());

        Map<Event, EventSchedule> schedules = tournament.getEventSchedules();

        assertTrue(schedules.get(cat1).filterMatchesByLocalization(courts.get(2)).isEmpty());
        assertTrue(schedules.get(cat2).filterMatchesByLocalization(courts.get(0)).isEmpty());
        assertFalse(schedules.get(cat1).filterMatchesByLocalization(courts.get(1)).isEmpty());
        assertFalse(schedules.get(cat2).filterMatchesByLocalization(courts.get(1)).isEmpty());
    }

    @Test
    public void multiTournamentWithPartiallySharedTimeslotsCaseTest() throws ValidationException {
        List<Player> players = TournamentUtils.buildGenericPlayers(8, "Player");
        List<Localization> courts = TournamentUtils.buildGenericLocalizations(3, "Court");
        List<Timeslot> timeslots = TournamentUtils.buildDayOfWeekTimeslots(7);

        Event cat1 = new Event("Category 1", players, courts, timeslots.subList(0, 4), 1, 1, 2);
        Event cat2 = new Event("Category 2", players, courts, timeslots.subList(2, 7), 1, 1, 2);
        Event cat3 = new Event("Category 3", players, courts, timeslots.subList(5, 7), 1, 1, 2);

        Tournament tournament = new Tournament("Tournament", cat1, cat2, cat3);
        tournament.getSolver().setSearchStrategy(SearchStrategy.MINDOM_UB);

        assertTrue(tournament.solve());

        Map<Event, EventSchedule> schedules = tournament.getEventSchedules();

        assertNull(schedules.get(cat1).filterMatchesDuringTimeslotRange(timeslots.get(4), timeslots.get(6)));
        assertEquals(4,
                schedules.get(cat1).filterMatchesDuringTimeslotRange(timeslots.get(0), timeslots.get(3)).size()
        );

        assertNull(schedules.get(cat2).filterMatchesDuringTimeslotRange(timeslots.get(0), timeslots.get(1)));
        assertEquals(4,
                schedules.get(cat2).filterMatchesDuringTimeslotRange(timeslots.get(2), timeslots.get(6)).size()
        );

        assertNull(schedules.get(cat3).filterMatchesDuringTimeslotRange(timeslots.get(0), timeslots.get(4)));
        assertEquals(4,
                schedules.get(cat3).filterMatchesDuringTimeslotRange(timeslots.get(5), timeslots.get(6)).size()
        );
    }

    @Test
    public void multiTournamentWithPartiallySharedTimeslotsAndDifferentPlayersCaseTest() throws ValidationException {
        List<Player> players = TournamentUtils.buildGenericPlayers(24, "Pl");
        List<Localization> courts = TournamentUtils.buildGenericLocalizations(3, "Court");
        List<Timeslot> timeslots = TournamentUtils.buildDayOfWeekTimeslots(7);

        Event cat1 = new Event("Category 1", players.subList(0, 8), courts, timeslots.subList(0, 4), 1, 1, 2);
        Event cat2 = new Event("Category 2", players.subList(8, 16), courts, timeslots.subList(2, 7), 1, 1, 2);
        Event cat3 = new Event("Category 3", players.subList(16, 24), courts, timeslots.subList(5, 7), 1, 1, 2);

        Tournament tournament = new Tournament("Tournament", cat1, cat2, cat3);

        assertTrue(tournament.solve());

        Map<Event, EventSchedule> schedules = tournament.getEventSchedules();

        assertNull(schedules.get(cat1).filterMatchesDuringTimeslotRange(timeslots.get(4), timeslots.get(6)));
        assertEquals(4,
                schedules.get(cat1).filterMatchesDuringTimeslotRange(timeslots.get(0), timeslots.get(3)).size()
        );

        assertNull(schedules.get(cat2).filterMatchesDuringTimeslotRange(timeslots.get(0), timeslots.get(1)));
        assertEquals(4,
                schedules.get(cat2).filterMatchesDuringTimeslotRange(timeslots.get(2), timeslots.get(6)).size()
        );

        assertNull(schedules.get(cat3).filterMatchesDuringTimeslotRange(timeslots.get(0), timeslots.get(4)));
        assertEquals(4,
                schedules.get(cat3).filterMatchesDuringTimeslotRange(timeslots.get(5), timeslots.get(6)).size()
        );
    }

    @Test
    public void multiTournamentInfeasibleCaseTest() throws ValidationException {
        List<Player> players = TournamentUtils.buildGenericPlayers(8, "Pl");
        List<Localization> courts = TournamentUtils.buildGenericLocalizations(1, "Court");
        List<Timeslot> timeslots = TournamentUtils.buildDayOfWeekTimeslots(7);

        Event cat1 = new Event("Category 1", players, courts, timeslots);
        Event cat2 = new Event("Category 2", players, courts, timeslots);
        Event cat3 =
                new Event("Category 3", TournamentUtils.buildGenericPlayers(16, "PCat3"), courts, timeslots, 1, 2, 4);

        Tournament tournament = new Tournament("Tournament", cat1, cat2, cat3);
        tournament.getSolver().setSearchStrategy(SearchStrategy.MINDOM_UB);

        assertFalse(tournament.solve());
        assertNull(tournament.getSchedule());
        assertNull(tournament.getEventSchedules());
        assertEquals(0, tournament.getSolver().getFoundSolutions());
    }

    @Test
    public void multiTournamentWithDifferentPlayersPerMatchCaseTest() throws ValidationException {
        List<Player> players = TournamentUtils.buildGenericPlayers(33, "Pl");
        List<Localization> courts = TournamentUtils.buildGenericLocalizations(4, "Court");
        List<Timeslot> timeslots = TournamentUtils.buildSimpleTimeslots(10);

        Event cat1 = new Event("Category 1", players.subList(0, 8), courts, timeslots);
        Event cat2 = new Event("Category 2", players.subList(8, 24), courts, timeslots, 1, 2, 4);
        Event cat3 = new Event("Category 3", players.subList(24, 33), courts, timeslots, 1, 2, 3);

        Tournament tournament = new Tournament("Tournament", cat1, cat2, cat3);

        assertTrue(tournament.solve());

        Map<Event, EventSchedule> schedules = tournament.getEventSchedules();

        for (Match match : schedules.get(cat1).getMatches())
            assertEquals(2, match.getPlayers().size());

        for (Match match : schedules.get(cat2).getMatches())
            assertEquals(4, match.getPlayers().size());

        for (Match match : schedules.get(cat3).getMatches())
            assertEquals(3, match.getPlayers().size());

    }

    @Test
    public void multiTournamentWithDifferentMatchesPerPlayerCaseTest() throws ValidationException {
        List<Player> players = TournamentUtils.buildGenericPlayers(24, "Pl");
        List<Localization> courts = TournamentUtils.buildGenericLocalizations(4, "Court");
        List<Timeslot> timeslots = TournamentUtils.buildSimpleTimeslots(16);

        Event cat1 = new Event("Category 1", players.subList(0, 8), courts, timeslots);
        Event cat2 = new Event("Category 2", players.subList(8, 16), courts, timeslots, 2, 2, 2);
        Event cat3 = new Event("Category 3", players.subList(16, 24), courts, timeslots, 3, 2, 2);

        Tournament tournament = new Tournament("Tournament", cat1, cat2, cat3);
        tournament.getSolver().setSearchStrategy(SearchStrategy.MINDOM_UB);

        assertTrue(tournament.solve());

        Map<Event, EventSchedule> schedules = tournament.getEventSchedules();
        for (Event event : tournament.getEvents())
            assertEquals(event.getNumberOfMatches(), schedules.get(event).getMatches().size());
    }

    @Test
    public void multiTournamentWithDifferentTimeslotsPerMatchCaseTest() throws ValidationException {
        List<Player> players = TournamentUtils.buildGenericPlayers(24, "Pl");
        List<Localization> courts = TournamentUtils.buildGenericLocalizations(4, "Court");
        List<Timeslot> timeslots = TournamentUtils.buildSimpleTimeslots(16);

        Event cat1 = new Event("Category 1", players.subList(0, 8), courts, timeslots);
        Event cat2 = new Event("Category 2", players.subList(8, 16), courts, timeslots, 1, 4, 2);
        Event cat3 = new Event("Category 3", players.subList(16, 24), courts, timeslots, 1, 3, 2);

        Tournament tournament = new Tournament("Tournament", cat1, cat2, cat3);
        tournament.getSolver().setSearchStrategy(SearchStrategy.MINDOM_UB);

        assertTrue(tournament.solve());

        Map<Event, EventSchedule> schedules = tournament.getEventSchedules();

        for (Match match : schedules.get(cat1).getMatches())
            assertEquals(2, match.getDuration());

        for (Match match : schedules.get(cat2).getMatches())
            assertEquals(4, match.getDuration());

        for (Match match : schedules.get(cat3).getMatches())
            assertEquals(3, match.getDuration());
    }

    @Test
    public void multiTournamentWithSameBreaksCaseTest() throws ValidationException {
        List<Localization> courts = TournamentUtils.buildGenericLocalizations(2, "Court");
        List<Timeslot> timeslots = TournamentUtils.buildSimpleTimeslots(15);

        Event cat1 = new Event("Category 1", TournamentUtils.buildGenericPlayers(8, "PCat1"), courts, timeslots);
        Event cat2 = new Event("Category 2", TournamentUtils.buildGenericPlayers(8, "PCat2"), courts, timeslots);
        Event cat3 = new Event("Category 3", TournamentUtils.buildGenericPlayers(8, "PCat3"), courts, timeslots);

        Tournament tournament = new Tournament("Tournament", cat1, cat2, cat3);
        tournament.getSolver().setSearchStrategy(SearchStrategy.MINDOM_UB);

        Timeslot b1 = timeslots.get(2);
        Timeslot b2 = timeslots.get(7);
        Timeslot b3 = timeslots.get(12);

        tournament.addBreak(b1);
        tournament.addBreak(b2);
        tournament.addBreak(b3);

        assertTrue(tournament.solve());

        List<Timeslot> tournamentBreaks = new ArrayList<>(Arrays.asList(b1, b2, b3));
        for (Match match : tournament.getSchedule().getMatches())
            for (Timeslot tournamentBreak : tournamentBreaks)
                assertFalse(match.during(tournamentBreak));
    }

    @Test
    public void multiTournamentWithDifferentBreaksCaseTest() throws ValidationException {
        List<Localization> courts = TournamentUtils.buildGenericLocalizations(2, "Court");
        List<Timeslot> timeslots = TournamentUtils.buildSimpleTimeslots(15);

        Event cat1 = new Event("Category 1", TournamentUtils.buildGenericPlayers(8, "PCat1"), courts, timeslots);
        Event cat2 = new Event("Category 2", TournamentUtils.buildGenericPlayers(8, "PCat2"), courts, timeslots);
        Event cat3 = new Event("Category 3", TournamentUtils.buildGenericPlayers(8, "PCat3"), courts, timeslots);

        Tournament tournament = new Tournament("Tournament", cat1, cat2, cat3);
        tournament.getSolver().setSearchStrategy(SearchStrategy.MINDOM_UB);

        cat1.addBreak(timeslots.get(2));
        cat1.addBreak(timeslots.get(8));
        cat2.addBreak(timeslots.get(7));
        cat3.addBreak(timeslots.get(3));
        cat3.addBreak(timeslots.get(9));
        cat3.addBreak(timeslots.get(13));

        assertTrue(tournament.solve());

        Map<Event, EventSchedule> schedules = tournament.getEventSchedules();

        for (Event event : tournament.getEvents())
            for (Match match : schedules.get(event).getMatches())
                for (Timeslot eventBreak : event.getBreaks())
                    assertFalse(match.during(eventBreak));
    }

    @Test
    public void multiTournamentWithUnavailablePlayersCaseTest() throws ValidationException {
        List<Player> players = TournamentUtils.buildGenericPlayers(16, "Pl");
        List<Localization> courts = TournamentUtils.buildGenericLocalizations(2, "Court");
        List<Timeslot> timeslots = TournamentUtils.buildSimpleTimeslots(15);

        Event cat1 = new Event("Category 1", players.subList(0, 10), courts, timeslots);
        Event cat2 = new Event("Category 2", players.subList(10, 16), courts, timeslots);
        Event cat3 = new Event("Category 3", players.subList(4, 12), courts, timeslots);

        Tournament tournament = new Tournament("Tournament", cat1, cat2, cat3);
        tournament.getSolver().setSearchStrategy(SearchStrategy.MINDOM_UB);

        tournament.addUnavailablePlayerAtTimeslots(players.get(0),
                new HashSet<>(Arrays.asList(timeslots.get(3), timeslots.get(4), timeslots.get(5), timeslots.get(6)))
        );
        tournament.addUnavailablePlayerAtTimeslots(players.get(2),
                new HashSet<>(Arrays.asList(timeslots.get(0), timeslots.get(1)))
        );
        tournament.addUnavailablePlayerAtTimeslots(players.get(3),
                new HashSet<>(Arrays.asList(timeslots.get(7),
                        timeslots.get(8),
                        timeslots.get(11),
                        timeslots.get(12),
                        timeslots.get(13)
                ))
        );
        tournament.addUnavailablePlayerAtTimeslots(players.get(5),
                new HashSet<>(Arrays.asList(timeslots.get(2),
                        timeslots.get(3),
                        timeslots.get(4),
                        timeslots.get(5),
                        timeslots.get(6)
                ))
        );
        tournament.addUnavailablePlayerAtTimeslots(players.get(6),
                new HashSet<>(Arrays.asList(timeslots.get(13), timeslots.get(14)))
        );
        tournament.addUnavailablePlayerAtTimeslots(players.get(9),
                new HashSet<>(Arrays.asList(timeslots.get(5), timeslots.get(6), timeslots.get(7), timeslots.get(8)))
        );
        tournament.addUnavailablePlayerAtTimeslots(players.get(10), new HashSet<>(Arrays.asList(timeslots.get(0))));
        tournament.addUnavailablePlayerAtTimeslots(players.get(13), new HashSet<>(Arrays.asList(timeslots.get(14))));
        tournament.addUnavailablePlayerAtTimeslots(players.get(14),
                new HashSet<>(Arrays.asList(timeslots.get(10), timeslots.get(11), timeslots.get(12)))
        );
        tournament.addUnavailablePlayerAtTimeslots(players.get(15),
                new HashSet<>(Arrays.asList(timeslots.get(2), timeslots.get(3), timeslots.get(4), timeslots.get(5)))
        );

        assertTrue(tournament.solve());

        List<Event> events = tournament.getEvents();
        TournamentSchedule schedule = tournament.getSchedule();
        for (Player player : tournament.getAllPlayers())
            for (Match match : schedule.filterMatchesByPlayer(player))
                for (Event event : events) {
                    Set<Timeslot> unavailableTimeslots = event.getUnavailablePlayers().get(player);
                    if (unavailableTimeslots != null)
                        for (Timeslot t : unavailableTimeslots)
                            assertFalse(match.during(t));
                }
    }

    @Test
    public void multiTournamentWithDifferentCategoryPlayerUnavailabilityCaseTest() throws ValidationException {
        List<Player> players = TournamentUtils.buildGenericPlayers(4, "Pl");
        List<Localization> courts = TournamentUtils.buildGenericLocalizations(2, "Court");
        List<Timeslot> timeslots = TournamentUtils.buildSimpleTimeslots(4);

        Event cat1 = new Event("Category 1", players, courts, timeslots);
        Event cat2 = new Event("Category 2", players, courts, timeslots);

        Tournament tournament = new Tournament("Tournament", cat1, cat2);
        tournament.getSolver().setSearchStrategy(SearchStrategy.MINDOM_UB);

        cat1.addUnavailablePlayerAtTimeslots(players.get(0),
                new HashSet<>(Arrays.asList(timeslots.get(0), timeslots.get(1)))
        );
        cat2.addUnavailablePlayerAtTimeslots(players.get(0),
                new HashSet<>(Arrays.asList(timeslots.get(2), timeslots.get(3)))
        );

        cat1.addUnavailablePlayerAtTimeslot(players.get(1), timeslots.get(1));

        assertTrue(tournament.solve());

        Map<Event, EventSchedule> schedules = tournament.getEventSchedules();

        do {
            assertFalse(schedules.get(cat1)
                    .filterMatchesByPlayer(players.get(0))
                    .get(0)
                    .during(timeslots.get(0), timeslots.get(1)));
            assertTrue(schedules.get(cat1)
                    .filterMatchesByPlayer(players.get(0))
                    .get(0)
                    .during(timeslots.get(2), timeslots.get(3)));
            assertFalse(schedules.get(cat2)
                    .filterMatchesByPlayer(players.get(0))
                    .get(0)
                    .during(timeslots.get(2), timeslots.get(3)));
            assertTrue(schedules.get(cat2)
                    .filterMatchesByPlayer(players.get(0))
                    .get(0)
                    .during(timeslots.get(0), timeslots.get(1)));

            assertFalse(schedules.get(cat1).filterMatchesByPlayer(players.get(1)).get(0).during(timeslots.get(1)));
            assertTrue(schedules.get(cat1)
                    .filterMatchesByPlayer(players.get(1))
                    .get(0)
                    .during(timeslots.get(2), timeslots.get(3)));
        } while (tournament.nextSchedules());

        assertEquals(40, tournament.getSolver().getFoundSolutions());
    }

    @Test
    public void multiTournamentWithUnavailableLocalizationCaseTest() throws ValidationException {
        List<Player> players = TournamentUtils.buildGenericPlayers(16, "Pl");
        List<Localization> courts = TournamentUtils.buildGenericLocalizations(5, "Court");
        List<Timeslot> timeslots = TournamentUtils.buildSimpleTimeslots(10);

        Event cat1 = new Event("Category 1", players.subList(0, 8), courts, timeslots);
        Event cat2 = new Event("Category 2", players.subList(4, 16), courts, timeslots);
        Event cat3 = new Event("Category 3", players.subList(4, 12), courts, timeslots);

        Tournament tournament = new Tournament("Tournament", cat1, cat2, cat3);
        tournament.getSolver().setSearchStrategy(SearchStrategy.MINDOM_UB);

        tournament.addUnavailableLocalizationAtTimeslots(courts.get(0),
                new HashSet<>(Arrays.asList(timeslots.get(6), timeslots.get(7), timeslots.get(8), timeslots.get(9)))
        );
        tournament.addUnavailableLocalizationAtTimeslots(courts.get(1),
                new HashSet<>(Arrays.asList(timeslots.get(3), timeslots.get(5), timeslots.get(6), timeslots.get(7)))
        );
        tournament.addUnavailableLocalizationAtTimeslots(courts.get(2),
                new HashSet<>(Arrays.asList(timeslots.get(0), timeslots.get(1), timeslots.get(2), timeslots.get(3)))
        );
        tournament.addUnavailableLocalizationAtTimeslots(courts.get(3),
                new HashSet<>(Arrays.asList(timeslots.get(0), timeslots.get(7), timeslots.get(8), timeslots.get(9)))
        );
        tournament.addUnavailableLocalizationAtTimeslots(courts.get(4),
                new HashSet<>(Arrays.asList(timeslots.get(0), timeslots.get(1)))
        );

        assertTrue(tournament.solve());

        List<Event> events = tournament.getEvents();
        TournamentSchedule schedule = tournament.getSchedule();
        for (Localization court : tournament.getAllLocalizations())
            for (Match match : schedule.filterMatchesByLocalization(court))
                for (Event event : events) {
                    Set<Timeslot> unavailableTimeslots = event.getUnavailableLocalizations().get(court);
                    if (unavailableTimeslots != null)
                        for (Timeslot t : unavailableTimeslots)
                            assertFalse(match.during(t));
                }
    }

    @Test
    public void multiTournamentWithDifferentCategoryLocalizationUnavailabilityCaseTest() throws ValidationException {
        List<Player> players = TournamentUtils.buildGenericPlayers(8, "Pl");
        List<Localization> courts = TournamentUtils.buildGenericLocalizations(2, "Court");
        List<Timeslot> timeslots = TournamentUtils.buildSimpleTimeslots(10);

        Event cat1 = new Event("Category 1", players, courts, timeslots);
        Event cat2 = new Event("Category 2", players, courts, timeslots);

        cat1.addUnavailableLocalizationAtTimeslots(courts.get(0),
                new HashSet<>(Arrays.asList(timeslots.get(0), timeslots.get(1), timeslots.get(2)))
        );
        cat2.addUnavailableLocalizationAtTimeslots(courts.get(1),
                new HashSet<>(Arrays.asList(timeslots.get(0), timeslots.get(1), timeslots.get(2)))
        );

        Tournament tournament = new Tournament("Tournament", cat1, cat2);
        tournament.getSolver().setSearchStrategy(SearchStrategy.MINDOM_UB);

        assertTrue(tournament.solve());

        Map<Event, EventSchedule> schedules = tournament.getEventSchedules();
        for (Match match : schedules.get(cat1).filterMatchesByLocalization(courts.get(0)))
            assertFalse(match.during(timeslots.get(0), timeslots.get(2)));

        for (Match match : schedules.get(cat2).filterMatchesByLocalization(courts.get(1)))
            assertFalse(match.during(timeslots.get(0), timeslots.get(2)));
    }

    @Test
    public void multiTournamentWithPredefinedMatchupsCaseTest() throws ValidationException {
        List<Player> players = TournamentUtils.buildGenericPlayers(8, "Pl");
        List<Localization> courts = TournamentUtils.buildGenericLocalizations(2, "Court");
        List<Timeslot> timeslots = TournamentUtils.buildSimpleTimeslots(10);

        Event cat1 = new Event("Category 1", players, courts, timeslots);
        Event cat2 = new Event("Category 2", players, courts, timeslots);

        Tournament tournament = new Tournament("Tournament", cat1, cat2);
        tournament.getSolver().setSearchStrategy(SearchStrategy.MINDOM_UB);

        cat1.addMatchup(players.get(0), players.get(2));
        cat2.addMatchup(players.get(0), players.get(2));

        assertTrue(tournament.solve());

        Map<Event, EventSchedule> schedules = tournament.getEventSchedules();
        Match match = schedules.get(cat2).filterMatchesByPlayer(players.get(0)).get(0);
        assertFalse(schedules.get(cat1)
                .filterMatchesByPlayer(players.get(0))
                .get(0)
                .during(match.getStartTimeslot(), match.getEndTimeslot()));
    }

    @Test
    public void multiTournamentWithAssignedLocalizationsToPlayersCaseTest() throws ValidationException {
        List<Player> players = TournamentUtils.buildGenericPlayers(8, "Pl");
        List<Localization> courts = TournamentUtils.buildGenericLocalizations(2, "Court");
        List<Timeslot> timeslots = TournamentUtils.buildSimpleTimeslots(10);

        Event cat1 = new Event("Category 1", players, courts, timeslots);
        Event cat2 = new Event("Category 2", players, courts, timeslots);

        Tournament tournament = new Tournament("Tournament", cat1, cat2);
        tournament.getSolver().setSearchStrategy(SearchStrategy.MINDOM_UB);

        cat1.addPlayerInLocalization(players.get(3), courts.get(1));
        cat1.addPlayerInLocalization(players.get(5), courts.get(1));
        cat1.addPlayerInLocalization(players.get(1), courts.get(0));
        cat2.addPlayerInLocalization(players.get(2), courts.get(1));
        cat2.addPlayerInLocalization(players.get(0), courts.get(0));
        cat2.addPlayerInLocalization(players.get(7), courts.get(0));

        assertTrue(tournament.solve());

        Map<Event, EventSchedule> schedules = tournament.getEventSchedules();
        Assert.assertEquals(courts.get(1),
                schedules.get(cat1).filterMatchesByPlayer(players.get(3)).get(0).getLocalization()
        );
        Assert.assertEquals(courts.get(1),
                schedules.get(cat1).filterMatchesByPlayer(players.get(5)).get(0).getLocalization()
        );
        Assert.assertEquals(courts.get(0),
                schedules.get(cat1).filterMatchesByPlayer(players.get(1)).get(0).getLocalization()
        );
        Assert.assertEquals(courts.get(1),
                schedules.get(cat2).filterMatchesByPlayer(players.get(2)).get(0).getLocalization()
        );
        Assert.assertEquals(courts.get(0),
                schedules.get(cat2).filterMatchesByPlayer(players.get(0)).get(0).getLocalization()
        );
        Assert.assertEquals(courts.get(0),
                schedules.get(cat2).filterMatchesByPlayer(players.get(7)).get(0).getLocalization()
        );
    }

    @Test
    public void multiTournamentWithAssignedTimeslotsToPlayersCaseTest() throws ValidationException {
        List<Player> players = TournamentUtils.buildGenericPlayers(8, "Pl");
        List<Localization> courts = TournamentUtils.buildGenericLocalizations(2, "Court");
        List<Timeslot> timeslots = TournamentUtils.buildSimpleTimeslots(10);

        Event cat1 = new Event("Category 1", players, courts, timeslots);
        Event cat2 = new Event("Category 2", players, courts, timeslots);

        Tournament tournament = new Tournament("Tournament", cat1, cat2);
        tournament.getSolver().setSearchStrategy(SearchStrategy.MINDOM_UB);

        cat1.addPlayerAtTimeslot(players.get(0), timeslots.get(5));
        cat1.addPlayerAtTimeslot(players.get(2), timeslots.get(7));
        cat1.addPlayerAtTimeslots(players.get(6),
                new HashSet<>(Arrays.asList(timeslots.get(3), timeslots.get(4), timeslots.get(5), timeslots.get(6)))
        );
        Set<Timeslot> assignedTimeslots =
                new HashSet<>(Arrays.asList(timeslots.get(6), timeslots.get(7), timeslots.get(8), timeslots.get(9)));
        cat2.addPlayerAtTimeslots(players.get(4), assignedTimeslots);
        cat2.addPlayerAtTimeslots(players.get(5), assignedTimeslots);

        assertTrue(tournament.solve());

        Map<Event, EventSchedule> schedules = tournament.getEventSchedules();
        Assert.assertEquals(timeslots.get(5),
                schedules.get(cat1).filterMatchesByPlayer(players.get(0)).get(0).getStartTimeslot()
        );
        Assert.assertEquals(timeslots.get(7),
                schedules.get(cat1).filterMatchesByPlayer(players.get(2)).get(0).getStartTimeslot()
        );
        assertTrue(schedules.get(cat1)
                .filterMatchesByPlayer(players.get(6))
                .get(0)
                .within(timeslots.get(3), timeslots.get(6)));
        assertTrue(schedules.get(cat2)
                .filterMatchesByPlayer(players.get(4))
                .get(0)
                .within(timeslots.get(6), timeslots.get(9)));
        assertTrue(schedules.get(cat2)
                .filterMatchesByPlayer(players.get(5))
                .get(0)
                .within(timeslots.get(6), timeslots.get(9)));
    }

    @Test
    public void multiTournamentWithDifferentMatchupModesCaseTest() throws ValidationException {
        List<Player> players = TournamentUtils.buildGenericPlayers(8, "Player");
        List<Localization> courts = TournamentUtils.buildGenericLocalizations(3, "Court");
        List<Timeslot> timeslots = TournamentUtils.buildSimpleTimeslots(16);

        Event cat1 = new Event("Category 1", players, courts, timeslots, 3, 1, 2);
        Event cat2 = new Event("Category 2", players, courts, timeslots, 3, 1, 2);
        Event cat3 = new Event("Category 3", players, courts, timeslots, 3, 1, 2);

        cat1.setMatchupMode(MatchupMode.ALL_DIFFERENT);
        cat2.setMatchupMode(MatchupMode.ALL_EQUAL);
        cat3.setMatchupMode(MatchupMode.ANY);

        Tournament tournament = new Tournament("Tournament", cat1, cat2, cat3);
        tournament.getSolver().setSearchStrategy(SearchStrategy.MINDOM_UB);

        assertTrue(tournament.solve());

        Map<Event, EventSchedule> schedules = tournament.getEventSchedules();
        for (Player player : players)
            for (Event event : tournament.getEvents())
                for (Match match : schedules.get(event).filterMatchesByPlayer(player))
                    tournament.getEvents().stream().filter(otherEvent -> event != otherEvent).forEach(otherEvent -> {
                        for (Match otherMatch : schedules.get(otherEvent).filterMatchesByPlayer(player))
                            assertFalse(otherMatch.during(match.getStartTimeslot(), match.getEndTimeslot()));
                    });

        List<Match> matches = schedules.get(cat1).getMatches();
        for (int i = 0; i < matches.size() - 1; i++)
            for (int j = i + 1; j < matches.size(); j++)
                assertNotEquals(matches.get(i).getPlayers(), matches.get(j).getPlayers());

        for (Player player : players) {
            List<Match> playerMatches = schedules.get(cat2).filterMatchesByPlayer(player);
            for (int i = 0; i < playerMatches.size() - 1; i++)
                for (int j = i + 1; j < playerMatches.size(); j++)
                    assertEquals(playerMatches.get(i).getPlayers(), playerMatches.get(j).getPlayers());
        }

        for (Player player : players) {
            List<Match> playerMatches = schedules.get(cat2).filterMatchesByPlayer(player);
            int count = 0;
            for (int i = 0; i < playerMatches.size() - 1; i++)
                for (int j = i + 1; j < playerMatches.size(); j++)
                    if (playerMatches.get(i).getPlayers().equals(playerMatches.get(j).getPlayers()))
                        count++;
            assertTrue(count >= 1 && count <= 3);
        }
    }

    @Test
    public void leagueCaseTest() throws ValidationException {
        List<Player> players = TournamentUtils.buildGenericPlayers(6, "Team");
        List<Localization> courts = TournamentUtils.buildGenericLocalizations(5, "Court");
        List<Timeslot> timeslots = TournamentUtils.buildDayOfWeekTimeslots(5);

        Event event = new Event("Event", players, courts, timeslots, 5, 1, 2);
        event.setMatchupMode(MatchupMode.ALL_DIFFERENT);

        Tournament tournament = new Tournament("Basketball League", event);

        tournament.getSolver().setSearchStrategy(SearchStrategy.MINDOM_UB);
        tournament.getSolver().prioritizeTimeslots(false);

        assertTrue(tournament.solve());

        TournamentSchedule schedule = tournament.getSchedule();
        for (Player player : players) {
            List<Match> playerMatches = schedule.filterMatchesByPlayer(player);
            assertEquals(5, playerMatches.size());
            for (int i = 0; i < playerMatches.size() - 1; i++)
                for (int j = i + 1; j < playerMatches.size(); j++)
                    assertNotEquals(playerMatches.get(i).getPlayers(), playerMatches.get(j).getPlayers());
        }
    }

    @Test
    public void multiLeagueWithSharedLocalizationsAndDifferentPlayersCaseTest() throws ValidationException {
        List<Localization> courts = TournamentUtils.buildGenericLocalizations(10, "Court");
        List<Timeslot> timeslots = TournamentUtils.buildDayOfWeekTimeslots(5);

        Event cat1 = new Event("Event", TournamentUtils.buildGenericPlayers(6, "PCat1"), courts, timeslots, 5, 1, 2);
        Event cat2 = new Event("Event", TournamentUtils.buildGenericPlayers(6, "PCat2"), courts, timeslots, 5, 1, 2);
        cat1.setMatchupMode(MatchupMode.ALL_DIFFERENT);
        cat2.setMatchupMode(MatchupMode.ALL_DIFFERENT);

        Tournament tournament = new Tournament("League", cat1, cat2);

        tournament.getSolver().setSearchStrategy(SearchStrategy.MINDOM_UB);

        assertTrue(tournament.solve());

        TournamentSchedule schedule = tournament.getSchedule();
        for (Player player : tournament.getAllPlayers()) {
            List<Match> playerMatches = schedule.filterMatchesByPlayer(player);
            assertEquals(5, playerMatches.size());
            for (int i = 0; i < playerMatches.size() - 1; i++)
                for (int j = i + 1; j < playerMatches.size(); j++)
                    assertNotEquals(playerMatches.get(i).getPlayers(), playerMatches.get(j).getPlayers());
        }
    }

    @Test
    public void multiLeagueWithSharedLocalizationsAndPartiallySharedPlayersCaseTest() throws ValidationException {
        List<Player> players = TournamentUtils.buildGenericPlayers(12, "Team");
        List<Localization> courts = TournamentUtils.buildGenericLocalizations(3, "Court");
        List<Timeslot> timeslots = TournamentUtils.buildDayOfWeekTimeslots(10);

        Event cat1 = new Event("Event", players.subList(0, 6), courts, timeslots, 5, 1, 2);
        Event cat2 = new Event("Event", players.subList(4, 10), courts, timeslots, 5, 1, 2);
        cat1.setMatchupMode(MatchupMode.ALL_DIFFERENT);
        cat2.setMatchupMode(MatchupMode.ALL_DIFFERENT);

        Tournament tournament = new Tournament("Basketball League", cat1, cat2);

        tournament.getSolver().setSearchStrategy(SearchStrategy.MINDOM_UB);

        assertTrue(tournament.solve());

        TournamentSchedule schedule = tournament.getSchedule();
        for (int p = 0; p < tournament.getAllPlayers().size(); p++) {
            List<Match> playerMatches = schedule.filterMatchesByPlayer(players.get(p));
            int nMatches = p == 4 || p == 5 ? 10 : 5;
            assertEquals(nMatches, playerMatches.size());
        }

        Map<Event, EventSchedule> schedules = tournament.getEventSchedules();
        for (Player player : cat1.getPlayers()) {
            List<Match> playerMatches = schedules.get(cat1).filterMatchesByPlayer(player);
            for (int i = 0; i < playerMatches.size() - 1; i++)
                for (int j = i + 1; j < playerMatches.size(); j++)
                    assertNotEquals(playerMatches.get(i).getPlayers(), playerMatches.get(j).getPlayers());
        }

        for (Player player : cat2.getPlayers()) {
            List<Match> playerMatches = schedules.get(cat2).filterMatchesByPlayer(player);
            for (int i = 0; i < playerMatches.size() - 1; i++)
                for (int j = i + 1; j < playerMatches.size(); j++)
                    assertNotEquals(playerMatches.get(i).getPlayers(), playerMatches.get(j).getPlayers());
        }
    }

    @Test
    public void resolutionDataTest() throws ValidationException, JsonProcessingException {
        Tournament tournament = new Tournament("Tournament", new Event("Event",
                TournamentUtils.buildGenericPlayers(32, "Pl"),
                TournamentUtils.buildGenericLocalizations(2, "Court"),
                TournamentUtils.buildSimpleTimeslots(8),
                1,
                2,
                4
        ));
        tournament.getSolver().setSearchStrategy(SearchStrategy.MINDOM_UB);

        assertNull(tournament.getSolver().getResolutionData());

        assertTrue(tournament.solve());

        ResolutionData data = tournament.getSolver().getResolutionData();
        assertNotNull(data);

        assertEquals(tournament.getSolver().getInternalSolver(), data.getSolver());
        assertEquals(tournament, data.getTournament());
        assertEquals("Tournament Solver [Tournament]", data.getSolverName());
        assertTrue(data.getResolutionProcessCompleted());
        assertEquals("MINDOM_UB", data.getSearchStrategy().toString());
        assertEquals("STARTED", data.getResolutionState().toString());
        assertEquals(1845, data.getVariables());
        assertEquals(1394, data.getConstraints());
        assertFalse(data.isDeafultSearchUsed());
        assertFalse(data.isSearchCompleted());
        assertEquals(1, data.getSolutions());
        assertTrue(data.getBuildingTime() > 0);
        assertTrue(data.getResolutionTime() > 0);
        assertEquals(29, data.getNodes());
        assertEquals(data.getNodes() / data.getResolutionTime(), data.getNodeProcessingRate(), 1);
        assertEquals(0, data.getFails());
        assertEquals(0, data.getBacktracks());
        assertEquals(0, data.getRestarts());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        System.out.print(data);
        assertThat(out.toString(), StringContains.containsString("Solver [Tournament Solver [Tournament]] features:"));
        assertThat(out.toString(), StringContains.containsString("Variables: 1.845"));
        assertThat(out.toString(), StringContains.containsString("Search strategy: MINDOM_UB"));
        assertThat(out.toString(), StringContains.containsString("Nodes: 29"));

        out.reset();
        System.out.print(data.toJson());
        assertThat(out.toString(), StringContains.containsString("\"constraints\":1394"));
        assertThat(out.toString(),
                StringContains.containsString(
                        "\"solutions\":1," + "\"resolutionState\":\"STARTED\",\"resolutionProcessCompleted\":true")
        );

        out.reset();
        System.out.print(data.toJsonPretty());
        assertThat(out.toString(), StringContains.containsString("\"searchStrategy\" : \"MINDOM_UB\""));
        assertThat(out.toString(), StringContains.containsString("\"tournament\" : \"Tournament\""));
    }
}
