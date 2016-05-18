import data.model.schedule.LocalizationSchedule;
import data.model.schedule.Match;
import data.model.schedule.TournamentSchedule;
import data.model.tournament.Tournament;
import data.model.tournament.event.domain.Player;
import data.validation.validable.ValidationException;
import solver.TournamentSolver.SearchStrategy;
import utils.TournamentUtils;

import java.util.List;
import java.util.Scanner;

public class Application {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("0 Zarlon");
        System.out.println("1 Tournament");
        System.out.println("2 ATP");
        System.out.println("3 Different domains");
        System.out.print("Choose tournament: ");
        int tournamentOption = sc.nextInt();

        Tournament t;
        switch (tournamentOption) {
            case 0:
                t = TournamentUtils.getZarlonTournament();
                break;
            case 1:
                t = TournamentUtils.getSampleTournament();
                break;
            case 2:
                t = TournamentUtils.getSampleAtp();
                break;
            case 3:
                t = TournamentUtils.getSampleWithDifferentDomains();
                break;
            default:
                t = TournamentUtils.getSampleTournament();
                break;
        }

        System.out.println("\n1 domOverWDeg");
        System.out.println("2 minDom_UB");
        System.out.println("3 minDom_LB");
        System.out.print("Choose Search Strategy: ");
        int searchStrategyOption = sc.nextInt();

        SearchStrategy searchStrategy = null;
        switch (searchStrategyOption) {
            case 1:
                searchStrategy = SearchStrategy.DOMOVERWDEG;
                break;
            case 2:
                searchStrategy = SearchStrategy.MINDOM_UB;
                break;
            case 3:
                searchStrategy = SearchStrategy.MINDOM_LB;
                break;
        }

        t.getSolver().setSearchStrategy(searchStrategy);


        final Tournament tournament = t;

        boolean printSolutions = true;
        boolean printMatches = true;
        boolean askForInput = false;
        boolean printMatchesByPlayer = false;
        int maxSolutions = 1; // 0 -> todas las soluciones
        int foundSolutions = 0;

        boolean solutionFound = false;
        try {
            solutionFound = tournament.solve();
        } catch (ValidationException e) {
            t.getMessages().forEach(System.out::println);
        }

        if (solutionFound) {
            do {
                if (printSolutions) {
                    System.out.println("-------------------------------------------------------");
                    System.out.println(tournament + "\n");
                    tournament.printCurrentSchedules(printMatches);

                    if (tournament.getCurrentSchedules() != null) {
                        TournamentSchedule combinedSchedule = tournament.getSchedule();

                        System.out.println("All schedules combined in one");
                        System.out.println(combinedSchedule);

                        if (printMatches) {
                            List<Match> matches = combinedSchedule.getMatches();
                            System.out.println("All matches (" + matches.size() + ")");
                            matches.forEach(System.out::println);
                            System.out.println();
                        }

                        LocalizationSchedule groupedSchedule = new LocalizationSchedule(tournament);
                        System.out.println("Combined schedule grouped by courts");
                        System.out.println(groupedSchedule);

                        int occupation = groupedSchedule.getOccupation();
                        int availableTimeslots = groupedSchedule.getAvailableTimeslots();
                        System.out.println(String.format(
                                "Timeslot (%s) occupation: %s/%s (%s %%)\n",
                                groupedSchedule.getTotalTimeslots(),
                                occupation,
                                availableTimeslots,
                                (occupation / (double) availableTimeslots) * 100
                        ));

                        if (printMatchesByPlayer) {
                            for (Player player : tournament.getAllPlayers()) {
                                System.out.println(player + " matches:");
                                combinedSchedule.filterMatchesByPlayer(player).forEach(System.out::println);
                                System.out.println();
                            }
                        }
                    }
                }

                if (solutionFound)
                    foundSolutions++;

                if (askForInput) {
                    System.out.print("Show next solution (y/n)?: ");
                    String input = sc.next();
                    if (!input.equalsIgnoreCase("y"))
                        break;
                }

                if (maxSolutions > 0 && foundSolutions >= maxSolutions)
                    break;

            } while (tournament.nextSchedules());
        }

        sc.close();

        System.out.println("\n" + foundSolutions + " solutions found.");
    }
}
