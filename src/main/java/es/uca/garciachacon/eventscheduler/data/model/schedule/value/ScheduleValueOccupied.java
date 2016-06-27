package es.uca.garciachacon.eventscheduler.data.model.schedule.value;

import java.util.Collections;
import java.util.List;

/**
 * Representa un valor de horario ocupado, donde un jugador concreto juega a una hora concreta en una localización
 * almacenada en instancias de esta clase.
 */
public class ScheduleValueOccupied extends ScheduleValue {
    /**
     * Valor que representa la localización de juego donde habrá de jugar el jugador a esta hora
     */
    private final int localization;

    protected static final List<Value> possibleValues = Collections.singletonList(OCCUPIED);

    public ScheduleValueOccupied(int localization) {
        super(OCCUPIED);
        this.localization = localization;
    }

    protected List<Value> getPossibleValues() {
        return possibleValues;
    }

    public int getLocalization() {
        return localization;
    }

    public String toString() {
        return Integer.toString(localization);
    }

    public boolean equals(Object o) {
        return o != null && o instanceof ScheduleValueOccupied &&
                ((ScheduleValueOccupied) o).getLocalization() == localization;

    }
}
