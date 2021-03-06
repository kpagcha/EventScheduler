package es.uca.garciachacon.eventscheduler.solver.constraint;

import es.uca.garciachacon.eventscheduler.data.model.tournament.*;
import es.uca.garciachacon.eventscheduler.solver.TournamentSolver.MatchupMode;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Aplica las restricciones que aseguran que los partidos predefinidos tendrán lugar. Según el modo de juego, cada
 * enfrentamiento tendrá lugar una sola vez si el modo es "todos diferentes", tantas veces como número de partidos
 * por jugador defina el evento si el modo es "todos iguales" o al menos una vez si es "cualquiera".
 * <p>
 * Si un enfrentamiento no define ninguna localización o <i>timeslot</i> donde deba tener lugar, se tomarán
 * cualquiera de las disponibles.
 */
public class PredefinedMatchupsConstraint extends EventConstraint {

    public PredefinedMatchupsConstraint(Event event) {
        super(event);

        MatchupMode matchupMode = event.getMatchupMode();

        List<Localization> localizations = event.getLocalizations();
        List<Timeslot> timeslots = event.getTimeslots();
        int nPlayersPerMatch = event.getPlayersPerMatch();
        int nMatchesPerPlayer = event.getMatchesPerPlayer();

        for (Matchup matchup : event.getPredefinedMatchups()) {
            Set<Localization> matchupLocalizations = matchup.getLocalizations();
            Set<Timeslot> matchupTimeslots = matchup.getTimeslots();

            if (matchupLocalizations.isEmpty())
                matchupLocalizations = new HashSet<>(localizations);

            if (matchupTimeslots.isEmpty())
                matchupTimeslots =
                        new HashSet<>(timeslots.subList(0, timeslots.size() - event.getTimeslotsPerMatch() + 1));

            int nLocalizations = matchupLocalizations.size();
            int nTimeslots = matchupTimeslots.size();

            List<Integer> localizationsIndices = new ArrayList<>(nLocalizations);
            List<Integer> timeslotsIndices = new ArrayList<>(nTimeslots);

            matchupLocalizations.forEach(l -> localizationsIndices.add(localizations.indexOf(l)));
            matchupTimeslots.forEach(t -> timeslotsIndices.add(timeslots.indexOf(t)));

            // Todos los posibles enfrentamientos
            IntVar[] possibleMatchups =
                    VariableFactory.boundedArray("PossibleMatchups", nLocalizations * nTimeslots, 0, 1, solver);

            int i = 0;
            for (int c : localizationsIndices) {
                for (int t : timeslotsIndices) {
                    // Posible enfrentamiento en timeslot_t
                    IntVar[] possibleMatchup = new IntVar[nPlayersPerMatch];
                    int p = 0;
                    for (Player player : matchup.getPlayers())
                        possibleMatchup[p++] = g[eventIndex][event.getPlayers().indexOf(player)][c][t];

                    constraints.add(IntConstraintFactory.minimum(possibleMatchups[i++], possibleMatchup));
                }
            }

            switch (matchupMode) {
                case ALL_DIFFERENT:
                    constraints.add(IntConstraintFactory.sum(possibleMatchups, VariableFactory.fixed(1, solver)));
                    break;

                case ALL_EQUAL:
                    constraints.add(IntConstraintFactory.sum(possibleMatchups,
                            VariableFactory.fixed(nMatchesPerPlayer, solver)
                    ));
                    break;

                case ANY:
                    constraints.add(IntConstraintFactory.sum(possibleMatchups,
                            VariableFactory.bounded("NMatches", 1, nMatchesPerPlayer, solver)
                    ));
                    break;

                case CUSTOM:
                    constraints.add(IntConstraintFactory.sum(possibleMatchups,
                            VariableFactory.fixed(matchup.getOccurrences(), solver)
                    ));
            }
        }
    }

}
