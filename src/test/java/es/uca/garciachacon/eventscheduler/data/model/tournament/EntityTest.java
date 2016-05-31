package es.uca.garciachacon.eventscheduler.data.model.tournament;

import es.uca.garciachacon.eventscheduler.data.model.tournament.event.domain.Entity;
import es.uca.garciachacon.eventscheduler.data.model.tournament.event.domain.Player;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests de la clase {@link Entity}.
 */
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
