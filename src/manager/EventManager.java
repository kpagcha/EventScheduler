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
			new int[]{ }
		);
		
		Event event1 = new Event("Category 1", new Player[]{ players[0], players[1], players[2], players[3] }, localizations, timeslots);
		event1.setMatchesPerPlayer(1);
		event1.setMatchDuration(2);
		
		// EVENT 2 --------------------------------------------
		
		Event event2 = new Event("Category 2", new Player[]{ players[1], players[4], players[3], players[5] }, localizations, timeslots);
		event2.setMatchesPerPlayer(1);
		event2.setMatchDuration(2);
		
		// EVENT 3 --------------------------------------------
		
		Event event3 = new Event(
			"Category 3",
			new Player[]{ players[6], players[7] },
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
		for (int i = 0; i < wtaPlayers.length;i++) allPlayers[i + atpPlayers.length] = wtaPlayers[i];
		
		Event mensDraw = new Event("Men's Draw", atpPlayers, localizations, timeslots);
		Event womensDraw = new Event("Women's Draw", wtaPlayers, localizations, timeslots);
		Event doublesDraw = new Event("Mixed Doubles Draw", allPlayers, localizations, timeslots);
		
		doublesDraw.setPlayersPerMatch(4);
		
		return new Tournament("Tennis Tournament", new Event[]{ mensDraw, womensDraw, doublesDraw });
	}
	
	private Player[] buildPlayers(String[] playersArray) {
		Player[] players = new Player[playersArray.length];
		for (int i = 0; i < playersArray.length; i++)
			players[i] = new Player(playersArray[i]);
		return players;
	}
	
	private Localization[] buildLocalizations(int[] courtsArray) {
		Localization[] localizations = new Localization[courtsArray.length];
		for (int i = 0; i < courtsArray.length; i++)
			localizations[i] = new Localization("Court " + (i + 1));
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
