package es.uca.garciachacon.eventscheduler.rest.serializer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.uca.garciachacon.eventscheduler.data.model.tournament.event.domain.Timeslot;
import org.junit.Test;

import java.io.IOException;
import java.time.*;
import java.time.chrono.MinguoDate;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests de la clase de serialización de un <i>timeslot</i>, {@link TimeslotSerializer}.
 */
public class TimeslotSerializerTest {
    private Timeslot timeslot;
    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void serializeSimpleTimeslotTest() throws IOException {
        timeslot = new Timeslot(10);

        JsonNode node = getTimeslotNode();

        assertEquals("Timeslot [order=10]", node.get("name").asText());
        assertEquals(10, node.get("chronologicalOrder").asInt());
    }

    @Test
    public void serializeDayOfWeekTest() throws IOException {
        timeslot = new Timeslot(1, DayOfWeek.MONDAY);

        JsonNode node = getTimeslotNode();

        JsonNode startNode = node.get("start");
        assertEquals("DayOfWeek", startNode.get("type").asText());
        assertEquals(DayOfWeek.MONDAY, DayOfWeek.valueOf(startNode.get("value").asText()));
    }

    @Test
    public void serializeMonthTest() throws IOException {
        timeslot = new Timeslot(1, Month.JUNE);

        JsonNode node = getTimeslotNode();

        JsonNode startNode = node.get("start");
        assertEquals("Month", startNode.get("type").asText());
        assertEquals(Month.JUNE, Month.valueOf(startNode.get("value").asText()));
    }

    @Test
    public void serializeMonthDayTest() throws IOException {
        timeslot = new Timeslot(1, MonthDay.of(Month.APRIL, 24));

        JsonNode node = getTimeslotNode();

        JsonNode startNode = node.get("start");
        assertEquals("MonthDay", startNode.get("type").asText());
        assertEquals(Month.APRIL, Month.of(startNode.get("value").get("month").asInt()));
        assertEquals(24, startNode.get("value").get("dayOfMonth").asInt());
    }

    @Test
    public void serializeYearTest() throws IOException {
        timeslot = new Timeslot(1, Year.of(2016));

        JsonNode node = getTimeslotNode();

        JsonNode startNode = node.get("start");
        assertEquals("Year", startNode.get("type").asText());
        assertEquals(2016, startNode.get("value").asInt());
    }

    @Test
    public void serializeYearMonthTest() throws IOException {
        timeslot = new Timeslot(1, YearMonth.of(2017, Month.JANUARY));

        JsonNode node = getTimeslotNode();

        JsonNode startNode = node.get("start");
        assertEquals("YearMonth", startNode.get("type").asText());
        assertEquals(2017, startNode.get("value").get("year").asInt());
        assertEquals(Month.JANUARY, Month.of(startNode.get("value").get("month").asInt()));
    }

    @Test
    public void serializeLocalTimeTest() throws IOException {
        timeslot = new Timeslot(1, LocalTime.of(22, 24));

        JsonNode node = getTimeslotNode();

        JsonNode startNode = node.get("start");
        assertEquals("LocalTime", startNode.get("type").asText());
        assertEquals(LocalTime.of(22, 24), LocalTime.parse(startNode.get("value").asText()));
    }

    @Test
    public void serializeLocalDateTest() throws IOException {
        timeslot = new Timeslot(1, LocalDate.of(2000, 1, 1));

        JsonNode node = getTimeslotNode();

        JsonNode startNode = node.get("start");
        assertEquals("LocalDate", startNode.get("type").asText());
        assertEquals(LocalDate.of(2000, 1, 1), LocalDate.parse(startNode.get("value").asText()));
    }

    @Test
    public void serializeLocalDateTimeTest() throws IOException {
        timeslot = new Timeslot(1, LocalDateTime.of(LocalDate.of(2016, 6, 20), LocalTime.of(22, 27)));

        JsonNode node = getTimeslotNode();

        JsonNode startNode = node.get("start");
        assertEquals("LocalDateTime", startNode.get("type").asText());
        assertEquals(
                LocalDateTime.of(LocalDate.of(2016, 6, 20), LocalTime.of(22, 27)),
                LocalDateTime.parse(startNode.get("value").asText())
        );
    }

    @Test
    public void serializeOtherStartTest() throws IOException {
        timeslot = new Timeslot(1, MinguoDate.from(LocalDate.of(2016, 3, 7)));

        JsonNode node = getTimeslotNode();

        JsonNode startNode = node.get("start");
        assertEquals("MinguoDate", startNode.get("type").asText());
        assertNotNull(startNode.get("value"));
    }

    @Test
    public void serializeMillisecondsTest() throws IOException {
        timeslot = new Timeslot(1, Duration.ofMillis(1500));

        JsonNode node = getTimeslotNode();

        JsonNode durationNode = node.get("duration");
        assertEquals("milliseconds", durationNode.get("type").asText());
        assertEquals(1500, durationNode.get("value").asInt());


        timeslot = new Timeslot(1, Duration.ofMillis(20));

        node = getTimeslotNode();

        durationNode = node.get("duration");
        assertEquals("milliseconds", durationNode.get("type").asText());
        assertEquals(20, durationNode.get("value").asInt());

        timeslot = new Timeslot(1, Duration.ofMillis(134090));

        node = getTimeslotNode();

        durationNode = node.get("duration");
        assertEquals("milliseconds", durationNode.get("type").asText());
        assertEquals(134090, durationNode.get("value").asInt());
    }

