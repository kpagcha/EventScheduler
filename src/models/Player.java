package models;

public class Player {
	private int id;
	private String name;
	
	public Player() {}
	
	public Player(int id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public int getId() {
		return id;
	}
	
	public String toString() {
		return name;
	}
}
