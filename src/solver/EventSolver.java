package solver;

import java.util.ArrayList;
import java.util.List;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;

import manager.Tournament;
import models.Event;
import models.Player;
import models.Schedule;
import models.Timeslot;

public class EventSolver {
	private Solver solver;
	
	private Event[] events;
	
	private int nCategories;
	
	private List<Player> allPlayers;
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
	
	// Pista está ocupada. Dominio [0, 1]
	private IntVar[][][] crt;
	
	
	// Horarios calculados de la solución actual
	private Schedule[] schedules;
	
	// Índices de cada jugador en el array de jugadores correspondiente a cada categoría
	// p.e. playersIndices[2][1] = 3 significa que el índice del jugador número 3 en
	// la categoría número 2 es 3 (si el jugador no existe en la categoría, el índice es -1)
	private int[][] playersIndices;
	
	// Índices de cada timeslot en el array de timeslots correspondiente a cada categoría
	private int[][] timeslotsIndices;
	
	private boolean lastSolutionFound = false;
	
	public EventSolver(Tournament tournament) {
		events = tournament.getEvents();
		
		nCategories = events.length;
		
		allPlayers = tournament.getAllPlayers();
		allTimeslots = tournament.getAllTimeslots();
		
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
		
		crt = new IntVar[nCategories][][];
		
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
		/*
		System.out.println("Players indices");
		for (int i = 0; i < playersIndices.length; i++) {
			for (int j = 0; j < playersIndices[i].length; j++)
				System.out.print(String.format("%3s", playersIndices[i][j]));
			System.out.println();
		}
		
		System.out.println("Timeslots indices");
		for (int i = 0; i < timeslotsIndices.length; i++) {
			for (int j = 0; j < timeslotsIndices[i].length; j++)
				System.out.print(String.format("%3s", timeslotsIndices[i][j]));
			System.out.println();
		}
		*/
	}
	
	public void execute() {
		createSolver();
		buildModel();
		configureSearch();
		solve();
	}
	
	private void createSolver() {
		solver = new Solver("Tennis tournament");
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
		
		setConstraintsPlayersUnavailable();
		
		setConstraintsBreaks();
		
		setConstraintsMapMatchesBeginning();
		
		setConstraintsMapMatches();
		
		setConstraintsPlayersInCourt();
		
		setConstraintsPlayersNotSimultaneous();
		
		setConstraintsPlayersMatchesNumber();
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
	
	private void setConstraintsPlayersInCourt() {
		for (int e = 0; e < nCategories; e++) {
			// Puede haber 0 jugadores (no hay partido) o nPlayersPerMatch (hay partido)
			crt[e] = VariableFactory.enumeratedMatrix("OcuppiedCourts", nCourts[e], nTimeslots[e], new int[]{0, nPlayersPerMatch[e]}, solver);
			
			for (int c = 0; c < nCourts[e]; c++) {
				for (int t = 0; t < nTimeslots[e]; t++) {
					// Las "participaciones" de todos los jugadores en la pista_c a la hora_t
					IntVar[] playerSum = new IntVar[nPlayers[e]];
					for (int p = 0; p < nPlayers[e]; p++)
						playerSum[p] = x[e][p][c][t];
					
					// Que la suma de las participaciones de todos los jugadores sea
					// igual a 0 o el número de jugadores por partido, es decir, que nadie juegue o que jueguen
					// el número de jugadores requeridos por partido
					solver.post(IntConstraintFactory.sum(playerSum, crt[e][c][t]));
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
				
				for (int e = 0; e < nCategories; e++) {
					// Si el jugador_p juega en la categoría_e a la hora_t
					if (playersIndices[p][e] != -1 && timeslotsIndices[t][e] != -1) {
						for (int c = 0; c < nCourts[e]; c++)
							courtSum.add(x[e][playersIndices[p][e]][c][timeslotsIndices[t][e]]);
					}
				}
				
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
	
	private void configureSearch() {	
		IntVar[][][] concatX = new IntVar[nCategories][][];
		for (int i = 0; i < nCategories; i++)
			concatX[i] = ArrayUtils.flatten(x[i]);
		
		//solver.set(IntStrategyFactory.minDom_UB(ArrayUtils.flatten(x)));
		//solver.set(IntStrategyFactory.minDom_LB(ArrayUtils.flatten(x)));
		solver.set(IntStrategyFactory.domOverWDeg(ArrayUtils.flatten(concatX), 0));
	}
	
	private void solve() {
		solver.findSolution();
	}
	
	public Schedule[] getSchedules() {
		if (lastSolutionFound)
			schedules = null;
		
		else if (solver.isFeasible() != ESat.TRUE) {
			System.out.println("Problem infeasible.");
			schedules = null;
			
		} else if (schedules == null) {
			schedules = new Schedule[nCategories];
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
			schedules[i] = new Schedule(events[i], x[i]);
	}
}