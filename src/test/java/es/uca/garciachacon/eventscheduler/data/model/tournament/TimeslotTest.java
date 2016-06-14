package es.uca.garciachacon.eventscheduler.data.model.tournament;

import es.uca.garciachacon.eventscheduler.data.model.tournament.event.domain.Timeslot;
import es.uca.garciachacon.eventscheduler.utils.TournamentUtils;
import org.junit.Test;

import java.time.*;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAmount;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests de la clase {@link Timeslot}.
 */
public class TimeslotTest {

    @Test(expected = NullPointerException.class)
    public void timeslotConstructorNullStartTest() {
        new Timeslot(4, (TemporalAccessor) null);
    }

    @Test(expected = NullPointerException.class)
    public void timeslotConstructorNullDurationTest() {
        new Timeslot(7, (TemporalAmount) null);
    }

    @Test(expected = NullPointerException.class)
    public void timeslotConstructorNullStartAndDurationTest() {
        new Timeslot(3, null, null);
    }

    @Test
    public void simpleTimeslotTest() {
        Timeslot t1 = new Timeslot(5);
        Timeslot t2 = new Timeslot(8);

        assertEquals(5, t1.getChronologicalOrder());
        assertEquals(8, t2.getChronologicalOrder());

        assertFalse(t1.getStart().isPresent());
        assertFalse(t1.getDuration().isPresent());
        assertFalse(t2.getStart().isPresent());
        assertFalse(t2.getDuration().isPresent());

        assertEquals("Timeslot [order=5]", t1.getName());
        assertEquals("Timeslot [order=8]", t2.getName());
    }

    @Test(expected = NullPointerException.class)
    public void simpleTimeslotCompareToTest() {
        Timeslot t1 = new Timeslot(1);
        Timeslot t2 = new Timeslot(2);

        assertEquals(1, t1.compareTo(t2));
        assertEquals(1, t1.compareTo(new Timeslot(5)));
        assertEquals(1, t2.compareTo(new Timeslot(3)));

        assertEquals(0, t1.compareTo(new Timeslot(1)));
        assertEquals(0, t2.compareTo(new Timeslot(2)));
        assertEquals(0, new Timeslot(10).compareTo(new Timeslot(10)));

        assertEquals(-1, t2.compareTo(t1));
        assertEquals(-1, t1.compareTo(new Timeslot(0)));
        assertEquals(-1, t2.compareTo(new Timeslot(0)));
        assertEquals(-1, new Timeslot(3).compareTo(t1));
        assertEquals(-1, new Timeslot(6).compareTo(t2));

        t1.compareTo(null);
    }

    @Test
    public void timeslotWithStartTest() {
        Timeslot t1 = new Timeslot(3, DayOfWeek.MONDAY);
        Timeslot t2 = new Timeslot(1, LocalDate.of(2016, 6, 26));
        Timeslot t3 = new Timeslot(20, LocalTime.NOON);
        Timeslot t4 = new Timeslot(7, LocalDateTime.of(LocalDate.ofYearDay(2016, 87), LocalTime.of(19, 15)));

        assertEquals(3, t1.getChronologicalOrder());
        assertEquals(1, t2.getChronologicalOrder());
        assertEquals(20, t3.getChronologicalOrder());
        assertEquals(7, t4.getChronologicalOrder());

        assertTrue(t1.getStart().isPresent());
        assertTrue(t2.getStart().isPresent());
        assertTrue(t3.getStart().isPresent());
        assertTrue(t4.getStart().isPresent());

        assertFalse(t1.getDuration().isPresent());
        assertFalse(t2.getDuration().isPresent());
        assertFalse(t3.getDuration().isPresent());
        assertFalse(t4.getDuration().isPresent());

        assertEquals(DayOfWeek.MONDAY, t1.getStart().get());
        assertEquals(LocalDate.of(2016, 6, 26), t2.getStart().get());
        assertEquals(LocalTime.of(12, 0), t3.getStart().get());
        assertEquals(LocalDateTime.of(2016, 3, 27, 19, 15), t4.getStart().get());

        assertEquals("Timeslot [order=3, start=MONDAY]", t1.getName());
        assertEquals("Timeslot [order=1, start=2016-06-26]", t2.getName());
        assertEquals("Timeslot [order=20, start=12:00]", t3.getName());
        assertEquals("Timeslot [order=7, start=2016-03-27T19:15]", t4.getName());
    }

