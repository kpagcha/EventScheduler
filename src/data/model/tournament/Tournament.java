package data.model.tournament;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import data.model.schedule.CombinedSchedule;
import data.model.schedule.EventSchedule;
import data.model.schedule.GroupedSchedule;
import data.model.schedule.data.Match;
import data.model.tournament.event.Event;
import data.model.tournament.event.entity.Localization;
import data.model.tournament.event.entity.Player;
import data.model.tournament.event.entity.timeslot.Timeslot;
import manager.EventManager;
import solver.TournamentSolver;

public class Tournament {
	/**
	 * Nombre del torneo
	 */
	private String name;
	
	/**
	 * Categor�as que componen el torneo 
	 */
	private Event[] events;

	/**
	 * Horarios para cada categor�a
	 */
	private EventSchedule[] currentSchedules = null;
	
	/**
	 * Horario del torneo que combina los horarios de todas las categor�as en uno solo
	 */
	private CombinedSchedule schedule;
	
	/**
	 * El solver que obtendr� los horarios de cada categor�a el torneo
	 */
	private TournamentSolver solver;
	
	/**
	 * @param name       nombre del torneo
	 * @param categories categor�as que componen el torneo
	 */
	public Tournament(String name, Event[] categories) {
		this.name = name;
		events = categories;
		
		solver = new TournamentSolver(this);
	}
	
