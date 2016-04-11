package data.model.schedule.data;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import data.model.tournament.event.entity.Localization;
import data.model.tournament.event.entity.Player;
import data.model.tournament.event.entity.Team;
import data.model.tournament.event.entity.timeslot.Timeslot;

public class Match {
	/**
	 * Lista de jugadores que componen el enfrentamiento
	 */
	private List<Player> players;
	
	/**
	 * Localización de juego donde tiene lugar el enfrentamiento 
	 */
	private Localization localization;
	
	/**
	 * Período en el que el enfrentamiento comienza
	 */
	private Timeslot startTimeslot;
	
	/**
	 * Último período en el que el enfrentamiento tiene lugar
	 */
	private Timeslot endTimeslot;
	
	/**
	 * Lista de equipos, si hubiera
	 */
	List<Team> teams;
	
	public Match(List<Player> players, Localization localization, Timeslot start, Timeslot end) {
		this.players = players;
		this.localization = localization;
		startTimeslot = start;
		endTimeslot = end;
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
	
	public void setTeams(List<Team> teams) {
		this.teams = teams;
	}
	
	public List<Team> getTeams() {
		return teams;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder(
			String.format("From %s to %s in %s: ", startTimeslot, endTimeslot, localization)
		);
		
		if (teams == null)
			sb.append(StringUtils.join(players, " vs "));
		else
			sb.append(StringUtils.join(teams, " vs "));
		
		return sb.toString();
	}
}
