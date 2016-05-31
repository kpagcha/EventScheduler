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
public class PlayerScheduleValue extends AbstractScheduleValue {

    /**
     * El jugador juega en esta hora en una pista determinada
     */
    public static final ScheduleValue OCCUPIED = new ScheduleValue("OCCUPIED");

    /**
     * El jugador no juega en esta hora
     */
    public static final ScheduleValue FREE = new ScheduleValue("FREE");

    /**
     * El jugador no se encuentra disponible a esta hora
     */
    public static final ScheduleValue UNAVAILABLE = new ScheduleValue("UNAVAILABLE");

    /**
     * Esta hora se corresponde a un break u hora en la que no tienen lugar enfrentamientos
     */
    public static final ScheduleValue BREAK = new ScheduleValue("BREAK");

    /**
     * Esta hora se corresponde a una hora sobre la que se han limitado las pistas disponibles
     */
    public static final ScheduleValue LIMITED = new ScheduleValue("LIMITED");

    /**
     * Esta hora no pertenece al dominio de horas de la categoría. Este valor solo lo tomarán horarios combinados (de
     * torneo)
     */
    public static final ScheduleValue NOT_IN_DOMAIN = new ScheduleValue("NOT_IN_DOMAIN");

    /**
     * Posibles valores internos que puede tomar un valor de hueco de horario por jugadores
     */
    protected static final List<ScheduleValue> possibleValues =
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
    public PlayerScheduleValue(ScheduleValue val) {
        super(val);
    }

    protected List<ScheduleValue> getPossibleValues() {
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
        return o != null && o instanceof PlayerScheduleValue && ((PlayerScheduleValue) o).getValue().equals(value);

    }
}
