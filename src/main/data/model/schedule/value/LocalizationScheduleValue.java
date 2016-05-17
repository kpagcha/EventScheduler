package data.model.schedule.value;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import data.model.schedule.LocalizationSchedule;

/**
 * Valores que puede tener un elemento de un horario agrupado (clase {@link LocalizationSchedule}). 
 * <p>
 * Cada elemento corresponde a una hora en una pista
 *
 */
public class LocalizationScheduleValue extends AbstractScheduleValue {
	
	/**
	 * En la localizaci�n comienza un partido a la hora correspondiente en el horario
	 */
	public static ScheduleValue OCCUPIED = new ScheduleValue("OCCUPIED");
	
	/**
	 * Localizaci�n libre a la hora correspondiente
	 */
	public static ScheduleValue FREE = new ScheduleValue("FREE");
	
	/**
	 * Localizaci�n no disponible a la hora correspondiente para ninguna categor�a
	 */
	public static ScheduleValue UNAVAILABLE = new ScheduleValue("UNAVAILABLE");
	
	/**
	 * La disponibilidad de la localizaci�n a la hora correspondiente est� limitada para una o m�s categor�as, pero no para todas
	 */
	public static ScheduleValue LIMITED = new ScheduleValue("LIMITED");
	
	/**
	 * En la localizaci�n discurre la continuaci�n de un partido a la hora correspondiente
	 */
	public static ScheduleValue CONTINUATION = new ScheduleValue("CONTINUATION");
	
	/**
	 * Posibles valores internos que puede tomar un valor de hueco de horario por localizaciones 
	 */
	protected static final List<ScheduleValue> possibleValues = Collections.unmodifiableList(new ArrayList<>(Arrays.asList(
		OCCUPIED, FREE, UNAVAILABLE, LIMITED, CONTINUATION
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
			strVal = "�";
		} else if (isContinuation()) {
			strVal = "<";
		}
		return strVal;
	}
	
	public boolean equals(Object o) {
		if (o == null)
			return false;
		
		if (!(o instanceof LocalizationScheduleValue))
			return false;
		
		return ((LocalizationScheduleValue)o).getValue().equals(value);
	}
}
