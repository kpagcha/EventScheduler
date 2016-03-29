package models.schedules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import models.Event;
import models.Match;
import models.Player;
import models.Timeslot;
import models.Tournament;

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
		
		EventSchedule[] schedules = tournament.getSchedules();
		
		if (schedules == null)
			throw new IllegalStateException("Tournament schedule not calculated yet.");
		
		name = tournament.getName();
		
		players = tournament.getAllPlayers();
		localizations = tournament.getAllLocalizations();
		timeslots = tournament.getAllTimeslots();
		
		nPlayers = players.size();
		nCourts = localizations.size();
		nTimeslots = timeslots.size();
		
		schedule = new int[nPlayers][nTimeslots];
		
		// Marcar horario como no inicializado (-4 significa valor aún por asignar)
		for (int p = 0; p < nPlayers; p++)
			for (int t = 0; t < nTimeslots; t++)
				schedule[p][t] = -4;
		
		Event[] events = tournament.getEvents();
		
		for (int s = 0; s < schedules.length; s++) {
			int[][] eventSchedule = schedules[s].getSchedule();
			
			int nPlayers = events[s].getNumberOfPlayers();
			int nTimeslots = events[s].getNumberOfTimeslots();
			
			for (int p = 0; p < nPlayers; p++) {
				for (int t = 0; t < nTimeslots; t++) {
					Player player = events[s].getPlayerAt(p);
					Timeslot timeslot = events[s].getTimeslotAt(t);
					
					int playerIndex = players.indexOf(player);
					int timeslotIndex = timeslots.indexOf(timeslot);
					
					// Si no hay ya una pista marcada sobre la hora_t para el jugador_p (esto evita sobreescribir valores
					// de pistas ya escritos sobre el horario)
					if (schedule[playerIndex][timeslotIndex] < 0)
						schedule[playerIndex][timeslotIndex] = eventSchedule[p][t];
				}
			}
		}
	}
	
	public void calculateMatches() {
		EventSchedule[] eventSchedules = tournament.getSchedules();
		
		List<Match> allMatches = new ArrayList<Match>(tournament.getNumberOfMatches());
		for (int i = 0; i < eventSchedules.length; i++) {
			eventSchedules[i].calculateMatches();
			Match[] eventMatches = eventSchedules[i].getMatches();
			
			for (int match = 0; match < eventMatches.length; match++)
				allMatches.add(eventMatches[match]);
		}
		
		Collections.sort(allMatches, new Comparator<Match>() {
			public int compare(Match o1, Match o2) {
				return Integer.compare(o1.getTimeslot().getLowerBound(), o2.getTimeslot().getLowerBound());
			}
		});
		
		matches = allMatches.toArray(new Match[allMatches.size()]);
	}
	
}
