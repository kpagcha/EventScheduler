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
	 * Nombre del evento o la categoría
	 */
	private String name;
	
	/**
	 * Jugadores concretos o abstractos (equipos) que participan en el evento
	 */
	private List<Player> players;
	 
	/**
	 * Localizaciones o terrenos de juego disponibles para la categoría
	 */
	private List<Localization> localizations;
	
	/**
	 * Horas en las que tendrá lugar esta categoría (dominio temporal del evento o categoría)
	 */
	private List<Timeslot> timeslots;
	
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
	 * Composición de los equipos, si hay
	 */
	private List<Team> teams;
	
	/**
	 * Lista de timeslots del evento donde no pueden tener lugar partidos
	 */
	private List<Timeslot> breaks;
	
	/**
	 * Timeslots u horas en las que cada jugador no está disponible
	 */
	private Map<Player, Set<Timeslot>> unavailablePlayers;
	
	/**
	 * Diccionario de localizaciones de juego no disponibles en determinadas horas
	 */
	private Map<Localization, Set<Timeslot>> unavailableLocalizations;
	
	/**
	 * Emparejamientos fijos predefinidos. Es obligatorio que los jugadores que forman cada lista compongan enfrentamiento/s
	 */
	private List<Set<Player>> fixedMatchups;
	
	/**
	 * Diccionario de jugadores para cada cual los enfrentamientos de los que forme parte han de tener lugar
	 * en cualquiera de las localizaciones del conjunto de localizaciones de juego asociado a su entrada
	 */
	private Map<Player, Set<Localization>> playersInLocalizations;
	
	/**
	 * Diccionario de jugadores para cada cual los enfrentamientos de los que forme parte han de tener lugar
	 * en cualquiera de las horas del conjunto de timeslots asociado a su entrada
	 */
	private Map<Player, Set<Timeslot>> playersAtTimeslots;
	
	/**
	 * Si esta categoría define más de un partido por jugador, indica el modo de emparejamiento
	 */
	private MatchupMode matchupMode = MatchupMode.ANY;
	
	/**
	 * Construye un evento con la información esencial: nombre, jugadores, localizaciones y timeslots
	 * 
	 * @param name          nombre del evento o de la categoría
	 * @param players       jugadores que participan
	 * @param localizations localizaciones de juego en las que tendrá lugar
	 * @param timeslots     horas o timeslots en los que discurrirá
	 */
	public Event(String name, List<Player> players, List<Localization> localizations, List<Timeslot> timeslots) {
		this.name = name;
		this.players = players;
		this.localizations = localizations;
		this.timeslots = timeslots;
		unavailablePlayers = new HashMap<Player, Set<Timeslot>>(players.size());
		
		fixedMatchups = new ArrayList<Set<Player>>();
		
		breaks = new ArrayList<Timeslot>();
		unavailableLocalizations = new HashMap<Localization, Set<Timeslot>>();
		playersInLocalizations = new HashMap<Player, Set<Localization>>();
		playersAtTimeslots = new HashMap<Player, Set<Timeslot>>();
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setPlayers(List<Player> players) {
		this.players = players;
	}
	
	public List<Player> getPlayers() {
		return players;
	}
	
	public void setLocalizations(List<Localization> localizations) {
		this.localizations = localizations;
	}
	
	public List<Localization> getLocalizations() {
		return localizations;
	}
	
	public void setTimeslots(List<Timeslot> timeslots) {
		this.timeslots = timeslots;
	}
	
	public List<Timeslot> getTimeslots() {
		return timeslots;
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
	 * Añade un equipo, si la composición del mismo es de dos jugadores o más
	 * 
	 * @param players jugadores que compondrán el nuevo equipo a añadir y pertenecientes a este evento
	 */
	public void addTeam(Player... players) {
		if (players.length >= 2) {
			if (teams == null)
				teams = new ArrayList<Team>();
			teams.add(new Team(players));
		}
	}
	
	/**
	 * @return true si sobre la categoría se definen equipos explícitos, y false si no
	 */
	public boolean hasTeams() {
		return teams != null && !teams.isEmpty();
	}
	
	public void setUnavailablePlayers(Map<Player, Set<Timeslot>> unavailability) {
		unavailablePlayers = unavailability;
	}
	
	public Map<Player, Set<Timeslot>> getUnavailablePlayers() {
		return unavailablePlayers;
	}
	
	/**
	 * Marca al jugador como no disponible a una hora determinada
	 * 
	 * @param player   jugador que pertenece a este evento
	 * @param timeslot hora perteneciente al dominio de este evento
	 */
	public void addUnavailablePlayer(Player player, Timeslot timeslot) {
		if (player != null && timeslots != null && players.contains(player) && timeslots.contains(timeslot)) {
			Set<Timeslot> unavailablePlayerTimeslots = unavailablePlayers.get(player);
			
			if (unavailablePlayerTimeslots == null) {
				unavailablePlayers.put(player, new HashSet<Timeslot>(Arrays.asList(timeslot)));
			} else {
				unavailablePlayerTimeslots.add(timeslot);
			}
		}
	}
	
	/**
	 * Marca al jugador como no disponible en una serie de horas
	 * 
	 * @param player    jugador que pertenece a este evento
	 * @param timeslots conjunto no vacío de horas, y todas ellas pertenecientes al dominio del evento
	 */
	public void addUnavailablePlayer(Player player, Set<Timeslot> timeslots) {	
		if (player != null && timeslots != null)
			for (Timeslot timeslot : timeslots)
				addUnavailablePlayer(player, timeslot);
	}
	
	/**
	 * Si el jugador no está disponible a la hora timeslot, se elimina de la lista y vuelve a estar disponible a esa hora
	 * 
	 * @param player   jugador que pertenece a este evento
	 * @param timeslot hora perteneciente al dominio del evento
	 */
	public void removePlayerUnavailableTimeslot(Player player, Timeslot timeslot) {
		if (player != null && timeslot != null) {
			Set<Timeslot> unavailablePlayerTimeslots = unavailablePlayers.get(player);
			
			if (unavailablePlayerTimeslots != null && unavailablePlayerTimeslots.contains(timeslot)) {
				unavailablePlayerTimeslots.remove(timeslot);
				
				if (unavailablePlayerTimeslots.isEmpty())
					unavailablePlayers.remove(player);
			}
		}
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
	 * @param matchup conjunto de jugadores entre los cuales habrá de darse un enfrentamiento. Los jugadores
	 *                pertenecen al conjunto de jugadores de este evento. El tamaño es igual al número de 
	 *                jugadores por partido definido
	 */
	public void addFixedMatchup(Set<Player> matchup) {
		if (matchup != null && matchup.size() == nPlayersPerMatch && !fixedMatchups.contains(matchup)) {
			boolean allInDomain = true;
			for (Player player : matchup) {
				if (!players.contains(player)) {
					allInDomain = false;
					break;
				}
			}
			
			if (allInDomain && !fixedMatchups.contains(matchup))
				fixedMatchups.add(matchup);
		}	
	}
	
	/**
	 * Añade un enfrentamiento fijo entre jugadoers
	 * 
	 * @param players conjunto de jugadores entre los cuales habrá de darse un enfrentamiento. Los jugadores
	 *                pertenecen al conjunto de jugadores de este evento. El tamaño es igual al número de 
	 *                jugadores por partido definido
	 */
	public void addFixedMatchup(Player... players) {
		if (players != null)
			addFixedMatchup(new HashSet<Player>(Arrays.asList(players)));
	}
	
	/**
	 * Añade un enfrentamiento fijo entre equipos
	 * 
	 * @param matchup conjunto de equipos entre los cuales habrá de darse un enfrentamiento. Los equipos, y por ende,
	 *                los jugadores que los componen, pertenecen a este evento, y el tamaño del conjunto total de
	 *                jugadores es igual al número definido de jugadores por partido
	 */
	public void addFixedTeamsMatchup(Set<Team> matchup) {
		if (matchup != null && matchup.size() > 1) {
			List<Team> matchupList = new ArrayList<Team>(matchup);
			
			int totalPlayers = 0;
			int playersPerTeam = matchupList.get(0).getPlayers().size();
			
			boolean valid = true;
			
			for (Team team : matchup) {
				int nPlayers = team.getPlayers().size();
				if (nPlayers != playersPerTeam) {
					valid = false;
					break;
				}
				
				totalPlayers += nPlayers;
			}
			
			if (valid && totalPlayers == nPlayersPerMatch) {
				Set<Player> playersInMatchup = new HashSet<Player>();
				for (Team team : matchup)
					playersInMatchup.addAll(team.getPlayers());
				
				addFixedMatchup(playersInMatchup);
			}
		}
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
	 * Comprueba si este evento tiene emparejamientos predefinidos
	 * 
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
	 * Añade una hora a la que no podrán tener lugar enfrentamientos en esta categoría, si no existe
	 * 
	 * @param timeslotBreak una hora perteneciente al dominio del evento
	 */
	public void addBreak(Timeslot timeslotBreak) {
		if (timeslotBreak != null && timeslots.contains(timeslotBreak) && !breaks.contains(timeslotBreak))
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
	
	public void setUnavailableLocalizations(Map<Localization, Set<Timeslot>> unavailableLocalizations) {
		this.unavailableLocalizations = unavailableLocalizations;
	}
	
	public Map<Localization, Set<Timeslot>> getUnavailableLocalizations() {
		return unavailableLocalizations;
	}
	
	/**
	 * Marca como inválida o no disponible una localización de juego a una hora determinada
	 * 
	 * @param localization localización perteneciente al conjunto de localizaciones del evento
	 * @param timeslot     hora perteneciente al conjunto de horas en las que el evento discurre
	 */
	public void addUnavailableLocalization(Localization localization, Timeslot timeslot) {
		if (localization != null && timeslot != null && localizations.contains(localization) && timeslots.contains(localization)) {
			Set<Timeslot> unavailableLocalizationTimeslots = unavailableLocalizations.get(localization);
			
			if (unavailableLocalizationTimeslots == null) {
				unavailableLocalizations.put(localization, new HashSet<Timeslot>(Arrays.asList(timeslot)));
			} else {
				unavailableLocalizationTimeslots.add(timeslot);
			}
		}
	}
	
	/**
	 * Marca como inválida o no disponible una localización de juego a un conjunto de horas determinado
	 * 
	 * @param localization localización perteneciente al conjunto de localizaciones del evento
	 * @param timeslot     conjunto de horas pertenecientes al dominio del evento
	 */
	public void addUnavailableLocalization(Localization localization, Set<Timeslot> timeslots) {
		if (localization != null && timeslots != null)
			for (Timeslot timeslot : timeslots)
				addUnavailableLocalization(localization, timeslot);
	}
	
	/**
	 * Elimina la invalidez de una localización, si la hubiese, volviendo a estar disponible a cualquier hora
	 * 
	 * @param localization localización perteneciente al conjunto de localizaciones del evento
	 */
	public void removeUnavailableLocalization(Localization localization) {
		unavailableLocalizations.remove(localization);
	}
	
	/**
	 * Elimina la invalidez de una localización a una hora, si la hubiese, volviendo a estar disponible a esa hora
	 * 
	 * @param localization localización perteneciente al conjunto de localizaciones del evento
	 * @param timeslot     hora perteneciente al conjunto de horas en las que el evento discurre
	 */
	public void removeUnavailableLocalizationTimeslot(Localization localization, Timeslot timeslot) {
		if (localization != null && timeslots != null) {
			Set<Timeslot> unavailableLocalizationTimeslots = unavailableLocalizations.get(localization);
			
			if (unavailableLocalizationTimeslots != null) {
				unavailableLocalizationTimeslots.remove(timeslot);
				
				if (unavailableLocalizationTimeslots.isEmpty())
					unavailableLocalizations.remove(localization);
			}
		}
	}
	
	/**
	 * Comprueba si hay localizaciones de juego no disponibles
	 * 
	 * @return true si hay alguna localización de juego no disponible, y false si no hay ninguna
	 */
	public boolean hasUnavailableLocalizations() {
		return !unavailableLocalizations.isEmpty();
	}
	
	public void setPlayersInLocalizations(Map<Player, Set<Localization>> playersInLocalizations) {
		this.playersInLocalizations = playersInLocalizations;
	}
	
	public Map<Player, Set<Localization>> getPlayersInLocalizations() {
		return playersInLocalizations;
	}
	
	/**
	 * Asigna al jugador una localización explícita donde ha de jugar
	 * 
	 * @param player       jugador perteneciente al conjunto de jugadores del evento
	 * @param localization localización perteneciente al conjunto de localizaciones del evento
	 */
	public void addPlayerInLocalization(Player player, Localization localization) {
		if (player != null && localization != null && players.contains(player) && localizations.contains(localization)) {
			Set<Localization> playerLocalizations = playersInLocalizations.get(player);
			
			if (playerLocalizations == null) {
				playersInLocalizations.put(player, new HashSet<Localization>(Arrays.asList(localization)));
			} else {
				playerLocalizations.add(localization);
			}
		}
	}
	
	/**
	 * Asigna al jugador las localizaciones de juego explícitas donde ha de jugar
	 * 
	 * @param player        jugador perteneciente al conjunto de jugadores del evento
	 * @param localizations conjunto de localizaciones pertenecientes al conjunto de localizaciones del evento
	 */
	public void addPlayerInLocalizations(Player player, List<Localization> localizations) {
		if (player != null && localizations != null)
			for (Localization localization : localizations)
				addPlayerInLocalization(player, localization);
	}
	
	/**
	 * Asigna a los jugadores las localizaciones de juego explícitas donde han de jugar
	 * 
	 * @param players       jugadores pertenecientes al evento
	 * @param localizations conjunto de localizaciones pertenecientes al conjunto de localizaciones del evento
	 */
	public void addPlayersInLocalizations(List<Player> players, List<Localization> localizations) {
		if (players != null && localizations != null)
			for (Player player : players)
				addPlayerInLocalizations(player, localizations);
	}
	
	/**
	 * Elimina de la configuración que el jugador deba jugar en la localización
	 * 
	 * @param player       jugador perteneciente al conjunto de jugadores del evento
	 * @param localization localización perteneciente al conjunto de localizaciones del evento
	 */
	public void removePlayerInLocalization(Player player, Localization localization) {
		if (player != null && localization != null) {
			Set<Localization> playerLocalizations = playersInLocalizations.get(player);
			
			if (playerLocalizations != null) {
				playerLocalizations.remove(localization);
				
				if (playerLocalizations.isEmpty())
					playersInLocalizations.remove(player);
			}
		}
	}
	
	/**
	 * Comprueba si hay jugadores a los que se les han asignado las localizaciones donde jugar
	 * 
	 * @return true si sobre el evento hay definidas asignaciones fijas de jugadores sobre localizaciones
	 */
	public boolean hasPlayersInLocalizations() {
		return !playersInLocalizations.isEmpty();
	}
	
	public void setPlayersAtTimeslots(Map<Player, Set<Timeslot>> playersAtTimeslots) {
		this.playersAtTimeslots = playersAtTimeslots;
	}
	
	public Map<Player, Set<Timeslot>> getPlayersAtTimeslots() {
		return playersAtTimeslots;
	}
	
	/**
	 * Asigna al jugador al timeslot explícito donde ha de jugar
	 * 
	 * @param player   jugador perteneciente al conjunto de jugadores del evento
	 * @param timeslot hora perteneciente al conjunto de horas en las que el evento discurre
	 */
	public void addPlayerAtTimeslot(Player player, Timeslot timeslot) {
		if (player != null && timeslot != null && players.contains(player) && timeslots.contains(timeslot)) {
			Set<Timeslot> playerTimeslots = playersAtTimeslots.get(player);
			
			if (playersAtTimeslots == null) {
				playersAtTimeslots.put(player, new HashSet<Timeslot>(Arrays.asList(timeslot)));
			} else {
				playerTimeslots.add(timeslot);
			}
		}
	}
	
	/**
	 * Asigna al jugador los timeslots explícitos donde ha de jugar
	 * 
	 * @param player    jugador perteneciente al conjunto de jugadores del evento
	 * @param timeslots conjunto de horas pertenecientes al conjunto de horas en las que el evento discurre
	 */
	public void addPlayerAtTimeslots(Player player, List<Timeslot> timeslots) {
		if (player != null && timeslots != null)
			for (Timeslot timeslot : timeslots)
				addPlayerAtTimeslot(player, timeslot);
	}
	
	/**
	 * Asigna a los jugadores los timeslots explícitos donde han de jugar
	 * 
	 * @param players   jugadores pertenecientes al evento
	 * @param timeslots conjunto de horas pertenecientes al conjunto de horas en las que el evento discurre
	 */
	public void addPlayersAtTimeslots(List<Player> players, List<Timeslot> timeslots) {
		if (players != null && timeslots != null)
			for (Player player : players)
				addPlayerAtTimeslots(player, timeslots);
	}
	
	/**
	 * Elimina de la configuración que el jugador deba jugar a la hora indicada
	 * 
	 * @param player   jugador perteneciente al conjunto de jugadores del evento
	 * @param timeslot hora perteneciente al conjunto de horas en las que el evento discurre
	 */
	public void removePlayerAtTimeslot(Player player, Timeslot timeslot) {
		if (player != null && timeslot != null) {
			Set<Timeslot> playerTimeslots = playersAtTimeslots.get(player);
			
			if (playerTimeslots != null) {
				playerTimeslots.remove(timeslot);
				
				if (playerTimeslots.isEmpty())
					playersAtTimeslots.remove(player);
			}
		}
	}
	
	/**
	 * Elimina de la configuración que el jugador deba jugar a las horas indicadas
	 * 
	 * @param player    jugador perteneciente al conjunto de jugadores del evento
	 * @param timeslots conjunto de horas pertenecientes al conjunto de horas en las que el evento discurre
	 */
	public void removePlayerAtTimeslots(Player player, List<Timeslot> timeslots) {
		if (player != null && timeslots != null)
			for (Timeslot timeslot : timeslots)
				removePlayerAtTimeslot(player, timeslot);
	}
	
	/**
	 * Comprueba si hay jugadores a los que se les han asignado horas de juego fijas
	 * 
	 * @return true si sobre el evento hay definidas asignaciones fijas de jugadores sobre timeslots
	 */
	public boolean hasPlayersAtTimeslots() {
		return !playersAtTimeslots.isEmpty();
	}
	
	/**
	 * Asigna el modo de emparejamiento de este evento, sólo si el número de partidos por jugador es superior a uno y
	 * el número de jugadores por partido es superior a uno
	 * 
	 * @param matchupMode
	 */
	public void setMatchupMode(MatchupMode matchupMode) {
		if (nMatchesPerPlayer > 1 && nPlayersPerMatch > 1)
			this.matchupMode = matchupMode;
	}
	
	public MatchupMode getMatchupMode() {
		return matchupMode;
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
	 * Comprueba si el jugador está disponible a una hora determinada
	 * 
	 * @param player   el jugador
	 * @param timeslot la hora
	 * @return         true si el jugador está disponible a esa hora, false si no
	 */
	public boolean isPlayerUnavailable(Player player, Timeslot timeslot) {
		return unavailablePlayers.containsKey(player) && unavailablePlayers.get(player).contains(timeslot);
	}
	
	/**
	 * Comprueba si la localización está invalidada a una hora determinada
	 * 
	 * @param localization la localización
	 * @param timeslot     la hora
	 * @return             true si está descartada, false si no
	 */
	public boolean isLocalizationUnavailable(Localization localization, Timeslot timeslot) {
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
	 * @return número de partidos del evento
	 */
	public int getNumberOfMatches() {
		return players.size() / nPlayersPerMatch * nMatchesPerPlayer;
	}
	
	public String toString() {
		return name;
	}
}
