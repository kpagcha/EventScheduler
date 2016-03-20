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
		String matchStr = String.format("At %s (%d) in %s: ", timeslot.getLowerBoundStr(), matchDuration, localization);
		for (Player player : players)
			matchStr += String.format("%s, ", player);
		
		return matchStr.substring(0, matchStr.length() - 2);
	}
}
