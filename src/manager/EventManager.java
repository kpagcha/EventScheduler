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
	
	public Event getSampleEvent() {
		String[] playerNames = { "Novak", "Andy", "Roger", "Stan", "Rafael", "Kei", "Tomas", "David" };
		
		int[] courts = { 1, 2, 3 };
		
		int[] timeslotsInt = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 };
		
		int[][] unavailabilityInt = {
			{ 0, 1, 5 },			// Novak
			{ 8, 10, 11 },			// Andy
			{ 1, 2, 11 },			// Roger
			{ 0, 1 },				// Stan
			{ 2, 3, 4, 5, 6 },		// Rafael
			{ 3, 4, 9, 10, 11 },	// Kei
			{ 4, 5 },				// Tomas
			{ 2, 3 }				// David
		};
		
		
		Player[] players = new Player[playerNames.length];
		for (int i = 0; i < playerNames.length; i++)
			players[i] = new Player(i, playerNames[i]);
		
		Localization[] localizations = new Localization[courts.length];
		for (int i = 0; i < courts.length; i++)
			localizations[i] = new Localization(i, "Court " + (i + 1));
		
		Timeslot[] timeslots = new Timeslot[timeslotsInt.length];
		int oneHour = 60 * 60 * 1000;
		for (int i = 0; i < timeslotsInt.length; i++)
			timeslots[i] = new Timeslot(i, oneHour * i, oneHour * (i + 1), Timeslot.TimeUnit.HOURS);
		
		Event event = new Event(players, localizations, timeslots);
		
		Map<Player, Timeslot[]> unavailability = new HashMap<Player, Timeslot[]>(players.length);
		for (int p = 0; p < unavailabilityInt.length; p++) {
			Player player = players[p];
			Timeslot[] playerUnavailability = new Timeslot[unavailabilityInt[p].length];
			
			int t = 0;
			for (int timeslot : unavailabilityInt[p])
				playerUnavailability[t++] = event.getTimeslotById(timeslot);
			
			unavailability.put(player, playerUnavailability);
		}
		
		event.setUnavailableTimeslots(unavailability);
		event.setMatchesPerPlayer(1);
		event.setMatchDuration(4);
		
		return event;
	}
	
	public Event getSample32PlayersEvent() {
		String[] playerNames = {
				"Djokovic", "Murray", "Federer", "Wawrinka", "Nadal", "Nishikori", "Berdych", "Ferrer",
				"Tsonga", "Gasquet", "Isner", "Cilic", "Thiem", "Raonic", "Anderson", "Monfils",
				"Bautista Agut", "Goffin", "Simon", "Tomic", "Lopez", "Paire", "Troicki", "Sock",
				"Cuevas", "Dimitrov", "Kyrgios", "Klizan", "Dolgopolov", "Kohlschreiber", "Fognini", "Chardy"
		};
		
		int[] courts = { 1, 2, 3, 4, 5, 6, 7 };
		
		int[] timeslotsInt = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 };
		
		int[][] unavailabilityInt = {
			{ 3, 4, 5, 6, 7, 8, 9, 10, 11 },	// Djokovic		
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{}
		};
		
		
		Player[] players = new Player[playerNames.length];
		for (int i = 0; i < playerNames.length; i++)
			players[i] = new Player(i, playerNames[i]);
		
		Localization[] localizations = new Localization[courts.length];
		for (int i = 0; i < courts.length; i++)
			localizations[i] = new Localization(i, "Court " + (i + 1));
		
		Timeslot[] timeslots = new Timeslot[timeslotsInt.length];
		int oneHour = 60 * 60 * 1000;
		for (int i = 0; i < timeslotsInt.length; i++)
			timeslots[i] = new Timeslot(i, oneHour * i, oneHour * (i + 1), Timeslot.TimeUnit.HOURS);
		
		Event event = new Event(players, localizations, timeslots);
		
		Map<Player, Timeslot[]> unavailability = new HashMap<Player, Timeslot[]>(players.length);
		for (int p = 0; p < unavailabilityInt.length; p++) {
			Player player = players[p];
			Timeslot[] playerUnavailability = new Timeslot[unavailabilityInt[p].length];
			
			int t = 0;
			for (int timeslot : unavailabilityInt[p])
				playerUnavailability[t++] = event.getTimeslotById(timeslot);
			
			unavailability.put(player, playerUnavailability);
		}
		
		event.setUnavailableTimeslots(unavailability);
		event.setMatchesPerPlayer(1);
		event.setMatchDuration(3);
		
		return event;
	}
	
	public Event getSampleSmallEvent() {
		String[] playerNames = { "Novak", "Andy" };
		
		int[] courts = { 1, 2 };
		
		int[] timeslotsInt = { 0, 1, 2, 3 };
		
		int[][] unavailabilityInt = {
			{ },			    // Novak
			{ },			    // Andy
		};
		
		
		Player[] players = new Player[playerNames.length];
		for (int i = 0; i < playerNames.length; i++)
			players[i] = new Player(i, playerNames[i]);
		
		Localization[] localizations = new Localization[courts.length];
		for (int i = 0; i < courts.length; i++)
			localizations[i] = new Localization(i, "Court " + (i + 1));
		
		Timeslot[] timeslots = new Timeslot[timeslotsInt.length];
		int oneHour = 60 * 60 * 1000;
		for (int i = 0; i < timeslotsInt.length; i++)
			timeslots[i] = new Timeslot(i, oneHour * i, oneHour * (i + 1), Timeslot.TimeUnit.HOURS);
		
		Event event = new Event(players, localizations, timeslots);
		
		Map<Player, Timeslot[]> unavailability = new HashMap<Player, Timeslot[]>(players.length);
		for (int p = 0; p < unavailabilityInt.length; p++) {
			Player player = players[p];
			Timeslot[] playerUnavailability = new Timeslot[unavailabilityInt[p].length];
			
			int t = 0;
			for (int timeslot : unavailabilityInt[p])
				playerUnavailability[t++] = event.getTimeslotById(timeslot);
			
			unavailability.put(player, playerUnavailability);
		}
		
		event.setUnavailableTimeslots(unavailability);
		event.setMatchesPerPlayer(1);
		event.setMatchDuration(2);
		
		return event;
	}
}
