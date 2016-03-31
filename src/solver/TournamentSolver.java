package solver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.trace.Chatterbox;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;

import models.Event;
import models.Localization;
import models.Player;
import models.Timeslot;
import models.Tournament;
import models.schedules.EventSchedule;

public class TournamentSolver {
	private Solver solver;
	
	private Tournament tournament;
	private Event[] events;
	
	private int nCategories;
	
	private List<Player> allPlayers;
	private List<Localization> allCourts;
	private List<Timeslot> allTimeslots;
	
	private int[] nPlayers;
	private int[] nCourts;
	private int[] nTimeslots;
	
	// Timeslots en los que el jugador_i no está disponible (para cada categoría)
	private int[][][] unavailability;
	
	// Número de partidos que debe jugar cada jugador (para cada categoría)
	private int[] nMatchesPerPlayer;
	
	// Número de horas (timeslots) que ocupa cada partido (para cada categoría)
	private int[] nTimeslotsPerMatch;
	
	// Número de jugadores por partido (lo normal será 2) (para cada categoría)
	private int[] nPlayersPerMatch;
	
	
	// Horario. x_e,p,c,t -> horario_categoria,jugador,pista,hora. Dominio [0, 1]
	private IntVar[][][][] x;
	
	// Comienzos de partidos. Dominio [0, 1]
	private IntVar[][][][] g;
	
	
	// Horarios calculados de la solución actual
	private EventSchedule[] schedules;
	
	// Índices de cada jugador en el array de jugadores correspondiente a cada categoría
	// p.e. playersIndices[2][1] = 3 significa que el índice del jugador número 3 en
	// la categoría número 2 es 3 (si el jugador no existe en la categoría, el índice es -1)
	private int[][] playersIndices;
	
	// Índices de cada timeslot en el array de timeslots correspondiente a cada categoría
	private int[][] timeslotsIndices;
	
	// Índices de cada pista en el array de localidades de juego (pistas) correspondiente a cada categoría
	private int[][] courtsIndices;
	
	private boolean lastSolutionFound = false;
	
	// Opción de estrategia de búsqueda
	private int searchStrategyOption = 1;
	
	public TournamentSolver(Tournament tournament) {
		this.tournament = tournament;
		events = tournament.getEvents();
		
		nCategories = events.length;
		
		allPlayers = tournament.getAllPlayers();
		allTimeslots = tournament.getAllTimeslots();
		allCourts = tournament.getAllLocalizations();
		
		nPlayers = new int[nCategories];
		nCourts = new int[nCategories];
		nTimeslots = new int[nCategories];
		
		unavailability = new int[nCategories][][];
		
		nMatchesPerPlayer = new int[nCategories];
		nTimeslotsPerMatch = new int[nCategories];
		nPlayersPerMatch = new int[nCategories];
		
		for (int i = 0; i < nCategories; i++) {
			nPlayers[i] = events[i].getNumberOfPlayers();
			nCourts[i] = events[i].getNumberOfLocalizations();
			nTimeslots[i] = events[i].getNumberOfTimeslots();
			
			unavailability[i] = events[i].getUnavailableTimeslotsAs2DIntArray();
			
			nMatchesPerPlayer[i] = events[i].getMatchesPerPlayer();
			nTimeslotsPerMatch[i] = events[i].getMatchDuration();
			nPlayersPerMatch[i] = events[i].getPlayersPerMatch();
		}

		x = new IntVar[nCategories][][][];
		g = new IntVar[nCategories][][][];
		
		playersIndices = new int[allPlayers.size()][nCategories];
		for (int i = 0; i < allPlayers.size(); i++) {
			for (int e = 0; e < nCategories; e++) {
				Player[] eventPlayers = events[e].getPlayers();
				Player player = allPlayers.get(i);
				
				for (int j = 0; j < eventPlayers.length; j++) {
					if (player.equals(eventPlayers[j])) {
						playersIndices[i][e] = j;
						break;
					}
					playersIndices[i][e] = -1;
				}
			}
		}
		
		timeslotsIndices = new int[allTimeslots.size()][nCategories];
		for (int i = 0; i < allTimeslots.size(); i++) {
			for (int e = 0; e < nCategories; e++) {
				Timeslot[] eventTimeslots = events[e].getTimeslots();
				Timeslot timeslot = allTimeslots.get(i);
				
				for (int j = 0; j < eventTimeslots.length; j++) {
					if (timeslot.equals(eventTimeslots[j])) {
						timeslotsIndices[i][e] = j;
						break;
					}
					timeslotsIndices[i][e] = -1;
				}
			}
		}
		
		courtsIndices = new int[allCourts.size()][nCategories];
		for (int i = 0; i < allCourts.size(); i++) {
			for (int e = 0; e < nCategories; e++) {
				Localization[] eventCourts = events[e].getLocalizations();
				Localization court = allCourts.get(i);
				
				for (int j = 0; j < eventCourts.length; j++) {
					if (court.equals(eventCourts[j])) {
						courtsIndices[i][e] = j;
						break;
					}
					courtsIndices[i][e] = -1;
				}
			}
		}
	}
	
