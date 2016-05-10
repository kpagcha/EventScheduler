package data.validation.validator.tournament;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import data.model.tournament.event.Event;
import data.model.tournament.event.entity.Localization;
import data.model.tournament.event.entity.Player;
import data.model.tournament.event.entity.Team;
import data.model.tournament.event.entity.timeslot.Timeslot;
import data.validation.validator.Validator;

/**
 * Valida el estado final de un evento o categor�a.
 * <p>
 * El estado final de un evento se ha de sujetar a las siguientes invariantes:
 * 
 * <h2>Nombre</h2>
 * <ul>
 * <li>No <code>null</code>
 * </ul>
 * 
 * <h2>Jugadores</h2>
 * <ul>
 * <li>Lista no <code>null</code>
 * <li>Lista no vac�a
 * <li>Lista sin ning�n elemento <code>null</code>
 * <li>El n�mero de jugadores debe ser m�ltiplo del n�mero de jugadores por partido
 * <li>Ning�n jugador puede estar repetido en la lista
 * </ul>
 * 
 * <h2>Localizaciones de juego</h2>
 * <ul>
 * <li>Lista no <code>null</code>
 * <li>Lista no vac�a
 * <li>Lista sin elementos <code>null</code>
 * <li>Lista sin elementos repetidos
 * </ul>
 * 
 * <h2>Horas de juego <i>(timeslots)</i></h2>
 * <ul>
 * <li>Lista no <code>null</code>
 * <li>Lista no vac�a
 * <li>Lista sin elementos <code>null</code>
 * <li>Ninguna hora puede estar repetida
 * <li>Cada hora debe ser anterior a que le sigue en la lista
 * <li>El n�mero de <i>timeslots</i> disponibles debe ser mayor o igual que el n�mero de partidos por jugador multiplicado por la duraci�n de un partido
 * </ul>
 * 
 * <h2>N�mero de partidos por jugador</h2>
 * <ul>
 * <li>Debe ser mayor que 0
 * <li>Su producto por la duraci�n de un partido debe ser menor o igual que el n�mero de <i>timeslots</i>
 * </ul>
 * 
 * <h2>N�mero de jugadores por partido</h2>
 * <ul>
 * <li>Debe ser mayor que 0
 * <li>Debe ser divisor del n�mero de jugadores del evento
 * </ul>
 * 
 * <h2>Duraci�n de un partido (n�mero de <i>timeslots</i> que ocupa)</h2>
 * <ul>
 * <li>Debe ser mayor que 0
 * <li>Su producto por el n�mero de partidos por jugador debe ser menor o igual que el n�mero de <i>timeslots</i>
 * </ul>
 * 
 * <h2>Equipos</h2>
 * <ul>
 * <li>Lista no <code>null</code>
 * <li>Lista sin elementos <code>null</code>
 * <li>Lista sin equipos repetidos
 * <li>Si hay equipos, es decir, la lista no est� vac�a, debe haber m�s de 1
 * <li>Todos los equipos deben tener el mismo n�mero de jugadores
 * <li>Todos los jugadores de cada equipo deben pertenecer a la lista de jugadores del evento
 * <li>No puede existir un jugador en m�s de un equipo
 * <li>El n�mero total de jugadores en todos los equipos no puede superior al n�mero total de jugadores del evento
 * </ul>
 * 
 * <h2>Jugadores no disponibles a determinadas horas</h2>
 * <ul>
 * <li>Diccionario no <code>null</code>
 * <li>Ning�n jugador puede ser <code>null</code>
 * <li>Ninguna hora asociada a un jugador puede ser <code>null</code>
 * <li>Todos los jugadores deben existir en la lista de jugadores del evento
 * <li>Todos los <i>timeslots</i> asociados a cada jugador deben existir en la lista de horas de juego del evento
 * <li>No se puede asociar una lista de horas no disponibles vac�a a un jugador
 * </ul>
 * 
 * <h2>Lista de enfrentamientos fijos</h2>
 * <ul>
 * <li>Lista no <code>null</code>
 * <li>Ning�n conjunto de enfrentamientos puede ser <code>null</code>
 * <li>Ning�n jugador en los conjuntos de enfrentamientos puede ser <code>null</code>
 * <li>Un mismo enfrentamiento no puede estar repetido
 * <li>Todos los jugadores de cada enfrentamiento deben pertenecer a la lista de jugadores del evento
 * <li>El n�mero de jugadores que componen cada enfrentamiento debe ser el esperado, es decir, el n�mero de jugadores por partido especificado por el evento
 * </ul>
 * 
 * <h2>Lista de <i>breaks</i></h2>
 * <ul>
 * <li>Lista no <code>null</code>
 * <li>Ning�n <i>timeslot</i> de la lista puede ser <code>null</code>
 * <li>Ning�n <i>timeslot</i> de la lista puede estar repetido
 * <li>Todos los <i>timeslots</i> deben pertenecer a la lista de <i>timeslots</i> del evento
 * </ul>
 * 
 * <h2>Localizaciones no disponibles a determinadas horas</h2>
 * <ul>
 * <li>Lista no <code>null</code>
 * <li>Ninguna localizaci�n puede ser <code>null</code>
 * <li>Ning�n conjunto de <i>timeslots</i> asociados a una localizaci�n puede ser <code>null</code>
 * <li>Ning�n <i>timeslot</i> de los conjuntos asociados a las localizaciones puede ser <code>null</code>
 * <li>Todas las localizaciones deben pertenecer a la lista de localizaciones de juego del evento
 * <li>Todos los <i>timeslots</i> de los conjuntos asociados a las localizaciones deben pertenecer a la lista de horas de juego del evento
 * </ul>
 * 
 * <h2>Jugadores en localizaciones fijas</h2>
 * <ul>
 * <li>Lista no <code>null</code>
 * <li>Ning�n jugador puede ser <code>null</code>
 * <li>La lista de localizaciones fijas no puede ser <code>null</code>
 * <li>Ninguna localizaci�n fija asignada a un jugador puede ser <code>null</code>
 * <li>Todos los jugadores deben pertenecer a la lista de jugadores del evento
 * <li>Todas las localizaciones deben pertenecer a la lista de localizaciones del evento
 * </ul>
 * 
 * <h2>Jugadores en horas fijas</h2>
 * <ul>
 * <li>Lista no <code>null</code>
 * <li>Ning�n jugador puede ser <code>null</code>
 * <li>La lista de horas fijas no puede ser <code>null</code>
 * <li>Ninguna hora fija asignada a un jugador puede ser <code>null</code>
 * <li>Todos los jugadores deben pertenecer a la lista de jugadores del evento
 * <li>Todas las horas deben pertenecer a la lista de <i>timeslots</i> del evento
 * </ul>
 *
 */
