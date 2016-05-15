package data.model.schedule.value;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import data.model.schedule.Schedule;

/**
 * Esta clase representa los valores que pueden tener los elementos de un horario (clase {@link Schedule}).
 * <p>
 * Cada elemento se corresponde a la participación de un jugador a una hora determinada.
 * 
 */
public class PlayerScheduleValue extends AbstractScheduleValue {
	
	protected static final List<ScheduleValue> possibleValues = Collections.unmodifiableList(new ArrayList<>(Arrays.asList(
		new ScheduleValue("FREE"), new ScheduleValue("UNAVAILABLE"), new ScheduleValue("BREAK"), 
		new ScheduleValue("LIMITED"), new ScheduleValue("NOT_IN_DOMAIN")
	)));
	
	/**
	 * El jugador juega en esta hora en una pista determinada
	 */
	public static ScheduleValue OCCUPIED = new ScheduleValue("OCCUPIED");
	
	/**
	 * El jugador no juega en esta hora
	 */
	public static ScheduleValue FREE = possibleValues.stream().filter(v -> v.is("FREE")).findFirst().orElse(null);
	
	/**
	 * El jugador no se encuentra disponible a esta hora
	 */
	public static ScheduleValue UNAVAILABLE = possibleValues.stream().filter(v -> v.is("UNAVAILABLE")).findFirst().orElse(null);
	
	/**
	 * Esta hora se corresponde a un break u hora en la que no tienen lugar enfrentamientos
	 */
	public static ScheduleValue BREAK = possibleValues.stream().filter(v -> v.is("BREAK")).findFirst().orElse(null);
	
	/**
	 * Esta hora se corresponde a una hora sobre la que se han limitado las pistas disponibles
	 */
	public static ScheduleValue LIMITED = possibleValues.stream().filter(v -> v.is("LIMITED")).findFirst().orElse(null);
	
	/**
	 * Esta hora no pertenece al dominio de horas de la categoría. Este valor solo lo tomarán horarios combinados (de torneo)
	 */
	public static ScheduleValue NOT_IN_DOMAIN = possibleValues.stream().filter(v -> v.is("NOT_IN_DOMAIN")).findFirst().orElse(null);
	
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
		if (isOccupied()) {
			strVal = ((PlayerScheduleValueOccupied)this).toString();
		} else if (isFree()) {
			strVal = "-";
		} else if (isUnavailable()) {
			strVal = "~";
		} else if (isBreak()) {
			strVal = "*";
		} else if (isLimited()) {
			strVal = "¬";
		} else if (isNotInDomain()) {
			strVal = "x";
		}
		return strVal;
	}
	
	public boolean equals(Object o) {
		if (!(o instanceof PlayerScheduleValue))
			return false;
		
		PlayerScheduleValue v = (PlayerScheduleValue)o;
		if (v.isOccupied())
			return v.getValue().equals(value);
		
		return (((PlayerScheduleValueOccupied)v)).getLocalization() == ((PlayerScheduleValueOccupied)this).getLocalization();
	}
}
