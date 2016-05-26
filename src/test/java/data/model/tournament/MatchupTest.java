package data.model.tournament;

import data.model.tournament.event.Event;
import data.model.tournament.event.Matchup;
import data.model.tournament.event.domain.Localization;
import data.model.tournament.event.domain.Player;
import data.model.tournament.event.domain.timeslot.AbstractTimeslot;
import data.model.tournament.event.domain.timeslot.Timeslot;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import utils.TournamentUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test de la clase {@link Matchup}
 */
public class MatchupTest {

    private Event event;
    private List<Player> players;
    private List<Localization> localizations;
    private List<Timeslot> timeslots;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Before
    public void setUp() {
        event = new Event("Event",
                TournamentUtils.buildGenericPlayers(8, "Player"),
                TournamentUtils.buildGenericLocalizations(4, "Court"),
                TournamentUtils.buildAbstractTimeslots(10)
        );
        players = event.getPlayers();
        localizations = event.getLocalizations();
        timeslots = event.getTimeslots();
    }

    @Test
    public void constructorTest() {
        Matchup matchup = new Matchup(event,
                new HashSet<>(Arrays.asList(players.get(3), players.get(5))),
                new HashSet<>(localizations),
                new HashSet<>(timeslots),
                1
        );

        assertEquals(event, matchup.getEvent());
        assertEquals(2, matchup.getPlayers().size());
        assertTrue(players.containsAll(matchup.getPlayers()));
        assertEquals(localizations.size(), matchup.getLocalizations().size());
        assertTrue(localizations.containsAll(matchup.getLocalizations()));
        assertEquals(timeslots.size() - 1, matchup.getTimeslots().size());
        assertTrue(timeslots.containsAll(matchup.getTimeslots()));
        assertEquals(1, matchup.getOccurences());
    }

    @Test
    public void constructorNullEventTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Event cannot be null");
        new Matchup(null,
                new HashSet<>(Arrays.asList(players.get(3), players.get(5))),
                new HashSet<>(localizations),
                new HashSet<>(timeslots),
                1
        );
    }

    @Test
    public void constructorNullPlayersTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Players cannot be null");
        new Matchup(event, null, new HashSet<>(localizations), new HashSet<>(timeslots), 1);
    }

    @Test
    public void constructorNullLocalizationsTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Localizations cannot be null");
        new Matchup(event,
                new HashSet<>(Arrays.asList(players.get(3), players.get(5))),
                null,
                new HashSet<>(timeslots),
                1
        );
    }

    @Test
    public void constructorNullTimeslotsTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Timeslots cannot be null");
        new Matchup(event, new HashSet<>(players), new HashSet<>(localizations), null, 1);
    }

    @Test
    public void constructorInvalidNumberOfPlayersTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage(
                "Players cannot contain a number of players (8) different than the number of players per match the " +
                        "event specifies (2)");
        new Matchup(event, new HashSet<>(players), new HashSet<>(localizations), new HashSet<>(timeslots), 1);
    }

    @Test
    public void constructorNullPlayerTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Players cannot contain null");
        new Matchup(event,
                new HashSet<>(Arrays.asList(players.get(3), null)),
                new HashSet<>(localizations),
                new HashSet<>(timeslots),
                1
        );
    }

    @Test
    public void constructorNonexistingPlayerTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Not all players belong to the domain of the event");
        new Matchup(event,
                new HashSet<>(Arrays.asList(players.get(3), new Player("Unknown Player"))),
                new HashSet<>(localizations),
                new HashSet<>(timeslots),
                1
        );
    }

    @Test
    public void constructorEmptyLocalizationsTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Localizations cannot be empty");
        new Matchup(event,
                new HashSet<>(Arrays.asList(players.get(3), players.get(2))),
                new HashSet<>(),
                new HashSet<>(timeslots),
                1
        );
    }

    @Test
    public void constructorNullLocalizationTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Localizations cannot contain null");
        new Matchup(event,
                new HashSet<>(Arrays.asList(players.get(3), players.get(2))),
                new HashSet<>(Arrays.asList(localizations.get(2), null)),
                new HashSet<>(timeslots),
                1
        );
    }

    @Test
    public void constructorNonexistingLocalizationTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Not all localizations belong to the domain of the event");
        new Matchup(event,
                new HashSet<>(Arrays.asList(players.get(3), players.get(2))),
                new HashSet<>(Arrays.asList(localizations.get(2), new Localization("Unknown Localization"))),
                new HashSet<>(timeslots),
                1
        );
    }

    @Test
    public void constructorTimeslotsEmptyTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Timeslots cannot be empty");
        new Matchup(event,
                new HashSet<>(Arrays.asList(players.get(3), players.get(2))),
                new HashSet<>(Arrays.asList(localizations.get(2))),
                new HashSet<>(),
                1
        );
    }

    @Test
    public void constructorInvalidTimeslotTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Timeslots cannot be empty");
        new Matchup(event,
                new HashSet<>(Arrays.asList(players.get(3), players.get(2))),
                new HashSet<>(Arrays.asList(localizations.get(2))),
                new HashSet<>(Arrays.asList(timeslots.get(timeslots.size() - 1))),
                1
        );
    }

    @Test
    public void constructorNullTimeslotTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Timeslots cannot contain null");
        new Matchup(event,
                new HashSet<>(Arrays.asList(players.get(3), players.get(2))),
                new HashSet<>(Arrays.asList(localizations.get(2))),
                new HashSet<>(Arrays.asList(timeslots.get(3), null)),
                1
        );
    }

    @Test
    public void constructorNonexistingTimeslotTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Not all timeslots belong to the domain of the event");
        new Matchup(event,
                new HashSet<>(Arrays.asList(players.get(3), players.get(2))),
                new HashSet<>(Arrays.asList(localizations.get(2))),
                new HashSet<>(Arrays.asList(timeslots.get(3), new AbstractTimeslot(3))),
                1
        );
    }

    @Test
    public void constructorMatchupExcessTest() {
        event.addMatchup(new Matchup(event,
                new HashSet<>(Arrays.asList(players.get(3), players.get(2))),
                new HashSet<>(Arrays.asList(localizations.get(1))),
                new HashSet<>(timeslots),
                1
        ));

        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("number of predefined matchups (2) would exceed the limit (1)");
        new Matchup(event,
                new HashSet<>(Arrays.asList(players.get(3), players.get(2))),
                new HashSet<>(Arrays.asList(localizations.get(2))),
                new HashSet<>(timeslots),
                1
        );
    }
}