	public void setSearchStrategy(int option) {
		searchStrategyOption = option;
	}
	
	public void execute() {
		createSolver();
		buildModel();
		configureSearch(searchStrategyOption);
		solve();
	}
	
	private void createSolver() {
		solver = new Solver("Tournament Solver");
	}
	
	private void buildModel() {
		for (int i = 0; i < events.length; i++) {
			x[i] = new IntVar[nPlayers[i]][nCourts[i]][nTimeslots[i]];
			g[i] = new IntVar[nPlayers[i]][nCourts[i]][nTimeslots[i]];
		}
		
		for (int e = 0; e < nCategories; e++) {
			for (int p = 0; p < nPlayers[e]; p++) {
				// Dominio [0, 1]: 0 -> no juega, 1 -> juega
				x[e][p] = VariableFactory.boundedMatrix("Player" + p + "Schedule", nCourts[e], nTimeslots[e], 0, 1, solver);
				
				// Dominio [0, 1]: 0 -> el partido no empieza a esa hora, 1 -> el partido empieza a esa hora
				g[e][p] = VariableFactory.boundedMatrix("Player" + p + "GameStart", nCourts[e], nTimeslots[e], 0, 1, solver);
			}
		}
		
		setConstraintsPredefineMatchups();
		
		setConstraintsMatchesSum();
		
		setConstraintsPlayersUnavailable();
		
		setConstraintsBreaks();
		
		setConstraintsMapMatchesBeginning();
		
		setConstraintsMapMatches();
		
		setConstraintsPlayersInCourtsForEachCategory();
		
		if (events.length > 1)
			setConstraintsCourtsCollisions();
		
		setConstraintsPlayersNotSimultaneous();
		
		setConstraintsPlayersMatchesNumber();
	}
	
	/**
	 * Emparejamientos por sorteo: predefine los jugadores que compondrán un partido. Esto tiene dos consecuencias:
	 * 1. El cálculo del horario es mucho más rápido porque la restricción es más fuerte
	 * 2. Se reducen las posibles combinaciones de horarios porque ya no puede ser emparejado cualquier jugador con cualquier otro
	 * 2a. Puede provocar que no se encuentren soluciones
	 */
	private void setConstraintsPredefineMatchups() {
		Map<Event, List<List<Player>>> predefinedMatchups = new HashMap<Event, List<List<Player>>>();
		for (Event event : events) {
			// Si el evento se organiza por sorteo
			if (event.getRandomDrawings()) {
				List<Player> players = new ArrayList<Player>(Arrays.asList(event.getPlayers()));
				List<List<Player>> matchups = new ArrayList<List<Player>>(event.getNumberOfMatches());
				
				Random random = new Random();
				int nPlayersPerMatch = event.getPlayersPerMatch();
				
				while (!players.isEmpty()) {
					List<Player> matchup = new ArrayList<Player>(nPlayersPerMatch);
					
					for (int i = 0; i < nPlayersPerMatch; i++) {
						int randIndex = random.ints(0, players.size()).findFirst().getAsInt();
						
						matchup.add(players.get(randIndex));
						
						players.remove(randIndex);
					}
					
					matchups.add(matchup);
				}
					
				predefinedMatchups.put(event, matchups);
			}
		}
		
		/*System.out.println("PREDEFINED MATCHUPS:");
		for (Event e : predefinedMatchups.keySet()) {
			System.out.println(e);
			for (List<Player> matchup : predefinedMatchups.get(e)) {
				for (Player p : matchup)
					System.out.print(p + " ");
				System.out.println();
			}
			System.out.println();
		}*/
		
		for (Event event : predefinedMatchups.keySet()) {
			int e = getEventIndex(event);
			int nPlayersInMatch = event.getPlayersPerMatch();
			
			List<List<Player>> matchups = predefinedMatchups.get(event);
			
			for (List<Player> matchup : matchups) {
				int[] pIndex = new int[nPlayersInMatch];
				
				for (int p = 0; p < nPlayersInMatch; p++)
					pIndex[p] = event.getPlayerIndex(matchup.get(p));
				
				for (int c = 0; c < nCourts[e]; c++) {
					for (int t = 0; t < nTimeslots[e]; t++) {
						for (int p = 0; p < nPlayersInMatch - 1; p++)
							solver.post(IntConstraintFactory.arithm(x[e][pIndex[p]][c][t], "=", x[e][pIndex[p + 1]][c][t]));
					}
				}
			}
		}
	}
	
