package models;

public class Localization {
	private static int lastId = 0;
	
	private int id;
	private String name;
	
	public Localization(String name) {
		this.name = name;
		id = lastId++;
	}
	
	public int getId() {
		return id;
	}
	
	public String toString() {
		return name;
	}
	
	public boolean equals(Localization localization) {
		return localization != null && id == localization.getId();
	}
}
