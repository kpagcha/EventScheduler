package solver;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import data.model.schedule.EventSchedule;
import data.model.schedule.GroupedSchedule;
import data.model.schedule.TournamentSchedule;
import data.model.schedule.data.Match;
import data.model.tournament.Tournament;
import data.model.tournament.event.Event;
import data.model.tournament.event.entity.Localization;
import data.model.tournament.event.entity.Player;
import data.model.tournament.event.entity.timeslot.Timeslot;
import data.validation.validable.ValidationException;
import solver.TournamentSolver.MatchupMode;
import solver.TournamentSolver.SearchStrategy;
import utils.TournamentUtils;

/**
 * Tests de la clase {@link TournamentSolver}
 *
 */
public class TournamentSolverTest {
	
	private Tournament tournament;

	@Test
	public void constructorTest() {
		Event event = new Event(
			"Event",
			TournamentUtils.buildGenericPlayers(8, "Player"),
			TournamentUtils.buildGenericLocalizations(3, "Court"),
			TournamentUtils.buildDefiniteDayOfWeekTimeslots(14)
		);
		tournament = new Tournament("Tournament", event);
		
		TournamentSolver solver = new TournamentSolver(tournament);
		solver.setSearchStrategy(SearchStrategy.DOMOVERWDEG);
		solver.setFillTimeslotsFirst(true);
		solver.setResolutionTimeLimit(0);
		
		assertNull(solver.getSolver());
		assertEquals("Tournament", solver.getTournament().getName());
		assertTrue(solver.getPredefinedMatchups().isEmpty());
		
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
		assertTrue(solver.getFillTimeslotsFirst());
		
		assertEquals(8, solver.getPlayersIndices().length);
		assertEquals(3, solver.getLocalizationsIndices().length);
		assertEquals(14, solver.getTimeslotsIndices().length);
		assertEquals(0, solver.getResolutionTimeLimit());
		
		try {
			solver.setResolutionTimeLimit(-5);
			fail("IllegalArgumentException expected for invalid resolution time limit value");
		} catch (IllegalArgumentException e) {
			assertEquals("Resolution time limit cannot be less than zero", e.getMessage());
		}
	}
	
