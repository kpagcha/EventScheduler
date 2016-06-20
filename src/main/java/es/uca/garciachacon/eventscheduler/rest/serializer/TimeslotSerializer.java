package es.uca.garciachacon.eventscheduler.rest.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import es.uca.garciachacon.eventscheduler.data.model.tournament.event.domain.Timeslot;

import java.io.IOException;
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
            gen.writeObjectField("start", start.get());

        Optional<TemporalAmount> duration = value.getDuration();
        if (duration.isPresent())
            gen.writeObjectField("duration", duration.get());

        gen.writeEndObject();
    }
}
