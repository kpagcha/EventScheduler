package es.uca.garciachacon.eventscheduler.solver.constraint;

import org.chocosolver.solver.constraints.Constraint;

import java.util.List;

/**
 * Restricción que se publica sobre el modelo de un torneo.
 */
public interface ISolverConstraint {
    /**
     * Devuelve el conjunto de restricciones de las que se compone la restricción.
     *
     * @return lista de restricciones
     */
    List<Constraint> getConstraints();
}
