package solver.constraint;

import java.util.List;

import org.chocosolver.solver.constraints.Constraint;

public interface ISolverConstraint {
	public List<Constraint> getConstraints();
}
