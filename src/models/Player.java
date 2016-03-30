package models;

public class Player {
	private static int lastId = 0;
	
	private int id;
	private String name;
	
	public Player() {}
	
	public Player(String name) {
		this.name = name;
		id = lastId++;
	}
	
	public int getId() {
		return id;
	}
	
	public String toString() {
		return name;
	}
	
	public boolean equals(Player player) {
		return player != null && id == player.getId();
	}
}