public class EventValidator implements Validator<Event> {
	/**
	 * Mensajes de error de la validaci�n
	 */
	private List<String> messages = new ArrayList<String>();
	
	public boolean validate(Event event) {
		messages.clear();
		
		// No se devuelve directamente para formar los mensajes de validaci�n
		boolean isValid = validateName(event) && validatePlayers(event) && validateLocalizations(event) && validateTimeslots(event) &&
			validateMatchesPerPlayer(event) && validatePlayersPerMatch(event) && validateMatchDuration(event) &&
			validateTeams(event) && validateUnavailablePlayers(event) && validateFixedMatchups(event) && validateBreaks(event) && 
			validateUnavailableLocaliztions(event) && validatePlayersInLocalizations(event) && validatePlayersAtTimeslots(event);
		
		return isValid;
	}

	/**
	 * Valida un nombre para el evento
	 * 
	 * @param event event no <code>null</code>
	 * @return <code>true</code> si la pasa la validaci�n, <code>false</code> si es fallida
	 */
	private boolean validateName(Event event) {
		if (event.getName() == null) {
			messages.add("Name cannot be null");
			return false;
		}
		return true;
	}
	
	/**
	 * Valida la lista de jugadores del evento
	 * 
	 * @param e evento no <code>null</code>
	 * @return <code>true</code> si la lista de jugadores es v�lida, <code>false</code> si no
	 */
	private boolean validatePlayers(Event e) {
		 List<Player> players = e.getPlayers();
		 
		 if (players == null) {
			 messages.add("Players cannot be null");
			 return false;
		 }
		 
		 boolean isValid = true;
		 
		 if (players.isEmpty()) {
			 isValid = false;
			 messages.add("Players cannot be empty, there must be at least one player");
		 }
		 
		 if (players.contains(null)) {
			 messages.add("Player cannot contain a null player");
			 return false;
		 }
		 
		 for (int i = 0; i < players.size() - 1; i++)
			 for (int j = i + 1; j < players.size(); j++)
				 if (players.get(i) == players.get(j)) {
					 isValid = false;
					 messages.add(String.format("Players must contain unique elements; player (%s) is duplicated)", players.get(i)));
				 }
		 
		 if (players.size() % e.getPlayersPerMatch() != 0) {
			 isValid = false;
			 messages.add(String.format("Number of players (%d) must be a multiple of the specified number of players per match (%d)",
					 players.size(), e.getPlayersPerMatch()));
		 }
		 
		 return isValid;
	}
	
