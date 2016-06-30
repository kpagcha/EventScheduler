package es.uca.garciachacon.eventscheduler.data.model.tournament;

import es.uca.garciachacon.eventscheduler.utils.TournamentUtils;
import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests de la clase {@link Matchup}.
 */
public class MatchupTest {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();
    private List<Player> players = TournamentUtils.buildGenericPlayers(8, "Player");
    private List<Localization> localizations = TournamentUtils.buildGenericLocalizations(4, "Court");
    private List<Timeslot> timeslots = TournamentUtils.buildSimpleTimeslots(10);

    @Test
    public void constructorTest() {
        Matchup matchup = new Matchup(
                new HashSet<>(Arrays.asList(players.get(3), players.get(5))),
                new HashSet<>(localizations),
                new HashSet<>(timeslots),
                1
        );

        assertEquals(2, matchup.getPlayers().size());
        assertTrue(players.containsAll(matchup.getPlayers()));
        assertEquals(localizations.size(), matchup.getLocalizations().size());
        assertTrue(localizations.containsAll(matchup.getLocalizations()));
        assertEquals(timeslots.size(), matchup.getTimeslots().size());
        assertTrue(timeslots.containsAll(matchup.getTimeslots()));
        assertEquals(1, matchup.getOccurrences());
    }

    @Test(expected = NullPointerException.class)
    public void constructorNullPlayersTest() {
        new Matchup(null, new HashSet<>(localizations), new HashSet<>(timeslots), 1);
    }

    @Test
    public void constructorEmptyPlayersTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Players cannot be empty");
        new Matchup(new HashSet<>(), new HashSet<>(), new HashSet<>());
    }

    @Test(expected = NullPointerException.class)
    public void constructorNullLocalizationsTest() {
        new Matchup(new HashSet<>(Arrays.asList(players.get(3), players.get(5))), null, new HashSet<>(timeslots), 1);
    }

    @Test(expected = NullPointerException.class)
    public void constructorNullTimeslotsTest() {
        new Matchup(new HashSet<>(players), new HashSet<>(localizations), null, 1);
    }

    @Test
    public void constructorNullPlayerTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Players cannot contain null");
        new Matchup(
                new HashSet<>(Arrays.asList(players.get(3), null)),
                new HashSet<>(localizations),
                new HashSet<>(timeslots),
                1
        );
    }

    @Test
    public void constructorNullLocalizationTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Localizations cannot contain null");
        new Matchup(
                new HashSet<>(Arrays.asList(players.get(3), players.get(2))),
                new HashSet<>(Arrays.asList(localizations.get(2), null)),
                new HashSet<>(timeslots),
                1
        );
    }

    @Test
    public void constructorNullTimeslotTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Timeslots cannot contain null");
        new Matchup(
                new HashSet<>(Arrays.asList(players.get(3), players.get(2))),
                new HashSet<>(Arrays.asList(localizations.get(2))),
                new HashSet<>(Arrays.asList(timeslots.get(3), null)),
                1
        );
    }

    @Test
    public void constructorOccurrencesLessThanOneTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Occurrences cannot be less than 1");
        new Matchup(
                new HashSet<>(Arrays.asList(players.get(3), players.get(5))),
                new HashSet<>(localizations),
                new HashSet<>(timeslots),
                0
        );
    }

    @Test
    public void constructorWithoutOccurrencesTest() {
        Matchup matchup = new Matchup(
                new HashSet<>(Arrays.asList(players.get(3), players.get(5))),
                new HashSet<>(localizations),
                new HashSet<>(timeslots)
        );
        assertEquals(1, matchup.getOccurrences());
    }

    @Test
    public void constructorOnlyWithPlayersTest() {
        Matchup matchup = new Matchup(new HashSet<>(Arrays.asList(players.get(3), players.get(5))));
        assertTrue(matchup.getLocalizations().isEmpty());
        assertTrue(matchup.getTimeslots().isEmpty());
        assertEquals(1, matchup.getOccurrences());
    }

    @Test
    public void toStringTest() {
        Matchup matchup = new Matchup(
                new HashSet<>(Arrays.asList(players.get(0), players.get(1))),
                new HashSet<>(Arrays.asList(localizations.get(1))),
                new HashSet<>(Arrays.asList(timeslots.get(3), timeslots.get(5))),
                1
        );
        String str = matchup.toString();
        System.out.println(str);
        assertThat(str, CoreMatchers.containsString("Matchup {Players="));
        assertThat(str, CoreMatchers.containsString("Player 2"));
        assertThat(str, CoreMatchers.containsString("Player 1"));
        assertThat(str, CoreMatchers.containsString("Localizations=[Court 2]"));
        assertThat(str, CoreMatchers.containsString("Timeslots=[Timeslot [order=3],Timeslot [order=5]]"));
        assertThat(str, CoreMatchers.containsString("Occurrences=1"));
    }
}
