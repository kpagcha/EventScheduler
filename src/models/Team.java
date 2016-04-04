package models;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

public class Team extends Entity {
	private Set<Player> players;
	
	public Team() {
		this("Team");
	}
	
	public Team(String name) {
		super(name);
		players = new HashSet<Player>();
	}
	
	public Team(String name, Set<Player> players) {
		super(name);
		this.players = players;
	}
	
	public Team(String name, Player[] playersArray) {
		super(name);
		players = new HashSet<Player>(Arrays.asList(playersArray));
	}
	
	public Team(Player... playersArray) {
		this("Team (" + playersArray.length + ")", playersArray);
	}
	
	public Set<Player> getPlayers() {
		return players;
	}
	
	public boolean contains(Player player) {
		return players.contains(player);
	}
	
	public boolean contains(Collection<Player> playersCollection) {
		for (Player player : playersCollection)
			if (!players.contains(player))
				return false;
		return true;
	}
	
	public boolean contains(Player[] playersArray) {
		return contains(new HashSet<Player>(Arrays.asList(playersArray)));
	}
	
	public String toString() {
		return new StringBuilder(name + ": ").append(StringUtils.join(players, ", ")).toString();
	}
}
