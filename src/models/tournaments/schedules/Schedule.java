package models.tournaments.schedules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.tournaments.events.entities.Localization;
import models.tournaments.events.entities.Player;
import models.tournaments.events.entities.Timeslot;
import models.tournaments.schedules.data.Match;
import models.tournaments.schedules.data.ScheduleValue;

public abstract class Schedule {
	
	/**
	 * Representación del horario con la ayuda de la clase ScheduleValue
	 */
	protected ScheduleValue[][] schedule;
	
	protected List<Match> matches;

	protected int nPlayers;
	protected int nCourts;
	protected int nTimeslots;

	protected List<Player> players;
	protected List<Localization> localizations;
	protected List<Timeslot> timeslots;
	
	protected String name;
	
	private Map<Localization, Map<Timeslot, List<Player>>> groupedSchedule;
	
	/**
	 * @return el horario como array bidimensional de enteros
	 */
	public ScheduleValue[][] getSchedule() {
		return schedule;
	}
	
	/**
	 * @return los partidos que componen este horario
	 */
	public List<Match> getMatches() {
		return matches;
	}
	
	/**
	 * Devuelve el conjunto de partidos en los que participa el jugador
	 * 
	 * @param player
	 * @return lista de partidos
	 */
	public List<Match> getMatchesByPlayer(Player player) {
		List<Match> playerMatches = null;
		if (matches != null) {
			playerMatches = new ArrayList<Match>();
			
			for (Match match : matches)
				if (match.getPlayers().contains(player))
					playerMatches.add(match);
		}
		return playerMatches;
	}
	
	/**
	 * Devuelve el conjunto de partidos en los que participan los jugadores
	 * 
	 * @param players
	 * @return lista de partidos
	 */
	public List<Match> getMatchesByPlayers(List<Player> players) {
		List<Match> playersMatches = null;
		if (matches != null) {
			playersMatches = new ArrayList<Match>();
			
			for (Match match : matches) {
				if (match.getPlayers().containsAll(players))
					playersMatches.add(match);
			}
		}
		return playersMatches;
	}
	
	/**
	 * Devuelve el conjunto de partidos que tienen lugar en la localización de juego
	 * 
	 * @param localization
	 * @return lista de partidos
	 */
	public List<Match> getMatchesByLocalization(Localization localization) {
		List<Match> localizationMatches = null;
		if (matches != null) {
			localizationMatches = new ArrayList<Match>();
			
			for (Match match : matches)
				if (match.getLocalization().equals(localization))
					localizationMatches.add(match);
		}
		return localizationMatches;
	}
	
	/**
	 * Devuelve el conjunto de partidos que tienen lugar en cualquiera de las localizaciones de juego
	 * 
	 * @param localization
	 * @return lista de partidos
	 */
	public List<Match> getMatchesByLocalizations(List<Localization> localizations) {
		List<Match> localizationMatches = null;
		if (matches != null) {
			localizationMatches = new ArrayList<Match>();
			
			for (Match match : matches) {
				Localization matchLocalization = match.getLocalization();
				for (Localization localization : localizations)
					if (localization.equals(matchLocalization))
						localizationMatches.add(match);
			}
		}
		return localizationMatches;
	}
	
	/**
	 * Devuelve el conjunto de partidos que empiezan en el timeslot indicado
	 * 
	 * @param timeslot
	 * @return lista de partidos
	 */
	public List<Match> getMatchesByStartTimeslot(Timeslot timeslot) {
		List<Match> timeslotMatches = null;
		if (matches != null) {
			timeslotMatches = new ArrayList<Match>();
			
			for (Match match : matches)
				if (match.getStartTimeslot().equals(timeslot))
					timeslotMatches.add(match);
		}
		return timeslotMatches;
	}
	
	/**
	 * Devuelve el conjunto de partidos que terminan en el timeslot indicado
	 * 
	 * @param timeslot
	 * @return lista de partidos
	 */
	public List<Match> getMatchesByEndTimeslot(Timeslot timeslot) {
		List<Match> timeslotMatches = null;
		if (matches != null) {
			timeslotMatches = new ArrayList<Match>();
			
			for (Match match : matches)
				if (match.getEndTimeslot().equals(timeslot))
					timeslotMatches.add(match);
		}
		return timeslotMatches;
	}
	
	/**
	 * Devuelve el conjunto de partidos que empiezan en el timeslot inicial y terminan en el final
	 * 
	 * @param start timeslot de comienzo
	 * @param end   timeslot final
	 * @return lista de partidos
	 */
	public List<Match> getMatchesByTimeslotRange(Timeslot start, Timeslot end) {
		List<Match> timeslotMatches = null;
		if (matches != null) {
			timeslotMatches = new ArrayList<Match>();
			
			for (Match match : matches)
				if (match.getStartTimeslot().equals(start) && match.getEndTimeslot().equals(end))
					timeslotMatches.add(match);
		}
		return timeslotMatches;
	}
	