    @Test
    public void timeslotWithStartCompareToTest() {
        Timeslot t = new Timeslot(5, LocalTime.of(16, 0));

        assertEquals(1, t.compareTo(new Timeslot(7, LocalTime.of(15, 30))));
        assertEquals(1, t.compareTo(new Timeslot(6, LocalTime.of(16, 0))));
        assertEquals(1, t.compareTo(new Timeslot(5, LocalTime.of(18, 0))));
        assertEquals(1, t.compareTo(new Timeslot(10, LocalDate.of(2016, 4, 4))));

        assertEquals(0, t.compareTo(new Timeslot(5, LocalTime.of(16, 0))));
        assertEquals(0, t.compareTo(new Timeslot(5, Year.of(2009))));

        assertEquals(-1, t.compareTo(new Timeslot(4, LocalTime.of(15, 30))));
        assertEquals(-1, t.compareTo(new Timeslot(2, LocalTime.of(16, 0))));
        assertEquals(-1, t.compareTo(new Timeslot(5, LocalTime.of(15, 50))));
        assertEquals(-1, t.compareTo(new Timeslot(1, DayOfWeek.SUNDAY)));

    }

    @Test
    public void timeslotWithDurationTest() {
        Timeslot t1 = new Timeslot(1, Duration.ofHours(1));
        Timeslot t2 = new Timeslot(7, Period.ofMonths(6));

        assertEquals(1, t1.getChronologicalOrder());
        assertEquals(7, t2.getChronologicalOrder());

        assertTrue(t1.getDuration().isPresent());
        assertTrue(t2.getDuration().isPresent());

        assertFalse(t1.getStart().isPresent());
        assertFalse(t2.getStart().isPresent());

        assertEquals(Duration.ofHours(1), t1.getDuration().get());
        assertEquals(Period.ofMonths(6), t2.getDuration().get());

        assertEquals("Timeslot [order=1, duration=PT1H]", t1.getName());
        assertEquals("Timeslot [order=7, duration=P6M]", t2.getName());
    }

    @Test
    public void timeslotWithDurationCompareToTest() {
        Timeslot t = new Timeslot(10, Duration.ofMinutes(90));

        assertEquals(1, t.compareTo(new Timeslot(15, Duration.ofMinutes(90))));
        assertEquals(-1, t.compareTo(new Timeslot(8, Period.ofDays(2))));
        assertEquals(0, t.compareTo(new Timeslot(10, Duration.ofMinutes(90))));
        assertEquals(0, t.compareTo(new Timeslot(10, Duration.ofMinutes(30))));
    }

    @Test
    public void timeslotWithStartAndDurationTest() {
        Timeslot t1 = new Timeslot(1, LocalTime.of(11, 0), Duration.ofHours(1));
        Timeslot t2 = new Timeslot(30, Month.APRIL, Period.ofDays(7));

        assertEquals(1, t1.getChronologicalOrder());
        assertEquals(30, t2.getChronologicalOrder());

        assertTrue(t1.getStart().isPresent());
        assertTrue(t2.getStart().isPresent());

        assertTrue(t1.getDuration().isPresent());
        assertTrue(t2.getDuration().isPresent());

        assertEquals(LocalTime.of(11, 0), t1.getStart().get());
        assertEquals(Duration.ofMinutes(60), t1.getDuration().get());
        assertEquals(Month.APRIL, t2.getStart().get());
        assertEquals(Period.ofDays(7), t2.getDuration().get());

        assertEquals("Timeslot [order=1, start=11:00, duration=PT1H]", t1.getName());
        assertEquals("Timeslot [order=30, start=APRIL, duration=P7D]", t2.getName());
    }

    @Test
    public void timeslotWithStartAndDurationCompareToTest() {
        Timeslot t = new Timeslot(4, Month.JUNE, Duration.ofDays(3));

        assertEquals(1, t.compareTo(new Timeslot(5, Month.JUNE, Duration.ofDays(2))));
        assertEquals(1, t.compareTo(new Timeslot(4, Month.JULY, Duration.ofDays(2))));

        assertEquals(0, t.compareTo(new Timeslot(4, Month.JUNE, Duration.ofDays(3))));
        assertEquals(0, t.compareTo(new Timeslot(4, Month.JUNE, Duration.ofDays(1))));
        assertEquals(0, t.compareTo(new Timeslot(4, LocalDate.of(2000, 1, 1), Duration.ofHours(12))));

        assertEquals(-1, t.compareTo(new Timeslot(3, Month.JULY, Period.ofMonths(1))));
        assertEquals(-1, t.compareTo(new Timeslot(4, Month.APRIL, Duration.ofDays(2))));
    }

