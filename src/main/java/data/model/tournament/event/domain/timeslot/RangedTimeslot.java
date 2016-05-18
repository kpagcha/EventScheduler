package data.model.tournament.event.domain.timeslot;

import java.time.temporal.TemporalAmount;

/**
 * Representa un <i>timeslot</i> con rango, es decir, una duración o período temporal en el que tiene lugar.
 */
public abstract class RangedTimeslot extends AbstractTimeslot {
    /**
     * Duración del timeslot
     */
    protected final TemporalAmount duration;

    /**
     * Construye un <i>timeslot</i> con duración.
     *
     * @param duration           duración del período, no <code>null</code>
     * @param chronologicalOrder orden cronológico de este <i>timeslot</i>
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
