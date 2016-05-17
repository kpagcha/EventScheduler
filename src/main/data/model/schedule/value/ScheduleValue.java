package data.model.schedule.value;

/**
 * Clase para la representación interna del valor de un hueco en un horario.
 *
 */
public class ScheduleValue {
	/**
	 * Nombre que identifica al valor
	 */
	private String name;

	public ScheduleValue(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	/**
	 * Comprueba si este valor de horario es el indicado por la cadena.
	 * 
	 * @param val cadena con un nombre de valor de horario
	 * @return <code>true</code> si el nombre del valor es igual al indicado, ignorando mayúsculas;
	 * <code>false</code> si no
	 */
	public boolean is(String val) {
		return name.equalsIgnoreCase(val);
	}
	
	public boolean equals(Object o) {
		if (o == null)
			return false;
		return o instanceof ScheduleValue && ((ScheduleValue)o).getName().equalsIgnoreCase(name);
	}
}
