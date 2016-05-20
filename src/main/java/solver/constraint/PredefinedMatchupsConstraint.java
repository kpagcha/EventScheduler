package solver.constraint;

import data.model.tournament.event.Event;
import data.model.tournament.event.domain.Player;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;
import solver.TournamentSolver.MatchupMode;

import java.util.Set;

/**
 * Aplica las restricciones que aseguran que los partidos predefinidos tendrán lugar. Según el modo de juego, cada
 * enfrentamiento tendrá lugar una sola vez si el modo es "todos diferentes", tantas veces como número de partidos
 * por jugador defina el evento si el modo es "todos iguales" o al menos una vez si es "cualquiera".
 */
public class PredefinedMatchupsConstraint extends EventConstraint {

    public PredefinedMatchupsConstraint(Event event) {
        super(event);

        MatchupMode matchupMode = event.getMatchupMode();

        int nLocalizations = event.getLocalizations().size();
        int nTimeslots = event.getTimeslots().size();
        int nPlayersPerMatch = event.getPlayersPerMatch();
        int nMatchesPerPlayer = event.getMatchesPerPlayer();

        for (Set<Player> matchup : event.getPredefinedMatchups()) {
            // Todos los posibles enfrentamientos
            IntVar[] possibleMatchups =
                    VariableFactory.boundedArray("PossibleMatchups", nLocalizations * nTimeslots, 0, 1, solver);

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
                    solver.post(IntConstraintFactory.sum(possibleMatchups,
                            VariableFactory.fixed(nMatchesPerPlayer, solver)
                    ));
                    break;

                case ANY:
                    solver.post(IntConstraintFactory.sum(possibleMatchups,
                            VariableFactory.bounded("NMatches", 1, nMatchesPerPlayer, solver)
                    ));
                    break;
            }
        }
    }

}
