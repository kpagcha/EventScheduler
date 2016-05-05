package data.model.tournament;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import data.model.schedule.TournamentSchedule;
import data.model.schedule.EventSchedule;
import data.model.schedule.data.Match;
import data.model.tournament.event.Event;
import data.model.tournament.event.entity.Localization;
import data.model.tournament.event.entity.Player;
import data.model.tournament.event.entity.timeslot.Timeslot;
import data.validation.validable.Validable;
import data.validation.validable.ValidationException;
import data.validation.validator.Validator;
import data.validation.validator.tournament.TournamentValidator;
import solver.TournamentSolver;

/**
 * Representa un torneo deportivo. Un torneo se compone de, al menos, un evento o categoría, ver {@link Event}.
 * <p>
 * El dominio un torneo en cuanto a jugadores, localizaciones de juego y horas de juego, es la suma de estos elementos
 * no repetidos en cada una de las categorías del torneo.
 * <p>
 * A un torneo le corresponde una variable de la clase {@link TournamentSolver} que se encarga del proceso de resolución
 * del problema que se modela gracias a la información que este torneo contiene y, sobre todo, sus eventos.
 * <p>
 * Un torneo lleva asociado un conjunto de horarios para cada evento (ver {@link EventSchedule}) y un horario combinado
 * de todos estos que representa el horario de todo el torneo (ver {@link TournamentSchedule}). Inicialmente, el valor
 * de estos horarios no ha sido asignado. Los horarios contendrán valores específicos una vez se ejecute el método
 * {@link #solve()} que inicia por primera vez el proceso de resolución, del que es responsable {@link TournamentSolver}.
 * <p>
 * Si el proceso de resolución ha sido satisfactorio, los horarios de los eventos y, por ende, el horario del torneo combinado,
 * se actualizarán a los valores de la primera solución encontrada. Si no se encuentra ninguna solución, los horarios permanecerán
 * con un valor por defecto <code>null</code> sin asignar.
 * <p>
 * Los horarios se podrán actualizar al valor de la siguiente solución mediante el método {@link #nextSchedules()}. Es importante
 * notar que este método sobrescribirá el valor actual de los horarios, de forma que si se pretenden guardar los valores previos,
 * se deberá implementar una funcionalidad específica para este propósito. Si la clase que ejecuta la resolución del problema ya
 * no encuentra más soluciones y se vuelve a invocar a {@link #nextSchedules()}, el valor de los horarios se volverán a reiniciar
 * con el valor de <code>null</code>, indicando que no hay más horarios disponibles para este torneo.
 *
 */
public class Tournament implements Validable {
	/**
	 * Nombre del torneo
	 */
	private String name;
	
	/**
	 * Categorías que componen el torneo 
	 */
	private final List<Event> events;
	
	/**
	 * Todos los jugadores que participan en el torneo. No se repiten los presentes en múltiples categorías
	 */
	private final List<Player> allPlayers;
	
	/**
	 * Todos los terrenos de juego en los que se desarrolla en el torneo. No se repiten los presentes en múltiples categorías
	 */
	private final List<Localization> allLocalizations;
	
	/**
	 * Todos los timeslots en los que discurre el torneo. No se repiten los presentes en múltiples categorías
	 */
	private final List<Timeslot> allTimeslots;

	/**
	 * Horarios para cada categoría
	 */
	private Map<Event, EventSchedule> currentSchedules;
	
	/**
	 * Horario del torneo que combina los horarios de todas las categorías en uno solo
	 */
	private TournamentSchedule schedule;
	
	/**
	 * El solver que obtendrá los horarios de cada categoría el torneo
	 */
	private final TournamentSolver solver;
	
	/**
	 * Validador del torneo
	 */
	private Validator<Tournament> validator = new TournamentValidator();
	
