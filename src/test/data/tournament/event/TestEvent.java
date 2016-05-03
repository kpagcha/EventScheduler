package data.tournament.event;

import static org.junit.Assert.*;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import data.model.tournament.event.Event;
import data.model.tournament.event.entity.Localization;
import data.model.tournament.event.entity.Player;
import data.model.tournament.event.entity.Team;
import data.model.tournament.event.entity.timeslot.AbstractTimeslot;
import data.model.tournament.event.entity.timeslot.DefiniteTimeslot;
import data.model.tournament.event.entity.timeslot.Timeslot;
import data.validation.validable.ValidationException;
import data.validation.validator.tournament.EventValidator;
import solver.TournamentSolver.MatchupMode;
import utils.TournamentUtils;

/**
 * Tests de la clase {@link Event}.
 *
 */
public class TestEvent {
	@Rule
	public ExpectedException expectedEx = ExpectedException.none();
	
	private Event event;
	private List<Player> players;
	private List<Localization> localizations;
	private List<Timeslot> timeslots;
	private int nMatchesPerPlayer, nTimeslotsPerMatch, nPlayersPerMatch;
	
	@Before
	public void setUp() {
		players = TournamentUtils.buildGenericPlayers(8, "Player");
		localizations = TournamentUtils.buildGenericLocalizations(2, "Court");
		timeslots = TournamentUtils.buildUndefiniteTimeslots(8);
		
		event = new Event("Event", players, localizations, timeslots, 1, 2, 2);
		
		nMatchesPerPlayer = 1;
		nTimeslotsPerMatch = 2;
		nPlayersPerMatch = 2;
	}
	
	@Test
	public void constructorsTestSuccess() {
		try {
			event = new Event("Event", players, localizations, timeslots);
			assertEquals(8, event.getPlayers().size());
			assertEquals(2, event.getLocalizations().size());
			assertEquals(8, event.getTimeslots().size());
			
			players = TournamentUtils.buildGenericPlayers(24, "Player");
			localizations = TournamentUtils.buildGenericLocalizations(5, "Court");
			timeslots = TournamentUtils.buildAbstractTimeslots(16);
			
			event = new Event("Event", players, localizations, timeslots, 2, 3, 4);
			assertEquals(24, event.getPlayers().size());
			assertEquals(5, event.getLocalizations().size());
			assertEquals(16, event.getTimeslots().size());
			assertEquals(2, event.getMatchesPerPlayer());
			assertEquals(3, event.getTimeslotsPerMatch());
			assertEquals(4, event.getPlayersPerMatch());
			
		} catch (IllegalArgumentException e) {
			fail("Exception thrown for an event expected to be valid");
		}
	}
	
