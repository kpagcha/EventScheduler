package data.model.tournament;

import data.model.tournament.event.domain.Player;
import data.model.tournament.event.domain.Team;
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

        Team team1 = new Team("Team 1", new HashSet<>(Arrays.asList(player1, player2)));

        assertEquals("Team 1", team1.getName());
        assertEquals(2, team1.getPlayers().size());
        assertTrue(team1.contains(player1));
        assertFalse(team1.contains(player3));

        Team team2 = new Team(player3, player4, player5);

        assertEquals("Player 3-Player 4-Player 5", team2.getName());
        assertEquals(3, team2.getPlayers().size());
        assertTrue(team2.contains(player4));
        assertFalse(team2.contains(player1));
    }

    @Test
    public void constructorNullNameTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Name cannot be null");
        new Team(null, new HashSet<>(Arrays.asList(new Player("John Smith"), new Player("Jane Doe"))));
    }

    @Test
    public void constructorNullPlayersTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Players cannot be null");
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
    public void toStringTest() {
        Player player1 = new Player("Player 1");
        Player player2 = new Player("Player 2");

        Team team1 = new Team("Team", new HashSet<>(Arrays.asList(player1, player2)));

        assertEquals("Team", team1.toString());

        Team team2 = new Team(player1, player2);

        assertEquals("Player 1-Player 2", team2.toString());
    }
}
