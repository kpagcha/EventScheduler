package data.model.tournament;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import data.model.schedule.CombinedSchedule;
import data.model.schedule.EventSchedule;
import data.model.schedule.data.Match;
import data.model.tournament.event.Event;
import data.model.tournament.event.entity.Localization;
import data.model.tournament.event.entity.Player;
import data.model.tournament.event.entity.timeslot.Timeslot;
import solver.TournamentSolver;

public class Tournament {
	/**
	 * Nombre del torneo
	 */
	private String name;
	
	/**
	 * Categor�as que componen el torneo 
	 */
	private List<Event> events;
	
	/**
	 * Todos los jugadores que participan en el torneo. No se repiten los presentes en m�ltiples categor�as
	 */
	private List<Player> allPlayers;
	
	/**
	 * Todos los terrenos de juego en los que se desarrolla en el torneo. No se repiten los presentes en m�ltiples categor�as
	 */
	private List<Localization> allLocalizations;
	
	/**
	 * Todos los timeslots en los que discurre el torneo. No se repiten los presentes en m�ltiples categor�as
	 */
	private List<Timeslot> allTimeslots;

	/**
	 * Horarios para cada categor�a
	 */
	private List<EventSchedule> currentSchedules;
	
	/**
	 * Horario del torneo que combina los horarios de todas las categor�as en uno solo
	 */
	private CombinedSchedule schedule;
	
	/**
	 * El solver que obtendr� los horarios de cada categor�a el torneo
	 */
	private TournamentSolver solver;
	
	/**
	 * Construye del torneo
	 * 
	 * @param name       nombre del torneo
	 * @param categories categor�as que componen el torneo
	 */
	public Tournament(String name, List<Event> categories) {
		this.name = name;
		events = categories;
		
		allPlayers = new ArrayList<Player>();
		for (Event event : events)
			for (Player player : event.getPlayers())
				if (!allPlayers.contains(player))
					allPlayers.add(player);
		

		allTimeslots = new ArrayList<Timeslot>();
		for (Event event : events)
			for (Timeslot timeslot : event.getTimeslots())
				if (!allTimeslots.contains(timeslot))
					allTimeslots.add(timeslot);
		
		allLocalizations = new ArrayList<Localization>();
		for (Event event : events)
			for (Localization localization : event.getLocalizations())
				if (!allLocalizations.contains(localization))
					allLocalizations.add(localization);
		
		solver = new TournamentSolver(this);
	}
	
	/**
	 * Construye un torneo
	 * @param name       nombre
	 * @param categories categor�as que componen el torneo
	 */
	public Tournament(String name, Event... categories) {
		this(name, new ArrayList<Event>(Arrays.asList(categories)));
	}
	
