package es.uca.garciachacon.eventscheduler.utils;

import es.uca.garciachacon.eventscheduler.data.model.tournament.*;
import es.uca.garciachacon.eventscheduler.solver.TournamentSolver.MatchupMode;
import org.apache.commons.lang3.StringUtils;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@SuppressWarnings("unused")
public class TournamentUtils {

    public static Tournament getTournamentWithTeamsAndAllDifferentMatchupMode() {
        List<Player> players = buildGenericPlayers(16, "Pl");
        List<Localization> courts = buildGenericLocalizations(3, "Court");
        List<Timeslot> timeslots = buildSimpleTimeslots(13);

        Event event = new Event("Event", players, courts, timeslots, 3, 2, 4);

        event.setMatchupMode(MatchupMode.ALL_DIFFERENT);

        for (int i = 0; i < players.size(); i += 2)
            event.addTeam(players.get(i), players.get(i + 1));

        return new Tournament("Tournament", event);
    }

    public static Tournament getSimpleTournament() {
        Event event = new Event("Generic Event",
                buildGenericPlayers(8, "Player"),
                buildGenericLocalizations(1, "Court"),
                buildLocalTimeTimeslots(8)
        );

        return new Tournament("Generic Tournament", event);
    }

    /*
     * TORNEO ZARLON 15 ABRIL
     */
    public static Tournament getZarlonTournament() {
        int nTimeslots = 12;
        int startHour = 17;
        int startMinute = 0;
        List<Timeslot> timeslots = new ArrayList<>(nTimeslots);
        for (int t = 0; t < nTimeslots; t++) {
            timeslots.add(new Timeslot(1, LocalTime.of(startHour, startMinute), Duration.ofMinutes(30)));
            if (t % 2 != 0) {
                startHour++;
                startMinute = 0;
            } else {
                startMinute = 30;
            }
        }

		/*
        t0  [1] 17:00 (PT30M)
		t1  [1] 17:30 (PT30M)
		t2  [1] 18:00 (PT30M)
		t3  [1] 18:30 (PT30M)
		t4  [1] 19:00 (PT30M)
		t5  [1] 19:30 (PT30M)
		t6  [1] 20:00 (PT30M)
		t7  [1] 20:30 (PT30M)
		t8  [1] 21:00 (PT30M)
		t9  [1] 21:30 (PT30M)
		t10 [1] 22:00 (PT30M)
		t11 [1] 22:30 (PT30M)
		*/

        List<Localization> pistas = buildGenericLocalizations(6, "Pista");

        List<Player> pVeterano =
                buildPlayers(new String[]{ "FERNANDEZ, M.", "DE MIGUEL, FCO. J", "ROMERO, R.", "FUNKE, C.", "PEREZ, O.",
                        "ARRIETA, J.", "PARDAL, R.", "PIEDROLA, D.", "CANEDA, M.", "REAL, A.", "DEVOS, L.",
                        "MAESTRE, D.", "ROMERA, M.", "IGLESIAS, E.", "MORENO, J.A.", "RIVAS, D"
                });

        List<Player> pInfantilM =
                buildPlayers(new String[]{ "DE LA RIVA, P.", "GALLERO, C.", "COLLANTES F.", "ZARZUELA, J.",
                        "ARGUDO, D.", "REAL, A.", "REY, A.", "PLATT, H."
                });

        List<Player> pCadeteM =
                buildPlayers(new String[]{ "VAZQUEZ, A", "PARRADO, R.", "CANEDA, P.", "PERIGNAT, T.", "HERRERA, A.",
                        "PORFIRIO, N.", "TROYA, P.", "GARRIDA, A.M.", "NIEVES, F."
                });

        List<Player> pAlevinM =
                buildPlayers(new String[]{ "VAZQUEZ, I.", "PORTALES, J.A.", "RAMIREZ, S.", "GALERA, A.", "CASTILLA, J.",
                        "OLIVA, M" + ".", /*"HERRERA, P.",*/
                        "RIZO, H.", "PARRADO, A.", "BOCANEGRA, J.", "DAVILA, A.", /*"REAL, P.",*/ "BOLOIX, J.",
                        "MIGUEL, A.", "BARBERA, L.", "MORENO, H"
                });

        List<Player> pAbsoluto =
                buildPlayers(new String[]{ "CAÑO, M.", "FUNKE, C.", "CASTAING, C.M.", "DIAZ, A.", "DIAZ, L.A.",
                        "GARCIA, C.", "ZAPATA", "QUEVEDO"
                });

        List<Player> pInfantilF = buildPlayers(new String[]{ "GARCIA, F.", "VILLANUEVA, L." });

        List<Player> pCadeteF = buildPlayers(new String[]{ "REICHERT, A.", "DIANEZ." });

        List<Player> pAlevinF = buildPlayers(new String[]{ "VILLANUEVA, L.", "TRIVIÑO, I." });

        Event veterano = new Event("Veterano", pVeterano, pistas, timeslots);
        //Event infantilM = new Event("Infantil Masculino", pInfantilM, pistas, timeslots);
        Event infantilF = new Event("Infantil Femenino", pInfantilF, pistas, timeslots);
        //Event cadeteM = new Event("Cadete Masculino", pCadeteM, pistas, timeslots);
        //Event cadeteF = new Event("Cadete Femenino", pCadeteF, pistas, timeslots);
        Event alevinM = new Event("Alevin Masculino", pAlevinM, pistas, timeslots);
        //Event alevinF = new Event("Alevin Femenino", pAlevinF, pistas, timeslots);
        //Event absoluto = new Event("Absoluto", pAbsoluto, pistas, timeslots);

        Tournament zarlon = new Tournament("Torneo Zarlon", alevinM, /*alevinF, infantilM,*/ infantilF, /*cadeteM,
        cadeteF, absoluto,*/
                veterano
        );

        zarlon.getSolver().prioritizeTimeslots(false);

        // Duración de un partido: 3 timeslots
        for (Event event : zarlon.getEvents())
            event.setTimeslotsPerMatch(3);


        // Enfrentamientos alevín masculino
        alevinM.addMatchup(findPlayerByName("vazquez", pAlevinM), findPlayerByName("parrado", pAlevinM));
        alevinM.addMatchup(findPlayerByName("oliva", pAlevinM), findPlayerByName("castilla", pAlevinM));
        alevinM.addMatchup(findPlayerByName("ramirez", pAlevinM), findPlayerByName("barbera", pAlevinM));
        //alevinM.addFixedMatchup(findPlayerByName("herrera", pAlevinM), findPlayerByName("real", pAlevinM));
        alevinM.addMatchup(findPlayerByName("bocanegra", pAlevinM), findPlayerByName("davila", pAlevinM));
        alevinM.addMatchup(findPlayerByName("boloix", pAlevinM), findPlayerByName("galera", pAlevinM));
        alevinM.addMatchup(findPlayerByName("miguel", pAlevinM), findPlayerByName("moreno", pAlevinM));
        alevinM.addMatchup(findPlayerByName("rizo", pAlevinM), findPlayerByName("portales", pAlevinM));

        // Enfrentamientos infantil femenino
        infantilF.addMatchup(findPlayerByName("garcia", pInfantilF), findPlayerByName("villanueva", pInfantilF));

        // Enfrentamientos Veterano
        veterano.addMatchup(findPlayerByName("fernandez", pVeterano), findPlayerByName("piedrola", pVeterano));
        veterano.addMatchup(findPlayerByName("devos", pVeterano), findPlayerByName("caneda", pVeterano));
        veterano.addMatchup(findPlayerByName("funke", pVeterano), findPlayerByName("rivas", pVeterano));
        veterano.addMatchup(findPlayerByName("moreno", pVeterano), findPlayerByName("arrieta", pVeterano));
        veterano.addMatchup(findPlayerByName("iglesias", pVeterano), findPlayerByName("maestre", pVeterano));
        veterano.addMatchup(findPlayerByName("pardal", pVeterano), findPlayerByName("romero", pVeterano));
        veterano.addMatchup(findPlayerByName("real", pVeterano), findPlayerByName("perez", pVeterano));
        veterano.addMatchup(findPlayerByName("romera", pVeterano), findPlayerByName("de miguel", pVeterano));


        // Pista 1
        zarlon.addUnavailableLocalizationAtTimeslots(pistas.get(0),
                new HashSet<>(Arrays.asList(timeslots.get(0), timeslots.get(1), timeslots.get(2)))
        );

        // Pista 2
        zarlon.addUnavailableLocalizationAtTimeslots(pistas.get(1), new HashSet<>(Arrays.asList(timeslots.get(0),
                timeslots.get(1),
                timeslots.get(2),
                timeslots.get(3),
                timeslots.get(4),
                timeslots.get(5),
                timeslots.get(6)
        )));

        // Pista 5
        zarlon.addUnavailableLocalizationAtTimeslots(pistas.get(4), new HashSet<>(Arrays.asList(timeslots.get(6),
                timeslots.get(7),
                timeslots.get(8),
                timeslots.get(9),
                timeslots.get(10),
                timeslots.get(11)
        )));

        // Pista 6
        zarlon.addUnavailableLocalizationAtTimeslots(pistas.get(5),
                new HashSet<>(Arrays.asList(timeslots.get(0), timeslots.get(1)))
        );

        return zarlon;
    }

