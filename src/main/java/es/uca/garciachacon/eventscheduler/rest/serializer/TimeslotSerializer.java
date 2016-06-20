package es.uca.garciachacon.eventscheduler.rest.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import es.uca.garciachacon.eventscheduler.data.model.tournament.event.domain.Timeslot;

import java.io.IOException;
import java.time.*;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAmount;
import java.util.Optional;

public class TimeslotSerializer extends JsonSerializer<Timeslot> {
    @Override
    public void serialize(Timeslot value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();

        gen.writeStringField("name", value.getName());
        gen.writeNumberField("chronologicalOrder", value.getChronologicalOrder());

        Optional<TemporalAccessor> start = value.getStart();
        if (start.isPresent())
            writeStart(gen, start);

        Optional<TemporalAmount> duration = value.getDuration();
        if (duration.isPresent())
            writeDuration(gen, duration);

        gen.writeEndObject();
    }

    private void writeStart(JsonGenerator gen, Optional<TemporalAccessor> start) throws IOException {
        gen.writeObjectFieldStart("start");

        TemporalAccessor startValue = start.get();

        if (startValue instanceof DayOfWeek) {

            gen.writeStringField("type", "DayOfWeek");
            gen.writeStringField("value", startValue.toString());

        } else if (startValue instanceof Month) {

            gen.writeStringField("type", "Month");
            gen.writeStringField("value", startValue.toString());

        } else if (startValue instanceof MonthDay) {

            gen.writeStringField("type", "MonthDay");

            MonthDay monthDay = (MonthDay) startValue;
            gen.writeObjectFieldStart("value");
            gen.writeNumberField("month", monthDay.getMonthValue());
            gen.writeNumberField("dayOfMonth", monthDay.getDayOfMonth());
            gen.writeEndObject();

        } else if (startValue instanceof Year) {

            gen.writeStringField("type", "Year");
            gen.writeNumberField("value", ((Year) startValue).getValue());

        } else if (startValue instanceof YearMonth) {

            gen.writeStringField("type", "YearMonth");

            YearMonth yearMonth = (YearMonth) startValue;
            gen.writeObjectFieldStart("value");
            gen.writeNumberField("year", yearMonth.getYear());
            gen.writeNumberField("month", yearMonth.getMonthValue());
            gen.writeEndObject();

        } else if (startValue instanceof LocalTime) {

            gen.writeStringField("type", "LocalTime");
            gen.writeStringField("value", startValue.toString());

        } else if (startValue instanceof LocalDate) {

            gen.writeStringField("type", "LocalDate");
            gen.writeStringField("value", startValue.toString());

        } else if (startValue instanceof LocalDateTime) {

            gen.writeStringField("type", "LocalDateTime");
            gen.writeStringField("value", startValue.toString());

        } else {

            gen.writeStringField("type", startValue.getClass().getSimpleName());
            gen.writeObjectField("value", startValue);

        }

        gen.writeEndObject();
    }

    private void writeDuration(JsonGenerator gen, Optional<TemporalAmount> duration) throws IOException {
        gen.writeObjectFieldStart("duration");

        TemporalAmount durationValue = duration.get();

        if (durationValue instanceof Duration) {

            Duration d = (Duration) durationValue;
            long seconds = d.getSeconds();

            String type = "seconds";
            long val = seconds;

            if (d.getNano() != 0) {
                type = "milliseconds";
                val = d.toMillis();
            } else if (seconds != 0) {
                if (seconds % 3600 == 0) {
                    type = "hours";
                    val = d.toHours();
                } else if (seconds % 60 == 0) {
                    type = "minutes";
                    val = d.toMinutes();
                }
            }

            gen.writeStringField("type", type);
            gen.writeNumberField("value", val);

        } else if (durationValue instanceof Period) {

            Period p = (Period) durationValue;

            int days = p.getDays();
            int years = p.getYears();
            int months = p.getMonths();

            if (days != 0 && years == 0 && months == 0) {
                gen.writeStringField("type", "days");
                gen.writeObjectField("value", days);
            } else if (years == 0 && days == 0) {
                gen.writeStringField("type", "months");
                gen.writeObjectField("value", months);
            } else if (months == 0 && days == 0) {
                gen.writeStringField("type", "years");
                gen.writeObjectField("value", years);
            } else {
                gen.writeStringField("type", durationValue.getClass().getSimpleName());
                gen.writeObjectField("value", durationValue);
            }

        } else {

            String className = durationValue.getClass().getSimpleName();
            gen.writeStringField("type", className.isEmpty() ? durationValue.getClass().getName() : className);
            gen.writeObjectField("value", durationValue);

        }

        gen.writeEndObject();
    }
}
