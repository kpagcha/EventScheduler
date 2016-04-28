package data.model.schedule;

import java.util.ArrayList;
import java.util.List;

import data.model.schedule.data.Match;
import data.model.schedule.data.ScheduleValue;
import data.model.tournament.event.entity.Localization;
import data.model.tournament.event.entity.Player;
import data.model.tournament.event.entity.timeslot.Timeslot;

/**
 * Representa un horario mediante una matriz bidimensional de {@link ScheduleValue} que dota de significado a cada elemento.
 * <p>
 * La primera dimensión de la matriz (filas) corresponde a los jugadores y la segunda dimensión (columnas)
 * corresponde con las horas de juego
 * <p>
 * Además, mantiene una lista de partidos (ver {@link Match}) que se extraen del procesamiento de la matriz mencionada.
 *
 */
public abstract class Schedule {
	
	/**
	 * Representación del horario con la ayuda de la clase {@link ScheduleValue}
	 */
	protected ScheduleValue[][] schedule;
	
	/**
	 * Lista de partidos que se dan en el horario
	 */
	protected List<Match> matches;

	/**
	 * Número de jugadores
	 */
	protected int nPlayers;
	
	/**
	 * Número de pistas
	 */
	protected int nLocalizations;
	
	/**
	 * Número de timeslots
	 */
	protected int nTimeslots;

	/**
	 * Lista de jugadores
	 */
	protected List<Player> players;
	
	/**
	 * Lista de localizaciones de juego
	 */
	protected List<Localization> localizations;
	
	/**
	 * Lista de horas de juego
	 */
	protected List<Timeslot> timeslots;
	
	/**
	 * Nombre del evento o torneo al que corresponde el horario
	 */
	protected String name;
	
	/**
	 * Devuelve la matriz bidimensional con la representación del horario
	 * 
	 * @return matriz bidimensional que representa el horario
	 */
	public ScheduleValue[][] getScheduleValues() {
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
	 * @param player jugador del que se quieren obtener los partidos
	 * @return lista de partidos, <code>null</code> si la lista de partidos aún no ha sido calculada
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
	 * @param players jugadores de los que se quieren obtener los partidos
	 * @return lista de partidos, <code>null</code> si la lista de partidos aún no ha sido calculada
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
	 * @param localization localización de juego de la que se quiere obtener la lista de partidos
	 * @return lista de partidos, <code>null</code> si la lista de partidos aún no ha sido calculada
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
	 * @param localizations localizaciones para las que se quiere obtener la lista de partidos
	 * @return lista de partidos, <code>null</code> si la lista de partidos aún no ha sido calculada
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
	 * @param timeslot hora de comienzo para la que se quiere obtener la lista de partidos
	 * @return lista de partidos, <code>null</code> si la lista de partidos aún no ha sido calculada
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
	 * @param timeslot hora de final para la que se quiere obtener la lista de partidos
	 * @return lista de partidos, <code>null</code> si la lista de partidos aún no ha sido calculada
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
	 * @param end timeslot final
	 * @return lista de partidos, <code>null</code> si la lista de partidos aún no ha sido calculada
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
	 * @param timeslot hora para la que se quieren buscar partidos que discurren sobre ella. No <code>null</code>
	 * @return lista de partidos, <code>null</code> si la lista de partidos aún no ha sido calculada
	 */
	public List<Match> getMatchesByTimeslot(Timeslot timeslot) {
		if (timeslot == null)
			throw new IllegalArgumentException("Timeslot cannot be null");
		
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
	 * Cadena que representa un horario
	 * 
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
