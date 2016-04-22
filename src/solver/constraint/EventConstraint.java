package solver.constraint;

import data.model.tournament.Tournament;
import data.model.tournament.event.Event;

public abstract class EventConstraint extends SolverConstraint {
	protected Event event;
	protected int eventIndex;

	public EventConstraint(Event e, Tournament t) {
		super(t);
		
		event = e;
		eventIndex = t.getEvents().indexOf(e);
    }
}
