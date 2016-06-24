package es.uca.garciachacon.eventscheduler.data.model.tournament;

import es.uca.garciachacon.eventscheduler.utils.TournamentUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.Assert.*;

/**
 * Tests de la clase {@link Team}.
 */
public class TeamTest {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void constructorTest() {
        Player player1 = new Player("Player 1");
        Player player2 = new Player("Player 2");
        Player player3 = new Player("Player 3");
        Player player4 = new Player("Player 4");
        Player player5 = new Player("Player 5");

        Team team = new Team("Team 1", new HashSet<>(Arrays.asList(player1, player2)));

        assertEquals("Team 1", team.getName());
        assertEquals(2, team.getPlayers().size());
        assertTrue(team.contains(player1));
        assertFalse(team.contains(player3));

        team = new Team(player3, player4, player5);

        assertEquals("Player 3-Player 4-Player 5", team.getName());
        assertEquals(3, team.getPlayers().size());
        assertTrue(team.contains(player4));
        assertFalse(team.contains(player1));

        team = new Team(new HashSet<>(Arrays.asList(player4, player2)));
        assertTrue(team.getName().equals("Player 4-Player 2") || team.getName().equals("Player 2-Player 4"));
        assertEquals(2, team.getPlayers().size());
        assertTrue(team.contains(player2));
        assertFalse(team.contains(player1));
    }

    @Test
    public void constructorNullNameTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Name cannot be null");
        new Team(null, new HashSet<>(Arrays.asList(new Player("John Smith"), new Player("Jane Doe"))));
    }

    @Test(expected = NullPointerException.class)
    public void constructorNullPlayersTest() {
        new Team("Team", null);
    }

    @Test
    public void constructorLessThanTwoPlayersTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("team cannot have less than two players");
        new Team("Team", new HashSet<>(Collections.singletonList(new Player("Jane Doe"))));
    }

    @Test
    public void constructorNullPlayerTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("team cannot contain a null player");
        new Team("Team", new HashSet<>(Arrays.asList(null, new Player("Jane Doe"))));
    }

    @Test
    public void constructorDuplicatedPlayersTest() {
        Player player1 = new Player("Player 1");
        Player player2 = new Player("Player 2");
        Player player3 = new Player("Player 3");

        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("All players must be unique");
        new Team(player1, player2, player2, player1, player3);
    }

    @Test
    public void constructorLessThanTwoPlayersInArrayTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("team cannot have less than two players");
        new Team(new Player("Player"));
    }

    @Test
    public void constructorNullPlayerInArrayTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("team cannot contain a null player");
        new Team(new Player("Jane Doe"), new Player("John Doe"), null);
    }

    @Test
    public void setEventTest() {
        Team team = new Team(new Player("Player 1"), new Player("Player 2"));
        Event event = new Event("Event",
                TournamentUtils.buildGenericPlayers(8, "Player"),
                TournamentUtils.buildGenericLocalizations(3, "Court"),
                TournamentUtils.buildLocalTimeTimeslots(8)
        );
        team.setEvent(event);
    }

    @Test(expected = NullPointerException.class)
    public void setNullEventTest() {
        Team team = new Team(new Player("Player 1"), new Player("Player 2"));
        team.setEvent(null);
    }

    @Test(expected = IllegalStateException.class)
    public void setEventTwiceTest() {
        Team team = new Team(new Player("Player 1"), new Player("Player 2"));
        Event event = new Event("Event",
                TournamentUtils.buildGenericPlayers(8, "Player"),
                TournamentUtils.buildGenericLocalizations(3, "Court"),
                TournamentUtils.buildLocalTimeTimeslots(8)
        );
        team.setEvent(event);
        team.setEvent(event);
    }

    @Test
    public void toStringTest() {
        Player player1 = new Player("Player 1");
        Player player2 = new Player("Player 2");

        Team team = new Team("Team", new HashSet<>(Arrays.asList(player1, player2)));

        assertEquals("Team", team.toString());

        team = new Team(player1, player2);

        assertEquals("Player 1-Player 2", team.toString());
    }
}
