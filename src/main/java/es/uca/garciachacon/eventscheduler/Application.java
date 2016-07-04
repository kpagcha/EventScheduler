package es.uca.garciachacon.eventscheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import es.uca.garciachacon.eventscheduler.data.model.tournament.*;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class Application {
    /*public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("0 Zarlon");
        System.out.println("1 Simple");
        System.out.println("2 Teams and ALL_DIFFERENT");
        System.out.print("Choose tournament: ");
        int tournamentOption = sc.nextInt();

        Tournament t;
        switch (tournamentOption) {
            case 0:
                t = TournamentUtils.getZarlonTournament();
                break;
            case 1:
                t = TournamentUtils.getSimpleTournament();
                break;
            case 2:
                t = TournamentUtils.getTournamentWithTeamsAndAllDifferentMatchupMode();
                break;
            default:
                t = TournamentUtils.getSimpleTournament();
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

                    if (tournament.getEventSchedules() != null) {
                        TournamentSchedule combinedSchedule = tournament.getSchedule();

                        System.out.println("All schedules combined in one");
                        System.out.println(combinedSchedule);

                        if (printMatches) {
                            List<Match> matches = combinedSchedule.getMatches();
                            System.out.println("All matches (" + matches.size() + ")");
                            matches.forEach(System.out::println);
                            System.out.println();
                        }

                        InverseSchedule groupedSchedule = new InverseSchedule(tournament);
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
    }*/

    public static void main(String[] args) throws JsonProcessingException {
        Player querry = new Player("Sam Querrey");
        Player mahut = new Player("Nicolas Mahut");
        Player goffin = new Player("David Goffin");
        Player raonic = new Player("Milos Raonic");
        Player federer = new Player("Roger Federer");
        Player johnson = new Player("Steve Johnson");
        Player cilic = new Player("Marin Cilic");
        Player nishikori = new Player("Kei Nishikori");
        Player vesely = new Player("Jiri Vesely");
        Player berdych = new Player("Tomas Berdych");
        Player tomic = new Player("Bernard Tomic");
        Player pouille = new Player("Lucas Pouille");
        Player gasquet = new Player("Richard Gasquet");
        Player tsonga = new Player("Jo-Wilfried Tsonga");
        Player kyrgios = new Player("Nick Kyrgios");
        Player murray = new Player("Andy Murray");

        Localization centreCourt = new Localization("Centre Court");
        Localization no1Court = new Localization("No. 1 Court");
        Localization no2Court = new Localization("No. 2 Court");
        Localization no3Court = new Localization("No. 3 Court");
        Localization court12 = new Localization("Court 12");
        Localization court18 = new Localization("Court 18");

        Timeslot timeslot11am = new Timeslot(1, LocalTime.of(11, 0), Duration.ofHours(1));
        Timeslot timeslot12pm = new Timeslot(1, LocalTime.of(12, 0), Duration.ofHours(1));
        Timeslot timeslot13pm = new Timeslot(1, LocalTime.of(13, 0), Duration.ofHours(1));
        Timeslot timeslot14pm = new Timeslot(1, LocalTime.of(14, 0), Duration.ofHours(1));
        Timeslot timeslot15pm = new Timeslot(1, LocalTime.of(15, 0), Duration.ofHours(1));
        Timeslot timeslot16pm = new Timeslot(1, LocalTime.of(16, 0), Duration.ofHours(1));
        Timeslot timeslot17pm = new Timeslot(1, LocalTime.of(17, 0), Duration.ofHours(1));
        Timeslot timeslot18pm = new Timeslot(1, LocalTime.of(18, 0), Duration.ofHours(1));
        Timeslot timeslot19pm = new Timeslot(1, LocalTime.of(19, 0), Duration.ofHours(1));

        List<Player> atpPlayers = Arrays.asList(
                querry,
                mahut,
                goffin,
                raonic,
                federer,
                johnson,
                cilic,
                nishikori,
                vesely,
                berdych,
                tomic,
                pouille,
                gasquet,
                tsonga,
                kyrgios,
                murray
        );

        List<Localization> wimbledonCourts = Arrays.asList(centreCourt, no1Court, no2Court, no3Court, court12, court18);

        List<Timeslot> monday4July2016 = Arrays.asList(
                timeslot11am,
                timeslot12pm,
                timeslot13pm,
                timeslot14pm,
                timeslot15pm,
                timeslot16pm,
                timeslot17pm,
                timeslot18pm,
                timeslot19pm
        );

        Event gentlemenFourthRound = new Event("Gentlemen R16", atpPlayers, wimbledonCourts, monday4July2016);

        gentlemenFourthRound.setTimeslotsPerMatch(3);

        gentlemenFourthRound.addPlayerInLocalization(federer, centreCourt);
        gentlemenFourthRound.addPlayerInLocalization(murray, centreCourt);

        gentlemenFourthRound.addPlayerAtTimeslotRange(federer, timeslot12pm, timeslot14pm);
        gentlemenFourthRound.addPlayerAtTimeslotRange(murray, timeslot15pm, timeslot17pm);

        gentlemenFourthRound.addMatchup(federer, johnson);
        gentlemenFourthRound.addMatchup(kyrgios, murray);
        gentlemenFourthRound.addMatchup(new Matchup(
                new HashSet<>(Arrays.asList(gasquet, tsonga)),
                new HashSet<>(Arrays.asList(no1Court)),
                new HashSet<>()
        ));
        gentlemenFourthRound.addMatchup(cilic, nishikori);
        gentlemenFourthRound.addMatchup(goffin, raonic);
        gentlemenFourthRound.addMatchup(vesely, berdych);
        gentlemenFourthRound.addMatchup(tomic, pouille);
        gentlemenFourthRound.addMatchup(querry, mahut);


        Player williamsSerena = new Player("S. Williams");
        Player kuznetsova = new Player("S. Kuznetsova");
        Player pavlyuchenkova = new Player("A. Pavlyuchenkova ");
        Player vandeweghe = new Player("C. Vandeweghe");
        Player radwanska = new Player("A. Radwanska");
        Player cibulkova = new Player("D. Cibulkova");
        Player makarova = new Player("E. Makarova");
        Player vesnina = new Player("E. Vesnina");
        Player halep = new Player("S. Halep");
        Player keys = new Player("M. Keys");
        Player doi = new Player("M. Doi");
        Player kerber = new Player("A. Kerber");
        Player williamsVenus = new Player("V. Williams");
        Player suarezNavarro = new Player("C. Suarez Navarro");
        Player shvedova = new Player("Y. Shvedova");
        Player safarova = new Player("L. Safarova");

        List<Player> wtaPlayers = Arrays.asList(
                williamsSerena,
                kuznetsova,
                pavlyuchenkova,
                vandeweghe,
                radwanska,
                cibulkova,
                makarova,
                vesnina,
                halep,
                keys,
                doi,
                kerber,
                williamsVenus,
                suarezNavarro,
                shvedova,
                safarova
        );

        Event ladiesFourthRound = new Event("Lades R16", wtaPlayers, wimbledonCourts, monday4July2016);

        ladiesFourthRound.addMatchup(williamsSerena, kuznetsova);
        ladiesFourthRound.addMatchup(pavlyuchenkova, vandeweghe);
        ladiesFourthRound.addMatchup(radwanska, cibulkova);
        ladiesFourthRound.addMatchup(makarova, vesnina);
        ladiesFourthRound.addMatchup(halep, keys);
        ladiesFourthRound.addMatchup(doi, kerber);
        ladiesFourthRound.addMatchup(williamsVenus, suarezNavarro);
        ladiesFourthRound.addMatchup(shvedova, safarova);

        Tournament wimbledon = new Tournament("Wimbledon 2016", gentlemenFourthRound, ladiesFourthRound);

        System.out.println(wimbledon.toJson());
    }
}
