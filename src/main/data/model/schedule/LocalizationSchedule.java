package data.model.schedule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import data.model.schedule.value.LocalizationScheduleValue;
import data.model.schedule.value.LocalizationScheduleValueOccupied;
import data.model.tournament.Tournament;
import data.model.tournament.event.Event;
import data.model.tournament.event.domain.Localization;
import data.model.tournament.event.domain.Player;
import data.model.tournament.event.domain.timeslot.Timeslot;

/**
 * Representa un horario agrupado por localizaciones de juego y horas de juego mediante una matriz
 * bidimensional de {@link LocalizationScheduleValue}.
 *
 */
public class LocalizationSchedule extends Schedule {
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
	 * @param event evento no nulo
	 * @param matches lista de partidos a partir de los que se construye el horario
	 * 
	 * @throws IllegalArgumentException si los parámetos son <code>null</code> o si el tamaño de la lista de
	 * partidos no se corresponde por el esperado por el evento
	 */
	public LocalizationSchedule(Event event, List<Match> matches) {
		if (event == null || matches == null)
			throw new IllegalArgumentException("The parameters cannot be null");
		
		if (matches.size() != event.getNumberOfMatches())
			throw new IllegalArgumentException("The size of the list of matches (" + matches.size() + ") should be " +
				"equal to the expected number of matches for the event (" + event.getNumberOfMatches() + ")");
			
		name = event.getName();
		players = event.getPlayers();
		localizations = event.getLocalizations();
		timeslots = event.getTimeslots();
		this.matches = matches;
		
		schedule = new LocalizationScheduleValue[localizations.size()][timeslots.size()];
		
		List<Timeslot> breaks = event.getBreaks();
		Map<Localization, Set<Timeslot>> unavailableLocalizations = event.getUnavailableLocalizations();
		
		for (int i = 0; i < localizations.size(); i++)
			for (int j = 0; j < timeslots.size(); j++)
				schedule[i][j] = new LocalizationScheduleValue(LocalizationScheduleValue.FREE);
		
		for (Timeslot breakTimeslot : breaks)
			for (int i = 0; i < localizations.size(); i++)
				schedule[i][timeslots.indexOf(breakTimeslot)] = new LocalizationScheduleValue(LocalizationScheduleValue.UNAVAILABLE);
		
		for (Localization localization : unavailableLocalizations.keySet()) {
			Set<Timeslot> localizationTimeslots = unavailableLocalizations.get(localization);
			int c = localizations.indexOf(localization);
			for (Timeslot timeslot : localizationTimeslots)
				schedule[c][timeslots.indexOf(timeslot)] = new LocalizationScheduleValue(LocalizationScheduleValue.UNAVAILABLE);
		}
		
		int matchDuration = event.getTimeslotsPerMatch();
		for (Match match : matches) {
			int c = localizations.indexOf(match.getLocalization());
			int t = timeslots.indexOf(match.getStartTimeslot());
			
			List<Player> matchPlayers = match.getPlayers();
			List<Integer> playersIndices = new ArrayList<Integer>(players.size());
			for (Player player : matchPlayers)
				playersIndices.add(players.indexOf(player));
				
			schedule[c][t] = new LocalizationScheduleValueOccupied(playersIndices);
			 
			if (matchDuration > 1)
				for (int i = 1; i < matchDuration; i++)
					schedule[c][i + t] = new LocalizationScheduleValue(LocalizationScheduleValue.CONTINUATION);
		}
	}
	
