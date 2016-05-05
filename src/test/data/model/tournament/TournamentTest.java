package data.model.tournament;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import data.model.tournament.event.Event;
import data.model.tournament.event.entity.Localization;
import data.model.tournament.event.entity.Player;
import data.model.tournament.event.entity.timeslot.AbstractTimeslot;
import data.model.tournament.event.entity.timeslot.Timeslot;
import data.validation.validable.ValidationException;
import data.validation.validator.tournament.TournamentValidator;
import utils.TournamentUtils;

public class TournamentTest {

	@Rule
	public ExpectedException expectedEx = ExpectedException.none();
	
	private Tournament tournament;
	
	@Before
	public void setUp() {
		tournament = new Tournament("Tournament", new Event(
			"Event",
			TournamentUtils.buildGenericPlayers(16, "Player"),
			TournamentUtils.buildGenericLocalizations(2, "Localization"),
			TournamentUtils.buildAbstractTimeslots(10)
		));
	}
	
	@Test
	public void constructorTest() {
		List<Localization> localizations = TournamentUtils.buildGenericLocalizations(2, "Localization");
		List<Timeslot> timeslots = TournamentUtils.buildAbstractTimeslots(10);
		
		Event primaryEvent = new Event(
			"Primary Event",
			TournamentUtils.buildGenericPlayers(16, "Player"),
			localizations,
			timeslots
		);
		
		Event secondaryEvent = new Event(
			"Secondary Event",
			TournamentUtils.buildGenericPlayers(8, "Player"),
			localizations,
			timeslots
		);
		
		Tournament tournament = new Tournament("Tournament", new HashSet<Event>(Arrays.asList(primaryEvent, secondaryEvent)));
		
		assertEquals("Tournament", tournament.getName());
		assertEquals(2, tournament.getEvents().size());
		assertEquals(24, tournament.getAllPlayers().size());
		assertEquals(2, tournament.getAllLocalizations().size());
		assertEquals(10, tournament.getAllTimeslots().size());
		
		localizations = TournamentUtils.buildGenericLocalizations(3, "Localization");
		timeslots = TournamentUtils.buildAbstractTimeslots(15);
		
		primaryEvent = new Event(
			"Primary Event",
			TournamentUtils.buildGenericPlayers(32, "Player"),
			localizations,
			timeslots
		);
		
		secondaryEvent = new Event(
			"Secondary Event",
			TournamentUtils.buildGenericPlayers(9, "Player"),
			localizations,
			timeslots,
			2, 4, 3
		);
		
		tournament = new Tournament("Tournament", primaryEvent, secondaryEvent);
		
		assertEquals("Tournament", tournament.getName());
		assertEquals(2, tournament.getEvents().size());
		assertEquals(41, tournament.getAllPlayers().size());
		assertEquals(3, tournament.getAllLocalizations().size());
		assertEquals(15, tournament.getAllTimeslots().size());
		
		List<Player> players = TournamentUtils.buildGenericPlayers(24, "Player");
		
		primaryEvent = new Event(
			"Primary Event",
			players,
			localizations,
			timeslots
		);
		
		Collections.shuffle(players);
		secondaryEvent = new Event(
			"Secondary Event",
			new Random().ints(0, players.size()).distinct().limit(6).mapToObj(i -> players.get(i)).collect(Collectors.toList()),
			localizations,
			timeslots
		);
		
		tournament = new Tournament("Tournament", primaryEvent, secondaryEvent);
		
		assertEquals(24, tournament.getAllPlayers().size());
	}
	
	@Test
	public void constructorNullNameTest() {
		Event event = new Event(
			"Event",
			TournamentUtils.buildGenericPlayers(16, "Player"),
			TournamentUtils.buildGenericLocalizations(2, "Localization"),
			TournamentUtils.buildAbstractTimeslots(10)
		);
	
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("parameters cannot be null");
		new Tournament(null, event);
	}
	
	@Test
	public void constructorNullEventsTest() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("parameters cannot be null");
		new Tournament("Tournament", (Set<Event>)null);
	}
	
	@Test
	public void constructorEmtpyEventsTest() {
		Set<Event> events = new HashSet<Event>();
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("list of categories cannot be empty");
		new Tournament("Tournament", events);
	}
	
	@Test
	public void constructorNullEventTest() {
		Set<Event> events = new HashSet<Event>(Arrays.asList((Event)null));
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("category cannot be null");
		new Tournament("Tournament", events);
	}
	
	@Test
	public void setNameTest() {
		Event event = new Event(
			"Event",
			TournamentUtils.buildGenericPlayers(16, "Player"),
			TournamentUtils.buildGenericLocalizations(2, "Localization"),
			TournamentUtils.buildAbstractTimeslots(10)
		);
	
		Tournament tournament = new Tournament("Tournament", event);
		
		tournament.setName("Modified Tournament");
		
		assertEquals("Modified Tournament", tournament.getName());
		
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("Name cannot be null");
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
		tournament.getAllTimeslots().set(3, new AbstractTimeslot(3));
	}
	
	@Test
	public void solveTest() {
		try {
			assertNull(tournament.getSchedule());
			assertNull(tournament.getCurrentSchedules());
			assertTrue(tournament.solve());
			
			assertNotNull(tournament.getSchedule());
			assertNotNull(tournament.getCurrentSchedules());
		} catch (ValidationException e) {
			fail("Unexpected exception thrown; tournament is valid");
		}
	}
	
	@Test
	public void validatorTest() {
		tournament.setValidator(new TournamentValidator());
		
		assertTrue(tournament.getMessages().isEmpty());
		
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("parameter cannot be null");
		tournament.setValidator(null);
	}
}