	/**
	 * Valida las localizaciones de juego del evento
	 * 
	 * @param event evento no <code>null</code>
	 * @return si la validaci�n es satisfactoria <code>true</code>, de lo contrario, se devuelve <code>false</code>
	 */
	private boolean validateLocalizations(Event event) {
		List<Localization> localizations = event.getLocalizations();
		
		if (localizations == null) {
			messages.add("Localizations cannot be null");
			return false;
		}
		
		boolean isValid = true;
		
		if (localizations.isEmpty()) {
			isValid = false;
			messages.add("Localizations cannot be empty, there must be at least one localization");
		}
		
		if (localizations.contains(null)) {
			messages.add("Localizations cannot contain a null localization");
			return false;
		}
		
		for (int i = 0; i < localizations.size() - 1; i++)
			for (int j = i + 1; j < localizations.size(); j++)
				if (localizations.get(i) == localizations.get(j)) {
					isValid = false;
					messages.add(String.format("Localizations must contain unique elements; localization (%s) is duplicated", localizations.get(i)));
				}
		
		return isValid;
	}

	/**
	 * Valida las horas de juego del evento
	 * 
	 * @param event evento no <code>null</code>
	 * @return si la validaci�n es satisfactoria <code>true</code>, de lo contrario, se devuelve <code>false</code>
	 */
	private boolean validateTimeslots(Event event) {
		List<Timeslot> timeslots = event.getTimeslots();
		
		if (timeslots == null) {
			messages.add("Timeslots cannot be null");
			return false;
		}
		
		boolean isValid = true;
		
		if (timeslots.isEmpty()) {
			isValid = false;
			messages.add("Timeslots cannot be empty, there must be at least one timeslot");
		}
		
		if (timeslots.contains(null)) {
			messages.add("Timeslots cannot contain a null timeslot");
			return false;
		}
		
		for (int i = 0; i < timeslots.size() - 1; i++) {
			for (int j = i + 1; j < timeslots.size(); j++)
				if (timeslots.get(i) == timeslots.get(j)) {
					isValid = false;
					messages.add(String.format("Timeslots must contain unique elements; timeslot (%s) is duplicated",  timeslots.get(i)));
				}
		}
		
		for (int i = 0; i < timeslots.size() - 1; i++) {
			if (timeslots.get(i).compareTo(timeslots.get(i + 1)) < 1) {
				isValid = false;
				messages.add(String.format("A timeslot must be greater than the following; timeslot (%s) is not greater than timeslot (%s)",
						timeslots.get(i), timeslots.get(i + 1)));
			}
		}
		
		if (timeslots.size() < event.getMatchesPerPlayer() * event.getTimeslotsPerMatch()) {
			isValid = false;
			messages.add(String.format("Number of timeslots (%d) must not be less than the minimum needed amount (%d)",
					timeslots.size(), event.getMatchesPerPlayer() * event.getTimeslotsPerMatch()));
		}
		
		return isValid;
	}

	/**
	 * Valida el n�mero de jugadores por partido del evento
	 * 
	 * @param event evento no <code>null</code>
	 * @return si la validaci�n es satisfactoria <code>true</code>, de lo contrario, se devuelve <code>false</code>
	 */
	private boolean validatePlayersPerMatch(Event event) {
		boolean isValid = true;
		
		int nPlayersPerMatch = event.getPlayersPerMatch();
		
		if (nPlayersPerMatch < 1) {
			isValid = false;
			messages.add(String.format("Number of players (%d) per match must be greater than 0", nPlayersPerMatch));
		}
		
		if (event.getPlayers().size() % nPlayersPerMatch != 0) {
			isValid = false;
			messages.add(String.format("Number of players per match (%d) must be a multiple of the total number of players (%d)",
					nPlayersPerMatch, event.getPlayers().size())); 
		}
		
		return isValid;
	}

