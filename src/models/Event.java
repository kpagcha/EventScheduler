package models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Event {
	/**
	 * Nombre del evento o la categor�a
	 */
	private String name;
	
	/**
	 * Jugadores concretos o abstractos (equipos) que participan en el evento
	 */
	private Player[] players;
	 
	/**
	 * Localizaciones o terrenos de juego disponibles para la categor�a
	 */
	private Localization[] localizations;
	
	/**
	 * Horas en las que tendr� lugar esta categor�a (dominio temporal del evento o categor�a)
	 */
	private Timeslot[] timeslots;
	
	/**
	 * Timeslots u horas en las que cada jugador no est� disponible
	 */
	private Map<Player, Timeslot[]> unavailableTimeslots;
	
	/**
	 * N�mero de partidos que cada jugador ha de jugar en esta categor�a
	 */
	private int nMatchesPerPlayer = 1;
	
	/**
	 * Duraci�n de un partido en timeslots u horas
	 */
	private int nTimeslotsPerMatch = 2;
	
	/**
	 * N�mero de jugador que componen un partido
	 */
	private int nPlayersPerMatch = 2;
	
	/**
	 * Indica si el evento se organiza por sorteo, es decir, los emparejamientos para cada partido se sortean previamente
	 */
	private boolean randomDrawings = false;
	
	/**
	 * Composici�n de los equipos
	 */
	private List<Team> teams;
	
	/**
	 * Emparejamientos fijos predefinidos. Es obligatorio que los jugadores que forman cada lista compongan enfrentamiento/s
	 */
	private List<Set<Player>> fixedMatchups;
	
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
	 * Construye un evento con la informaci�n m�s b�sica: nombre, jugadores, localizaciones y timeslots
	 * 
	 * @param name          nombre del evento
	 * @param players       jugadores
	 * @param localizations localizaciones de juego
	 * @param timeslots     horas o timeslots de juego
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
	
	/**
	 * @return true si sobre la categor�a se definen equipos expl�citos, y false si no
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
	 * A�ade un enfrentamiento fijo entre jugadores
	 * 
	 * @param matchup conjunto de jugadores entre los cuales habr� de darse un enfrentamiento
	 */
	public void addFixedMatchup(Set<Player> matchup) {
		fixedMatchups.add(matchup);
	}
	
	/**
	 * A�ade un enfrentamiento fijo entre equipos
	 * 
	 * @param matchup conjunto de equipos entre los cuales habr� de darse un enfrentamiento
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
	
	public void setDiscardedLocalizations(HashMap<Localization, List<Timeslot>> discardedLocalizations) {
		this.discardedLocalizations = discardedLocalizations;
	}
	
	public Map<Localization, List<Timeslot>> getDiscardedLocalizations() {
		return discardedLocalizations;
	}
	
	/**
	 * A�ade una localizaci�n de juego inv�lida a una hora determinada
	 * 
	 * @param localization la localizaci�n inv�lida
	 * @param timeslot     la hora a la que se invalida la localizaci�n
	 */
	public void addDiscardedLocalization(Localization localization, Timeslot timeslot) {
		if (discardedLocalizations.containsKey(localization))
			discardedLocalizations.get(localization).add(timeslot);
		else
			discardedLocalizations.put(localization, new ArrayList<Timeslot>(Arrays.asList(new Timeslot[]{ timeslot })));
	}
	
	/**
	 * @return true si hay alguna localizaci�n inv�lida, y false si no
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
	 * Asigna al jugador las localizaciones de juego expl�citas donde ha de jugar
	 * 
	 * @param player        el jugador
	 * @param localizations las localizaciones de juego
	 */
	public void addPlayerInLocalizations(Player player, List<Localization> localizations) {
		if (playersInLocalizations.containsKey(player))
			playersInLocalizations.get(player).addAll(localizations);
		else
			playersInLocalizations.put(player, localizations);
	}
	
	/**
	 * Asigna a los jugadores las localizaciones de juego expl�citas donde han de jugar
	 * 
	 * @param players       los jugadores
	 * @param localizations las localizaciones de juego
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
	 * La primera dimensi�n (filas) corresponde a los jugadores y la segunda (columnas) a los timeslots. Un valor
	 * de 1 indica que hay indisponibilidad de ese jugador a esa hora, y 0 que est� disponible.
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
	 * Devuelve el jugador en la posici�n indicada
	 * 
	 * @param index una posici�n
	 * @return      un jugador
	 */
	public Player getPlayerAt(int index) {
		return players[index];
	}
	
	/**
	 * Devuelve la localizaci�n en la posici�n indicada
	 * 
	 * @param index una posici�n
	 * @return      una localizaci�n
	 */
	public Localization getLocalizationAt(int index) {
		return localizations[index];
	}
	
	/**
	 * Devuelve el timeslot en la posici�n indicada
	 * 
	 * @param index una posici�n
	 * @return      un timeslot
	 */
	public Timeslot getTimeslotAt(int index) {
		return timeslots[index];
	}
	
	/**
	 * Devuelve la posici�n del jugador indicado
	 * 
	 * @param player un jugador
	 * @return       la posici�n en el array de jugadores del jugador indicado
	 */
	public int indexOf(Player player) {
		for (int p = 0; p < players.length; p++)
			if (players[p].equals(player))
				return p;
		return -1;
	}
	
	/**
	 * Devuelve la posici�n de la localizaci�n indicada
	 * 
	 * @param localization una localizaci�n
	 * @return             la posici�n en el array de localizaciones de la localizaci�n indicada
	 */
	public int indexOf(Localization localization) {
		for (int c = 0; c < localizations.length; c++)
			if (localizations[c].equals(localization))
				return c;
		return -1;
	}
	
	/**
	 * Devuelve la posici�n del timeslot indicado
	 * 
	 * @param timeslot un timeslot
	 * @return         la posici�n en el array de timeslots del timeslot indicado
	 */
	public int indexOf(Timeslot timeslot) {
		for (int t = 0; t < timeslots.length; t++)
			if (timeslots[t].equals(timeslot)) 
				return t;
		return -1;
	}
	
	/**
	 * @return n�mero de jugadores que participan en el evento
	 */
	public int getNumberOfPlayers() {
		return players.length;
	}
	
	/**
	 * @return n�mero de localizaciones de juego en disposici�n del evento
	 */
	public int getNumberOfLocalizations() {
		return localizations.length;
	}
	
	/**
	 * @return n�mero de timeslots en los que transcurre el evento
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
	 * Comprueba si el conjunto de localizaciones del evento contiene la localizaci�n
	 * 
	 * @param localization una localizaci�n
	 * @return             true si contiene la localizaci�n, false si no
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
	 * Comprueba si el jugador est� disponible a una hora determinada
	 * 
	 * @param player   el jugador
	 * @param timeslot la hora
	 * @return         true si el jugador est� disponible a esa hora, false si no
	 */
	public boolean isUnavailable(Player player, Timeslot timeslot) {
		Timeslot[] unavailablePlayerTimeslots = unavailableTimeslots.get(player);
		for (Timeslot t : unavailablePlayerTimeslots)
			if (t.equals(timeslot))
				return true;
		return false;
	}
	
	/**
	 * Comprueba si la localizaci�n est� invalidada a una hora determinada
	 * 
	 * @param localization la localizaci�n
	 * @param timeslot     la hora
	 * @return             true si est� descartada, false si no
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
	 * @return n�mero de partidos del evento
	 */
	public int getNumberOfMatches() {
		return players.length / nPlayersPerMatch * nMatchesPerPlayer;
	}
	
	public String toString() {
		return name;
	}
}
