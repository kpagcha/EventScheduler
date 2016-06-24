package es.uca.garciachacon.eventscheduler.solver.constraint;

import es.uca.garciachacon.eventscheduler.data.model.tournament.Event;
import es.uca.garciachacon.eventscheduler.data.model.tournament.Timeslot;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;

import java.util.List;

/**
 * Define la restricción para que solamemente haya dos posibles situaciones en las que haya jugadores en una
 * localización de juego determinada a una hora en concreto: o 0 (nadie) o el número de jugadores por partido de la
 * categoría, es decir, que tenga lugar un enfrentamiento o que no.
 */
public class LocalizationOccupationConstraint extends EventConstraint {

    public LocalizationOccupationConstraint(Event e) {
        super(e);

        int nPlayers = e.getPlayers().size();
        int nLocalizations = e.getLocalizations().size();
        int nTimeslots = e.getTimeslots().size();
        int nPlayersPerMatch = e.getPlayersPerMatch();
        List<Timeslot> timeslots = e.getTimeslots();

        for (int c = 0; c < nLocalizations; c++) {
            for (int t = 0; t < nTimeslots; t++) {
                // Si la hora_t es un break, no hace falta tenerla en cuenta
                if (!e.isBreak(timeslots.get(t))) {
                    // Las "participaciones" de todos los jugadores en la pista_c a la hora_t
                    IntVar[] playerSum = new IntVar[nPlayers];
                    for (int p = 0; p < nPlayers; p++)
                        playerSum[p] = x[eventIndex][p][c][t];

                    // Que la suma de las participaciones de todos los jugadores sea igual a 0 o el número de
                    // jugadores por partido, es decir, que nadie juegue o que jueguen el número de jugadores
                    // requeridos por partido
                    constraints.add(IntConstraintFactory.sum(
                            playerSum,
                            VariableFactory.enumerated("Sum", new int[]{ 0, nPlayersPerMatch }, solver)
                    ));
                }
            }
        }
    }

}