	@Test
	public void constructorNullNameTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("name cannot be null");
		event = new Event(null, players, localizations, timeslots);
	}
	
	@Test
	public void extendedConstructorNullNameTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("name cannot be null");
		event = new Event(null, players, localizations, timeslots, nMatchesPerPlayer, nTimeslotsPerMatch, nPlayersPerMatch);
	}
	
	@Test
	public void constructorNullPlayersTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("players cannot be null");
		event = new Event("Event", null, localizations, timeslots);
	}
	
	@Test
	public void constructorEmptyPlayersTest() {
		players.clear();
		
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("cannot be less than 1");
		event = new Event("Event", players, localizations, timeslots);
	}
	
	@Test
	public void constructorContainedNullPlayerTest() {
		players.set(3, null);
		players.set(6, null);
		
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("player cannot be null");
		event = new Event("Event", players, localizations, timeslots);
	}
	
	@Test
	public void constructorDuplicatedPlayersTest() {
		players.set(4, players.get(2));
		
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("players must be unique");
		event = new Event("Event", players, localizations, timeslots);
	}
	
	/**
	 * Test que comprueba que se lanza excpeción al usar el primer constructor de evento ({@link Event#Event(String, List, List, List)},
	 * cuando el número de jugadores no es múltiplo del número por defecto de jugadores por partido
	 */
	@Test
	public void constructorDefaultPlayerPerMatchTest() {
		players.add(new Player("Extra Player"));
		
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("must be a multiple of the number of players per match");
		event = new Event("Event", players, localizations, timeslots);
	}
	
	@Test
	public void constructorNullLocalizationsTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("localizations cannot be null");
		event = new Event("Event", players, null, timeslots);
	}
	
	@Test
	public void constructorEmptyLocalizationsTest() {
		localizations.clear();
		
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("cannot be less than 1");
		event = new Event("Event", players, localizations, timeslots);
	}
	
	@Test
	public void constructorContainedNullLocalizationTest() {
		localizations.set(1, null);
		
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("localization cannot be null");
		event = new Event("Event", players, localizations, timeslots);
	}
	
	@Test
	public void constructorDuplicatedLocalizationsTest() {
		localizations.add(localizations.get(0));
		
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("localizations must be unique");
		event = new Event("Event", players, localizations, timeslots);
	}
	
	@Test
	public void constructorNullTimeslotsTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("timeslots cannot be null");
		event = new Event("Event", players, localizations, null);
	}
	
	@Test
	public void constructorEmptyTimeslotsTest() {
		timeslots.clear();
		
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("cannot be less than 1");
		event = new Event("Event", players, localizations, timeslots);
	}
	
	@Test
	public void constructorContainedNullTimeslotTest() {
		timeslots.set(7, null);
		
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("timeslot cannot be null");
		event = new Event("Event", players, localizations, timeslots);
	}
	
	@Test
	public void constructorNotEnoughTimeslotTest() {
		timeslots = TournamentUtils.buildDefiniteDayOfWeekTimeslots(1);
		
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("cannot be less than the product of the number of matches per player and the duration of a match");
		event = new Event("Event", players, localizations, timeslots);
	}
	
	@Test
	public void constructorDuplicatedTimeslotsTest() {
		timeslots.set(2, timeslots.get(7));
		timeslots.set(5, timeslots.get(7));
		
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("timeslots must be unique");
		event = new Event("Event", players, localizations, timeslots);
	}
	
	@Test
	public void constructorDisorderedTimeslotsTest() {
		Timeslot tmp = timeslots.get(3);
		timeslots.set(3, timeslots.get(4));
		timeslots.set(4, tmp);
		
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("must be greater than the next one");
		event = new Event("Event", players, localizations, timeslots);
	}
	
	@Test
	public void constructorPlayerPerMatchLessThanOneTest() {
		nPlayersPerMatch = 0;
		
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("cannot be less than 1");
		event = new Event("Event", players, localizations, timeslots, nMatchesPerPlayer, nTimeslotsPerMatch, nPlayersPerMatch);
	}
	
	@Test
	public void constructorPlayerPerMatchDivisorOfNumberOfPlayersTest() {
		nPlayersPerMatch = 3;
		
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("must be a multiple of the number of players per match");
		event = new Event("Event", players, localizations, timeslots, nMatchesPerPlayer, nTimeslotsPerMatch, nPlayersPerMatch);
	}
	
	@Test
	public void constructorMatchesPerPlayerLessThanOneTest() {
		nMatchesPerPlayer = 0;
		
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("cannot be less than 1");
		event = new Event("Event", players, localizations, timeslots, nMatchesPerPlayer, nTimeslotsPerMatch, nPlayersPerMatch);
	}
	
	@Test
	public void constructorTimeslotsPerMatchLessThanOneTest() {
		nTimeslotsPerMatch = 0;
		
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("cannot be less than 1");
		event = new Event("Event", players, localizations, timeslots, nMatchesPerPlayer, nTimeslotsPerMatch, nPlayersPerMatch);
	}
	
	@Test
	public void setNameTest() {
		event.setName("New Event");
		assertEquals("New Event", event.getName());
	}
	
	@Test
	public void setNullNameTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("Name cannot be null");
		event.setName(null);
	}
	
	@Test
	public void getPlayersTest() {
		expectedEx.expect(UnsupportedOperationException.class);
		event.getPlayers().add(new Player("New Player"));
	}
	
	@Test
	public void getLocalizationsTest() {
		expectedEx.expect(UnsupportedOperationException.class);
		event.getLocalizations().add(new Localization("Court 10"));
	}
	
	@Test
	public void getTimeslotsTest() {
		expectedEx.expect(UnsupportedOperationException.class);
		event.getTimeslots().remove(3);
	}
	
	@Test
	public void setMatchesPerPlayerTest() {
		event.setMatchesPerPlayer(3);
		assertEquals(3, event.getMatchesPerPlayer());
	}
	
	@Test
	public void setValidatorTest() {
		event.setValidator(new EventValidator());
		try {
			event.validate();
		} catch (ValidationException e) {
			fail("Unexpected exception for valid event");
		}
	}
	
	@Test
	public void setNullValidatorTest() {
		expectedEx.expect(IllegalArgumentException.class);
		event.setValidator(null);
	}
	
	@Test
	public void getMessagesTest() {
		try {
			event.validate();
			assertEquals(0, event.getMessages().size());
		} catch (ValidationException e) {
			fail("Unexpected exception for valid event");
		}
	}
	
	@Test
	public void setMatchesPerPlayerLessThanOneTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("cannot be less than 1");
		event.setMatchesPerPlayer(0);
	}
	
	@Test
	public void setMatchesPerPlayerMoreThanTimeslotsTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("cannot be less than the product of the number of matches per player and the duration of a match");
		event.setMatchesPerPlayer(5);
	}
	
	@Test
	public void setTimeslotsPerMatchTest() {
		event.setTimeslotsPerMatch(4);
		assertEquals(4, event.getTimeslotsPerMatch());
	}
	
	@Test
	public void setTimeslotsPerMatchLessThanOneTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("cannot be less than 1");
		event.setTimeslotsPerMatch(0);
	}
	
	@Test
	public void setTimeslotsPerMatchMoreThanTimeslotsTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("cannot be less than the product of the number of matches per player and the duration of a match");
		event.setTimeslotsPerMatch(9);
	}
	
	@Test
	public void setPlayersPerMatchTest() {
		event.setPlayersPerMatch(4);
		assertEquals(4, event.getPlayersPerMatch());
		assertFalse(event.hasTeams());
		assertFalse(event.hasFixedMatchups());
	}
	
	@Test
	public void setPlayersPerMatchLessThanOneTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("cannot be less than 1");
		event.setPlayersPerMatch(0);
	}
	
	@Test
	public void setPlayersPerMatchDivisorOfNumberOfPlayersTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("must be a multiple of the number of players per match");
		event.setPlayersPerMatch(3);
	}
	
	@Test
	public void setTeamsTest() {
		players = TournamentUtils.buildGenericPlayers(32, "Player");
		event = new Event("Doubles", players, localizations, timeslots, 1, 2, 4);
		
		assertFalse(event.hasTeams());
		
		List<Team> teams = new ArrayList<Team>(16);
		for (int i = 0; i < 32; i += 2) {
			Player[] teamPlayers = new Player[2];
			for (int j = 0; j < 2; j++)
				teamPlayers[j] = players.get(i + j);
			teams.add(new Team(teamPlayers));
		}
		
		event.setTeams(teams);
		
		assertTrue(event.hasTeams());
		assertEquals(16, event.getTeams().size());
		assertTrue(event.getTeams().get(8).contains(players.get(17)));
	}
	
	@Test
	public void setNullTeamsTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("Teams cannot be null");
		event.setTeams(null);
	}
	
	@Test
	public void setTeamsLessThanTwoTest() {
		event.setPlayersPerMatch(4);
		List<Team> teams = new ArrayList<Team>();
		teams.add(new Team(players.get(0), players.get(1)));
		
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("at least two teams");
		event.setTeams(teams);
	}
	
	@Test
	public void setTeamsNullTeamTest() {
		event.setPlayersPerMatch(4);
		List<Team> teams = new ArrayList<Team>();
		teams.add(new Team(players.get(0), players.get(1)));
		teams.add(null);
		
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("team cannot be null");
		event.setTeams(teams);
	}
	
	@Test
	public void setTeamsUnexistingPlayerTest() {
		Player unknownPlayer = new Player("Unknown Player");
		event.setPlayersPerMatch(4);
		List<Team> teams = new ArrayList<Team>();
		teams.add(new Team(players.get(0), players.get(1)));
		teams.add(new Team(players.get(2), unknownPlayer));
		
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("does not exist in the list of players of this event");
		event.setTeams(teams);
	}
	
	@Test
	public void setTeamsSameNumberOfPlayersTest() {
		event.setPlayersPerMatch(4);
		List<Team> teams = new ArrayList<Team>();
		teams.add(new Team(players.get(0), players.get(1)));
		teams.add(new Team(players.get(6), players.get(7), players.get(2)));
		
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("teams must have the same number of players");
		event.setTeams(teams);
	}
	
	@Test
	public void setTeamsDivisorNumberOfPlayersPerMatchTest() {
		event.setPlayersPerMatch(4);
		List<Team> teams = new ArrayList<Team>();
		teams.add(new Team(players.get(0), players.get(3), players.get(1)));
		teams.add(new Team(players.get(5), players.get(4), players.get(2)));
		
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("must be a divisor of the number of players per match");
		event.setTeams(teams);
	}
	
	@Test
	public void setTeamsDuplicatedPlayersTest() {
		event.setPlayersPerMatch(4);
		List<Team> teams = new ArrayList<Team>();
		teams.add(new Team(players.get(5), players.get(3)));
		teams.add(new Team(players.get(7), players.get(4)));
		teams.add(new Team(players.get(5), players.get(1)));
		
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("can only be present in one team");
		event.setTeams(teams);
	}
	
	@Test
	public void getTeamsTest() {
		List<Team> teams = new ArrayList<Team>();
		teams.add(new Team(players.get(5), players.get(3)));
		teams.add(new Team(players.get(7), players.get(4)));
		event.setTeams(teams);
		
		expectedEx.expect(UnsupportedOperationException.class);
		event.getTeams().remove(0);
	}
	
	@Test
	public void addTeamTest() {
		event.setPlayersPerMatch(4);
		event.addTeamPlayers(players.get(0), players.get(1));
		event.addTeamPlayers(players.get(2), players.get(3));
		event.addTeamPlayers(players.get(4), players.get(5));
		
		assertTrue(event.hasTeams());
		assertEquals(3, event.getTeams().size());
		assertTrue(event.getTeams().get(1).contains(players.get(2)));
		
		event.addTeam(new Team(players.get(6), players.get(7)));
		assertEquals(4, event.getTeams().size());
	}
	
	@Test
	public void addTeamPlayersNullPlayersTest() {
		Player[] teamPlayers = null;
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("Players cannot be null");
		event.addTeamPlayers(teamPlayers);
	}
	
	@Test
	public void addTeamNullPlayersTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("Team cannot be null");
		event.addTeam(null);
	}
	
	@Test
	public void addTeamUnexistingPlayerTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("does not exist in the list of players of this event");
		event.addTeam(new Team(players.get(3), new Player("Unknown Player")));
	}
	
	@Test
	public void addTeamDivisorOfNumberOfPlayersTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("must be a divisor of the number of players per match");
		event.addTeam(new Team(players.get(3), players.get(4), players.get(7)));
	}
	
	@Test
	public void addTeamSameNumberOfPlayersTest() {
		event.setPlayersPerMatch(4);
		event.addTeamPlayers(players.get(0), players.get(3));
		
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("must be the same");
		event.addTeamPlayers(players.get(2), players.get(4), players.get(6), players.get(7));
	}
	
	@Test
	public void addTeamPlayerOnlyInOneTeamTest() {
		event.setPlayersPerMatch(4);
		event.addTeamPlayers(players.get(0), players.get(3));
		
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("can only be present in one team");
		event.addTeamPlayers(players.get(2), players.get(3));
	}
	
	@Test
	public void removeTeamTest() {
		event.addTeamPlayers(players.get(0), players.get(1));
		event.addTeamPlayers(players.get(2), players.get(3));
		event.addTeamPlayers(players.get(4), players.get(5));
		event.addTeamPlayers(players.get(6), players.get(7));
		
		Team team = event.getTeams().get(3);
		
		event.removeTeam(team);
		
		assertFalse(event.getTeams().contains(team));
		assertEquals(3, event.getTeams().size());
	}
	
	@Test
	public void removeNonexistingTeamTest() {
		event.addTeamPlayers(players.get(0), players.get(1));
		
		Team team = new Team(players.get(3), players.get(6));
		
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("does not exist in the list of teams of this event");
		event.removeTeam(team);
	}
	
	@Test
	public void hasTeamsTest() {
		assertFalse(event.hasTeams());
		
		event.addTeamPlayers(players.get(0), players.get(1));
		
		assertTrue(event.hasTeams());
		
		event.removeTeam(event.getTeams().get(0));
		
		assertFalse(event.hasTeams());
	}
	
	@Test
	public void setUnavailablePlayersTest() {
		Map<Player, Set<Timeslot>> unavailability = new HashMap<Player, Set<Timeslot>>();
		unavailability.put(players.get(3), new HashSet<Timeslot>(Arrays.asList(timeslots.get(3), timeslots.get(4))));
		unavailability.put(players.get(5), new HashSet<Timeslot>(Arrays.asList(timeslots.get(7))));
		unavailability.put(players.get(0), new HashSet<Timeslot>(Arrays.asList(timeslots.get(4), timeslots.get(5), timeslots.get(6))));
		
		event.setUnavailablePlayers(unavailability);
		
		assertTrue(event.hasUnavailablePlayers());
		assertEquals(3, event.getUnavailablePlayers().size());
		assertTrue(event.getUnavailablePlayers().containsKey(players.get(0)));
		assertEquals(2, event.getUnavailablePlayers().get(players.get(3)).size());
		assertTrue(event.getUnavailablePlayers().get(players.get(5)).contains(timeslots.get(7)));
	}
	
	@Test
	public void setUnavailablePlayersNullTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("Map cannot be null");
		event.setUnavailablePlayers(null);
	}
	
	@Test
	public void setUnavailablePlayersNullPlayerTest() {
		Map<Player, Set<Timeslot>> unavailability = new HashMap<Player, Set<Timeslot>>();
		unavailability.put(null, new HashSet<Timeslot>());
		
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("player cannot be null");
		event.setUnavailablePlayers(unavailability);
	}
	
	@Test
	public void setUnavailablePlayersNonexistingPlayerTest() {
		Map<Player, Set<Timeslot>> unavailability = new HashMap<Player, Set<Timeslot>>();
		unavailability.put(new Player("Unknown Player"), new HashSet<Timeslot>());
		
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("does not exist in the list of players of this event");
		event.setUnavailablePlayers(unavailability);
	}
	
	@Test
	public void setUnavailablePlayersNullSetTest() {
		Map<Player, Set<Timeslot>> unavailability = new HashMap<Player, Set<Timeslot>>();
		unavailability.put(players.get(6), null);
		
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("set of unavailable timeslots for a player cannot be null");
		event.setUnavailablePlayers(unavailability);
	}
	
	@Test
	public void setUnavailablePlayersEmptySetTest() {
		Map<Player, Set<Timeslot>> unavailability = new HashMap<Player, Set<Timeslot>>();
		unavailability.put(players.get(6), new HashSet<Timeslot>());
		
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("set of unavailable timeslots for a player cannot be empty");
		event.setUnavailablePlayers(unavailability);
	}
	
	@Test
	public void setUnavailablePlayersNullTimeslotTest() {
		Map<Player, Set<Timeslot>> unavailability = new HashMap<Player, Set<Timeslot>>();
		unavailability.put(players.get(6), new HashSet<Timeslot>(Arrays.asList(timeslots.get(6), null)));
		
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("timeslot cannot be null");
		event.setUnavailablePlayers(unavailability);
	}
	
	@Test
	public void setUnavailablePlayersNonexistingTimeslotTest() {
		Map<Player, Set<Timeslot>> unavailability = new HashMap<Player, Set<Timeslot>>();
		unavailability.put(players.get(6), new HashSet<Timeslot>(Arrays.asList(timeslots.get(2), new AbstractTimeslot(1))));
		
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("does not exist in this event");
		event.setUnavailablePlayers(unavailability);
	}
	
	@Test
	public void getUnavailablePlayersTest() {
		expectedEx.expect(UnsupportedOperationException.class);
		event.getUnavailablePlayers().clear();
	}
	
	@Test
	public void addUnavailablePlayerTest() {
		assertFalse(event.hasUnavailablePlayers());
		
		event.addUnavailablePlayer(players.get(3), timeslots.get(5));
		
		assertTrue(event.hasUnavailablePlayers());
		assertEquals(1, event.getUnavailablePlayers().size());
		assertEquals(1, event.getUnavailablePlayers().get(players.get(3)).size());
		assertEquals(timeslots.get(5), event.getUnavailablePlayers().get(players.get(3)).iterator().next());
		
		event.addUnavailablePlayer(players.get(3), timeslots.get(6));
		assertEquals(1, event.getUnavailablePlayers().size());
		assertEquals(2, event.getUnavailablePlayers().get(players.get(3)).size());
		assertTrue(event.getUnavailablePlayers().get(players.get(3)).contains(timeslots.get(6)));
	}
	
	@Test
	public void addUnavailablePlayerNullPlayerTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("parameters cannot be null");
		event.addUnavailablePlayer(null, timeslots.get(5));
	}
	
	@Test
	public void addUnavailablePlayerNullTimeslotTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("parameters cannot be null");
		event.addUnavailablePlayer(players.get(4), null);
	}
	
	@Test
	public void addUnavailablePlayerNonexistingPlayerTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("does not exist in the list of players of the event");
		event.addUnavailablePlayer(new Player("Unknown Player"), timeslots.get(5));
	}
	
	@Test
	public void addUnavailablePlayerNonexistingTimeslotTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("doest not exist in the list of timeslots of the event");
		event.addUnavailablePlayer(players.get(3), new DefiniteTimeslot(LocalTime.of(10, 0), Duration.ofHours(2), 1));
	}
	
	@Test
	public void addUnavailablePlayerAlreadyExistingTimeslotTest() {
		event.addUnavailablePlayer(players.get(3), timeslots.get(5));
		
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("already exists in the set of unvailable timeslots for the player");
		event.addUnavailablePlayer(players.get(3), timeslots.get(5));
	}
	
	@Test
	public void addUnavailablePlayerAtTimeslotsTest() {
		assertFalse(event.hasUnavailablePlayers());
		
		event.addUnavailablePlayerAtTimeslots(players.get(6), new HashSet<Timeslot>(Arrays.asList(timeslots.get(3), timeslots.get(4))));
		
		assertTrue(event.hasUnavailablePlayers());
		assertEquals(1, event.getUnavailablePlayers().size());
		assertEquals(2, event.getUnavailablePlayers().get(players.get(6)).size());
		
		event.addUnavailablePlayerAtTimeslots(
			players.get(0),
			new HashSet<Timeslot>(Arrays.asList(timeslots.get(3), timeslots.get(4), timeslots.get(6), timeslots.get(7)))
		);
		assertEquals(2, event.getUnavailablePlayers().size());
		assertEquals(4, event.getUnavailablePlayers().get(players.get(0)).size());
		assertTrue(event.getUnavailablePlayers().get(players.get(0)).contains(timeslots.get(4)));
	}
	
	@Test
	public void addUnavailablePlayerAtTimeslotsNullPlayerTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("parameters cannot be null");
		event.addUnavailablePlayerAtTimeslots(null, new HashSet<Timeslot>(Arrays.asList(timeslots.get(3), timeslots.get(4))));
	}
	
	@Test
	public void addUnavailablePlayerAtTimeslotsNullSetTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("parameters cannot be null");
		event.addUnavailablePlayerAtTimeslots(players.get(2), null);
	}
	
	@Test
	public void removeUnavailablePlayerTest() {
		assertFalse(event.hasUnavailablePlayers());
		
		event.addUnavailablePlayer(players.get(3), timeslots.get(5));
		event.addUnavailablePlayer(players.get(3), timeslots.get(6));
		
		assertTrue(event.hasUnavailablePlayers());
		assertEquals(2, event.getUnavailablePlayers().get(players.get(3)).size());
		
		event.removePlayerUnavailableTimeslot(players.get(3), timeslots.get(5));
		
		assertEquals(1, event.getUnavailablePlayers().get(players.get(3)).size());
		
		event.removePlayerUnavailableTimeslot(players.get(3), timeslots.get(6));
		
		assertFalse(event.hasUnavailablePlayers());
	}
	
	@Test
	public void removeUnavailableNullPlayerTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("parameters cannot be null");
		event.removePlayerUnavailableTimeslot(null, timeslots.get(3));
	}
	
	@Test
	public void removeUnavailablePlayerNullTimeslotsTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("parameters cannot be null");
		event.removePlayerUnavailableTimeslot(players.get(2), null);
	}
	
	@Test
	public void removeUnavailableNonexistingPlayerTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("does not exist in the list of players of the event");
		event.removePlayerUnavailableTimeslot(new Player("Unknown Player"), timeslots.get(6));
	}
	
	@Test
	public void removeUnavailablePlayerNonexistingTimeslotTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("does not exist in the list of timeslots of the event");
		event.removePlayerUnavailableTimeslot(players.get(5), new AbstractTimeslot(3));
	}
	
	@Test
	public void hasUnavailablePlayersTest() {
		assertFalse(event.hasUnavailablePlayers());
		
		event.addUnavailablePlayer(players.get(3), timeslots.get(5));
		
		assertTrue(event.hasUnavailablePlayers());
		
		event.removePlayerUnavailableTimeslot(players.get(3), timeslots.get(5));
		
		assertFalse(event.hasUnavailablePlayers());
	}
	
	@Test
	public void setFixedMatchupsTest() {
		assertFalse(event.hasFixedMatchups());
		
		List<Set<Player>> fixedMatchups = new ArrayList<Set<Player>>();
		fixedMatchups.add(new HashSet<Player>(Arrays.asList(players.get(0), players.get(1))));
		fixedMatchups.add(new HashSet<Player>(Arrays.asList(players.get(4), players.get(7))));
		fixedMatchups.add(new HashSet<Player>(Arrays.asList(players.get(6), players.get(3))));
		
		event.setFixedMatchups(fixedMatchups);
		
		assertTrue(event.hasFixedMatchups());
		assertEquals(3, event.getFixedMatchups().size());
		assertTrue(event.getFixedMatchups().get(0).contains(players.get(1)));
		for (Set<Player> matchup : event.getFixedMatchups())
			assertEquals(2, matchup.size());
	}
	
	@Test
	public void setFixedMatchupsNullTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("fixed matchups cannot be null");
		event.setFixedMatchups(null);
	}
	
	@Test
	public void setFixedMatchupsDuplicatedTest() {
		List<Set<Player>> fixedMatchups = new ArrayList<Set<Player>>();
		fixedMatchups.add(new HashSet<Player>(Arrays.asList(players.get(0), players.get(1))));
		fixedMatchups.add(new HashSet<Player>(Arrays.asList(players.get(4), players.get(7))));
		fixedMatchups.add(new HashSet<Player>(Arrays.asList(players.get(0), players.get(1))));
		
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("matchup cannot be repeated");
		event.setFixedMatchups(fixedMatchups);
	}
	
	@Test
	public void setFixedMatchupsNullMatchupTest() {
		List<Set<Player>> fixedMatchups = new ArrayList<Set<Player>>();
		fixedMatchups.add(null);
		
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("matchup cannot be null");
		event.setFixedMatchups(fixedMatchups);
	}
	
	@Test
	public void setFixedMatchupsNullPlayerTest() {
		List<Set<Player>> fixedMatchups = new ArrayList<Set<Player>>();
		fixedMatchups.add(new HashSet<Player>(Arrays.asList(players.get(3), null)));
		
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("player cannot be null");
		event.setFixedMatchups(fixedMatchups);
	}
	
	@Test
	public void setFixedMatchupsNonexistingPlayerTest() {
		List<Set<Player>> fixedMatchups = new ArrayList<Set<Player>>();
		fixedMatchups.add(new HashSet<Player>(Arrays.asList(players.get(3), new Player("Unknown Player"))));
		
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("players must exist in the list of players of the event");
		event.setFixedMatchups(fixedMatchups);
	}
	
	@Test
	public void setFixedMatchupsNumberOfPlayersTest() {
		List<Set<Player>> fixedMatchups = new ArrayList<Set<Player>>();
		fixedMatchups.add(new HashSet<Player>(Arrays.asList(players.get(0), players.get(1))));
		fixedMatchups.add(new HashSet<Player>(Arrays.asList(players.get(4), players.get(7), players.get(5))));
		
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("number of players per match specified by this event");
		event.setFixedMatchups(fixedMatchups);
	}
	
	@Test
	public void setFixedMatchupsExceededMatchupsTest() {
		List<Set<Player>> fixedMatchups = new ArrayList<Set<Player>>();
		fixedMatchups.add(new HashSet<Player>(Arrays.asList(players.get(0), players.get(1))));
		fixedMatchups.add(new HashSet<Player>(Arrays.asList(players.get(0), players.get(2))));
		
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("cannot be present in more than the number of matches per player");
		event.setFixedMatchups(fixedMatchups);
	}
	
	@Test
	public void addFixedMatchupTest() {
		event.addFixedMatchup(new HashSet<Player>(Arrays.asList(players.get(2), players.get(6))));
		
		assertTrue(event.hasFixedMatchups());
		assertEquals(1, event.getFixedMatchups().size());
		
		event.addFixedPlayersMatchup(players.get(3), players.get(4));
		
		assertEquals(2, event.getFixedMatchups().size());
	}
	
	@Test
	public void addFixedMatchupNullPlayersTest() {
		Player[] matchupPlayers = null;
		
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("players cannot be null");
		event.addFixedPlayersMatchup(matchupPlayers);
	}
	
	@Test
	public void addFixedMatchupExistingMatchupTest() {
		event.addFixedMatchup(new HashSet<Player>(Arrays.asList(players.get(2), players.get(6))));
		
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("same matchup cannot be added more than once");
		event.addFixedPlayersMatchup(players.get(2), players.get(6));
	}
	
	@Test
	public void addFixedMatchupExceededMatchupsTest() {
		event.addFixedMatchup(new HashSet<Player>(Arrays.asList(players.get(2), players.get(6))));
		
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("cannot be present in more than the number of matches per player");
		event.addFixedMatchup(new HashSet<Player>(Arrays.asList(players.get(2), players.get(3))));
	}
	
	@Test
	public void addFixedTeamsMatchupTest() {
		event.setPlayersPerMatch(4);
		event.addTeamPlayers(players.get(3), players.get(5));
		event.addTeamPlayers(players.get(1), players.get(4));
		
		List<Team> teams = event.getTeams();
		event.addFixedTeamsMatchup(new HashSet<Team>(Arrays.asList(teams.get(0), teams.get(1))));
		
		assertTrue(event.hasFixedMatchups());
		assertEquals(1, event.getFixedMatchups().size());
		assertEquals(4, event.getFixedMatchups().get(0).size());
	}
	
	@Test
	public void addFixedTeamsMatchupNullTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("matchup cannot be null");
		event.addFixedTeamsMatchup(null);
	}
	
	@Test
	public void addFixedTeamsMatchupNonexistingTeamTest() {
		List<Team> teams = new ArrayList<Team>();
		teams.add(new Team(players.get(3), players.get(4)));
		teams.add(new Team(players.get(6), players.get(0)));
		
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("does not exist in the list of teams of this event");
		event.addFixedTeamsMatchup(new HashSet<Team>(Arrays.asList(teams.get(0), teams.get(1))));
	}
	
	@Test
	public void removeFixedMatchupTest() {
		event.addFixedMatchup(new HashSet<Player>(Arrays.asList(players.get(2), players.get(6))));
		event.addFixedMatchup(new HashSet<Player>(Arrays.asList(players.get(0), players.get(3))));
		
		assertTrue(event.hasFixedMatchups());
		
		event.removeFixedMatchup(event.getFixedMatchups().get(0));
		
		assertEquals(1, event.getFixedMatchups().size());
		
		event.removeFixedMatchup(event.getFixedMatchups().get(0));
		
		assertFalse(event.hasFixedMatchups());
	}
	
	@Test
	public void removeFixedTeamsMatchupTest() {
		event.setPlayersPerMatch(4);
		event.addTeamPlayers(players.get(3), players.get(5));
		event.addTeamPlayers(players.get(1), players.get(4));
		
		List<Team> teams = event.getTeams();
		event.addFixedTeamsMatchup(new HashSet<Team>(Arrays.asList(teams.get(0), teams.get(1))));
		
		assertTrue(event.hasFixedMatchups());
		
		event.removeFixedTeamsMatchup(new HashSet<Team>(Arrays.asList(event.getTeams().get(0), event.getTeams().get(1))));
		
		assertFalse(event.hasFixedMatchups());
	}
	
	@Test
	public void removeFixedTeamsMatchupNullTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("matchup cannot be null");
		event.removeFixedTeamsMatchup(null);
	}
	
	@Test
	public void hasFixedMatchupsTest() {
		assertFalse(event.hasFixedMatchups());
		
		event.addFixedPlayersMatchup(players.get(3), players.get(7));
		
		assertTrue(event.hasFixedMatchups());
	}
	
	@Test
	public void getFixedMatchupsTest() {
		event.addTeamPlayers(players.get(3), players.get(5));
		event.addTeamPlayers(players.get(1), players.get(4));
		
		expectedEx.expect(UnsupportedOperationException.class);
		event.getFixedMatchups().remove(1);
	}
	
	@Test
	public void setBreaksTest() {
		assertFalse(event.hasBreaks());
		
		List<Timeslot> breaks = new ArrayList<Timeslot>();
		breaks.add(timeslots.get(4));
		breaks.add(timeslots.get(5));
		
		event.setBreaks(breaks);
		
		assertTrue(event.hasBreaks());
		assertEquals(2, event.getBreaks().size());
	}
	
	@Test
	public void setBreaksNullTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("list of breaks cannot be null");
		event.setBreaks(null);
	}
	
	@Test
	public void setBreaksNullBreakTest() {
		List<Timeslot> breaks = new ArrayList<Timeslot>();
		breaks.add(timeslots.get(4));
		breaks.add(timeslots.get(5));
		breaks.add(null);
		
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("break cannot be null");
		event.setBreaks(breaks);
	}
	
	@Test
	public void setBreaksDuplicatedTest() {
		List<Timeslot> breaks = new ArrayList<Timeslot>();
		breaks.add(timeslots.get(0));
		breaks.add(timeslots.get(1));
		breaks.add(timeslots.get(1));
		
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("Break cannot be repeated");
		event.setBreaks(breaks);
	}
	
	@Test
	public void setBreaksNonexistingTimeslotTest() {
		List<Timeslot> breaks = new ArrayList<Timeslot>();
		breaks.add(timeslots.get(1));
		breaks.add(new AbstractTimeslot(3));
		
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("break timeslots must exist in the list of timeslots of this event");
		event.setBreaks(breaks);
	}
	
	@Test
	public void getBreaksTest() {
		List<Timeslot> breaks = new ArrayList<Timeslot>();
		breaks.add(timeslots.get(4));
		
		expectedEx.expect(UnsupportedOperationException.class);
		event.getBreaks().add(timeslots.get(5));
	}
	
	@Test
	public void addBreakTest() {
		assertFalse(event.hasBreaks());
		
		event.addBreak(timeslots.get(3));
		event.addBreak(timeslots.get(4));
		event.addBreak(timeslots.get(7));
		
		assertTrue(event.hasBreaks());
		assertEquals(3, event.getBreaks().size());
	}
	
	@Test
	public void addBreakNullTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("break cannot be null");
		event.addBreak(null);
	}
	
	@Test
	public void addBreakNonexistingTimeslotTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("does not exist in this event");
		event.addBreak(new DefiniteTimeslot(LocalTime.of(21, 0), Duration.ofHours(3), 6));
	}
	
	@Test
	public void addBreakExistingBreakTest() {
		event.addBreak(timeslots.get(2));
		event.addBreak(timeslots.get(3));
		
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("already exists in the list of breaks");
		event.addBreak(timeslots.get(3));
	}
	
	@Test
	public void removeBreakTest() {
		event.addBreak(timeslots.get(2));
		event.addBreak(timeslots.get(3));
		
		assertEquals(2, event.getBreaks().size());
		
		event.removeBreak(timeslots.get(3));
		
		assertEquals(1, event.getBreaks().size());
	}
	
	@Test
	public void removeBreakNullTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("break cannot be null");
		event.removeBreak(null);
	}
	
	@Test
	public void removeBreakNonexistingTimeslotTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("does not exist in this event");
		event.removeBreak(new DefiniteTimeslot(LocalTime.of(17, 15), Duration.ofMinutes(90), 6));
	}
	
	@Test
	public void isBreakTest() {
		assertFalse(event.isBreak(timeslots.get(4)));
		assertFalse(event.isBreak(timeslots.get(6)));
		
		event.addBreak(timeslots.get(4));
		
		assertTrue(event.isBreak(timeslots.get(4)));
		
		event.removeBreak(timeslots.get(4));
		
		assertFalse(event.isBreak(timeslots.get(4)));
	}
	
	@Test
	public void isBreakNullTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("timeslot cannot be null");
		event.isBreak(null);
	}
	
	@Test
	public void isBreakNonexistingTimeslotTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("does not exist in this event");
		event.isBreak(new AbstractTimeslot(2));
	}
	
	@Test
	public void hasBreaksTest() {
		assertFalse(event.hasBreaks());
		
		event.addBreak(timeslots.get(6));
		event.addBreak(timeslots.get(5));
		
		assertTrue(event.hasBreaks());

		event.removeBreak(timeslots.get(6));
		event.removeBreak(timeslots.get(5));
		
		assertFalse(event.hasBreaks());
	}
	
	@Test
	public void setUnavailableLocalizationsTest() {
		assertFalse(event.hasUnavailableLocalizations());
		
		Map<Localization, Set<Timeslot>> unavailableLocalizations = new HashMap<Localization, Set<Timeslot>>();
		unavailableLocalizations.put(
			localizations.get(0),
			new HashSet<Timeslot>(Arrays.asList(timeslots.get(5), timeslots.get(6)))
		);
		unavailableLocalizations.put(
			localizations.get(1),
			new HashSet<Timeslot>(Arrays.asList(timeslots.get(0), timeslots.get(1), timeslots.get(2), timeslots.get(3)))
		);
		
		event.setUnavailableLocalizations(unavailableLocalizations);
		
		assertTrue(event.hasUnavailableLocalizations());
		assertEquals(2, event.getUnavailableLocalizations().size());
		assertEquals(4, event.getUnavailableLocalizations().get(localizations.get(1)).size());
		assertTrue(event.getUnavailableLocalizations().get(localizations.get(0)).contains(timeslots.get(5)));
	}
	
	@Test
	public void setUnavailableLocalizationsNullTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("unavailable localizations cannot be null");
		event.setUnavailableLocalizations(null);
	}
	
	@Test
	public void setUnavailableLocalizationsNullLocalizationTest() {
		Map<Localization, Set<Timeslot>> unavailableLocalizations = new HashMap<Localization, Set<Timeslot>>();
		unavailableLocalizations.put(
			localizations.get(0),
			new HashSet<Timeslot>(Arrays.asList(timeslots.get(5), timeslots.get(6)))
		);
		unavailableLocalizations.put(
			null,
			new HashSet<Timeslot>(Arrays.asList(timeslots.get(0)))
		);
		
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("localization cannot be null");
		event.setUnavailableLocalizations(unavailableLocalizations);
	}
	
	@Test
	public void setUnavailableLocalizationsNonexistingLocalizationTest() {
		Map<Localization, Set<Timeslot>> unavailableLocalizations = new HashMap<Localization, Set<Timeslot>>();
		unavailableLocalizations.put(
			localizations.get(0),
			new HashSet<Timeslot>(Arrays.asList(timeslots.get(5), timeslots.get(6)))
		);
		unavailableLocalizations.put(
			new Localization("Unknown Localization"),
			new HashSet<Timeslot>(Arrays.asList(timeslots.get(0)))
		);
		
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("does not exist in this event");
		event.setUnavailableLocalizations(unavailableLocalizations);
	}
	
	@Test
	public void setUnavailableLocalizationsNullTimeslotsTest() {
		Map<Localization, Set<Timeslot>> unavailableLocalizations = new HashMap<Localization, Set<Timeslot>>();
		unavailableLocalizations.put(localizations.get(0), null);
		
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("unavailable timeslots set for the localization");
		event.setUnavailableLocalizations(unavailableLocalizations);
	}
	
	@Test
	public void setUnavailableLocalizationsNonexistingTimeslotTest() {
		Map<Localization, Set<Timeslot>> unavailableLocalizations = new HashMap<Localization, Set<Timeslot>>();
		unavailableLocalizations.put(
			localizations.get(1),
			new HashSet<Timeslot>(Arrays.asList(timeslots.get(5), new AbstractTimeslot(4)))
		);
		
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("does not exist in this event");
		event.setUnavailableLocalizations(unavailableLocalizations);
	}
	
	@Test
	public void getUnavailableLocalizationsTest() {
		expectedEx.expect(UnsupportedOperationException.class);
		event.getUnavailableLocalizations().put(localizations.get(1), new HashSet<Timeslot>(Arrays.asList(timeslots.get(4))));
	}
	
	@Test
	public void addUnavailableLocalizationTest() {
		assertFalse(event.hasUnavailableLocalizations());
		
		event.addUnavailableLocalization(localizations.get(1), timeslots.get(3));
		event.addUnavailableLocalization(localizations.get(1), timeslots.get(4));
		event.addUnavailableLocalization(localizations.get(1), timeslots.get(5));
		event.addUnavailableLocalization(localizations.get(1), timeslots.get(6));
		
		assertTrue(event.hasUnavailableLocalizations());
		assertEquals(1, event.getUnavailableLocalizations().size());
		assertEquals(4, event.getUnavailableLocalizations().get(localizations.get(1)).size());
	}
	
	@Test
	public void addUnavailableLocalizationNullLocalizationTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("parameters cannot be null");
		event.addUnavailableLocalization(null, timeslots.get(2));
	}
	
	@Test
	public void addUnavailableLocalizationNullTimeslotTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("parameters cannot be null");
		event.addUnavailableLocalization(localizations.get(0), null);
	}
	
	@Test
	public void addUnavailableLocalizationNonexistingLocalizationTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("does not exist in this event");
		event.addUnavailableLocalization(new Localization("Unknown Localization"), timeslots.get(2));
	}
	
	@Test
	public void addUnavailableLocalizationNonexistingTimeslotTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("does not exist in this event");
		event.addUnavailableLocalization(localizations.get(0), new AbstractTimeslot(6));
	}
	
	@Test
	public void addUnavailableLocalizationAlreadyExistingTest() {
		event.addUnavailableLocalization(localizations.get(0), timeslots.get(2));
		event.addUnavailableLocalization(localizations.get(0), timeslots.get(3));
		event.addUnavailableLocalization(localizations.get(0), timeslots.get(4));
		
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("already exists in the set un unavailable timeslots for the localization");
		event.addUnavailableLocalization(localizations.get(0), timeslots.get(4));
	}
	
	@Test
	public void addUnavailableLocalizationAtTimeslotsTest() {
		assertFalse(event.hasUnavailableLocalizations());
		
		event.addUnavailableLocalizationAtTimeslots(
			localizations.get(1),
			new HashSet<>(Arrays.asList(timeslots.get(4), timeslots.get(5), timeslots.get(6)))
		);
		event.addUnavailableLocalizationAtTimeslots(localizations.get(0), new HashSet<>(Arrays.asList(timeslots.get(6))));
		
		assertTrue(event.hasUnavailableLocalizations());
		assertEquals(2, event.getUnavailableLocalizations().size());
		assertEquals(3, event.getUnavailableLocalizations().get(localizations.get(1)).size());
	}
	
	@Test
	public void addUnavailableLocalizationAtTimeslotsNullLocalizationTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("parameters cannot be null");
		event.addUnavailableLocalizationAtTimeslots(null, new HashSet<>(Arrays.asList(timeslots.get(4), timeslots.get(5), timeslots.get(6))));
	}
	
	@Test
	public void addUnavailableLocalizationAtTimeslotsNullTimeslotsTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("parameters cannot be null");
		event.addUnavailableLocalizationAtTimeslots(localizations.get(1), null);
	}
	
	@Test
	public void removeUnavailableLocalizationTest() {
		event.addUnavailableLocalization(localizations.get(0), timeslots.get(2));
		event.addUnavailableLocalization(localizations.get(0), timeslots.get(3));
		event.addUnavailableLocalization(localizations.get(0), timeslots.get(4));
		
		assertEquals(3, event.getUnavailableLocalizations().get(localizations.get(0)).size());
		
		event.removeUnavailableLocalization(localizations.get(0));
		
		assertNull(event.getUnavailableLocalizations().get(localizations.get(0)));
	}
	
	@Test
	public void removeUnavailableLocalizationNullLocalizationTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("localization cannot be null");
		event.removeUnavailableLocalization(null);
	}
	
	@Test
	public void removeUnavailableLocalizationNonexistingLocalizationTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("does not exist in this event");
		event.removeUnavailableLocalization(new Localization("Unknown Localization"));
	}
	
	@Test
	public void removeUnavailableLocalizationTimeslotTest() {
		event.addUnavailableLocalization(localizations.get(1), timeslots.get(0));
		event.addUnavailableLocalization(localizations.get(1), timeslots.get(1));
		event.addUnavailableLocalization(localizations.get(1), timeslots.get(2));
		event.addUnavailableLocalization(localizations.get(1), timeslots.get(3));
		
		assertEquals(4, event.getUnavailableLocalizations().get(localizations.get(1)).size());
		
		event.removeUnavailableLocalizationTimeslot(localizations.get(0), timeslots.get(3));
		
		assertEquals(4, event.getUnavailableLocalizations().get(localizations.get(1)).size());
		
		event.removeUnavailableLocalizationTimeslot(localizations.get(1), timeslots.get(3));
		
		assertEquals(3, event.getUnavailableLocalizations().get(localizations.get(1)).size());
		
		event.removeUnavailableLocalizationTimeslot(localizations.get(1), timeslots.get(2));
		event.removeUnavailableLocalizationTimeslot(localizations.get(1), timeslots.get(0));
		event.removeUnavailableLocalizationTimeslot(localizations.get(1), timeslots.get(1));
		
		assertNull(event.getUnavailableLocalizations().get(localizations.get(1)));
		assertFalse(event.hasUnavailableLocalizations());
	}
	
	@Test
	public void removeUnavailableLocalizationTimeslotNullLocalizationTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("parameters cannot be null");
		event.removeUnavailableLocalizationTimeslot(null, timeslots.get(3));
	}
	
	@Test
	public void removeUnavailableLocalizationTimeslotNullTimeslotTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("parameters cannot be null");
		event.removeUnavailableLocalizationTimeslot(localizations.get(1), null);
	}
	
	@Test
	public void removeUnavailableLocalizationTimeslotNonexistingLocalizationTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("does not exist in this event");
		event.removeUnavailableLocalizationTimeslot(new Localization("Unknown Localization"), timeslots.get(3));
	}
	
	@Test
	public void removeUnavailableLocalizationTimeslotNonexistingTimeslotTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("does not exist in this event");
		event.removeUnavailableLocalizationTimeslot(localizations.get(1), new AbstractTimeslot(0));
	}
	
	@Test
	public void hasUnavailableLocalizationsTest() {
		assertFalse(event.hasUnavailableLocalizations());
		
		event.addUnavailableLocalization(localizations.get(1), timeslots.get(1));
		event.addUnavailableLocalization(localizations.get(1), timeslots.get(2));
		event.addUnavailableLocalization(localizations.get(1), timeslots.get(3));
		
		assertTrue(event.hasUnavailableLocalizations());
	}
	
	@Test
	public void setPlayersInLocalizationsTest() {
		Map<Player, Set<Localization>> playersInLocalizations = new HashMap<Player, Set<Localization>>();
		playersInLocalizations.put(players.get(4), new HashSet<Localization>(Arrays.asList(localizations.get(0))));
		playersInLocalizations.put(
			players.get(1),
			new HashSet<Localization>(Arrays.asList(localizations.get(1), localizations.get(0)))
		);
		
		event.setPlayersInLocalizations(playersInLocalizations);
		
		assertTrue(event.hasPlayersInLocalizations());
		assertEquals(2, event.getPlayersInLocalizations().size());
		assertEquals(2, event.getPlayersInLocalizations().get(players.get(1)).size());
	}
	
	@Test
	public void setPlayersInLocalizationsNullTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("parameter cannot be null");
		event.setPlayersInLocalizations(null);
	}
	
	@Test
	public void setPlayersInLocalizationsNullPlayerTest() {
		Map<Player, Set<Localization>> playersInLocalizations = new HashMap<Player, Set<Localization>>();
		playersInLocalizations.put(null, new HashSet<Localization>(Arrays.asList(localizations.get(0))));
		playersInLocalizations.put(
			players.get(1),
			new HashSet<Localization>(Arrays.asList(localizations.get(1), localizations.get(0)))
		);
		
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("player cannot be null");
		event.setPlayersInLocalizations(playersInLocalizations);
	}
	
	@Test
	public void setPlayersInLocalizationsNonexistingPlayerTest() {
		Map<Player, Set<Localization>> playersInLocalizations = new HashMap<Player, Set<Localization>>();
		playersInLocalizations.put(new Player("Unknown Player"), new HashSet<Localization>(Arrays.asList(localizations.get(0))));
		playersInLocalizations.put(
			players.get(1),
			new HashSet<Localization>(Arrays.asList(localizations.get(1), localizations.get(0)))
		);
		
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("does not exist in the list of players of this event");
		event.setPlayersInLocalizations(playersInLocalizations);
	}
	
	@Test
	public void setPlayersInLocalizationsNullLocalizationsTest() {
		Map<Player, Set<Localization>> playersInLocalizations = new HashMap<Player, Set<Localization>>();
		playersInLocalizations.put(players.get(1), null);
		
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("localizations assigned to the player cannot be null");
		event.setPlayersInLocalizations(playersInLocalizations);
	}
	
	@Test
	public void setPlayersInLocalizationsNonexistingLocalizationTest() {
		Map<Player, Set<Localization>> playersInLocalizations = new HashMap<Player, Set<Localization>>();
		playersInLocalizations.put(players.get(1), new HashSet<Localization>(Arrays.asList(new Localization("Unknown Localization"))));
		
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("does not exist in this event");
		event.setPlayersInLocalizations(playersInLocalizations);
	}
	
	@Test
	public void getPlayersInLocalizationsTest() {
		expectedEx.expect(UnsupportedOperationException.class);
		event.getPlayersInLocalizations().put(players.get(1), new HashSet<Localization>(Arrays.asList(localizations.get(1))));
	}
	
	@Test
	public void addPlayerInLocalizationTest() {
		event.addPlayerInLocalization(players.get(6), localizations.get(0));
		event.addPlayerInLocalization(players.get(7), localizations.get(0));
		event.addPlayerInLocalization(players.get(6), localizations.get(1));
		
		assertEquals(2, event.getPlayersInLocalizations().size());
		assertTrue(event.getPlayersInLocalizations().get(players.get(7)).contains(localizations.get(0)));
	}
	
	@Test
	public void addPlayerInLocalizationNullPlayerTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("cannot be null");
		event.addPlayerInLocalization(null, localizations.get(1));
	}
	
	@Test
	public void addPlayerInLocalizationNullLocalizationTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("cannot be null");
		event.addPlayerInLocalization(players.get(7), null);
	}
	
	@Test
	public void addPlayerInLocalizationNonexistingPlayerTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("does not exist in the list of players of this event");
		event.addPlayerInLocalization(new Player("Unknown Player"), localizations.get(1));
	}
	
	@Test
	public void addPlayerInLocalizationNonexistingLocalizationTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("does not exist");
		event.addPlayerInLocalization(players.get(2), new Localization("Unknown Localization"));
	}
	
	@Test
	public void addPlayerInLocalizationAlreadyExistingTest() {
		event.addPlayerInLocalization(players.get(3), localizations.get(1));
		event.addPlayerInLocalization(players.get(4), localizations.get(1));
		
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("already assigned to that player");
		event.addPlayerInLocalization(players.get(3), localizations.get(1));
	}
	
	@Test
	public void removePlayerInLocalizationTest() {
		event.addPlayerInLocalization(players.get(3), localizations.get(1));
		event.addPlayerInLocalization(players.get(3), localizations.get(0));
		
		assertEquals(2, event.getPlayersInLocalizations().get(players.get(3)).size());
		
		event.removePlayerInLocalization(players.get(3), localizations.get(0));
		
		assertEquals(1, event.getPlayersInLocalizations().get(players.get(3)).size());
		
		event.removePlayerInLocalization(players.get(3), localizations.get(1));
		
		assertNull(event.getPlayersInLocalizations().get(players.get(3)));
	}
	
	@Test
	public void removePlayerInLocalizationNullPlayerTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("cannot be null");
		event.removePlayerInLocalization(null, localizations.get(1));
	}
	
	@Test
	public void removePlayerInLocalizationNullLocalizationTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("cannot be null");
		event.removePlayerInLocalization(players.get(5), null);
	}
	
	@Test
	public void removePlayerInLocalizationNonexistingPlayerTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("does not exist in the list of players of this event");
		event.removePlayerInLocalization(new Player("Unknown Player"), localizations.get(1));
	}
	
	@Test
	public void removePlayerInLocalizationNonexistingLocalizationTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("does not exist in this event");
		event.removePlayerInLocalization(players.get(5), new Localization("Unknown localization"));
	}
	
	@Test
	public void hasPlayersInLocalizationsTest() {
		assertFalse(event.hasPlayersInLocalizations());
		
		event.addPlayerInLocalization(players.get(3), localizations.get(1));
		
		assertTrue(event.hasPlayersInLocalizations());
		
		event.removePlayerInLocalization(players.get(3), localizations.get(1));
		
		assertFalse(event.hasPlayersInLocalizations());
	}
	
	@Test
	public void setPlayersAtTimeslotsTest() {
		assertFalse(event.hasPlayersAtTimeslots());
		
		Map<Player, Set<Timeslot>> playersAtTimeslots = new HashMap<Player, Set<Timeslot>>();
		playersAtTimeslots.put(players.get(7), new HashSet<Timeslot>(Arrays.asList(timeslots.get(0), timeslots.get(1), timeslots.get(2))));
		playersAtTimeslots.put(players.get(4), new HashSet<Timeslot>(Arrays.asList(timeslots.get(3))));
		playersAtTimeslots.put(players.get(3), new HashSet<Timeslot>(Arrays.asList(timeslots.get(3))));
		playersAtTimeslots.put(players.get(0), new HashSet<Timeslot>(Arrays.asList(timeslots.get(5), timeslots.get(7))));
		
		event.setPlayersAtTimeslots(playersAtTimeslots);
		
		assertTrue(event.hasPlayersAtTimeslots());
		assertEquals(4, event.getPlayersAtTimeslots().size());
	}
	
	@Test
	public void setPlayersAtTimeslotsNullTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("parameter cannot be null");
		event.setPlayersAtTimeslots(null);
	}
	
	@Test
	public void setPlayersAtTimeslotsNullPlayerTest() {
		assertFalse(event.hasPlayersAtTimeslots());
		
		Map<Player, Set<Timeslot>> playersAtTimeslots = new HashMap<Player, Set<Timeslot>>();
		playersAtTimeslots.put(players.get(4), new HashSet<Timeslot>(Arrays.asList(timeslots.get(3))));
		playersAtTimeslots.put(null, new HashSet<Timeslot>(Arrays.asList(timeslots.get(3))));
		playersAtTimeslots.put(players.get(0), new HashSet<Timeslot>(Arrays.asList(timeslots.get(5), timeslots.get(7))));
		
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("player cannot be null");
		event.setPlayersAtTimeslots(playersAtTimeslots);
	}
	
	@Test
	public void setPlayersAtTimeslotsNonexistingPlayerTest() {
		assertFalse(event.hasPlayersAtTimeslots());
		
		Map<Player, Set<Timeslot>> playersAtTimeslots = new HashMap<Player, Set<Timeslot>>();
		playersAtTimeslots.put(players.get(4), new HashSet<Timeslot>(Arrays.asList(timeslots.get(3))));
		playersAtTimeslots.put(new Player("Unknown Player"), new HashSet<Timeslot>(Arrays.asList(timeslots.get(3))));
		playersAtTimeslots.put(players.get(0), new HashSet<Timeslot>(Arrays.asList(timeslots.get(5), timeslots.get(7))));
		
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("does not exist in the list of players of this event");
		event.setPlayersAtTimeslots(playersAtTimeslots);
	}
	
	@Test
	public void setPlayersAtTimeslotsNullTimeslotsTest() {
		assertFalse(event.hasPlayersAtTimeslots());
		
		Map<Player, Set<Timeslot>> playersAtTimeslots = new HashMap<Player, Set<Timeslot>>();
		playersAtTimeslots.put(players.get(4), new HashSet<Timeslot>(Arrays.asList(timeslots.get(3))));
		playersAtTimeslots.put(players.get(0), null);
		
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("timeslots assigned to the player cannot be null");
		event.setPlayersAtTimeslots(playersAtTimeslots);
	}
	
	@Test
	public void setPlayersAtTimeslotsNonexistingTimeslotTest() {
		assertFalse(event.hasPlayersAtTimeslots());
		
		Map<Player, Set<Timeslot>> playersAtTimeslots = new HashMap<Player, Set<Timeslot>>();
		playersAtTimeslots.put(players.get(4), new HashSet<Timeslot>(Arrays.asList(timeslots.get(3))));
		playersAtTimeslots.put(players.get(0), new HashSet<Timeslot>(Arrays.asList(timeslots.get(3), new AbstractTimeslot(3))));
		
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("does not exist in this event");
		event.setPlayersAtTimeslots(playersAtTimeslots);
	}
	
	@Test
	public void getPlayersAtTimeslotsTest() {
		expectedEx.expect(UnsupportedOperationException.class);
		event.getPlayersAtTimeslots().put(players.get(4), new HashSet<Timeslot>(Arrays.asList(timeslots.get(3))));
	}
	
	@Test
	public void addPlayerAtTimeslotTest() {
		event.addPlayerAtTimeslot(players.get(5), timeslots.get(4));
		event.addPlayerAtTimeslot(players.get(5), timeslots.get(5));
		event.addPlayerAtTimeslot(players.get(5), timeslots.get(6));
		event.addPlayerAtTimeslot(players.get(5), timeslots.get(7));
		
		assertTrue(event.hasPlayersAtTimeslots());
		assertEquals(1, event.getPlayersAtTimeslots().size());
		assertEquals(4, event.getPlayersAtTimeslots().get(players.get(5)).size());
	}
	
	@Test
	public void addPlayerAtTimeslotNullPlayerTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("Player cannot be null");
		event.addPlayerAtTimeslot(null, timeslots.get(5));
	}
	
	@Test
	public void addPlayerAtTimeslotNullTimeslotTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("Timeslot cannot be null");
		event.addPlayerAtTimeslot(players.get(4), null);
	}
	
	@Test
	public void addPlayerAtTimeslotNonexistingPlayerTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("does not exist in the list of players of this event");
		event.addPlayerAtTimeslot(new Player("Unknown Player"), timeslots.get(5));
	}
	
	@Test
	public void addPlayerAtTimeslotNonexistingTimeslotTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("does not exist in this event");
		event.addPlayerAtTimeslot(players.get(4), new AbstractTimeslot(4));
	}
	
	@Test
	public void addPlayerAtTimeslotAlreadyExistingTest() {
		event.addPlayerAtTimeslot(players.get(4), timeslots.get(7));
		
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("already assigned to the player");
		event.addPlayerAtTimeslot(players.get(4), timeslots.get(7));
	}
	
	@Test
	public void addPlayerAtTimeslotsTest() {
		event.addPlayerAtTimeslots(
			players.get(4),
			new HashSet<Timeslot>(Arrays.asList(timeslots.get(3), timeslots.get(4), timeslots.get(5), timeslots.get(6)))
		);
		
		assertEquals(4, event.getPlayersAtTimeslots().get(players.get(4)).size());
	}
	
	@Test
	public void addPlayerAtTimeslotsNullTimeslotsTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("Timeslots cannot be null");
		event.addPlayerAtTimeslots(players.get(4), null);
	}
	
	@Test
	public void addPlayersAtTimeslotsTest() {
		event.addPlayersAtTimeslots(
			new HashSet<Player>(Arrays.asList(players.get(4), players.get(7))),
			new HashSet<Timeslot>(Arrays.asList(timeslots.get(0), timeslots.get(1), timeslots.get(5), timeslots.get(6)))
		);
			
		assertEquals(2, event.getPlayersAtTimeslots().size());
		assertEquals(4, event.getPlayersAtTimeslots().get(players.get(4)).size());
	}

	@Test
	public void addPlayersAtTimeslotsTestNullPlayersTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("parameters cannot be null");
		event.addPlayersAtTimeslots(
			null,
			new HashSet<Timeslot>(Arrays.asList(timeslots.get(0), timeslots.get(1), timeslots.get(5), timeslots.get(6)))
		);
	}
	
	@Test
	public void addPlayersAtTimeslotsTestNullTimeslotsTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("parameters cannot be null");
		event.addPlayersAtTimeslots(new HashSet<Player>(Arrays.asList(players.get(4), players.get(7))), null);
	}
	
	@Test
	public void removePlayerAtTimeslotTest() {
		event.addPlayersAtTimeslots(
			new HashSet<Player>(Arrays.asList(players.get(4), players.get(7))),
			new HashSet<Timeslot>(Arrays.asList(timeslots.get(0), timeslots.get(1), timeslots.get(5), timeslots.get(6)))
		);
		
		assertEquals(4, event.getPlayersAtTimeslots().get(players.get(4)).size());
		
		event.removePlayerAtTimeslot(players.get(4), timeslots.get(0));
		event.removePlayerAtTimeslot(players.get(4), timeslots.get(1));
		
		assertEquals(2, event.getPlayersAtTimeslots().get(players.get(4)).size());
		
		event.removePlayerAtTimeslot(players.get(4), timeslots.get(6));
		event.removePlayerAtTimeslot(players.get(4), timeslots.get(5));
		
		assertNull(event.getPlayersAtTimeslots().get(players.get(4)));
	}
	
	@Test
	public void removePlayerAtTimeslotTestNullPlayer() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("parameters cannot be null");
		event.removePlayerAtTimeslot(null, timeslots.get(0));
	}
	
	@Test
	public void removePlayerAtTimeslotTestNullTimeslots() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("parameters cannot be null");
		event.removePlayerAtTimeslot(players.get(3), null);
	}
	
	@Test
	public void removePlayerAtTimeslotTestNonexistingPlayer() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("does not exist in the list of players of this event");
		event.removePlayerAtTimeslot(new Player("Unknown Player"), timeslots.get(0));
	}
	
	@Test
	public void removePlayerAtTimeslotTestNonexistingTimeslots() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("does not exist in the list of timeslots of this event");
		event.removePlayerAtTimeslot(players.get(3), new AbstractTimeslot(0));
	}
	
	@Test
	public void removePlayerAtTimeslotsTest() {
		event.addPlayersAtTimeslots(
			new HashSet<Player>(Arrays.asList(players.get(4), players.get(7))),
			new HashSet<Timeslot>(Arrays.asList(timeslots.get(0), timeslots.get(1), timeslots.get(5), timeslots.get(6)))
		);
			
		assertEquals(4, event.getPlayersAtTimeslots().get(players.get(4)).size());
		
		event.removePlayerAtTimeslots(players.get(4), new HashSet<Timeslot>(Arrays.asList(timeslots.get(0), timeslots.get(1))));
		
		assertEquals(2, event.getPlayersAtTimeslots().get(players.get(4)).size());
		
		event.removePlayerAtTimeslots(players.get(4), new HashSet<Timeslot>(Arrays.asList(timeslots.get(6), timeslots.get(5))));
		
		assertNull(event.getPlayersAtTimeslots().get(players.get(4)));
	}
	
	@Test
	public void removePlayerAtTimeslotsNullTimeslotsTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("Timeslots cannot be null");
		event.removePlayerAtTimeslots(players.get(4), null);
	}
	
	@Test
	public void hasPlayersAtTimeslotsTest() {
		assertFalse(event.hasPlayersAtTimeslots());
		
		event.addPlayersAtTimeslots(
			new HashSet<Player>(Arrays.asList(players.get(4), players.get(7))),
			new HashSet<Timeslot>(Arrays.asList(timeslots.get(0), timeslots.get(1), timeslots.get(5), timeslots.get(6)))
		);
		
		assertTrue(event.hasPlayersAtTimeslots());
	}
	
	@Test
	public void matchupModeTest() {
		event.setMatchupMode(MatchupMode.ALL_DIFFERENT);
		
		assertEquals(MatchupMode.ANY, event.getMatchupMode());
		
		event.setMatchesPerPlayer(2);
		event.setMatchupMode(MatchupMode.ALL_DIFFERENT);
		
		assertEquals(MatchupMode.ALL_DIFFERENT, event.getMatchupMode());
		
		event.setMatchesPerPlayer(1);
		event.setMatchesPerPlayer(2);
		event.setMatchupMode(MatchupMode.ALL_EQUAL);
		
		assertEquals(MatchupMode.ALL_EQUAL, event.getMatchupMode());
		
		event.setPlayersPerMatch(1);
		event.setMatchupMode(MatchupMode.ALL_DIFFERENT);
		
		assertEquals(MatchupMode.ANY, event.getMatchupMode());
	}
	
	@Test
	public void setMatchupModeNullTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("Matchup mode cannot be null");
		event.setMatchupMode(null);
	}
	
	@Test
	public void getTeamByPlayerTest() {
		event.setPlayersPerMatch(4);
		event.addTeamPlayers(players.get(0), players.get(1));
		event.addTeamPlayers(players.get(2), players.get(3));
		
		assertEquals(event.getTeams().get(0), event.getTeamByPlayer(players.get(1)));
		assertEquals(event.getTeams().get(0), event.getTeamByPlayer(players.get(0)));
		assertEquals(event.getTeams().get(1), event.getTeamByPlayer(players.get(3)));
		
		assertEquals(null, event.getTeamByPlayer(players.get(4)));
		assertEquals(null, event.getTeamByPlayer(players.get(7)));
	}
	
	@Test
	public void isPlayerUnavailableTest() {
		event.addUnavailablePlayer(players.get(3), timeslots.get(0));
		event.addUnavailablePlayer(players.get(3), timeslots.get(1));
		event.addUnavailablePlayer(players.get(3), timeslots.get(2));
		event.addUnavailablePlayer(players.get(7), timeslots.get(7));
		
		assertTrue(event.isPlayerUnavailable(players.get(3), timeslots.get(1)));
		assertTrue(event.isPlayerUnavailable(players.get(3), timeslots.get(2)));
		assertTrue(event.isPlayerUnavailable(players.get(7), timeslots.get(7)));
		assertFalse(event.isPlayerUnavailable(players.get(4), timeslots.get(1)));
		assertFalse(event.isPlayerUnavailable(players.get(6), timeslots.get(5)));
	}
	
	@Test
	public void isLocalizationUnavailableTest() {
		event.addUnavailableLocalization(localizations.get(0), timeslots.get(0));
		event.addUnavailableLocalization(localizations.get(0), timeslots.get(1));
		event.addUnavailableLocalization(localizations.get(0), timeslots.get(2));
		
		assertTrue(event.isLocalizationUnavailable(localizations.get(0), timeslots.get(0)));
		assertTrue(event.isLocalizationUnavailable(localizations.get(0), timeslots.get(1)));
		assertTrue(event.isLocalizationUnavailable(localizations.get(0), timeslots.get(2)));
		assertFalse(event.isLocalizationUnavailable(localizations.get(0), timeslots.get(3)));
		assertFalse(event.isLocalizationUnavailable(localizations.get(0), timeslots.get(4)));
		assertFalse(event.isLocalizationUnavailable(localizations.get(1), timeslots.get(0)));
		assertFalse(event.isLocalizationUnavailable(localizations.get(1), timeslots.get(1)));
	}
	
	@Test
	public void getNumberOfMatchesTest() {
		assertEquals(event.getNumberOfMatches() * event.getTimeslotsPerMatch() / event.getPlayersPerMatch(), event.getNumberOfMatches());
	}
	
	@Test
	public void toStringTest() {
		assertEquals("Event", event.toString());
		assertNotEquals("", event.toString());
	}
}
