package data.model.tournament.event.domain.timeslot;

import java.time.temporal.TemporalAmount;

/**
 * Representa un timeslot con rango, es decir, una duraci�n o per�odo temporal en el que tiene lugar
 *
 */
public abstract class RangedTimeslot extends AbstractTimeslot {
	/**
	 * Duraci�n del timeslot
	 */
	protected final TemporalAmount duration;
	
	/**
	 * Construye un timeslot con duraci�n
	 * 
	 * @param duration duraci�n del per�odo, no <code>null</code>
	 * @param chronologicalOrder orden cronol�gico de este timeslot
	 */
	public RangedTimeslot(final TemporalAmount duration, int chronologicalOrder) {
		super(chronologicalOrder);
		
		if (duration == null)
			throw new IllegalArgumentException("Duration cannot be null");
		
		this.duration = duration;
	}
	
	public TemporalAmount getDuration() {
		return duration;
	}
	
	public String toString() {
		return chronologicalOrder + " (" + duration + ")";
	}
}
