package models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import manager.EventManager;
import models.schedules.CombinedSchedule;
import models.schedules.EventSchedule;
import solver.TournamentSolver;

public class Tournament {
	private String name;
	private Event[] events;

	private EventSchedule[] currentSchedules;
	
	private TournamentSolver solver;
	
	public Tournament(String name, Event[] categories) {
		this.name = name;
		events = categories;
		
		solver = new TournamentSolver(this);
		solver.execute();
		
		currentSchedules = new EventSchedule[categories.length];
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public Event[] getEvents() {
		return events;
	}
	
	/**
	 * Devuelve todos los jugadores que componen las distintas categor�as del torneo, teniendo en cuenta
	 * a los jugadores que juegan en distintas categor�as a la vez
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
	 * Devuelve todos los timeslots que componen las distintas categor�as del torneo, teniendo en cuenta
	 * los timeslots compartidos por distintas categor�as
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
	
	public List<Localization> getAllLocalizations() {
		List<Localization> localizations = new ArrayList<Localization>();
		
		for (Event event : events) {
			Localization[] eventLocalizations = event.getLocalizations();
			for (Localization localization : eventLocalizations)
				if (!localizations.contains(localization))
					localizations.add(localization);
		}
		
		return localizations;
	}
	
	public Map<Integer, List<Event>> groupEventsByNumberOfPlayersPerMatch() {
		Map<Integer, List<Event>> eventsByNumberOfPlayersPerMatch = new HashMap<Integer, List<Event>>();
		
		for (Event event : events) {
			int n = event.getPlayersPerMatch();
			if (eventsByNumberOfPlayersPerMatch.containsKey(n))
				eventsByNumberOfPlayersPerMatch.get(n).add(event);
			else
				eventsByNumberOfPlayersPerMatch.put(n, new ArrayList<Event>(Arrays.asList(new Event[]{ event })));
		}
		
		return eventsByNumberOfPlayersPerMatch;
	}
	
	public boolean nextSchedules() {
		currentSchedules = solver.getSchedules();
		return currentSchedules != null;
	}
	
	public EventSchedule[] getSchedules() {
		return currentSchedules;
	}
	
	public CombinedSchedule getCombinedSchedule() {
		return new CombinedSchedule(this);
	}
	
	public void printCurrentSchedules() {
		StringBuilder sb = new StringBuilder();
		
		if (currentSchedules == null)
			sb.append("No more solutions found.\n");
		else {
			for (int i = 0; i < currentSchedules.length; i++) {
				EventSchedule schedule = currentSchedules[i];
				
				sb.append(schedule.toString());
				
				sb.append("\n");
				
				if (schedule != null) {
					sb.append(String.format("Match duration: %d timelots\n", events[i].getMatchDuration()));
					
					schedule.calculateMatches();
					Match[] matches = schedule.getMatches();
					for (int j = 0; j < matches.length; j++)
						sb.append(matches[j]).append("\n");
				}
					
				sb.append("\n");
			}
		}
		
		System.out.println(sb.toString());
	}
	
	public int getNumberOfMatches() {
		int numberOfMatches = 0;
		for (Event event : events)
			numberOfMatches += event.getNumberOfMatches();
		return numberOfMatches;
	}
	
	public String toString() {
		return name;
	}
	
	public static void main(String[] args) {
		//Tournament tournament = EventManager.getInstance().getSampleSmallTournament();
		//Tournament tournament = EventManager.getInstance().getSampleTournamentWithOneCategory();
		//Tournament tournament = EventManager.getInstance().getSampleTennisTournament();
		//Tournament tournament = EventManager.getInstance().getSampleBigTennisTournament();
		Tournament tournament = EventManager.getInstance().getSampleMediumTennisTournament();
		
		tournament.nextSchedules();
		
		Scanner sc = new Scanner(System.in);
		
		boolean printSolutions = true;
		boolean askForInput = false;
		int maxSolutions = 1; // 0 -> todas las soluciones
		int foundSolutions = 0;
		
		do {
			if (printSolutions) {
				System.out.println("-------------------------------------------------------");
				System.out.println(tournament + "\n");
				tournament.printCurrentSchedules();
				
				CombinedSchedule combinedSchedule = tournament.getCombinedSchedule();
				
				System.out.println("All schedules combined in one");
				System.out.println(combinedSchedule);
				
				combinedSchedule.calculateMatches();
				Match[] matches = combinedSchedule.getMatches();
				System.out.println("All matches (" + matches.length + ")");
				for (Match match : matches)
					System.out.println(match);
				System.out.println();
				
				combinedSchedule.groupByLocalizations();
				
				System.out.println("Combined schedule grouped by courts");
				System.out.println(combinedSchedule.groupedScheduleToString());
			}
			
			foundSolutions++;
			
			if (askForInput) {
				System.out.print("Show next solution (y/n)?: ");
				String input = sc.next();
				if (!input.equalsIgnoreCase("y"))
					break;
			}
			
			if (maxSolutions > 0 && foundSolutions >= maxSolutions)
				break;
		
		} while (tournament.nextSchedules());
		
		sc.close();
		
		System.out.println(foundSolutions + " solutions found.");
	}
}