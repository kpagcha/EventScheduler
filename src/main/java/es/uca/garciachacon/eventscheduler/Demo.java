package es.uca.garciachacon.eventscheduler;

import es.uca.garciachacon.eventscheduler.data.model.schedule.InverseSchedule;
import es.uca.garciachacon.eventscheduler.data.model.tournament.*;
import es.uca.garciachacon.eventscheduler.data.validation.validable.ValidationException;
import es.uca.garciachacon.eventscheduler.solver.TournamentSolver;
import es.uca.garciachacon.eventscheduler.utils.TournamentUtils;
import org.chocosolver.solver.ResolutionPolicy;

import java.util.List;

public class Demo {

    public static void main(String[] args) throws ValidationException {
        List<Player> players = TournamentUtils.buildGenericPlayers(16, "Player");
        List<Localization> courts = TournamentUtils.buildGenericLocalizations(4, "Court");
        List<Timeslot> timeslots = TournamentUtils.buildSimpleTimeslots(6);

        Event event1 = new Event("Event 1", players.subList(0, 10), courts, timeslots);
        Event event2 = new Event("Event 2", players.subList(6, 16), courts, timeslots);

        Tournament tournament = new Tournament("Tournament", event1, event2);
        TournamentSolver solver = tournament.getSolver();

        // solver.setOptimization(TournamentSolver.OptimizationMode.OPTIMAL, ResolutionPolicy.MAXIMIZE);
        solver.setOptimization(TournamentSolver.OptimizationMode.STEP_STRICT, ResolutionPolicy.MAXIMIZE);
        //solver.setSearchStrategy(TournamentSolver.SearchStrategy.MINDOM_UB);

        if (tournament.solve()) {
            System.out.println("Score: " + solver.getScore());
            while (tournament.nextSchedules())
                System.out.println("Score: " + solver.getScore());

            System.out.println(new InverseSchedule(tournament));

            System.out.println("Score: " + solver.getScore());
            System.out.println(solver.getFoundSolutions() + " solutions");
        } else {
            System.out.println("No solution");
        }
    }
}
