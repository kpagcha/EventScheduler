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

	private EventSchedule[] currentSchedules = null;
	
	private TournamentSolver solver;
	
	public Tournament(String name, Event[] categories) {
		this.name = name;
		events = categories;
		
		solver = new TournamentSolver(this);
	}
	
	public boolean solve() {
		return solver.execute();
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
	
	public int getNumberOfEvents() {
		return events.length;
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
	
	/**
	 * Actualiza el valor de los horarios con la nueva soluci�n combinada. Si se ha llegado
	 * a la �ltima soluci�n se establece el valor de los horarios a null
	 * 
	 * @return true si se han actualizado los horarios con una nueva soluci�n, y false si
	 * se ha alcanzado la �ltima soluci�n
	 */
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
	
	public void printCurrentSchedules(boolean printMatches) {
		StringBuilder sb = new StringBuilder();
		
		if (currentSchedules == null)
			sb.append("Empty schedule.\n");
		else {
			for (int i = 0; i < currentSchedules.length; i++) {
				EventSchedule schedule = currentSchedules[i];
				
				sb.append(schedule.toString());
				
				sb.append("\n");
				
				if (schedule != null && printMatches) {
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
	
	public void printCurrentSchedules() {
		printCurrentSchedules(true);
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
		System.out.println("6 Sample Tournament With Variable Domains and Collisions");
		System.out.print("Choose tournament: ");
		int tournamentOption = sc.nextInt();
		
		System.out.print("\nRandom drawings (0 no, 1 yes): ");
		boolean randomDrawings = sc.nextInt() == 1;
		
		Tournament tournament = null;
		switch (tournamentOption) {
			case 1:
				tournament = EventManager.getInstance().getSampleOneCategoryTournament(randomDrawings);
				break;
			case 2:
				tournament = EventManager.getInstance().getSampleTennisTournament(randomDrawings);
				break;
			case 3:
				tournament = EventManager.getInstance().getSampleMediumTennisTournament(randomDrawings);
				break;
			case 4:
				tournament = EventManager.getInstance().getSampleLargeTennisTournament(randomDrawings);
				break;
			case 5:
				tournament = EventManager.getInstance().getSampleLargeTennisTournamentWithCollisions(randomDrawings);
				break;
			case 6:
				tournament = EventManager.getInstance().getSampleVariableDomainsTournamentWithCollisions(randomDrawings);
				break;
		}
		
		System.out.println("\n1 domOverWDeg");
		System.out.println("2 minDom_UB");
		System.out.println("3 minDom_LB");
		System.out.println("4 minDom_UB, domOverWDeg");
		System.out.print("Choose Search Strategy: ");
		int searchStrategyOption = sc.nextInt();

		tournament.getSolver().setSearchStrategy(searchStrategyOption);
		
		boolean printSolutions = true;
		boolean printMatches = true;
		boolean askForInput = false;
		boolean tryDifferentRandomDrawings = true;
		int maxSolutions = 1; // 0 -> todas las soluciones
		int foundSolutions = 0;
		int tries = 500; // n�mero de intentos para encontrar soluci�n por sorteo (0: infinito)
		
		boolean solutionFound = tournament.solve();
		
		if (solutionFound || (randomDrawings && tryDifferentRandomDrawings)) {
		
			if (solutionFound)
				solutionFound = tournament.nextSchedules();
			else if (randomDrawings && tryDifferentRandomDrawings) {
				int tryCount = 0;
				// probar nuevas combinaciones de sorteo hasta que se encuentre soluci�n o se supere el n�mero de intentos
				while (!solutionFound && (tries == 0 || tryCount++ < tries))
					solutionFound = tournament.solve();
				
				if (solutionFound) {
					System.out.println("\nSolution found after " + (tryCount + 1) + " tries.\n");
					tournament.nextSchedules();
				}
			}
			
			do {
				if (printSolutions) {
					System.out.println("-------------------------------------------------------");
					System.out.println(tournament + "\n");
					tournament.printCurrentSchedules(printMatches);
					
					if (tournament.currentSchedules != null) {
						CombinedSchedule combinedSchedule = tournament.getCombinedSchedule();
					
						System.out.println("All schedules combined in one");
						System.out.println(combinedSchedule);
						
						combinedSchedule.calculateMatches();
						
						if (printMatches) {
							Match[] matches = combinedSchedule.getMatches();
							System.out.println("All matches (" + matches.length + ")");
							for (Match match : matches)
								System.out.println(match);
							System.out.println();
						}
						
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
				
				if (solutionFound)
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
		}
		
		sc.close();
		
		System.out.println("\n" + foundSolutions + " solutions found.");
	}
}