	/**
	 * Comienza el proceso de resoluci�n para calcular el horario
	 * 
	 * @return true si se ha encontrado una soluci�n, false si ocurre lo contrario
	 */
	public boolean solve() {
		boolean solved = solver.execute();
		
		currentSchedules = solver.getSchedules();
		schedule = null;
		
		return solved;
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
	 * @return n�mero de categor�as
	 */
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
	 * 
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
	
	/**
	 * Devuelve todas las localizaciones de juegos que componen las distintas categor�as del torneo,
	 * teniendo en cuenta aqu�llas compartidas distintas categor�as
	 * 
	 * @return lista de localizaciones de juego del torneo
	 */
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
	
	/**
	 * Agrupa en un diccionario las categor�as del torneo por el n�mero de jugadores por partido
	 * 
	 * @return un diccionario donde la clave es el n�mero de jugadores por partido y el valor, la lista
	 * de categor�as que definen dicho n�mero de jugadores por partido
	 */
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
	 * a la �ltima soluci�n se establece el valor de los horarios a null. Adem�s, se resetea el valor
	 * del horario combinado
	 * 
	 * @return true si se han actualizado los horarios con una nueva soluci�n, y false si
	 * se ha alcanzado la �ltima soluci�n
	 */
	public boolean nextSchedules() {
		currentSchedules = solver.getSchedules();
		schedule = null;
		return currentSchedules != null;
	}
	
	/**
	 * Devuelve los horarios de cada categor�a con el valor actual. Si no se ha actualizado el valor llamando
	 * al m�todo nextSchedules o si el solver ha alcanzado la �ltima soluci�n y se ha llamado seguidamente a
	 * nextSchedules, devuelve null
	 * 
	 * @return los horarios de cada categor�a
	 */
	public EventSchedule[] getSchedules() {
		return currentSchedules;
	}
	
	/**
	 * Devuelve un �nico horario combinado del torneo que include todos los jugadores, todas las localizaciones de
	 * juego y todas las horas o timeslots de los que compone
	 * 
	 * @return horario combinado del torneo
	 */
	public CombinedSchedule getSchedule() {
		if (schedule == null)
			schedule = new CombinedSchedule(this);
		return schedule;
	}
	
	/**
	 * A�ade timeslots no disponibles para el jugador en todas las categor�as donde participe
	 * 
	 * @param player
	 * @param timeslots
	 */
	public void addPlayerUnavailableTimeslots(Player player, List<Timeslot> timeslots) {	
		for (Event event : events)
			if (event.containsPlayer(player))
				event.addPlayerUnavailableTimeslots(player, timeslots);
	}
	
	/**
	 * A�ade un timeslot no disponible para el jugador en todas las categor�as donde participe
	 * 
	 * @param player
	 * @param timeslot
	 */
	public void addPlayerUnavailableTimeslot(Player player, Timeslot timeslot) {
		for (Event event : events)
			if (event.containsPlayer(player))
				event.addPlayerUnavailableTimeslot(player, timeslot);
	}
	
	/**
	 * Si el jugador no est� disponible a la hora timeslot, se elimina de la lista y vuelve a estar disponible a esa hora,
	 * para todas las categor�as
	 * 
	 * @param player
	 * @param timeslot
	 */
	public void removePlayerUnavailableTimeslot(Player player, Timeslot timeslot) {
		for (Event event : events)
			if (event.containsPlayer(player))
				event.removePlayerUnavailableTimeslot(player, timeslot);
	}
	
	/**
	 * Marca los timeslots de la lista como breaks para todas las categor�as
	 * 
	 * @param breakTimeslots
	 */
	public void addBreaks(List<Timeslot> timeslotBreaks) {
		for (Timeslot timeslot : timeslotBreaks)
			for (Event event : events)
				if (event.containsTimeslot(timeslot) && !event.isBreak(timeslot))
					event.addBreak(timeslot);
	}
	
	/**
	 * A�ade el timeslot como un break para todas las categor�as
	 * 
	 * @param timeslotBreak
	 */
	public void addBreak(Timeslot timeslotBreak) {
		for (Event event : events)
			if (event.containsTimeslot(timeslotBreak))
				event.addBreak(timeslotBreak);
	}
	
	/**
	 * Elimina el break para todas las categor�as
	 * 
	 * @param timeslotBreak
	 */
	public void removeBreak(Timeslot timeslot) {
		for (Event event : events)
			if (event.isBreak(timeslot))
				event.removeBreak(timeslot);
	}
	
	/**
	 * Invalida las pistas del diccionario a las horas indicadas para todas las categor�as (si la categor�a tiene
	 * esa pista y esos timeslots)
	 * 
	 * @param unavailableLocalizations diccionario de localizaciones de juego descartadas en la lista de timeslots
	 */
	public void setUnavailableLocalizations(HashMap<Localization, List<Timeslot>> unavailableLocalizations) {
		Set<Localization> localizations = unavailableLocalizations.keySet();	
		for (Event event : events)
			for (Localization localization : localizations)
				if (event.containsLocalization(localization)) {
					List<Timeslot> timeslots = unavailableLocalizations.get(localization);
					for (Timeslot timeslot : timeslots)
						if (event.containsTimeslot(timeslot))
							event.addUnavailableLocalization(localization, timeslot);
				}
	}
	
	/**
	 * Invalida una pista a una hora o timeslot para todas las categor�as (si la categor�a tiene dicha pista
	 * y dicha hora)
	 * 
	 * @param localization localizaci�n de juego a invalidar
	 * @param timeslot     hora a la que invalidar
	 */
	public void addUnavailableLocalization(Localization localization, Timeslot timeslot) {
		for (Event event : events)
			if (event.containsLocalization(localization) && event.containsTimeslot(timeslot))
				event.addUnavailableLocalization(localization, timeslot);
	}
	
	/**
	 * A�ade una pista no disponible a las horas indicadas para todas las categor�as
	 * 
	 * @param localization
	 * @param timeslots
	 */
	public void addUnavailableLocalization(Localization localization, List<Timeslot> timeslots) {
		for (Event event : events)
			for (Timeslot timeslot : timeslots)
				if (event.containsLocalization(localization) && event.containsTimeslot(timeslot))
					event.addUnavailableLocalization(localization, timeslot);
	}
	
	/**
	 * Para cada categor�a que contenga la pista, elimina la invalidez de dicha localizaci�n
	 * 
	 * @param localization
	 */
	public void removeUnavailableLocalization(Localization localization) {
		for (Event event : events)
			if (event.containsLocalization(localization))
				event.removeUnavailableLocalization(localization);
	}
	
	/**
	 * Para cada categor�a que contenga la pista y el timeslot, elimina la invalidez de dicha localizaci�n
	 * 
	 * @param localization
	 */
	public void removeUnavailableLocalizationTimeslot(Localization localization, Timeslot timeslot) {
		for (Event event : events)
			if (event.containsLocalization(localization) && event.containsTimeslot(timeslot))
				event.removeUnavailableLocalizationTimeslot(localization, timeslot);
	}
	
	/**
	 * Muestra por la salida est�ndar una representaci�n de los horarios de cada categor�a
	 * 
	 * @param printMatches si es true se mostrar� un resumen de los partidos por cada categor�a, y si es false, no
	 */
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
					List<Match> matches = schedule.getMatches();
					for (Match match : matches)
						sb.append(match).append("\n");
				}
					
