package models.tournaments.schedules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.chocosolver.solver.variables.IntVar;

import models.tournaments.events.Event;
import models.tournaments.events.entities.Player;
import models.tournaments.events.entities.Team;
import models.tournaments.events.entities.Timeslot;
import models.tournaments.schedules.data.Match;
import models.tournaments.schedules.data.ScheduleValue;

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
		
		schedule = new ScheduleValue[nPlayers][nTimeslots];
		for (int p = 0; p < nPlayers; p++) {
			for (int t = 0; t < nTimeslots; t++) {
				Timeslot timeslot = event.getTimeslotAt(t);
				
				if (event.isBreak(timeslot))
					schedule[p][t] = new ScheduleValue(ScheduleValue.BREAK);
				else if (event.isUnavailable(event.getPlayerAt(p), timeslot)) {
					schedule[p][t] = new ScheduleValue(ScheduleValue.UNAVAILABLE);
				} else {
					schedule[p][t] = new ScheduleValue(ScheduleValue.FREE);
					
					boolean matchInCourt = false;
					for (int c = 0; c < nCourts; c++) {
						if (x[p][c][t].getValue() == 1) {
							schedule[p][t] = new ScheduleValue(ScheduleValue.OCCUPIED, c);
							matchInCourt = true;
							break;
						}
					}
					
					if (!matchInCourt) {
						for (int c = 0; c < nCourts; c++) {
							if (event.isDiscarded(event.getLocalizationAt(c), timeslot)) {
								schedule[p][t] = new ScheduleValue(ScheduleValue.LIMITED);
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
		ScheduleValue[][] scheduleBeginnings = new ScheduleValue[nPlayers][nTimeslots];
		for (int p = 0; p < nPlayers; p++) {
			for (int t = 0; t < nTimeslots; t++) {
				// si se juega un partido se marcan los siguientes de su rango como libres
				if (schedule[p][t].isOccupied()) {
					scheduleBeginnings[p][t] = schedule[p][t];
					for (int i = 1; i < matchDuration; i++)
						scheduleBeginnings[p][t + i] = new ScheduleValue(ScheduleValue.FREE);
					t += matchDuration - 1;
				} else {
					scheduleBeginnings[p][t] = schedule[p][t];
				}
			}
		}
		matches = new ArrayList<Match>((event.getNumberOfPlayers() / event.getPlayersPerMatch()) * event.getMatchesPerPlayer());
		
		int nTimeslotsPerMatch = event.getMatchDuration();
		int nPlayersPerMatch = event.getPlayersPerMatch();
		for (int t = 0; t < nTimeslots; t++) {
			List<Integer> playersAlreadyMatched = new ArrayList<>();
			
			for (int thisPlayer = 0; thisPlayer < nPlayers - nPlayersPerMatch + 1; thisPlayer++) {
				if (scheduleBeginnings[thisPlayer][t].isOccupied()) {
					List<Integer> playersBelongingToMatch = new ArrayList<>();
					playersBelongingToMatch.add(thisPlayer);
					
					boolean matchCompleted = false;
					
					for (int otherPlayer = thisPlayer + 1; otherPlayer < nPlayers; otherPlayer++) {
						if (scheduleBeginnings[otherPlayer][t].isOccupied() && !playersAlreadyMatched.contains(otherPlayer)
								&& scheduleBeginnings[thisPlayer][t].equals(scheduleBeginnings[otherPlayer][t])) {
							
							playersAlreadyMatched.add(otherPlayer);
							
							playersBelongingToMatch.add(otherPlayer);
							
							if (playersBelongingToMatch.size() == nPlayersPerMatch) {
								matchCompleted = true;
								break;
							}
						}
					}
					
					if (matchCompleted || nPlayersPerMatch == 1) {
						List<Player> playersList = new ArrayList<Player>(nPlayersPerMatch);
						for (int playerIndex : playersBelongingToMatch)
							playersList.add(event.getPlayerAt(playerIndex));
						
						Match match = new Match(
							playersList,
							event.getLocalizationAt(scheduleBeginnings[thisPlayer][t].getLocalization()),
							event.getTimeslotAt(t),
							event.getTimeslotAt(t + nTimeslotsPerMatch - 1)
						);
						
						matches.add(match);

						if (event.hasTeams()) {
							List<Team> teamsInMatch = new ArrayList<Team>();
							for (Player player : playersList) {
								Team team = event.getTeamByPlayer(player);
								if (!teamsInMatch.contains(team))
									teamsInMatch.add(team);
							}
							
							match.setTeams(teamsInMatch);
						}
					}
				}
			}
		}
	}
	
}
