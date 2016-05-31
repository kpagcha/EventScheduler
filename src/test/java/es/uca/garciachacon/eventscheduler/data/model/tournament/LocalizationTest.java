package es.uca.garciachacon.eventscheduler.data.model.tournament;

import es.uca.garciachacon.eventscheduler.data.model.tournament.event.domain.Localization;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests de la clase {@link Localization}.
 */
public class LocalizationTest {

    @Test
    public void test() {
        Localization localization = new Localization("Court 3");
        assertEquals("Court 3", localization.getName());
    }

}