	/**
	 * Valida el n�mero de partidos por jugador del evento
	 * 
	 * @param event evento no <code>null</code>
	 * @return si la validaci�n es satisfactoria <code>true</code>, de lo contrario, se devuelve <code>false</code>
	 */
	private boolean validateMatchesPerPlayer(Event event) {
		boolean isValid = true;
		
		int nMatchesPerPlayer = event.getMatchesPerPlayer();
		
		if (nMatchesPerPlayer < 1) {
			isValid = false;
			messages.add(String.format("Number of matches per player (%d) must be greater than 0", nMatchesPerPlayer));
		}
		
		if (event.getTimeslots().size() < nMatchesPerPlayer * event.getTimeslotsPerMatch()) {
			isValid = false;
			messages.add(String.format("Number of timeslots (%d) must not be less than the minimum needed amount (%d)",
					event.getTimeslots().size(), nMatchesPerPlayer * event.getTimeslotsPerMatch()));
		}
		
		return isValid;
	}

	/**
	 * Valida la duraci�n de un partido del evento
	 * 
	 * @param event evento no <code>null</code>
	 * @return si la validaci�n es satisfactoria <code>true</code>, de lo contrario, se devuelve <code>false</code>
	 */
	private boolean validateMatchDuration(Event event) {
		boolean isValid = true;
		
		int nTimeslotsPerMatch = event.getTimeslotsPerMatch();
		
		if (nTimeslotsPerMatch < 1) {
			isValid = false;
			messages.add(String.format("Number of timeslots per match (%d) must be greater than 0", nTimeslotsPerMatch));
		}
		
		if (event.getTimeslots().size() < event.getMatchesPerPlayer() * nTimeslotsPerMatch) {
			isValid = false;
			messages.add(String.format("Number of timeslots (%d) must not be less than the minimum needed amount (%d)",
					event.getTimeslots().size(), event.getMatchesPerPlayer() * nTimeslotsPerMatch));
		}
		
		return isValid;
	}

	/**
	 * Valida los equipos del evento
	 * 
	 * @param event evento no <code>null</code>
	 * @return si la validaci�n es satisfactoria <code>true</code>, de lo contrario, se devuelve <code>false</code>
	 */
	private boolean validateTeams(Event event) {
		List<Team> teams = event.getTeams();
		
		if (teams == null) {
			messages.add("Teams cannot be null");
			return false;
		}
		
		boolean isValid = true;
		
		if (!teams.isEmpty()) {
			if (teams.size() < 2) {
				isValid = false;
				messages.add("There cannot be less than 2 teams");
			}
			
			if (teams.contains(null)) {
				messages.add("Teams cannot contain a null team");
				return false;
			}
			
			for (int i = 0; i < teams.size() - 1; i++)
				for (int j = i + 1; j < teams.size(); j++)
					if (teams.get(i) == teams.get(j)) {
						isValid = false;
						messages.add(String.format("Teams cannot be duplicated; team (%s) is", teams.get(i)));
					}
			
			int nPlayersPerTeam = teams.get(0).getPlayers().size();
			for (int i = 1; i < teams.size(); i++)
				if (teams.get(i).getPlayers().size() != nPlayersPerTeam) {
					isValid = false;
					messages.add(String.format("All teams must have the same number of players (%d); team (%s) has %d",
							nPlayersPerTeam, teams.get(i), teams.get(i).getPlayers().size()));
				}
			
			for (Team team : teams)
				for (Player player : team.getPlayers())
					if (!event.getPlayers().contains(player)) {
						isValid = false;
						messages.add(String.format("All players must exist in the list of players of the event; player (%s) does not", player));
					}
			
			for (int i = 0; i < teams.size() - 1; i++)
				for (Player player : teams.get(i).getPlayers())
					for (int j = i + 1; j < teams.size(); j++)
						if (teams.get(j).getPlayers().contains(player)) {
							isValid = false;
							messages.add(String.format("A player cannot exist in multiple teams; player (%s) is duplicated", player));
						}
			
			int nPlayers = 0;
			for (Team team : teams)
				nPlayers += team.getPlayers().size();
			if (nPlayers > event.getPlayers().size()) {
				isValid = false;
				messages.add(String.format("The number of player in all teams (%d) cannot be greater than the total number of players in the event (%d)",
						nPlayers, event.getPlayers().size()));
			}
		}
		
		return isValid;
	}

