package data.model.schedule;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import data.model.schedule.data.Match;
import data.model.schedule.data.ScheduleValue;
import data.model.tournament.event.entity.Localization;
import data.model.tournament.event.entity.Player;
import data.model.tournament.event.entity.timeslot.Timeslot;

/**
 * Representa un horario mediante una matriz bidimensional de
 * {@link ScheduleValue} que dota de significado a cada elemento.
 * <p>
 * La primera dimensión de la matriz (filas) corresponde a los jugadores y la
 * segunda dimensión (columnas) corresponde con las horas de juego
 * <p>
 * Además, mantiene una lista de partidos (ver {@link Match}) que se extraen del
 * procesamiento de la matriz mencionada.
 *
 */
public abstract class Schedule {

	/**
	 * Representación del horario con la ayuda de la clase {@link ScheduleValue}
	 */
	protected ScheduleValue[][] schedule;

	/**
	 * Lista de partidos que se dan en el horario
	 */
	protected List<Match> matches;

	/**
	 * Número de jugadores
	 */
	protected int nPlayers;

	/**
	 * Número de pistas
	 */
	protected int nLocalizations;

	/**
	 * Número de timeslots
	 */
	protected int nTimeslots;

	/**
	 * Lista de jugadores
	 */
	protected List<Player> players;

	/**
	 * Lista de localizaciones de juego
	 */
	protected List<Localization> localizations;

	/**
	 * Lista de horas de juego
	 */
	protected List<Timeslot> timeslots;

	/**
	 * Nombre del evento o torneo al que corresponde el horario
	 */
	protected String name;

	/**
	 * Devuelve la matriz bidimensional con la representación del horario
	 * 
	 * @return matriz bidimensional que representa el horario
	 */
	public ScheduleValue[][] getScheduleValues() {
		return schedule;
	}

	/**
	 * @return los partidos que componen este horario
	 */
	public List<Match> getMatches() {
		return matches;
	}

	/**
	 * Devuelve el conjunto de partidos en los que participa el jugador
	 * 
	 * @param player
	 *            jugador del que se quieren obtener los partidos
	 * @return lista de partidos
	 */
	public List<Match> filterMatchesByPlayer(Player player) {
		return matches.stream().filter(m -> m.getPlayers().contains(player)).collect(Collectors.toList());
	}

	/**
	 * Devuelve el conjunto de partidos en los que participan los jugadores
	 * 
	 * @param players
	 *            jugadores de los que se quieren obtener los partidos
	 * @return lista de partidos
	 */
	public List<Match> filterMatchesByPlayers(List<Player> players) {
		return matches.stream().filter(m -> m.getPlayers().containsAll(players)).collect(Collectors.toList());
	}

	/**
	 * Devuelve el conjunto de partidos que tienen lugar en la localización de
	 * juego
	 * 
	 * @param localization
	 *            localización de juego de la que se quiere obtener la lista de
	 *            partidos
	 * @return lista de partidos
	 */
	public List<Match> filterMatchesByLocalization(Localization localization) {
		return matches.stream().filter(m -> m.getLocalization().equals(localization)).collect(Collectors.toList());
	}

	/**
	 * Devuelve el conjunto de partidos que empiezan en el timeslot indicado
	 * 
	 * @param timeslot
	 *            hora de comienzo para la que se quiere obtener la lista de
	 *            partidos
	 * @return lista de partidos
	 */
	public List<Match> filterMatchesByStartTimeslot(Timeslot timeslot) {
		List<Match> timeslotMatches = new ArrayList<Match>();
		for (Match match : matches)
			if (match.getStartTimeslot().equals(timeslot))
				timeslotMatches.add(match);

		return timeslotMatches;
	}

	/**
	 * Devuelve el conjunto de partidos que terminan en el timeslot indicado
	 * 
	 * @param timeslot
	 *            hora de final para la que se quiere obtener la lista de
	 *            partidos
	 * @return lista de partidos
	 */
	public List<Match> filterMatchesByEndTimeslot(Timeslot timeslot) {
		List<Match> timeslotMatches = new ArrayList<Match>();
		for (Match match : matches)
			if (match.getEndTimeslot().equals(timeslot))
				timeslotMatches.add(match);

		return timeslotMatches;
	}

