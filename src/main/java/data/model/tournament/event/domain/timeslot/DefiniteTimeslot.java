package data.model.tournament.event.domain.timeslot;

import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAmount;

/**
 * Representa un timeslot que define un comienzo en el tiempo.
 * <p>
 * <p>El orden cronológico de un conjunto de timeslots definidos puede ser único o no único para cada uno de ellos,
 * sin embargo, lo normal sería que no sean únicos. Por ejemplo, [orden 1, 10:00-11:00], [orden 1, 11:00-12:00],
 * [orden 1, 12:00-13:00], [orden 2, 10:00-11:00]...</p>
 */
public class DefiniteTimeslot extends RangedTimeslot {
    /**
     * Comienzo del timeslot
     */
    protected final TemporalAccessor start;

    /**
     * Constructor de un <i>timeslot</i> definido sobre un período y con una duración.
     *
     * @param start              comienzo del período, no <code>null</code>
     * @param duration           duración del período, no <code>null</code>
     * @param chronologicalOrder orden cronológico y jerárquico
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
            return super.compareTo(timeslot);

        final DefiniteTimeslot definiteTimeslot = (DefiniteTimeslot) timeslot;

        // Si ambos comienzos son clases comparables y tienen el mismo orden cronológico, se rompe el empate
        // comparando los comienzos
        if (start instanceof Comparable<?> && definiteTimeslot instanceof Comparable<?> &&
                super.compareTo(timeslot) == 0) {
            final Comparable<Comparable<?>> thisStart = (Comparable<Comparable<?>>) start;
            final Comparable<Comparable<?>> otherStart = (Comparable<Comparable<?>>) definiteTimeslot.start;

            // Se invierte el resultado porque los resultados "menores" son los de más alta prioridad
            return -thisStart.compareTo(otherStart);
        } else {
            return super.compareTo(timeslot);
        }
    }

    public String toString() {
        return start + " (" + duration + ") [" + chronologicalOrder + "]";
    }
}
