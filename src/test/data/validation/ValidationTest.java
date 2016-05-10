package data.validation;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import data.model.tournament.Tournament;
import data.model.tournament.event.Event;
import data.model.tournament.event.entity.Localization;
import data.model.tournament.event.entity.Player;
import data.model.tournament.event.entity.timeslot.Timeslot;
import data.validation.validable.ValidationException;
import utils.TournamentUtils;

public class ValidationTest {
	
	private Event event;
	
	@Before
	public void setUp() {
		event = new Event(
			"Event",
			TournamentUtils.buildGenericPlayers(16, "Player"),
			TournamentUtils.buildGenericLocalizations(3, "Localization"),
			TournamentUtils.buildAbstractTimeslots(12),
			2, 1, 4
		);
		List<Player> players = event.getPlayers();
		List<Localization> localizations = event.getLocalizations();
		List<Timeslot> timeslots = event.getTimeslots();
		
		event.addTeam(players.get(0), players.get(3));
		event.addTeam(players.get(2), players.get(5));
		
		event.addUnavailablePlayerAtTimeslot(players.get(4), timeslots.get(1));
		event.addUnavailablePlayerAtTimeslot(players.get(4), timeslots.get(2));
		event.addUnavailablePlayerAtTimeslot(players.get(3), timeslots.get(3));
		event.addUnavailablePlayerAtTimeslot(players.get(7), timeslots.get(9));
		event.addUnavailablePlayerAtTimeslot(players.get(7), timeslots.get(10));
		
		event.addUnavailableLocalizationAtTimeslot(localizations.get(0), timeslots.get(0));
		event.addUnavailableLocalizationAtTimeslot(localizations.get(0), timeslots.get(1));
		event.addUnavailableLocalizationAtTimeslot(localizations.get(1), timeslots.get(7));
		event.addUnavailableLocalizationAtTimeslot(localizations.get(1), timeslots.get(10));
		event.addUnavailableLocalizationAtTimeslot(localizations.get(2), timeslots.get(11));
		
		event.addBreak(timeslots.get(3));
		
		event.addPlayerInLocalization(players.get(2), localizations.get(1));
		
		event.addPlayerAtTimeslot(players.get(4), timeslots.get(5));
		event.addPlayerAtTimeslot(players.get(4), timeslots.get(6));
		event.addPlayerAtTimeslot(players.get(5), timeslots.get(5));
	}

	@Test
	public void eventValidatorTest() {
		try {
			event.validate();
			assertTrue(event.getMessages().isEmpty());
		} catch (ValidationException e) {
			for (String err : event.getMessages())
				System.out.println(err);
			fail("Unexpected ValidationException thrown");
		}
	}
	
	@Test
	public void tournamentValidatorTest() {
		Tournament tournament = new Tournament("Tournament", event);
		
		try {
			tournament.validate();
			assertTrue(event.getMessages().isEmpty());
		} catch (ValidationException e) {
			for (String err : event.getMessages())
				System.out.println(err);
			fail("Unexpected ValidationException thrown");
		}
	}

}
