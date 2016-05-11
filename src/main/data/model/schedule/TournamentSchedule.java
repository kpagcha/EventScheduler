package data.model.schedule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import data.model.schedule.data.Match;
import data.model.schedule.data.ScheduleValue;
import data.model.tournament.Tournament;
import data.model.tournament.event.Event;
import data.model.tournament.event.entity.Localization;
import data.model.tournament.event.entity.Player;
import data.model.tournament.event.entity.timeslot.Timeslot;

/**
 * Horario de un torneo formado por la combinación de los horarios de cada categoría que lo compone
 *
 */
public class TournamentSchedule extends Schedule {
	/**
	 * Torneo al que el horario combinado pertenece
	 */
	private Tournament tournament;
	
	/**
	 * Construye un horario combinado a partir de los horarios de cada categoría del
	 * torneo
	 * @param tournament torneo al que pertenece el horario que se va a construir
	 */
	public TournamentSchedule(Tournament tournament) {
		if (tournament == null)
			throw new IllegalArgumentException("Tournament cannot be null");
		
		this.tournament = tournament;
		
		Map<Event, EventSchedule> schedules = tournament.getCurrentSchedules();
		
		if (schedules == null)
			throw new IllegalStateException("Tournament schedules not calculated");
		
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
		
		for (Event event : tournament.getEvents()) {
			ScheduleValue[][] eventSchedule = schedules.get(event).getScheduleValues();
			
			int nPlayers = event.getPlayers().size();
			int nTimeslots = event.getTimeslots().size();
			
			List<Localization> eventLocalizations = event.getLocalizations();
			
			for (int p = 0; p < nPlayers; p++) {
				for (int t = 0; t < nTimeslots; t++) {
					Player player = event.getPlayers().get(p);
					Timeslot timeslot = event.getTimeslots().get(t);
					
					int playerIndex = players.indexOf(player);
					int timeslotIndex = timeslots.indexOf(timeslot);
					
					// Si no hay ya una pista marcada sobre la hora_t para el jugador_p (esto evita sobreescribir valores
					// de pistas ya escritos sobre el horario)
					if (!schedule[playerIndex][timeslotIndex].isOccupied()) {
						if (eventSchedule[p][t].isOccupied()) {
							schedule[playerIndex][timeslotIndex] = new ScheduleValue(
								ScheduleValue.OCCUPIED,
								localizations.indexOf(eventLocalizations.get(eventSchedule[p][t].getLocalization()))
							);
						} else if (!schedule[playerIndex][timeslotIndex].isLimited())
							schedule[playerIndex][timeslotIndex] = eventSchedule[p][t];
					}		
				}
			}
		}
		
		calculateMatches();
	}
	
	/**
	 * Construye los partidos a partir del horario combinado
	 */
	private void calculateMatches() {	
		matches = new ArrayList<Match>(tournament.getNumberOfMatches());
		for (EventSchedule schedule : tournament.getCurrentSchedules().values()) {
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
