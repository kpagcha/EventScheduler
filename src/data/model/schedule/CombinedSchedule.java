package data.model.schedule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import data.model.schedule.data.Match;
import data.model.schedule.data.ScheduleValue;
import data.model.tournament.Tournament;
import data.model.tournament.event.Event;
import data.model.tournament.event.entity.Player;
import data.model.tournament.event.entity.timeslot.Timeslot;

public class CombinedSchedule extends Schedule {
	/**
	 * Torneo al que el horario combinado pertenece
	 */
	private Tournament tournament;
	
	/**
	 * Construye un horario combinado a partir de los horarios de cada categoría del
	 * torneo
	 * @param tournament torneo al que pertenece el horario que se va a construir
	 */
	public CombinedSchedule(Tournament tournament) {
		this.tournament = tournament;
		
		List<EventSchedule> schedules = tournament.getSchedules();
		
		if (schedules == null)
			throw new IllegalStateException("Tournament schedule not calculated yet.");
		
		name = tournament.getName();
		
		players = tournament.getAllPlayers();
		localizations = tournament.getAllLocalizations();
		timeslots = tournament.getAllTimeslots();
		
		nPlayers = players.size();
		nLocalizations = localizations.size();
		nTimeslots = timeslots.size();
		
		schedule = new ScheduleValue[nPlayers][nTimeslots];
		
		for (int p = 0; p < nPlayers; p++)
			for (int t = 0; t < nTimeslots; t++)
				schedule[p][t] = new ScheduleValue(ScheduleValue.NOT_IN_DOMAIN);
		
		for (int s = 0; s < schedules.size(); s++) {
			ScheduleValue[][] eventSchedule = schedules.get(s).getSchedule();
			
			Event event = schedules.get(s).getEvent();
			
			int nPlayers = event.getPlayers().size();
			int nTimeslots = event.getTimeslots().size();
			
			for (int p = 0; p < nPlayers; p++) {
				for (int t = 0; t < nTimeslots; t++) {
					Player player = event.getPlayers().get(p);
					Timeslot timeslot = event.getTimeslots().get(t);
					
					int playerIndex = players.indexOf(player);
					int timeslotIndex = timeslots.indexOf(timeslot);
					
					// Si no hay ya una pista marcada sobre la hora_t para el jugador_p (esto evita sobreescribir valores
					// de pistas ya escritos sobre el horario)
					if (!schedule[playerIndex][timeslotIndex].isOccupied()) {
						if (eventSchedule[p][t].isOccupied())
							schedule[playerIndex][timeslotIndex] = eventSchedule[p][t];
						else if (!schedule[playerIndex][timeslotIndex].isLimited())
							schedule[playerIndex][timeslotIndex] = eventSchedule[p][t];
					}		
				}
			}
		}
	}
	
	/**
	 * Construye los partidos a partir del horario combinado
	 */
	public void calculateMatches() {	
		matches = new ArrayList<Match>(tournament.getNumberOfMatches());
		for (EventSchedule schedule : tournament.getSchedules()) {
			schedule.calculateMatches();
			List<Match> eventMatches = schedule.getMatches();
			
			for (Match match : eventMatches)
				matches.add(match);
		}
		
		Collections.sort(matches, new Comparator<Match>() {
			public int compare(Match o1, Match o2) {
				return Timeslot.compare(o2.getStartTimeslot(), o1.getStartTimeslot());
			}
		});
	}
	
}
