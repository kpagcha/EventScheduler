package solver.constraint;

import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;

import data.model.tournament.Tournament;
import data.model.tournament.event.Event;

/**
 * Mapea la matriz del horario a partir de la matriz de los comienzos de partido
 */
public class MatchMappingConstraint extends TournamentConstraint {
	public MatchMappingConstraint(Tournament trmnt) {
		super(trmnt);
		
		int nCategories = tournament.getEvents().size();
		
		// Mapear x_e,p,c,t a partir de los posibles comienzos de partido (g) cuyo rango "cubre" x_t
		for (int e = 0; e < nCategories; e++) {
			Event event = tournament.getEvents().get(e);
			
			int nPlayers = event.getPlayers().size();
			int nLocalizations = event.getLocalizations().size();
			int nTimeslots = event.getTimeslots().size();
			int nTimeslotsPerMatch = event.getMatchDuration();
			
			for (int p = 0; p < nPlayers; p++) {
				for (int c = 0; c < nLocalizations; c++) {
					for (int t = 0; t < nTimeslots; t++) {
						int nRange = nTimeslotsPerMatch;
						
						// para los nTimeslotsPerMatch primeros x que no se pueden mapear a nTimeslotsPerMatch elementos de g
						if (t + 1 < nTimeslotsPerMatch)
							nRange -= nTimeslotsPerMatch - t - 1;
						
						IntVar[] matchBeginningRange = new IntVar[nRange];
						for (int i = 0; i < nRange; i++)
							matchBeginningRange[i] = g[e][p][c][t - i];
						
						// La suma de ese posible rango de g, g_t-n..g_t (siendo n nTimeslotsPerMatch) únicamente
						// puede ser 0 o 1, es decir, que no empiece ningún partido o que empiece, pero nunca puede ser
						// mayor puesto que supondría que dos partidos se superpondrían
						IntVar matchStartSum = VariableFactory.bounded("MatchStartSum", 0, 1, solver);
						solver.post(IntConstraintFactory.sum(matchBeginningRange, matchStartSum));
						solver.post(IntConstraintFactory.arithm(x[e][p][c][t], "=", matchStartSum));
					}
				}
			}
		}
	}
}
