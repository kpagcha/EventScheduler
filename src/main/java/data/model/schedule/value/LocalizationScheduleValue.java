package data.model.schedule.value;

import data.model.schedule.LocalizationSchedule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Valores que puede tener un elemento de un horario agrupado (clase {@link LocalizationSchedule}).
 * <p>
 * Cada elemento corresponde a una hora en una pista
 */
public class LocalizationScheduleValue extends AbstractScheduleValue {

    /**
     * En la localización comienza un partido a la hora correspondiente en el horario
     */
    public static final ScheduleValue OCCUPIED = new ScheduleValue("OCCUPIED");

    /**
     * Localización libre a la hora correspondiente
     */
    public static final ScheduleValue FREE = new ScheduleValue("FREE");

    /**
     * Localización no disponible a la hora correspondiente para ninguna categoría
     */
    public static final ScheduleValue UNAVAILABLE = new ScheduleValue("UNAVAILABLE");

    /**
     * La disponibilidad de la localización a la hora correspondiente está limitada para una o más categorías, pero no
     * para todas
     */
    public static final ScheduleValue LIMITED = new ScheduleValue("LIMITED");

    /**
     * En la localización discurre la continuación de un partido a la hora correspondiente
     */
    public static final ScheduleValue CONTINUATION = new ScheduleValue("CONTINUATION");

    /**
     * Posibles valores internos que puede tomar un valor de hueco de horario por localizaciones
     */
    protected static final List<ScheduleValue> possibleValues =
            Collections.unmodifiableList(new ArrayList<>(Arrays.asList(OCCUPIED,
                    FREE,
                    UNAVAILABLE,
                    LIMITED,
                    CONTINUATION
            )));

    /**
     * Construye un valor concreto de un horario de localizaciones por <i>timeslots</i>
     *
     * @param val valor interno que especifica el hueco de horario
     */
    public LocalizationScheduleValue(ScheduleValue val) {
        super(val);
    }

    protected List<ScheduleValue> getPossibleValues() {
        return possibleValues;
    }

    public String toString() {
        String strVal = "";
        if (isFree()) {
            strVal = "-";
        } else if (isUnavailable()) {
            strVal = "*";
        } else if (isLimited()) {
            strVal = "¬";
        } else if (isContinuation()) {
            strVal = "<";
        }
        return strVal;
    }

    public boolean equals(Object o) {
        return o != null && o instanceof LocalizationScheduleValue &&
                ((LocalizationScheduleValue) o).getValue().equals(value);

    }
}
