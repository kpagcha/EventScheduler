package data.model.schedule;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hamcrest.core.StringContains;
import org.junit.Before;
import org.junit.Test;

import data.model.schedule.data.GroupedScheduleValue;
import data.model.schedule.data.Match;
import data.model.tournament.Tournament;
import data.model.tournament.event.Event;
import data.model.tournament.event.entity.Localization;
import data.model.tournament.event.entity.Player;
import data.model.tournament.event.entity.timeslot.Timeslot;
import data.validation.validable.ValidationException;
import solver.TournamentSolver.SearchStrategy;
import utils.TournamentUtils;

public class GroupedScheduleTest {
	
	private Tournament tournament;
	private Event single;
	private List<Match> singleMatches;
	private List<Match> tournamentMatches;
	
	@Before
	public void setUp() throws ValidationException {
		List<Localization> localizations = TournamentUtils.buildGenericLocalizations(2, "Court");
		List<Timeslot> timeslots = TournamentUtils.buildAbstractTimeslots(10);
		
		Event singles = new Event("Singles", TournamentUtils.buildGenericPlayers(8, "SPl"), localizations, timeslots);
		Event doubles = new Event(
			"Doubles",
			TournamentUtils.buildGenericPlayers(8, "DPl"),
			localizations,
			timeslots.subList(3, 8),
			1, 2, 4
		);
		
		List<Player> sPlayers = singles.getPlayers();
		singles.addFixedMatchup(sPlayers.get(0), sPlayers.get(7));
		
		List<Player> dPlayers = doubles.getPlayers();
		doubles.addTeamPlayers(dPlayers.get(0), dPlayers.get(5));
		doubles.addTeamPlayers(dPlayers.get(7), dPlayers.get(2));
		doubles.addTeamPlayers(dPlayers.get(4), dPlayers.get(1));
		doubles.addTeamPlayers(dPlayers.get(3), dPlayers.get(6));
		
		tournament = new Tournament("Tennis Tournament", singles, doubles);
		
		tournament.addBreak(timeslots.get(5));
		tournament.addUnavailableLocalizationAtTimeslot(localizations.get(0), timeslots.get(0));
		tournament.addUnavailablePlayerAtTimeslot(sPlayers.get(0), timeslots.get(7));
		doubles.addUnavailableLocalizationAtTimeslot(localizations.get(1), timeslots.get(4));
		
		tournament.getSolver().setSearchStrategy(SearchStrategy.MINDOM_UB);
		
		tournament.solve();
		
		single = tournament.getEvents().get(0);
		singleMatches = tournament.getCurrentSchedules().get(single).getMatches();
		tournamentMatches = tournament.getSchedule().getMatches();
	}

	@Test
	public void constructorEventGroupedSchedule() {
		GroupedSchedule schedule = new GroupedSchedule(single, singleMatches);
		
		List<GroupedScheduleValue> vals = 
			Stream.of(schedule.getScheduleValues()).flatMap(v -> Arrays.stream(v)).collect(Collectors.toList());
		
		assertEquals(single.getNumberOfMatches(), vals.stream().filter(v -> v.isOccupied()).count());
		
		long nUnavailableTimeslots = single.getLocalizations().size() * single.getBreaks().size() +
				single.getUnavailableLocalizations().values().stream().distinct().count();
		
		assertTrue(vals.stream().filter(v -> v.isLimited()).count() <= nUnavailableTimeslots);
		assertTrue(vals.stream().filter(v -> v.isUnavailable()).count() <= nUnavailableTimeslots);
		
		try {
			new GroupedSchedule((Event)null, singleMatches);
			fail("IllegalArgumentException expected");
		} catch (IllegalArgumentException e) {
			assertEquals("The parameters cannot be null", e.getMessage());
		}
		
		try {
			new GroupedSchedule(single, null);
			fail("IllegalArgumentException expected");
		} catch (IllegalArgumentException e) {
			assertEquals("The parameters cannot be null", e.getMessage());
		}
		
		try {
			singleMatches.remove(singleMatches.size() - 1);
			new GroupedSchedule(single, singleMatches);
			fail("IllegalArgumentException expected");
		} catch (IllegalArgumentException e) {
			assertThat(e.getMessage(), StringContains.containsString("equal to the expected number of matches for the event"));
		}
	}
	
	@Test
	public void constructorTournamentGroupedSchedule() {
		GroupedSchedule schedule = new GroupedSchedule(tournament, tournamentMatches);
		
		List<GroupedScheduleValue> vals = 
			Stream.of(schedule.getScheduleValues()).flatMap(v -> Arrays.stream(v)).collect(Collectors.toList());
		
		assertEquals(tournament.getNumberOfMatches(), vals.stream().filter(v -> v.isOccupied()).count());
		
		long nUnavailableTimeslots = 0;
		for (Event event : tournament.getEvents())
			nUnavailableTimeslots += event.getUnavailableLocalizations().values().stream().distinct().count() +
				event.getBreaks().size() * event.getLocalizations().size();
		
		assertTrue(vals.stream().filter(v -> v.isLimited()).count() <= nUnavailableTimeslots);
		assertTrue(vals.stream().filter(v -> v.isUnavailable()).count() <= nUnavailableTimeslots);
		
		try {
			new GroupedSchedule((Tournament)null, tournamentMatches);
			fail("IllegalArgumentException expected");
		} catch (IllegalArgumentException e) {
			assertEquals("The parameters cannot be null", e.getMessage());
		}
		
		try {
			new GroupedSchedule(tournament, null);
			fail("IllegalArgumentException expected");
		} catch (IllegalArgumentException e) {
			assertEquals("The parameters cannot be null", e.getMessage());
		}
		
		try {
			tournamentMatches.remove(tournamentMatches.size() - 1);
			new GroupedSchedule(tournament, tournamentMatches);
			fail("IllegalArgumentException expected");
		} catch (IllegalArgumentException e) {
			assertThat(e.getMessage(), StringContains.containsString("equal to the expected number of matches for the tournament"));
		}
	}
	
