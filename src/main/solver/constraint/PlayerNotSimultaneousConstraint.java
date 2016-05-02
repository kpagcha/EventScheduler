package solver.constraint;

import java.util.ArrayList;
import java.util.List;

import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;

import data.model.tournament.Tournament;

/**
 * Si un jugador_p juega en m�s de una categor�a, evitar que le coincidan partidos a la misma hora
 */
public class PlayerNotSimultaneousConstraint extends TournamentConstraint {
	public PlayerNotSimultaneousConstraint(Tournament trnmnt) {
		super(trnmnt);
		
		int nAllPlayers = tournament.getAllPlayers().size();
		int nAllTimeslots = tournament.getAllTimeslots().size();
		int nCategories = tournament.getEvents().size();
		
		int[][] playersIndices = tournamentSolver.getPlayersIndices();
		int[][] timeslotsIndices = tournamentSolver.getTimeslotsIndices();
		
		// Para cada jugador del torneo explorar las participaciones en cada categor�a y
		// controlar colisiones que puedan producirse (mismo jugador, mismo timeslot)
		for (int p = 0; p < nAllPlayers; p++) {
			for (int t = 0; t < nAllTimeslots; t++) {
				// Las posibles ocupaciones de pistas del jugador_p a la hora_t
				List<IntVar> courtSum = new ArrayList<IntVar>();
				
				for (int e = 0; e < nCategories; e++) {
					int nLocalizations = tournament.getEvents().get(e).getLocalizations().size();
					
					// Si el jugador_p juega en la categor�a_e a la hora_t
					if (playersIndices[p][e] != -1 && timeslotsIndices[t][e] != -1)
						for (int c = 0; c < nLocalizations; c++) 
							courtSum.add(x[e][playersIndices[p][e]][c][timeslotsIndices[t][e]]);
				}
				
				// Que la suma de las ocupaciones de todas las pistas por parte del
				// jugador_p a la hora_t sea o 0 (no juega a la hora_t) o 1 (el jugador
				// juega a la hora_t en una de las pistas en una de las categor�as)
				solver.post(IntConstraintFactory.sum(
					(IntVar[]) courtSum.toArray(new IntVar[courtSum.size()]),
					VariableFactory.enumerated("PossibleParticipations", new int[]{ 0, 1 }, solver))
				);
			}
		}
	}
}
