package data.model.tournament.event.entity.timeslot;

import java.time.temporal.TemporalAmount;

/**
 * Clase que representa un timeslot anónimo, es decir, que no define un comienzo temporal particular,
 * sino que es abstracto.
 *
 */
public class UndefiniteTimeslot extends RangedTimeslot {
	/**
	 * @param temporalAmount     duración o período
	 * @param chronologicalOrder orden cronológico del timeslot
	 */
	public UndefiniteTimeslot(final TemporalAmount temporalAmount, final int chronologicalOrder) {
		super(temporalAmount, chronologicalOrder);
	}
}