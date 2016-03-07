package models;

public class Localization {
	private int id;
	private String name;
	
	public Localization() {}
	
	public Localization(int id, String name) {
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
