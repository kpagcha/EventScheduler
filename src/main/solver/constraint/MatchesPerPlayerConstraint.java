package solver.constraint;

import java.util.ArrayList;

import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.variables.VariableFactory;
import org.chocosolver.util.tools.ArrayUtils;

import data.model.tournament.event.Event;

/**
 * Asegurar que el número de partidos que juega cada jugador es el correspondiente al requerido por cada categoría
 */
public class MatchesPerPlayerConstraint extends EventConstraint {
	public MatchesPerPlayerConstraint(Event e) {
		super(e);
		
		constraints = new ArrayList<Constraint>();
		
		// Que cada jugador juegue nMatchesPerPlayer partidos
		int playerNumberOfTimeslots = event.getMatchesPerPlayer() * event.getTimeslotsPerMatch();

		for (int p = 0; p < event.getPlayers().size(); p++) {
			constraints.add(IntConstraintFactory.sum(ArrayUtils.flatten(g[eventIndex][p]), VariableFactory.fixed(event.getMatchesPerPlayer() , solver)));	
			constraints.add(IntConstraintFactory.sum(ArrayUtils.flatten(x[eventIndex][p]), VariableFactory.fixed(playerNumberOfTimeslots, solver)));
		}
	}
}