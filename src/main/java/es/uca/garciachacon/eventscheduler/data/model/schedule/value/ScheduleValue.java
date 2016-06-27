package es.uca.garciachacon.eventscheduler.data.model.schedule.value;

import es.uca.garciachacon.eventscheduler.data.model.schedule.Schedule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Esta clase representa los valores que pueden tener los elementos de un horario (clase {@link Schedule}).
 * <p>
 * Cada elemento se corresponde a la participación de un jugador a una hora determinada.
 */
public class ScheduleValue extends AbstractScheduleValue {

    /**
     * El jugador juega en esta hora en una pista determinada
     */
    public static final Value OCCUPIED = new Value("OCCUPIED");

    /**
     * El jugador no juega en esta hora
     */
    public static final Value FREE = new Value("FREE");

    /**
     * El jugador no se encuentra disponible a esta hora
     */
    public static final Value UNAVAILABLE = new Value("UNAVAILABLE");

    /**
     * Esta hora se corresponde a un break u hora en la que no tienen lugar enfrentamientos
     */
    public static final Value BREAK = new Value("BREAK");

    /**
     * Esta hora se corresponde a una hora sobre la que se han limitado las pistas disponibles
     */
    public static final Value LIMITED = new Value("LIMITED");

    /**
     * Esta hora no pertenece al dominio de horas de la categoría. Este valor solo lo tomarán horarios combinados (de
     * torneo)
     */
    public static final Value NOT_IN_DOMAIN = new Value("NOT_IN_DOMAIN");

    /**
     * Posibles valores internos que puede tomar un valor de hueco de horario por jugadores
     */
    protected static final List<Value> possibleValues =
            Collections.unmodifiableList(new ArrayList<>(Arrays.asList(FREE,
                    UNAVAILABLE,
                    BREAK,
                    LIMITED,
                    NOT_IN_DOMAIN
            )));

    /**
     * Construye un valor concreto de un horario de jugadores por <i>timeslots</i>
     *
     * @param val valor interno que especifica el hueco de horario
     */
    public ScheduleValue(Value val) {
        super(val);
    }

    protected List<Value> getPossibleValues() {
        return possibleValues;
    }

    public String toString() {
        String strVal = "";
        if (isFree())
            strVal = "-";
        else if (isUnavailable())
            strVal = "~";
        else if (isBreak())
            strVal = "*";
        else if (isLimited())
            strVal = "¬";
        else if (isNotInDomain())
            strVal = "x";
        return strVal;
    }

    public boolean equals(Object o) {
        return o != null && o instanceof ScheduleValue && ((ScheduleValue) o).getValue().equals(value);

    }
}
