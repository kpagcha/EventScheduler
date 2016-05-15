package data.model.tournament.event.domain.timeslot;

import java.time.temporal.TemporalAmount;

/**
 * Clase que representa un timeslot anónimo o indefinido, es decir, que no define un comienzo temporal particular,
 * sino que es abstracto: los comienzos de partidos podrían ser horas, días abstractos, etc. como hora 1, hora 2, día 1,
 * día 2, semana 4, semana 5...
 * <p>
 * El orden cronológico debería ser único para cada timeslot en un supuesto conjunto de timeslots anónimos
 */
public class UndefiniteTimeslot extends RangedTimeslot {
	/**
	 * @param temporalAmount duración o período
	 * @param chronologicalOrder orden cronológico del timeslot
	 */
	public UndefiniteTimeslot(final TemporalAmount temporalAmount, final int chronologicalOrder) {
		super(temporalAmount, chronologicalOrder);
	}
}