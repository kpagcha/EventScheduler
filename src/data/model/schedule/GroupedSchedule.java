package data.model.schedule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import data.model.schedule.data.GroupedScheduleValue;
import data.model.schedule.data.Match;
import data.model.tournament.Tournament;
import data.model.tournament.event.Event;
import data.model.tournament.event.entity.Localization;
import data.model.tournament.event.entity.Player;
import data.model.tournament.event.entity.timeslot.Timeslot;

/**
 * Un horario agrupado por localizaciones de juego y horas
 *
 */
public class GroupedSchedule {
	/**
	 * Horario agrupado
	 */
	private final GroupedScheduleValue[][] groupedSchedule;
	
	/**
	 * Nombre del torneo o evento al que se refiere
	 */
	private final String name;
	
	/**
	 * Jugadores
	 */
	private final List<Player> players;
	
	/**
	 * Localizaciones de juego
	 */
	private final List<Localization> localizations;
	
	/**
	 * Horas de juego
	 */
	private final List<Timeslot> timeslots;
	
	/**
	 * Partidos que se juegan en el horario
	 */
	private final List<Match> matches;
	
	/**
	 * Número de timeslots
	 */
	private int occupation = -1;
	
	/**
	 * Número de timeslots disponibles donde asignar partidos
	 */
	private int availableTimeslots = -1;
	
	/**
	 * Construye el horario agrupado de un evento, teniendo en cuenta los partidos del horario, los breaks
	 * y la indisponibilidad de pistas
	 * 
	 * @param event
	 * @param matches
	 */
	public GroupedSchedule(Event event, List<Match> matches) {
		name = event.getName();
		players = event.getPlayers();
		localizations = event.getLocalizations();
		timeslots = event.getTimeslots();
		this.matches = matches;
		
		groupedSchedule = new GroupedScheduleValue[localizations.size()][timeslots.size()];
		
		List<Timeslot> breaks = event.getBreaks();
		Map<Localization, Set<Timeslot>> unavailableLocalizations = event.getUnavailableLocalizations();
		
		for (int i = 0; i < localizations.size(); i++)
			for (int j = 0; j < timeslots.size(); j++)
				groupedSchedule[i][j] = new GroupedScheduleValue(GroupedScheduleValue.FREE);
		
		for (Timeslot breakTimeslot : breaks)
			for (int i = 0; i < localizations.size(); i++)
				groupedSchedule[i][timeslots.indexOf(breakTimeslot)] = new GroupedScheduleValue(GroupedScheduleValue.UNAVAILABLE);
		
		for (Localization localization : unavailableLocalizations.keySet()) {
			Set<Timeslot> localizationTimeslots = unavailableLocalizations.get(localization);
			int c = localizations.indexOf(localization);
			for (Timeslot timeslot : localizationTimeslots)
				groupedSchedule[c][timeslots.indexOf(timeslot)] = new GroupedScheduleValue(GroupedScheduleValue.UNAVAILABLE);
		}
		
		int matchDuration = event.getTimeslotsPerMatch();
		for (Match match : matches) {
			int c = localizations.indexOf(match.getLocalization());
			int t = timeslots.indexOf(match.getStartTimeslot());
			
			List<Player> matchPlayers = match.getPlayers();
			List<Integer> playersIndices = new ArrayList<Integer>(players.size());
			for (Player player : matchPlayers)
				playersIndices.add(players.indexOf(player));
				
			groupedSchedule[c][t] = new GroupedScheduleValue(GroupedScheduleValue.OCCUPIED, playersIndices);
			 
			if (matchDuration > 1)
				for (int i = 1; i < matchDuration; i++)
					groupedSchedule[c][i + t] = new GroupedScheduleValue(GroupedScheduleValue.CONTINUATION);
		}
	}
	
