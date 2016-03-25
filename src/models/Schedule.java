package models;

import java.util.ArrayList;
import java.util.List;

import org.chocosolver.solver.variables.IntVar;

public class Schedule {
	/**
	 * Evento al que pertenece el horario
	 */
	private Event event;
	
	/**
	 * Arry de enteros de dos dimensiones que represnta el horario con valores -3..n-1 donde
	 *  	-3:    el timeslot corresponde a un período en el que no se juega
	 * 	    -2:	   el jugador no está disponible en esa hora
	 * 	    -1:    el jugador no juega a esa hora
	 * 	0..n-1:    el jugador juega en esa la pista con ese id
	 */
	private int[][] schedule;
	
	private int nCourts;
	private int nPlayers;
	private int nTimeslots;
	
	/**
	 * Construye un horario final con las pistas donde juega cada
	 * jugador a cada hora, a partir de la matriz 3D de Choco con la solución
	 */
	public Schedule(Event event, IntVar[][][] x) {
		this.event = event;
		
		nCourts = event.getNumberOfLocalizations();
		nPlayers = event.getNumberOfPlayers();
		nTimeslots = event.getNumberOfTimeslots();
		
		schedule = new int[nPlayers][nTimeslots];
		for (int p = 0; p < nPlayers; p++) {
			for (int t = 0; t < nTimeslots; t++) {
				if (event.getTimeslotAt(t).getIsBreak())
					schedule[p][t] = -3;
				else if (event.isUnavailable(event.getPlayerAt(p), event.getTimeslotAt(t))) {
					schedule[p][t] = -2;
				} else {
					schedule[p][t] = -1;
					
					for (int c = 0; c < nCourts; c++)
						if (x[p][c][t].getValue() == 1)
							schedule[p][t] = c;
				}
			}
		}
	}
	
	/**
	 * @return int[][]	un array de enteros de dos dimensiones
	 */
	public int[][] getSchedule() {
		return schedule;
	}
	
	/**
	 * @return Match[]	un array con los partidos que componen este horario
	 */
	public Match[] getMatches() {
		int matchDuration = event.getMatchDuration();
		
		// Horario donde solo se marcan los comienzos de partidos
		int[][] scheduleBeginnings = new int[nPlayers][nTimeslots];
		for (int p = 0; p < nPlayers; p++) {
			for (int t = 0; t < nTimeslots; t++) {
				// si se juega un partido se marcan los siguientes de su rango con -1
				if (schedule[p][t] >= 0) {
					scheduleBeginnings[p][t] = schedule[p][t];
					for (int i = 1; i < matchDuration; i++)
						scheduleBeginnings[p][t + i] = -1;
					t += matchDuration - 1;
				} else {
					scheduleBeginnings[p][t] = schedule[p][t];
				}
			}
		}
		
		Match[] matches = new Match[(event.getNumberOfPlayers() / event.getPlayersPerMatch()) * event.getMatchesPerPlayer()];
		
		int nPlayersPerMatch = event.getPlayersPerMatch();
		int matchesIndex = 0;
		for (int t = 0; t < nTimeslots; t++) {
			List<Integer> playersAlreadyMatched = new ArrayList<>();
			
			for (int thisPlayer = 0; thisPlayer < nPlayers - nPlayersPerMatch + 1; thisPlayer++) {
				if (scheduleBeginnings[thisPlayer][t] >= 0) {
					List<Integer> playersBelongingToMatch = new ArrayList<>();
					playersBelongingToMatch.add(thisPlayer);
					
					boolean matchCompleted = false;
					
					for (int otherPlayer = thisPlayer + 1; otherPlayer < nPlayers; otherPlayer++) {
						if (scheduleBeginnings[otherPlayer][t] >= 0 && !playersAlreadyMatched.contains(otherPlayer)
								&& scheduleBeginnings[thisPlayer][t] == scheduleBeginnings[otherPlayer][t]) {
							
							playersAlreadyMatched.add(otherPlayer);
							
							playersBelongingToMatch.add(otherPlayer);
							
							if (playersBelongingToMatch.size() == nPlayersPerMatch) {
								matchCompleted = true;
								break;
							}
						}
					}
					
					if (matchCompleted || nPlayersPerMatch == 1) {
						Player[] playersArray = new Player[nPlayersPerMatch];
						int i = 0;
						for (int playerIndex : playersBelongingToMatch)
							playersArray[i++] = event.getPlayerAt(playerIndex);
						
						matches[matchesIndex++] = new Match(
								playersArray,
								event.getLocalizationAt(scheduleBeginnings[thisPlayer][t]),
								event.getTimeslotAt(t),
								event.getMatchDuration()
						);
					}
				}
			}
		}
		
		return matches;
	}
	
	public String toString() {
		return scheduleToString(schedule);
	}
	
	/**
	 * @param scheduleArray		array que representa un horario
	 * @return representación en forma de cadena del horario
	 */
	private String scheduleToString(int[][] scheduleArray) {
		StringBuilder sb = new StringBuilder(event.toString());
		
		sb.append(String.format("\n\n%8s", " "));
		for (int t = 0; t < nTimeslots; t++)
			sb.append(String.format("%4s", "t" + event.getTimeslotAt(t).getLowerBoundStr()));
		sb.append("\n");
		
		Player[] players = event.getPlayers();
		for (int p = 0; p < nPlayers; p++) {
			String playerStr = players[p].toString();
			if (playerStr.length() > 8)
				playerStr = playerStr.substring(0, 8);
				
			sb.append(String.format("%8s", playerStr));
			for (int t = 0; t < nTimeslots; t++)
				sb.append(String.format("%4s", getMatchStringValue(scheduleArray[p][t])));
			sb.append("\n");
		}
		
		return sb.toString();
	}
	
	/**
	 * Método para ayudar al formateo del horario
	 * 
	 * @param  matchVal		valor del partido
	 * @return string		cadena con la representación del valor del partido donde
	 *     *:    el timeslot corresponde a un período en el que no se juega
	 *     ~:	 el jugador no está disponible en esa hora
	 *     -:    el jugador no juega a esa hora
	 *  1..n:    el jugador juega en esa pista (n es la última pista)
	 */
	private String getMatchStringValue(int matchVal) {
		String match = String.valueOf(matchVal);
		if (matchVal == -3)
			match = "*";
		else if (matchVal == -2)
			match = "~";
		else if (matchVal == -1)
			match = "-";
		return match;
	}
}
