package solver.constraint;

import data.model.tournament.Tournament;

public abstract class TournamentConstraint extends SolverConstraint {
	protected Tournament tournament;

    public TournamentConstraint(Tournament t) {
        super(t);
        tournament = t;
    }

}