    @Test
    public void mixedTimeslotsCompareToTest() {
        Timeslot t1 = new Timeslot(1);
        Timeslot t2 = new Timeslot(2, LocalTime.of(9, 0));
        Timeslot t3 = new Timeslot(3, Duration.ofHours(4));
        Timeslot t4 = new Timeslot(4, YearMonth.of(2016, 6));

        assertEquals(1, t1.compareTo(t2));
        assertEquals(1, t1.compareTo(t3));
        assertEquals(1, t1.compareTo(t4));
        assertEquals(0, t1.compareTo(new Timeslot(1)));
        assertEquals(-1, t1.compareTo(new Timeslot(0)));

        assertEquals(1, t2.compareTo(t3));
        assertEquals(1, t2.compareTo(t4));
        assertEquals(1, t2.compareTo(new Timeslot(2, LocalTime.of(10, 0))));
        assertEquals(0, t2.compareTo(new Timeslot(2, LocalTime.of(9, 0))));
        assertEquals(-1, t2.compareTo(new Timeslot(2, LocalTime.of(8, 0))));
        assertEquals(-1, t2.compareTo(t1));

        assertEquals(1, t3.compareTo(t4));
        assertEquals(0, t3.compareTo(new Timeslot(3)));
        assertEquals(0, t3.compareTo(new Timeslot(3, LocalDate.of(2016, 6, 14))));
        assertEquals(0, t3.compareTo(new Timeslot(3, LocalDate.of(2016, 6, 14), Duration.ofHours(3))));
        assertEquals(0, t3.compareTo(new Timeslot(3, Period.ofDays(10))));
        assertEquals(-1, t3.compareTo(t1));
        assertEquals(-1, t3.compareTo(t2));

        assertEquals(1, t4.compareTo(new Timeslot(5)));
        assertEquals(0, t4.compareTo(new Timeslot(4)));
        assertEquals(0, t4.compareTo(new Timeslot(4, Duration.ofHours(20))));
        assertEquals(0, t4.compareTo(new Timeslot(4, DayOfWeek.WEDNESDAY)));
        assertEquals(-1, t4.compareTo(t1));
        assertEquals(-1, t4.compareTo(t2));
        assertEquals(-1, t4.compareTo(t3));
    }

    @Test(expected = NullPointerException.class)
    public void withinFirstArgumentNullTest() {
        new Timeslot(10).within(null, new Timeslot(12));
    }

    @Test(expected = NullPointerException.class)
    public void withinSecondArgumentNullTest() {
        new Timeslot(1, LocalDate.of(2000, 8, 15)).within(new Timeslot(6), null);
    }

    @Test(expected = NullPointerException.class)
    public void withinArgumentsNullTest() {
        new Timeslot(39).within(null, null);
    }

    @Test
    public void withinTest() {
        List<Timeslot> timeslots = TournamentUtils.buildLocalTimeTimeslots(10);
        assertTrue(timeslots.get(3).within(timeslots.get(1), timeslots.get(5)));
        assertTrue(timeslots.get(7).within(timeslots.get(7), timeslots.get(9)));
        assertTrue(timeslots.get(1).within(timeslots.get(1), timeslots.get(0)));
        assertFalse(timeslots.get(2).within(timeslots.get(3), timeslots.get(5)));
        assertFalse(timeslots.get(0).within(timeslots.get(5), timeslots.get(3)));
        assertFalse(timeslots.get(8).within(timeslots.get(2), timeslots.get(6)));
        assertFalse(timeslots.get(9).within(timeslots.get(6), timeslots.get(8)));
        assertTrue(timeslots.get(3).within(timeslots.get(3), timeslots.get(3)));
        assertFalse(timeslots.get(4).within(timeslots.get(3), timeslots.get(3)));

        Timeslot noon = new Timeslot(1, LocalTime.NOON);
        Timeslot beforeNoon = new Timeslot(1, LocalTime.of(10, 0));
        Timeslot afterNoon = new Timeslot(1, LocalTime.of(14, 0));

        assertTrue(noon.within(beforeNoon, afterNoon));
        assertTrue(noon.within(afterNoon, beforeNoon));
        assertTrue(noon.within(noon, beforeNoon));
        assertTrue(noon.within(beforeNoon, noon));
        assertTrue(noon.within(noon, afterNoon));
        assertTrue(noon.within(afterNoon, noon));
    }
}
