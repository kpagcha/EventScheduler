package solver.constraint;

import java.util.Set;

import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;

import data.model.tournament.Tournament;
import data.model.tournament.event.Event;
import data.model.tournament.event.entity.Player;
import solver.TournamentSolver.MatchupMode;

/**
 * Aplica las restricciones que aseguran que los partidos predefinidos tendr�n lugar. Seg�n el modo de juego, cada
 * enfrentamiento tendr� lugar una sola vez si el modo es "todos diferentes", tantas veces como n�mero de partidos
 * por jugador defina el evento si el modo es "todos iguales" o al menos una vez si es "cualquiera"
 */
public class PredefinedMatchupsConstraint extends EventConstraint {

	public PredefinedMatchupsConstraint(Event event, Tournament tournament) {
		super(event, tournament);

		MatchupMode matchupMode = event.getMatchupMode();
		
		int nLocalizations = event.getLocalizations().size();
		int nTimeslots = event.getTimeslots().size();
		int nPlayersPerMatch = event.getPlayersPerMatch();
		int nMatchesPerPlayer = event.getMatchesPerPlayer();
		
		for (Set<Player> matchup : tournamentSolver.getPredefinedMatchups().get(event)) {
			// Todos los posibles enfrentamientos
			IntVar[] possibleMatchups = VariableFactory.boundedArray("PossibleMatchups", nLocalizations * nTimeslots, 0, 1, solver);
			
			int i = 0;
			for (int c = 0; c < nLocalizations; c++) {	
				for (int t = 0; t < nTimeslots; t++) {
					// Posible enfrentamiento en timeslot_t
					IntVar[] possibleMatchup = new IntVar[nPlayersPerMatch];
					int p = 0;
					for (Player player : matchup)
						possibleMatchup[p++] = g[eventIndex][event.getPlayers().indexOf(player)][c][t];
					
					solver.post(IntConstraintFactory.minimum(possibleMatchups[i++], possibleMatchup));
				}
			}

			switch (matchupMode) {
				case ALL_DIFFERENT:
					solver.post(IntConstraintFactory.sum(possibleMatchups, VariableFactory.fixed(1, solver)));
					break;
					
				case ALL_EQUAL:
					solver.post(IntConstraintFactory.sum(possibleMatchups, VariableFactory.fixed(nMatchesPerPlayer, solver)));
					break;
					
				case ANY:
					solver.post(IntConstraintFactory.sum(possibleMatchups, VariableFactory.bounded("NMatches", 1, nMatchesPerPlayer, solver)));
					break;
			}
		}
	}

}