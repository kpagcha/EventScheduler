package solver.constraint;

import org.chocosolver.solver.constraints.Constraint;

import java.util.List;

public class ConstraintBuilder implements ISolverConstraint {
    private final ISolverConstraint solverConstraint;

    public ConstraintBuilder(ISolverConstraint solverConstraint) {
        this.solverConstraint = solverConstraint;
    }

    public List<Constraint> getConstraints() {
        return solverConstraint.getConstraints();
    }
}
