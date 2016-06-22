package es.uca.garciachacon.eventscheduler.rest.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import es.uca.garciachacon.eventscheduler.data.model.tournament.Tournament;
import es.uca.garciachacon.eventscheduler.data.model.tournament.event.Event;
import es.uca.garciachacon.eventscheduler.data.model.tournament.event.domain.Localization;
import es.uca.garciachacon.eventscheduler.data.model.tournament.event.domain.Player;
import es.uca.garciachacon.eventscheduler.data.model.tournament.event.domain.Timeslot;
import es.uca.garciachacon.eventscheduler.rest.deserializer.TimeslotDeserializer;
import es.uca.garciachacon.eventscheduler.solver.TournamentSolver;
import es.uca.garciachacon.eventscheduler.utils.TournamentUtils;

import java.io.IOException;
import java.time.*;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAmount;
import java.util.List;
import java.util.Optional;

/**
 * Serializador de un <i>timeslot</i>, que transforma una instancia de la clase {@link Timeslot} a su representación
 * mediante JSON. Es compatible en gran parte con la clase de deserialización {@link TimeslotDeserializer}, por lo
 * que la serialización y deserialización es bidireccional.
 * <p>
 * No obstante, hay una serie de casos en los que un <i>timeslot</i> no puede ser deserializado porque en sus campos
 * <code>"start"</code> y/o <code>"duration"</code>, donde se indica el tipo de dato que representa el comienzo del
 * <i>timeslot</i> y su duración respectivamente, el valor es desconocido por el deserializador.
 * <p>
 * Para el comienzo de un <i>timeslot</i>, representado por la clase {@link TemporalAccessor}, son los casos en los
 * que se usa una implementación personalizada de la interfaz, por ejemplo, una clase anónima, o una implementación
 * nativa distinta de las siguientes: {@link DayOfWeek}, {@link Month}, {@link MonthDay}, {@link Year},
 * {@link YearMonth}, {@link LocalTime}, {@link LocalDate} y {@link LocalDateTime}. Si el tipo del comienzo es
 * distinto, la serialización del valor del mismo será automática y por defecto, por tanto, será desconocida por el
 * deserializador.
 * <p>
 * Para la duración de un <i>timeslot</i>, representado por la clase {@link TemporalAmount}, un caso de
 * deserialización imposible sería si se usase una implementación propia de la interfaz, en lugar de las clases
 * nativas {@link Duration} o {@link Period}. También sería el caso para duraciones o períodos de composición
 * múltiple, por ejemplo: duraciones con valores distintos de 0 de tanto segundos como nanosegundos, o períodos donde
 * más de uno de sus valores internos (días, meses y años) sean distintos de 0. En estos casos, la deserialización no
 * sería viable.
 * <p>
 * El formato de la serialización es el mismo que el descrito en {@link TimeslotDeserializer}, excepto para los casos
 * desconocidos que se han descrito. En tal caso, de igual modo, habrá campos <code>"type"</code> tanto bajo la
 * propiedad <code>"start"</code> como <code>"duration"</code> (si la instancia tiene estos atributos presentes) e
 * indicará el tipo del valor del comienzo y/o duración mediante el nombre de la clase que se esté utilizando; y
 * también de forma análoga se incluirá el campo <code>"value"</code> con el valor del comienzo y/o duración y el
 * valor será automáticamente serializado por Jackson.
 */
public class TimeslotSerializer extends JsonSerializer<Timeslot> {
    public static void main(String[] args) throws JsonProcessingException {
        List<Player> p = TournamentUtils.buildGenericPlayers(4, "Player");
        List<Localization> l = TournamentUtils.buildGenericLocalizations(2, "Court");
        List<Timeslot> t = TournamentUtils.buildSimpleTimeslots(4);
        Event event = new Event("Event", p, l, t);

        event.setMatchupMode(TournamentSolver.MatchupMode.ALL_DIFFERENT);

        Tournament tournament = new Tournament("Tournament", event);

        System.out.println(new ObjectMapper().writeValueAsString(tournament));
    }

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
