package models.tournaments.schedules.data;

import models.tournaments.events.entities.Localization;
import models.tournaments.events.entities.Player;
import models.tournaments.events.entities.Timeslot;

public class Match {
	Player[] players;
	Localization localization;
	Timeslot timeslot;
	int matchDuration;
	
	public Match(Player[] players, Localization localization, Timeslot timeslot, int matchDuration) {
		this.players = players;
		this.localization = localization;
		this.timeslot = timeslot;
		this.matchDuration = matchDuration;
	}
	
	public Player[] getPlayers() {
		return players;
	}

	public Localization getLocalization() {
		return localization;
	}

	public Timeslot getTimeslot() {
		return timeslot;
	}

	public int getMatchDuration() {
		return matchDuration;
	}

	public String toString() {
		String matchStr = String.format("At %s (%d) in %s: ", timeslot.getLowerBoundStr(), matchDuration, localization);
		for (Player player : players)
			matchStr += String.format("%s, ", player);
		
		return matchStr.substring(0, matchStr.length() - 2);
	}
}
