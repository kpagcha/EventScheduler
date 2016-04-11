package data.model.tournament.event.entity;

public abstract class Entity {
	protected String name;
	
	public Entity() {
		name = "";
	}
	
	public Entity(String name) {
		this.name = name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public String toString() {
		return name;
	}
}
