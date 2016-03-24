package manager;

import java.util.ArrayList;
import java.util.List;

import models.Event;
import models.Match;
import models.Player;
import models.Schedule;
import models.Timeslot;
import solver.EventSolver;

public class Tournament {
	private String name;
	private Event[] events;

	private Schedule[] currentSchedules;
	
	private EventSolver solver;
	
	public Tournament(String name, Event[] categories) {
		this.name = name;
		events = categories;
		
		solver = new EventSolver(this);
		solver.execute();
		
		currentSchedules = new Schedule[categories.length];
	}
	
	public Event[] getEvents() {
		return events;
	}
	
	/**
	 * Devuelve todos los jugadores que componen las distintas categorías del torneo, teniendo en cuenta
	 * a los jugadores que juegan en distintas categorías a la vez
	 * 
	 * @return lista de jugadores del torneo
	 */
	public List<Player> getAllPlayers() {
		List<Player> players = new ArrayList<Player>();
		
		for (Event event : events) {
			Player[] eventPlayers = event.getPlayers();
			for (Player player : eventPlayers)
				if (!players.contains(player))
					players.add(player);
		}
		
		return players;
	}
	
	/**
	 * Devuelve todos los timeslots que componen las distintas categorías del torneo, teniendo en cuenta
	 * los timeslots compartidos por distintas categorías
	 * @return lista de timeslots del torneo
	 */
	public List<Timeslot> getAllTimeslots() {
		List<Timeslot> timeslots = new ArrayList<Timeslot>();
		
		for (Event event : events) {
			Timeslot[] eventTimeslots = event.getTimeslots();
			for (Timeslot timeslot : eventTimeslots)
				if (!timeslots.contains(timeslot))
					timeslots.add(timeslot);
		}
		
		return timeslots;
	}
	
	public Schedule[] getSchedules() {
		currentSchedules = solver.getSchedules();
		return currentSchedules;
	}
	
	public void printCurrentSchedules() {
		StringBuilder sb = new StringBuilder();
		
		if (currentSchedules == null)
			sb.append("No more solutions found.\n");
		else {
			for (int i = 0; i < currentSchedules.length; i++) {
				Schedule schedule = currentSchedules[i];
				
				sb.append(schedule.toString());
				
				sb.append("\n");
				
				if (schedule != null) {
					sb.append(String.format("Match duration: %d timelots\n", events[i].getMatchDuration()));
					Match[] matches = schedule.getMatches();
					for (int j = 0; j < matches.length; j++)
						sb.append(matches[j]).append("\n");
				}
					
				sb.append("\n");
			}
		}
		
		System.out.println(sb.toString());
	}
	
	public String toString() {
		return name;
	}
	
	public static void main(String[] args) {
		Tournament tournament = EventManager.getInstance().getSampleSmallTournament();
		
		int nSol = 15;
		int solutions = 0;
		for (int i = 0; i < nSol; i++) {
			tournament.getSchedules();
			
			if (tournament.currentSchedules == null)
				break;
			
			System.out.println("-------------------------------------------------------");
			System.out.println(tournament + "\n");
			tournament.printCurrentSchedules();
			
			solutions++;
		}
		System.out.println(solutions + " solutions found.");
	}
}
