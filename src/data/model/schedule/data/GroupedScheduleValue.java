package data.model.schedule.data;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * Valores que puede tener un elemento de un horario agrupado. Cada elemento corresponde a una hora en una pista
 *
 */
public class GroupedScheduleValue {
	/**
	 * Hay un partido 
	 */
	public static int OCCUPIED = 1;
	
	/**
	 * Est� libre
	 */
	public static int FREE = 2;
	
	/**
	 * La pista no est� disponible a esa hora para ninguna categor�a
	 */
	public static int UNAVAILABLE = 3;
	
	/**
	 * La disponibilidad de esa pista a esa hora est� limitada a solo algunas categor�as
	 */
	public static int LIMITED = 4;
	
	/**
	 * Continuaci�n de un partido
	 */
	public static int CONTINUATION = 5;
	
	/**
	 * Valor del elemento del horario
	 */
	private int value;
	
	/**
	 * �ndices de los jugadores que se enfrentan en un partido (si hay un partido)
	 */
	private List<Integer> playersIndices;
	
	public GroupedScheduleValue(int val) {
		if (val == OCCUPIED)
			throw new IllegalStateException("A match must be specified if the schedule value is OCCUPIED.");
		
		value = val;
	}
	
	public GroupedScheduleValue(int val, List<Integer> indices) {
		if (val != OCCUPIED)
			throw new IllegalStateException("Only schedule values of OCCUPIED can specify a match taking place.");
		
		value = val;
		playersIndices = indices;
	}
	
	public int getValue() {
		return value;
	}
	
	public List<Integer> getPlayersIndices() {
		if (value != OCCUPIED)
			throw new IllegalStateException("Only schedule values of OCCUPIED can specify a match taking place.");
		
		return playersIndices;
	}
	
	public boolean isOccupied() {
		return value == OCCUPIED;
	}
	
	public boolean isFree() {
		return value == FREE;
	}
	
	public boolean isUnavailable() {
		return value == UNAVAILABLE;
	}
	
	public boolean isLimited() {
		return value == LIMITED;
	}
	
	public boolean isContinuation() {
		return value == CONTINUATION;
	}
	
	public String toString() {
		String strVal = "";
		if (value == OCCUPIED) {
			strVal = StringUtils.join(playersIndices, ",");
		} else if (value == FREE) {
			strVal = "-";
		} else if (value == UNAVAILABLE) {
			strVal = "*";
		} else if (value == LIMITED) {
			strVal = "�";
		} else if (value == CONTINUATION) {
			strVal = "<";
		}
		return strVal;
	}
}
