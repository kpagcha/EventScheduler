package es.uca.garciachacon.eventscheduler.solver.constraint;

import es.uca.garciachacon.eventscheduler.data.model.tournament.Tournament;
import es.uca.garciachacon.eventscheduler.solver.TournamentSolver;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.IntVar;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase abstracta que representa una restricción que se aplica sobre un modelo o problema de un torneo deportivo a ser
 * resuelto por un <i>solucionador</i> o <i>solver</i>.
 * <p>
 * <p>Una restricción del modelo se construye a partir de un objeto torneo ({@see Tournament)}). De éste obtiene el
 * <i>solver</i> interno utilizado por el <i>solver</i> principal ({@see TournamentSolver}, además como las matrices
 * internas de este último que representan el modelo. La información de estas matrices en su estado inicial será el
 * utilizado por el mecanismo que defina esta clase para publicar sobre el modelo la restricción especificada.</p>
 */
public abstract class SolverConstraint implements ISolverConstraint {
    protected List<Constraint> constraints = new ArrayList<>();

    protected final TournamentSolver tournamentSolver;
    protected final Solver solver;
    protected final IntVar[][][][] x;
    protected final IntVar[][][][] g;

    /**
     * Construye una restricción a apliacar sobre el modelo de un torneo.
     *
     * @param t torneo del que se obtiene el <i>solver</i> interno de Choco 3, así como las matrices internas que
     *          representan el modelo
     */
    public SolverConstraint(Tournament t) {
        tournamentSolver = t.getSolver();
        solver = tournamentSolver.getInternalSolver();
        x = tournamentSolver.getMatchesModel();
        g = tournamentSolver.getMatchesBeginningsModel();
    }

    /**
     * Devuelve la lista de restricciones elaborada por clases que heredan de ésta.
     *
     * @return lista de restricciones; vacía si no se añade ninguna.
     */
    public List<Constraint> getConstraints() {
        return constraints;
    }
}
