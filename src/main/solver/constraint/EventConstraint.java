package solver.constraint;

import data.model.tournament.event.Event;

public abstract class EventConstraint extends SolverConstraint {
	protected Event event;
	protected int eventIndex;

	public EventConstraint(Event e) {
		super(e.getTournament());
		
		event = e;
		eventIndex = e.getTournament().getEvents().indexOf(e);
    }
}
