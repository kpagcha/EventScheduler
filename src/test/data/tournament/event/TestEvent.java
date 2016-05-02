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
}
