package models;

import java.util.HashMap;
import java.util.Map;

public class Event {
	// Jugadores o equipos que participan en el evento
	private Player[] players;
	
	// Localizaciones disponibles donde tendrán lugar los partidos (terreno de juego) 
	private Localization[] localizations;
	
	// Horas en las que tendrá lugar el evento
	private Timeslot[] timeslots;
	
	// Horas en las que cada jugador no está disponible
	private Map<Player, Timeslot[]> unavailableTimeslots;
	
	// Número de partidos que cada jugador ha de jugar (predet. 1)
	private int nMatchesPerPlayer = 1;
	
	// Número de timeslots que dura cada partido (preted. 2)
	private int nTimeslotsPerMatch = 2;
	
	public Event() {}
	
	public Event(Player[] players, Localization[] localizations, Timeslot[] timeslots) {
		this.players = players;
		this.localizations = localizations;
		this.timeslots = timeslots;
		unavailableTimeslots = new HashMap<Player, Timeslot[]>(players.length);
	}
	
	public Event(Player[] players, Localization[] localizations, Timeslot[] timeslots, Map<Player, Timeslot[]> unavailability, int nMatches, int matchDuration) {
		this.players = players;
		this.localizations = localizations;
		this.timeslots = timeslots;
		unavailableTimeslots = unavailability;
		nMatchesPerPlayer = nMatches;
		nTimeslotsPerMatch = matchDuration;
	}
	
	public void setPlayers(Player[] players) {
		this.players = players;
	}
	
	public Player[] getPlayers() {
		return players;
	}
	
	public void setLocalizations(Localization[] localizations) {
		this.localizations = localizations;
	}
	
	public Localization[] getLocalizations() {
		return localizations;
	}
	
	public void setTimeslots(Timeslot[] timeslots) {
		this.timeslots = timeslots;
	}
	
	public Timeslot[] getTimeslots() {
		return timeslots;
	}
	
	public void setUnavailableTimeslots(Map<Player, Timeslot[]> unavailability) {
		unavailableTimeslots = unavailability;
	}
	
	public Map<Player, Timeslot[]> getUnavailableTimeslots() {
		return unavailableTimeslots;
	}
	
	public void setMatchesPerPlayer(int n) {
		nMatchesPerPlayer = n;
	}
	
	public int getMatchesPerPlayer() {
		return nMatchesPerPlayer;
	}
	
	public void setMatchDuration(int matchDuration) {
		nTimeslotsPerMatch = matchDuration;
	}
	
	public int getMatchDuration() {
		return nTimeslotsPerMatch;
	}
	
	public int[] getLocalizationsAsIntArray() {
		int[] localizationsInt = new int[localizations.length];
		for (int i = 0; i < localizations.length; i++)
			localizationsInt[i] = localizations[i].getId();
		return localizationsInt;
	}
	
	public int[] getTimeslotsAsIntArray() {
		int[] timeslotsInt = new int[timeslots.length];
		for (int i = 0; i < timeslots.length; i++)
			timeslotsInt[i] = timeslots[i].getId();
		return timeslotsInt;
	}
	
	public int[][] getUnavailableTimeslotsAs2DIntArray() {
		int n = unavailableTimeslots.size();
		int[][] unavailableTimeslotsInt = new int[n][];
		
		for (Map.Entry<Player, Timeslot[]> entry : unavailableTimeslots.entrySet()) {
			Player player = entry.getKey();
			Timeslot[] unavailability = entry.getValue();
			
			int playerId = player.getId();
			
			unavailableTimeslotsInt[playerId] = new int[unavailability.length];
			int i = 0;
			for (Timeslot timeslot : unavailability)
				unavailableTimeslotsInt[playerId][i++] = timeslot.getId();
		}
		
		return unavailableTimeslotsInt;
	}
	
	
	public Player getPlayerById(int id) {
		for (Player player : players)
			if (player.getId() == id)
				return player;
		return null;
	}
	
	public Localization getLocalizationById(int id) {
		for (Localization localization : localizations)
			if (localization.getId() == id)
				return localization;
		return null;
	}
	
	public Timeslot getTimeslotById(int id) {
		for (Timeslot timeslot : timeslots)
			if (timeslot.getId() == id)
				return timeslot;
		return null;
	}
	
	public int getNumberOfPlayers() {
		return players.length;
	}
	
	public int getNumberOfLocalizations() {
		return localizations.length;
	}
	
	public int getNumberOfTimeslots() {
		return timeslots.length;
	}
	
	public boolean isUnavailable(int playerId, int timeslotId) {
		Player player = null;
		for (Player p : players)
			if (p.getId() == playerId)
				player = p;
		
		Timeslot[] unavailablePlayerTimeslots = unavailableTimeslots.get(player);
		for (Timeslot timeslot : unavailablePlayerTimeslots)
			if (timeslot.getId() == timeslotId)
				return true;
		
		return false;
	}
}
