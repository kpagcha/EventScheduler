package es.uca.garciachacon.eventscheduler.rest.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import es.uca.garciachacon.eventscheduler.data.model.schedule.Match;
import es.uca.garciachacon.eventscheduler.data.model.schedule.Schedule;
import es.uca.garciachacon.eventscheduler.data.model.schedule.value.AbstractScheduleValue;
import es.uca.garciachacon.eventscheduler.data.model.tournament.Localization;
import es.uca.garciachacon.eventscheduler.data.model.tournament.Player;
import es.uca.garciachacon.eventscheduler.data.model.tournament.Timeslot;

import java.io.IOException;

/**
 * Serializador de un horario {@link Schedule}, específicamente de las clases que extienden a este clase abstracta.
 */
public class ScheduleSerializer extends JsonSerializer<Schedule> {
    @Override
    public void serialize(Schedule schedule, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();

        gen.writeStringField("name", schedule.getName());

        gen.writeArrayFieldStart("matches");
        for (Match match : schedule.getMatches())
            gen.writeObject(match);
        gen.writeEndArray();

        AbstractScheduleValue[][] values = schedule.getScheduleValues();
        gen.writeArrayFieldStart("scheduleValues");
        for (AbstractScheduleValue[] valueRow : values) {
            gen.writeStartArray();
            for (AbstractScheduleValue value : valueRow)
                gen.writeObject(value);
            gen.writeEndArray();
        }
        gen.writeEndArray();

        gen.writeArrayFieldStart("players");
        for (Player player : schedule.getPlayers())
            gen.writeObject(player);
        gen.writeEndArray();

        gen.writeArrayFieldStart("localizations");
        for (Localization localization : schedule.getLocalizations())
            gen.writeObject(localization);
        gen.writeEndArray();

        gen.writeArrayFieldStart("timeslots");
        for (Timeslot timeslot : schedule.getTimeslots())
            gen.writeObject(timeslot);
        gen.writeEndArray();

        gen.writeNumberField("totalTimeslots", schedule.getTotalTimeslots());
        gen.writeNumberField("availableTimeslots", schedule.getAvailableTimeslots());
        gen.writeNumberField("occupation", schedule.getOccupation());
        gen.writeNumberField("occupationRatio", schedule.getOccupationRatio());

        gen.writeEndObject();
    }
}
