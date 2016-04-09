package models.tournaments.schedules.data;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import models.tournaments.events.entities.Localization;
import models.tournaments.events.entities.Player;
import models.tournaments.events.entities.Team;
import models.tournaments.events.entities.Timeslot;

public class Match {
	List<Player> players;
	Localization localization;
	Timeslot startTimeslot, endTimeslot;
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
			String.format("From %s to %s in %s: ", startTimeslot.getLowerBoundStr(), endTimeslot.getUpperBoundStr(), localization)
		);
		
		if (teams == null)
			sb.append(StringUtils.join(players, " vs "));
		else
			sb.append(StringUtils.join(teams, " vs "));
		
		return sb.toString();
	}
}
