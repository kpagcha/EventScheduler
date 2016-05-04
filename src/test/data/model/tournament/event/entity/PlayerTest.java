package data.model.tournament.event.entity;

import static org.junit.Assert.*;

import org.junit.Test;

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
