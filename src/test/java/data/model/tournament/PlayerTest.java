package data.model.tournament;

import data.model.tournament.event.domain.Player;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests de la clase {@link Player}.
 */
public class PlayerTest {

    @Test
    public void test() {
        Player player = new Player("John Smith");
        assertEquals("John Smith", player.getName());
    }

}
