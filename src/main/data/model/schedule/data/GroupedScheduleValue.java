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
	 * Está libre
	 */
	public static int FREE = 2;
	
	/**
	 * La pista no está disponible a esa hora para ninguna categoría
	 */
	public static int UNAVAILABLE = 3;
	
	/**
	 * La disponibilidad de esa pista a esa hora está limitada a solo algunas categorías
	 */
	public static int LIMITED = 4;
	
	/**
	 * Continuación de un partido
	 */
	public static int CONTINUATION = 5;
	
	/**
	 * Valor del elemento del horario
	 */
	private int value;
	
	/**
	 * Índices de los jugadores que se enfrentan en un partido (si hay un partido)
	 */
	private List<Integer> playersIndices;
	
	/**
	 * Construye un valor de horario agrupado distinto de "ocupado"
	 * 
	 * @param val un valor distinto del correspondiente a {@link #OCCUPIED}
	 */
	public GroupedScheduleValue(int val) {
		if (val < OCCUPIED && val > CONTINUATION)
			throw new IllegalArgumentException("Illegal value (" + val + ")");
		
		if (val == OCCUPIED)
			throw new IllegalStateException("A match must be specified if the schedule value is OCCUPIED.");
		
		value = val;
	}
	
	/**
	 * Construye un valor de horario "ocupado"
	 * 
	 * @param val el valor explícito de horario "ocupado", que debe equivaler al especificado por {@link #OCCUPIED}
	 * @param indices un lista no <code>null</code> y no vacía de enteros no que representa índices de jugadores
	 */
	public GroupedScheduleValue(int val, List<Integer> indices) {
		if (val < OCCUPIED && val > CONTINUATION)
			throw new IllegalArgumentException("Illegal value (" + val + ")");
		
		if (val != OCCUPIED)
			throw new IllegalStateException("Only schedule values of OCCUPIED can specify a match taking place.");
		
		if (indices == null)
			throw new IllegalArgumentException("Indices cannot be null");
		
		if (indices.isEmpty())
			throw new IllegalAccessError("Indices cannot be empty");
		
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
			strVal = "¬";
		} else if (value == CONTINUATION) {
			strVal = "<";
		}
		return strVal;
	}
}
