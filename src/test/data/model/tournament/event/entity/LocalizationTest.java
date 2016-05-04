package data.model.tournament.event.entity;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Tests de la clase {@link Localization}.
 *
 */
public class LocalizationTest {

	@Test
	public void test() {
		Localization localization = new Localization("Court 3");
		assertEquals("Court 3", localization.getName());
	}

}
