package es.uca.garciachacon.eventscheduler.data.model.schedule.value;

import java.util.Collections;
import java.util.List;

/**
 * Representa un valor de horario ocupado, donde un jugador concreto juega a una hora concreta en una localización
 * almacenada en instancias de esta clase.
 */
public class PlayerScheduleValueOccupied extends PlayerScheduleValue {
    /**
     * Valor que representa la localización de juego donde habrá de jugar el jugador a esta hora
     */
    private final int localization;

    protected static final List<ScheduleValue> possibleValues = Collections.singletonList(OCCUPIED);

    public PlayerScheduleValueOccupied(int localization) {
        super(OCCUPIED);
        this.localization = localization;
    }

    protected List<ScheduleValue> getPossibleValues() {
        return possibleValues;
    }

    public int getLocalization() {
        return localization;
    }

    public String toString() {
        return Integer.toString(localization);
    }

    public boolean equals(Object o) {
        return o != null && o instanceof PlayerScheduleValueOccupied &&
                ((PlayerScheduleValueOccupied) o).getLocalization() == localization;

    }
}
