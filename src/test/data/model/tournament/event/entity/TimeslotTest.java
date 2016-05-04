package data.model.tournament.event.entity;

import static org.junit.Assert.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.Test;

import data.model.tournament.event.entity.timeslot.AbstractTimeslot;
import data.model.tournament.event.entity.timeslot.DefiniteTimeslot;
import data.model.tournament.event.entity.timeslot.Timeslot;
import data.model.tournament.event.entity.timeslot.UndefiniteTimeslot;

public class TimeslotTest {

	@Test
	public void abstractTimeslotTest() {
		AbstractTimeslot firstTimeslot = new AbstractTimeslot(0);
		AbstractTimeslot secondTimeslot = new AbstractTimeslot(1);
		
		assertEquals(0, firstTimeslot.getChronologicalOrder());
		assertEquals(1, secondTimeslot.getChronologicalOrder());
		assertEquals("Timeslot [order=0]", firstTimeslot.getName());
		assertEquals("0", firstTimeslot.toString());
		assertEquals("Timeslot [order=1]", secondTimeslot.getName());
		assertEquals("1", secondTimeslot.toString());
		
		AbstractTimeslot thirdTimeslot = new AbstractTimeslot(3);
		AbstractTimeslot anotherTimeslot = new AbstractTimeslot(1);
		
		assertEquals(1, firstTimeslot.compareTo(secondTimeslot));
		assertEquals(1, firstTimeslot.compareTo(thirdTimeslot));
		assertEquals(-1, thirdTimeslot.compareTo(firstTimeslot));
		assertEquals(0, secondTimeslot.compareTo(anotherTimeslot));
		assertEquals(1, thirdTimeslot.compareTo(null));
	}
	
	@Test
	public void definiteTimeslotTest() {
		DefiniteTimeslot at10amOnDay1 = new DefiniteTimeslot(LocalTime.of(10, 0), Duration.ofHours(2), 1);
		DefiniteTimeslot at12pmOnDay1 = new DefiniteTimeslot(LocalTime.of(12, 0), Duration.ofHours(2), 1);
		DefiniteTimeslot at10amOnDay2 = new DefiniteTimeslot(LocalTime.of(10, 0), Duration.ofHours(2), 2);
		
		try {
			new DefiniteTimeslot(null, Duration.ofHours(2), 1);
			fail("Expected exception when start time is null");
		} catch (IllegalArgumentException e) {
			assertEquals("Start cannot be null", e.getMessage());
		}
				
		assertEquals("10:00 (PT2H) [1]", at10amOnDay1.toString());
		assertEquals("12:00 (PT2H) [1]", at12pmOnDay1.toString());
		assertEquals("10:00 (PT2H) [2]", at10amOnDay2.toString());
		
		assertEquals(LocalTime.of(12, 0), at12pmOnDay1.getStart());
		
		DefiniteTimeslot anotherAt10amOnDay1 = new DefiniteTimeslot(LocalTime.of(10, 0), Duration.ofHours(2), 1);
		
		assertEquals(1, at10amOnDay1.compareTo(at10amOnDay2));
		assertEquals(-1, at10amOnDay2.compareTo(at12pmOnDay1));
		assertEquals(0, at10amOnDay1.compareTo(anotherAt10amOnDay1));
		
		assertEquals(1, anotherAt10amOnDay1.compareTo(null));
		
		assertEquals(-1, anotherAt10amOnDay1.compareTo(new AbstractTimeslot(0)));
		assertEquals(0, anotherAt10amOnDay1.compareTo(new AbstractTimeslot(1)));
		assertEquals(1, anotherAt10amOnDay1.compareTo(new AbstractTimeslot(2)));
	}
	
	@Test
	public void rangedTimeslotTest() {
		try {
			new DefiniteTimeslot(LocalDate.of(2016, 4, 24), null, 1);
			fail("Expected exception when duration is null");
		} catch (IllegalArgumentException e) {
			assertEquals("Duration cannot be null", e.getMessage());
		}
		
		DefiniteTimeslot timeslot = new DefiniteTimeslot(LocalDate.of(2016, 4, 24), Duration.ofMinutes(120), 3);
		assertEquals(Duration.ofMinutes(120), timeslot.getDuration());
	}
	
	@Test
	public void undefiniteTimeslotTest() {
		UndefiniteTimeslot undefiniteTimeslot = new UndefiniteTimeslot(Duration.ofMinutes(5), 1);
		
		assertEquals(1, undefiniteTimeslot.getChronologicalOrder());
		assertEquals(Duration.ofMinutes(5), undefiniteTimeslot.getDuration());
		assertEquals("1 (PT5M)", undefiniteTimeslot.toString());
	}
	
	@Test
	public void compareTest() {
		AbstractTimeslot abstractTimeslot1 = new AbstractTimeslot(5);
		AbstractTimeslot abstractTimeslot2 = new AbstractTimeslot(7);
		
		DefiniteTimeslot definiteTimeslot1 = new DefiniteTimeslot(LocalTime.of(6, 0), Duration.ofMinutes(10), 10);
		DefiniteTimeslot definiteTimeslot2 = new DefiniteTimeslot(LocalTime.of(6, 20), Duration.ofMinutes(10), 10);
		
		UndefiniteTimeslot undefiniteTimeslot1 = new UndefiniteTimeslot(Duration.ofMinutes(5), 1);
		UndefiniteTimeslot undefiniteTimeslot2 = new UndefiniteTimeslot(Duration.ofMinutes(5), 3);
		
		assertEquals(1, Timeslot.compare(abstractTimeslot1, abstractTimeslot2));
		assertEquals(-1, Timeslot.compare(abstractTimeslot2, abstractTimeslot1));
		assertEquals(0, Timeslot.compare(new AbstractTimeslot(5), abstractTimeslot1));
		
		assertEquals(1, Timeslot.compare(definiteTimeslot1, definiteTimeslot2));
		assertEquals(-1, Timeslot.compare(definiteTimeslot2, definiteTimeslot1));
		assertEquals(0, Timeslot.compare(new DefiniteTimeslot(LocalTime.of(6, 20), Duration.ofMinutes(90), 10), definiteTimeslot2));
		
		assertEquals(1, Timeslot.compare(undefiniteTimeslot1, undefiniteTimeslot2));
		assertEquals(-1, Timeslot.compare(undefiniteTimeslot2, undefiniteTimeslot1));
		assertEquals(0, Timeslot.compare(new UndefiniteTimeslot(Duration.ofMinutes(30), 1), undefiniteTimeslot1));
		
		assertEquals(1, Timeslot.compare(new AbstractTimeslot(3), null));
		assertEquals(-1, Timeslot.compare(null, new AbstractTimeslot(3)));
		assertEquals(0, Timeslot.compare(null, null));
	}
}
