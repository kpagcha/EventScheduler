package data.model.tournament.event.entity.timeslot;

import java.time.temporal.TemporalAmount;

/**
 * Clase que representa un timeslot an�nimo, es decir, que no define un comienzo temporal particular,
 * sino que es abstracto.
 *
 */
public class UndefiniteTimeslot extends RangedTimeslot {
	/**
	 * @param temporalAmount     duraci�n o per�odo
	 * @param chronologicalOrder orden cronol�gico del timeslot
	 */
	public UndefiniteTimeslot(final TemporalAmount temporalAmount, final int chronologicalOrder) {
		super(temporalAmount, chronologicalOrder);
	}
}