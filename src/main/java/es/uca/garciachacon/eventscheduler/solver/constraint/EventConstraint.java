package es.uca.garciachacon.eventscheduler.solver.constraint;

import es.uca.garciachacon.eventscheduler.data.model.tournament.Event;

public abstract class EventConstraint extends SolverConstraint {
    protected final Event event;
    protected final int eventIndex;

    public EventConstraint(Event e) {
        super(e.getTournament());

        event = e;
        eventIndex = e.getTournament().getEvents().indexOf(e);
    }
}
