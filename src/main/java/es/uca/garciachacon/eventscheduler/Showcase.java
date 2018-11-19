package es.uca.garciachacon.eventscheduler;

import es.uca.garciachacon.eventscheduler.data.model.schedule.InverseSchedule;
import es.uca.garciachacon.eventscheduler.data.model.tournament.*;
import es.uca.garciachacon.eventscheduler.data.validation.validable.ValidationException;
import es.uca.garciachacon.eventscheduler.solver.TournamentSolver;
import es.uca.garciachacon.eventscheduler.utils.TournamentUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class Showcase {

    /**
     * Torneo simple: ejemplo instanciación
     */
    private static void demo1() throws ValidationException  {
        List<Player> players = TournamentUtils.buildGenericPlayers(16, "Jug");
        List<Localization> courts = TournamentUtils.buildGenericLocalizations(3, "Pista");
        List<Timeslot> timeslots = TournamentUtils.buildSimpleTimeslots(6);

        // crear división: compuesta de jugadores, pistas y horas
        Event division = new Event("División", players, courts, timeslots);

        // crear torneo: objecto central
        Tournament torneo = new Tournament("Torneo", division);
        TournamentSolver solver = torneo.getSolver();

        // primera solución
        if (torneo.solve()) {
            torneo.printCurrentSchedules();
            System.out.println(new InverseSchedule(torneo));
            System.out.println(solver.getResolutionData());
        }
    }

    /**
     * Torneo simple: configuraciones básicas
     */
    private static void demo2() throws ValidationException  {
        List<Player> players = TournamentUtils.buildGenericPlayers(16, "Jug");
        List<Localization> courts = TournamentUtils.buildGenericLocalizations(3, "Pista");
        List<Timeslot> timeslots = TournamentUtils.buildSimpleTimeslots(14);

        // crear división: compuesta de jugadores, pistas y horas
        Event division = new Event("División", players, courts, timeslots);

        // modificamos la duración de cada partido
        division.setTimeslotsPerMatch(2);

        // modificamos el número de jugadores que forman cada partido
        division.setPlayersPerMatch(2);

        // modificamos el número de partidos que tiene que jugar cada jugador
        division.setMatchesPerPlayer(2);

        // division.setMatchupMode(TournamentSolver.MatchupMode.ALL_DIFFERENT);

        // crear torneo: objecto central
        Tournament torneo = new Tournament("Torneo", division);
        TournamentSolver solver = torneo.getSolver();

        // solver.setOptimization(TournamentSolver.OptimizationMode.OPTIMAL);
        solver.setSearchStrategy(TournamentSolver.SearchStrategy.MINDOM_UB);

        if (torneo.solve()) {
            torneo.printCurrentSchedules();
            System.out.println(new InverseSchedule(torneo));
        }
    }

    /**
     * Torneo simple: estrategias de resolución
     */
    private static void demo3() throws ValidationException  {
        List<Player> players = TournamentUtils.buildGenericPlayers(8, "Jug");
        List<Localization> courts = TournamentUtils.buildGenericLocalizations(3, "Pista");
        List<Timeslot> timeslots = TournamentUtils.buildSimpleTimeslots(5);

        // crear división: compuesta de jugadores, pistas y horas
        Event division = new Event("División", players, courts, timeslots);

        // crear torneo: objecto central
        Tournament torneo = new Tournament("Torneo", division);
        TournamentSolver solver = torneo.getSolver();

        // solver.setOptimization(TournamentSolver.OptimizationMode.OPTIMAL);

        // implementación Choco, resultado "aleatorio"
        solver.setSearchStrategy(TournamentSolver.SearchStrategy.DOMOVERWDEG);

        // upper bound
        // solver.setSearchStrategy(TournamentSolver.SearchStrategy.MINDOM_UB);

        // lower bound
        // solver.setSearchStrategy(TournamentSolver.SearchStrategy.MINDOM_LB);

        if (torneo.solve()) {
            torneo.printCurrentSchedules();
            System.out.println(new InverseSchedule(torneo));
        }
    }

    /**
     * Torneo simple: optimización
     */
    private static void demo4() throws ValidationException  {
        List<Player> players = TournamentUtils.buildGenericPlayers(8, "Jug");
        List<Localization> courts = TournamentUtils.buildGenericLocalizations(4, "Pista");
        List<Timeslot> timeslots = TournamentUtils.buildSimpleTimeslots(8);

        // crear división: compuesta de jugadores, pistas y horas
        Event division = new Event("División", players, courts, timeslots);

        for (int i = 0; i < players.size(); i += 2)
            division.addMatchup(players.get(i), players.get(i + 1));

        // crear torneo: objecto central
        Tournament torneo = new Tournament("Torneo", division);
        TournamentSolver solver = torneo.getSolver();

        // solver.setOptimization(TournamentSolver.OptimizationMode.OPTIMAL);

        if (torneo.solve()) {
            torneo.printCurrentSchedules();
            System.out.println(new InverseSchedule(torneo));
        }

        // solver.setOptimization(TournamentSolver.OptimizationMode.STEP);
        // solver.setOptimization(TournamentSolver.OptimizationMode.STEP_STRICT);
        // if (torneo.solve()) {
        //     while (torneo.nextSchedules()) {
        //         System.out.println(solver.getScore());
        //         System.out.println(new InverseSchedule(torneo));
        //     }
        // }
    }

    /**
     * Torneo con varios eventos
     */
    private static void demo5() throws ValidationException  {
        List<Player> players = TournamentUtils.buildGenericPlayers(20, "Jug");
        List<Localization> courts = TournamentUtils.buildGenericLocalizations(4, "Pista");
        List<Timeslot> timeslots = TournamentUtils.buildSimpleTimeslots(10);

        // creamos divisiones
        // cada división tiene sus jugadores, pistas y horas; pero cualquiera de estas dimensiones puede ser
        // compartida parcial o totalmente con cualquier otra división o divisiones
        Event benjamin = new Event("Benjamín", players.subList(0, 8), courts, timeslots);
        Event alevin = new Event("Alevín", players.subList(8, 12), courts, timeslots);
        Event infantil = new Event("Infantil", players.subList(12, 20), courts, timeslots);

        // crear torneo: objecto central
        Tournament torneo = new Tournament("Torneo", benjamin, alevin, infantil);

        if (torneo.solve()) {
            torneo.printCurrentSchedules();
            System.out.println(torneo.getSchedule());
            System.out.println(new InverseSchedule(torneo));
        }
    }

    /**
     * Torneo con equipos
     */
    private static void demo6() throws ValidationException  {
        List<Player> players = TournamentUtils.buildGenericPlayers(16, "Pl");
        List<Localization> courts = TournamentUtils.buildGenericLocalizations(3, "Court");
        List<Timeslot> timeslots = TournamentUtils.buildSimpleTimeslots(13);

        Event event = new Event("Event", players, courts, timeslots, 3, 2, 4);

        for (int i = 0; i < players.size(); i += 2)
            event.addTeam(players.get(i), players.get(i + 1));

        Tournament torneo = new Tournament("Torneo", event);

        if (torneo.solve()) {
            torneo.printCurrentSchedules();
            System.out.println(new InverseSchedule(torneo));
        }
    }

    /**
     * Descansos
     */
    private static void demo7() throws ValidationException  {
        List<Player> players = TournamentUtils.buildGenericPlayers(8, "Jug");
        List<Localization> courts = TournamentUtils.buildGenericLocalizations(3, "Pista");
        List<Timeslot> timeslots = TournamentUtils.buildSimpleTimeslots(12);

        Event division = new Event("División", players, courts, timeslots);
        division.addBreak(timeslots.get(3));
        division.addBreak(timeslots.get(8));

        Tournament torneo = new Tournament("Torneo", division);
        torneo.getSolver().setSearchStrategy(TournamentSolver.SearchStrategy.MINDOM_UB);

        if (torneo.solve()) {
            torneo.printCurrentSchedules();
            System.out.println(new InverseSchedule(torneo));
        }
    }

    /**
     * Restricción: "indisponibilidad"
     */
    private static void demo8() throws ValidationException  {
        List<Player> players = TournamentUtils.buildGenericPlayers(8, "Jug");
        List<Localization> courts = TournamentUtils.buildGenericLocalizations(3, "Pista");
        List<Timeslot> timeslots = TournamentUtils.buildSimpleTimeslots(6);

        Event division = new Event("División", players, courts, timeslots);

        // "Pista 3" no disponible de t0 a t3
        division.addUnavailableLocalizationAtTimeslotRange(courts.get(2), timeslots.get(0), timeslots.get(3));
        // "Jug 6" no disponible de t3 a t5
        division.addUnavailablePlayerAtTimeslotRange(players.get(5), timeslots.get(3), timeslots.get(5));

        Tournament torneo = new Tournament("Torneo", division);

        if (torneo.solve()) {
            torneo.printCurrentSchedules();
            System.out.println(new InverseSchedule(torneo));
        }
    }

    /**
     * Restricción: todos diferentes
     */
    private static void demo9() throws ValidationException  {
        List<Player> players = TournamentUtils.buildGenericPlayers(8, "Jug");
        List<Localization> courts = TournamentUtils.buildGenericLocalizations(6, "Pista");
        List<Timeslot> timeslots = TournamentUtils.buildSimpleTimeslots(14);

        Event division = new Event("División", players, courts, timeslots);
        division.setMatchesPerPlayer(7);
        division.setMatchupMode(TournamentSolver.MatchupMode.ALL_DIFFERENT);

        Tournament torneo = new Tournament("Torneo", division);
        torneo.getSolver().setSearchStrategy(TournamentSolver.SearchStrategy.MINDOM_UB);

        if (torneo.solve()) {
            torneo.printCurrentSchedules();
            System.out.println(new InverseSchedule(torneo));
        }
    }

    /**
     * Restricción: todos iguales
     */
    private static void demo10() throws ValidationException  {
        List<Player> players = TournamentUtils.buildGenericPlayers(8, "Jug");
        List<Localization> courts = TournamentUtils.buildGenericLocalizations(6, "Pista");
        List<Timeslot> timeslots = TournamentUtils.buildSimpleTimeslots(14);

        Event division = new Event("División", players, courts, timeslots);
        division.setMatchesPerPlayer(7);
        division.setMatchupMode(TournamentSolver.MatchupMode.ALL_EQUAL);

        Tournament torneo = new Tournament("Torneo", division);
        torneo.getSolver().setSearchStrategy(TournamentSolver.SearchStrategy.MINDOM_UB);

        if (torneo.solve()) {
            torneo.printCurrentSchedules();
            System.out.println(new InverseSchedule(torneo));
        }
    }

    /**
     * Restricción: enfrentamientos predefinidos
     */
    private static void demo11() throws ValidationException  {
        List<Player> players = TournamentUtils.buildGenericPlayers(8, "Jug");
        List<Localization> courts = TournamentUtils.buildGenericLocalizations(2, "Pista");
        List<Timeslot> timeslots = TournamentUtils.buildSimpleTimeslots(6);

        Event division = new Event("División", players, courts, timeslots);
        division.setMatchupMode(TournamentSolver.MatchupMode.CUSTOM);
        for (int i = 0; i < players.size() / 2; i++)
            division.addMatchup(players.get(i), players.get(players.size() - i - 1));

        // (también se pueden añadir equipos, si el evento tiene)

        Tournament torneo = new Tournament("Torneo", division);
        torneo.getSolver().setSearchStrategy(TournamentSolver.SearchStrategy.MINDOM_UB);

        if (torneo.solve()) {
            torneo.printCurrentSchedules();
            System.out.println(new InverseSchedule(torneo));
        }
    }

    /**
     * Restricción: enfrentamientos predefinidos especificando pista y hora
     */
    private static void demo12() throws ValidationException  {
        List<Player> players = TournamentUtils.buildGenericPlayers(8, "Jug");
        List<Localization> courts = TournamentUtils.buildGenericLocalizations(2, "Pista");
        List<Timeslot> timeslots = TournamentUtils.buildSimpleTimeslots(6);

        Event division = new Event("División", players, courts, timeslots);
        division.setMatchupMode(TournamentSolver.MatchupMode.CUSTOM);

        // "Jug 3" vs "Jug 6" en "Pista 1" y empezando en t2 o t3
        division.addMatchup(new Matchup(new HashSet<>(new HashSet<>(Arrays.asList(players.get(2), players.get(5)))),
                new HashSet<>(courts.subList(0, 0)),
                new HashSet<>(timeslots.subList(2, 3))));

        Tournament torneo = new Tournament("Torneo", division);
        torneo.getSolver().setSearchStrategy(TournamentSolver.SearchStrategy.MINDOM_UB);

        if (torneo.solve()) {
            torneo.printCurrentSchedules();
            System.out.println(new InverseSchedule(torneo));
        }
    }

    /**
     * Restricción: enfrentamientos predefinidos especificando ocurrencias
     */
    private static void demo13() throws ValidationException  {
        List<Player> players = TournamentUtils.buildGenericPlayers(8, "Jug");
        List<Localization> courts = TournamentUtils.buildGenericLocalizations(4, "Pista");
        List<Timeslot> timeslots = TournamentUtils.buildSimpleTimeslots(8);

        Event division = new Event("División", players, courts, timeslots);
        division.setMatchesPerPlayer(4);

        // "Jug 1" se enfrenta a "Jug 7" y a "Jug 8" 2 veces con cada uno
        division.addMatchup(new Matchup(new HashSet<>(Arrays.asList(players.get(0), players.get(7))),
                new HashSet<>(), new HashSet<>(), 2));
        division.addMatchup(new Matchup(new HashSet<>(Arrays.asList(players.get(0), players.get(6))),
                new HashSet<>(), new HashSet<>(), 2));
        division.setMatchupMode(TournamentSolver.MatchupMode.CUSTOM);

        Tournament torneo = new Tournament("Torneo", division);
        // en este ejemplo DOMOVERWDEG se comporta mucho mejor que LB
        torneo.getSolver().setSearchStrategy(TournamentSolver.SearchStrategy.DOMOVERWDEG);

        if (torneo.solve()) {
            torneo.printCurrentSchedules();
            System.out.println(new InverseSchedule(torneo));
        }
    }

    /**
     * Restricción: enfrentamientos predefinidos ->
     * simular round-robin usando enfrentamientos predefinidos (mismo efecto que ALL_DIFFERENT)
     */
    private static void demo14() throws ValidationException  {
        List<Player> players = TournamentUtils.buildGenericPlayers(8, "Jug");
        List<Localization> courts = TournamentUtils.buildGenericLocalizations(4, "Pista");
        List<Timeslot> timeslots = TournamentUtils.buildSimpleTimeslots(14);

        Event division = new Event("División", players, courts, timeslots);
        division.setMatchesPerPlayer(7);
        division.setMatchupMode(TournamentSolver.MatchupMode.CUSTOM);

        // simular round-robin usando enfrentamientos predefinidos (mismo efecto que ALL_DIFFERENT)

        int n = players.size();
        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                Player p1 = players.get(i);
                Player p2 = players.get(j);

                division.addMatchup(new Matchup(new HashSet<>(Arrays.asList(p1, p2)), new HashSet<>(),
                        new HashSet<>()));
            }
        }

        Tournament torneo = new Tournament("Torneo", division);
        torneo.getSolver().setSearchStrategy(TournamentSolver.SearchStrategy.MINDOM_UB);

        if (torneo.solve()) {
            torneo.printCurrentSchedules();
            System.out.println(new InverseSchedule(torneo));
        }
    }

    /**
     * Validación -> mensajes de error; y otros mensajes
     */
    private static void demo15()  {
        List<Player> players = TournamentUtils.buildGenericPlayers(8, "Jug");
        List<Localization> courts = TournamentUtils.buildGenericLocalizations(3, "Pista");
        List<Timeslot> timeslots = TournamentUtils.buildSimpleTimeslots(1);

        Event division = new Event("División", players, courts, timeslots);
        Tournament torneo = new Tournament("Torneo", division);
        try {
            torneo.solve();
        } catch (ValidationException e) {
            System.out.println(torneo.getMessages());
        }

        try {
            division = new Event("División", players, courts, timeslots);
            division.setPlayersPerMatch(3);
            torneo = new Tournament("Torneo", division);
            torneo.solve();
        } catch (IllegalArgumentException | ValidationException e) {
            System.out.println(e);
        }
    }

    /**
     * Torneo con 2 divisiones: individual y dobles
     */
    private static void demo16() throws ValidationException  {
        List<Player> players = TournamentUtils.buildGenericPlayers(15, "Pl");
        List<Localization> courts = TournamentUtils.buildGenericLocalizations(5, "Court");
        List<Timeslot> timeslots = TournamentUtils.buildSimpleTimeslots(5);

        Event singles = new Event("Individual", players.subList(0, 8), courts.subList(0, 4), timeslots);
        singles.setTimeslotsPerMatch(1);
        singles.setMatchesPerPlayer(4);
        singles.setMatchupMode(TournamentSolver.MatchupMode.ALL_DIFFERENT);

        Event dobles = new Event("Dobles", players.subList(7, 15), courts.subList(4, 5), timeslots);
        dobles.setTimeslotsPerMatch(1);
        dobles.setPlayersPerMatch(4);

        for (int i = 0; i < dobles.getPlayers().size(); i += 2)
            dobles.addTeam(dobles.getPlayers().get(i), dobles.getPlayers().get(i + 1));

        Tournament torneo = new Tournament("Torneo", singles, dobles);
        torneo.getSolver().setSearchStrategy(TournamentSolver.SearchStrategy.MINDOM_UB);

        if (torneo.solve()) {
            torneo.printCurrentSchedules();
            System.out.println(new InverseSchedule(torneo));
        }
    }


    public static void main(String[] args) throws ValidationException {
        demo1();

        // extra:
        // - métodos de utilidad para manipular la configuración de un evento
        // - servicio web
    }
}
