package models;

public class Match {
	Player[] players;
	Localization localization;
	Timeslot timeslot;
	int matchDuration;
	
	public Match() {}
	
	public Match(Player[] players, Localization localization, Timeslot timeslot, int matchDuration) {
		this.players = players;
		this.localization = localization;
		this.timeslot = timeslot;
		this.matchDuration = matchDuration;
	}
	
	public String toString() {
		return String.format("At %s (%d) in %s: %s vs %s", timeslot.getLowerBoundStr(), matchDuration, localization, players[0], players[1]);
	}
}
