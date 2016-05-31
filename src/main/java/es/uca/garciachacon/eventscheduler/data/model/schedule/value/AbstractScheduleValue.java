package es.uca.garciachacon.eventscheduler.data.model.schedule.value;

import java.util.List;

/**
 * Representa el valor que tiene un hueco de un horario.
 */
public abstract class AbstractScheduleValue {
    /**
     * Valor del elemento del horario
     */
    protected ScheduleValue value;

    /**
     * Construye un objeto de una clase hija de esta con el valor especificado
     *
     * @param val un valor incluido en la lista de posibles valores
     * @throws IllegalArgumentException si el valor no está incluido en la lista de posibles valores
     */
    public AbstractScheduleValue(ScheduleValue val) {
        if (!getPossibleValues().contains(val))
            throw new IllegalArgumentException("Illegal value");
        value = val;
    }

    /**
     * Comprueba si el hueco de horario está ocupado por un partido
     *
     * @return <code>true</code> si el hueco está ocupado, <code>false</code> si no
     */
    public boolean isOccupied() {
        return value.is("OCCUPIED");
    }

    /**
     * Comprueba si el hueco de horario está libre
     *
     * @return <code>true</code> si el hueco está libre, <code>false</code> si no
     */
    public boolean isFree() {
        return value.is("FREE");
    }

    /**
     * Comprueba si el hueco de horario no está disponible
     *
     * @return <code>true</code> si el hueco no está disponible, <code>false</code> si no
     */
    public boolean isUnavailable() {
        return value.is("UNAVAILABLE");
    }

    /**
     * Comprueba si el hueco de horario corresponde a un <i>break</i>
     *
     * @return <code>true</code> si el hueco es un <i>break</i>, <code>false</code> si no
     */
    public boolean isBreak() {
        return value.is("BREAK");
    }

    /**
     * Comprueba si el hueco de horario está limitado
     *
     * @return <code>true</code> si el hueco está limitado, <code>false</code> si no
     */
    public boolean isLimited() {
        return value.is("LIMITED");
    }

    /**
     * Comprueba si el hueco de horario no pertenece al dominio de la entidad que especifique la fila
     *
     * @return <code>true</code> si el hueco no pertenece al dominio, <code>false</code> si no
     */
    public boolean isNotInDomain() {
        return value.is("NOT_IN_DOMAIN");
    }

    /**
     * Comprueba si el hueco de horario es una continuación de partido
     *
     * @return <code>true</code> si el hueco es la continuación de un partido, <code>false</code> si no
     */
    public boolean isContinuation() {
        return value.is("CONTINUATION");
    }

    /**
     * Devuelve los posibles valores que puede tomar la representación interna del hueco de horario
     */
    protected abstract List<ScheduleValue> getPossibleValues();

    public ScheduleValue getValue() {
        return value;
    }

    public boolean equals(Object o) {
        return o != null && o instanceof AbstractScheduleValue && ((AbstractScheduleValue) o).getValue().equals(value);
    }
}
