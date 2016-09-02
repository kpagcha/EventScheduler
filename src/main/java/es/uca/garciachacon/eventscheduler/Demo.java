package es.uca.garciachacon.eventscheduler;

import es.uca.garciachacon.eventscheduler.data.model.tournament.*;
import es.uca.garciachacon.eventscheduler.data.validation.validable.ValidationException;
import es.uca.garciachacon.eventscheduler.utils.TournamentUtils;

import java.util.List;

public class Demo {

    public static void main(String[] args) throws ValidationException {
        List<Player> players = TournamentUtils.buildGenericPlayers(4, "Player");
        List<Localization> courts = TournamentUtils.buildGenericLocalizations(2, "Court");
        List<Timeslot> timeslots = TournamentUtils.buildSimpleTimeslots(6);

        Event event = new Event("Event", players, courts, timeslots);

        Tournament tournament = new Tournament("Tournament", event);
        tournament.getSolver().setObjective(true);

        if (tournament.solve()) {
            tournament.printCurrentSchedules();

            while (tournament.nextSchedules());
            System.out.println(tournament.getSolver().getFoundSolutions() + " found solutions");
        } else {
            System.out.println("No solution");
        }
    }
}