	private void setConstraintsMatchesSum() {
		for (int e = 0; e < nCategories; e++) {
			int eventNumberOfMatches = nPlayers[e] * nMatchesPerPlayer[e];
			
			solver.post(IntConstraintFactory.sum(ArrayUtils.flatten(g[e]), VariableFactory.fixed(eventNumberOfMatches, solver)));
			solver.post(IntConstraintFactory.sum(ArrayUtils.flatten(x[e]), VariableFactory.fixed(eventNumberOfMatches * nTimeslotsPerMatch[e], solver)));
		}
	}
	
	private void setConstraintsPlayersUnavailable() {
		for (int e = 0; e < nCategories; e++) {
			for (int p = 0; p < nPlayers[e]; p++) {
				// Para cada jugador marcar para cada pista en cada hora como 0 cuando no puede jugar
				for (int c = 0; c < nCourts[e]; c++) {
					for (int t = 0; t < nTimeslots[e]; t++) {
						if (isUnavailable(e, p, t)) {
							int nRange = nTimeslotsPerMatch[e];		
							if (t + 1 < nTimeslotsPerMatch[e])
								nRange -= nTimeslotsPerMatch[e] - t - 1;
							
							// Si un jugador no está disponible en t, n podrá empezar un partido en el rango t..t-n
							// (siendo n la duración o número de timeslots de un partido)
							for (int i = 0; i < nRange; i++)
								solver.post(IntConstraintFactory.arithm(g[e][p][c][t - i], "=", VariableFactory.fixed(0, solver)));
							
							// Además, se marca con 0 las horas de la matriz de horario/partidos si el jugador no puede jugar
							solver.post(IntConstraintFactory.arithm(x[e][p][c][t], "=", VariableFactory.fixed(0, solver)));
						}
					}
				}
			}
		}
	}
	
	/**
	 * Define las restricciones para los timeslots que pertenecen a un break (período en el que no se juega)
	 */
	private void setConstraintsBreaks() {
		for (int e = 0; e < nCategories; e++) {
			for (int t = 0; t < nTimeslots[e]; t++) {
				// Si el timeslot_t es un break, entonces en él no se puede jugar y se marca como 0
				if (events[e].getTimeslotAt(t).getIsBreak()) {
					for (int p = 0; p < nPlayers[e]; p++) {
						for (int c = 0; c < nCourts[e]; c++) {
							solver.post(IntConstraintFactory.arithm(x[e][p][c][t], "=", VariableFactory.fixed(0, solver)));
							solver.post(IntConstraintFactory.arithm(g[e][p][c][t], "=", VariableFactory.fixed(0, solver)));
						}
					}
				}
			}
		}
	}
	
