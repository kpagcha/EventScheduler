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

/**
 * Deserializador de un <i>timeslot</i>, {@link Timeslot}. Espera el siguiente formato, con un campo entero
 * <i>chronologicalOrder</i> que es obligatorio:
 * <code>{"chronologicalOrder":1}</code>
 * <p>
 * Además, hay tres campos adicionales que son opcionales, que producirán un <i>timeslot</i> con un comienzo y/o una
 * duración haciendo uso de los constructores pertinentes. Hay distintos tipos de comienzos y de duraciones, estos
 * tipos se indican mediante el campo <i>type</i> y el valor con <i>value</i>. El tercer campo se trata de
 * <i>name</i> y permite indicar un nombre para el <i>timeslot</i>.
 * <p>
 * El primer campo adicional es el <strong>comienzo del <i>timeslot</i>.</strong>
 * <p>
 * <p>
 * Comienzo del <i>timeslot</i> un día de la semana:
 * <pre>
 * {
 *   "chronologicalOrder": 1,
 *   "start": {
 *       "type": "DayOfWeek",
 *       "value": 3
 *    }
 * }
 * </pre>
 * <p>
 * O bien:
 * <p>
 * <pre>
 * {
 *   "chronologicalOrder": 1,
 *   "start": {
 *     "type": "DayOfWeek",
 *     "value": "THURSDAY"
 *   }
 * }
 * </pre>
 * <p>
 * A partir de aquí no se indicará el cuerpo JSON completo porque el formato es el mismo; únicamente varía el campo
 * <i>start</i>.
 * <p>
 * Comienzo un mes determinado: <code>"start": {"type": "Month", "value": 10}</code> o bien <code>"start": {"type":
 * "Month", "value": "January"}</code>.
 * <p>
 * Comienzo el día de un mes: <code>"type": "MonthDay", "value": {"month": 4, "dayOfMonth": 24}</code>.
 * <p>
 * Comienzo un año determinado: <code>"type": "Year", "value": 2016</code>.
 * <p>
 * Comienzo el mes de un año: <code>"type": "YearMonth", "value": {"year": 2016, "month": 7}</code>
 * <p>
 * Comienzo a una hora local: <code>"type": "LocalTime", "value": {"hour": 16, "minute": 34, "second": 46}</code>. El
 * atributo <i>second</i> es opcional. Si no se incluye el valor de los segundos será 0. También se puede indicar con
 * el siguiente formato para el valor: <code>"value": "16:51:38"</code>. Igualmente, la parte de los segundos es
 * opcional.
 * <p>
 * Comienzo en una fecha local: <code>"type": "LocalDate", "value": {"year": 2016, "month": 6, "day": 17}</code>. O bien
 * se puede indicar el valor de la fecha con el siguiente formato: <code>"type": "LocalDate", "value":
 * "2016-06-17"</code>. Este formato de fecha es el que define la norma ISO-8601.
 * <p>
 * Comienzo en una fecha local y a una hora local determinadas: el tipo es <code>"type": "LocalDateTime"</code> y el
 * valor se divide en los campos <code>"date"</code>, que responde al mismo formato que se describe anteriormente
 * para el tipo <i>LocalDate</i>, y <code>"time"</code>, que sigue el mismo formato que el tipo <i>LocalTime</i>.
 * También se puede indicar siguiendo el siguiente formato: <code>"value": "2007-12-03T10:15:30"</code>. Un
 * ejemplo puede ser:
 * <p>
 * <pre>
 * {
 *   "chronologicalOrder": 1,
 *   "start": {
 *     "type": "LocalDateTime",
 *     "value": {
 *       "date": "2016-06-17",
 *       "time": "10:35:10"
 *     }
 *   }
 * }
 * </pre>
 * <p>
 * El segundo campo adicional es la <strong>duración del <i>timeslot</i>.</strong> y se identifica con el campo
 * <i>duration</i>. A diferencia del campo <i>start</i>, con formatos variables, esta propiedad sigue un formato fijo
 * . Está compuesto de <i>type</i>, texto que indica el tipo de duración, y <i>value</i>, un entero positivo que
 * indica la cantidad correspondiente al tipo indicado. Los tipos pueden ser <code>"milliseconds"</code>,
 * <code>"seconds"</code>, <code>"minutes"</code>, <code>"hours"</code>, <code>"days"</code>, <code>"weeks"</code>,
 * <code>"months"</code> y <code>"years"</code>, que se corresponden a duraciones de milisegundos, segundos, minutos,
 * horas, días, semanas, meses y años respectivamente.
 * <p>
 * Un ejemplo de un <i>timeslot</i> con una duración de un día:
 * <pre>
 * {
 *   "chronologicalOrder": 1,
 *   "duration": {
 *     "type": "days",
 *     "value": 1
 *   }
 * }
 * </pre>
 * <p>
 * Ambos campos <i>start</i> y <i>duration</i> pueden ser incluidos en el mismo objeto JSON, indicando que el
 * <i>timeslot</i> tendrá tanto un comienzo como una duración conocidos. El <i>timeslot</i> que comienza a las 10:30
 * y tiene una duración de 90 minutos se representa como sigue:
 * <p>
 * <pre>
 * {
 * "chronologicalOrder": 10,
 *   "start": {
 *     "type": "LocalTime",
 *     "value": "10:30"
 *   },
 *   "duration": {
 *     "type": "minutes",
 *      "value": 90
 *   }
 * }
 * </pre>
 */

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

        if (startNode != null)
            start = parseStart(startNode, start);

        if (durationNode != null)
            duration = parseDuration(durationNode, duration);

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
            String name = nameNode.isTextual() ? nameNode.asText() : "";
            if (!name.isEmpty())
                timeslot.setName(name);
        }

        return timeslot;
    }

    private TemporalAccessor parseStart(JsonNode startNode, TemporalAccessor start) {
        JsonNode startTypeNode = startNode.get("type");
        String startType = startTypeNode.isTextual() ? startTypeNode.asText() : null;

        JsonNode valueNode = startNode.get("value");

        switch (startType.trim()) {
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
                start = MonthDay.of(
                        Integer.parseInt(valueNode.get("month").asText()),
                        Integer.parseInt(valueNode.get("dayOfMonth").asText())
                );
                break;

            case "Year":
                start = Year.of(Integer.parseInt(valueNode.asText()));
                break;

            case "YearMonth":
                start = YearMonth.of(
                        Integer.parseInt(valueNode.get("year").asText()),
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
                    start = LocalDateTime.of(parseDate(valueNode.get("date")), parseTime(valueNode.get("time")));
        }
        return start;
    }

    private TemporalAmount parseDuration(JsonNode durationNode, TemporalAmount duration) {
        JsonNode durationTypeNode = durationNode.get("type");
        String durationType = durationTypeNode.isTextual() ? durationTypeNode.asText() : null;

        JsonNode valueNode = durationNode.get("value");

        long longValue = Long.parseLong(valueNode.asText());

        switch (durationType.trim()) {
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
                duration = Period.ofDays(Math.toIntExact(longValue));
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
        return duration;
    }

    private LocalTime parseTime(JsonNode node) {
        LocalTime time;
        if (node.isTextual())
            time = LocalTime.parse(node.asText());
        else {
            int second = 0;
            JsonNode valueSecondNode = node.path("second");
            if (valueSecondNode.isInt())
                second = valueSecondNode.asInt();

            time = LocalTime.of(
                    Integer.parseInt(node.get("hour").asText()),
                    Integer.parseInt(node.get("minute").asText()),
                    second
            );
        }
        return time;
    }

    private LocalDate parseDate(JsonNode node) {
        LocalDate date;
        if (node.isTextual())
            date = LocalDate.parse(node.asText());
        else {
            date = LocalDate.of(
                    Integer.parseInt(node.get("year").asText()),
                    Integer.parseInt(node.get("month").asText()),
                    Integer.parseInt(node.get("day").asText())
            );
        }
        return date;
    }
}