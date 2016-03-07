package solver;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;

public class EventSolver {
	Solver solver;
	
	// Número de soluciones que queremos que se muestren (0: todas las soluciones)
	int solutions = 1;
	
	// Número de partidos que debe jugar cada jugador
	int nMatchesPerPlayer = 1;
	
	// Número de horas (timeslots) que ocupa cada partido
	int nTimeslotsPerMatch = 2;
	
	String[] players = { "Novak", "Andy", "Roger", "Stan", "Rafael", "Kei", "Tomas", "David" };
	int[] courts = { 1, 2, 3 };
	int[] timeslots = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 };
	
	int nPlayers = players.length;
	int nCourts = courts.length;
	int nTimeslots = timeslots.length;
	
	// Timeslots en los que el jugador_i no está disponible
	int[][] unavailability = {
		{ 0, 1, 5 },			// Novak
		{ 8, 10, 11 },			// Andy
		{ 1, 2, 11 },			// Roger
		{ 0, 1 },				// Stan
		{ 2, 3, 4, 5, 6 },		// Rafael
		{ 3, 4, 9, 10, 11 },	// Kei
		{ 4, 5 },				// Tomas
		{ 2, 3 }				// David
	};
	
	// Horario. x_p,c,t -> horario_jugador,pista,hora. Dominio [0, 1]
	IntVar[][][] x;
	
	// Comienzos de partidos. Dominio [0, 1]
	IntVar[][][] g;
	
	// Pista está ocupada. Dominio [0, 1]
	IntVar[][] crt;
	
	public EventSolver(int nSolutions, int matches, int matchDuration) {
		solutions = nSolutions;
		nMatchesPerPlayer = matches;
		nTimeslotsPerMatch = matchDuration;
	}
	
	public void execute() {
		createSolver();
		buildModel();
		configureSearch();
		solve();
		output();
	}
	
	private void createSolver() {
		solver = new Solver("Tennis tournament");
	}
	
	private void buildModel() {
		x = new IntVar[nPlayers][nCourts][nTimeslots];
		g = new IntVar[nPlayers][nCourts][nTimeslots];
		
		for (int p = 0; p < nPlayers; p++) {
			// Dominio [0, 1]: 0 -> no juega, 1 -> juega
			x[p] = VariableFactory.boundedMatrix("Player" + p + "Schedule", nCourts, nTimeslots, 0, 1, solver);
			
			// Dominio [0, 1]: 0 -> el partido no empieza a esa hora, 1 -> el partido empieza a esa hora
			g[p] = VariableFactory.boundedMatrix("Player" + p + "GameStart", nCourts, nTimeslots, 0, 1, solver);
		}
		
		//for (int p = 0; p < nPlayers; p++)
			//unavailability[p] = new int[]{};
		
		setConstraintsPlayersUnavailable();
		
		setConstraintsMapMatchesBeginning();
		
		setConstraintsMapMatches();
		
		setConstraintsPlayersInCourt();
		
		setConstraintsPlayersNotSimultaneous();
		
		setConstraintsPlayersMatchesNumber();
	}
	
	private void setConstraintsPlayersUnavailable() {	
		for (int p = 0; p < nPlayers; p++) {
			// Para cada jugador marcar para cada pista en cada hora como 0 cuando no puede jugar
			for (int c = 0; c < nCourts; c++) {
				for (int t = 0; t < nTimeslots; t++) {
					if (isUnavailable(p, t)) {
						int nRange = nTimeslotsPerMatch;		
						if (t + 1 < nTimeslotsPerMatch)
							nRange -= nTimeslotsPerMatch - t - 1;
						
						// Si un jugador no está disponible en t, n podrá empezar un partido en el rango t..t-n
						// (siendo n la duración o número de timeslots de un partido)
						for (int i = 0; i < nRange; i++)
							solver.post(IntConstraintFactory.arithm(g[p][c][t - i], "=", VariableFactory.fixed(0, solver)));
						
						// Además, se marca con 0 las horas de la matriz de horario/partidos si el jugador no puede jugar
						solver.post(IntConstraintFactory.arithm(x[p][c][t], "=", VariableFactory.fixed(0, solver)));
					}
				}
			}
		}
	}
	
	private void setConstraintsMapMatchesBeginning() {
		// Mapear entre los comienzos de cada partido (g) y las horas en las que se juega
		for (int p = 0; p < nPlayers; p++) {
			for (int c = 0; c < nCourts; c++) {
				for (int t = 0; t < nTimeslots - nTimeslotsPerMatch; t++) {
					if (nTimeslotsPerMatch == 1) {
						// Si un partido dura un timeslot la matriz g es idéntica a la matriz x
						solver.post(IntConstraintFactory.arithm(g[p][c][t], "=", x[p][c][t]));
					} else {
						// Mapear g_p,c,t a partir del rango que g_p,c,t cubre en x, es decir,
						// [x_p,c,t, x_p,c,t+n] (n es el número de timeslots por partido).
						// En términos de operación booleana, a g_t se asignaría el valor [0, 1] a partir
						// de la operación "and" aplicada sobre ese rango en x correspondiente a g_t,
						// es decir, si todos los x en el rango son 1, entonces efectivamente el
						// partido empieza en g_t, luego se marca con 1. Si hay al menos un elemento
						// del rango en x que sea 0 quiere decir que ese rango no corresponde a un partido,
						// luego en g_t no empieza un partido y se marca como 0
						
						IntVar[] matchRange = new IntVar[nTimeslotsPerMatch];
						for (int i = 0; i < nTimeslotsPerMatch; i++)
							matchRange[i] = x[p][c][t + i];
						
						BoolVar matchTakesPlace = VariableFactory.bool("MatchTakesPlace", solver);
						solver.post(IntConstraintFactory.minimum(matchTakesPlace, matchRange));
						solver.post(IntConstraintFactory.arithm(g[p][c][t], "=", matchTakesPlace));
					}
				}
				if (nTimeslotsPerMatch == 1) {
					// Si un partido dura un timeslot la matriz g es idéntica a la matriz x
					solver.post(IntConstraintFactory.arithm(g[p][c][nTimeslots - 1], "=", x[p][c][nTimeslots - 1]));
				} else {
					// Si un partido dura más de un timeslot se marcan los últimos elementos de la matriz de comienzos de partidos (g)
					// con 0 para evitar que dé comienzo un partido que salga del rango del dominio de los timeslots por causa del
					// rango de la duración del propio partido
					for (int i = 0; i < nTimeslotsPerMatch - 1; i++)
						solver.post(IntConstraintFactory.arithm(g[p][c][nTimeslots - i - 1], "=", 0));
				}
			}
		}
	}
	
	private void setConstraintsMapMatches() {
		// Un partido no puede empezar en una hora t y una t + 1
		// Mapear x_p,c,t a partir de los posibles comienzos de partido (g) cuyo rango "cubre" x_t
		for (int p = 0; p < nPlayers; p++) {
			for (int c = 0; c < nCourts; c++) {
				for (int t = 0; t < nTimeslots; t++) {
					int nRange = nTimeslotsPerMatch;
					
					// para los nTimeslotsPerMatch primeros x que no se pueden mapear a nTimeslotsPerMatch elementos de g
					if (t + 1 < nTimeslotsPerMatch)
						nRange -= nTimeslotsPerMatch - t - 1;
					
					IntVar[] matchBeginningRange = new IntVar[nRange];
					for (int i = 0; i < nRange; i++)
						matchBeginningRange[i] = g[p][c][t - i];
					
					// La suma de ese posible rango de g, g_t-n..g_t (siendo n nTimeslotsPerMatch) únicamente
					// puede ser 0 o 1, es decir, que no empiece ningún partido o que empiece, pero nunca puede ser
					// mayor puesto que supondría que dos partidos se superpondrían
					IntVar matchStartSum = VariableFactory.bounded("MatchStartSum", 0, 1, solver);
					solver.post(IntConstraintFactory.sum(matchBeginningRange, matchStartSum));
					solver.post(IntConstraintFactory.arithm(x[p][c][t], "=", matchStartSum));
				}
			}
		}
	}
	
	private void setConstraintsPlayersInCourt() {
		// Puede haber 0 jugadores (no hay partido) o 2 (hay partido)
		crt = VariableFactory.enumeratedMatrix("OcuppiedCourts", nCourts, nTimeslots, new int[]{0, 2}, solver);
		
		for (int c = 0; c < nCourts; c++) {
			for (int t = 0; t < nTimeslots; t++) {
				// Las "participaciones" de todos los jugadores en la pista_c a la hora_t
				IntVar[] playerSum = new IntVar[nPlayers];
				for (int p = 0; p < nPlayers; p++)
					playerSum[p] = x[p][c][t];
				
				// Que la suma de las participaciones de todos los jugadores sea
				// igual a 0 o 2, es decir, que nadie juegue o que 2 jugadores jueguen
				solver.post(IntConstraintFactory.sum(playerSum, crt[c][t]));
			}
		}
	}
	
	private void setConstraintsPlayersNotSimultaneous() {
		for (int p = 0; p < nPlayers; p++) {
			for (int t = 0; t < nTimeslots; t++) {
				// Las posibles ocupaciones de pistas del jugador_p a la hora_t
				IntVar[] courtSum = new IntVar[nCourts];
				for (int c = 0; c < nCourts; c++)
					courtSum[c] = x[p][c][t];
				
				// Que la suma de las ocupaciones de todas las pistas por parte del
				// jugador_p a la hora_t sea <= 1, es decir, que el jugador no
				// juegue en más de una pista al mismo tiempo
				solver.post(IntConstraintFactory.sum(courtSum, "<=", VariableFactory.fixed(1, solver)));
			}
		}
	}
	
	private void setConstraintsPlayersMatchesNumber() {
		// Que cada jugador juegue nMatchesPerPlayer partidos
		for (int p = 0; p < nPlayers; p++)
			solver.post(IntConstraintFactory.sum(ArrayUtils.flatten(g[p]), VariableFactory.fixed(nMatchesPerPlayer, solver)));
	}
	
	private boolean isUnavailable(int player, int timeslot) {
		for (int t = 0; t < unavailability[player].length; t++)
			if (unavailability[player][t] == timeslot)
				return true;
		return false;
	}
	
	private void configureSearch() {
		//solver.set(IntStrategyFactory.minDom_UB(ArrayUtils.flatten(x)));
		//solver.set(IntStrategyFactory.minDom_LB(ArrayUtils.flatten(x)));
		solver.set(IntStrategyFactory.domOverWDeg(ArrayUtils.flatten(x), 0));
	}
	
	private void solve() {
		solver.findSolution();
	}
	
	private void output() {
		StringBuilder sb = new StringBuilder("Tennis tournament\n\n");
		
		if (solver.isFeasible() == ESat.TRUE) {
			int nSolutions = 0;
			boolean showMatrices = false;
			
			do {
				sb.append("Matches:\n");
				
				if (showMatrices) {
					for (int p = 0; p < nPlayers; p++) {
						sb.append(String.format("Jugador %d (%s):\n", p, players[p]));
						for (int c = 0; c < nCourts; c++) {
							for (int t = 0; t < nTimeslots; t++) {
								sb.append(String.format("%d ", x[p][c][t].getValue()));
							}
							sb.append("\n");
						}
						sb.append("\n");
						
						for (int c = 0; c < nCourts; c++) {
							for (int t = 0; t < nTimeslots; t++) {
								sb.append(String.format("%d ", g[p][c][t].getValue()));
							}
							sb.append("\n");
						}
						sb.append("\n\n");
					}
				}
				
				int[][] schedule = getSchedule();
				
				sb.append(String.format("%8s", " "));
				for (int t = 0; t < nTimeslots; t++)
					sb.append(String.format("%4s", "t" + t));
				sb.append("\n");
				
				for (int p = 0; p < nPlayers; p++) {
					sb.append(String.format("%8s", players[p]));
					for (int t = 0; t < nTimeslots; t++)
						sb.append(String.format("%4s", getMatchStringValue(schedule[p][t])));
					sb.append("\n");
				}
				
				sb.append("\n");
				nSolutions++;
				
				if (solutions > 0 && nSolutions >= solutions)
					break;
			} while (solver.nextSolution());
			
			sb.append(String.format("%d solutions were found.\n", nSolutions));
		} else {
			sb.append("Problem infeasible.");
		}
		
		System.out.println(sb.toString());
	}
	
	/**
	 * Método para ayudar al formateo de la solución
	 * 
	 * @param  matchVal		valor del partido (ver significado en getSchedule())
	 * @return string		cadena con la representación del valor del partido donde
	 *     ~:	 el jugador no está disponible en esa hora
	 *     -:    el jugador no juega a esa hora
	 *  1..n:    el jugador juega en esa pista (n es la última pista)
	 */
	private String getMatchStringValue(int matchVal) {
		String match = String.valueOf(matchVal);
		if (matchVal == -1)
			match = "~";
		else if (matchVal == 0)
			match = "-";
		return match;
	}
	
	/**
	 * Método que devuelve un horario final con las pistas donde juega cada
	 * jugador a cada hora, a partir de la matriz 3D de Choco con la solución
	 * 
	 * @return int[][] con valores -1..n donde
	 * 	  -1:	 el jugador no está disponible en esa hora
	 * 	   0:    el jugador no juega a esa hora
	 * 	1..n:    el jugador juega en esa pista (n es la última pista)
	 */
	private int[][] getSchedule() {
		int[][] schedule = new int[nPlayers][nTimeslots];
		
		for (int p = 0; p < nPlayers; p++) {
			for (int t = 0; t < nTimeslots; t++) {
				if (isUnavailable(p, t))
					schedule[p][t] = -1;
				else {
					schedule[p][t] = 0;
					
					for (int c = 0; c < nCourts; c++)
						if (x[p][c][t].getValue() == 1)
							schedule[p][t] = c + 1; // para que las pistas estén numeradas a partir de 1
				}
			}
		}
		
		return schedule;
	}
	
	public static void main(String[] args) {
		int s = 5; // número de soluciones a mostrar
		int n = 1; // número de partidos que cada jugador ha de jugar
		int m = 2; // número de horas (timeslots) que dura cada partido
		
		new EventSolver(s, n, m).execute();
	}
}