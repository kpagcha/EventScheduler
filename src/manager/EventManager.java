package manager;

import java.util.HashMap;
import java.util.Map;

import models.Event;
import models.Localization;
import models.Player;
import models.Timeslot;
import models.Tournament;

public class EventManager {
	private static EventManager instance = null;
	
	private EventManager() { }
	
	public static EventManager getInstance() {
		if (instance == null)
			instance = new EventManager();
		return instance;
	}
	
	public Tournament getSampleSmallTournament() {
		// EVENT 1 --------------------------------------------
		
		Player[] players = buildPlayers(new String[]{ "Djokovic", "Federer", "Nadal", "Murray", "Wawrinka", "Ferrer", "Nishikori", "Berdych" });
		Localization[] localizations = buildLocalizations(new int[]{ 1, 2 });
		Timeslot[] timeslots = buildTimeslots(
			new int[]{ 0, 1, 2, 3, 4, 5, 6, 7, 8 },
			new int[]{ 4 }
		);
		
		Event event1 = new Event("Category 1", new Player[]{ players[0], players[1] }, localizations, timeslots);
		event1.setMatchesPerPlayer(1);
		event1.setMatchDuration(2);
		
		// EVENT 2 --------------------------------------------
		
		Event event2 = new Event("Category 2", new Player[]{ players[2], players[3] }, localizations, timeslots);
		event2.setMatchesPerPlayer(1);
		event2.setMatchDuration(2);
		
		// EVENT 3 --------------------------------------------
		
		Event event3 = new Event(
			"Category 3",
			new Player[]{ players[3], players[0],  players[1], players[2] },
			localizations,
			new Timeslot[]{ timeslots[2], timeslots[3], timeslots[4], timeslots[5] }
		);
		event3.setMatchesPerPlayer(1);
		event3.setMatchDuration(1);
		
		return new Tournament("Small Tournament", new Event[]{ event1, event2, event3 });
	}
	
	public Tournament getSampleTournamentWithOneCategory() {
		Player[] players = buildPlayers(new String[]{ "Djokovic", "Murray", "Federer", "Wawrinka", "Nadal", "Nishikori", "Berdych", "Ferrer" });
		Localization[] localizations = buildLocalizations(new int[]{ 1, 2 });
		Timeslot[] timeslots = buildTimeslots(
			new int[]{ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 },
			new int[]{ 3 }
		);
		
		Event event = new Event("Main Category", players, localizations, timeslots);
		
		Map<Player, Timeslot[]> unavailability = buildUnavailability(
			event,
			new int[][]
			{
				{ 4, 5 }, // Djokovic
				{ 4 },    // Murray
				{ 7, 8 }, // Federer
				{ 0, 6 }, // Wawrinka
				{ 1, 2 }, // Nadal
				{ 3 },    // Nishikori
				{ },      // Berdych
				{ 8, 9 }  // Ferrer
			}
		);
		
		event.setUnavailableTimeslots(unavailability);
		event.setMatchesPerPlayer(1);
		event.setMatchDuration(3);
		
		return new Tournament("Exhibition Tournament", new Event[]{ event });
	}
	
	public Tournament getSampleTennisTournament() {
		Player[] atpPlayers = buildPlayers(new String[]{ "Djokovic", "Murray", "Federer", "Wawrinka", "Nadal", "Nishikori", "Berdych", "Ferrer" });
		Player[] wtaPlayers = buildPlayers(new String[]{ "Williams", "Radwanska", "Kerber", "Muguruza", "Halep", "Suárez Navarro", "Kvitova", "Azarenka" });
		Localization[] localizations = buildLocalizations(new int[]{ 1, 2, 3 });
		Timeslot[] timeslots = buildTimeslots(
			new int[]{ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 },
			new int[]{ }
		);
		
		Player[] allPlayers = new Player[atpPlayers.length + wtaPlayers.length];
		for (int i = 0; i < atpPlayers.length; i++) allPlayers[i] = atpPlayers[i];
		for (int i = 0; i < wtaPlayers.length; i++) allPlayers[i + atpPlayers.length] = wtaPlayers[i];
		
		Event mensDraw = new Event("Men's Draw", atpPlayers, localizations, timeslots);
		Event womensDraw = new Event("Women's Draw", wtaPlayers, localizations, timeslots);
		Event doublesDraw = new Event("Mixed Doubles Draw", allPlayers, localizations, timeslots);
		
		doublesDraw.setPlayersPerMatch(4);
		
		return new Tournament("Tennis Tournament", new Event[]{ mensDraw, womensDraw, doublesDraw });
	}

