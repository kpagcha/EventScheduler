package data.model.schedule;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hamcrest.core.StringContains;
import org.junit.Before;
import org.junit.Test;

import data.model.schedule.data.Match;
import data.model.schedule.data.ScheduleValue;
import data.model.tournament.Tournament;
import data.model.tournament.event.Event;
import data.model.tournament.event.entity.Localization;
import data.model.tournament.event.entity.Player;
import data.model.tournament.event.entity.timeslot.Timeslot;
import data.validation.validable.ValidationException;
import solver.TournamentSolver.SearchStrategy;
import utils.TournamentUtils;

/**
 * Tests de las clases {@link Schedule}, {@link EventSchedule} y {@link TournamentSchedule}.
 *
 */
public class ScheduleTest {
	
	private Tournament tournament;
	
	@Before
	public void setUp() throws ValidationException {
		List<Localization> localizations = TournamentUtils.buildGenericLocalizations(2, "Court");
		List<Timeslot> timeslots = TournamentUtils.buildAbstractTimeslots(8);
		
		Event singles = new Event("Singles", TournamentUtils.buildGenericPlayers(8, "SPl"), localizations, timeslots);
		Event doubles = new Event(
			"Doubles",
			TournamentUtils.buildGenericPlayers(8, "DPl"),
			localizations,
			timeslots.subList(3, 8),
			1, 2, 4
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
		
		tournament.getSolver().setSearchStrategy(SearchStrategy.MINDOM_UB);
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
			
			List<ScheduleValue> vals = Stream.of(schedule.getScheduleValues()).flatMap(v -> Arrays.stream(v)).collect(Collectors.toList());
			
			assertEquals(
				event.getNumberOfOccupiedTimeslots(),
				vals.stream().filter(v -> v.isOccupied()).count()
			);
			
			assertTrue(
				vals.stream().filter(v -> v.isLimited()).count() <=
				event.getUnavailableLocalizations().values().stream().distinct().count() * event.getPlayers().size()
			);
			
			assertEquals(
				event.getBreaks().size() * event.getPlayers().size(),
				vals.stream().filter(v -> v.isBreak()).count()
			);
			
			assertTrue(
				vals.stream().filter(v -> v.isUnavailable()).count() <=
				event.getUnavailablePlayers().values().stream().count()
			);
		}
		
		try {
			new EventSchedule(null, new int[][][]{});
			fail("IllegalArgumentException expected");
		} catch (IllegalArgumentException e) {
			assertEquals("Parameters cannot be null", e.getMessage());
		}
		
		try {
			new EventSchedule(tournament.getEvents().get(0), null);
			fail("IllegalArgumentException expected");
		} catch (IllegalArgumentException e) {
			assertEquals("Parameters cannot be null", e.getMessage());
		}
	}
	
	@Test
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
		
		List<ScheduleValue> vals = Stream.of(schedule.getScheduleValues()).flatMap(v -> Arrays.stream(v)).collect(Collectors.toList());
		
		assertEquals(
			tournament.getNumberOfOccupiedTimeslots(),
			vals.stream().filter(v -> v.isOccupied()).count()
		);
		
		int nUnavailableLocalizationTimeslots = 0;
		int nBreakTimeslots = 0;
		for (Event event : tournament.getEvents()) {
			nUnavailableLocalizationTimeslots += 
				event.getUnavailableLocalizations().values().stream().distinct().count() * event.getPlayers().size();
			
			nBreakTimeslots += event.getBreaks().size() * event.getPlayers().size();
		}
		assertTrue(vals.stream().filter(v -> v.isLimited()).count() <= nUnavailableLocalizationTimeslots);
		
		assertEquals(nBreakTimeslots,vals.stream().filter(v -> v.isBreak()).count());
		
		assertTrue(
			vals.stream().filter(v -> v.isUnavailable()).count() <= 
			tournament.getEvents().stream().map(Event::getUnavailablePlayers).map(Map::values).flatMap(Collection::stream).count()
		);
		
		assertNotNull(schedule);
		assertEquals(tournament.getNumberOfMatches(), schedule.getMatches().size());
		