	private void setConstraintsMapMatchesBeginning() {
		// Mapear entre los comienzos de cada partido (g) y las horas en las que se juega
		for (int e = 0; e < nCategories; e++) {
			for (int p = 0; p < nPlayers[e]; p++) {
				for (int c = 0; c < nCourts[e]; c++) {
					for (int t = 0; t < nTimeslots[e] - nTimeslotsPerMatch[e]; t++) {
						if (nTimeslotsPerMatch[e] == 1) {
							// Si un partido dura un timeslot la matriz g es idéntica a la matriz x
							solver.post(IntConstraintFactory.arithm(g[e][p][c][t], "=", x[e][p][c][t]));
						} else {
							// Mapear g_e,p,c,t a partir del rango que g_e,p,c,t cubre en x, es decir,
							// [x_e,p,c,t, x_e,p,c,t+n] (n es el número de timeslots por partido).
							// En términos de operación booleana, a g_t se asignaría el valor [0, 1] a partir
							// de la operación "and" aplicada sobre ese rango en x correspondiente a g_t,
							// es decir, si todos los x en el rango son 1, entonces efectivamente el
							// partido empieza en g_t, luego se marca con 1. Si hay al menos un elemento
							// del rango en x que sea 0 quiere decir que ese rango no corresponde a un partido,
							// luego en g_t no empieza un partido y se marca como 0
							
							IntVar[] matchRange = new IntVar[nTimeslotsPerMatch[e]];
							for (int i = 0; i < nTimeslotsPerMatch[e]; i++)
								matchRange[i] = x[e][p][c][t + i];
							
							BoolVar matchTakesPlace = VariableFactory.bool("MatchTakesPlace", solver);
							solver.post(IntConstraintFactory.minimum(matchTakesPlace, matchRange));
							solver.post(IntConstraintFactory.arithm(g[e][p][c][t], "=", matchTakesPlace));
						}
					}
					if (nTimeslotsPerMatch[e] == 1) {
						// Si un partido dura un timeslot la matriz g es idéntica a la matriz x
						solver.post(IntConstraintFactory.arithm(g[e][p][c][nTimeslots[e] - 1], "=", x[e][p][c][nTimeslots[e] - 1]));
					} else {
						// Si un partido dura más de un timeslot se marcan los últimos elementos de la matriz de comienzos de partidos (g)
						// con 0 para evitar que dé comienzo un partido que salga del rango del dominio de los timeslots por causa del
						// rango de la duración del propio partido
						for (int i = 0; i < nTimeslotsPerMatch[e] - 1; i++)
							solver.post(IntConstraintFactory.arithm(g[e][p][c][nTimeslots[e] - i - 1], "=", 0));
					}
				}
			}
		}
	}
	
	private void setConstraintsMapMatches() {
		// Mapear x_e,p,c,t a partir de los posibles comienzos de partido (g) cuyo rango "cubre" x_t
		for (int e = 0; e < nCategories; e++) {
			for (int p = 0; p < nPlayers[e]; p++) {
				for (int c = 0; c < nCourts[e]; c++) {
					for (int t = 0; t < nTimeslots[e]; t++) {
						int nRange = nTimeslotsPerMatch[e];
						
						// para los nTimeslotsPerMatch primeros x que no se pueden mapear a nTimeslotsPerMatch elementos de g
						if (t + 1 < nTimeslotsPerMatch[e])
							nRange -= nTimeslotsPerMatch[e] - t - 1;
						
						IntVar[] matchBeginningRange = new IntVar[nRange];
						for (int i = 0; i < nRange; i++)
							matchBeginningRange[i] = g[e][p][c][t - i];
						
						// La suma de ese posible rango de g, g_t-n..g_t (siendo n nTimeslotsPerMatch) únicamente
						// puede ser 0 o 1, es decir, que no empiece ningún partido o que empiece, pero nunca puede ser
						// mayor puesto que supondría que dos partidos se superpondrían
						IntVar matchStartSum = VariableFactory.bounded("MatchStartSum", 0, 1, solver);
						solver.post(IntConstraintFactory.sum(matchBeginningRange, matchStartSum));
						solver.post(IntConstraintFactory.arithm(x[e][p][c][t], "=", matchStartSum));
					}
				}
			}
		}
	}
	
	private void setConstraintsPlayersInCourtsForEachCategory() {
		for (int e = 0; e < nCategories; e++) {
			for (int c = 0; c < nCourts[e]; c++) {
				for (int t = 0; t < nTimeslots[e]; t++) {
					// Las "participaciones" de todos los jugadores en la pista_c a la hora_t
					IntVar[] playerSum = new IntVar[nPlayers[e]];
					for (int p = 0; p < nPlayers[e]; p++)
						playerSum[p] = x[e][p][c][t];
					
					// Que la suma de las participaciones de todos los jugadores sea
					// igual a 0 o el número de jugadores por partido, es decir, que nadie juegue o que jueguen
					// el número de jugadores requeridos por partido
					solver.post(IntConstraintFactory.sum(playerSum, VariableFactory.enumerated("Sum", new int[]{0, nPlayersPerMatch[e]}, solver)));
				}
			}
		}
	}
	
