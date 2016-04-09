package models.tournaments.events;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.tournaments.events.entities.Localization;
import models.tournaments.events.entities.Player;
import models.tournaments.events.entities.Team;
import models.tournaments.events.entities.Timeslot;
import solver.TournamentSolver.MatchupMode;

public class Event {
	/**
	 * Nombre del evento o la categoría
	 */
	private String name;
	
	/**
	 * Jugadores concretos o abstractos (equipos) que participan en el evento
	 */
	private Player[] players;
	 
	/**
	 * Localizaciones o terrenos de juego disponibles para la categoría
	 */
	private Localization[] localizations;
	
	/**
	 * Horas en las que tendrá lugar esta categoría (dominio temporal del evento o categoría)
	 */
	private Timeslot[] timeslots;
	
	/**
	 * Timeslots u horas en las que cada jugador no está disponible
	 */
	private Map<Player, Timeslot[]> unavailableTimeslots;
	
	/**
	 * Número de partidos que cada jugador ha de jugar en esta categoría
	 */
	private int nMatchesPerPlayer = 1;
	
	/**
	 * Duración de un partido en timeslots u horas
	 */
	private int nTimeslotsPerMatch = 2;
	
	/**
	 * Número de jugador que componen un partido
	 */
	private int nPlayersPerMatch = 2;
	
	/**
	 * Composición de los equipos
	 */
	private List<Team> teams;
	
	/**
	 * Si esta categoría define más de un partido por jugador, indica el modo de emparejamiento
	 */
	private MatchupMode matchupMode = MatchupMode.ANY;
	
	/**
	 * Indica si el evento se organiza por sorteo, es decir, los emparejamientos para cada partido se sortean previamente
	 */
	private boolean randomDrawings = false;
	
	/**
	 * Emparejamientos fijos predefinidos. Es obligatorio que los jugadores que forman cada lista compongan enfrentamiento/s
	 */
	private List<Set<Player>> fixedMatchups;
	
	/**
	 * Lista de timeslots del evento donde no pueden tener lugar partidos
	 */
	private List<Timeslot> breaks;
	
	/**
	 * Diccionario de localizaciones de juego descartadas en determinadas horas
	 */
	private Map<Localization, List<Timeslot>> discardedLocalizations;
	
	/**
	 * Diccionario de jugadores para cada cual los enfrentamientos de los que forme parte han de tener lugar
	 * en cualquiera de las localizaciones de la lista de localizaciones de juego vinculada a su entrada
	 */
	private Map<Player, List<Localization>> playersInLocalizations;
	
	/**
	 * Diccionario de jugadores para cada cual los enfrentamientos de los que forme parte han de tener lugar
	 * en cualquiera de las horas de la lista de timeslots vinculada a su entrada
	 */
	private Map<Player, List<Timeslot>> playersAtTimeslots;
	
