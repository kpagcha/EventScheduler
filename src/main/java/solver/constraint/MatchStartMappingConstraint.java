package solver.constraint;

import data.model.tournament.Tournament;
import data.model.tournament.event.Event;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;

/**
 * Mapea los comienzos de los partidos a partir de la asignación de horas en la matriz del horario.
 */
public class MatchStartMappingConstraint extends TournamentConstraint {

    public MatchStartMappingConstraint(Tournament trnmnt) {
        super(trnmnt);

        int nCategories = tournament.getEvents().size();

        // Mapear entre los comienzos de cada partido (g) y las horas en las que se juega
        for (int e = 0; e < nCategories; e++) {
            Event event = tournament.getEvents().get(e);

            int nPlayers = event.getPlayers().size();
            int nLocalizations = event.getLocalizations().size();
            int nTimeslots = event.getTimeslots().size();
            int nTimeslotsPerMatch = event.getTimeslotsPerMatch();

            for (int p = 0; p < nPlayers; p++) {
                for (int c = 0; c < nLocalizations; c++) {
                    for (int t = 0; t < nTimeslots - nTimeslotsPerMatch; t++) {
                        if (nTimeslotsPerMatch == 1) {
                            // Si un partido dura un timeslot la matriz g es idéntica a la matriz x
                            solver.post(IntConstraintFactory.arithm(g[e][p][c][t], "=", x[e][p][c][t]));
                        } else {
                            // Mapear g_e,p,c,t a partir del rango que g_e,p,c,t cubre en x, es decir,
                            // [x_e,p,c,t, x_e,p,c,t+n] (n es el número de timeslots por partido).
                            // En términos de operación booleana, a g_t se asignaría el valor [0, 1] a partir
                            // de la operación "and" aplicada sobre ese rango en x correspondiente a g_t,
                            // es decir, si todos los x en el rango son 1, entonces efectivamente el
                            // partido empieza en g_t, luego se marca con 1. Si hay al menos un elemento
                            // del rango en x que sea 0 quiere decir que ese rango no corresponde a un partido,
                            // luego en g_t no empieza un partido y se marca como 0

                            IntVar[] matchRange = new IntVar[nTimeslotsPerMatch];
                            System.arraycopy(x[e][p][c], t, matchRange, 0, nTimeslotsPerMatch);

                            BoolVar matchTakesPlace = VariableFactory.bool("MatchTakesPlace", solver);
                            solver.post(IntConstraintFactory.minimum(matchTakesPlace, matchRange));
                            solver.post(IntConstraintFactory.arithm(g[e][p][c][t], "=", matchTakesPlace));
                        }
                    }
                    if (nTimeslotsPerMatch == 1) {
                        // Si un partido dura un timeslot la matriz g es idéntica a la matriz x
                        solver.post(IntConstraintFactory.arithm(g[e][p][c][nTimeslots - 1],
                                "=",
                                x[e][p][c][nTimeslots - 1]
                        ));
                    } else {
                        // Si un partido dura más de un timeslot se marcan los últimos elementos de la matriz de
                        // comienzos de partidos (g)
                        // con 0 para evitar que dé comienzo un partido que salga del rango del dominio de los
                        // timeslots por causa del
                        // rango de la duración del propio partido
                        for (int i = 0; i < nTimeslotsPerMatch - 1; i++)
                            solver.post(IntConstraintFactory.arithm(g[e][p][c][nTimeslots - i - 1], "=", 0));
                    }
                }
            }
        }
    }

}