	private void setConstraintsCourtsCollisions() {
		Map<Integer, List<Event>> eventsByNumberOfPlayersPerMatch = tournament.groupEventsByNumberOfPlayersPerMatch();
		
		// Posibles números de jugadores que componen un partido del torneo (incluye 0)
		int[] allPossibleNumberOfPlayers = getAllPosibleNumberOfPlayersPerMatchArray(eventsByNumberOfPlayersPerMatch);
		
		int nAllCourts = allCourts.size();
		int nAllTimeslots = allTimeslots.size();
		
		// Para cada pista del torneo explorar las participaciones de jugadores en cada categoría
		// y controlar que no se juegue más de un partido en una pista a la misma hora
		for (int c = 0; c < nAllCourts; c++) {
			for (int t = 0; t < nAllTimeslots; t++) {
				// Las posibles ocupaciones de los jugadores de la pista_c a la hora_t
				List<IntVar> playerSum = new ArrayList<IntVar>();
				
				for (int e = 0; e < nCategories; e++)
					// Si en el evento_e se puede jugar en la pista_c y a la hora_t
					if (courtsIndices[c][e] != -1 && timeslotsIndices[t][e] != -1)
						for (int p = 0; p < nPlayers[e]; p++)
							playerSum.add(x[e][p][courtsIndices[c][e]][timeslotsIndices[t][e]]);
				
				// Que la suma de las participaciones sea o 0 (no se juega en la pista_c a la hora_t)
				// o cualquier valor del conjunto de número de jugadores por partido (cada evento tiene el suyo)
				solver.post(IntConstraintFactory.sum(
					(IntVar[]) playerSum.toArray(new IntVar[playerSum.size()]),
					VariableFactory.enumerated("PossibleNumberOfPlayersPerMatch", allPossibleNumberOfPlayers, solver))
				);
			}
		}
		
		// Caso excepcional: puede ocurrir que se cumpla la condición de que la suma de las participaciones de
		// jugadores en la pista_c a la hora_t sea una de las posibilidades, pero aún así sea una combinación inválida
		// Por ejemplo: en un torneo con 2 categorías individuales (partidos de 2 jugadores) y 1 categoría de dobles
		// (partidos de 4 jugadores), puede ocurrir que la suma de las participaciones sea 4, con lo cual según
		// la restricción definida es correcto, pero no porque haya un partido de dobles, sino porque hay
		// 2 partidos individuales, con lo cual sumarían participaciones de jugadores 2+2=4. Además, la restricción
		// de jugadores para cada categoría (método setConstraintsPlayersInCourtsForEachCategory) se cumpliría
		// porque el númeo de jugadores por partido para las 2 categorías individuales sería 2, y para la categoría
		// de dobles sería 0.
		// Solución: forzar que la suma de las participaciones en las categorías con el mismo número de jugadores
		// por partido sea o 0 o el número de jugadores por partido de esa categoría
		
		// Por cada conjunto de categorías con el mismo número de jugadores por partido, la suma de participaciones
		// de todos los jugadores en una pista_c a una hora_t es 0 o el número de jugadores por partido
		for (Integer numberOfPlayersPerMatch : eventsByNumberOfPlayersPerMatch.keySet()) {
			for (int c = 0; c < nAllCourts; c++) {
				for (int t = 0; t < nAllTimeslots; t++) {
					// Las posibles ocupaciones de los jugadores de la pista_c a la hora_t
					List<IntVar> playerSum = new ArrayList<IntVar>();
					
					List<Event> eventList = eventsByNumberOfPlayersPerMatch.get(numberOfPlayersPerMatch);
					for (Event event : eventList) {
						int e = getEventIndex(event);
						
						// Si en el evento_e se puede jugar en la pista_c y a la hora_t
						if (courtsIndices[c][e] != -1 && timeslotsIndices[t][e] != -1)
							for (int p = 0; p < nPlayers[e]; p++)
								playerSum.add(x[e][p][courtsIndices[c][e]][timeslotsIndices[t][e]]);
					}
					
					// Que la suma de las participaciones sea o 0 (no se juega en la pista_c a la hora_t)
					// o el número de jugadores por partido (de este conjunto de categorías con el mismo número)
					solver.post(IntConstraintFactory.sum(
						(IntVar[]) playerSum.toArray(new IntVar[playerSum.size()]),
						VariableFactory.enumerated("PossibleNumberOfPlayersPerMatch", new int[]{ 0, numberOfPlayersPerMatch }, solver))
					);
				}
			}
		}
	}
	
