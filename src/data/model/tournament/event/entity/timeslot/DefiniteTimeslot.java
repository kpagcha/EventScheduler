package data.model.tournament.event.entity.timeslot;

import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAmount;

/**
 * Representa un timeslot que define un comienzo en el tiempo
 * <p>
 * El orden cronol�gico de un conjunto de timeslots definidos puede ser �nico o no �nico para cada uno
 * de ellos, sin embargo, lo normal ser�a que no sean �nicos. 
 * Por ejemplo, [orden 1, 10:00-11:00], [orden 1, 11:00-12:00], [orden 1, 12:00-13:00], [orden 2, 10:00-11:00]...
 *
 */
public class DefiniteTimeslot extends RangedTimeslot {
	/**
	 * Comienzo del timeslot
	 */
	protected final TemporalAccessor start;
	
	/**
	 * @param start comienzo del per�odo, no <code>null</code>
	 * @param duration duraci�n del per�odo, no <code>null</code>
	 * @param chronologicalOrder orden cronol�gico y jer�rquico
	 */
	public DefiniteTimeslot(final TemporalAccessor start, final TemporalAmount duration, final int chronologicalOrder) {
		super(duration, chronologicalOrder);
		
		if (start == null)
			throw new IllegalArgumentException("Start cannot be null");
		
		this.start = start;
	}
	
	public TemporalAccessor getStart() {
		return start;
	}
	
	@SuppressWarnings("unchecked")
	public int compareTo(Timeslot timeslot) {
		if (timeslot == null)
			return 1;
		
		if (!(timeslot instanceof DefiniteTimeslot))
			return -1;
		
		final DefiniteTimeslot definiteTimeslot = (DefiniteTimeslot)timeslot;
		
		// Si ambos comienzos son clases comparables y tienen el mismo orden cronol�gico, se rompe el empate
		// comparando los comienzos
		if (start instanceof Comparable<?> && definiteTimeslot instanceof Comparable<?> && super.compareTo(timeslot) == 0) {
			final Comparable<Comparable<?>> thisStart = (Comparable<Comparable<?>>) start;
			final Comparable<Comparable<?>> otherStart = (Comparable<Comparable<?>>)definiteTimeslot.start;
			
			// Se invierte el resultado porque los resultados "menores" son los de m�s alta prioridad
			return -thisStart.compareTo(otherStart);
		} else {
			return super.compareTo(timeslot);
		}
	}
	
	public String toString() {
		return start + " (" + duration + ") [" + chronologicalOrder + "]";
	}
}
