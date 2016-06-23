package es.uca.garciachacon.eventscheduler.rest.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import es.uca.garciachacon.eventscheduler.data.model.tournament.event.domain.Timeslot;

import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeParseException;
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

        JsonNode chronologicalOrderNode = node.path("chronologicalOrder");
        if (!chronologicalOrderNode.isInt())
            throw new MalformedJsonException("Expected \"chronologicalOrder\" integer field");

        int chronologicalOrder = chronologicalOrderNode.asInt();
        JsonNode startNode = node.path("start");
        JsonNode durationNode = node.path("duration");

        TemporalAccessor start = null;
        TemporalAmount duration = null;

        if (!startNode.isMissingNode())
            start = parseStart(startNode);

        if (!durationNode.isMissingNode())
            duration = parseDuration(durationNode);

        if (start == null && duration == null)
            timeslot = new Timeslot(chronologicalOrder);
        else if (duration == null)
            timeslot = new Timeslot(chronologicalOrder, start);
        else if (start == null)
            timeslot = new Timeslot(chronologicalOrder, duration);
        else
            timeslot = new Timeslot(chronologicalOrder, start, duration);

        JsonNode nameNode = node.path("name");
        if (!nameNode.isMissingNode()) {
            if (!nameNode.isTextual())
                throw new MalformedJsonException("Field \"name\" expected to be textual");
            timeslot.setName(nameNode.asText());
        }

        return timeslot;
    }

    private TemporalAccessor parseStart(JsonNode startNode) throws MalformedJsonException {
        if (!startNode.isObject())
            throw new MalformedJsonException("Field \"start\" expected to be an object");

        JsonNode startTypeNode = startNode.path("type");
        if (!startTypeNode.isTextual())
            throw new MalformedJsonException("Expected \"type\" textual field");

        TemporalAccessor start;
        JsonNode valueNode = startNode.path("value");

        if (valueNode.isMissingNode())
            throw new MalformedJsonException("Expected \"value\" node");

        switch (startTypeNode.asText().trim()) {
            case "DayOfWeek":
                if (valueNode.isInt()) {
                    try {
                        start = DayOfWeek.of(valueNode.asInt());
                    } catch (DateTimeException e) {
                        throw new MalformedJsonException("Invalid day of week: " + e.getMessage());
                    }
                } else if (valueNode.isTextual()) {
                    try {
                        start = DayOfWeek.valueOf(valueNode.asText());
                    } catch (IllegalArgumentException e) {
                        throw new MalformedJsonException("Invalid day of week: " + e.getMessage());
                    }
                } else
                    throw new MalformedJsonException("Field \"value\" expected to be an integer or a string");
                break;

            case "Month":
                if (valueNode.isInt()) {
                    try {
                        start = Month.of(valueNode.asInt());
                    } catch (DateTimeException e) {
                        throw new MalformedJsonException("Invalid month: " + e.getMessage());
                    }
                } else if (valueNode.isTextual()) {
                    try {
                        start = Month.valueOf(valueNode.asText());
                    } catch (IllegalArgumentException e) {
                        throw new MalformedJsonException("Invalid month: " + e.getMessage());
                    }
                } else
                    throw new MalformedJsonException("Field \"value\" expected to be an integer or a string");
                break;

            case "MonthDay":
                if (!valueNode.isObject())
                    throw new MalformedJsonException("Field \"value\" expected to be an object");

                JsonNode monthDayMonthNode = valueNode.path("month");
                if (!monthDayMonthNode.isInt())
                    throw new MalformedJsonException("Expected \"month\" integer field");

                JsonNode monthDayDayOfMonthNode = valueNode.path("dayOfMonth");
                if (!monthDayDayOfMonthNode.isInt())
                    throw new MalformedJsonException("Expected \"dayOfMonth\" integer field");

                try {
                    start = MonthDay.of(monthDayMonthNode.asInt(), monthDayDayOfMonthNode.asInt());
                } catch (DateTimeException e) {
                    throw new MalformedJsonException("Invalid month-day: " + e.getMessage());
                }
                break;

            case "Year":
                if (!valueNode.isInt())
                    throw new MalformedJsonException("Field \"value\" expected to be an integer");

                try {
                    start = Year.of(valueNode.asInt());
                } catch (DateTimeException e) {
                    throw new MalformedJsonException("Invalid year: " + e.getMessage());
                }
                break;

            case "YearMonth":
                if (!valueNode.isObject())
                    throw new MalformedJsonException("Field \"value\" expected to be an object");

                JsonNode yearMonthYearNode = valueNode.path("year");
                if (!yearMonthYearNode.isInt())
                    throw new MalformedJsonException("Expected \"year\" integer field");

                JsonNode yearMonthMonthNode = valueNode.path("month");
                if (!yearMonthMonthNode.isInt())
                    throw new MalformedJsonException("Expected \"month\" integer field");

                try {
                    start = YearMonth.of(yearMonthYearNode.asInt(), yearMonthMonthNode.asInt());
                } catch (DateTimeException e) {
                    throw new MalformedJsonException("Invalid year-month: " + e.getMessage());
                }
                break;

            case "LocalTime":
                start = parseTime(valueNode);
                break;

            case "LocalDate":
                start = parseDate(valueNode);
                break;

            case "LocalDateTime":
                if (valueNode.isTextual()) {
                    try {
                        start = LocalDateTime.parse(valueNode.asText());
                    } catch (DateTimeParseException e) {
                        throw new MalformedJsonException("Cannot parse date-time: " + e.getMessage());
                    }
                } else if (valueNode.isObject())
                    start = LocalDateTime.of(parseDate(valueNode.path("date")), parseTime(valueNode.path("time")));
                else
                    throw new MalformedJsonException("Field \"value\" expected to be textual or an object");
                break;

            default:
                throw new MalformedJsonException("Unknown start type");
        }
        return start;
    }

    private TemporalAmount parseDuration(JsonNode durationNode) throws MalformedJsonException {
        if (!durationNode.isObject())
            throw new MalformedJsonException("Field \"duration\" expected to be an object");

        JsonNode durationTypeNode = durationNode.path("type");
        if (!durationTypeNode.isTextual())
            throw new MalformedJsonException("Expected \"type\" textual field");

        JsonNode valueNode = durationNode.path("value");
        if (!(valueNode.isInt() || valueNode.isLong()))
            throw new MalformedJsonException("Expected \"value\" integer field");

        TemporalAmount duration;
        long longValue = valueNode.asLong();

        try {
            switch (durationTypeNode.asText().trim()) {
                case "milliseconds":
                    duration = Duration.ofMillis(longValue);
                    break;
                case "seconds":
                    duration = Duration.ofSeconds(longValue);
                    break;
                case "minutes":
                    try {
                        duration = Duration.ofMinutes(longValue);
                    } catch (ArithmeticException e) {
                        throw new MalformedJsonException("Invalid minutes: " + e.getMessage());
                    }
                    break;
                case "hours":
                    try {
                        duration = Duration.ofHours(longValue);
                    } catch (ArithmeticException e) {
                        throw new MalformedJsonException("Invalid hours: " + e.getMessage());
                    }
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
                    break;
                default:
                    throw new MalformedJsonException("Unknown duration type");
            }
        } catch (ArithmeticException e) {
            throw new MalformedJsonException("Duration value too big: " + e.getMessage());
        }
        return duration;
    }

    private LocalTime parseTime(JsonNode timeNode) throws MalformedJsonException {
        if (timeNode.isMissingNode())
            throw new MalformedJsonException("Expected time value");

        LocalTime time;
        if (timeNode.isTextual()) {
            try {
                time = LocalTime.parse(timeNode.asText());
            } catch (DateTimeParseException e) {
                throw new MalformedJsonException("Cannot parse time: " + e.getMessage());
            }
        } else if (timeNode.isObject()) {
            int second = timeNode.path("second").asInt();

            JsonNode hourNode = timeNode.path("hour");
            if (!hourNode.isInt())
                throw new MalformedJsonException("Expected \"hour\" integer field");

            JsonNode minuteNode = timeNode.path("minute");
            if (!minuteNode.isInt())
                throw new MalformedJsonException("Expected \"minute\" integer field");

            try {
                time = LocalTime.of(hourNode.asInt(), minuteNode.asInt(), second);
            } catch (DateTimeException e) {
                throw new MalformedJsonException("Invalid time: " + e.getMessage());
            }
        } else
            throw new MalformedJsonException("Time value expected to be textual or an object");

        return time;
    }

    private LocalDate parseDate(JsonNode dateNode) throws MalformedJsonException {
        if (dateNode.isMissingNode())
            throw new MalformedJsonException("Expected date value");

        LocalDate date;
        if (dateNode.isTextual()) {
            try {
                date = LocalDate.parse(dateNode.asText());
            } catch (DateTimeParseException e) {
                throw new MalformedJsonException("Cannot parse date: " + e.getMessage());
            }
        } else if (dateNode.isObject()) {
            JsonNode yearNode = dateNode.path("year");
            if (!yearNode.isInt())
                throw new MalformedJsonException("Expected \"year\" integer field");

            JsonNode monthNode = dateNode.get("month");
            if (!monthNode.isInt())
                throw new MalformedJsonException("Expected \"month\" integer field");

            JsonNode dayNode = dateNode.get("day");
            if (!dayNode.isInt())
                throw new MalformedJsonException("Expected \"day\" integer field");

            try {
                date = LocalDate.of(yearNode.asInt(), monthNode.asInt(), dayNode.asInt());
            } catch (DateTimeException e) {
                throw new MalformedJsonException("Invalid date: " + e.getMessage());
            }
        } else
            throw new MalformedJsonException("Date value expected to be textual or an object");

        return date;
    }
}