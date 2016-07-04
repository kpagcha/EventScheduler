package es.uca.garciachacon.eventscheduler.solver.constraint;

import org.chocosolver.solver.constraints.Constraint;

import java.util.List;

/**
 * Construye las restricciones que se publicarán sobre un modelo de un torneo y se publicarán sobre el mismo para su
 * aplicación durante el proceso de resolución.
 *
 */
public class ConstraintBuilder implements ISolverConstraint {
    private final ISolverConstraint solverConstraint;

    /**
     * Construye la restricción
     *
     * @param solverConstraint restricción a publicar
     */
    public ConstraintBuilder(ISolverConstraint solverConstraint) {
        this.solverConstraint = solverConstraint;
    }

    /**
     * Devuelve las restricciones construidas.
     *
     * @return lista de restricciones
     */
    public List<Constraint> getConstraints() {
        return solverConstraint.getConstraints();
    }
}
