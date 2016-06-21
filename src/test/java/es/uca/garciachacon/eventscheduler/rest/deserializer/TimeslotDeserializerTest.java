package es.uca.garciachacon.eventscheduler.rest.deserializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.uca.garciachacon.eventscheduler.data.model.tournament.event.domain.Timeslot;
import org.junit.Test;

import java.io.IOException;
import java.time.*;

import static org.junit.Assert.*;

/**
 * Tests de la clase de deserialización de <i>timeslots</i>, {@link TimeslotDeserializer}.
 */
public class TimeslotDeserializerTest {
    private Timeslot timeslot;
    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void deserializeSimpleTimeslotTest() throws IOException {
        timeslot = new Timeslot(2);

        Timeslot t = mapper.readValue(getJsonBody(), Timeslot.class);
        assertEquals(2, t.getChronologicalOrder());
        assertFalse(t.getStart().isPresent());
        assertFalse(t.getDuration().isPresent());
    }

    @Test
    public void deserializeDayOfWeekTest() throws IOException {
        timeslot = new Timeslot(1, DayOfWeek.TUESDAY);

        Timeslot t = mapper.readValue(getJsonBody(), Timeslot.class);
        assertEquals(DayOfWeek.TUESDAY, t.getStart().get());
        assertFalse(t.getDuration().isPresent());
    }

    @Test
    public void deserializeDayOfWeekIntTest() throws IOException {
        String json = "{\"chronologicalOrder\":1,\"start\":{\"type\":\"DayOfWeek\", \"value\":1}}";

        Timeslot t = mapper.readValue(json, Timeslot.class);
        assertEquals(DayOfWeek.MONDAY, t.getStart().get());
        assertFalse(t.getDuration().isPresent());
    }

    @Test
    public void deserializeMonthTest() throws IOException {
        timeslot = new Timeslot(1, Month.DECEMBER);

        Timeslot t = mapper.readValue(getJsonBody(), Timeslot.class);
        assertEquals(Month.DECEMBER, t.getStart().get());
        assertFalse(t.getDuration().isPresent());
    }

    @Test
    public void deserializeMonthIntTest() throws IOException {
        String json = "{\"chronologicalOrder\":1,\"start\":{\"type\":\"Month\", \"value\":12}}";

        Timeslot t = mapper.readValue(json, Timeslot.class);
        assertEquals(Month.DECEMBER, t.getStart().get());
        assertFalse(t.getDuration().isPresent());
    }

    @Test
    public void deserializeMonthDayTest() throws IOException {
        timeslot = new Timeslot(1, MonthDay.of(7, 26));

        Timeslot t = mapper.readValue(getJsonBody(), Timeslot.class);
        assertEquals(MonthDay.of(7, 26), t.getStart().get());
        assertFalse(t.getDuration().isPresent());
    }

    @Test
    public void deserializeYearTest() throws IOException {
        timeslot = new Timeslot(1, Year.of(1998));

        Timeslot t = mapper.readValue(getJsonBody(), Timeslot.class);
        assertEquals(Year.of(1998), t.getStart().get());
        assertFalse(t.getDuration().isPresent());
    }

    @Test
    public void deserializeYearMonthTest() throws IOException {
        timeslot = new Timeslot(1, YearMonth.of(2014, 10));

        Timeslot t = mapper.readValue(getJsonBody(), Timeslot.class);
        assertEquals(YearMonth.of(2014, 10), t.getStart().get());
        assertFalse(t.getDuration().isPresent());
    }

    @Test
    public void deserializeLocalTimeTest() throws IOException {
        timeslot = new Timeslot(1, LocalTime.of(17, 21));

        Timeslot t = mapper.readValue(getJsonBody(), Timeslot.class);
        assertEquals(LocalTime.of(17, 21), t.getStart().get());
        assertFalse(t.getDuration().isPresent());
    }

    @Test
    public void deserializeLocalTimeIntTest() throws IOException {
        String json = "{\"chronologicalOrder\":1,\"start\":{\"type\":\"LocalTime\", \"value\":{\"hour\":17," +
                "\"minute\":21}}}";

        Timeslot t = mapper.readValue(json, Timeslot.class);
        assertEquals(LocalTime.of(17, 21), t.getStart().get());
        assertFalse(t.getDuration().isPresent());
    }

    @Test
    public void deserializeLocalTimeIntWithSecondTest() throws IOException {
        String json = "{\"chronologicalOrder\":1,\"start\":{\"type\":\"LocalTime\", \"value\":{\"hour\":17," +
                "\"minute\":21,\"second\":39}}}";

        Timeslot t = mapper.readValue(json, Timeslot.class);
        assertEquals(LocalTime.of(17, 21, 39), t.getStart().get());
        assertFalse(t.getDuration().isPresent());
    }

    @Test
    public void deserializeLocalDateTest() throws IOException {
        timeslot = new Timeslot(1, LocalDate.of(2016, Month.JUNE, 21));

        Timeslot t = mapper.readValue(getJsonBody(), Timeslot.class);
        assertEquals(LocalDate.of(2016, Month.JUNE, 21), t.getStart().get());
        assertFalse(t.getDuration().isPresent());
    }

