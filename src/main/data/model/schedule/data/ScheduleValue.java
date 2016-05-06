package data.model.schedule.data;

/**
 * Esta clase representa los valores que pueden tener los elementos de un horario (clase Schedule).
 * Cada elemento se corresponde a la participación de un jugador a una hora determinada.
 * 
 */
public class ScheduleValue {
	/**
	 * El jugador juega en esta hora en una pista determinada
	 */
	public static int OCCUPIED = 1;
	
	/**
	 * El jugador no juega en esta hora
	 */
	public static int FREE = 2;
	
	/**
	 * El jugador no se encuentra disponible a esta hora
	 */
	public static int UNAVAILABLE = 3;
	
	/**
	 * Esta hora se corresponde a un break u hora en la que no tienen lugar enfrentamientos
	 */
	public static int BREAK = 4;
	
	/**
	 * Esta hora se corresponde a una hora sobre la que se han limitado las pistas disponibles
	 */
	public static int LIMITED = 5;
	
	/**
	 * Esta hora no pertenece al dominio de horas de la categoría. Este valor solo lo tomarán horarios combinados.
	 */
	public static int NOT_IN_DOMAIN = 6;
	
	/**
	 * Valor del elemento del horario
	 */
	private int value;
	
	/**
	 * Si el valor del elemento es el de una pista ocupada, almacena un valor que representa la localización de juego
	 * donde habrá de estar el jugador a esta hora
	 */
	private int localization;
	
	/**
	 * Construye el valor de un horario distinto de "ocupado"
	 * 
	 * @param val un valor distinto de {@link #OCCUPIED}
	 */
	public ScheduleValue(int val) {
		if (!(val >= OCCUPIED && val <= NOT_IN_DOMAIN))
			throw new IllegalArgumentException("Illegal value (" + val + ")");
			
		if (val == OCCUPIED)
			throw new IllegalArgumentException("A localization must be specified if the schedule value is OCCUPIED");
		
		value = val;
	}
	
	/**
	 * Construye un valor de horario "ocupado"
	 * 
	 * @param val un valor equivalente a {@link #OCCUPIED}
	 * @param l el valor de una localización de juego como un entero, es decir, su índice
	 */
	public ScheduleValue(int val, int l) {
		if (!(val >= OCCUPIED && val <= NOT_IN_DOMAIN))
			throw new IllegalArgumentException("Illegal value (" + val + ")");
		
		if (val != OCCUPIED)
			throw new IllegalArgumentException("Only schedule values of OCCUPIED can specify a localization");
		
		value = val;
		localization = l;
	}
	
	public int getValue() {
		return value;
	}
	
	public int getLocalization() {
		if (value != OCCUPIED)
			throw new IllegalStateException("Only schedule values of OCCUPIED can specify a localization");
		
		return localization;
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
	
	public boolean isBreak() {
		return value == BREAK;
	}
	
	public boolean isLimited() {
		return value == LIMITED;
	}
	
	public boolean isNotInDomain() {
		return value == NOT_IN_DOMAIN;
	}
	
	public boolean equals(ScheduleValue scheduleValue) {
		if (scheduleValue == null || value != scheduleValue.value)
			return false;
		
		if (value == OCCUPIED)
			return localization == scheduleValue.localization;
		
		return true;
	}
	
	public String toString() {
		String strVal = "";
		if (value == OCCUPIED) {
			strVal = Integer.toString(localization);
		} else if (value == FREE) {
			strVal = "-";
		} else if (value == UNAVAILABLE) {
			strVal = "~";
		} else if (value == BREAK) {
			strVal = "*";
		} else if (value == LIMITED) {
			strVal = "¬";
		} else if (value == NOT_IN_DOMAIN) {
			strVal = "x";
		}
		return strVal;
	}
	
}
