package es.uca.garciachacon.eventscheduler.data.model.schedule.value;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Clase para la representación interna del valor de un hueco en un horario.
 */
public class Value {
    /**
     * Nombre que identifica al valor
     */
    private final String name;

    /**
     * Construye un valor con la cadena especificada, eliminando espacios en los extremos de la cadena y
     * transformándola a mayúsculas.
     *
     * @param name el valor interno de este valor de horario
     */
    public Value(String name) {
        this.name = name.trim().toUpperCase();
    }

    @JsonValue
    public String getName() {
        return name;
    }

    /**
     * Comprueba si este valor de horario es el indicado por la cadena.
     *
     * @param val cadena con un nombre de valor de horario
     * @return <code>true</code> si el nombre del valor es igual al indicado, ignorando mayúsculas
     * <code>false</code> si no
     */
    public boolean is(String val) {
        return name.equalsIgnoreCase(val);
    }

    public boolean equals(Object o) {
        return o != null && o instanceof Value && ((Value) o).getName().equalsIgnoreCase(name);
    }
}