    @Test
    public void deserializeLocalDateIntTest() throws IOException {
        String json = "{\"chronologicalOrder\":1,\"start\":{\"type\":\"LocalDate\", \"value\":{\"year\":2016," +
                "\"month\":6,\"day\":21}}}";

        Timeslot t = mapper.readValue(json, Timeslot.class);
        assertEquals(LocalDate.of(2016, Month.JUNE, 21), t.getStart().get());
        assertFalse(t.getDuration().isPresent());
    }

    @Test
    public void deserializeLocalDateTimeTest() throws IOException {
        timeslot = new Timeslot(1, LocalDateTime.of(LocalDate.of(2016, Month.JUNE, 21), LocalTime.of(17, 26)));

        Timeslot t = mapper.readValue(getJsonBody(), Timeslot.class);
        assertEquals(LocalDateTime.of(LocalDate.of(2016, Month.JUNE, 21), LocalTime.of(17, 26)), t.getStart().get());
        assertFalse(t.getDuration().isPresent());
    }

    @Test
    public void deserializeLocalDateTimeFromPiecesTest() throws IOException {
        String json = "{\"chronologicalOrder\":1,\"start\":{\"type\":\"LocalDateTime\",\"value\":" +
                "{\"date\":\"2016-06-21\",\"time\":\"17:26\"}}}";

        Timeslot t = mapper.readValue(json, Timeslot.class);
        assertEquals(LocalDateTime.of(LocalDate.of(2016, Month.JUNE, 21), LocalTime.of(17, 26)), t.getStart().get());
        assertFalse(t.getDuration().isPresent());
    }

    @Test
    public void deserializeMillisecondsTest() throws IOException {
        String json = "{\"chronologicalOrder\":1,\"duration\":{\"type\":\"milliseconds\", \"value\":390}}";

        Timeslot t = mapper.readValue(json, Timeslot.class);
        assertEquals(Duration.ofMillis(390), t.getDuration().get());
        assertFalse(t.getStart().isPresent());
    }

    @Test
    public void deserializeSecondsTest() throws IOException {
        String json = "{\"chronologicalOrder\":1,\"duration\":{\"type\":\"seconds\", \"value\":120}}";

        Timeslot t = mapper.readValue(json, Timeslot.class);
        assertEquals(Duration.ofSeconds(120), t.getDuration().get());
        assertFalse(t.getStart().isPresent());
    }

    @Test
    public void deserializeMinutesTest() throws IOException {
        String json = "{\"chronologicalOrder\":1,\"duration\":{\"type\":\"minutes\", \"value\":45}}";

        Timeslot t = mapper.readValue(json, Timeslot.class);
        assertEquals(Duration.ofMinutes(45), t.getDuration().get());
        assertFalse(t.getStart().isPresent());
    }

    @Test
    public void deserializeHoursTest() throws IOException {
        String json = "{\"chronologicalOrder\":1,\"duration\":{\"type\":\"hours\", \"value\":1}}";

        Timeslot t = mapper.readValue(json, Timeslot.class);
        assertEquals(Duration.ofHours(1), t.getDuration().get());
        assertFalse(t.getStart().isPresent());
    }

    @Test
    public void deserializeDaysTest() throws IOException {
        String json = "{\"chronologicalOrder\":1,\"duration\":{\"type\":\"days\", \"value\":2}}";

        Timeslot t = mapper.readValue(json, Timeslot.class);
        assertEquals(Period.ofDays(2), t.getDuration().get());
        assertFalse(t.getStart().isPresent());
    }

    @Test
    public void deserializeWeeksTest() throws IOException {
        String json = "{\"chronologicalOrder\":1,\"duration\":{\"type\":\"weeks\", \"value\":2}}";

        Timeslot t = mapper.readValue(json, Timeslot.class);
        assertEquals(Period.ofDays(14), t.getDuration().get());
        assertFalse(t.getStart().isPresent());
    }

    @Test
    public void deserializeMonthsTest() throws IOException {
        String json = "{\"chronologicalOrder\":1,\"duration\":{\"type\":\"months\", \"value\":6}}";

        Timeslot t = mapper.readValue(json, Timeslot.class);
        assertEquals(Period.ofMonths(6), t.getDuration().get());
        assertFalse(t.getStart().isPresent());
    }

    @Test
    public void deserializeYearsTest() throws IOException {
        String json = "{\"chronologicalOrder\":1,\"duration\":{\"type\":\"years\", \"value\":3}}";

        Timeslot t = mapper.readValue(json, Timeslot.class);
        assertEquals(Period.ofYears(3), t.getDuration().get());
        assertFalse(t.getStart().isPresent());
    }

    @Test
    public void deserializeStartAndDuration() throws IOException {
        timeslot = new Timeslot(5, LocalDate.of(2013, 3, 18), Period.ofDays(3));

        Timeslot t = mapper.readValue(getJsonBody(), Timeslot.class);
        assertTrue(t.getStart().isPresent());
        assertTrue(t.getStart().isPresent());
        assertEquals(LocalDate.of(2013, 3, 18), t.getStart().get());
        assertEquals(Period.ofDays(3), t.getDuration().get());
    }

    private String getJsonBody() throws JsonProcessingException {
        return mapper.writeValueAsString(timeslot);
    }
}