	@Test
	public void getTotalTimeslotsTest() {
		GroupedSchedule schedule = new GroupedSchedule(tournament, tournamentMatches);
		
		assertEquals(tournament.getAllLocalizations().size() * tournament.getAllTimeslots().size(), schedule.getTotalTimeslots());
	}
	
	@Test
	public void getAvailableTimeslotsTest() {
		GroupedSchedule schedule = new GroupedSchedule(tournament, tournamentMatches);
		List<GroupedScheduleValue> vals = 
				Stream.of(schedule.getScheduleValues()).flatMap(v -> Arrays.stream(v)).collect(Collectors.toList());
		
		assertEquals(
			vals.stream().filter(v -> v.isOccupied() || v.isFree() || v.isContinuation()).count(),
			schedule.getAvailableTimeslots()
		);
	}

	
	@Test
	public void getOccupationTest() {
		GroupedSchedule schedule = new GroupedSchedule(tournament, tournamentMatches);
		List<GroupedScheduleValue> vals = 
				Stream.of(schedule.getScheduleValues()).flatMap(v -> Arrays.stream(v)).collect(Collectors.toList());
		
		assertEquals(
			vals.stream().filter(v -> v.isOccupied() || v.isContinuation()).count(),
			schedule.getOccupation()
		);
	}
	
	@Test
	public void toStringTest() {
		GroupedSchedule schedule = new GroupedSchedule(tournament, tournamentMatches);
		String scheduleStr = schedule.toString();
		
		assertThat(scheduleStr, StringContains.containsString("5,6"));
		assertThat(scheduleStr, StringContains.containsString("8,9,12,13"));
		assertThat(scheduleStr, StringContains.containsString("¬"));
	}
	
	@Test
	public void groupedScheduleValueTest() {
		GroupedScheduleValue v = new GroupedScheduleValue(GroupedScheduleValue.FREE);
		assertTrue(v.isFree());
		
		v = new GroupedScheduleValue(GroupedScheduleValue.LIMITED);
		assertTrue(v.isLimited());
		
		v = new GroupedScheduleValue(GroupedScheduleValue.UNAVAILABLE);
		assertTrue(v.isUnavailable());
		
		v = new GroupedScheduleValue(GroupedScheduleValue.CONTINUATION);
		assertTrue(v.isContinuation());
		
		v = new GroupedScheduleValue(GroupedScheduleValue.OCCUPIED, new ArrayList<Integer>(Arrays.asList(3, 4)));
		assertTrue(v.isOccupied());
		
		try {
			new GroupedScheduleValue(GroupedScheduleValue.LIMITED + 50);
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			assertThat(e.getMessage(), StringContains.containsString("Illegal value"));
		}
		
		try {
			new GroupedScheduleValue(GroupedScheduleValue.OCCUPIED);
			fail("Expected IllegalStateException");
		} catch (IllegalStateException e) {
			assertEquals("A match must be specified if the schedule value is OCCUPIED", e.getMessage());
		}
		
		try {
			new GroupedScheduleValue(GroupedScheduleValue.LIMITED + 50, new ArrayList<Integer>(Arrays.asList(3, 4)));
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			assertThat(e.getMessage(), StringContains.containsString("Illegal value"));
		}
		
		try {
			new GroupedScheduleValue(GroupedScheduleValue.CONTINUATION, new ArrayList<Integer>(Arrays.asList(3, 4)));
			fail("Expected IllegalStateException");
		} catch (IllegalStateException e) {
			assertEquals("Only schedule values of OCCUPIED can specify a match taking place", e.getMessage());
		}
		
		try {
			new GroupedScheduleValue(GroupedScheduleValue.OCCUPIED, null);
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			assertEquals("Indices cannot be null", e.getMessage());
		}
		
		try {
			new GroupedScheduleValue(GroupedScheduleValue.OCCUPIED, new ArrayList<Integer>());
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			assertEquals("Indices cannot be empty", e.getMessage());
		}
		
		try {
			new GroupedScheduleValue(GroupedScheduleValue.OCCUPIED, new ArrayList<Integer>(Arrays.asList(3, null)));
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			assertEquals("Index cannot be null", e.getMessage());
		}
		
		try {
			new GroupedScheduleValue(GroupedScheduleValue.UNAVAILABLE).getPlayersIndices();
			fail("Expected IllegalStateException");
		} catch (IllegalStateException e) {
			assertEquals("Only schedule values of OCCUPIED can specify a match taking place", e.getMessage());
		}
	}
}
