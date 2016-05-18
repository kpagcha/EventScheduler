package solver.constraint;

import data.model.tournament.Tournament;

/**
 * Clase abstracta que representa una restricción a aplicar sobre un torneo.
 */
public abstract class TournamentConstraint extends SolverConstraint {
    protected final Tournament tournament;

    /**
     * Construye una restricción del torneo.
     *
     * @param t torneo al que se le aplica la restricción
     */
    public TournamentConstraint(Tournament t) {
        super(t);
        tournament = t;
    }

}