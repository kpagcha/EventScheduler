package solver.constraint;

import data.model.tournament.event.Event;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.variables.VariableFactory;
import org.chocosolver.util.tools.ArrayUtils;

/**
 * Impone que la suma de timeslots utilizados por el evento se corresponda con el número de encuentros esperados.
 */
public class TotalMatchesConstraint extends EventConstraint {
    public TotalMatchesConstraint(Event event) {
        super(event);

        int eventNumberOfMatches = event.getPlayers().size() * event.getMatchesPerPlayer();

        solver.post(IntConstraintFactory.sum(ArrayUtils.flatten(g[eventIndex]),
                VariableFactory.fixed(eventNumberOfMatches, solver)
        ));
        solver.post(IntConstraintFactory.sum(ArrayUtils.flatten(x[eventIndex]),
                VariableFactory.fixed(eventNumberOfMatches * event.getTimeslotsPerMatch(), solver)
        ));
    }
}
