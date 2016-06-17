package es.uca.garciachacon.eventscheduler.rest.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import es.uca.garciachacon.eventscheduler.data.model.tournament.event.domain.Timeslot;

import java.io.IOException;
import java.time.*;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAmount;

public class TimeslotDeserializer extends JsonDeserializer<Timeslot> {
    @Override
    public Timeslot deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);

        Timeslot timeslot;

        int chronologicalOrder = Integer.parseInt(node.get("chronologicalOrder").asText());
        JsonNode startNode = node.get("start");
        JsonNode durationNode = node.get("duration");

        TemporalAccessor start = null;
        TemporalAmount duration = null;

        if (startNode != null) {
            JsonNode startTypeNode = startNode.get("type");
            String startType = startTypeNode.isTextual() ? startTypeNode.asText() : null;

            JsonNode valueNode = startNode.get("value");

            switch (startType) {
                case "DayOfWeek":
                    if (valueNode.isInt())
                        start = DayOfWeek.of(valueNode.asInt());
                    else if (valueNode.isTextual())
                        start = DayOfWeek.valueOf(valueNode.asText());
                    break;

                case "Month":
                    if (valueNode.isInt())
                        start = Month.of(valueNode.asInt());
                    else if (valueNode.isTextual())
                        start = Month.valueOf(valueNode.asText());
                    break;

                case "MonthDay":
                    start = MonthDay.of(Integer.parseInt(valueNode.get("month").asText()),
                            Integer.parseInt(valueNode.get("dayOfMonth").asText())
                    );
                    break;

                case "Year":
                    start = Year.of(Integer.parseInt(valueNode.get("year").asText()));
                    break;

                case "YearMonth":
                    start = YearMonth.of(Integer.parseInt(valueNode.get("year").asText()),
                            Integer.parseInt(valueNode.get("month").asText())
                    );
                    break;

                case "LocalTime":
                    start = parseTime(valueNode);
                    break;

                case "LocalDate":
                    start = parseDate(valueNode);
                    break;

                case "LocalDateTime":
                    if (valueNode.isTextual())
                        start = LocalDateTime.parse(valueNode.asText());
                    else
                        start = LocalDateTime.of((LocalDate) parseDate(valueNode.get("date")),
                                (LocalTime) parseTime(valueNode.get("time"))
                        );
            }
        }

        if (durationNode != null) {
            JsonNode durationTypeNode = durationNode.get("type");
            String durationType = durationTypeNode.isTextual() ? durationTypeNode.asText() : null;

            JsonNode valueNode = durationNode.get("value");

            long longValue = Long.parseLong(valueNode.asText());

            switch (durationType) {

                case "milliseconds":
                    duration = Duration.ofMillis(longValue);
                    break;
                case "seconds":
                    duration = Duration.ofSeconds(longValue);
                    break;
                case "minutes":
                    duration = Duration.ofMinutes(longValue);
                    break;
                case "hours":
                    duration = Duration.ofHours(longValue);
                    break;
                case "days":
                    duration = Duration.ofDays(longValue);
                    break;
                case "weeks":
                    duration = Period.ofWeeks(Math.toIntExact(longValue));
                    break;
                case "months":
                    duration = Period.ofMonths(Math.toIntExact(longValue));
                    break;
                case "years":
                    duration = Period.ofYears(Math.toIntExact(longValue));
            }
        }

        if (start == null && duration == null)
            timeslot = new Timeslot(chronologicalOrder);
        else if (duration == null)
            timeslot = new Timeslot(chronologicalOrder, start);
        else if (start == null)
            timeslot = new Timeslot(chronologicalOrder, duration);
        else
            timeslot = new Timeslot(chronologicalOrder, start, duration);

        JsonNode nameNode = node.get("name");
        if (nameNode != null) {
            String name = nameNode.isTextual() ? "" : nameNode.asText();
            if (!name.isEmpty())
                timeslot.setName(name);
        }

        return timeslot;
    }

    private TemporalAccessor parseTime(JsonNode node) {
        TemporalAccessor start;
        if (node.isTextual())
            start = LocalTime.parse(node.asText());
        else {
            int second = 0;
            JsonNode valueSecondNode = node.path("second");
            if (valueSecondNode.isInt())
                second = valueSecondNode.asInt();

            start = LocalTime.of(Integer.parseInt(node.get("hour").asText()),
                    Integer.parseInt(node.get("minute").asText()),
                    second
            );
        }
        return start;
    }

    private TemporalAccessor parseDate(JsonNode node) {
        TemporalAccessor start;
        if (node.isTextual())
            start = LocalDate.parse(node.asText());
        else {
            start = LocalDate.of(Integer.parseInt(node.get("year").asText()),
                    Integer.parseInt(node.get("month").asText()),
                    Integer.parseInt(node.get("day").asText())
            );
        }
        return start;
    }
}