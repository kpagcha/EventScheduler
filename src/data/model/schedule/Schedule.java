package data.model.schedule;

import java.util.ArrayList;
import java.util.List;

import data.model.schedule.data.Match;
import data.model.schedule.data.ScheduleValue;
import data.model.tournament.event.entity.Localization;
import data.model.tournament.event.entity.Player;
import data.model.tournament.event.entity.timeslot.Timeslot;

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
				if (timeslot.compareTo(match.getStartTimeslot()) <= 0 && timeslot.compareTo(match.getEndTimeslot()) >= 0)
					timeslotMatches.add(match);
		}
		return timeslotMatches;
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
			sb.append(String.format("%4s", "t" + t));
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
