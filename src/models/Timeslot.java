package models;

public class Timeslot {
	/**
	 * �ltimo id
	 */
	private static int lastId = 0;
	
	/**
	 * Identificador
	 */
	private int id;
	
	/**
	 * L�mite inferior en milisegundos 
	 */
	private int lowerBound;
	
	/**
	 * L�mite superior en milisegundos
	 */
	private int upperBound;
	
	public enum TimeUnit { MILLISECONDS, SECONDS, MINUTES, HOURS, DAYS, YEARS };
	
	/**
	 * Unidad usada para este timeslot (el valor almacenado es el mismo, solamente afecta al formato cadena)
	 */
	private TimeUnit timeUnit = TimeUnit.HOURS;
	
	private boolean isBreak = false;
	
	public Timeslot(int lb, int ub) {
		lowerBound = lb;
		upperBound = ub;
		
		id = lastId++;
	}
	
	public Timeslot(int lb, int ub, boolean isBreak) {
		this(lb, ub);
		this.isBreak = isBreak;
	}
	
	public Timeslot(int lb, int ub, TimeUnit timeUnit) {
		this(lb, ub);
		this.timeUnit = timeUnit;
	}
	
	public Timeslot(int lb, int ub, boolean isBreak, TimeUnit timeUnit) {
		this(lb, ub);
		this.timeUnit = timeUnit;
		this.isBreak = isBreak;
	}
	
	public int getId() {
		return id;
	}
	
	public int getLowerBound() {
		return lowerBound;
	}
	
	public int getUpperBound() {
		return upperBound;
	}
	
	public TimeUnit getTimeUnit() {
		return timeUnit;
	}
	
	public void setIsBreak(boolean isBreak) {
		this.isBreak = isBreak;
	}
	
	public boolean getIsBreak() {
		return isBreak;
	}
	
	public String getLowerBoundStr() {
		return formatStringWithTimeUnit(lowerBound);
	}
	
	public String getUpperBoundStr() {
		return formatStringWithTimeUnit(upperBound);
	}
	
	public String toString() {
		return getLowerBoundStr() + " - " + getUpperBoundStr();
	}
	
	public boolean equals(Timeslot timeslot) {
		return timeslot != null && id == timeslot.getId();
	}
	
	private String formatStringWithTimeUnit(int value) {
		switch (timeUnit) {
			case MILLISECONDS:	// el valor del timeslot est� almacenado en ms
				break;
			case SECONDS:
				value /= 1000;
				break;
			case MINUTES:
				value /= 1000 * 60;
				break;
			case HOURS:
				value /= 1000 * 60 * 60;
				break;
			case DAYS:
				value /= 1000 * 60 * 60 * 24;
				break;
			case YEARS:
				value /= 1000 * 60 * 60 * 24 * 365;
				break;
			default:
				break;
		}
		return Integer.toString(value);
	}
}