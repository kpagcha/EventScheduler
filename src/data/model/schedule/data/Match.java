package data.model.schedule.data;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import data.model.tournament.event.entity.Localization;
import data.model.tournament.event.entity.Player;
import data.model.tournament.event.entity.Team;
import data.model.tournament.event.entity.timeslot.Timeslot;

/**
 * Representa un partido o enfrentamiento. Un partido normalmente se compone de dos o más jugadores que se enfrentan
 * o compiten entre sí, aunque también puede ser compuesto por un solo jugador, para deportes o juegos individuales.
 *
 */
public class Match {
	/**
	 * Lista de jugadores que componen el enfrentamiento
	 */
	private final List<Player> players;
	
	/**
	 * Localización de juego donde tiene lugar el enfrentamiento 
	 */
	private final Localization localization;
	
	/**
	 * Período en el que el enfrentamiento comienza
	 */
	private final Timeslot startTimeslot;
	
	/**
	 * Último período en el que el enfrentamiento tiene lugar
	 */
	private final Timeslot endTimeslot;
	
	/**
	 * Número de timeslots que ocupa
	 */
	private final int duration;
	
	/**
	 * Lista de equipos, si hubiera
	 */
	private List<Team> teams;
	
	/**
	 * Construye un enfrentamiento
	 * 
	 * @param players lista de jugadores que componen el partido no nula y con al menos un jugador
	 * @param localization localización de juego donde tiene lugar el enfrentamiento
	 * @param start hora o <i>timeslot</i> de comienzo del partido
	 * @param end hora o <i>timeslot</i> de fin del partido
	 * @param duration número de timeslots que dura el partido. Mayor que 0
	 * 
	 * @throws IllegalArgumentException si algún parámetro es <code>null</code>, si la lista de jugadores está vacía
	 * o si la duración no es superior a 0, o si <code>end</code> es anterior a <code>start</code>
	 */
	public Match(List<Player> players, Localization localization, Timeslot start, Timeslot end, int duration) {
		if (players == null || localization == null || start == null || end == null)
			throw new IllegalArgumentException("The parameters cannot be null");
		
		if (players.isEmpty())
			throw new IllegalArgumentException("Players cannot be empty");
		
		if (duration < 1)
			throw new IllegalArgumentException("The duration of the match cannot amount to a timeslot span lesser than 1");
		
		if (end.compareTo(start) > 0)
			throw new IllegalArgumentException("The end timeslot cannot precede the start timeslot");
		
		if (duration > 1 && start.compareTo(end) == 0)
			throw new IllegalArgumentException("The duration of the match cannot amount to a timeslot span greater than 1 if the start timeslot is the same than the end timeslot");
		
		this.players = players;
		this.localization = localization;
		startTimeslot = start;
		endTimeslot = end;
		this.duration = duration;
	}
	
	public List<Player> getPlayers() {
		return players;
	}

	public Localization getLocalization() {
		return localization;
	}

	public Timeslot getStartTimeslot() {
		return startTimeslot;
	}
	
	public Timeslot getEndTimeslot() {
		return endTimeslot;
	}
	
	public int getDuration() {
		return duration;
	}
	
	/**
	 * Establece el conjunto de equipos que se enfrentan en este partido
	 * 
	 * @param teams una lista de equipos que cumple las precondiciones especificadas
	 * @throws IllegalArgumentException si <code>teams</code> es <code>null</code>, si tiene menos de 2 elementos, si contiene
	 * un equipo <code>null</code>, si el número de jugadores de cada equipo no es el mismo o si no todos los jugadores de cada
	 * equipo están contenidos en la lista de jugadores del partido
	 */
	public void setTeams(List<Team> teams) {
		if (teams == null)
			throw new IllegalArgumentException("Teams cannot be null");
		
		if (teams.size() < 2)
			throw new IllegalArgumentException("Teams cannot have less than 2 elements");
		
		if (teams.contains(null))
			throw new IllegalArgumentException("Teams cannot contain a null team");
		
		int nPlayersPerTeam = teams.get(0).getPlayers().size();
		for (int i = 1; i < teams.size(); i++)
			if (teams.get(i).getPlayers().size() != nPlayersPerTeam)
				throw new IllegalArgumentException("Teams cannot have different number of players");
		
		for (Team team : teams)
			if (!players.containsAll(team.getPlayers()))
				throw new IllegalArgumentException("All players in a team must be contained in the list of players of this match");
		
		this.teams = teams;
	}
	
	public List<Team> getTeams() {
		return teams;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(String.format("At %s in %s: ", startTimeslot, localization));
		
		if (teams == null)
			sb.append(StringUtils.join(players, " vs "));
		else
			sb.append(StringUtils.join(teams, " vs "));
		
		return sb.toString();
	}
}
