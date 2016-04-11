package data.model.tournament.event.entity.timeslot;

import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAmount;

/**
 * Representa un timeslot que define un comienzo en el tiempo
 *
 */
public class DefiniteTimeslot extends RangedTimeslot {
	/**
	 * Comienzo del timeslot
	 */
	protected final TemporalAccessor start;
	
	/**
	 * @param start
	 * @param duration
	 * @param chronologicalOrder
	 */
	public DefiniteTimeslot(final TemporalAccessor start, final TemporalAmount duration, final int chronologicalOrder) {
		super(duration, chronologicalOrder);
		this.start = start;
	}
	
	public TemporalAccessor getStart() {
		return start;
	}
	
	@SuppressWarnings("unchecked")
	public int compareTo(DefiniteTimeslot timeslot) {
		if (start instanceof Comparable<?> && super.compareTo(timeslot) == 0)
			return ((Comparable<Comparable<?>>) start).compareTo((Comparable<Comparable<?>>)timeslot.start);
		else
			return super.compareTo(timeslot);
	}
	
	public String toString() {
		return "[" + chronologicalOrder + "] " + start + " (" + duration + ")";
	}
}
