package data.model.tournament.event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import data.model.tournament.event.entity.Localization;
import data.model.tournament.event.entity.Player;
import data.model.tournament.event.entity.Team;
import data.model.tournament.event.entity.timeslot.Timeslot;
import solver.TournamentSolver.MatchupMode;

/**
 * @author Pablo
 *
 */
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
	private Map<Player, List<Timeslot>> unavailablePlayers;
	
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
	 * Composici�n de los equipos
	 */
	private List<Team> teams;
	
	/**
	 * Si esta categor�a define m�s de un partido por jugador, indica el modo de emparejamiento
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
	 * Diccionario de localizaciones de juego no disponibles en determinadas horas
	 */
	private Map<Localization, List<Timeslot>> unavailableLocalizations;
	
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
		unavailablePlayers = new HashMap<Player, List<Timeslot>>(players.length);
		
		fixedMatchups = new ArrayList<Set<Player>>();
		
		breaks = new ArrayList<Timeslot>();
		unavailableLocalizations = new HashMap<Localization, List<Timeslot>>();
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
	
	public void setUnavailablePlayers(Map<Player, List<Timeslot>> unavailability) {
		unavailablePlayers = unavailability;
	}
	
	public Map<Player, List<Timeslot>> getUnavailablePlayers() {
		return unavailablePlayers;
	}
	
	/**
	 * A�ade una lista de timeslots a los timeslots en los que el jugador no est� disponible
	 * 
	 * @param player
	 * @param timeslots
	 */
	public void addPlayerUnavailableTimeslots(Player player, List<Timeslot> timeslots) {	
		if (!unavailablePlayers.containsKey(player)) {
			unavailablePlayers.put(player, timeslots);
		} else {
			List<Timeslot> playerUnavailableTimeslots = unavailablePlayers.get(player);
			for (Timeslot timeslot : timeslots) {
				if (!playerUnavailableTimeslots.contains(timeslot))
					playerUnavailableTimeslots.add(timeslot);
			}
		}
	}
	
	/**
	 * A�ade un timeslot a la lista de timeslots en los que el jugador no est� disponible
	 * 
	 * @param player
	 * @param timeslot
	 */
	public void addPlayerUnavailableTimeslot(Player player, Timeslot timeslot) {
		if (!unavailablePlayers.containsKey(player))
			unavailablePlayers.put(player, new ArrayList<Timeslot>(Arrays.asList(timeslot)));
		else if (!unavailablePlayers.get(player).contains(timeslot))
			unavailablePlayers.get(player).add(timeslot);
	}
	
	/**
	 * Si el jugador no est� disponible a la hora timeslot, se elimina de la lista y vuelve a estar disponible a esa hora
	 * 
	 * @param player
	 * @param timeslot
	 */
	public void removePlayerUnavailableTimeslot(Player player, Timeslot timeslot) {
		List<Timeslot> playerUnavailableTimeslots = unavailablePlayers.get(player);
		if (playerUnavailableTimeslots != null && playerUnavailableTimeslots.contains(timeslot)) {
			playerUnavailableTimeslots.remove(timeslot);
			
			if (playerUnavailableTimeslots.isEmpty())
				unavailablePlayers.remove(player);
		}
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
	 * @return true si sobre la categor�a se definen equipos expl�citos, y false si no
	 */
	public boolean hasTeams() {
		return teams != null && !teams.isEmpty();
	}
	
	/**
	 * Asigna el modo de emparejamiento de este evento, s�lo si el n�mero de partidos por jugador es superior a uno
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
		if (!fixedMatchups.contains(matchup))
			fixedMatchups.add(matchup);
	}
	
	/**
	 * A�ade un enfrentamiento fijo entre jugadoers
	 * 
	 * @param players array de jugadores entre los que habr� de darse un enfrentamiento
	 */
	public void addFixedMatchup(Player... players) {
		Set<Player> matchup = new HashSet<Player>(Arrays.asList(players));
		addFixedMatchup(matchup);
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
	
		if (!fixedMatchups.contains(playersInMatchup))
			fixedMatchups.add(playersInMatchup);
	}
	
	/**
	 * Elimina un enfrentamiento fijo entre jugadores, si existe
	 * 
	 * @param matchup
	 */
	public void removeFixedMatchup(Set<Player> matchup) {
		fixedMatchups.remove(matchup);
	}
	
	/**
	 * Elimina un enfrentamiento fijo entre equipos, si existe
	 * 
	 * @param matchup
	 */
	public void removeFixedTeamsMatchup(Set<Team> matchup) {
		Set<Player> playersInMatchup = new HashSet<Player>();
		for (Team team : matchup)
			playersInMatchup.addAll(team.getPlayers());
		
		fixedMatchups.remove(playersInMatchup);
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
	 * A�ade un break si no existe
	 * 
	 * @param timeslotBreak la hora a la que no podr�n tener lugar enfrentamientos en esta categor�a
	 */
	public void addBreak(Timeslot timeslotBreak) {
		if (!breaks.contains(timeslotBreak))
			breaks.add(timeslotBreak);
	}
	
	/**
	 * Elimina un break, si existe
	 * 
	 * @param timeslotBreak
	 */
	public void removeBreak(Timeslot timeslotBreak) {
		breaks.remove(timeslotBreak);
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
	
	/**
	 * Comprueba si este evento tiene breaks
	 * 
	 * @return true si tiene breaks o false si no
	 */
	public boolean hasBreaks() {
		return !breaks.isEmpty();
	}
	
	public void setUnavailableLocalizations(HashMap<Localization, List<Timeslot>> unavailableLocalizations) {
		this.unavailableLocalizations = unavailableLocalizations;
	}
	
	public Map<Localization, List<Timeslot>> getUnavailableLocalizations() {
		return unavailableLocalizations;
	}
	
	/**
	 * A�ade una localizaci�n de juego inv�lida a una hora determinada
	 * 
	 * @param localization
	 * @param timeslot
	 */
	public void addUnavailableLocalization(Localization localization, Timeslot timeslot) {
		if (!unavailableLocalizations.containsKey(localization))
			unavailableLocalizations.put(localization, new ArrayList<Timeslot>(Arrays.asList(timeslot)));
		else if (!unavailableLocalizations.get(localization).contains(timeslot))
			unavailableLocalizations.get(localization).add(timeslot);
	}
	
	/**
	 * A�ade una localizaci�n de juego no disponible a las horas indicadas
	 * 
	 * @param localization
	 * @param timeslot
	 */
	public void addUnavailableLocalization(Localization localization, List<Timeslot> timeslots) {
		if (!unavailableLocalizations.containsKey(localization))
			unavailableLocalizations.put(localization, timeslots);
		else {
			List<Timeslot> unavailableLocalizationTimeslots = unavailableLocalizations.get(localization);
			for (Timeslot timeslot : timeslots)
				if (!unavailableLocalizationTimeslots.contains(timeslot))
					unavailableLocalizationTimeslots.add(timeslot);
		}
	}
	
	/**
	 * Elimina la invalidez de una localizaci�n, si la hubiese
	 * 
	 * @param localization
	 */
	public void removeUnavailableLocalization(Localization localization) {
		unavailableLocalizations.remove(localization);
	}
	
	/**
	 * Elimina la invalidez de una localizaci�n a una hora, si la hubiese
	 * 
	 * @param localization
	 * @param timeslot
	 */
	public void removeUnavailableLocalizationTimeslot(Localization localization, Timeslot timeslot) {
		List<Timeslot> discardedTimeslots = unavailableLocalizations.get(localization);
		if (discardedTimeslots != null && discardedTimeslots.contains(timeslot)) {
			discardedTimeslots.remove(timeslot);
		
			if (discardedTimeslots.isEmpty())
				unavailableLocalizations.remove(localization);
		}
	}
	
	/**
	 * @return true si hay alguna localizaci�n inv�lida, y false si no
	 */
	public boolean hasUnavailableLocalizations() {
		return !unavailableLocalizations.isEmpty();
	}
	
	public void setPlayersInLocalizations(Map<Player, List<Localization>> playersInLocalizations) {
		this.playersInLocalizations = playersInLocalizations;
	}
	
	public Map<Player, List<Localization>> getPlayersInLocalizations() {
		return playersInLocalizations;
	}
	
	/**
	 * Asigna al jugador una localizaci�n expl�cita donde ha de jugar
	 * 
	 * @param player
	 * @param localization
	 */
	public void addPlayerInLocalization(Player player, Localization localization) {
		if (!playersInLocalizations.containsKey(player))
			playersInLocalizations.put(player, new ArrayList<Localization>(Arrays.asList(localization)));
		else if (!playersInLocalizations.get(player).contains(localization))
			playersInLocalizations.get(player).add(localization);
	}
	
	/**
	 * Asigna al jugador las localizaciones de juego expl�citas donde ha de jugar
	 * 
	 * @param player
	 * @param localizations
	 */
	public void addPlayerInLocalizations(Player player, List<Localization> localizations) {
		for (Localization localization : localizations)
			addPlayerInLocalization(player, localization);
	}
	
	/**
	 * Asigna a los jugadores las localizaciones de juego expl�citas donde han de jugar
	 * 
	 * @param players
	 * @param localizations
	 */
	public void addPlayersInLocalizations(List<Player> players, List<Localization> localizations) {
		for (Player player : players)
			addPlayerInLocalizations(player, localizations);
	}
	
	/**
	 * Elimina de la configuraci�n que el jugador deba jugar en la localizaci�n
	 * 
	 * @param player
	 * @param localization
	 */
	public void removePlayerInLocalization(Player player, Localization localization) {
		if (playersInLocalizations.containsKey(player)) {
			List<Localization> playerLocalizations = playersInLocalizations.get(player);
			if (playerLocalizations.contains(localization)) {
				playerLocalizations.remove(localization);
				
				if (playerLocalizations.isEmpty())
					playersInLocalizations.remove(player);
			}
		}
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
	 * Asigna al jugador al timeslot expl�cito donde ha de jugar
	 * 
	 * @param player
	 * @param timeslot
	 */
	public void addPlayerAtTimeslot(Player player, Timeslot timeslot) {
		if (!playersAtTimeslots.containsKey(player))
			playersAtTimeslots.put(player, new ArrayList<Timeslot>(Arrays.asList(timeslot)));
		else if (!playersAtTimeslots.get(player).contains(timeslot))
			playersAtTimeslots.get(player).add(timeslot);
	}
	
	/**
	 * Asigna al jugador los timeslots expl�citos donde ha de jugar
	 * 
	 * @param player  el jugador
	 * @param players los timeslots
	 */
	public void addPlayerAtTimeslots(Player player, List<Timeslot> timeslots) {
		if (!playersAtTimeslots.containsKey(player)) {
			playersAtTimeslots.put(player, timeslots);
		} else {
			for (Timeslot timeslot : timeslots)
				addPlayerAtTimeslot(player, timeslot);
		}
	}
	
	/**
	 * Asigna a los jugadores los timeslots expl�citos donde han de jugar
	 * 
	 * @param players
	 * @param timeslots
	 */
	public void addPlayersAtTimeslots(List<Player> players, List<Timeslot> timeslots) {
		for (Player player : players)
			addPlayerAtTimeslots(player, timeslots);
	}
	
	/**
	 * Elimina de la configuraci�n que el jugador deba jugar a la hora indicada
	 * 
	 * @param player
	 * @param timeslot
	 */
	public void removePlayerAtTimeslot(Player player, Timeslot timeslot) {
		if (playersAtTimeslots.containsKey(player)) {
			List<Timeslot> playerTimeslots = playersAtTimeslots.get(player);
			if (playerTimeslots.contains(timeslot)) {
				playerTimeslots.remove(timeslot);
				
				if (playerTimeslots.isEmpty())
					playersAtTimeslots.remove(player);
			}
		}
	}
	
	/**
	 * Elimina de la configuraci�n que el jugador deba jugar a las horas indicadas
	 * 
	 * @param player
	 * @param timeslots
	 */
	public void removePlayerAtTimeslots(Player player, List<Timeslot> timeslots) {
		for (Timeslot timeslot : timeslots)
			removePlayerAtTimeslot(player, timeslot);
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
		return unavailablePlayers.containsKey(player) && unavailablePlayers.get(player).contains(timeslot);
	}
	
	/**
	 * Comprueba si la localizaci�n est� invalidada a una hora determinada
	 * 
	 * @param localization la localizaci�n
	 * @param timeslot     la hora
	 * @return             true si est� descartada, false si no
	 */
	public boolean isDiscarded(Localization localization, Timeslot timeslot) {
		Set<Localization> localizations = unavailableLocalizations.keySet();
		if (localizations.contains(localization)) {
			for (Localization l : localizations) {
				if (unavailableLocalizations.get(l).contains(timeslot))
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
