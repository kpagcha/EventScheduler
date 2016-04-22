package solver.constraint;

import java.util.ArrayList;
import java.util.List;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.IntVar;

import data.model.tournament.Tournament;
import solver.TournamentSolver;

public abstract class SolverConstraint implements ISolverConstraint {
	protected List<Constraint> constraints;
	
	protected TournamentSolver tournamentSolver;
	protected Solver solver;
	protected IntVar[][][][] x;
	protected IntVar[][][][] g;
	
	public SolverConstraint(Tournament t) {
		tournamentSolver = t.getSolver();
        solver = tournamentSolver.getSolver();
        x = tournamentSolver.getMatchesModel();
        g = tournamentSolver.getMatchesBeginningsModel();
        
        constraints = new ArrayList<Constraint>();
	}

    public List<Constraint> getConstraints() {
        return constraints;
    }
}