    @Test
    public void serializeSecondsTest() throws IOException {
        timeslot = new Timeslot(1, Duration.ofSeconds(30));

        JsonNode node = getTimeslotNode();

        JsonNode durationNode = node.get("duration");
        assertEquals("seconds", durationNode.get("type").asText());
        assertEquals(30, durationNode.get("value").asInt());


        timeslot = new Timeslot(1, Duration.ofSeconds(340));

        node = getTimeslotNode();

        durationNode = node.get("duration");
        assertEquals("seconds", durationNode.get("type").asText());
        assertEquals(340, durationNode.get("value").asInt());
    }

    @Test
    public void serializeMinutesTest() throws IOException {
        timeslot = new Timeslot(1, Duration.ofMinutes(25));

        JsonNode node = getTimeslotNode();

        JsonNode durationNode = node.get("duration");
        assertEquals("minutes", durationNode.get("type").asText());
        assertEquals(25, durationNode.get("value").asInt());


        timeslot = new Timeslot(1, Duration.ofMinutes(90));

        node = getTimeslotNode();

        durationNode = node.get("duration");
        assertEquals("minutes", durationNode.get("type").asText());
        assertEquals(90, durationNode.get("value").asInt());


        timeslot = new Timeslot(1, Duration.ofSeconds(120));

        node = getTimeslotNode();

        durationNode = node.get("duration");
        assertEquals("minutes", durationNode.get("type").asText());
        assertEquals(2, durationNode.get("value").asInt());
    }

    @Test
    public void serializeHoursTest() throws IOException {
        timeslot = new Timeslot(1, Duration.ofHours(1));

        JsonNode node = getTimeslotNode();

        JsonNode durationNode = node.get("duration");
        assertEquals("hours", durationNode.get("type").asText());
        assertEquals(1, durationNode.get("value").asInt());


        timeslot = new Timeslot(1, Duration.ofHours(24));

        node = getTimeslotNode();

        durationNode = node.get("duration");
        assertEquals("hours", durationNode.get("type").asText());
        assertEquals(24, durationNode.get("value").asInt());


        timeslot = new Timeslot(1, Duration.ofMinutes(180));

        node = getTimeslotNode();

        durationNode = node.get("duration");
        assertEquals("hours", durationNode.get("type").asText());
        assertEquals(3, durationNode.get("value").asInt());


        timeslot = new Timeslot(1, Duration.ofSeconds(36000));

        node = getTimeslotNode();

        durationNode = node.get("duration");
        assertEquals("hours", durationNode.get("type").asText());
        assertEquals(10, durationNode.get("value").asInt());
    }

    @Test
    public void serializeDaysTest() throws IOException {
        timeslot = new Timeslot(1, Period.ofDays(1));

        JsonNode node = getTimeslotNode();

        JsonNode durationNode = node.get("duration");
        assertEquals("days", durationNode.get("type").asText());
        assertEquals(1, durationNode.get("value").asInt());


        timeslot = new Timeslot(1, Period.ofWeeks(2));

        node = getTimeslotNode();

        durationNode = node.get("duration");
        assertEquals("days", durationNode.get("type").asText());
        assertEquals(14, durationNode.get("value").asInt());
    }

    @Test
    public void serializeMonthsTest() throws IOException {
        timeslot = new Timeslot(1, Period.ofMonths(3));

        JsonNode node = getTimeslotNode();

        JsonNode durationNode = node.get("duration");
        assertEquals("months", durationNode.get("type").asText());
        assertEquals(3, durationNode.get("value").asInt());
    }

    @Test
    public void serializeYearsTest() throws IOException {
        timeslot = new Timeslot(1, Period.ofYears(2));

        JsonNode node = getTimeslotNode();

        JsonNode durationNode = node.get("duration");
        assertEquals("years", durationNode.get("type").asText());
        assertEquals(2, durationNode.get("value").asInt());
    }

    @Test
    public void serializeOtherPeriodTest() throws IOException {
        timeslot = new Timeslot(1, Period.of(0, 1, 10));

        JsonNode node = getTimeslotNode();

        JsonNode durationNode = node.get("duration");
        assertEquals("Period", durationNode.get("type").asText());
        assertNotNull(durationNode.get("value"));
    }

    @Test
    public void serializeOtherDurationTest() throws IOException {
        timeslot = new Timeslot(1, new TemporalAmount() {
            public long get(TemporalUnit unit) {
                return 0;
            }

            public List<TemporalUnit> getUnits() {
                return null;
            }

            public Temporal addTo(Temporal temporal) {
                return null;
            }

            public Temporal subtractFrom(Temporal temporal) {
                return null;
            }
        });

        JsonNode node = getTimeslotNode();

        JsonNode durationNode = node.get("duration");
        assertEquals(
                "es.uca.garciachacon.eventscheduler.rest.serializer.TimeslotSerializerTest$1",
                durationNode.get("type").asText()
        );
        assertNotNull(durationNode.get("value"));
    }

    @Test
    public void serializeStartAndDurationTest() throws IOException {
        timeslot = new Timeslot(1, LocalTime.of(12, 6), Duration.ofHours(2));

        JsonNode node = getTimeslotNode();

        JsonNode startNode = node.get("start");
        JsonNode durationNode = node.get("duration");

        assertNotNull(startNode);
        assertNotNull(durationNode);
    }

    private JsonNode getTimeslotNode() throws IOException {
        return mapper.readTree(mapper.writeValueAsString(timeslot));
    }
}
