package models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Event {
	private String name;
	
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
	
	// Número de jugadores que componen un partido (lo normal es 2)
	private int nPlayersPerMatch = 2;
	
	// Indica si el evento se organiza por sorteo (sorteo de emparejamientos por partido) 
	private boolean randomDrawings = false;
	
	// Lista con la composicón de los equipos
	private List<Team> teams;
	
	// Lista con emparejamientos fijos predefinidos. Es obligatorio que entre los jugadores que
	// forman cada lista compongan enfrentamiento
	private List<Set<Player>> fixedMatchups;
	
	public Event(String name, Player[] players, Localization[] localizations, Timeslot[] timeslots) {
		this.name = name;
		this.players = players;
		this.localizations = localizations;
		this.timeslots = timeslots;
		unavailableTimeslots = new HashMap<Player, Timeslot[]>(players.length);
		
		for (Player player : players)
			unavailableTimeslots.put(player, new Timeslot[]{});
		
		fixedMatchups = new ArrayList<Set<Player>>();
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
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
	
	public void setMatchesPerPlayer(int nMatches) {
		nMatchesPerPlayer = nMatches;
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
	
	public void setPlayersPerMatch(int nPlayers) {
		nPlayersPerMatch = nPlayers;
	}
	
	public int getPlayersPerMatch() {
		return nPlayersPerMatch;
	}
	
	public void setRandomDrawings(boolean randomDrawings) {
		this.randomDrawings = randomDrawings;
	}
	
	public boolean getRandomDrawings() {
		return randomDrawings;
	}
	
	public void setTeams(List<Team> teams) {
		this.teams = teams;
	}
	
	public List<Team> getTeams() {
		return teams;
	}
	
	public void setFixedMatchups(List<Set<Player>> fixedMatchups) {
		this.fixedMatchups = fixedMatchups;
	}
	
	public List<Set<Player>> getFixedMatchups() {
		return fixedMatchups;
	}
	
	public void addFixedMatchup(Set<Player> matchup) {
		fixedMatchups.add(matchup);
	}
	
	public void addFixedTeamsMatchup(Set<Team> matchup) {
		Set<Player> playersInMatchup = new HashSet<Player>();
		for (Team team : matchup)
			playersInMatchup.addAll(team.getPlayers());
		
		fixedMatchups.add(playersInMatchup);
	}
	
	public boolean hasFixedMatchups() {
		return !fixedMatchups.isEmpty();
	}
	
	public boolean hasTeams() {
		return teams != null && !teams.isEmpty();
	}
	
	/**
	 * @param player
	 * @return equipo al que pertenece el jugador player
	 */
	public Team getTeamByPlayer(Player player) {
		for (Team team : teams) {
			Set<Player> playersInTeam = team.getPlayers();
			for (Player p : playersInTeam)
				if (p.equals(player))
					return team;
		}
		return null;
	}
	
	public int[][] getUnavailableTimeslotsAs2DIntArray() {
		int n = unavailableTimeslots.size();
		int[][] unavailableTimeslotsInt = new int[n][];
		
		for (Map.Entry<Player, Timeslot[]> entry : unavailableTimeslots.entrySet()) {
			int playerIndex = indexOf(entry.getKey());
			Timeslot[] unavailability = entry.getValue();
			
			unavailableTimeslotsInt[playerIndex] = new int[unavailability.length];
			int i = 0;
			for (Timeslot timeslot : unavailability)
				unavailableTimeslotsInt[playerIndex][i++] = indexOf(timeslot);
		}
		
		return unavailableTimeslotsInt;
	}
	
	public Player getPlayerAt(int index) {
		return players[index];
	}
	
	public Localization getLocalizationAt(int index) {
		return localizations[index];
	}
	
	public Timeslot getTimeslotAt(int index) {
		return timeslots[index];
	}
	
	public int indexOf(Player player) {
		for (int p = 0; p < players.length; p++)
			if (players[p].equals(player))
				return p;
		return -1;
	}
	
	public int indexOf(Timeslot timeslot) {
		for (int t = 0; t < timeslots.length; t++)
			if (timeslots[t].equals(timeslot)) 
				return t;
		return -1;
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
	
	public boolean containsPlayer(Player player) {
		for (Player p : players)
			if (p.equals(player))
				return true;
		return false;
	}
	
	public boolean isUnavailable(Player player, Timeslot timeslot) {
		Timeslot[] unavailablePlayerTimeslots = unavailableTimeslots.get(player);
		for (Timeslot t : unavailablePlayerTimeslots)
			if (t.equals(timeslot))
				return true;
		return false;
	}
	
	public int getNumberOfMatches() {
		return players.length / nPlayersPerMatch * nMatchesPerPlayer;
	}
	
	public String toString() {
		return name;
	}
}
