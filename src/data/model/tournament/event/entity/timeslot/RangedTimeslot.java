package data.model.tournament.event.entity.timeslot;

import java.time.temporal.TemporalAmount;

/**
 * Representa un timeslot con rango, es decir, una duración o período temporal en el que tiene lugar
 *
 */
public abstract class RangedTimeslot extends AbstractTimeslot {
	/**
	 * Duración del timeslot
	 */
	protected final TemporalAmount duration;
	
	/**
	 * Construye un timeslot con duración
	 * 
	 * @param duration
	 * @param chronologicalOrder
	 */
	public RangedTimeslot(final TemporalAmount duration, int chronologicalOrder) {
		super(chronologicalOrder);
		this.duration = duration;
	}
	
	public TemporalAmount getDuration() {
		return duration;
	}
	
	public String toString() {
		return chronologicalOrder + " (" + duration + ")";
	}
}