	/**
	 * Devuelve el conjunto de partidos que empiezan en el timeslot inicial y terminan en el final.
	 * <p>
	 * Si alguno de los <i>timeslots</i> que definine el rango, o ambos, no pertenecen al dominio de horas de juego
	 * del horario, se devolverá <code>null</code>
	 * 
	 * @param start
	 *            timeslot de comienzo
	 * @param end
	 *            timeslot final
	 * @return lista de partidos, o <code>null</code> si alguno de los <i>timeslots</i> no pertenece al dominio
	 */
	public List<Match> filterMatchesInTimeslotRange(Timeslot start, Timeslot end) {
		if (!(timeslots.contains(start) && timeslots.contains(end)))
			return null;
		
		List<Match> timeslotMatches = new ArrayList<Match>();
		for (Match match : matches)
			if (match.getStartTimeslot().equals(start) && match.getEndTimeslot().equals(end))
				timeslotMatches.add(match);

		return timeslotMatches;
	}

	/**
	 * Devuelve el conjunto de partidos cuyo transcurso discurre alguno de los <i>timeslots</i> indicados
	 * 
	 * @param timeslots
	 *            horas para las que se quieren buscar partidos que discurren
	 *            sobre alguna de ellas. No <code>null</code>
	 * @return lista de partidos
	 */
	public List<Match> filterMatchesDuringTimeslot(Timeslot timeslot) {
		List<Match> timeslotMatches = new ArrayList<Match>();
		for (Match match : matches)
			if (timeslot.compareTo(match.getStartTimeslot()) <= 0 && timeslot.compareTo(match.getEndTimeslot()) >= 0)
				timeslotMatches.add(match);

		return timeslotMatches;
	}
	
	/**
	 * Devuelve el conjunto de partidos cuyo transcurso discurre sobre el
	 * timeslot indicado
	 * 
	 * @param timeslot
	 *            hora para la que se quieren buscar partidos que discurren
	 *            sobre ella. No <code>null</code>
	 * @return lista de partidos
	 */
	public List<Match> filterMatchesDuringTimeslots(List<Timeslot> timeslots) {
		Set<Match> timeslotsMatches = new HashSet<>();
		for (Timeslot timeslot : timeslots)
			timeslotsMatches.addAll(filterMatchesDuringTimeslot(timeslot));

		return new ArrayList<Match>(timeslotsMatches);
	}
	
	/**
	 * Devuelve el conjunto de partidos cuyo transcurso discurre durante el rango de <i>timeslots</i> indicado,
	 * ambos <i>timeslots</i> incluidos.
	 * <p>
	 * Si alguno de los <i>timeslots</i> que definine el rango, o ambos, no pertenecen al dominio de horas de juego
	 * del horario, se devolverá <code>null</code>
	 * <p>
	 * Si <code>start</code> es mayor que <code>end</end>, se invierten los extremos de forma que estén en orden.
	 * 
	 * @param start
	 *            timeslot de comienzo
	 * @param end
	 *            timeslot final
	 * @return lista de partidos, o <code>null</code> si alguno de los <i>timeslots</i> no pertenece al dominio
	 */
	public List<Match> filterMatchesDuringTimeslotRange(Timeslot start, Timeslot end) {
		if (!(timeslots.contains(start) && timeslots.contains(end)))
			return null;
		
		Timeslot startTimeslot, endTimeslot;
		if (start.compareTo(end) >= 0) {
			startTimeslot = start;
			endTimeslot = end;
		} else {
			startTimeslot = end;
			endTimeslot = end;
		}
		
		List<Timeslot> timeslotRange = new ArrayList<Timeslot>();
		for (int i = timeslots.indexOf(startTimeslot); i <= timeslots.indexOf(endTimeslot); i++)
			timeslotRange.add(timeslots.get(i));
		
		return filterMatchesDuringTimeslots(timeslotRange);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder(name);

		sb.append(String.format("\n\n%8s", " "));

		for (int t = 0; t < nTimeslots; t++)
			sb.append(String.format("%4s", "t" + t));
		sb.append("\n");

		for (int p = 0; p < nPlayers; p++) {
			String playerStr = players.get(p).toString();
			if (playerStr.length() > 8)
				playerStr = playerStr.substring(0, 8);

			sb.append(String.format("%8s", playerStr));
			for (int t = 0; t < nTimeslots; t++)
				sb.append(String.format("%4s", schedule[p][t]));
			sb.append("\n");
		}

		return sb.toString();
	}
}