	public GroupedSchedule(Tournament tournament, List<Match> matches) {
		name = tournament.getName();
		players = tournament.getAllPlayers();
		localizations = tournament.getAllLocalizations();
		timeslots = tournament.getAllTimeslots();
		this.matches = matches;
		
		groupedSchedule = new GroupedScheduleValue[localizations.size()][timeslots.size()];
		
		int nTimeslots = timeslots.size();
		int nLocalization = localizations.size();
		
		// Al principio se marca todo el horario como libre
		for (int i = 0; i < nLocalization; i++)
			for (int j = 0; j < nTimeslots; j++)
				groupedSchedule[i][j] = new GroupedScheduleValue(GroupedScheduleValue.FREE);
		
		List<Event> events = tournament.getEvents();
		
		Map<Event, List<Timeslot>> breaks = new HashMap<>();
		Map<Event, Map<Localization, Set<Timeslot>>> unavailableLocalizations = new HashMap<>();
		
		for (Event event : events) {
			if (event.hasBreaks()) {
				List<Timeslot> eventBreaks = event.getBreaks();
				
				// Se marcan las pistas a las horas de break como limitadas
				for (Timeslot timeslot : eventBreaks) {
					int t = timeslots.indexOf(timeslot);
					for (int c = 0; c < nLocalization; c++)
						groupedSchedule[c][t] = new GroupedScheduleValue(GroupedScheduleValue.LIMITED);
				}	
				breaks.put(event, eventBreaks);
			}
			
			if (event.hasUnavailableLocalizations()) {
				Map<Localization, Set<Timeslot>> eventUnavailableLocalizations = event.getUnavailableLocalizations();
				
				// Se marcan las pistas a las horas no disponibles como limitadas
				for (Localization localization : eventUnavailableLocalizations.keySet()) {
					int c = localizations.indexOf(localization);
					Set<Timeslot> unavailableLocalizationTimeslots = eventUnavailableLocalizations.get(localization);
					
					for (Timeslot timeslot : unavailableLocalizationTimeslots)
						groupedSchedule[c][timeslots.indexOf(timeslot)] = new GroupedScheduleValue(GroupedScheduleValue.LIMITED);
				}
				unavailableLocalizations.put(event, eventUnavailableLocalizations);
			}
		}
		
		for (int t = 0; t < nTimeslots; t++) {
			Timeslot timeslot = timeslots.get(t);
			
			for (int c = 0; c < nLocalization; c++) {
				boolean all = true;
				Localization localization = localizations.get(c);
				
				// Si todas las categorías tienen un break a la hora_t, se marca como no disponible
				for (Event event : events) {
					if (!event.getLocalizations().contains(localization) || !event.getTimeslots().contains(timeslot) || !breaks.containsKey(event) ||
						!breaks.get(event).contains(timeslot)) {
						all = false;
						break;
					}
				}
				if (all)
					groupedSchedule[c][t] = new GroupedScheduleValue(GroupedScheduleValue.UNAVAILABLE);
			}
		}
		
		// Si todos los eventos tienen la pista no disponible a la misma hora se marca como no disponible
		for (Event e1 : events) {
			if (unavailableLocalizations.containsKey(e1)) {
				Map<Localization, Set<Timeslot>> e1UnavailableLocalizations = unavailableLocalizations.get(e1);
				
				for (Localization e1Localization : e1UnavailableLocalizations.keySet()) {
					int c = localizations.indexOf(e1Localization);
					Set<Timeslot> e1UnavailableTimeslots = e1UnavailableLocalizations.get(e1Localization);
					
					for (Timeslot e1Timeslot : e1UnavailableTimeslots) {
						boolean all = true;
						int t = timeslots.indexOf(e1Timeslot);
						
						for (Event e2 : events) {
							if (!e2.equals(e1) && unavailableLocalizations.containsKey(e2)) {
								if (!e2.getLocalizations().contains(e1Localization) || ! e2.getPlayersAtTimeslots().containsKey(e1Timeslot))
									continue;
								
								if (!(unavailableLocalizations.get(e2).containsKey(e1Localization) && 
										unavailableLocalizations.get(e2).get(e1Localization).contains(e1Timeslot))) {
									all = false;
									break;
								}
							}
						}
						
						if (all)
							groupedSchedule[c][t] = new GroupedScheduleValue(GroupedScheduleValue.UNAVAILABLE);
					}
				}
			}	
		}
		
		// Se añaden los partidos al horario
		for (Match match : matches) {
			int c = localizations.indexOf(match.getLocalization());
			int t = timeslots.indexOf(match.getStartTimeslot());
			
			List<Player> matchPlayers = match.getPlayers();
			List<Integer> playersIndices = new ArrayList<Integer>(players.size());
			for (Player player : matchPlayers)
				playersIndices.add(players.indexOf(player));
				
			groupedSchedule[c][t] = new GroupedScheduleValue(GroupedScheduleValue.OCCUPIED, playersIndices);
			
			int matchDuration = match.getDuration();
			if (matchDuration > 1)
				for (int i = 1; i < matchDuration; i++)
					groupedSchedule[c][i + t] = new GroupedScheduleValue(GroupedScheduleValue.CONTINUATION);
		}
	}
	
	/**
	 * Calcula el número total de timeslots del horario
	 * @return un valor mayor que 0
	 */
	public int getTotalTimeslots() {
		return localizations.size() * timeslots.size();
	}
	
	public int getAvailableTimeslots() {
		if (availableTimeslots < 0) {
			for (int c = 0; c < localizations.size(); c++) {
				for (int t = 0; t < timeslots.size(); t++) {
					GroupedScheduleValue val = groupedSchedule[c][t];
					if (val.isOccupied() || val.isContinuation() || val.isFree())
						availableTimeslots++;
				}
			}
		}
		return availableTimeslots;
	}
	
	public int getOccupation() {
		if (occupation < 0) {
			for (int c = 0; c < localizations.size(); c++) {
				for (int t = 0; t < timeslots.size(); t++) {
					GroupedScheduleValue val = groupedSchedule[c][t];
					if (val.isOccupied() || val.isContinuation())
						occupation++;
				}
			}
		}
		return occupation;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder(name);
		
		sb.append(String.format("\n\n%8s", " "));
		
		int maxPlayersPerMatch = 0;
		for (Match match : matches) {
			int nPlayers = match.getPlayers().size();
			if (nPlayers > maxPlayersPerMatch)
				maxPlayersPerMatch = nPlayers;
		}
		
		int padding = maxPlayersPerMatch * 2 + 4;
		int nTimeslots = groupedSchedule[0].length;
		int nLocalizations = groupedSchedule.length;
		
		for (int t = 0; t < nTimeslots; t++)
			sb.append(String.format("%" + padding + "s", "t" + t));
		sb.append("\n");
		
		for (int c = 0; c < nLocalizations; c++) {
			sb.append(String.format("%8s", localizations.get(c)));
			for (int t = 0; t < nTimeslots; t++) {
				sb.append(String.format("%" + padding + "s", groupedSchedule[c][t]));
			}
			sb.append("\n");
		}
		
		return sb.toString();
	}
}