	/**
	 * Devuelve el conjunto de partidos cuyo transcurso discurre sobre el timeslot indicado
	 * 
	 * @param timeslot
	 * @return lista de partidos
	 */
	public List<Match> getMatchesByTimeslot(Timeslot timeslot) {
		List<Match> timeslotMatches = null;
		if (matches != null) {
			timeslotMatches = new ArrayList<Match>();
			
			for (Match match : matches)
				if (timeslot.compareTo(match.getStartTimeslot()) >= 0 && timeslot.compareTo(match.getEndTimeslot()) <= 0)
					timeslotMatches.add(match);
		}
		return timeslotMatches;
	}
	
	/**
	 * Agrupa el horario por localizaciones de juego. Por cada pista habrá una lista de
	 * timeslots en los que se juega en esa pista, y a cada timeslot se asociará un conjunto
	 * de jugadores que se juegan en esa pista en ese momento
	 *
	 * @return número de timeslots ocupados
	 */
	public int groupByLocalizations() {
		groupedSchedule = new HashMap<Localization, Map<Timeslot, List<Player>>>();
		
		for (Localization localization : localizations)
			groupedSchedule.put(localization, new HashMap<Timeslot, List<Player>>());
			
		int timeslotOccupation = 0;
		for (int t = 0; t < nTimeslots; t++) {
			for (int p = 0; p < nPlayers; p++) {
				// Si el jugador_p juega en la hora_t
				if (schedule[p][t].isOccupied()) {
					Timeslot timeslot = timeslots.get(t);
					Player player = players.get(p);
					
					Localization localization = localizations.get(schedule[p][t].getLocalization());
					
					Map<Timeslot, List<Player>> timeslotMap = groupedSchedule.get(localization);
					if (timeslotMap.containsKey(timeslot))
						timeslotMap.get(timeslot).add(player);
					else {
						timeslotMap.put(timeslot, new ArrayList<Player>(Arrays.asList(new Player[]{ player })));
						timeslotOccupation++;
					}
				}
			}
		}
		
		return timeslotOccupation;
	}
	
	/**
	 * @return el horario agrupado por localizaciones de juego
	 */
	public Map<Localization, Map<Timeslot, List<Player>>> getScheduleGroupedByLocalizations() {
		return groupedSchedule;
	}
	
	/**
	 * Representa un horario agrupado por localizaciones con una cadena. Cada elemento muestra bien
	 * todos los jugadores que se encuentran en la pista_c a la hora_t (representados por su índice)
	 * o bien que en esa pista_c a esa hora_t no hay ningún enfrentamiento (porque simplemente
	 * ha quedado libre o porque hay un break o porque la pista ha sido descartada)
	 * 
	 * @return cadena con la representación del horario agrupado por pistas
	 */
	public String groupedScheduleToString() {
		if (groupedSchedule == null)
			throw new IllegalStateException("Grouped schedule has not been calculated yet.");
			
		StringBuilder sb = new StringBuilder(name);
		
		sb.append(String.format("\n\n%8s", " "));
		
		int maxPlayersPerMatch = 0;
		for (Map<Timeslot, List<Player>> timeslotMap : groupedSchedule.values())
			for (List<Player> playerList : timeslotMap.values())
				if (playerList.size() > maxPlayersPerMatch)
					maxPlayersPerMatch = playerList.size();
		
		int padding = maxPlayersPerMatch * 2 + 4;
		
		for (int t = 0; t < nTimeslots; t++)
			sb.append(String.format("%" + padding + "s", "t" + timeslots.get(t).getLowerBoundStr()));
		sb.append("\n");
		
		for (int c = 0; c < nCourts; c++) {
			Localization localization = localizations.get(c);
			
			sb.append(String.format("%8s", localization));
			
			Map<Timeslot, List<Player>> timeslotMap = groupedSchedule.get(localization);
			for (int t = 0; t < nTimeslots; t++) {
				Timeslot timeslot = timeslots.get(t);
				
				String strValue = "";
				if (timeslotMap.containsKey(timeslot)) {
					strValue = "";
					for (Player player : timeslotMap.get(timeslot))
						strValue += players.indexOf(player) + ",";
					
					strValue = strValue.substring(0, strValue.length() - 1);
				} else {
					strValue = "=";
				}
				sb.append(String.format("%" + padding + "s", strValue));
			}
			sb.append("\n");
		}
		
		return sb.toString();
	}
	
	public String toString() {
		return scheduleToString(schedule);
	}
	
	/**
	 * @param scheduleArray array que representa un horario
	 * @return cadena que representa el horario
	 */
	private String scheduleToString(ScheduleValue[][] scheduleArray) {
		StringBuilder sb = new StringBuilder(name);
		
		sb.append(String.format("\n\n%8s", " "));
		
		for (int t = 0; t < nTimeslots; t++)
			sb.append(String.format("%4s", "t" + timeslots.get(t).getLowerBoundStr()));
		sb.append("\n");
		
		for (int p = 0; p < nPlayers; p++) {
			String playerStr = players.get(p).toString();
			if (playerStr.length() > 8)
				playerStr = playerStr.substring(0, 8);
				
			sb.append(String.format("%8s", playerStr));
			for (int t = 0; t < nTimeslots; t++)
				sb.append(String.format("%4s", scheduleArray[p][t]));
			sb.append("\n");
		}
		
		return sb.toString();
	}
}
