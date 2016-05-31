package es.uca.garciachacon.eventscheduler.solver.constraint;

import org.chocosolver.solver.constraints.Constraint;

import java.util.List;

public interface ISolverConstraint {
    List<Constraint> getConstraints();
}
