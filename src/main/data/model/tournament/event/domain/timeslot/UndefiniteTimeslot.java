package data.model.tournament.event.domain.timeslot;

import java.time.temporal.TemporalAmount;

/**
 * Clase que representa un timeslot an�nimo o indefinido, es decir, que no define un comienzo temporal particular,
 * sino que es abstracto: los comienzos de partidos podr�an ser horas, d�as abstractos, etc. como hora 1, hora 2, d�a 1,
 * d�a 2, semana 4, semana 5...
 * <p>
 * El orden cronol�gico deber�a ser �nico para cada timeslot en un supuesto conjunto de timeslots an�nimos
 */
public class UndefiniteTimeslot extends RangedTimeslot {
	/**
	 * @param temporalAmount duraci�n o per�odo
	 * @param chronologicalOrder orden cronol�gico del timeslot
	 */
	public UndefiniteTimeslot(final TemporalAmount temporalAmount, final int chronologicalOrder) {
		super(temporalAmount, chronologicalOrder);
	}
}