	/** Construye el horario agrupado de un torneo
	 * 
	 * @param tournament torneo cuyo horario agrupado se va a construir
	 * @param matches lista de partidos a partir de la que se construye este objeto
	 * 
	 * @throws IllegalArgumentException si los parámetos son <code>null</code> o si el tamaño de la lista de
	 * partidos no se corresponde por el esperado por el torneo
	 * @throws IllegalStateException si los horarios del torneo aún no han sido calculados, o ya se han calculado
	 * todos, es decir, que el horario sea <code>null</code>
	 */
	public LocalizationSchedule(Tournament tournament) {
		if (tournament == null)
			throw new IllegalArgumentException("Tournament cannot be null");
		
		if (tournament.getSchedule() == null)
			throw new IllegalStateException("Tournament schedule not calculated");
		
		name = tournament.getName();
		players = tournament.getAllPlayers();
		localizations = tournament.getAllLocalizations();
		timeslots = tournament.getAllTimeslots();
		this.matches = tournament.getSchedule().getMatches();
		
		schedule = new LocalizationScheduleValue[localizations.size()][timeslots.size()];
		
		int nTimeslots = timeslots.size();
		int nLocalization = localizations.size();
		
		// Al principio se marca todo el horario como libre
		for (int i = 0; i < nLocalization; i++)
			for (int j = 0; j < nTimeslots; j++)
				schedule[i][j] = new LocalizationScheduleValue(LocalizationScheduleValue.FREE);
		
		List<Event> events = tournament.getEvents();
		
		Map<Event, List<Timeslot>> breaks = new HashMap<>();
		Map<Event, Map<Localization, Set<Timeslot>>> unavailableLocalizations = new HashMap<>();
		
		for (Event event : events) {
			if (event.hasBreaks()) {
				List<Timeslot> eventBreaks = event.getBreaks();
				
				// Al principio se marcan las pistas a las horas de break como limitadas
				for (Timeslot timeslot : eventBreaks) {
					int t = timeslots.indexOf(timeslot);
					for (int c = 0; c < nLocalization; c++)
						schedule[c][t] = new LocalizationScheduleValue(LocalizationScheduleValue.LIMITED);
				}	
				breaks.put(event, eventBreaks);
			}
			
			if (event.hasUnavailableLocalizations()) {
				Map<Localization, Set<Timeslot>> eventUnavailableLocalizations = event.getUnavailableLocalizations();
				
				// Al principio se marcan las pistas a las horas no disponibles como limitadas
				for (Localization localization : eventUnavailableLocalizations.keySet()) {
					int c = localizations.indexOf(localization);
					Set<Timeslot> unavailableLocalizationTimeslots = eventUnavailableLocalizations.get(localization);
					
					// Si solamente hay un evento se marca directamente como no disponible porque el bucle que se encuentra
					// más adelante solamente funciona para un torneo con más de una categoría
					for (Timeslot timeslot : unavailableLocalizationTimeslots)
						if (events.size() > 1)
							schedule[c][timeslots.indexOf(timeslot)] = new LocalizationScheduleValue(LocalizationScheduleValue.LIMITED);
						else
							schedule[c][timeslots.indexOf(timeslot)] = new LocalizationScheduleValue(LocalizationScheduleValue.UNAVAILABLE);
				}
				unavailableLocalizations.put(event, eventUnavailableLocalizations);
			}
		}
		
		// Sobreescribe con no disponibles las marcadas anteriormente como limitadas, si lo son para todas las categorías
		for (int t = 0; t < nTimeslots; t++) {
			Timeslot timeslot = timeslots.get(t);
			
			for (int c = 0; c < nLocalization; c++) {
				boolean all = true;
				Localization localization = localizations.get(c);
				
				// Si todas las categorías tienen un break a la hora_t, se marca como no disponible
				for (Event event : events) {
					if (!event.getLocalizations().contains(localization) || !event.getTimeslots().contains(timeslot) || 
							!breaks.containsKey(event) || !breaks.get(event).contains(timeslot)) {
						all = false;
						break;
					}
				}
				if (all)
					schedule[c][t] = new LocalizationScheduleValue(LocalizationScheduleValue.UNAVAILABLE);
			}
		}
		
		// Si todos los eventos tienen la pista no disponible a la misma hora se marca como no disponible, sobreescribiendo
		// las marcadas como limitadas anteriormente, si se da el caso
		for (int i = 0; i < events.size() - 1; i++) {
			Event thisEvent = events.get(i);
			
			if (unavailableLocalizations.containsKey(thisEvent)) {
				Map<Localization, Set<Timeslot>> eventUnavailableLocalizations = unavailableLocalizations.get(thisEvent);
				
				for (Localization localization : eventUnavailableLocalizations.keySet()) {
					int c = localizations.indexOf(localization);
			
					for (Timeslot unavailableLocalizationTimeslot : eventUnavailableLocalizations.get(localization)) {
						boolean all = true;
						int t = timeslots.indexOf(unavailableLocalizationTimeslot);
						
						for (int j = i + 1; j < events.size(); j++) {
							Event otherEvent = events.get(j);
							
							if (unavailableLocalizations.containsKey(otherEvent) && otherEvent.getLocalizations().contains(localization) && 
								otherEvent.getPlayersAtTimeslots().containsKey(unavailableLocalizationTimeslot) && 
									!(unavailableLocalizations.get(otherEvent).containsKey(localization) && 
										unavailableLocalizations.get(otherEvent).get(localization).contains(unavailableLocalizationTimeslot))) {
								
								all = false;
								break;
							}
						}
						
						if (all)
							schedule[c][t] = new LocalizationScheduleValue(LocalizationScheduleValue.UNAVAILABLE);
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
				
			schedule[c][t] = new LocalizationScheduleValueOccupied(playersIndices);
			
			int matchDuration = match.getDuration();
			if (matchDuration > 1)
				for (int i = 1; i < matchDuration; i++)
					schedule[c][i + t] = new LocalizationScheduleValue(LocalizationScheduleValue.CONTINUATION);
		}
	}
	
	/**
	 * Calcula el número total de <i>timeslots</i> del horario
	 * 
	 * @return el número total de horas de juego (<i>timeslots</i>), no negativo
	 */
	public int getTotalTimeslots() {
		return localizations.size() * timeslots.size();
	}
	
	/**
	 * Devuelve el número de <i>timeslots</i> disponibles, es decir, donde partidos podrían tener lugar
	 * 
	 * @return el número de <i>timeslots</i> disponibles, no negativo
	 */
	public int getAvailableTimeslots() {
		if (availableTimeslots < 0) {
			availableTimeslots = 0;
			for (int c = 0; c < localizations.size(); c++) {
				for (int t = 0; t < timeslots.size(); t++) {
					LocalizationScheduleValue val = (LocalizationScheduleValue)schedule[c][t];
					if (val.isOccupied() || val.isContinuation() || val.isFree())
						availableTimeslots++;
				}
			}
		}
		return availableTimeslots;
	}
	
	/**
	 * Devuelve el número de huecos del horario ocupados para todas las pistas
	 * 
	 * @return tasa de ocupación del horario
	 */
	public int getOccupation() {
		if (occupation < 0) {
			occupation = 0;
			for (int c = 0; c < localizations.size(); c++) {
				for (int t = 0; t < timeslots.size(); t++) {
					LocalizationScheduleValue val = (LocalizationScheduleValue)schedule[c][t];
					if (val.isOccupied() || val.isContinuation()) {
						occupation++;
					}
				}
			}
		}
		return occupation;
	}
	
	/**
	 * Devuelve el ratio de ocupación de localizaciones de juego disponibles
	 * 
	 * @return un decimal entre 0 y 1
	 */
	public double getOccupationRatio() {
		return getOccupation() / (double)getAvailableTimeslots();
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
		int nTimeslots = schedule[0].length;
		int nLocalizations = schedule.length;
		
		for (int t = 0; t < nTimeslots; t++)
			sb.append(String.format("%" + padding + "s", "t" + t));
		sb.append("\n");
		
		for (int c = 0; c < nLocalizations; c++) {
			sb.append(String.format("%8s", localizations.get(c)));
			for (int t = 0; t < nTimeslots; t++) {
				sb.append(String.format("%" + padding + "s", schedule[c][t]));
			}
			sb.append("\n");
		}
		
		return sb.toString();
	}
}
