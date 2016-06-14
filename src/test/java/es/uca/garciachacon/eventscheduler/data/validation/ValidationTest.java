package es.uca.garciachacon.eventscheduler.data.validation;

import es.uca.garciachacon.eventscheduler.data.model.tournament.Tournament;
import es.uca.garciachacon.eventscheduler.data.model.tournament.event.Event;
import es.uca.garciachacon.eventscheduler.data.model.tournament.event.domain.Localization;
import es.uca.garciachacon.eventscheduler.data.model.tournament.event.domain.Player;
import es.uca.garciachacon.eventscheduler.data.model.tournament.event.domain.Timeslot;
import es.uca.garciachacon.eventscheduler.data.validation.validable.ValidationException;
import es.uca.garciachacon.eventscheduler.utils.TournamentUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests de las clases de validación e implementaciones concretas.
 */
public class ValidationTest {

    private Event event;

    @Before
    public void setUp() {
        event = new Event(
                "Event",
                TournamentUtils.buildGenericPlayers(16, "Player"),
                TournamentUtils.buildGenericLocalizations(3, "Localization"),
                TournamentUtils.buildSimpleTimeslots(12),
                2,
                1,
                4
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
            event.getMessages().forEach(System.out::println);
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
            event.getMessages().forEach(System.out::println);
            fail("Unexpected ValidationException thrown");
        }
    }

}
