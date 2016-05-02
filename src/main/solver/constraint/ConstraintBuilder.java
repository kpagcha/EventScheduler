package solver.constraint;

import java.util.List;

import org.chocosolver.solver.constraints.Constraint;

public class ConstraintBuilder implements ISolverConstraint {
	private final ISolverConstraint solverConstraint;
	
	public ConstraintBuilder(ISolverConstraint solverConstraint) {
		this.solverConstraint = solverConstraint;
	}

	public List<Constraint> getConstraints() {
		return solverConstraint.getConstraints();
	}
}
