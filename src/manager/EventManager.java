package manager;

import java.util.HashMap;
import java.util.Map;

import models.Event;
import models.Localization;
import models.Player;
import models.Timeslot;

public class EventManager {
	private static EventManager instance = null;
	
	private EventManager() { }
	
	public static EventManager getInstance() {
		if (instance == null)
			instance = new EventManager();
		return instance;
	}
	
	public Tournament getSampleSmallTournament() {
		Player[] players = buildPlayers(new String[]{ "Novak", "Andy", "Rafael", "Stan", "David", "Tomas" });
		
		Player[] players1 = new Player[]{ players[0], players[1], players[2], players[3] };
		
		Localization[] localizations = buildLocalizations(new int[]{ 1 });
		
		Timeslot[] timeslots = buildTimeslots(
			new int[]{ 0, 1, 2, 3 },
			new int[]{ }
		);
		
		Event event1 = new Event("Category 1", players1, localizations, timeslots);
		
		Map<Player, Timeslot[]> unavailability1 = buildUnavailability(
			event1,
			new int[][]
			{
				{ },
				{ },
				{ },
				{ }
			}
		);
		
		event1.setUnavailableTimeslots(unavailability1);
		event1.setMatchesPerPlayer(1);
		event1.setMatchDuration(2);
		
		
		Player[] players2 = new Player[]{ players[0], players[1], players[2], players[3] };
		
		Event event2 = new Event("Category 2", players2, localizations, timeslots);
		
		Map<Player, Timeslot[]> unavailability2 = buildUnavailability(
			event2,
			new int[][]
			{
				{ },
				{ },
				{ },
				{ }
			}
		);
		
		event2.setUnavailableTimeslots(unavailability2);
		event2.setMatchesPerPlayer(1);
		event2.setMatchDuration(2);
		
		return new Tournament("Small Tournament", new Event[]{ event1, event2 });
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
