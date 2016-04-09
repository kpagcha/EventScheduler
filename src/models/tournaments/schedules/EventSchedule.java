package models.tournaments.schedules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.chocosolver.solver.variables.IntVar;

import models.tournaments.events.Event;
import models.tournaments.events.entities.Player;
import models.tournaments.events.entities.Timeslot;
import models.tournaments.schedules.data.Match;

public class EventSchedule extends Schedule {
	/**
	 * Evento al que pertenece el horario
	 */
	private Event event;
	
	/**
	 * Construye un horario final con las pistas donde juega cada
	 * jugador a cada hora, a partir de la matriz 3D de Choco con la solución
	 * 
	 * @param event evento al que pertenece el horario que se va a construir
	 * @param x 	array de IntVar de tres dimensiones con los valores de la solución calculada por el EventSolver
	 */
	public EventSchedule(Event event, IntVar[][][] x) {
		this.event = event;
		
		name = event.getName();
		
		players = Arrays.asList(event.getPlayers());
		localizations = Arrays.asList(event.getLocalizations());
		timeslots = Arrays.asList(event.getTimeslots());
		
		nCourts = event.getNumberOfLocalizations();
		nPlayers = event.getNumberOfPlayers();
		nTimeslots = event.getNumberOfTimeslots();
		
		schedule = new int[nPlayers][nTimeslots];
		for (int p = 0; p < nPlayers; p++) {
			for (int t = 0; t < nTimeslots; t++) {
				Timeslot timeslot = event.getTimeslotAt(t);
				
				if (event.isBreak(timeslot))
					schedule[p][t] = -3;
				else if (event.isUnavailable(event.getPlayerAt(p), timeslot)) {
					schedule[p][t] = -2;
				} else {
					schedule[p][t] = -1;
					
					boolean matchInCourt = false;
					for (int c = 0; c < nCourts; c++) {
						if (x[p][c][t].getValue() == 1) {
							schedule[p][t] = c;
							matchInCourt = true;
							break;
						}
					}
					
					if (!matchInCourt) {
						for (int c = 0; c < nCourts; c++) {
							if (event.isDiscarded(event.getLocalizationAt(c), timeslot)) {
								schedule[p][t] = -5;
								break;
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * Calcula los partidos que componen el horario 
	 */
	public void calculateMatches() {
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
		
		matches = new Match[(event.getNumberOfPlayers() / event.getPlayersPerMatch()) * event.getMatchesPerPlayer()];
		
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
	}
	
}