	/**
	 * Construye un evento con la información más básica: nombre, jugadores, localizaciones y timeslots
	 * 
	 * @param name
	 * @param players
	 * @param localizations
	 * @param timeslots
	 */
	public Event(String name, Player[] players, Localization[] localizations, Timeslot[] timeslots) {
		this.name = name;
		this.players = players;
		this.localizations = localizations;
		this.timeslots = timeslots;
		unavailableTimeslots = new HashMap<Player, Timeslot[]>(players.length);
		
		for (Player player : players)
			unavailableTimeslots.put(player, new Timeslot[]{});
		
		fixedMatchups = new ArrayList<Set<Player>>();
		
		breaks = new ArrayList<Timeslot>();
		discardedLocalizations = new HashMap<Localization, List<Timeslot>>();
		playersInLocalizations = new HashMap<Player, List<Localization>>();
		playersAtTimeslots = new HashMap<Player, List<Timeslot>>();
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
	
	public void setTeams(List<Team> teams) {
		this.teams = teams;
	}
	
	public List<Team> getTeams() {
		return teams;
	}
	
	/**
	 * Asigna el modo de emparejamiento de este evento, sólo si el número de partidos por jugador es superior a uno
	 * 
	 * @param matchupMode
	 */
	public void setMatchupMode(MatchupMode matchupMode) {
		if (nMatchesPerPlayer > 1)
			this.matchupMode = matchupMode;
	}
	
	public MatchupMode getMatchupMode() {
		return matchupMode;
	}
	
	public void setRandomDrawings(boolean randomDrawings) {
		this.randomDrawings = randomDrawings;
	}
	
	public boolean getRandomDrawings() {
		return randomDrawings;
	}
	
	/**
	 * @return true si sobre la categoría se definen equipos explícitos, y false si no
	 */
	public boolean hasTeams() {
		return teams != null && !teams.isEmpty();
	}
	
	public void setFixedMatchups(List<Set<Player>> fixedMatchups) {
		this.fixedMatchups = fixedMatchups;
	}
	
	public List<Set<Player>> getFixedMatchups() {
		return fixedMatchups;
	}
	
	/**
	 * Añade un enfrentamiento fijo entre jugadores
	 * 
	 * @param matchup conjunto de jugadores entre los cuales habrá de darse un enfrentamiento
	 */
	public void addFixedMatchup(Set<Player> matchup) {
		fixedMatchups.add(matchup);
	}
	
	/**
	 * Añade un enfrentamiento fijo entre equipos
	 * 
	 * @param matchup conjunto de equipos entre los cuales habrá de darse un enfrentamiento
	 */
	public void addFixedTeamsMatchup(Set<Team> matchup) {
		Set<Player> playersInMatchup = new HashSet<Player>();
		for (Team team : matchup)
			playersInMatchup.addAll(team.getPlayers());
		
		fixedMatchups.add(playersInMatchup);
	}
	
	/**
	 * @return true si sobre el evento se han definido enfrentamientos fijos predefinidos, y false si no
	 */
	public boolean hasFixedMatchups() {
		return !fixedMatchups.isEmpty();
	}
	
	public void setBreaks(List<Timeslot> breaks) {
		this.breaks = breaks;
	}
	
	public List<Timeslot> getBreaks() {
		return breaks;
	}
	
	/**
	 * Añade un break si no existe
	 * 
	 * @param timeslotBreak la hora a la que no podrán tener lugar enfrentamientos en esta categoría
	 */
	public void addBreak(Timeslot timeslotBreak) {
		if (!breaks.contains(timeslotBreak))
			breaks.add(timeslotBreak);
	}
	
	/**
	 * Comprueba si un timeslot es un break
	 * 
	 * @param timeslot
	 * @return true si es break, false si no
	 */
	public boolean isBreak(Timeslot timeslot) {
		return breaks.contains(timeslot);
	}
	
	public void setDiscardedLocalizations(HashMap<Localization, List<Timeslot>> discardedLocalizations) {
		this.discardedLocalizations = discardedLocalizations;
	}
	
	public Map<Localization, List<Timeslot>> getDiscardedLocalizations() {
		return discardedLocalizations;
	}
	
	/**
	 * Añade una localización de juego inválida a una hora determinada
	 * 
	 * @param localization
	 * @param timeslot
	 */
	public void addDiscardedLocalization(Localization localization, Timeslot timeslot) {
		if (discardedLocalizations.containsKey(localization))
			discardedLocalizations.get(localization).add(timeslot);
		else
			discardedLocalizations.put(localization, new ArrayList<Timeslot>(Arrays.asList(new Timeslot[]{ timeslot })));
	}
	
	/**
	 * @return true si hay alguna localización inválida, y false si no
	 */
	public boolean hasDiscardedLocalizations() {
		return !discardedLocalizations.isEmpty();
	}
	
	public void setPlayersInLocalizations(Map<Player, List<Localization>> playersInLocalizations) {
		this.playersInLocalizations = playersInLocalizations;
	}
	
	public Map<Player, List<Localization>> getPlayersInLocalizations() {
		return playersInLocalizations;
	}
	
	/**
	 * Asigna al jugador las localizaciones de juego explícitas donde ha de jugar
	 * 
	 * @param player
	 * @param localizations
	 */
	public void addPlayerInLocalizations(Player player, List<Localization> localizations) {
		if (playersInLocalizations.containsKey(player))
			playersInLocalizations.get(player).addAll(localizations);
		else
			playersInLocalizations.put(player, localizations);
	}
	
	/**
	 * Asigna a los jugadores las localizaciones de juego explícitas donde han de jugar
	 * 
	 * @param players
	 * @param localizations
	 */
	public void addPlayersInLocalizations(List<Player> players, List<Localization> localizations) {
		for (Player player : players)
			addPlayerInLocalizations(player, localizations);
	}
	
	/**
	 * @return true si sobre el evento hay definidas asignaciones fijas de jugadores sobre localizaciones
	 */
	public boolean hasPlayersInLocalizations() {
		return !playersInLocalizations.isEmpty();
	}
	
	public void setPlayersAtTimeslots(Map<Player, List<Timeslot>> playersAtTimeslots) {
		this.playersAtTimeslots = playersAtTimeslots;
	}
	
	public Map<Player, List<Timeslot>> getPlayersAtTimeslots() {
		return playersAtTimeslots;
	}
	
	/**
	 * Asigna al jugador los timeslots explícitos donde ha de jugar
	 * 
	 * @param player  el jugador
	 * @param players los timeslots
	 */
	public void addPlayerAtTimeslots(Player player, List<Timeslot> timeslots) {
		if (playersAtTimeslots.containsKey(player))
			playersAtTimeslots.get(players).addAll(timeslots);
		else
			playersAtTimeslots.put(player, timeslots);
	}
	
	/**
	 * Asigna a los jugadores los timeslots explícitos donde han de jugar
	 * 
	 * @param players
	 * @param timeslots
	 */
	public void addPlayersAtTimeslots(List<Player> players, List<Timeslot> timeslots) {
		for (Player player : players)
			addPlayerAtTimeslots(player, timeslots);
	}
	
	/**
	 * @return true si sobre el evento hay definidas asignaciones fijas de jugadores sobre timeslots
	 */
	public boolean hasPlayersAtTimeslots() {
		return !playersAtTimeslots.isEmpty();
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
	
	/**
	 * @return array de enteros de dos dimensiones que representa las horas no disponibles de cada jugador.
	 * La primera dimensión (filas) corresponde a los jugadores y la segunda (columnas) a los timeslots. Un valor
	 * de 1 indica que hay indisponibilidad de ese jugador a esa hora, y 0 que está disponible.
	 */
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
	
	/**
	 * Devuelve el jugador en la posición indicada
	 * 
	 * @param index una posición
	 * @return      un jugador
	 */
	public Player getPlayerAt(int index) {
		return players[index];
	}
	
	/**
	 * Devuelve la localización en la posición indicada
	 * 
	 * @param index una posición
	 * @return      una localización
	 */
	public Localization getLocalizationAt(int index) {
		return localizations[index];
	}
	
	/**
	 * Devuelve el timeslot en la posición indicada
	 * 
	 * @param index una posición
	 * @return      un timeslot
	 */
	public Timeslot getTimeslotAt(int index) {
		return timeslots[index];
	}
	
	/**
	 * Devuelve la posición del jugador indicado
	 * 
	 * @param player un jugador
	 * @return       la posición en el array de jugadores del jugador indicado
	 */
	public int indexOf(Player player) {
		for (int p = 0; p < players.length; p++)
			if (players[p].equals(player))
				return p;
		return -1;
	}
	
	/**
	 * Devuelve la posición de la localización indicada
	 * 
	 * @param localization una localización
	 * @return             la posición en el array de localizaciones de la localización indicada
	 */
	public int indexOf(Localization localization) {
		for (int c = 0; c < localizations.length; c++)
			if (localizations[c].equals(localization))
				return c;
		return -1;
	}
	
	/**
	 * Devuelve la posición del timeslot indicado
	 * 
	 * @param timeslot un timeslot
	 * @return         la posición en el array de timeslots del timeslot indicado
	 */
	public int indexOf(Timeslot timeslot) {
		for (int t = 0; t < timeslots.length; t++)
			if (timeslots[t].equals(timeslot)) 
				return t;
		return -1;
	}
	
	/**
	 * @return número de jugadores que participan en el evento
	 */
	public int getNumberOfPlayers() {
		return players.length;
	}
	
	/**
	 * @return número de localizaciones de juego en disposición del evento
	 */
	public int getNumberOfLocalizations() {
		return localizations.length;
	}
	
	/**
	 * @return número de timeslots en los que transcurre el evento
	 */
	public int getNumberOfTimeslots() {
		return timeslots.length;
	}
	
	/**
	 * Comprueba si el conjunto de jugadores del evento contiene el jugador
	 * 
	 * @param player un jugador
	 * @return       true si contiene el jugador, false si no
	 */
	public boolean containsPlayer(Player player) {
		for (Player p : players)
			if (p.equals(player))
				return true;
		return false;
	}
	
	/**
	 * Comprueba si el conjunto de localizaciones del evento contiene la localización
	 * 
	 * @param localization una localización
	 * @return             true si contiene la localización, false si no
	 */
	public boolean containsLocalization(Localization localization) {
		for (Localization l : localizations)
			if (l.equals(localization))
				return true;
		return false;
	}
	
	/**
	 * Comprueba si el conjunto de timeslots del evento contiene el timeslot
	 * 
	 * @param timeslot un timeslot
	 * @return         true si contiene el timeslot, false si no
	 */
	public boolean containsTimeslot(Timeslot timeslot) {
		for (Timeslot t : timeslots)
			if (t.equals(timeslot))
				return true;
		return false;
	}
	
	/**
	 * Comprueba si el jugador está disponible a una hora determinada
	 * 
	 * @param player   el jugador
	 * @param timeslot la hora
	 * @return         true si el jugador está disponible a esa hora, false si no
	 */
	public boolean isUnavailable(Player player, Timeslot timeslot) {
		Timeslot[] unavailablePlayerTimeslots = unavailableTimeslots.get(player);
		for (Timeslot t : unavailablePlayerTimeslots)
			if (t.equals(timeslot))
				return true;
		return false;
	}
	
	/**
	 * Comprueba si la localización está invalidada a una hora determinada
	 * 
	 * @param localization la localización
	 * @param timeslot     la hora
	 * @return             true si está descartada, false si no
	 */
	public boolean isDiscarded(Localization localization, Timeslot timeslot) {
		Set<Localization> localizations = discardedLocalizations.keySet();
		if (localizations.contains(localization)) {
			for (Localization l : localizations) {
				if (discardedLocalizations.get(l).contains(timeslot))
					return true;
			}
		}
		return false;
	}
	
	/**
	 * @return número de partidos del evento
	 */
	public int getNumberOfMatches() {
		return players.length / nPlayersPerMatch * nMatchesPerPlayer;
	}
	
	public String toString() {
		return name;
	}
}