	/**
	 * Valida las horas a las que determinados jugadores no est�n disponibles
	 * 
	 * @param event evento no <code>null</code>
	 * @return si la validaci�n es satisfactoria <code>true</code>, de lo contrario, se devuelve <code>false</code>
	 */
	private boolean validateUnavailablePlayers(Event event) {
		Map<Player, Set<Timeslot>> unavailablePlayers = event.getUnavailablePlayers();
		
		if (unavailablePlayers == null) {
			messages.add("Players unavailable cannot be null");
			return false;
		}
		
		boolean isValid = true;
		
		if (unavailablePlayers.containsKey(null)) {
			isValid = false;
			messages.add("Player cannot be null");
		}
		
		if (unavailablePlayers.containsValue(null)) {
			isValid = false;
			messages.add("Set of unavailable timeslots associated to a player cannot be null");
		}
		
		for (Set<Timeslot> timeslots : unavailablePlayers.values()) {
			if (timeslots.contains(null)) {
				isValid = false;
				messages.add("Timeslot cannot be null");
			} else if (timeslots.isEmpty()) {
				isValid = false;
				messages.add("Set of unavailable timeslots associated to a player cannot be empty");
			} else {
				for (Timeslot timeslot : timeslots)
					if (!event.getTimeslots().contains(timeslot)) {
						isValid = false;
						messages.add(String.format("All timeslots must exist in the list of timeslots of the event; timeslot (%s) does not",
								timeslot));
					}
			}	
		}
		
		for (Player player : unavailablePlayers.keySet())
			if (!event.getPlayers().contains(player)) {
				isValid = false;
				messages.add(String.format("All players must exist in the list of players of the event; player (%s) does not", player));
			}
		
		return isValid;
	}

	/**
	 * Valida la lista de emparejamientos predefinidos
	 * 
	 * @param event evento no <code>null</code>
	 * @return si la validaci�n es satisfactoria <code>true</code>, de lo contrario, se devuelve <code>false</code>
	 */
	private boolean validateFixedMatchups(Event event) {
		List<Set<Player>> matchups = event.getPredefinedMatchups();
		
		if (matchups == null) {
			messages.add("List of fixed matchups cannot be null");
			return false;
		}
		
		boolean isValid = true;
		
		if (matchups.contains(null)) {
			messages.add("Matchup cannot be null");
			return false;
		}
		
		for (Set<Player> matchup : matchups) {
			if (matchup.contains(null)) {
				isValid = false;
				messages.add("Matchup cannot contain a null player");
			}
		}
		
		for (int i = 0; i < matchups.size() - 1; i++)
			for (int j = i + 1; j < matchups.size(); j++)
				if (matchups.get(i) == matchups.get(j)) {
					isValid = false;
					messages.add(String.format("A matchup cannot be exist multiple times; matchup (%s) is duplicated", matchups.get(i)));
				}
		
		for (Set<Player> matchup : matchups) {
			for (Player player : matchup)
				if (!event.getPlayers().contains(player)) {
					isValid = false;
					messages.add(String.format("All players must exist in the list of players of the event; player (%s) does not", player));
				}
			
			if (matchup.size() != event.getPlayersPerMatch()) {
				isValid = false;
				messages.add(String.format(
						"The number of players in this matchup (%d) must be the number of players per match specified in the event (%d)", 
						matchup.size(), event.getPlayersPerMatch())
				);
			}
		}
		
		return isValid;
	}

	/**
	 * Valida los descansos o <i>breaks</i>
	 * 
	 * @param event evento no <code>null</code>
	 * @return si la validaci�n es satisfactoria <code>true</code>, de lo contrario, se devuelve <code>false</code>
	 */
	private boolean validateBreaks(Event event) {
		List<Timeslot> breaks = event.getBreaks();
		
		if (breaks == null) {
			messages.add("Breaks cannot be null");
			return false;
		}
		
		boolean isValid = true;
		
		if (breaks.contains(null)) {
			isValid = false;
			messages.add("Break cannot be null");
		}
		
		for (int i = 0; i < breaks.size() - 1; i++)
			for (int j = i + 1; j < breaks.size(); j++)
				if (breaks.get(i) == breaks.get(j)) {
					isValid = false;
					messages.add(String.format("All breaks must be unique; break (%s) is duplicated", breaks.get(i)));
				}
		
		for (Timeslot timeslot : breaks)
			if (!event.getTimeslots().contains(timeslot)) {
				isValid = false;
				messages.add(String.format("All breaks must exist in the list of timeslots of the event; timeslot (%s) does not", timeslot));
			}
		
		return isValid;
	}

