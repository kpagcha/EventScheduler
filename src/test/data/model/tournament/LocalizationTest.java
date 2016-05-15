package data.model.tournament;

import static org.junit.Assert.*;

import org.junit.Test;

import data.model.tournament.event.domain.Localization;

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
