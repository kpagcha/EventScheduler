package data.model.tournament.event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
	private List<Team> teams = new ArrayList<Team>();
	
	/**
	 * Lista de timeslots del evento donde no pueden tener lugar partidos
	 */
	private List<Timeslot> breaks = new ArrayList<Timeslot>();
	
	/**
	 * Timeslots u horas en las que cada jugador no está disponible
	 */
	private Map<Player, Set<Timeslot>> unavailablePlayers = new HashMap<Player, Set<Timeslot>>();
	
	/**
	 * Diccionario de localizaciones de juego no disponibles en determinadas horas
	 */
	private Map<Localization, Set<Timeslot>> unavailableLocalizations = new HashMap<Localization, Set<Timeslot>>();
	
	/**
	 * Emparejamientos fijos predefinidos. Es obligatorio que los jugadores que forman cada lista compongan enfrentamiento/s
	 */
	private List<Set<Player>> fixedMatchups = new ArrayList<Set<Player>>();
	
	/**
	 * Diccionario de jugadores para cada cual los enfrentamientos de los que forme parte han de tener lugar
	 * en cualquiera de las localizaciones del conjunto de localizaciones de juego asociado a su entrada
	 */
	private Map<Player, Set<Localization>> playersInLocalizations = new HashMap<Player, Set<Localization>>();
	
	/**
	 * Diccionario de jugadores para cada cual los enfrentamientos de los que forme parte han de tener lugar
	 * en cualquiera de las horas del conjunto de timeslots asociado a su entrada
	 */
	private Map<Player, Set<Timeslot>> playersAtTimeslots = new HashMap<Player, Set<Timeslot>>();
	
	/**
	 * Si esta categoría define más de un partido por jugador, indica el modo de emparejamiento
	 */
	private MatchupMode matchupMode = MatchupMode.ANY;
	
	/**
	 * Construye un evento con la información esencial: nombre, jugadores, localizaciones y timeslots.
	 * 
	 * Si alguno de los parámetros son null, se lanzará una excepción IllegalArgumentException
	 * 
	 * @param name          nombre del evento o de la categoría, no nulo
	 * @param players       jugadores que participan, lista no nula
	 * @param localizations localizaciones de juego en las que tendrá lugar, lista no nula
	 * @param timeslots     horas o timeslots en los que discurrirá, lista no nula
	 */
	public Event(String name, List<Player> players, List<Localization> localizations, List<Timeslot> timeslots) {
		if (name == null || players == null || localizations == null || timeslots == null)
			throw new IllegalArgumentException("The parameters cannot be null");
		
		this.name = name;
		this.players = players;
		this.localizations = localizations;
		this.timeslots = timeslots;
	}
	
	/**
	 * Asigna el nombre del torneo. Si es null se lanza IllegalArgumentException
	 * 
	 * @param name una cadena no nula que representa el nombre del torneo. Se lanzará excepción si la cadena es null
	 */
	public void setName(String name) {
		if (name == null)
			throw new IllegalArgumentException("Name cannot be null.");
			
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	/**
	 * Asigna la lista de jugadores si el parámetro no es null, si lo es se lanza una IllegalArgumentException
	 * 
	 * @param players una lista de jugadores. Si es null, se lanza una excepción
	 */
	public void setPlayers(List<Player> players) {
		if (players == null)
			throw new IllegalArgumentException("Players cannot be null.");
		
		this.players = players;
	}
	
	/**
	 * Devuelve la lista de jugadores como una lista no modificable
	 * 
	 * @return la lista de jugadores envuelta en un wrapper que la hace no modificable
	 */
	public List<Player> getPlayers() {
		return Collections.unmodifiableList(players);
	}
	
	/**
	 * Asigna la lista de localizaciones si el parámetro no es null, si lo es se lanza una IllegalArgumentException
	 * 
	 * @param localizations una lista de localizaciones. Si es null, se lanza una excepción
	 */
	public void setLocalizations(List<Localization> localizations) {
		if (localizations == null)
			throw new IllegalArgumentException("Localizations cannot be null.");
		
		this.localizations = localizations;
	}
	
	/**
	 * Devuelve la lista de localizaciones como una lista no modificable
	 * 
	 * @return la lista de localizaciones envuelta en un wrapper que la hace no modificable
	 */
	public List<Localization> getLocalizations() {
		return Collections.unmodifiableList(localizations);
	}
	
	/**
	 * Asigna la lista de horas o timeslots si el parámetro no es null, si lo es se lanza una IllegalArgumentException
	 * 
	 * @param timeslots una lista de timeslots. Si es null, se lanza una excepción
	 */
	public void setTimeslots(List<Timeslot> timeslots) {
		if (timeslots == null)
			throw new IllegalArgumentException("Timeslots cannot be null.");
		
		this.timeslots = timeslots;
	}
	
	/**
	 * Devuelve la lista de timeslots como una lista no modificable
	 * 
	 * @return la lista de timeslots envuelta en un wrapper que la hace no modificable
	 */
	public List<Timeslot> getTimeslots() {
		return Collections.unmodifiableList(timeslots);
	}
	
	/**
	 * Asigna el número de partidos que cada jugador del evento debe jugar. Si el parámetro es un
	 * valor inferior a 1, se lanza una IllegalArgumentException
	 * 
	 * @param nMatches un valor mayor o igual que 1. De lo contrario, se lanza una excepción
	 */
	public void setMatchesPerPlayer(int nMatches) {
		if (nMatches < 1)
			throw new IllegalArgumentException("Number of matches per player cannot be less than 1.");
		
		nMatchesPerPlayer = nMatches;
	}
	
	public int getMatchesPerPlayer() {
		return nMatchesPerPlayer;
	}
	
	/**
	 * Asigna la duración de los partidos del evento, es decir, el número de timeslots que cada partido
	 * ocupa. Si el valor que se intenta asignar es menor que 1, se lanza IllegalArgumentException
	 * 
	 * @param matchDuration un valor mayor o igual que 1. De lo contrario, se lanza una excepción
	 */
	public void setMatchDuration(int matchDuration) {
		if (matchDuration < 1)
			throw new IllegalArgumentException("Number of timeslots per match cannot be less than 1.");
		
		nTimeslotsPerMatch = matchDuration;
	}
	
	public int getMatchDuration() {
		return nTimeslotsPerMatch;
	}
	
	/**
	 * Asigna el número de jugadores de los que cada partido se compone. Si el valor es menor a 1, se lanza
	 * una IllegalArgumentException
	 * 
	 * @param nPlayers un valor mayor o igual que 1. De lo contrario, se lanza una excepción
	 */
	public void setPlayersPerMatch(int nPlayers) {
		if (nPlayers < 1)
			throw new IllegalArgumentException("Number of players per match cannot be less than 1.");
		
		nPlayersPerMatch = nPlayers;
	}
	
	public int getPlayersPerMatch() {
		return nPlayersPerMatch;
	}
	
	/**
	 * Asigna la lista de equipos que componen el evento.
	 * 
	 * Si el parámetro es null, se lanzará una IllegalArgumentException. Si algún jugador que compone algún 
	 * equipo no pertenece al conjunto de jugadores del evento, se lanza la misma excepción. También si un mismo jugador
	 * se encuentra en múltiples de los equipos pasados.
	 * 
	 * Si la lista de equipos pasada por parámetro incluye equipos repetidos, solamente se añadirán una vez a la lista de
	 * equipos del evento.
	 * 
	 * @param teams una lista de equipos no nula y donde los componentes de cada equipo pertenecen a la lista de jugadores
	 *              del evento. Si no se cumplean algunas de estas precondiciones se lanzará IllegalArgumentException.
	 */
	public void setTeams(List<Team> teams) {
		if (teams == null)
			throw new IllegalArgumentException("Teams cannot be null.");
		
		for (Team team : teams)
			if (!players.containsAll(team.getPlayers()))
				throw new IllegalArgumentException("All players in each team must be contained in the list of players of the event.");
		
		for (Team t1 : teams)
			for (Player player : t1.getPlayers())
				for (Team t2 : teams)
					if (t1 != t2 && t2.getPlayers().contains(player))
						throw new IllegalArgumentException("The same player cannot be present in multiple teams.");
			
		this.teams = new ArrayList<Team>();
		
		for (Team team : teams)
			if (!this.teams.contains(team))
				this.teams.add(team);
	}
	
	/**
	 * Devuelve la lista de equipos del torneo envuelta en un wrapper que la hace no modificable
	 * 
	 * @return la lista de equipos del torneo, no modificable
	 */
	public List<Team> getTeams() {
		return Collections.unmodifiableList(teams);
	}
	
	/**
	 * Añade un equipo, si la composición del mismo no es nula, de dos jugadores o más y pertenecientes al evento.
	 * 
	 * Si no se cumplen las precondiciones se lanzará una excepción IllegalArgumentException
	 * 
	 * @param players jugadores que compondrán el nuevo equipo a añadir y pertenecientes a este evento
	 */
	public void addTeam(Player... teamPlayers) {
		if (teamPlayers == null)
			throw new IllegalArgumentException("Players cannot be null");
		
		if (teamPlayers.length < 2)
			throw new IllegalArgumentException("Number of players in a team cannot be less than 2.");
		
		for (Player player : teamPlayers)
			if (!players.contains(player))
				throw new IllegalArgumentException("All players must be contained in the event's list of players.");
		
		teams.add(new Team(teamPlayers));
	}
	
	/**
	 * Elimina un equipo de la lista de equipos, si existe
	 * 
	 * @param team un equipo de jugadores
	 */
	public void removeTeam(Team team) {
		teams.remove(team);
	}
	
	/**
	 * @return true si sobre la categoría se definen equipos explícitos, y false si no
	 */
	public boolean hasTeams() {
		return !teams.isEmpty();
	}
	
	/**
	 * Asigna el diccionario que define las horas en las que los jugadores no están disponibles. Es opcional, y no
	 * todos los jugadores tienen por qué definir un conjunto de horas en las que no están disponibles.
	 * 
	 * Se lanzará IllegalArgumentException si el parámetro es null, si el diccionario incluye un jugador no perteneciente
	 * al evento, si el conjunto de horas no disponibles de un jugador es null o si alguna de las horas no pertenecen al evento.
	 * 
	 * @param unavailability un diccionario que define sobre cada jugador un conjunto de horas en las que no está disponible
	 */
	public void setUnavailablePlayers(Map<Player, Set<Timeslot>> unavailability) {
		if (unavailability == null)
			throw new IllegalArgumentException("Parameter cannot be null.");
		
		for (Player player : unavailability.keySet()) {
			if (!players.contains(player))
				throw new IllegalArgumentException("All players must be contained in the list of players of the event.");
			
			Set<Timeslot> playerUnavailableTimeslots = unavailability.get(player);
			if (playerUnavailableTimeslots == null)
				throw new IllegalArgumentException("The set of unavailable timeslots for the player cannot be null.");
			for (Timeslot t : playerUnavailableTimeslots)
				if (!timeslots.contains(t))
					throw new IllegalArgumentException("All timeslots must be contained in the list of timeslots of the event.");
		}
			
		unavailablePlayers = unavailability;
	}
	
	/**
	 * Devuelve el diccionario de jugadores y horas no disponibles en un wrapper que la hace no modificable
	 * 
	 * @return el diccionario de jugadores y sus horas no disponibles, no modificable
	 */
	public Map<Player, Set<Timeslot>> getUnavailablePlayers() {
		return Collections.unmodifiableMap(unavailablePlayers);
	}
	
	/**
	 * Marca al jugador como no disponible a una hora determinada
	 * 
	 * Se lanzará IllegalArgumentException si alguno de los parámetros son null o no pertenecen al dominio del evento.
	 * 
	 * Si para el jugador ya existe la hora que se intenta añadir como no disponible, no se modificará nada.
	 * 
	 * @param player   jugador que pertenece a este evento
	 * @param timeslot hora perteneciente al dominio de este evento
	 */
	public void addUnavailablePlayer(Player player, Timeslot timeslot) {
		if (player == null || timeslot == null)
			throw new IllegalArgumentException("The parameters cannot be null.");
		
		if (!players.contains(player))
			throw new IllegalArgumentException("The player must be contained in the list of players of the event.");
		
		if (!timeslots.contains(timeslot))
			throw new IllegalArgumentException("The timeslot must be contained in the list of timeslots of the event.");
		
		Set<Timeslot> unavailablePlayerTimeslots = unavailablePlayers.get(player);
		
		if (unavailablePlayerTimeslots == null) {
			unavailablePlayers.put(player, new HashSet<Timeslot>(Arrays.asList(timeslot)));
		} else {
			unavailablePlayerTimeslots.add(timeslot);
		}
	}
	
	/**
	 * Marca al jugador como no disponible en una serie de horas.
	 * 
	 * Si alguno de los parámetros es null, se lanza IllegalArgumentException
	 * 
	 * @param player    jugador que pertenece a este evento
	 * @param timeslots conjunto no vacío de horas, y todas ellas pertenecientes al dominio del evento
	 */
	public void addUnavailablePlayer(Player player, Set<Timeslot> timeslots) {	
		if (player == null || timeslots == null)
			throw new IllegalArgumentException("The parameters cannot be null.");
		
		for (Timeslot timeslot : timeslots)
			addUnavailablePlayer(player, timeslot);
	}
	
	/**
	 * Si el jugador no está disponible a la hora timeslot, se elimina de la lista y vuelve a estar disponible a esa hora.
	 * 
	 * Si alguno de los parámetros es null, no se hará nada.
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
	
	/**
	 * Asigna la lista de enfrentamientos fijos entre jugadores del evento.
	 * 
	 * Se lanza IllegalArgumentException si la list es null, o si contiene jugadores que no existen en este evento.
	 * 
	 * Si la lista pasada contiene enfrentamientos repetidos, solamente se añadirá uno de ellos
	 * 
	 * @param fixedMatchups una lista de múltiples enfrentamientos entre jugadores del evento
	 */
	public void setFixedMatchups(List<Set<Player>> fixedMatchups) {
		if (fixedMatchups == null)
			throw new IllegalArgumentException("The parameters cannot be null.");
		
		for (Set<Player> matchup : fixedMatchups)
			if (!players.containsAll(matchup))
				throw new IllegalArgumentException("All players must be contained in the list of players of the event.");
		
		for (Set<Player> matchup : fixedMatchups)
			if (!this.fixedMatchups.contains(matchup))
				this.fixedMatchups.add(matchup);
	}
	
	/**
	 * Devuelve la lista de emparejamientos fijos envuelta en un wrapper que la hace no modificable
	 * 
	 * @return la lista no modificable de emparejamientos fijos del evento
	 */
	public List<Set<Player>> getFixedMatchups() {
		return Collections.unmodifiableList(fixedMatchups);
	}
	
	/**
	 * Añade un enfrentamiento fijo entre jugadores.
	 * 
	 * Si el enfrentamiento es null, o el número de jugadores del enfrentamiento es distinto al número de jugadores por partido que este evento
	 * define, o alguno de los jugadores del enfrentamiento no pertenece al dominio del evento, se lanzará IllegalArgumentException.
	 * 
	 * Si el enfrentamiento ya existe, no habrá cambios.
	 * 
	 * @param matchup conjunto de jugadores entre los cuales habrá de darse un enfrentamiento. Los jugadores
	 *                pertenecen al conjunto de jugadores de este evento. El tamaño es igual al número de 
	 *                jugadores por partido definido
	 */
	public void addFixedMatchup(Set<Player> matchup) {
		if (matchup == null)
			throw new IllegalArgumentException("The matchup cannot be null.");
		
		if (matchup.size() != nPlayersPerMatch)
			throw new IllegalArgumentException("The number of players in the matchup must be equal to the number of players per match specified by this event.");
		
		if (!players.containsAll(matchup))
			throw new IllegalArgumentException("All players must be contained in the list of players of the event.");
				
		if (!fixedMatchups.contains(matchup))
			fixedMatchups.add(matchup);
	}
	
	/**
	 * Añade un enfrentamiento fijo entre jugadores.
	 * 
	 * Lanza IllegalArgumentException si el parámetro es null
	 * 
	 * @param players conjunto de jugadores entre los cuales habrá de darse un enfrentamiento. Los jugadores
	 *                pertenecen al conjunto de jugadores de este evento. El tamaño es igual al número de 
	 *                jugadores por partido definido
	 */
	public void addFixedMatchup(Player... players) {
		if (players == null)
			throw new IllegalArgumentException("The players cannot be null.");
		
		addFixedMatchup(new HashSet<Player>(Arrays.asList(players)));
	}
	
	/**
	 * Añade un enfrentamiento fijo entre equipos.
	 * 
	 * Se lanza IllegalArgumentException si el enfrentamiento es null, o si el número de equipos del enfrentamiento es menor que 2,
	 * o si alguno de los jugadores de los equipos no pertenecen al dominio del evento, o si el número de jugadores de cada equipo
	 * es diferente, o si el número de jugadores que componen el enfrentamiento no es igual al número de jugadores por partido
	 * especificado por este evento.
	 * 
	 * @param matchup conjunto de equipos entre los cuales habrá de darse un enfrentamiento. Los equipos, y por ende,
	 *                los jugadores que los componen, pertenecen a este evento, y el tamaño del conjunto total de
	 *                jugadores es igual al número definido de jugadores por partido
	 */
	public void addFixedTeamsMatchup(Set<Team> matchup) {
		if (matchup == null)
			throw new IllegalArgumentException("The matchup cannot be null.");
		
		if (matchup.size() < 2)
			throw new IllegalArgumentException("The number of teams to be matched up cannot be less than 2.");
		
		for (Team team : matchup)
			if (!players.containsAll(team.getPlayers()))
				throw new IllegalArgumentException("All players must be contained in the list of players of the event.");
		
		List<Team> matchupList = new ArrayList<Team>(matchup);
		
		int totalPlayers = 0;
		int playersPerTeam = matchupList.get(0).getPlayers().size();
		
		for (Team team : matchup) {
			int nPlayers = team.getPlayers().size();
			if (nPlayers != playersPerTeam)
				throw new IllegalArgumentException("The number of players in every team must be the same.");
			
			totalPlayers += nPlayers;
		}
		
		if (totalPlayers != nPlayersPerMatch)
			throw new IllegalArgumentException("The number of players in the matchup must be equal to the number of players per match specified by this event.");
		
		
		Set<Player> playersInMatchup = new HashSet<Player>();
		for (Team team : matchup)
			playersInMatchup.addAll(team.getPlayers());
		
		addFixedMatchup(playersInMatchup);
	}
	
	/**
	 * Elimina un enfrentamiento fijo entre jugadores. Si el enfrentamiento no existe, no se produce ninguna modificación.
	 * 
	 * @param matchup
	 */
	public void removeFixedMatchup(Set<Player> matchup) {
		fixedMatchups.remove(matchup);
	}
	
	/**
	 * Elimina un enfrentamiento fijo entre equipos. Si el enfrentamiento no existe, no se produce ninguna modificación.
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
