package es.uca.garciachacon.eventscheduler.rest.deserializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import es.uca.garciachacon.eventscheduler.data.model.tournament.Timeslot;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.time.*;

import static org.junit.Assert.*;

/**
 * Tests de la clase de deserialización de <i>timeslots</i>, {@link TimeslotDeserializer}.
 */
public class TimeslotDeserializerTest {
    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    private Timeslot timeslot;
    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void deserializeMissingChronologicalOrderTest() throws IOException {
        timeslot = new Timeslot(1);

        JsonNode root = getJsonNode();
        ((ObjectNode) root).remove("chronologicalOrder");

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Expected \"chronologicalOrder\" integer field");
        mapper.readValue(mapper.writeValueAsString(root), Timeslot.class);
    }

    @Test
    public void deserializeNonTextualNameTest() throws IOException {
        timeslot = new Timeslot(1);

        JsonNode root = getJsonNode();
        ((ObjectNode) root).put("name", 10);

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Field \"name\" expected to be textual");
        mapper.readValue(mapper.writeValueAsString(root), Timeslot.class);
    }

    @Test
    public void deserializeSimpleTimeslotTest() throws IOException {
        timeslot = new Timeslot(2);

        Timeslot t = mapper.readValue(getJsonBody(), Timeslot.class);
        assertEquals(2, t.getChronologicalOrder());
        assertFalse(t.getStart().isPresent());
        assertFalse(t.getDuration().isPresent());
    }

    @Test
    public void deserializeNonObjectStartTest() throws IOException {
        timeslot = new Timeslot(1, DayOfWeek.TUESDAY);

        JsonNode root = getJsonNode();
        ((ObjectNode) root).put("start", "not an object");

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Field \"start\" expected to be an object");
        mapper.readValue(mapper.writeValueAsString(root), Timeslot.class);
    }

    @Test
    public void deserializeNonTextualStartTypeTest() throws IOException {
        timeslot = new Timeslot(1, DayOfWeek.TUESDAY);

        JsonNode root = getJsonNode();
        ((ObjectNode) root.get("start")).putArray("type");

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Expected \"type\" textual field");
        mapper.readValue(mapper.writeValueAsString(root), Timeslot.class);
    }

    @Test
    public void deserializeMissingStartValueTest() throws IOException {
        timeslot = new Timeslot(1, DayOfWeek.TUESDAY);

        JsonNode root = getJsonNode();
        ((ObjectNode) root.get("start")).remove("value");

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Expected \"value\" node");
        mapper.readValue(mapper.writeValueAsString(root), Timeslot.class);
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
    public void deserializeNonIntegerOrTextualDayOfWeekTest() throws IOException {
        timeslot = new Timeslot(1, DayOfWeek.TUESDAY);

        JsonNode root = getJsonNode();
        ((ObjectNode) root.get("start")).putArray("value");

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Field \"value\" expected to be an integer or a string");
        mapper.readValue(mapper.writeValueAsString(root), Timeslot.class);
    }

    @Test
    public void deserializeInvalidDayOfWeekTextualTest() throws IOException {
        timeslot = new Timeslot(1, DayOfWeek.TUESDAY);

        JsonNode root = getJsonNode();
        ((ObjectNode) root.get("start")).put("value", "JANUARY");

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Invalid day of week");
        mapper.readValue(mapper.writeValueAsString(root), Timeslot.class);
    }

    @Test
    public void deserializeInvalidDayOfWeekIntegerTest() throws IOException {
        timeslot = new Timeslot(1, DayOfWeek.TUESDAY);

        JsonNode root = getJsonNode();
        ((ObjectNode) root.get("start")).put("value", 10);

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Invalid day of week");
        mapper.readValue(mapper.writeValueAsString(root), Timeslot.class);
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
    public void deserializeNonIntegerOrTextualMonthTest() throws IOException {
        timeslot = new Timeslot(1, Month.JUNE);

        JsonNode root = getJsonNode();
        ((ObjectNode) root.get("start")).putArray("value");

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Field \"value\" expected to be an integer or a string");
        mapper.readValue(mapper.writeValueAsString(root), Timeslot.class);
    }

    @Test
    public void deserializeInvalidMonthTextualTest() throws IOException {
        timeslot = new Timeslot(1, Month.JUNE);

        JsonNode root = getJsonNode();
        ((ObjectNode) root.get("start")).put("value", "WEDNESDAY");

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Invalid month");
        mapper.readValue(mapper.writeValueAsString(root), Timeslot.class);
    }

    @Test
    public void deserializeInvalidMonthIntegerTest() throws IOException {
        timeslot = new Timeslot(1, Month.JUNE);

        JsonNode root = getJsonNode();
        ((ObjectNode) root.get("start")).put("value", 13);

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Invalid month");
        mapper.readValue(mapper.writeValueAsString(root), Timeslot.class);
    }

    @Test
    public void deserializeMonthDayTest() throws IOException {
        timeslot = new Timeslot(1, MonthDay.of(7, 26));

        Timeslot t = mapper.readValue(getJsonBody(), Timeslot.class);
        assertEquals(MonthDay.of(7, 26), t.getStart().get());
        assertFalse(t.getDuration().isPresent());
    }

    @Test
    public void deserializeNonObjectMonthDayTest() throws IOException {
        timeslot = new Timeslot(1, MonthDay.of(8, 6));

        JsonNode root = getJsonNode();
        ((ObjectNode) root.get("start")).put("value", 12);

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Field \"value\" expected to be an object");
        mapper.readValue(mapper.writeValueAsString(root), Timeslot.class);
    }

    @Test
    public void deserializeMonthDayNonIntegerMonthTest() throws IOException {
        timeslot = new Timeslot(1, MonthDay.of(8, 6));

        JsonNode root = getJsonNode();
        System.out.println(mapper.writeValueAsString(root));
        ((ObjectNode) root.get("start").get("value")).put("month", "non integer");

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Expected \"month\" integer field");
        mapper.readValue(mapper.writeValueAsString(root), Timeslot.class);
    }

    @Test
    public void deserializeMonthDayNonIntegerDayOfMonthTest() throws IOException {
        timeslot = new Timeslot(1, MonthDay.of(8, 6));

        JsonNode root = getJsonNode();
        ((ObjectNode) root.get("start").get("value")).put("dayOfMonth", "non integer");

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Expected \"dayOfMonth\" integer field");
        mapper.readValue(mapper.writeValueAsString(root), Timeslot.class);
    }

    @Test
    public void deserializeInvalidMonthDayTest() throws IOException {
        timeslot = new Timeslot(1, MonthDay.of(8, 6));

        JsonNode root = getJsonNode();
        ((ObjectNode) root.get("start").get("value")).put("dayOfMonth", 32);

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Invalid month-day");
        mapper.readValue(mapper.writeValueAsString(root), Timeslot.class);
    }

    @Test
    public void deserializeYearTest() throws IOException {
        timeslot = new Timeslot(1, Year.of(1998));

        Timeslot t = mapper.readValue(getJsonBody(), Timeslot.class);
        assertEquals(Year.of(1998), t.getStart().get());
        assertFalse(t.getDuration().isPresent());
    }

    @Test
    public void deserializeNonIntegerYearTest() throws IOException {
        timeslot = new Timeslot(1, Year.of(2006));

        JsonNode root = getJsonNode();
        ((ObjectNode) root.get("start")).put("value", "Two thousand and six");

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Field \"value\" expected to be an integer");
        mapper.readValue(mapper.writeValueAsString(root), Timeslot.class);
    }

    @Test
    public void deserializeInvalidYearTest() throws IOException {
        timeslot = new Timeslot(1, Year.of(2006));

        JsonNode root = getJsonNode();
        ((ObjectNode) root.get("start")).put("value", Year.MAX_VALUE + 1);

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Invalid year");
        mapper.readValue(mapper.writeValueAsString(root), Timeslot.class);
    }

    @Test
    public void deserializeYearMonthTest() throws IOException {
        timeslot = new Timeslot(1, YearMonth.of(2014, 10));

        Timeslot t = mapper.readValue(getJsonBody(), Timeslot.class);
        assertEquals(YearMonth.of(2014, 10), t.getStart().get());
        assertFalse(t.getDuration().isPresent());
    }

    @Test
    public void deserializeNonObjectYearMonthTest() throws IOException {
        timeslot = new Timeslot(1, YearMonth.of(2007, 7));

        JsonNode root = getJsonNode();
        ((ObjectNode) root.get("start")).put("value", 200);

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Field \"value\" expected to be an object");
        mapper.readValue(mapper.writeValueAsString(root), Timeslot.class);
    }

    @Test
    public void deserializeYearMonthNonIntegerYearTest() throws IOException {
        timeslot = new Timeslot(1, YearMonth.of(2007, 7));

        JsonNode root = getJsonNode();
        ((ObjectNode) root.get("start").get("value")).put("year", "a year");

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Expected \"year\" integer field");
        mapper.readValue(mapper.writeValueAsString(root), Timeslot.class);
    }

    @Test
    public void deserializeYearMonthNonIntegerMonthTest() throws IOException {
        timeslot = new Timeslot(1, YearMonth.of(2007, 7));

        JsonNode root = getJsonNode();
        ((ObjectNode) root.get("start").get("value")).put("month", "a month");

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Expected \"month\" integer field");
        mapper.readValue(mapper.writeValueAsString(root), Timeslot.class);
    }

    @Test
    public void deserializeInvalidYearMonthTest() throws IOException {
        timeslot = new Timeslot(1, YearMonth.of(2007, 7));

        JsonNode root = getJsonNode();
        ((ObjectNode) root.get("start").get("value")).put("month", 0);

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Invalid year-month");
        mapper.readValue(mapper.writeValueAsString(root), Timeslot.class);
    }

    @Test
    public void deserializeLocalTimeTest() throws IOException {
        timeslot = new Timeslot(1, LocalTime.of(17, 21));

        Timeslot t = mapper.readValue(getJsonBody(), Timeslot.class);
        assertEquals(LocalTime.of(17, 21), t.getStart().get());
        assertFalse(t.getDuration().isPresent());
    }

    @Test
    public void deserializeNonTextualOrObjectLocalTimeTest() throws IOException {
        timeslot = new Timeslot(1, LocalTime.of(17, 42));

        JsonNode root = getJsonNode();
        ((ObjectNode) root.get("start")).put("value", 0);

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Time value expected to be textual or an object");
        mapper.readValue(mapper.writeValueAsString(root), Timeslot.class);
    }

    @Test
    public void deserializeInvalidLocalTimeParseTest() throws IOException {
        timeslot = new Timeslot(1, LocalTime.of(17, 42));

        JsonNode root = getJsonNode();
        ((ObjectNode) root.get("start")).put("value", "18:60");

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Cannot parse time");
        mapper.readValue(mapper.writeValueAsString(root), Timeslot.class);
    }

    @Test
    public void deserializeLocalTimeNonIntegerHourTest() throws IOException {
        timeslot = new Timeslot(1, LocalTime.of(17, 42));

        JsonNode root = getJsonNode();
        ObjectNode timeNode = mapper.createObjectNode();
        timeNode.put("hour", "an hour");
        timeNode.put("minute", 31);

        ((ObjectNode) root.get("start")).set("value", timeNode);

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Expected \"hour\" integer field");
        mapper.readValue(mapper.writeValueAsString(root), Timeslot.class);
    }

    @Test
    public void deserializeLocalTimeNonIntegerMinuteTest() throws IOException {
        timeslot = new Timeslot(1, LocalTime.of(17, 42));

        JsonNode root = getJsonNode();
        ObjectNode timeNode = mapper.createObjectNode();
        timeNode.put("hour", 12);
        timeNode.put("minute", "50");

        ((ObjectNode) root.get("start")).set("value", timeNode);

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Expected \"minute\" integer field");
        mapper.readValue(mapper.writeValueAsString(root), Timeslot.class);
    }

    @Test
    public void deserializeInvalidLocalTimeTest() throws IOException {
        timeslot = new Timeslot(1, LocalTime.of(17, 42));

        JsonNode root = getJsonNode();
        ObjectNode timeNode = mapper.createObjectNode();
        timeNode.put("hour", 19);
        timeNode.put("minute", -1);

        ((ObjectNode) root.get("start")).set("value", timeNode);

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Invalid time");
        mapper.readValue(mapper.writeValueAsString(root), Timeslot.class);
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
    public void deserializeNonTextualOrObjectLocalDateTest() throws IOException {
        timeslot = new Timeslot(1, LocalDate.of(2016, 6, 23));

        JsonNode root = getJsonNode();
        ((ObjectNode) root.get("start")).put("value", 19);

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Date value expected to be textual or an object");
        mapper.readValue(mapper.writeValueAsString(root), Timeslot.class);
    }

    @Test
    public void deserializeInvalidLocalDateParseTest() throws IOException {
        timeslot = new Timeslot(1, LocalDate.of(2016, 6, 23));

        JsonNode root = getJsonNode();
        ((ObjectNode) root.get("start")).put("value", "2016-06-32");

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Cannot parse date");
        mapper.readValue(mapper.writeValueAsString(root), Timeslot.class);
    }

    @Test
    public void deserializeLocalDateNonIntegerYearTest() throws IOException {
        timeslot = new Timeslot(1, LocalDate.of(2016, 6, 23));

        JsonNode root = getJsonNode();
        ObjectNode dateNode = mapper.createObjectNode();
        dateNode.put("year", "2016");
        dateNode.put("month", 6);
        dateNode.put("day", 23);

        ((ObjectNode) root.get("start")).set("value", dateNode);

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Expected \"year\" integer field");
        mapper.readValue(mapper.writeValueAsString(root), Timeslot.class);
    }

    @Test
    public void deserializeLocalDateNonIntegerMonthTest() throws IOException {
        timeslot = new Timeslot(1, LocalDate.of(2016, 6, 23));

        JsonNode root = getJsonNode();
        ObjectNode dateNode = mapper.createObjectNode();
        dateNode.put("year", 2016);
        dateNode.put("month", "6");
        dateNode.put("day", 23);

        ((ObjectNode) root.get("start")).set("value", dateNode);

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Expected \"month\" integer field");
        mapper.readValue(mapper.writeValueAsString(root), Timeslot.class);
    }

    @Test
    public void deserializeLocalDateNonIntegerDayTest() throws IOException {
        timeslot = new Timeslot(1, LocalDate.of(2016, 6, 23));

        JsonNode root = getJsonNode();
        ObjectNode dateNode = mapper.createObjectNode();
        dateNode.put("year", 2016);
        dateNode.put("month", 6);
        dateNode.put("day", "23");

        ((ObjectNode) root.get("start")).set("value", dateNode);

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Expected \"day\" integer field");
        mapper.readValue(mapper.writeValueAsString(root), Timeslot.class);
    }

    @Test
    public void deserializeInvalidLocalDateTest() throws IOException {
        timeslot = new Timeslot(1, LocalDate.of(2016, 6, 23));

        JsonNode root = getJsonNode();
        ObjectNode dateNode = mapper.createObjectNode();
        dateNode.put("year", 2016);
        dateNode.put("month", 14);
        dateNode.put("day", 23);

        ((ObjectNode) root.get("start")).set("value", dateNode);

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Invalid date");
        mapper.readValue(mapper.writeValueAsString(root), Timeslot.class);
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
    public void deserializeNonTextualOrObjectLocalDateTimeTest() throws IOException {
        timeslot = new Timeslot(1, LocalDateTime.of(LocalDate.of(2016, 6, 23), LocalTime.of(11, 56)));

        JsonNode root = getJsonNode();
        ((ObjectNode) root.get("start")).put("value", 1.993);

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Field \"value\" expected to be textual or an object");
        mapper.readValue(mapper.writeValueAsString(root), Timeslot.class);
    }

    @Test
    public void deserializeInvalidLocalDateTimeParseTest() throws IOException {
        timeslot = new Timeslot(1, LocalDateTime.of(LocalDate.of(2016, 6, 23), LocalTime.of(11, 56)));

        JsonNode root = getJsonNode();
        ((ObjectNode) root.get("start")).put("value", "2016-06-23T11:66");

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Cannot parse date-time");
        mapper.readValue(mapper.writeValueAsString(root), Timeslot.class);
    }

    @Test
    public void deserializeLocalDateTimeMissingDateTest() throws IOException {
        timeslot = new Timeslot(1, LocalDateTime.of(LocalDate.of(2016, 6, 23), LocalTime.of(11, 56)));

        JsonNode root = getJsonNode();
        ObjectNode valueNode = mapper.createObjectNode();
        valueNode.put("time", "9:55:14");

        ((ObjectNode) root.get("start")).set("value", valueNode);

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Expected date value");
        mapper.readValue(mapper.writeValueAsString(root), Timeslot.class);
    }

    @Test
    public void deserializeLocalDateTimeMissingTimeTest() throws IOException {
        timeslot = new Timeslot(1, LocalDateTime.of(LocalDate.of(2016, 6, 23), LocalTime.of(11, 56)));

        JsonNode root = getJsonNode();
        ObjectNode valueNode = mapper.createObjectNode();
        valueNode.put("date", "2016-06-23");

        ((ObjectNode) root.get("start")).set("value", valueNode);

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Expected time value");
        mapper.readValue(mapper.writeValueAsString(root), Timeslot.class);
    }

    @Test
    public void deserializeUnknownStartTypeTest() throws IOException {
        timeslot = new Timeslot(1, DayOfWeek.THURSDAY);

        JsonNode root = getJsonNode();
        ((ObjectNode) root.get("start")).put("type", "WeekTime");

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Unknown start type");
        mapper.readValue(mapper.writeValueAsString(root), Timeslot.class);
    }

    @Test
    public void deserializeNonObjectDurationTest() throws IOException {
        timeslot = new Timeslot(1, Duration.ofMinutes(30));

        JsonNode root = getJsonNode();
        ((ObjectNode) root).put("duration", "not an object");

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Field \"duration\" expected to be an object");
        mapper.readValue(mapper.writeValueAsString(root), Timeslot.class);
    }

    @Test
    public void deserializeNonTextualDurationTypeTest() throws IOException {
        timeslot = new Timeslot(1, Duration.ofMinutes(30));

        JsonNode root = getJsonNode();
        ((ObjectNode) root.get("duration")).put("type", 3);

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Expected \"type\" textual field");
        mapper.readValue(mapper.writeValueAsString(root), Timeslot.class);
    }

    @Test
    public void deserializeNonIntegerDurationValueTest() throws IOException {
        timeslot = new Timeslot(1, Duration.ofMinutes(30));

        JsonNode root = getJsonNode();
        ((ObjectNode) root.get("duration")).put("value", "duration value");

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Expected \"value\" integer field");
        mapper.readValue(mapper.writeValueAsString(root), Timeslot.class);
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
    public void deserializeInvalidMinutesTest() throws IOException {
        timeslot = new Timeslot(1, Duration.ofMinutes(30));

        JsonNode root = getJsonNode();
        ((ObjectNode) root.get("duration")).put("value", Long.MAX_VALUE);

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Invalid minutes");
        mapper.readValue(mapper.writeValueAsString(root), Timeslot.class);
    }

    @Test
    public void deserializeHoursTest() throws IOException {
        String json = "{\"chronologicalOrder\":1,\"duration\":{\"type\":\"hours\", \"value\":1}}";

        Timeslot t = mapper.readValue(json, Timeslot.class);
        assertEquals(Duration.ofHours(1), t.getDuration().get());
        assertFalse(t.getStart().isPresent());
    }

    @Test
    public void deserializeInvalidHoursTest() throws IOException {
        timeslot = new Timeslot(1, Duration.ofHours(1));

        JsonNode root = getJsonNode();
        ((ObjectNode) root.get("duration")).put("value", Long.MAX_VALUE);

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Invalid hours");
        mapper.readValue(mapper.writeValueAsString(root), Timeslot.class);
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
    public void deserializeDurationTooBigTest() throws IOException {
        timeslot = new Timeslot(1, Period.ofMonths(4));

        JsonNode root = getJsonNode();
        ((ObjectNode) root.get("duration")).put("value", (long) Integer.MAX_VALUE + 1);

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Duration value too big");
        mapper.readValue(mapper.writeValueAsString(root), Timeslot.class);
    }

    @Test
    public void deserializeUnknownDurationTest() throws IOException {
        timeslot = new Timeslot(1, Duration.ofMinutes(5));

        JsonNode root = getJsonNode();
        ((ObjectNode) root.get("duration")).put("type", "NANOSECONDS");

        expectedEx.expect(MalformedJsonException.class);
        expectedEx.expectMessage("Unknown duration type");
        mapper.readValue(mapper.writeValueAsString(root), Timeslot.class);
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

    private JsonNode getJsonNode() throws IOException {
        return mapper.readTree(getJsonBody());
    }
}
