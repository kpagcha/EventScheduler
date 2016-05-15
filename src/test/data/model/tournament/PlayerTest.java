package data.model.tournament;

import static org.junit.Assert.*;

import org.junit.Test;

import data.model.tournament.event.domain.Player;

/**
 * Tests de la clase {@link Player}.
 *
 */
public class PlayerTest {

	@Test
	public void test() {
		Player player = new Player("John Smith");
		assertEquals("John Smith", player.getName());
	}

}
