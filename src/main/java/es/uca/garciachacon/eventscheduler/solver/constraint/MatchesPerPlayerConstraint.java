package es.uca.garciachacon.eventscheduler.solver.constraint;

import es.uca.garciachacon.eventscheduler.data.model.tournament.event.Event;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.variables.VariableFactory;
import org.chocosolver.util.tools.ArrayUtils;

/**
 * Asegurar que el número de partidos que juega cada jugador es el correspondiente al requerido por cada categoría.
 */
public class MatchesPerPlayerConstraint extends EventConstraint {
    public MatchesPerPlayerConstraint(Event e) {
        super(e);

        // Que cada jugador juegue nMatchesPerPlayer partidos
        int nMatchesPerPlayer = event.getMatchesPerPlayer();
        int nTimeslotsPerPlayer = nMatchesPerPlayer * event.getTimeslotsPerMatch();

        for (int p = 0; p < event.getPlayers().size(); p++) {
            constraints.add(IntConstraintFactory.sum(ArrayUtils.flatten(g[eventIndex][p]),
                    VariableFactory.fixed(nMatchesPerPlayer, solver)
            ));
            constraints.add(IntConstraintFactory.sum(ArrayUtils.flatten(x[eventIndex][p]),
                    VariableFactory.fixed(nTimeslotsPerPlayer, solver)
            ));
        }
    }
}