	/**
	 * Construye del torneo con un nombre y el conjunto de categorías que lo componen
	 * 
	 * @param name nombre del torneo, cadena no <code>null</code>
	 * @param categories categorías no repetidas que componen el torneo, debe haber al menos una
	 * @throws IllegalArgumentException si alguno de los parámetros es <code>null</code> o si la lista de categorías está vacía
	 */
	public Tournament(String name, Set<Event> categories) {
		if (name == null || categories == null)
			throw new IllegalArgumentException("The parameters cannot be null");
		
		if (categories.isEmpty())
			throw new IllegalArgumentException("The list of categories cannot be empty");
		
		if (categories.contains(null))
			throw new IllegalArgumentException("A category cannot be null");
		
		this.name = name;
		events = new ArrayList<Event>(categories);
		
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
	 * @param name nombre del torneo, cadena no <code>null</code>
	 * @param categories categorías que componen el torneo, debe haber al menos una
	 */
	public Tournament(String name, Event... categories) {
		this(name, new HashSet<Event>(Arrays.asList(categories)));
	}
	
	/**
	 * Comienza el proceso de resolución para calcular un primer horario
	 * 
	 * @return true si se ha encontrado una solución, false si ocurre lo contrario
	 * @throws ValidationException 
	 */
	public boolean solve() throws ValidationException {
		validate();
		
		boolean solved = solver.execute();
		
		currentSchedules = solver.getSolvedSchedules();
		schedule = new TournamentSchedule(this);
		
		return solved;
	}
	
	/**
	 * Actualiza el valor de los horarios con la nueva solución combinada. Si se ha llegado
	 * a la última solución se establece el valor de los horarios a null. Además, se resetea el valor
	 * del horario combinado
	 * 
	 * @return true si se han actualizado los horarios con una nueva solución, y false si
	 * se ha alcanzado la última solución
	 */
	public boolean nextSchedules() {
		currentSchedules = solver.getSolvedSchedules();
		schedule = new TournamentSchedule(this);
		return currentSchedules != null;
	}
	
	/**
	 * Devuelve los horarios de cada categoría con el valor actual. Si no se ha actualizado el valor llamando
	 * al método nextSchedules o si el solver ha alcanzado la última solución y se ha llamado seguidamente a
	 * nextSchedules, devuelve null
	 * 
	 * @return los horarios de cada categoría
	 */
	public Map<Event, EventSchedule> getCurrentSchedules() {
		return currentSchedules;
	}
	
	/**
	 * Devuelve un único horario combinado del torneo que include todos los jugadores, todas las localizaciones de
	 * juego y todas las horas o timeslots de los que compone
	 * 
	 * @return horario combinado del torneo
	 */
	public TournamentSchedule getSchedule() {
		return schedule;
	}
	
	/**
	 * Asigna un nombre no nulo al torneo
	 * 
	 * @param name nombre no <code>null</code> del torneo
	 * @throws IllegalArgumentException si el nombre es <code>null</code>
	 */
	public void setName(String name) {
		if (name == null)
			throw new IllegalArgumentException("Name cannot be null");
		
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	/**
	 * Devuelve la lista no modificable de eventos del torneo
	 * 
	 * @return lista de eventos del torneo envuelta en un wrapper que la hace no modificable
	 */
	public List<Event> getEvents() {
		return Collections.unmodifiableList(events);
	}
	
	/**
	 * Devuelve la lista no modificable de todos los jugadores del torneo
	 * 
	 * @return lista de jugadores del torneo envuelta en un wrapper que la hace no modificable
	 */
	public List<Player> getAllPlayers() {
		return Collections.unmodifiableList(allPlayers);
	}
	
	/**
	 * Devuelve la lista no modificable de todas las localizaciones de juego del torneo
	 * 
	 * @return lista de localizaciones de juego del torneo envuelta en un wrapper que la hace no modificable
	 */
	public List<Localization> getAllLocalizations() {
		return Collections.unmodifiableList(allLocalizations);
	}

	/**
	 * Devuelve la lista no modificable de todas las horas de juego del torneo
	 * 
	 * @return lista de timeslots del torneo envuelta en un wrapper que la hace no modificable
	 */
	public List<Timeslot> getAllTimeslots() {
		return Collections.unmodifiableList(allTimeslots);
	}
	
	/**
	 * Devuelve el número de partidos del torneo
	 * 
	 * @return número de partidos que se juegan en el torneo
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
	
	@SuppressWarnings("unchecked")
	public <T> void setValidator(Validator<T> validator) {
		if (validator == null)
			throw new IllegalArgumentException("The parameter cannot be null");
		
		this.validator = (Validator<Tournament>) validator;
	}
	
	public List<String> getMessages() {
		return validator.getValidationMessages();
	}
	
	public void validate() throws ValidationException {
		if (!validator.validate(this))
			throw new ValidationException(String.format("Validation has failed for this tournament (%s)", name));
	}
	
	/**
	 * Agrupa en un diccionario las categorías del torneo por el número de jugadores por partido
	 * 
	 * @return un diccionario donde la clave es el número de jugadores por partido y el valor, la lista
	 * de categorías que definen dicho número de jugadores por partido
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
	 * Añade timeslots no disponibles para el jugador en todas las categorías donde participe. Si el jugador no participa
	 * en determinadas categorías o si éstas no incluyen en su horas de juego algunas de las horas en el conjunto <code>timeslots</code>,
	 * se ignoran esos valores para esa categoría en concreto
	 * 
	 * @param player cualquier jugador
	 * @param timeslots cualquier conjunto de horas en las que el jugador no esté disponible
	 */
	public void addPlayerUnavailableTimeslots(Player player, Set<Timeslot> timeslots) {	
		for (Event event : events)
			if (event.getPlayers().contains(player))
				for (Timeslot timeslot : timeslots)
					if (event.getTimeslots().contains(timeslot))
						event.addUnavailablePlayer(player, timeslot);
	}
	
	/**
	 * Añade un timeslot no disponible para el jugador en todas las categorías donde participe. Si el jugador no participa
	 * en una categoría o si la hora <code>timeslot</code> no pertenece al dominio de juego de la misma, se ignora para esa
	 * categoría, así como si la hora ya ha sido marcada como no disponible para el jugador
	 * 
	 * @param player culaquier jugador
	 * @param timeslot culquier hora
	 */
	public void addPlayerUnavailableTimeslot(Player player, Timeslot timeslot) {
		for (Event event : events)
			if (event.getPlayers().contains(player) && event.getTimeslots().contains(timeslot) && 
				(!event.getUnavailableLocalizations().containsKey(player) ||
					!event.getUnavailableLocalizations().get(player).contains(timeslot)))
				event.addUnavailablePlayer(player, timeslot);
	}
	
	/**
	 * Si el jugador no está disponible a la hora <code>timeslot</code>, se elimina de la lista y vuelve a estar disponible a esa hora,
	 * para todas las categorías. Si el jugador no pertenece a una categoría o la hora no existe en el dominio de ésta, no se
	 * lleva a cabo ninguna acción para esa categoría, así como si la hora ya ha sido marcada como no disponible para el jugador
	 * 
	 * @param player cualquier jugador
	 * @param timeslot cualquier hora
	 */
	public void removePlayerUnavailableTimeslot(Player player, Timeslot timeslot) {
		for (Event event : events)
			if (event.getPlayers().contains(player) && event.getTimeslots().contains(timeslot))
				event.removePlayerUnavailableTimeslot(player, timeslot);
	}
	
	/**
	 * Marca los timeslots de la lista como breaks para todas las categorías, si la categoría incluye en su dominio de horas de
	 * juego cada hora de la lista de breaks; si no lo incluye o si esa hora ya está marcada como break,
	 * no se ejecuta ninguna acción para ese timeslot
	 * 
	 * @param timeslotBreaks una lista de horas que indican un período de descanso o break
	 */
	public void addBreaks(List<Timeslot> timeslotBreaks) {
		for (Timeslot timeslot : timeslotBreaks)
			for (Event event : events)
				if (event.getTimeslots().contains(timeslot) && !event.isBreak(timeslot))
					event.addBreak(timeslot);
	}
	
	/**
	 * Añade el timeslot como un break para todas las categorías. Si una categoría no incluye en su dominio el timeslot o si éste
	 * ya ha sido marcado como break, no se lleva a cabo ninguna acción
	 * 
	 * @param timeslotBreak una hora cualquiera
	 */
	public void addBreak(Timeslot timeslotBreak) {
		for (Event event : events)
			if (event.getTimeslots().contains(timeslotBreak) && !event.isBreak(timeslotBreak))
				event.addBreak(timeslotBreak);
	}
	
	/**
	 * Elimina el break para todas las categorías, si existe ese timeslot en el dominio del evento y si ha sido marcado como 
	 * break, de lo contrario no se hace nada
	 * 
	 * @param timeslot una hora cualquiera que se marca como break
	 */
	public void removeBreak(Timeslot timeslot) {
		for (Event event : events)
			if (event.getTimeslots().contains(timeslot) && event.isBreak(timeslot))
				event.removeBreak(timeslot);
	}
	
	/**
	 * Invalida una pista a una hora o timeslot para todas las categorías, si la categoría tiene dicha pista
	 * y dicha hora, de lo contrario no se toma ninguna acción, así como si la hora ya ha sido marcada 
	 * como no disponible para la localización
	 * 
	 * @param localization localización de juego a invalidar
	 * @param timeslot hora a la que invalidar
	 */
	public void addUnavailableLocalization(Localization localization, Timeslot timeslot) {
		for (Event event : events)
			if (event.getLocalizations().contains(localization) && event.getTimeslots().contains(timeslot) &&
				(!event.getUnavailableLocalizations().containsKey(localization) ||
					!event.getUnavailableLocalizations().get(localization).contains(timeslot)))
				event.addUnavailableLocalization(localization, timeslot);
	}
	
	/**
	 * Añade una pista no disponible a las horas indicadas para todas las categorías
	 * 
	 * @param localization una localización de juego que se marca como inválida a las horas especificadas
	 * @param timeslots el conjunto de horas a las que se marca la pista inválida
	 */
	public void addUnavailableLocalization(Localization localization, List<Timeslot> timeslots) {
		for (Timeslot timeslot : timeslots)
			addUnavailableLocalization(localization, timeslot);
	}
	
	/**
	 * Revierte la no disponibilidad de una localización de juego, si existe para las categorías que la incluyan en 
	 * su dominio, es decir, en su lista de localizaciones de juego.
	 * 
	 * @param localization localización que vuelve a estar disponible para todas las horas
	 */
	public void removeUnavailableLocalization(Localization localization) {
		for (Event event : events)
			if (event.getLocalizations().contains(localization))
				event.removeUnavailableLocalization(localization);
	}
	
	/**
	 * Revierte la no disponibilidad de una localización de juego a la hora especificada, si existe para las categorías
	 * en las que tanto la localización como la hora existan en sus correspondientes dominios, así como que la localización
	 * se encuentre marcada como no disponible a la hora <code>timeslot</code>; de lo contrario, no se tomará ninguna acción
	 * 
	 * @param localization localización de juego para la que se va a marcar una hora como disponible de nuevo
	 * @param timeslot hora que se va a marcar nuevamente como disponible una localización
	 */
	public void removeUnavailableLocalizationTimeslot(Localization localization, Timeslot timeslot) {
		for (Event event : events)
			if (event.getLocalizations().contains(localization) && event.getTimeslots().contains(timeslot))
				event.removeUnavailableLocalizationTimeslot(localization, timeslot);
	}
	
	/**
	 * Muestra por la salida estándar una representación de los horarios de cada categoría
	 * 
	 * @param printMatches si es <code>true</code> se mostrará adicionalmente un resumen de los partidos para cada categoría, 
	 * y si es <code>false</code>, solamente se mostrarán los horarios
	 */
	public void printCurrentSchedules(boolean printMatches) {
		StringBuilder sb = new StringBuilder();
		
		if (currentSchedules == null)
			sb.append("Empty schedule.\n");
		else {
			for (EventSchedule schedule : currentSchedules.values()) {
				sb.append(schedule.toString());
				
				sb.append("\n");
				
				if (schedule != null && printMatches) {
					sb.append(String.format("Match duration: %d timelots\n", schedule.getEvent().getTimeslotsPerMatch()));
					
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
	 * Muestra los horarios y los partidos por la salida estándar
	 */
	public void printCurrentSchedules() {
		printCurrentSchedules(true);
	}
	
	public String toString() {
		return name;
	}
}
