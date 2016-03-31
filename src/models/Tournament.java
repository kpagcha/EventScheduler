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
		
		currentSchedules = new EventSchedule[categories.length];
	}
	
	public void solve() {
		solver.execute();
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
	
	public TournamentSolver getSolver() {
		return solver;
	}
	
	public String toString() {
		return name;
	}
	
	public boolean hasFinished = false;
	
	public static void main(String[] args) {		
		Scanner sc = new Scanner(System.in);
		
		System.out.println("1 Sample One Category Tournament");
		System.out.println("2 Sample Tennis Tournament");
		System.out.println("3 Sample Medium Tennis Tournament");
		System.out.println("4 Sample Large Tennis Tournament");
		System.out.println("5 Sample Large Tennis Tournament With Collisions");
		System.out.print("Choose tournament: ");
		int tournamentOption = sc.nextInt();
		
		Tournament tournament = null;
		switch (tournamentOption) {
			case 1:
				tournament = EventManager.getInstance().getSampleOneCategoryTournament();
				break;
			case 2:
				tournament = EventManager.getInstance().getSampleTennisTournament();
				break;
			case 3:
				tournament = EventManager.getInstance().getSampleMediumTennisTournament();
				break;
			case 4:
				tournament = EventManager.getInstance().getSampleLargeTennisTournament();
				break;
			case 5:
				tournament = EventManager.getInstance().getSampleLargeTennisTournamentWithCollisions();
				break;
		}
		
		System.out.println("\n1 domOverWDeg");
		System.out.println("2 minDom_UB");
		System.out.println("3 minDom_LB");
		System.out.print("Choose Search Strategy: ");
		int searchStrategyOption = sc.nextInt();

		tournament.getSolver().setSearchStrategy(searchStrategyOption);
		tournament.solve();
		
		tournament.nextSchedules();
		
		boolean printSolutions = true;
		boolean askForInput = false;
		int maxSolutions = 1; // 0 -> todas las soluciones
		int foundSolutions = 0;
		
		do {
			if (printSolutions) {
				System.out.println("-------------------------------------------------------");
				System.out.println(tournament + "\n");
				tournament.printCurrentSchedules();
				
				if (tournament.currentSchedules != null) {
					CombinedSchedule combinedSchedule = tournament.getCombinedSchedule();
				
					System.out.println("All schedules combined in one");
					System.out.println(combinedSchedule);
					
					combinedSchedule.calculateMatches();
					Match[] matches = combinedSchedule.getMatches();
					System.out.println("All matches (" + matches.length + ")");
					for (Match match : matches)
						System.out.println(match);
					System.out.println();
					
					int occupation = combinedSchedule.groupByLocalizations();
					
					System.out.println("Combined schedule grouped by courts");
					System.out.println(combinedSchedule.groupedScheduleToString());
					
					System.out.println(
						String.format("Timeslot occupation: %s/%s (%s %%)\n",
							occupation,
							tournament.getAllLocalizations().size() * tournament.getAllTimeslots().size(),
							occupation / (double)(tournament.getAllLocalizations().size() * tournament.getAllTimeslots().size()) * 100
						)
					);
				}
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
		
		System.out.println("\n" + foundSolutions + " solutions found.");
	}
}
