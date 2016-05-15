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
	
	protected static final List<ScheduleValue> possibleValues = Collections.unmodifiableList(new ArrayList<>(Arrays.asList(
		new ScheduleValue("FREE"), new ScheduleValue("UNAVAILABLE"), new ScheduleValue("LIMITED"), new ScheduleValue("CONTINUATION")
	)));
	
	/**
	 * En la localizaci�n comienza un partido a la hora correspondiente en el horario
	 */
	public static ScheduleValue OCCUPIED = new ScheduleValue("OCCUPIED");
	
	/**
	 * Localizaci�n libre a la hora correspondiente
	 */
	public static ScheduleValue FREE = possibleValues.stream().filter(v -> v.is("FREE")).findFirst().orElse(null);
	
	/**
	 * Localizaci�n no disponible a la hora correspondiente para ninguna categor�a
	 */
	public static ScheduleValue UNAVAILABLE = possibleValues.stream().filter(v -> v.is("UNAVAILABLE")).findFirst().orElse(null);
	
	/**
	 * La disponibilidad de la localizaci�n a la hora correspondiente est� limitada para una o m�s categor�as, pero no para todas
	 */
	public static ScheduleValue LIMITED = possibleValues.stream().filter(v -> v.is("LIMITED")).findFirst().orElse(null);
	
	/**
	 * En la localizaci�n discurre la continuaci�n de un partido a la hora correspondiente
	 */
	public static ScheduleValue CONTINUATION = possibleValues.stream().filter(v -> v.is("CONTINUATION")).findFirst().orElse(null);

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
		if (isOccupied()) {
			strVal = ((LocalizationScheduleValueOccupied)this).toString();
		} else if (isFree()) {
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
		if (!(o instanceof PlayerScheduleValue))
			return false;
		
		LocalizationScheduleValue v = (LocalizationScheduleValue)o;
		if (v.isOccupied())
			return v.getValue().equals(value);
		
		return (((LocalizationScheduleValueOccupied)v)).getPlayers() == ((LocalizationScheduleValueOccupied)this).getPlayers();
	}
}
