package data.model.tournament;

import static org.junit.Assert.*;

import org.junit.Test;

import data.model.tournament.event.domain.Player;

public class EntityTest {

	@Test
	public void test() {
		Player player = new Player("John Doe");
		
		assertEquals("John Doe", player.getName());
		assertEquals("John Doe", player.toString());
		
		player.setName("John Smith");
		
		assertEquals("John Smith", player.getName());
		assertEquals("John Smith", player.toString());
		
		try {
			player.setName(null);
			fail("Exception should be thrown when setting null name");
		} catch (IllegalArgumentException e) {
			assertNotNull(player.getName());
			assertEquals("John Smith", player.getName());
			assertEquals("Name cannot be null", e.getMessage());
		}
	}

}