	public Tournament getSampleMediumTennisTournament() {
		Player[] kids = buildGenericPlayers(8, "Kid");
		Player[] men = buildGenericPlayers(16, "Man");
		Player[] women = buildGenericPlayers(12, "Woman");
		Localization[] localizations = buildGenericLocalizations(7, "Pista");
		Timeslot[] timeslots = buildTimeslots(12, new int[]{ 5 });
		
		Event eventKids = new Event(
			"Kids' Category",
			kids, new Localization[]{ localizations[0], localizations[1], localizations[2], localizations[3] }, 
			timeslots
		);
		
		Event eventMen = new Event("Men's Category", men, localizations, timeslots);
		
		Event eventWomen = new Event("Women's Category", women, localizations, timeslots);
		
		Player[] doubles = new Player[men.length + women.length];
		for (int i = 0; i < men.length; i++) doubles[i] = men[i];
		for (int i = 0; i < women.length; i++) doubles[i + men.length] = women[i];
		
		Timeslot[] doublesTimeslots = new Timeslot[timeslots.length + 7];
		for (int i = 0; i < timeslots.length; i++) doublesTimeslots[i] = timeslots[i];
		for (int i = timeslots.length; i < doublesTimeslots.length; i++) doublesTimeslots[i] = new Timeslot(3600000 * i, 3600000 * (i + 1));
		doublesTimeslots[timeslots.length].setIsBreak(true);
		
		Event eventDoubles = new Event(
			"Double's Event", doubles, localizations, doublesTimeslots
		);
		
		eventDoubles.setPlayersPerMatch(4);
		
		Map<Player, Timeslot[]> kidsUnavailability = buildUnavailability(
			eventKids,
			new int[][]{
				{ 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 },
				{ 2, 3 },
				{ 0, 1 },
				{ 9, 10, 11 },
				{},
				{},
				{ 5, 6 },
				{ 0 } 
			}
		);
		
		eventKids.setUnavailableTimeslots(kidsUnavailability);
		
		eventKids.setRandomDrawings(true);
		eventMen.setRandomDrawings(true);
		eventWomen.setRandomDrawings(true);
		eventDoubles.setRandomDrawings(true);
		
		return new Tournament("Medium Tennis Tournament", new Event[]{ eventKids, eventMen, eventWomen, eventDoubles });
	}
	
	public Tournament getSampleBigTennisTournament() {
		Player[] benjamin = buildGenericPlayers(8, "Benj");
		Player[] alevin = buildGenericPlayers(8, "Alev");
		Player[] infantil = buildGenericPlayers(32, "Inf");
		Player[] cadete = buildGenericPlayers(32, "Cad");
		Player[] junior = buildGenericPlayers(8, "Jun");
		Player[] absoluto = buildGenericPlayers(16, "Abs");
		
		Localization[] localizations = buildGenericLocalizations(8, "Pista");
		Timeslot[] timeslots = buildTimeslots(27, new int[]{ 5, 13, 19 }); // 2 días de 9:00 a 21:00 con descanso a las 14:00 (y la noche entre d1 y d2)
		
		Event categoriaBenjamin = new Event("Categoría Benjamín", benjamin, localizations, timeslots);
		Event categoriaAlevin = new Event("Categoría Alevín", alevin, localizations, timeslots);
		Event categoriaInfantil = new Event("Categoría Infantil", infantil, localizations, timeslots);
		Event categoriaCadete = new Event("Categoría Cadete", cadete, localizations, timeslots);
		Event categoriaJunior = new Event("Categoría Junior", junior, localizations, timeslots);
		Event categoriaAbsoluto = new Event("Categoría Absoluto", absoluto, localizations, timeslots);
		
		categoriaBenjamin.setMatchDuration(1);
		categoriaAlevin.setMatchDuration(1);
		
		return new Tournament(
			"Torneo de tenis", 
			new Event[]{ categoriaBenjamin, categoriaAlevin, categoriaInfantil, categoriaCadete, categoriaJunior, categoriaAbsoluto }
		);
	}
	
	private Player[] buildPlayers(String[] playersArray) {
		Player[] players = new Player[playersArray.length];
		for (int i = 0; i < playersArray.length; i++)
			players[i] = new Player(playersArray[i]);
		return players;
	}
	
	private Player[] buildGenericPlayers(int n, String placeholder) {
		if (placeholder.isEmpty())
			placeholder = "Player";
		
		Player[] players = new Player[n];
		for (int i = 0; i < n; i++)
			players[i] = new Player(placeholder + " " + (i + 1));
		return players;
	}
	
	private Localization[] buildLocalizations(int[] courtsArray) {
		Localization[] localizations = new Localization[courtsArray.length];
		for (int i = 0; i < courtsArray.length; i++)
			localizations[i] = new Localization("Court " + (i + 1));
		return localizations;
	}
	
	private Localization[] buildGenericLocalizations(int n, String placeholder) {
		if (placeholder.isEmpty())
			placeholder = "Court";
		
		Localization[] localizations = new Localization[n];
		for (int i = 0; i < n ; i++)
			localizations[i] = new Localization(placeholder + " " + (i + 1));
		return localizations;
	}
	
	private Timeslot[] buildTimeslots(int[] timeslotsArray, int[] breaks) {
		Timeslot[] timeslots = new Timeslot[timeslotsArray.length];
		int oneHour = 60 * 60 * 1000;
		for (int i = 0; i < timeslotsArray.length; i++) {
			timeslots[i] = new Timeslot(oneHour * i, oneHour * (i + 1));
			for (int j = 0; j < breaks.length; j++)
				if (i == breaks[j]) {
					timeslots[i].setIsBreak(true);
					break;
				}
		}				
		return timeslots;
	}
	
	private Timeslot[] buildTimeslots(int nTimeslots, int[] breaks) {
		int[] timeslots = new int[nTimeslots];
		for (int i = 0; i < nTimeslots; i++)
			timeslots[i] = i;
		return buildTimeslots(timeslots, breaks);
	}
	
	private Map<Player, Timeslot[]> buildUnavailability(Event event, int[][] unavailabilityArray) {
		Player[] players = event.getPlayers();
		Map<Player, Timeslot[]> unavailability = new HashMap<Player, Timeslot[]>(players.length);
		for (int p = 0; p < unavailabilityArray.length; p++) {
			Player player = players[p];
			Timeslot[] playerUnavailability = new Timeslot[unavailabilityArray[p].length];
			
			int t = 0;
			for (int timeslot : unavailabilityArray[p])
				playerUnavailability[t++] = event.getTimeslotAt(timeslot);
			
			unavailability.put(player, playerUnavailability);
		}
		return unavailability;
	}
}
