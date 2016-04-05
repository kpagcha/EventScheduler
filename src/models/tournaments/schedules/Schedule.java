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

public abstract class Schedule {
	
	/**
	 * Arry de enteros de dos dimensiones que representa el horario con valores -3..n-1 donde
	 *  	-3:    el timeslot corresponde a un período en el que no se juega
	 * 	    -2:	   el jugador no está disponible en esa hora
	 * 	    -1:    el jugador no juega a esa hora
	 * 	0..n-1:    el jugador juega en esa la pista con ese id
	 */
	protected int[][] schedule;
	
	protected Match[] matches;

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
	public int[][] getSchedule() {
		return schedule;
	}
	
	/**
	 * @return los partidos que componen este horario
	 */
	public Match[] getMatches() {
		return matches;
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
				if (schedule[p][t] >= 0) {
					Timeslot timeslot = timeslots.get(t);
					Player player = players.get(p);
					
					Localization localization = localizations.get(schedule[p][t]);
					
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
				} else if (timeslot.getIsBreak()) {
					strValue = "*";
				} else {
					strValue = "-";
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
	 * @param scheduleArray		array que representa un horario
	 * @return representación en forma de cadena del horario
	 */
	private String scheduleToString(int[][] scheduleArray) {
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
				sb.append(String.format("%4s", getStringValue(scheduleArray[p][t])));
			sb.append("\n");
		}
		
		return sb.toString();
	}
	
	/**
	 * Método para ayudar al formateo del horario
	 * 
	 * @param  matchVal		valor del elemento del horario
	 * @return string		cadena con la representación del valor del partido donde
	 *     -:    el jugador no juega a esa hora
	 *     ~:    el jugador no está disponible en esa hora
	 *     *:    el timeslot corresponde a un período en el que no se juega (break)
	 *     x:    el timeslot no pertenece al rango de timeslots del evento
	 *     ¬:    alguna pista ha sido descartada en este timeslot
	 *     n:    el jugador juega en la pista con índice n
	 */
	private String getStringValue(int scheduleVal) {
		String match = String.valueOf(scheduleVal);
		
		switch (scheduleVal) {
			case -1: match = "-"; break;
			case -2: match = "~"; break;
			case -3: match = "*"; break;
			case -4: match = "x"; break;
			case -5: match = "¬"; break;
		}

		return match;
	}
}
