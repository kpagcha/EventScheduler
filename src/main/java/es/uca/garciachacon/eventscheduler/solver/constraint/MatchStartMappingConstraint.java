package es.uca.garciachacon.eventscheduler.solver.constraint;

import es.uca.garciachacon.eventscheduler.data.model.tournament.Event;
import es.uca.garciachacon.eventscheduler.data.model.tournament.Tournament;
import org.chocosolver.solver.constraints.IntConstraintFactory;
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
                        // Mapear g_e,p,c,t a partir del rango que g_e,p,c,t cubre en x, es decir, [x_e,p,c,t,
                        // x_e,p,c,t+n] (n es el número de timeslots por partido). En términos de operación
                        // booleana, a g_t se asignaría el valor [0, 1] a partir de la operación "and" aplicada
                        // sobre ese rango en x correspondiente a g_t, es decir, si todos los x en el rango son
                        // 1, entonces efectivamente el partido empieza en g_t, luego se marca con 1. Si hay al
                        // menos un elemento del rango en x que sea 0 quiere decir que ese rango no corresponde a
                        // un partido, luego en g_t no empieza un partido y se marca como 0

                        IntVar[] matchRange = new IntVar[nTimeslotsPerMatch];
                        System.arraycopy(x[e][p][c], t, matchRange, 0, nTimeslotsPerMatch);

                        //BoolVar matchTakesPlace = VariableFactory.bool("MatchTakesPlace", solver);
                        IntVar matchTakesPlace = VariableFactory.bounded("MatchTakesPlace", 0, 1, solver);
                        constraints.add(IntConstraintFactory.minimum(matchTakesPlace, matchRange));
                        constraints.add(IntConstraintFactory.arithm(g[e][p][c][t], "=", matchTakesPlace));
                    }

                    // Si un partido dura un timeslot, se termina de asignar el valor al último timeslot que no
                    // entraba en el bucle anterior. Su valor será el mismo que el de la matriz x porque el número de
                    // timeslots por partido es 1, luego las matrices g y x son iguales.
                    if (nTimeslotsPerMatch == 1)
                        constraints.add(IntConstraintFactory.arithm(
                                g[e][p][c][nTimeslots - 1],
                                "=",
                                x[e][p][c][nTimeslots - 1]
                        ));
                }
            }
        }
    }

}