    /**
     * LIGA
     */
    public static Tournament getLeague() {
        int n = 6;
        List<Player> players = buildGenericPlayers(n, "Team");
        List<Localization> fields = buildGenericLocalizations(n, "Stadium");
        List<Timeslot> timeslots = buildSimpleTimeslots(n);

        Event event = new Event("Sample League", players, fields, timeslots);
        event.setTimeslotsPerMatch(1);
        event.setMatchesPerPlayer(n - 1);
        event.setMatchupMode(MatchupMode.ALL_DIFFERENT);

        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                Player p1 = players.get(i);
                Player p2 = players.get(j);
                Localization field1 = fields.get(i);
                Localization field2 = fields.get(j);

                Matchup matchup = new Matchup(new HashSet<>(Arrays.asList(p1, p2)),
                        new HashSet<>(Arrays.asList(field1, field2)),
                        new HashSet<>()
                );
                event.addMatchup(matchup);
            }
        }

        return new Tournament("Tournament with League", event);
    }

    private static Player findPlayerByName(String name, List<Player> players) {
        for (Player player : players)
            if (StringUtils.containsIgnoreCase(player.getName(), name))
                return player;
        return null;
    }

    /**
     * Construye una lista de jugadores a partir de sus nombers.
     * <p>Si la lista de nombres es null o está vacía, se devuelve una lista de jugadores vacía</p>
     *
     * @param playersArray array de cadenas
     * @return lista de jugadores con los nombres indicados
     */
    public static List<Player> buildPlayers(String[] playersArray) {
        if (playersArray == null || playersArray.length <= 0)
            return new ArrayList<>();

        Player[] players = new Player[playersArray.length];
        for (int i = 0; i < playersArray.length; i++)
            players[i] = new Player(playersArray[i]);
        return new ArrayList<>(Arrays.asList(players));
    }

    /**
     * Construye una lista de jugadores genéricos.
     * <p>Si el número de jugadores es menor o igual que 0, se devuelve una lista vacía</p>
     * <p>Si placeholder es null o está vacío, se asigna la cadena "Player" como nombre del jugador</p>
     *
     * @param n           número de jugadores no negativo
     * @param placeholder nombre genérico de los jugadores
     * @return lista de jugadores genéricos
     */
    public static List<Player> buildGenericPlayers(int n, String placeholder) {
        if (n <= 0)
            return new ArrayList<>();

        if (placeholder == null || placeholder.isEmpty())
            placeholder = "Player";
        Player[] players = new Player[n];
        for (int i = 0; i < n; i++)
            players[i] = new Player(placeholder + " " + (i + 1));
        return new ArrayList<>(Arrays.asList(players));
    }

    /**
     * Construye una lista de localizaciones genéricas
     * <p>Si el número de localizaciones es menor o igual que 0, se devuelve una lista vacía</p>
     * <p>Si el placeholder es null o está vacío, se asigna la cadena "Court" como nombre de la localización</p>
     *
     * @param n           número de localizaciones
     * @param placeholder nombre genérico
     * @return lista de localizaciones genéricas
     */
    public static List<Localization> buildGenericLocalizations(int n, String placeholder) {
        if (n <= 0)
            return new ArrayList<>();

        if (placeholder == null || placeholder.isEmpty())
            placeholder = "Court";

        Localization[] localizations = new Localization[n];
        for (int i = 0; i < n; i++)
            localizations[i] = new Localization(placeholder + " " + (i + 1));
        return new ArrayList<>(Arrays.asList(localizations));
    }

    /**
     * Construye una lista de <i>timeslots</i> simples o abstractos (solamente con un orden cronológico).
     * <p>Si el número de horas de juego es menor o igual que 0, se devuelve una lista vacía</p>
     *
     * @param nTimeslots número de horas de juego
     * @return lista de <i>timeslots</i>
     */
    public static List<Timeslot> buildSimpleTimeslots(int nTimeslots) {
        if (nTimeslots <= 0)
            return new ArrayList<>();

        Timeslot[] timeslots = new Timeslot[nTimeslots];
        for (int i = 0; i < nTimeslots; i++)
            timeslots[i] = new Timeslot(i);
        return new ArrayList<>(Arrays.asList(timeslots));
    }

    /**
     * Construye una lista de <i>timeslots</i> definidos que representan días de la semana, con una duración de 1 hora.
     * <p>Si el número de horas de juego es menor o igual que 0, se devuelve una lista vacía</p>
     *
     * @param nTimeslots número de <i>timeslots</i>
     * @return lista de <i>timeslots</i>
     */
    public static List<Timeslot> buildDayOfWeekTimeslots(int nTimeslots) {
        if (nTimeslots <= 0)
            return new ArrayList<>();

        Timeslot[] timeslots = new Timeslot[nTimeslots];
        int order = 0;
        for (int i = 0; i < nTimeslots; i++) {
            if (i % 7 == 0 && i != 0)
                order++;
            timeslots[i] = new Timeslot(order, DayOfWeek.of(i % 7 + 1), Duration.ofHours(1));
        }
        return new ArrayList<>(Arrays.asList(timeslots));
    }

    /**
     * Construye una lista de <i>timeslots</i> definidos que representan una hora del día. La duración es de 1 hora.
     * <p>Si el número de horas de juego es menor o igual que 0, se devuelve una lista vacía</p>
     *
     * @param nTimeslots número de horas de juego
     * @return lista de <i>timeslots</i>
     */
    public static List<Timeslot> buildLocalTimeTimeslots(int nTimeslots) {
        if (nTimeslots <= 0)
            return new ArrayList<>();

        Timeslot[] timeslots = new Timeslot[nTimeslots];
        int order = 0;
        for (int i = 0; i < nTimeslots; i++) {
            timeslots[i] = new Timeslot(order, LocalTime.of(i % 24, 0), Duration.ofHours(1));
            if ((i + 1) % 24 == 0)
                order++;
        }
        return new ArrayList<>(Arrays.asList(timeslots));
    }

    /**
     * Construye una lista de horas de juego indefinidas de una duración de 1 hora.
     * <p>Si el número de horas de juego es menor o igual que 0, se devuelve una lista vacía</p>
     *
     * @param nTimeslots número de <i>timeslots</i>
     * @return lista de <i>timeslots</i>
     */
    public static List<Timeslot> buildOneHourTimeslots(int nTimeslots) {
        if (nTimeslots <= 0)
            return new ArrayList<>();

        Timeslot[] timeslots = new Timeslot[nTimeslots];
        for (int i = 0; i < nTimeslots; i++)
            timeslots[i] = new Timeslot(i, Duration.ofHours(1));
        return new ArrayList<>(Arrays.asList(timeslots));
    }
}
