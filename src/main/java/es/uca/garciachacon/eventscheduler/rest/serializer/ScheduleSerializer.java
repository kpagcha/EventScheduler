package es.uca.garciachacon.eventscheduler.rest.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import es.uca.garciachacon.eventscheduler.data.model.schedule.*;
import es.uca.garciachacon.eventscheduler.data.model.schedule.value.AbstractScheduleValue;
import es.uca.garciachacon.eventscheduler.data.model.schedule.value.ScheduleValue;
import es.uca.garciachacon.eventscheduler.data.model.tournament.Localization;
import es.uca.garciachacon.eventscheduler.data.model.tournament.Player;
import es.uca.garciachacon.eventscheduler.data.model.tournament.Timeslot;

import java.io.IOException;

/**
 * Serializador de un horario {@link Schedule}. La serialización es posible para cualquiera de las clases existentes
 * que extienden a {@link Schedule}: {@link EventSchedule}, {@link TournamentSchedule} e {@link InverseSchedule}. Sea
 * cual sea el tipo del horario, la serialización incluye los siguientes campos:
 * <p>
 * Campo <i>"name"</i>: el <strong>nombre</strong> del horario.
 * <p>
 * Campo <i>"tournament"</i>: el nombre del <strong>torneo</strong> al que pertenece el horario.
 * <p>
 * Los <strong>jugadores</strong> que componen los partidos que se juegan en el horario, en el campo <i>"players"</i>,
 * que será un <i>array</i> de objetos (cada jugador), con la propiedad <i>name</i>, el nombre del jugador.
 * <p>
 * Las <strong>localizaciones</strong> de juego se incluyen en el campo <i>"localizations"</i> y, de forma similar,
 * será un <i>array</i> de objetos con la propiedad <i>name</i> con el nombre de la localización.
 * <p>
 * Los <strong><i>timeslots</i></strong> bajo el campo <i>"timeslots"</i>, también un <i>array</i> de objetos, cada uno
 * de ellos siendo serializado mediante {@link TimeslotSerializer}.
 * <p>
 * El siguiente campo, <i>matches</i>, es una lista de todos los partidos que componen el horario. Cada objeto JSON
 * en el <i>array</i> es un partido con las siguientes propiedades (la serialización de las propiedades es igual que
 * anteriormente):
 * <ul>
 * <li><i>"players": array</i> de jugadores que componen el partido</li>
 * <li><i>"localization":</i> localización donde transcurre el partido</li>
 * <li><i>"startTimeslot":</i> <i>timeslot</i> en el que el partido comienza</li>
 * <li><i>"endTimeslot":</i> <i>timeslot</i> en el que el partido termina</li>
 * <li><i>"duration":</i> duración (número de <i>timeslots</i>) del partido</li>
 * <li><i>"teams":</i> si el partido se compone de equipos, se incluye la composición de los mismos</li>
 * </ul>
 * <p>
 * El campo <i>scheduleValues</i> contiene la información de cada hueco en el <strong>horario</strong> en forma de
 * <i>array</i> bidimensional, es decir, una tabla donde las filas se refieren a los jugadores (o a las
 * localizaciones, si se trata de un {@link InverseSchedule}) y las columnas se refieren a los <i>timeslots</i>, es
 * decir, un momento en el calendario del horario. Por tanto, una casilla <i>i,j</i> de esta tabla contiene la
 * información o estado del jugador <i>i</i> (o localización <i>i</i>) en un momento <i>j</i>. Cada casilla es un
 * objeto JSON con una o dos propiedades:
 * <ul>
 * <li><i>"value":</i> contiene el valor de la casilla, que puede ser <i>"OCCUPIED", "FREE",
 * "UNAVAILABLE"...</i> entre otros (los distintos valores se describen en {@link ScheduleValue}).</li>
 * <li><i>"localization":</i> esta propiedad solamente aparecerá si el valor de la casilla es <i>"OCCUPIED".</i>
 * Contiene información acerca de la localización donde transcurre el partido, siendo su valor la posición (empezando
 * en 0) de la misma en la lista de localizaciones del horario (campo <i>"localizations".</i>)</li>
 * <li>Alternativamente a <i>"localization"</i>, existe el campo <i>"players"</i> para horarios
 * {@link InverseSchedule} (y también aparece únicamente si el valor del hueco es <i>"OCCUPIED"</i>). Este campo es
 * de tipo <i>array</i> y contiene una lista de los jugadores que forman parte del partido que transcurre en la
 * casilla. Los jugadores se representan mediante su posición en la lista de jugadores del horario (campo
 * <i>"players"</i>), empezando por 0.
 * </li>
 * </ul>
 * <p>
 * Las demás propiedades hacen referencia a estadísticas e información adicional del horario:
 * <p>
 * El <strong>número total de <i>timeslots</i></strong> del horario, campo <i>"totalTimeslots"</i>.
 * <p>
 * El <strong>número de <i>timeslots</i> disponibles</strong> del horario, es decir, donde un partido puede
 * transcurrir; campo <i>"availableTimeslots"</i>.
 * <p>
 * El <strong>número de <i>timeslots</i> ocupados por un partido</strong>, campo <i>"occupation"</i>.
 * <p>
 * La <strong>relación entre la ocupación y el número de <i>timeslots</i> disponibles</strong>, campo
 * <i>"occupationRatio"</i>.
 */
public class ScheduleSerializer extends JsonSerializer<Schedule> {
    @Override
    public void serialize(Schedule schedule, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();

        gen.writeStringField("name", schedule.getName());

        gen.writeStringField("tournament", schedule.getTournament().getName());

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

        gen.writeNumberField("totalTimeslots", schedule.getTotalTimeslots());
        gen.writeNumberField("availableTimeslots", schedule.getAvailableTimeslots());
        gen.writeNumberField("occupation", schedule.getOccupation());
        gen.writeNumberField("occupationRatio", schedule.getOccupationRatio());

        gen.writeEndObject();
    }
}