	@Test
	public void basicTournamentCaseTest() throws ValidationException {
		Event event = new Event(
			"Event",
			TournamentUtils.buildGenericPlayers(8, "Player"),
			TournamentUtils.buildGenericLocalizations(1, "Court"),
			TournamentUtils.buildDefiniteDayOfWeekTimeslots(8)
		);
		tournament = new Tournament("Tournament", event);
		tournament.getSolver().setSearchStrategy(SearchStrategy.MINDOM_UB);
		
		assertTrue(tournament.solve());
		
		assertEquals(1, tournament.getSolver().getFoundSolutionsCount());
		
		TournamentSchedule schedule = tournament.getSchedule();
		for (Player player : tournament.getAllPlayers())
			assertEquals(1, schedule.getMatchesByPlayer(player).size());
		
		assertEquals(4, schedule.getMatchesByLocalization(tournament.getAllLocalizations().get(0)).size());
		
		assertEquals(
			tournament.getNumberOfOccupiedTimeslots(),
			Stream.of(schedule.getScheduleValues()).flatMap(v -> Arrays.stream(v)).filter(v -> v.isOccupied()).count()
		);
		
		assertEquals(100, new Double(new GroupedSchedule(tournament).getOccupationRatio() * 100).intValue());
		
		tournament.getSolver().setResolutionTimeLimit(1);
		assertFalse(tournament.solve());
		
		tournament.getSolver().setResolutionTimeLimit(0);
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				tournament.getSolver().stopResolutionProcess();
			}
		});
		try {
			thread.start();
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		assertFalse(tournament.solve());
	}
	
	@Test
	public void basicTournamentWithOneSolutionCastTest() throws ValidationException {
		Event event = new Event(
			"Event",
			TournamentUtils.buildGenericPlayers(2, "Player"),
			TournamentUtils.buildGenericLocalizations(1, "Court"),
			TournamentUtils.buildDefiniteDayOfWeekTimeslots(2)
		);
		tournament = new Tournament("Tournament", event);
		tournament.getSolver().setSearchStrategy(SearchStrategy.MINDOM_UB);
		
		assertTrue(tournament.solve());
		assertNotNull(tournament.getSchedule());
		assertEquals(1, tournament.getSolver().getFoundSolutionsCount());
		
		assertFalse(tournament.nextSchedules());
		assertNull(tournament.getSchedule());
		assertEquals(1, tournament.getSolver().getFoundSolutionsCount());
	}
	
	@Test
	public void basicTournamentWithFewSolutionsCaseTest() throws ValidationException {
		Event event = new Event(
			"Event",
			TournamentUtils.buildGenericPlayers(2, "Player"),
			TournamentUtils.buildGenericLocalizations(1, "Court"),
			TournamentUtils.buildDefiniteDayOfWeekTimeslots(6)
		);
		tournament = new Tournament("Tournament", event);
		tournament.getSolver().setSearchStrategy(SearchStrategy.MINDOM_UB);
		
		assertTrue(tournament.solve());
		assertNotNull(tournament.getSchedule());
		assertEquals(1, tournament.getSolver().getFoundSolutionsCount());
		
		assertTrue(tournament.nextSchedules());
		assertNotNull(tournament.getSchedule());
		assertEquals(2, tournament.getSolver().getFoundSolutionsCount());
		
		while (tournament.nextSchedules());
		
		assertFalse(tournament.nextSchedules());
		assertNull(tournament.getSchedule());
		assertEquals(5, tournament.getSolver().getFoundSolutionsCount());
	}
	
	@Test
	public void basicTournamentWithOnePlayerMatchesCaseTest() throws ValidationException {
		fail("TO DO");
	}
 	
	@Test
	public void basicTournamentInfeasibleCaseTest() throws ValidationException {
		Event event = new Event(
			"Event",
			TournamentUtils.buildGenericPlayers(8, "Player"),
			TournamentUtils.buildGenericLocalizations(1, "Court"),
			TournamentUtils.buildDefiniteDayOfWeekTimeslots(7)
		);
		tournament = new Tournament("Tournament", event);
		tournament.getSolver().setSearchStrategy(SearchStrategy.MINDOM_UB);
		
		assertFalse(tournament.solve());
		assertNull(tournament.getSchedule());
		assertEquals(0, tournament.getSolver().getFoundSolutionsCount());
	}
	
	@Test
	public void basicTournamentPlayersPerMatchCaseTest() throws ValidationException {
		Event event = new Event(
			"Event",
			TournamentUtils.buildGenericPlayers(24, "Player"),
			TournamentUtils.buildGenericLocalizations(2, "Court"),
			TournamentUtils.buildAbstractTimeslots(6),
			1, 2, 4
		);
		tournament = new Tournament("Tournament", event);
		tournament.getSolver().setSearchStrategy(SearchStrategy.MINDOM_UB);
		
		assertTrue(tournament.solve());
		
		List<Match> matches = tournament.getSchedule().getMatches();
		assertEquals(6, matches.size());
		
		for (Match match : matches)
			assertEquals(4, match.getPlayers().size());
		
		assertEquals(100, new Double(new GroupedSchedule(tournament).getOccupationRatio() * 100).intValue());
	}
	
	@Test
	public void basicTournamentMatchesPerPlayerCaseTest() throws ValidationException {
		Event event = new Event(
			"Event",
			TournamentUtils.buildGenericPlayers(8, "Player"),
			TournamentUtils.buildGenericLocalizations(2, "Court"),
			TournamentUtils.buildAbstractTimeslots(10),
			2, 2, 2
		);
		tournament = new Tournament("Tournament", event);
		
		assertTrue(tournament.solve());
		
		TournamentSchedule schedule = tournament.getSchedule();
		for (Player player : tournament.getAllPlayers())
			assertEquals(2, schedule.getMatchesByPlayer(player).size());

		assertEquals(80, new Double(new GroupedSchedule(tournament).getOccupationRatio() * 100).intValue());
	}
	
	@Test
	public void basicTournamentTimeslotsPermatchCaseTest() throws ValidationException {
		Event event = new Event(
			"Event",
			TournamentUtils.buildGenericPlayers(8, "Player"),
			TournamentUtils.buildGenericLocalizations(1, "Court"),
			TournamentUtils.buildAbstractTimeslots(12),
			1, 3, 2
		);
		tournament = new Tournament("Tournament", event);
		
		assertTrue(tournament.solve());
		
		for (Match match : tournament.getSchedule().getMatches())
			assertEquals(3, match.getDuration());
	}
	
	@Test
	public void basicTournamentWithBreaksCaseTest() throws ValidationException {
		Event event = new Event(
			"Event",
			TournamentUtils.buildGenericPlayers(8, "Player"),
			TournamentUtils.buildGenericLocalizations(1, "Court"),
			TournamentUtils.buildAbstractTimeslots(10)
		);
		event.addBreak(event.getTimeslots().get(2));
		event.addBreak(event.getTimeslots().get(5));
		
		tournament = new Tournament("Tournament", event);
		
		assertTrue(tournament.solve());
		
		TournamentSchedule schedule = tournament.getSchedule();
		assertTrue(schedule.getMatchesByTimeslot(tournament.getAllTimeslots().get(2)).isEmpty());
		assertTrue(schedule.getMatchesByTimeslot(tournament.getAllTimeslots().get(5)).isEmpty());
	}
	
	@Test
	public void basicTournamentWithTeamsCaseTest() throws ValidationException {
		Event event = new Event(
			"Event",
			TournamentUtils.buildGenericPlayers(8, "Player"),
			TournamentUtils.buildGenericLocalizations(1, "Court"),
			TournamentUtils.buildAbstractTimeslots(4),
			1, 2, 4
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
		
		assertEquals(1, schedule.getMatchesByPlayers(new ArrayList<Player>(Arrays.asList(players.get(0), players.get(3)))).size());
		assertEquals(1, schedule.getMatchesByPlayers(new ArrayList<Player>(Arrays.asList(players.get(6), players.get(7)))).size());
		assertEquals(1, schedule.getMatchesByPlayers(new ArrayList<Player>(Arrays.asList(players.get(4), players.get(1)))).size());
		assertEquals(1, schedule.getMatchesByPlayers(new ArrayList<Player>(Arrays.asList(players.get(2), players.get(5)))).size());
	}
	
	@Test
	public void basicTournamentUnavailablePlayersCaseTest() throws ValidationException {
		Event event = new Event(
			"Event",
			TournamentUtils.buildGenericPlayers(8, "Player"),
			TournamentUtils.buildGenericLocalizations(1, "Court"),
			TournamentUtils.buildAbstractTimeslots(10)
		);
		List<Player> players = event.getPlayers();
		List<Timeslot> timeslots = event.getTimeslots();
		event.addUnavailablePlayerAtTimeslots(
			players.get(0),
			new HashSet<Timeslot>(Arrays.asList(timeslots.get(0), timeslots.get(1), timeslots.get(2), timeslots.get(3)))
		);
		event.addUnavailablePlayerAtTimeslots(
			players.get(1),
			new HashSet<Timeslot>(Arrays.asList(timeslots.get(5), timeslots.get(6)))
		);
		event.addUnavailablePlayerAtTimeslots(
			players.get(4),
			new HashSet<Timeslot>(Arrays.asList(timeslots.get(4), timeslots.get(5), timeslots.get(6)))
		);
		event.addUnavailablePlayerAtTimeslots(
			players.get(5),
			new HashSet<Timeslot>(Arrays.asList(timeslots.get(5), timeslots.get(6), timeslots.get(7), timeslots.get(8)))
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
		
		for (Match match : schedule.getMatchesByPlayer(players.get(0))) {
			assertFalse(match.getStartTimeslot().within(timeslots.get(0), timeslots.get(3)));
			assertFalse(match.getEndTimeslot().within(timeslots.get(0), timeslots.get(3)));
		}
		
		for (Match match : schedule.getMatchesByPlayer(players.get(1))) {
			assertFalse(match.getStartTimeslot().within(timeslots.get(5), timeslots.get(6)));
			assertFalse(match.getEndTimeslot().within(timeslots.get(5), timeslots.get(6)));
		}
		
		for (Match match : schedule.getMatchesByPlayer(players.get(4))) {
			assertFalse(match.getStartTimeslot().within(timeslots.get(4), timeslots.get(6)));
			assertFalse(match.getEndTimeslot().within(timeslots.get(4), timeslots.get(6)));
		}
		
		for (Match match : schedule.getMatchesByPlayer(players.get(5))) {
			assertFalse(match.getStartTimeslot().within(timeslots.get(5), timeslots.get(8)));
			assertFalse(match.getEndTimeslot().within(timeslots.get(5), timeslots.get(8)));
		}
	}
	
	@Test
	public void basicTournamentUnavailableLocalizationsCaseTest() throws ValidationException {
		Event event = new Event(
			"Event",
			TournamentUtils.buildGenericPlayers(8, "Player"),
			TournamentUtils.buildGenericLocalizations(3, "Court"),
			TournamentUtils.buildAbstractTimeslots(6)
		);
		List<Localization> localizations = event.getLocalizations();
		List<Timeslot> timeslots = event.getTimeslots();
		event.addUnavailableLocalizationAtTimeslots(
			localizations.get(0),
			new HashSet<Timeslot>(Arrays.asList(timeslots.get(0), timeslots.get(1), timeslots.get(2), timeslots.get(3)))
		);
		event.addUnavailableLocalizationAtTimeslots(
			localizations.get(1),
			new HashSet<Timeslot>(Arrays.asList(timeslots.get(4), timeslots.get(5)))
		);
		event.addUnavailableLocalizationAtTimeslots(
			localizations.get(2),
			new HashSet<Timeslot>(Arrays.asList(timeslots.get(2), timeslots.get(3)))
		);
		
		tournament = new Tournament("Tournament", event);
		
		assertTrue(tournament.solve());
		
		TournamentSchedule schedule = tournament.getSchedule();
		
		for (Match match : schedule.getMatchesByLocalization(localizations.get(0)))
			assertFalse(match.during(timeslots.get(0), timeslots.get(3)));
		
		for (Match match : schedule.getMatchesByLocalization(localizations.get(1)))
			assertFalse(match.during(timeslots.get(4), timeslots.get(5)));
		
		for (Match match : schedule.getMatchesByLocalization(localizations.get(2)))
			assertFalse(match.during(timeslots.get(2), timeslots.get(3)));
	}
	
	@Test
	public void basicTournamentPredefinedMatchupsCaseTest() throws ValidationException {
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
		
		List<Player> players = new ArrayList<>(
			Arrays.asList(djokovic, robert, mahut, belucci, raonic, kyrgios, kohlschreiber, nadal, federer, zverev)
		);
		Event event = new Event(
			"Event",
			players,
			TournamentUtils.buildGenericLocalizations(1, "Court"),
			TournamentUtils.buildAbstractTimeslots(15),
			1, 3, 2
		);
		event.addMatchup(djokovic, robert);
		event.addMatchup(mahut, belucci);
		event.addMatchup(raonic, kyrgios);
		event.addMatchup(kohlschreiber, nadal);
		event.addMatchup(federer, zverev);
		
		tournament = new Tournament("Tournament", event);
		
		assertTrue(tournament.solve());
		
		TournamentSchedule schedule = tournament.getSchedule();
		
		assertTrue(schedule.getMatchesByPlayer(djokovic).get(0).getPlayers().contains(robert));
		assertTrue(schedule.getMatchesByPlayer(mahut).get(0).getPlayers().contains(belucci));
		assertTrue(schedule.getMatchesByPlayer(raonic).get(0).getPlayers().contains(kyrgios));
		assertTrue(schedule.getMatchesByPlayer(kohlschreiber).get(0).getPlayers().contains(nadal));
		assertTrue(schedule.getMatchesByPlayer(federer).get(0).getPlayers().contains(zverev));
		
		GroupedSchedule groupedSchedule = new GroupedSchedule(tournament);
		assertEquals(100, new Double(groupedSchedule.getOccupationRatio() * 100).intValue());
		assertEquals(tournament.getNumberOfOccupiedTimeslots(), 2 * groupedSchedule.getOccupation());
	}
	
	@Test
	public void basicTournamentAssignedLocalizationsToPlayersCaseTest() throws ValidationException {
		Event event = new Event(
			"Event",
			TournamentUtils.buildGenericPlayers(12, "Pl"),
			TournamentUtils.buildGenericLocalizations(5, "Court"),
			TournamentUtils.buildUndefiniteTimeslots(4)
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
		
		Localization localization = schedule.getMatchesByPlayer(players.get(0)).get(0).getLocalization();
		assertTrue(localization.equals(localizations.get(0)) || localization.equals(localizations.get(1)));
		
		localization = schedule.getMatchesByPlayer(players.get(6)).get(0).getLocalization();
		assertTrue(localization.equals(localizations.get(3)) || localization.equals(localizations.get(4)));
		
		assertTrue(
			schedule.getMatchesByLocalization(localizations.get(4)).stream()
				.map(m -> m.getPlayers())
				.flatMap(l -> l.stream())
				.collect(Collectors.toList())
				.containsAll(new ArrayList<Player>(Arrays.asList(players.get(2), players.get(7))))
		);
		
		assertTrue(
			schedule.getMatchesByLocalization(localizations.get(2)).stream()
				.map(m -> m.getPlayers())
				.flatMap(l -> l.stream())
				.collect(Collectors.toList())
				.containsAll(new ArrayList<Player>(Arrays.asList(players.get(10), players.get(11))))
		);
	}
	
	@Test
	public void basicTournamentAssignedTimeslotsToPlayersCaseTest() throws ValidationException {
		Event event = new Event(
			"Event",
			TournamentUtils.buildGenericPlayers(8, "Player"),
			TournamentUtils.buildGenericLocalizations(1, "Court"),
			TournamentUtils.buildUndefiniteTimeslots(10)
		);
		List<Player> players = event.getPlayers();
		List<Timeslot> timeslots = event.getTimeslots();
		event.addPlayerAtStartTimeslot(players.get(3), timeslots.get(0));
		event.addPlayerAtStartTimeslot(players.get(2), timeslots.get(0));
		event.addPlayerAtStartTimeslot(players.get(0), timeslots.get(8));
		event.addPlayerAtTimeslots(players.get(6), new HashSet<Timeslot>(
			Arrays.asList(timeslots.get(2), timeslots.get(3), timeslots.get(4), timeslots.get(5)))
		);
		
		tournament = new Tournament("Tournament", event);
		
		assertTrue(tournament.solve());
		
		TournamentSchedule schedule = tournament.getSchedule();
		
		assertTrue(schedule.getMatchesByPlayers(
			new ArrayList<Player>(Arrays.asList(players.get(2), players.get(3)))).get(0).getStartTimeslot().equals(timeslots.get(0))
		);
		assertTrue(schedule.getMatchesByPlayer(players.get(0)).get(0).getStartTimeslot().equals(timeslots.get(8)));
		assertTrue(schedule.getMatchesByPlayer(players.get(6)).get(0).within(timeslots.get(2), timeslots.get(5)));
	}
	
	@Test
	public void basicTournamentAllDifferentCaseTest() throws ValidationException {
		Event event = new Event(
			"Event",
			TournamentUtils.buildGenericPlayers(8, "Player"),
			TournamentUtils.buildGenericLocalizations(1, "Court"),
			TournamentUtils.buildUndefiniteTimeslots(12),
			3, 1, 2
		);
		event.setMatchupMode(MatchupMode.ALL_DIFFERENT);
		
		tournament = new Tournament("Tournament", event);
		
		assertTrue(tournament.solve());
		
		TournamentSchedule schedule = tournament.getSchedule();
		
		List<Player> players = tournament.getAllPlayers();
		for (Player player : players)
			assertEquals(3, schedule.getMatchesByPlayer(player).size());
			
		List<Match> matches = schedule.getMatches();
		for (int i = 0; i < matches.size() - 1; i++)
			for (int j = i + 1; j < matches.size(); j++)
				assertFalse(matches.get(i).getPlayers().equals(matches.get(j).getPlayers()));
	}
	
	@Test
	public void basicTournamentAllEqualCaseTest() throws ValidationException {
		Event event = new Event(
			"Event",
			TournamentUtils.buildGenericPlayers(8, "Player"),
			TournamentUtils.buildGenericLocalizations(1, "Court"),
			TournamentUtils.buildUndefiniteTimeslots(12),
			3, 1, 2
		);
		event.setMatchupMode(MatchupMode.ALL_EQUAL);
		
		tournament = new Tournament("Tournament", event);
		
		assertTrue(tournament.solve());
		
		TournamentSchedule schedule = tournament.getSchedule();
		
		List<Player> players = tournament.getAllPlayers();
		for (Player player : players)
			assertEquals(3, schedule.getMatchesByPlayer(player).size());
			
		List<Match> matches = schedule.getMatches();
		for (int i = 0; i < matches.size(); i++) {
			int count = 0;
			for (int j = 0; j < matches.size(); j++)
				if (matches.get(i).getPlayers().equals(matches.get(j).getPlayers()))
					count ++;
			assertEquals(3, count);
		}
	}
	
	@Test
	public void basicTournamentAnyCaseTest() throws ValidationException {
		Event event = new Event(
			"Event",
			TournamentUtils.buildGenericPlayers(8, "Player"),
			TournamentUtils.buildGenericLocalizations(2, "Court"),
			TournamentUtils.buildUndefiniteTimeslots(12),
			3, 1, 2
		);
		event.setMatchupMode(MatchupMode.ANY);
		
		tournament = new Tournament("Tournament", event);
		
		assertTrue(tournament.solve());
		
		TournamentSchedule schedule = tournament.getSchedule();
		
		List<Player> players = tournament.getAllPlayers();
		for (Player player : players)
			assertEquals(3, schedule.getMatchesByPlayer(player).size());
			
		List<Match> matches = schedule.getMatches();
		for (int i = 0; i < matches.size(); i++) {
			int count = 0;
			for (int j = 0; j < matches.size(); j++)
				if (matches.get(i).getPlayers().equals(matches.get(j).getPlayers()))
					count ++;
			assertTrue(count >= 1 && count <= 3);
		}
	}
	
	@Test
	public void basicMultiTournamentCaseTest() throws ValidationException {
		List<Localization> courts = TournamentUtils.buildGenericLocalizations(3, "Court");
		List<Timeslot> timeslots = TournamentUtils.buildAbstractTimeslots(8);
		
		Event cat1 = new Event("Category 1", TournamentUtils.buildGenericPlayers(8, "PCat1"), courts, timeslots);
		Event cat2 = new Event("Category 2", TournamentUtils.buildGenericPlayers(8, "PCat2"), courts, timeslots);
		Event cat3 = new Event("Category 3", TournamentUtils.buildGenericPlayers(8, "PCat3"), courts, timeslots);
		
		Tournament tournament = new Tournament("Tournament", cat1, cat2, cat3);
		tournament.getSolver().setSearchStrategy(SearchStrategy.MINDOM_UB);
		
		assertEquals(0, tournament.getSolver().getFoundSolutionsCount());
		
		assertTrue(tournament.solve());
		
		assertEquals(1, tournament.getSolver().getFoundSolutionsCount());
		
		Map<Event, EventSchedule> schedules = tournament.getCurrentSchedules();
		assertEquals(4, schedules.get(cat1).getMatches().size());
		assertEquals(4, schedules.get(cat2).getMatches().size());
		assertEquals(4, schedules.get(cat3).getMatches().size());
		
		assertEquals(4, schedules.get(cat1).getMatchesByLocalization(courts.get(0)).size());
		assertTrue(schedules.get(cat3).getMatchesByLocalization(courts.get(0)).isEmpty());
		
		TournamentSchedule schedule = tournament.getSchedule();
		for (int i = 0; i < timeslots.size(); i += 2) {
			List<Match> matches = schedule.getMatchesByStartTimeslot(timeslots.get(i));
			assertEquals(3, matches.size());
			for (int j = 0; j < matches.size() - 1; j++)
				for (int k = j + 1; k < matches.size(); k++)
					assertNotEquals(matches.get(j).getLocalization(), matches.get(k).getLocalization());
		}
		
		assertEquals(100, new Double(new GroupedSchedule(tournament).getOccupationRatio() * 100).intValue());
	}
	
	@Test
	public void basicMultiTournamentWithFewSolutionsCaseTest() throws ValidationException {
		List<Localization> courts = TournamentUtils.buildGenericLocalizations(1, "Court");
		List<Timeslot> timeslots = TournamentUtils.buildDefiniteLocalTimeTimeslots(4);
		
		Event cat1 = new Event("Category 1", TournamentUtils.buildGenericPlayers(2, "PCat1"), courts, timeslots);
		Event cat2 = new Event("Category 2", TournamentUtils.buildGenericPlayers(2, "PCat2"), courts, timeslots);
		
		Tournament tournament = new Tournament("Tournament", cat1, cat2);
		tournament.getSolver().setSearchStrategy(SearchStrategy.MINDOM_UB);
		
		assertTrue(tournament.solve());
		assertEquals(1, tournament.getSolver().getFoundSolutionsCount());
		
		while (tournament.nextSchedules());
		assertEquals(2, tournament.getSolver().getFoundSolutionsCount());
		
		tournament.nextSchedules();
		assertNull(tournament.getCurrentSchedules());
		assertNull(tournament.getSchedule());
	}
	
	@Test
	public void basicMultiTournamentPartiallySharedPlayersCaseTest() {
		fail("TO DO");
	}
	
	@Test
	public void basicMultiTournamentPartiallySharedLocalizationsCasteTest() {
		fail("TO DO");
	}
	
	@Test
	public void basicMultiTournamentPartiallySharedTimeslotsCasteTest() {
		fail("TO DO");
	}
}