	private void setConstraintsPlayersNotSimultaneous() {
		int nAllPlayers = allPlayers.size();
		int nAllTimeslots = allTimeslots.size();
		
		// Para cada jugador del torneo explorar las participaciones en cada categoría y
		// controlar colisiones que puedan producirse (mismo jugador, mismo timeslot)
		for (int p = 0; p < nAllPlayers; p++) {
			for (int t = 0; t < nAllTimeslots; t++) {
				// Las posibles ocupaciones de pistas del jugador_p a la hora_t
				List<IntVar> courtSum = new ArrayList<IntVar>();
				
				for (int e = 0; e < nCategories; e++)
					// Si el jugador_p juega en la categoría_e a la hora_t
					if (playersIndices[p][e] != -1 && timeslotsIndices[t][e] != -1)
						for (int c = 0; c < nCourts[e]; c++) 
							courtSum.add(x[e][playersIndices[p][e]][c][timeslotsIndices[t][e]]);
				
				// Que la suma de las ocupaciones de todas las pistas por parte del
				// jugador_p a la hora_t sea o 0 (no juega a la hora_t) o 1 (el jugador
				// juega a la hora_t en una de las pistas en una de las categorías)
				solver.post(IntConstraintFactory.sum(
					(IntVar[]) courtSum.toArray(new IntVar[courtSum.size()]),
					VariableFactory.enumerated("PossibleParticipations", new int[]{ 0, 1 }, solver))
				);
			}
		}
	}
	
	private void setConstraintsPlayersMatchesNumber() {
		// Que cada jugador juegue nMatchesPerPlayer partidos
		for (int e = 0; e < nCategories; e++)
			for (int p = 0; p < nPlayers[e]; p++)
				solver.post(IntConstraintFactory.sum(ArrayUtils.flatten(g[e][p]), VariableFactory.fixed(nMatchesPerPlayer[e], solver)));
	}
	
	private boolean isUnavailable(int category, int player, int timeslot) {
		for (int t = 0; t < unavailability[category][player].length; t++)
			if (unavailability[category][player][t] == timeslot)
				return true;
		return false;
	}
	
	private int[] getAllPosibleNumberOfPlayersPerMatchArray(Map<Integer, List<Event>> eventsByNumberOfPlayersPerMatch) {		
		Integer[] keysArray = eventsByNumberOfPlayersPerMatch.keySet().toArray(new Integer[eventsByNumberOfPlayersPerMatch.keySet().size()]);
		
		int[] array = new int[keysArray.length + 1];
		array[0] = 0;
		for (int i = 1; i < array.length; i++)
			array[i] = keysArray[i - 1];
		
		return array;
	}
	
	private int getEventIndex(Event event) {
		for (int i = 0; i < events.length; i++)
			if (events[i].equals(event))
				return i;
		return -1;
	}
	
	private void configureSearch(int option) {	
		IntVar[][][] vars = new IntVar[nCategories][][];
		for (int i = 0; i < nCategories; i++)
			vars[i] = ArrayUtils.flatten(x[i]);
		
		IntVar[] v = ArrayUtils.flatten(vars);
		
		switch (option) {
			case 1:
				solver.set(IntStrategyFactory.domOverWDeg(v, System.currentTimeMillis()));
				break;
			case 2:
				solver.set(IntStrategyFactory.minDom_UB(v));
				break;
			case 3:
				solver.set(IntStrategyFactory.minDom_LB(v));
				break;
			default:
				solver.set(IntStrategyFactory.domOverWDeg(v, System.currentTimeMillis()));
				break;
		}
	}
	
	private void solve() {
		//SearchMonitorFactory.limitTime(solver, 10000);
		if (solver.findSolution())
			Chatterbox.printStatistics(solver);
	}
	
	public EventSchedule[] getSchedules() {
		if (lastSolutionFound && schedules != null)
			schedules = null;
		
		else if (solver.isFeasible() != ESat.TRUE) {
			//System.out.println("Problem infeasible.");
			schedules = null;
			
			boolean hasRandomDrawings = false;
			for (Event event : events)
				if (event.getRandomDrawings()) {
					//System.out.println("Trying new resolution with different random drawings.\n");
					hasRandomDrawings = true;
					break;
				}
			
			if (hasRandomDrawings) {
				try {
					execute();
				} catch (StackOverflowError e) {
					return null;
				}
			
				return getSchedules();
			}
			
		} else if (schedules == null) {
			schedules = new EventSchedule[nCategories];
			buildSchedules();
		} else {
			if (solver.nextSolution())
				buildSchedules();
			else {
				lastSolutionFound = true;
				schedules = null;
			}
		}
		return schedules;
	}
	
	private void buildSchedules() {
		for (int i = 0; i < nCategories; i++)
			schedules[i] = new EventSchedule(events[i], x[i]);
	}
}