	/**
	 * Comienza el proceso de resoluci�n para calcular un primer horario
	 * 
	 * @return true si se ha encontrado una soluci�n, false si ocurre lo contrario
	 */
	public boolean solve() {
		boolean solved = solver.execute();
		
		currentSchedules = solver.getSchedules();
		schedule = null;
		
		return solved;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public List<Event> getEvents() {
		return events;
	}
	
	public List<Player> getAllPlayers() {
		return allPlayers;
	}

	public List<Timeslot> getAllTimeslots() {
		return allTimeslots;
	}
	
	public List<Localization> getAllLocalizations() {
		return allLocalizations;
	}
	
	/**
	 * Agrupa en un diccionario las categor�as del torneo por el n�mero de jugadores por partido
	 * 
	 * @return un diccionario donde la clave es el n�mero de jugadores por partido y el valor, la lista
	 * de categor�as que definen dicho n�mero de jugadores por partido
	 */
	public Map<Integer, List<Event>> groupEventsByNumberOfPlayersPerMatch() {
		Map<Integer, List<Event>> eventsByNumberOfPlayersPerMatch = new HashMap<Integer, List<Event>>();
		
		for (Event event : events) {
			int n = event.getPlayersPerMatch();
			if (eventsByNumberOfPlayersPerMatch.containsKey(n))
				eventsByNumberOfPlayersPerMatch.get(n).add(event);
			else
				eventsByNumberOfPlayersPerMatch.put(n, new ArrayList<Event>(Arrays.asList(new Event[]{ event })));
		}
		
		return eventsByNumberOfPlayersPerMatch;
	}
	
	/**
	 * Actualiza el valor de los horarios con la nueva soluci�n combinada. Si se ha llegado
	 * a la �ltima soluci�n se establece el valor de los horarios a null. Adem�s, se resetea el valor
	 * del horario combinado
	 * 
	 * @return true si se han actualizado los horarios con una nueva soluci�n, y false si
	 * se ha alcanzado la �ltima soluci�n
	 */
	public boolean nextSchedules() {
		currentSchedules = solver.getSchedules();
		schedule = null;
		return currentSchedules != null;
	}
	
	/**
	 * Devuelve los horarios de cada categor�a con el valor actual. Si no se ha actualizado el valor llamando
	 * al m�todo nextSchedules o si el solver ha alcanzado la �ltima soluci�n y se ha llamado seguidamente a
	 * nextSchedules, devuelve null
	 * 
	 * @return los horarios de cada categor�a
	 */
	public List<EventSchedule> getSchedules() {
		return currentSchedules;
	}
	
	/**
	 * Devuelve un �nico horario combinado del torneo que include todos los jugadores, todas las localizaciones de
	 * juego y todas las horas o timeslots de los que compone
	 * 
	 * @return horario combinado del torneo
	 */
	public CombinedSchedule getSchedule() {
		if (schedule == null)
			schedule = new CombinedSchedule(this);
		return schedule;
	}
	
	/**
	 * A�ade timeslots no disponibles para el jugador en todas las categor�as donde participe
	 * 
	 * @param player
	 * @param timeslots
	 */
	public void addPlayerUnavailableTimeslots(Player player, Set<Timeslot> timeslots) {	
		for (Event event : events)
			if (event.getPlayers().contains(player))
				event.addUnavailablePlayer(player, timeslots);
	}
	
	/**
	 * A�ade un timeslot no disponible para el jugador en todas las categor�as donde participe
	 * 
	 * @param player
	 * @param timeslot
	 */
	public void addPlayerUnavailableTimeslot(Player player, Timeslot timeslot) {
		for (Event event : events)
			if (event.getPlayers().contains(player))
				event.addUnavailablePlayer(player, timeslot);
	}
	
	/**
	 * Si el jugador no est� disponible a la hora timeslot, se elimina de la lista y vuelve a estar disponible a esa hora,
	 * para todas las categor�as
	 * 
	 * @param player
	 * @param timeslot
	 */
	public void removePlayerUnavailableTimeslot(Player player, Timeslot timeslot) {
		for (Event event : events)
			if (event.getPlayers().contains(player))
				event.removePlayerUnavailableTimeslot(player, timeslot);
	}
	
	/**
	 * Marca los timeslots de la lista como breaks para todas las categor�as
	 * 
	 * @param breakTimeslots
	 */
	public void addBreaks(List<Timeslot> timeslotBreaks) {
		for (Timeslot timeslot : timeslotBreaks)
			for (Event event : events)
				if (event.getTimeslots().contains(timeslot) && !event.isBreak(timeslot))
					event.addBreak(timeslot);
	}
	
	/**
	 * A�ade el timeslot como un break para todas las categor�as
	 * 
	 * @param timeslotBreak
	 */
	public void addBreak(Timeslot timeslotBreak) {
		for (Event event : events)
			if (event.getTimeslots().contains(timeslotBreak))
				event.addBreak(timeslotBreak);
	}
	
	/**
	 * Elimina el break para todas las categor�as
	 * 
	 * @param timeslotBreak
	 */
	public void removeBreak(Timeslot timeslot) {
		for (Event event : events)
			if (event.isBreak(timeslot))
				event.removeBreak(timeslot);
	}
	
	/**
	 * Invalida las pistas del diccionario a las horas indicadas para todas las categor�as (si la categor�a tiene
	 * esa pista y esos timeslots)
	 * 
	 * @param unavailableLocalizations diccionario de localizaciones de juego descartadas en la lista de timeslots
	 */
	public void setUnavailableLocalizations(HashMap<Localization, List<Timeslot>> unavailableLocalizations) {
		Set<Localization> localizations = unavailableLocalizations.keySet();	
		for (Event event : events)
			for (Localization localization : localizations)
				if (event.getLocalizations().contains(localization)) {
					List<Timeslot> timeslots = unavailableLocalizations.get(localization);
					for (Timeslot timeslot : timeslots)
						if (event.getTimeslots().contains(timeslot))
							event.addUnavailableLocalization(localization, timeslot);
				}
	}
	
	/**
	 * Invalida una pista a una hora o timeslot para todas las categor�as (si la categor�a tiene dicha pista
	 * y dicha hora)
	 * 
	 * @param localization localizaci�n de juego a invalidar
	 * @param timeslot     hora a la que invalidar
	 */
	public void addUnavailableLocalization(Localization localization, Timeslot timeslot) {
		for (Event event : events)
			if (event.getLocalizations().contains(localization) && event.getTimeslots().contains(timeslot))
				event.addUnavailableLocalization(localization, timeslot);
	}
	
	/**
	 * A�ade una pista no disponible a las horas indicadas para todas las categor�as
	 * 
	 * @param localization
	 * @param timeslots
	 */
	public void addUnavailableLocalization(Localization localization, List<Timeslot> timeslots) {
		for (Event event : events)
			for (Timeslot timeslot : timeslots)
				if (event.getLocalizations().contains(localization) && event.getTimeslots().contains(timeslot))
					event.addUnavailableLocalization(localization, timeslot);
	}
	
	/**
	 * Para cada categor�a que contenga la pista, elimina la invalidez de dicha localizaci�n
	 * 
	 * @param localization
	 */
	public void removeUnavailableLocalization(Localization localization) {
		for (Event event : events)
			if (event.getLocalizations().contains(localization))
				event.removeUnavailableLocalization(localization);
	}
	
	/**
	 * Para cada categor�a que contenga la pista y el timeslot, elimina la invalidez de dicha localizaci�n
	 * 
	 * @param localization
	 */
	public void removeUnavailableLocalizationTimeslot(Localization localization, Timeslot timeslot) {
		for (Event event : events)
			if (event.getLocalizations().contains(localization) && event.getTimeslots().contains(timeslot))
				event.removeUnavailableLocalizationTimeslot(localization, timeslot);
	}
	
	/**
	 * Muestra por la salida est�ndar una representaci�n de los horarios de cada categor�a
	 * 
	 * @param printMatches si es true se mostrar� un resumen de los partidos por cada categor�a, y si es false, no
	 */
	public void printCurrentSchedules(boolean printMatches) {
		StringBuilder sb = new StringBuilder();
		
		if (currentSchedules == null)
			sb.append("Empty schedule.\n");
		else {
			for (EventSchedule schedule : currentSchedules) {
				sb.append(schedule.toString());
				
				sb.append("\n");
				
				if (schedule != null && printMatches) {
					sb.append(String.format("Match duration: %d timelots\n", schedule.getEvent().getMatchDuration()));
					
					schedule.calculateMatches();
					List<Match> matches = schedule.getMatches();
					for (Match match : matches)
						sb.append(match).append("\n");
				}
					
				sb.append("\n");
			}
		}
		
		System.out.println(sb.toString());
	}
	
	/**
	 * Llama a printCurrentSchedules(true), mostr�ndose por la salida est�ndar los horarios y los partidos
	 */
	public void printCurrentSchedules() {
		printCurrentSchedules(true);
	}
	
	/**
	 * @return n�mero de partidos que se juegan en el torneo
	 */
	public int getNumberOfMatches() {
		int numberOfMatches = 0;
		for (Event event : events)
			numberOfMatches += event.getNumberOfMatches();
		return numberOfMatches;
	}
	
	public TournamentSolver getSolver() {
		return solver;
	}
	
	public String toString() {
		return name;
	}
}
