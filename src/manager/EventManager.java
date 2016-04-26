package manager;

import java.lang.reflect.Array;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;

import data.model.tournament.Tournament;
import data.model.tournament.event.Event;
import data.model.tournament.event.entity.Localization;
import data.model.tournament.event.entity.Player;
import data.model.tournament.event.entity.timeslot.UndefiniteTimeslot;
import solver.TournamentSolver.MatchupMode;
import data.model.tournament.event.entity.timeslot.AbstractTimeslot;
import data.model.tournament.event.entity.timeslot.DefiniteTimeslot;
import data.model.tournament.event.entity.timeslot.Timeslot;

@SuppressWarnings("unused")
public class EventManager {
	private static EventManager instance = null;
	
	private EventManager() { }
	
	public static EventManager getInstance() {
		if (instance == null)
			instance = new EventManager();
		return instance;
	}
	
	/*
	 * TORNEO 1
	 */
	public Tournament getSampleTournament() {
		List<Player> players = buildGenericPlayers(16, "Jug");
		List<Localization> courts = buildGenericLocalizations(3, "Court");
		List<Timeslot> timeslots = buildAbstractTimeslots(13);
		
		Event event = new Event("Event", players, courts, timeslots, 3, 2, 4);
		
		event.setMatchupMode(MatchupMode.ALL_DIFFERENT);
		
		for (int i = 0; i < players.size(); i += 2)
			event.addTeam(players.get(i), players.get(i + 1));
		
		Tournament tournament = new Tournament("Tournament", event);
		
		return tournament;
	}
	
