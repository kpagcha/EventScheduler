package solver.constraint;

import data.model.tournament.event.Event;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.variables.VariableFactory;
import org.chocosolver.util.tools.ArrayUtils;

import java.util.ArrayList;

/**
 * Asegurar que el número de partidos que juega cada jugador es el correspondiente al requerido por cada categoría.
 */
public class MatchesPerPlayerConstraint extends EventConstraint {
    public MatchesPerPlayerConstraint(Event e) {
        super(e);

        constraints = new ArrayList<>();

        // Que cada jugador juegue nMatchesPerPlayer partidos
        int playerNumberOfTimeslots = event.getMatchesPerPlayer() * event.getTimeslotsPerMatch();

        for (int p = 0; p < event.getPlayers().size(); p++) {
            constraints.add(IntConstraintFactory.sum(ArrayUtils.flatten(g[eventIndex][p]),
                    VariableFactory.fixed(event.getMatchesPerPlayer(), solver)
            ));
            constraints.add(IntConstraintFactory.sum(ArrayUtils.flatten(x[eventIndex][p]),
                    VariableFactory.fixed(playerNumberOfTimeslots, solver)
            ));
        }
    }
}