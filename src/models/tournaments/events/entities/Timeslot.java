package models.tournaments.events.entities;

public class Timeslot extends Entity implements Comparable<Timeslot> {
	/**
	 * Límite inferior en milisegundos 
	 */
	private int lowerBound;
	
	/**
	 * Límite superior en milisegundos
	 */
	private int upperBound;
	
	public enum TimeUnit { MILLISECONDS, SECONDS, MINUTES, HOURS, DAYS, YEARS };
	
	/**
	 * Unidad usada para este timeslot (el valor almacenado es el mismo, solamente afecta al formato cadena)
	 */
	private TimeUnit timeUnit = TimeUnit.HOURS;
	
	public Timeslot(String name, int lb, int ub) {
		super(name);
		lowerBound = lb;
		upperBound = ub;
	}
	
	public Timeslot(int lb, int ub) {
		this("Timeslot", lb, ub);
	}
	
	public Timeslot(int lb, int ub, TimeUnit timeUnit) {
		this(lb, ub);
		this.timeUnit = timeUnit;
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
	
	public String getLowerBoundStr() {
		return formatStringWithTimeUnit(lowerBound);
	}
	
	public String getUpperBoundStr() {
		return formatStringWithTimeUnit(upperBound);
	}
	
	public String toString() {
		return getLowerBoundStr() + " - " + getUpperBoundStr();
	}
	
	private String formatStringWithTimeUnit(int value) {
		switch (timeUnit) {
			case MILLISECONDS:	// el valor del timeslot está almacenado en ms
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

	public int compareTo(Timeslot timeslot) {
		if (getLowerBound() < timeslot.getLowerBound())
			return -1;
		else if (timeslot.getLowerBound() < getLowerBound())
			return 1;
		else if (getUpperBound() < timeslot.getUpperBound())
			return -1;
		else if (timeslot.getUpperBound() < getUpperBound())
			return 1;
		return 0;
	}
}