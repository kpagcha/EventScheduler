package manager;

import models.Event;
import models.Match;
import models.Schedule;
import solver.EventSolver;

public class Tournament {
	/**
	 * Distintas categorías del evento
	 */
	
	private Event[] events;
	/**
	 * Una de las soluciones de los horarios calculados de cada categoría
	 */
	private Schedule[] currentSchedules;
	
	/**
	 * Solvers para cada categoría
	 */
	private EventSolver[] solvers;
	
	public Tournament(Event[] categories) {
		events = categories;
		
		solvers = new EventSolver[events.length];
		for (int i = 0; i < solvers.length; i++) {
			solvers[i] = new EventSolver(events[i]);
			solvers[i].execute();
		}
		
		currentSchedules = new Schedule[solvers.length];
	}
	
	public Schedule[] getSchedules() {
		for (int i = 0; i < solvers.length; i++) {
			currentSchedules[i] = solvers[i].getSchedule();
		}
		return currentSchedules;
	}
	
	public void printCurrentSchedules() {
		StringBuilder sb = new StringBuilder();
		
		for (int i = 0; i < currentSchedules.length; i++) {
			Schedule schedule = currentSchedules[i];
			
			sb.append(String.format("Category %d\n\n", i));
			if (schedule == null)
				sb.append("No more solutions found for Category " + i + ".");
			else
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
		
		System.out.println(sb.toString());
	}
	
	public String toString() {
		return "Tournament with " + events.length + " categories";
	}
	
	public static void main(String[] args) {
		Event event = EventManager.getInstance().getSampleEvent();
		//Event event = EventManager.getInstance().getSample32PlayersEvent();
		//Event event = EventManager.getInstance().getSampleSmallEvent();
		//Event event = EventManager.getInstance().getSampleEventWithBreaks();
		//Event event2 = EventManager.getInstance().getSampleSmallEvent();
		
		//Tournament tournament = new Tournament(new Event[]{ event, event2 });
		
		//Event event = EventManager.getInstance().getSampleEventWith3PlayersMatches();
		//Event event = EventManager.getInstance().getSampleEventWith1PlayerMatches();
		
		Tournament tournament = new Tournament(new Event[]{ event });
		
		int nSol = 1;
		for (int i = 0; i < nSol; i++) {
			tournament.getSchedules();
			
			System.out.println(tournament + "\n");
			tournament.printCurrentSchedules();
		}
	}
}