		try {
			new TournamentSchedule(null);
			fail("IllegalArgumentException expected");
		} catch (IllegalArgumentException e) {
			assertEquals("Tournament cannot be null", e.getMessage());
		}
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
		
		matches = schedule.filterMatchesByPlayers(new ArrayList<Player>(Arrays.asList(sPlayers.get(4), sPlayers.get(6), dPlayers.get(1))));
		assertEquals(0, matches.size());
		
		matches = schedule.filterMatchesByPlayers(new ArrayList<Player>(Arrays.asList(dPlayers.get(7), dPlayers.get(2))));
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
		
		matches = schedule.filterMatchesDuringTimeslots(new ArrayList<Timeslot>(Arrays.asList(timeslots.get(4), timeslots.get(7))));
		assertEquals(4, matches.size());
		
		matches = schedule.filterMatchesDuringTimeslotRange(timeslots.get(3), timeslots.get(4));
		assertEquals(2, matches.size());
		
		matches = schedule.filterMatchesInTimeslotRange(timeslots.get(1), timeslots.get(2));
		assertEquals(1, matches.size());
		
		assertEquals(0, schedule.filterMatchesByPlayer(new Player("New Player")).size());
		assertEquals(0, schedule.filterMatchesByLocalization(new Localization("New Localization")).size());
		assertEquals(0, schedule.filterMatchesByPlayers(new ArrayList<Player>(Arrays.asList(sPlayers.get(0), sPlayers.get(1)))).size());
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
		assertThat(schedules.get(tournament.getEvents().get(1)).toString(), not(StringContains.containsString("SinglePl")));
	}
	
	@Test
	public void scheduleValueTest() {
		ScheduleValue v = new ScheduleValue(ScheduleValue.FREE);
		assertEquals(v.getValue(), ScheduleValue.FREE);
		assertTrue(v.isFree());
		assertFalse(v.isOccupied());
		
		v = new ScheduleValue(ScheduleValue.OCCUPIED, 2);
		assertEquals(ScheduleValue.OCCUPIED, v.getValue());
		assertEquals(2, v.getLocalization());
		assertTrue(v.isOccupied());
		assertFalse(v.isFree());
		
		v = new ScheduleValue(ScheduleValue.UNAVAILABLE);
		assertTrue(v.isUnavailable());
		
		v = new ScheduleValue(ScheduleValue.BREAK);
		assertTrue(v.isBreak());
		
		v = new ScheduleValue(ScheduleValue.LIMITED);
		assertTrue(v.isLimited());
		
		v = new ScheduleValue(ScheduleValue.NOT_IN_DOMAIN);
		assertTrue(v.isNotInDomain());
		
		try {
			v = new ScheduleValue(ScheduleValue.FREE + 20);
			fail("IllegalArgumentException expected for invalid schedule value");
		} catch (IllegalArgumentException e) {
			assertThat(e.getMessage(), StringContains.containsString("Illegal value"));
		}
		
		try {
			v = new ScheduleValue(ScheduleValue.OCCUPIED);
			fail("IllegalArgumentException expected");
		} catch (IllegalArgumentException e) {
			assertEquals("A localization must be specified if the schedule value is OCCUPIED", e.getMessage());
		}
		
		try {
			v = new ScheduleValue(ScheduleValue.OCCUPIED + 20, 5);
			fail("IllegalArgumentException expected for invalid schedule value");
		} catch (IllegalArgumentException e) {
			assertThat(e.getMessage(), StringContains.containsString("Illegal value"));
		}
		
		try {
			v = new ScheduleValue(ScheduleValue.BREAK, 0);
			fail("IllegalArgumentException expected for invalid schedule value");
		} catch (IllegalArgumentException e) {
			assertEquals("Only schedule values of OCCUPIED can specify a localization", e.getMessage());
		}
		
		try {
			v = new ScheduleValue(ScheduleValue.FREE);
			v.getLocalization();
			fail("IllegalStateException expected for invalid schedule value");
		} catch (IllegalStateException e) {
			assertEquals("Only schedule values of OCCUPIED can specify a localization", e.getMessage());
		}
	}
}