	/* 
	 * TORNEO ZARLON 15 ABRIL
	 */
	public Tournament getZarlonTournament() {
		int nTimeslots = 12;
		int startHour = 17;
		int startMinute = 0;
		List<Timeslot> timeslots = new ArrayList<Timeslot>(nTimeslots);
		for (int t = 0; t < nTimeslots; t++) {
			timeslots.add(new DefiniteTimeslot(LocalTime.of(startHour, startMinute), Duration.ofMinutes(30), 1));
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
		
		List<Player> pVeterano = buildPlayers(new String[]{ 
			"FERNANDEZ, M.", "DE MIGUEL, FCO. J", "ROMERO, R.", "FUNKE, C.", "PEREZ, O.", 
			"ARRIETA, J.", "PARDAL, R.", "PIEDROLA, D.", "CANEDA, M.", "REAL, A.", "DEVOS, L.", 
			"MAESTRE, D.", "ROMERA, M.", "IGLESIAS, E.", "MORENO, J.A.", "RIVAS, D" }
		);
		
		List<Player> pInfantilM = buildPlayers(new String[]{ 
			"DE LA RIVA, P.", "GALLERO, C.", "COLLANTES F.", "ZARZUELA, J.", "ARGUDO, D.",
			"REAL, A.", "REY, A.", "PLATT, H." }
		);
		
		List<Player> pCadeteM = buildPlayers(new String[]{ "VAZQUEZ, A", "PARRADO, R.", "CANEDA, P.", "PERIGNAT, T.",
			"HERRERA, A.", "PORFIRIO, N.", "TROYA, P.", "GARRIDA, A.M.", "NIEVES, F." }
		);
		
		List<Player> pAlevinM = buildPlayers(new String[]{ "VAZQUEZ, I.", "PORTALES, J.A.", "RAMIREZ, S.", 
			"GALERA, A.", "CASTILLA, J.", "OLIVA, M.", /*"HERRERA, P.",*/ "RIZO, H.", "PARRADO, A.",
			"BOCANEGRA, J.", "DAVILA, A.", /*"REAL, P.",*/ "BOLOIX, J.", "MIGUEL, A.", "BARBERA, L.", "MORENO, H" });
		
		List<Player> pAbsoluto = buildPlayers(new String[]{ "CAÑO, M.", "FUNKE, C.", "CASTAING, C.M.", "DIAZ, A.",
			"DIAZ, L.A.", "GARCIA, C.", "ZAPATA", "QUEVEDO" }
		);
		
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
		
		Tournament zarlon = new Tournament(
			"Torneo Zarlon",
			new Event[]{ alevinM, /*alevinF, infantilM,*/ infantilF, /*cadeteM, cadeteF, absoluto,*/ veterano }
		);
		
		zarlon.getSolver().setFillTimeslotsFirst(false);
		
		// Duración de un partido: 3 timeslots
		for (Event event : zarlon.getEvents())
			event.setTimeslotsPerMatch(3);
		
		
		// Enfrentamientos alevín masculino
		alevinM.addFixedMatchup(findPlayerByName("vazquez", pAlevinM), findPlayerByName("parrado", pAlevinM));
		alevinM.addFixedMatchup(findPlayerByName("oliva", pAlevinM), findPlayerByName("castilla", pAlevinM));
		alevinM.addFixedMatchup(findPlayerByName("ramirez", pAlevinM), findPlayerByName("barbera", pAlevinM));
		//alevinM.addFixedMatchup(findPlayerByName("herrera", pAlevinM), findPlayerByName("real", pAlevinM));
		alevinM.addFixedMatchup(findPlayerByName("bocanegra", pAlevinM), findPlayerByName("davila", pAlevinM));
		alevinM.addFixedMatchup(findPlayerByName("boloix", pAlevinM), findPlayerByName("galera", pAlevinM));
		alevinM.addFixedMatchup(findPlayerByName("miguel", pAlevinM), findPlayerByName("moreno", pAlevinM));
		alevinM.addFixedMatchup(findPlayerByName("rizo", pAlevinM), findPlayerByName("portales", pAlevinM));
		
		// Enfrentamientos infantil femenino
		infantilF.addFixedMatchup(findPlayerByName("garcia", pInfantilF), findPlayerByName("villanueva", pInfantilF));
		
		// Enfrentamientos Veterano
		veterano.addFixedMatchup(findPlayerByName("fernandez", pVeterano), findPlayerByName("piedrola", pVeterano));
		veterano.addFixedMatchup(findPlayerByName("devos", pVeterano), findPlayerByName("caneda", pVeterano));
		veterano.addFixedMatchup(findPlayerByName("funke", pVeterano), findPlayerByName("rivas", pVeterano));
		veterano.addFixedMatchup(findPlayerByName("moreno", pVeterano), findPlayerByName("arrieta", pVeterano));
		veterano.addFixedMatchup(findPlayerByName("iglesias", pVeterano), findPlayerByName("maestre", pVeterano));
		veterano.addFixedMatchup(findPlayerByName("pardal", pVeterano), findPlayerByName("romero", pVeterano));
		veterano.addFixedMatchup(findPlayerByName("real", pVeterano), findPlayerByName("perez", pVeterano));
		veterano.addFixedMatchup(findPlayerByName("romera", pVeterano), findPlayerByName("de miguel", pVeterano));
		
		
		// Pista 1
		zarlon.addUnavailableLocalization(pistas.get(0), new ArrayList<Timeslot>(Arrays.asList(
			timeslots.get(0), timeslots.get(1), timeslots.get(2)))
		);
		
		// Pista 2
		zarlon.addUnavailableLocalization(pistas.get(1), new ArrayList<Timeslot>(Arrays.asList(
			timeslots.get(0), timeslots.get(1), timeslots.get(2), timeslots.get(3), timeslots.get(4), timeslots.get(5), timeslots.get(6)))
		);
		
		// Pista 5
		zarlon.addUnavailableLocalization(pistas.get(4), new ArrayList<Timeslot>(Arrays.asList(
			timeslots.get(6), timeslots.get(7), timeslots.get(8), timeslots.get(9), timeslots.get(10), timeslots.get(11)))
		);
		
		// Pista 6
		zarlon.addUnavailableLocalization(pistas.get(5), new ArrayList<Timeslot>(Arrays.asList(
			timeslots.get(0), timeslots.get(1)))
		);
		
		return zarlon;
	}
	
	/* * * * * * * * * * * *
	 * * * * * * * * * * * *
	 * MÉTODOS AUXILIARES  *
	 * * * * * * * * * * * *
	 * * * * * * * * * * * *
	 */
	
	private Player findPlayerByName(String name, List<Player> players) {
		Player player = null;
		for (Player p : players)
			if (StringUtils.containsIgnoreCase(p.getName(), name))
				return p;
		return player;
	}
	
	private List<Player> buildPlayers(String[] playersArray) {
		Player[] players = new Player[playersArray.length];
		for (int i = 0; i < playersArray.length; i++)
			players[i] = new Player(playersArray[i]);
		return new ArrayList<Player>(Arrays.asList(players));
	}
	
	private List<Player> buildGenericPlayers(int n, String placeholder) {
		if (placeholder.isEmpty())
			placeholder = "Player";
		Player[] players = new Player[n];
		for (int i = 0; i < n; i++)
			players[i] = new Player(placeholder + " " + (i + 1));
		return new ArrayList<Player>(Arrays.asList(players));
	}
	
	private List<Localization> buildLocalizations(int[] courtsArray) {
		Localization[] localizations = new Localization[courtsArray.length];
		for (int i = 0; i < courtsArray.length; i++)
			localizations[i] = new Localization("Court " + (i + 1));
		return new ArrayList<Localization>(Arrays.asList(localizations));
	}
	
	private List<Localization>  buildGenericLocalizations(int n, String placeholder) {
		if (placeholder.isEmpty())
			placeholder = "Court";
		
		Localization[] localizations = new Localization[n];
		for (int i = 0; i < n ; i++)
			localizations[i] = new Localization(placeholder + " " + (i + 1));
		return new ArrayList<Localization>(Arrays.asList(localizations));
	}
	
	private List<Timeslot> buildAbstractTimeslots(int nTimeslots) {
		Timeslot[] timeslots = new Timeslot[nTimeslots];
		for (int i = 0; i < nTimeslots; i++)
			timeslots[i] = new AbstractTimeslot(i);
		return new ArrayList<Timeslot>(Arrays.asList(timeslots));
	}
	
	private List<Timeslot>  buildDefiniteDayOfWeekTimeslots(int nTimeslots) {
		Timeslot[] timeslots = new Timeslot[nTimeslots];
		int order = 0;
		for (int i = 0; i < nTimeslots; i++) {
			timeslots[i] = new DefiniteTimeslot(DayOfWeek.of(i % 7 + 1), Duration.ofHours(1), order);
			if (i % 7 + 1 == 0) order++;
		}
		return new ArrayList<Timeslot>(Arrays.asList(timeslots));
	}
	
	private List<Timeslot>  buildDefiniteLocalTimeTimeslots(int nTimeslots) {
		Timeslot[] timeslots = new Timeslot[nTimeslots];
		int order = 0;
		for (int i = 0; i < nTimeslots; i++) {
			timeslots[i] = new DefiniteTimeslot(LocalTime.of(i % 23, 0), Duration.ofHours(1), order);
			if (i % 23 == 0) order++;
		}
		return new ArrayList<Timeslot>(Arrays.asList(timeslots));
	}
	
	private List<Timeslot>  buildUndefiniteTimeslots(int nTimeslots) {
		Timeslot[] timeslots = new Timeslot[nTimeslots];
		for (int i = 0; i < nTimeslots; i++)
			timeslots[i] = new UndefiniteTimeslot(Duration.ofHours(1), i);
		return new ArrayList<Timeslot>(Arrays.asList(timeslots));
	}
}