	/**
	 * Valida las horas a las que determinadas localizaciones no est�n disponibles
	 * 
	 * @param event evento no <code>null</code>
	 * @return si la validaci�n es satisfactoria <code>true</code>, de lo contrario, se devuelve <code>false</code>
	 */
	private boolean validateUnavailableLocaliztions(Event event) {
		Map<Localization, Set<Timeslot>> unavailableLocalizations = event.getUnavailableLocalizations();
		
		if (unavailableLocalizations == null) {
			messages.add("Unavailable localizations cannot be null");
			return false;
		}
		
		boolean isValid = true;
		
		for (Localization localization : unavailableLocalizations.keySet()) {
			if (localization == null) {
				isValid = false;
				messages.add("Unavailable localizations cannot be null");
			} else if (!event.getLocalizations().contains(localization)) {
				isValid = false;
				messages.add(String.format("All localizations must exist in the list of localizations of the event; localization (%s) does not",
						localization));
			}
		}
		
		for (Set<Timeslot> timeslots : unavailableLocalizations.values()) {
			if (timeslots == null) {
				isValid = false;
				messages.add("Set of timeslots cannot be null");
			} else {
				for (Timeslot timeslot : timeslots) {
					if (timeslot == null) {
						isValid = false;
						messages.add("Timeslot cannot be null");
					} else if (!event.getTimeslots().contains(timeslot)) {
						isValid = false;
						messages.add(String.format("All timeslots must exist in the list of timeslots of the event; timeslot (%s) does not", 
								timeslot));
					}
				}
			}
		}
		
		return isValid;
	}

	/**
	 * Valida los jugadores a los que se les asgina un conjunto de localizaciones donde sus enfrentamientos deben tener lugar
	 * 
	 * @param event evento no <code>null</code>
	 * @return si la validaci�n es satisfactoria <code>true</code>, de lo contrario, se devuelve <code>false</code>
	 */
	private boolean validatePlayersInLocalizations(Event event) {
		Map<Player, Set<Localization>> playersInLocalizations = event.getPlayersInLocalizations();
		
		if (playersInLocalizations == null) {
			messages.add("Players in localizations cannot be null");
			return false;
		}
		
		boolean isValid = true;
		
		for (Player player : playersInLocalizations.keySet()) {
			if (player == null) {
				isValid = false;
				messages.add("Players in localizations cannot be null");
			} else if (!event.getPlayers().contains(player)) {
				isValid = false;
				messages.add(String.format("All players must exist in the list of players of the event; player (%s) does not",
						player));
			}
		}
		
		for (Set<Localization> localizations : playersInLocalizations.values()) {
			if (localizations == null) {
				isValid = false;
				messages.add("Set of localizations cannot be null");
			} else {
				for (Localization localization : localizations) {
					if (localization == null) {
						isValid = false;
						messages.add("Localization cannot be null");
					} else if (!event.getLocalizations().contains(localization)) {
						isValid = false;
						messages.add(String.format("All localizations must exist in the list of localizations of the event; localization (%s) does not", 
								localization));
					}
				}
			}
		}
		
		return isValid;
	}

	/**
	 * Valida la lista de jugadores a los que se les asigna un conjunto de horas a las que jugar
	 * 
	 * @param event evento no <code>null</code>
	 * @return si la validaci�n es satisfactoria <code>true</code>, de lo contrario, se devuelve <code>false</code>
	 */
	private boolean validatePlayersAtTimeslots(Event event) {
		Map<Player, Set<Timeslot>> playersAtTimeslots = event.getPlayersAtTimeslots();
		
		if (playersAtTimeslots == null) {
			messages.add("Players at timeslots cannot be null");
			return false;
		}
		
		boolean isValid = true;
		
		for (Player player : playersAtTimeslots.keySet()) {
			if (player == null) {
				isValid = false;
				messages.add("Players at timeslots cannot be null");
			} else if (!event.getPlayers().contains(player)) {
				isValid = false;
				messages.add(String.format("All players must exist in the list of players of the event; player (%s) does not",
						player));
			}
		}
		
		for (Set<Timeslot> timeslots : playersAtTimeslots.values()) {
			if (timeslots == null) {
				isValid = false;
				messages.add("Set of timeslots cannot be null");
			} else {
				for (Timeslot timeslot : timeslots) {
					if (timeslot == null) {
						isValid = false;
						messages.add("Timeslot cannot be null");
					} else if (!event.getTimeslots().contains(timeslot)) {
						isValid = false;
						messages.add(String.format("All timeslots must exist in the list of timeslots of the event; timeslots (%s) does not", 
								timeslot));
					}
				}
			}
		}
		
		return isValid;
	}

	public List<String> getValidationMessages() {
		return messages;
	}
}