				sb.append("\n");
			}
		}
		
		System.out.println(sb.toString());
	}
	
	/**
	 * Llama a printCurrentSchedules(true), mostr�ndose por la salida est�ndar los horarios y los partidos
	 */
	public void printCurrentSchedules() {
		printCurrentSchedules(true);
	}
	
	/**
	 * @return n�mero de partidos que se juegan en el torneo
	 */
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
	
		System.out.println("0 Zarlon");
		System.out.println("1 Tournament");
		System.out.print("Choose tournament: ");
		int tournamentOption = sc.nextInt();
		
		Tournament t = null;
		switch (tournamentOption) {
			case 0:
				t = EventManager.getInstance().getZarlonTournament();
				break;
			case 1:
				t = EventManager.getInstance().getSampleTournament();
				break;
			default:
				t = EventManager.getInstance().getSampleTournament();
				break;
		}
		
		System.out.println("\n1 domOverWDeg");
		System.out.println("2 minDom_UB");
		System.out.println("3 minDom_LB");
		System.out.print("Choose Search Strategy: ");
		int searchStrategyOption = sc.nextInt();

		t.getSolver().setSearchStrategy(searchStrategyOption);
		
		
		final Tournament tournament = t;
		
		boolean printSolutions = true;
		boolean printMatches = true;
		boolean askForInput = false;
		boolean printMatchesByPlayer = false;
		int maxSolutions = 1; // 0 -> todas las soluciones
		int foundSolutions = 0;
		
		/*Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				System.out.print("Stop resolution process? (Y/N): ");
				String answer = sc.next();
				if (answer.equalsIgnoreCase("y")) {
					tournament.getSolver().stopResolutionProcess();
				}
			}
		});
		try {
			thread.start();
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}*/
		
		boolean solutionFound = tournament.solve();
		
		if (solutionFound) {
			do {
				if (printSolutions) {
					System.out.println("-------------------------------------------------------");
					System.out.println(tournament + "\n");
					tournament.printCurrentSchedules(printMatches);
					
					if (tournament.getSchedules() != null) {
						CombinedSchedule combinedSchedule = tournament.getSchedule();
					
						System.out.println("All schedules combined in one");
						System.out.println(combinedSchedule);
						
						combinedSchedule.calculateMatches();
						
						if (printMatches) {
							List<Match> matches = combinedSchedule.getMatches();
							System.out.println("All matches (" + matches.size() + ")");
							for (Match match : matches)
								System.out.println(match);
							System.out.println();
						}
						
						GroupedSchedule groupedSchedule = new GroupedSchedule(tournament, tournament.getSchedule().getMatches());
						System.out.println("Combined schedule grouped by courts");
						System.out.println(groupedSchedule);
						
						int occupation = groupedSchedule.getOccupation();
						int availableTimeslots = groupedSchedule.getAvailableTimeslots();
						System.out.println(
							String.format("Timeslot (%s) occupation: %s/%s (%s %%)\n",
								groupedSchedule.getTotalTimeslots(),
								occupation,
								availableTimeslots,
								(occupation / (double)availableTimeslots) * 100
							)
						);
						
						if (printMatchesByPlayer) {
							for (Player player : tournament.getAllPlayers()) {
								System.out.println(player + " matches:");
								for (Match match : combinedSchedule.getMatchesByPlayer(player))
									System.out.println(match);
								System.out.println();
							}
						}
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
