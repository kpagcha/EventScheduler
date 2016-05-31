package es.uca.garciachacon.eventscheduler.solver.constraint;

import es.uca.garciachacon.eventscheduler.data.model.tournament.event.Event;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.variables.VariableFactory;
import org.chocosolver.util.tools.ArrayUtils;

/**
 * Impone que la suma de timeslots utilizados por el evento se corresponda con el número de encuentros esperados.
 * Esta es una restricción de refuerzo que mejora tiempos de resolución del modelo; no es necesaria para garantizar
 * que el número de partidos que se juegan en el evento es el especificado, esto ya se consigue mediante la
 * publicación de otras restricciones de forma indirecta.
 */
public class TotalMatchesConstraint extends EventConstraint {
    public TotalMatchesConstraint(Event event) {
        super(event);

        int nMatches = event.getPlayers().size() * event.getMatchesPerPlayer();

        constraints.add(IntConstraintFactory.sum(ArrayUtils.flatten(g[eventIndex]),
                VariableFactory.fixed(nMatches, solver)
        ));
        constraints.add(IntConstraintFactory.sum(ArrayUtils.flatten(x[eventIndex]),
                VariableFactory.fixed(nMatches * event.getTimeslotsPerMatch(), solver)
        ));
    }
}
