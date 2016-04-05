package solver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.search.loop.monitors.SearchMonitorFactory;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.trace.Chatterbox;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;

import models.tournaments.Tournament;
import models.tournaments.events.Event;
import models.tournaments.events.entities.Localization;
import models.tournaments.events.entities.Player;
import models.tournaments.events.entities.Team;
import models.tournaments.events.entities.Timeslot;
import models.tournaments.schedules.EventSchedule;

public class TournamentSolver {
	private Solver solver;
	
	private Tournament tournament;
	private Event[] events;
	
	private int nCategories;
	
	private List<Player> allPlayers;
	private List<Localization> allLocalizations;
	private List<Timeslot> allTimeslots;
	
	private int[] nPlayers;
	private int[] nLocalizations;
	private int[] nTimeslots;
	
	// Timeslots en los que el jugador_i no está disponible (para cada categoría)
	private int[][][] unavailability;
	
	// Número de partidos que debe jugar cada jugador (para cada categoría)
	private int[] nMatchesPerPlayer;
	
	// Número de horas (timeslots) que ocupa cada partido (para cada categoría)
	private int[] nTimeslotsPerMatch;
	
	// Número de jugadores por partido (lo normal será 2) (para cada categoría)
	private int[] nPlayersPerMatch;
	
	// Emparejamientos predefinidos (para cada categoría)
	private Map<Event, List<Set<Player>>> fixedMatchups;
	
	/**
	 * Por categoría, diccionario de jugadores para cada cual los enfrentamientos de los que forme parte han de tener lugar
	 * en cualquiera de las localizaciones de la lista de localizaciones de juego vinculada a su entrada
	 */
	private Map<Event, Map<Player, List<Localization>>> playersInLocalizations;
	
	/**
	 * Por categoría, Diccionario de jugadores para cada cual los enfrentamientos de los que forme parte han de tener lugar
	 * en cualquiera de las horas de la lista de timeslots vinculada a su entrada
	 */
	private Map<Event, Map<Player, List<Timeslot>>> playersAtTimeslots;
	
	
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
	
	// Tiempo máximo de resolución (0: infinito)
	private int resolutionTimeLimit = 0;
	
	private ResolutionData resolutionData;
	
	/**
	 * Estrategias de búsqueda empleadas en la resolución del problema
	 */
	private String searchStrategyName;
	
	/**
	 * Número de categorías del torneo de este solver que emplean emparejamientos predefinidos por sorteo
	 */
	private int randomDrawingsCount = 0;
	
	private static final Logger LOGGER = Logger.getLogger(TournamentSolver.class.getName());
	
	public TournamentSolver(Tournament tournament) {
		this.tournament = tournament;
		events = tournament.getEvents();
		
		nCategories = events.length;
		
		allPlayers = tournament.getAllPlayers();
		allTimeslots = tournament.getAllTimeslots();
		allLocalizations = tournament.getAllLocalizations();
		
		nPlayers = new int[nCategories];
		nLocalizations = new int[nCategories];
		nTimeslots = new int[nCategories];
		
		unavailability = new int[nCategories][][];
		
		nMatchesPerPlayer = new int[nCategories];
		nTimeslotsPerMatch = new int[nCategories];
		nPlayersPerMatch = new int[nCategories];
		
		fixedMatchups = new HashMap<Event, List<Set<Player>>>();
		playersInLocalizations = new HashMap<Event, Map<Player, List<Localization>>>();
		playersAtTimeslots = new HashMap<Event, Map<Player, List<Timeslot>>>();
		
		for (int i = 0; i < nCategories; i++) {
			nPlayers[i] = events[i].getNumberOfPlayers();
			nLocalizations[i] = events[i].getNumberOfLocalizations();
			nTimeslots[i] = events[i].getNumberOfTimeslots();
			
			unavailability[i] = events[i].getUnavailableTimeslotsAs2DIntArray();
			
			nMatchesPerPlayer[i] = events[i].getMatchesPerPlayer();
			nTimeslotsPerMatch[i] = events[i].getMatchDuration();
			nPlayersPerMatch[i] = events[i].getPlayersPerMatch();
			
			if (events[i].hasFixedMatchups())
				fixedMatchups.put(events[i], events[i].getFixedMatchups());
			
			if (events[i].hasPlayersInLocalizations())
				playersInLocalizations.put(events[i], events[i].getPlayersInLocalizations());
			
			if (events[i].hasPlayersAtTimeslots())
				playersAtTimeslots.put(events[i], events[i].getPlayersAtTimeslots());
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
		
		courtsIndices = new int[allLocalizations.size()][nCategories];
		for (int i = 0; i < allLocalizations.size(); i++) {
			for (int e = 0; e < nCategories; e++) {
				Localization[] eventCourts = events[e].getLocalizations();
				Localization court = allLocalizations.get(i);
				
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
	
	public void setResolutionTimeLimit(int limit) {
		resolutionTimeLimit = limit;
	}
	
	public ResolutionData getResolutionData() {
		return resolutionData;
	}
	
	public boolean execute() {
		createSolver();
		buildModel();
		configureSearch(searchStrategyOption);
		return solve();
	}
	
	private void createSolver() {
		solver = new Solver("Tournament Solver");
	}
	
	private void buildModel() {
		initMatrices();
		
		markDiscardedLocalizations();
		
		if (!playersInLocalizations.isEmpty())
			markPlayersNotInLocalizations();
		
		if (!playersAtTimeslots.isEmpty())
			markPlayersNotAtTimeslots();
		
		markBreaks();
		
		initConstraints();
	}
	
	/**
	 * Inicializa las matrices IntVar del problema, teniendo en cuenta la indisponibilidad de los jugadores
	 */
	private void initMatrices() {
		for (int e = 0; e < events.length; e++) {
			x[e] = new IntVar[nPlayers[e]][nLocalizations[e]][nTimeslots[e]];
			g[e] = new IntVar[nPlayers[e]][nLocalizations[e]][nTimeslots[e]];
		}
		
		for (int e = 0; e < nCategories; e++) {
			for (int p = 0; p < nPlayers[e]; p++) {
				for (int c = 0; c < nLocalizations[e]; c++) {
					for (int t = 0; t < nTimeslots[e]; t++) {
						// Si el jugador_p no está disponible a la hora_t se marca con 0
						if (isUnavailable(e, p, t)) {
							int nRange = nTimeslotsPerMatch[e];		
							if (t + 1 < nTimeslotsPerMatch[e])
								nRange -= nTimeslotsPerMatch[e] - t - 1;
							
							// Si un jugador no está disponible en t, n no podrá empezar un partido en el rango t-n..t
							// (siendo n la duración o número de timeslots de un partido)
							for (int i = 0; i < nRange; i++)
								g[e][p][c][t - i] = VariableFactory.fixed(0, solver);
							
							// Además, se marca con 0 las horas de la matriz de horario/partidos si el jugador no puede jugar
							x[e][p][c][t] = VariableFactory.fixed(0, solver);
							
						} else {
							// Dominio [0, 1]: 0 -> no juega, 1 -> juega
							x[e][p][c][t] = VariableFactory.bounded("x" + e + "," + p + "," + c + "," + t, 0, 1, solver);
							
							// Dominio [0, 1]: 0 -> el partido no empieza a esa hora, 1 -> el partido empieza a esa hora
							g[e][p][c][t] = VariableFactory.bounded("g" + e + "," + p + "," + c + "," + t, 0, 1, solver);
						}
					}
				}
			}
		}
	}
	
	/**
	 * Marca las localizaciones descartadas en las matrices del problema
	 */
	private void markDiscardedLocalizations() {
		// Marcar las localizaciones descartadas con 0
		for (int e = 0; e < nCategories; e++) {
			Map<Localization, List<Timeslot>> discardedLocalizations = events[e].getDiscardedLocalizations();
			Set<Localization> localizations = discardedLocalizations.keySet();
			
			for (Localization localization : localizations) {
				List<Timeslot> timeslots = discardedLocalizations.get(localization);
				int nDiscardedTimeslots = timeslots.size();
				
				int[] tIndex = new int[nDiscardedTimeslots];
				
				int i = 0;
				for (Timeslot timeslot : timeslots)
					tIndex[i++] = events[e].indexOf(timeslot);
				
				int c = events[e].indexOf(localization);
				
				for (int p = 0; p < nPlayers[e]; p++)
					for (int t = 0; t < nDiscardedTimeslots; t++) {
						x[e][p][c][tIndex[t]] = VariableFactory.fixed(0, solver);
						g[e][p][c][tIndex[t]] = VariableFactory.fixed(0, solver);
					}
			}
		}
	}
	
	/**
	 * Fuerza a que los jugadores indicados jueguen sus partidos en las localizaciones indicadas
	 */
	private void markPlayersNotInLocalizations() {
		// Si para el jugador_p en la categoría_e se indica que debe jugar en un conjunto de localizaciones,
		// se marcan con 0 todas las localizaciones del evento que no sean ésas, de este modo invalidándolas
		for (int e = 0; e < nCategories; e++) {
			if (events[e].hasPlayersInLocalizations()) {
				Map<Player, List<Localization>> eventPlayersInLocalizations = events[e].getPlayersInLocalizations();
				Set<Player> players = eventPlayersInLocalizations.keySet();
				
				// Para cada jugador al que se le ha indicado una lista de pistas donde jugar, "invalidar" las pistas
				// que no pertenecen a esa lista
				for (Player player : players) {
					List<Localization> assignedLocalizations = eventPlayersInLocalizations.get(player);
					
					int p = events[e].indexOf(player);
					
					for (int c = 0; c < nLocalizations[e]; c++) {
						// Si la pista no es de las asignadas al jugador
						if (!assignedLocalizations.contains(events[e].getLocalizationAt(c))) {
							for (int t = 0; t < nTimeslots[e]; t++) {
								x[e][p][c][t] = VariableFactory.fixed(0, solver);
								g[e][p][c][t] = VariableFactory.fixed(0, solver);
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * Fuerza a que los jugadores indicados jueguen sus partidos en los timeslots indicados
	 */
	private void markPlayersNotAtTimeslots() {
		// Si para el jugador_p en la categoría_e se indica que debe jugar en un conjunto de timeslots,
		// se marcan con 0 todos los timeslots del evento que no esan ésos, de este modo invalidándolos
		for (int e = 0; e < nCategories; e++) {
			if (events[e].hasPlayersAtTimeslots()) {
				Map<Player, List<Timeslot>> eventPlayersAtTimeslots = events[e].getPlayersAtTimeslots();
				Set<Player> players = eventPlayersAtTimeslots.keySet();
				
				for (Player player : players) {
					List<Timeslot> assignedTimeslots = eventPlayersAtTimeslots.get(player);
					
					int p = events[e].indexOf(player);
					
					for (int t = 0; t < nTimeslots[e]; t++) {
						if (!assignedTimeslots.contains(events[e].getTimeslotAt(t))) {
							for (int c = 0; c < nLocalizations[e]; c++) {
								x[e][p][c][t] = VariableFactory.fixed(0, solver);
								g[e][p][c][t] = VariableFactory.fixed(0, solver);
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * Marca los descansos o breaks en las matrices del problema 
	 */
	private void markBreaks() {
		// Marcar los breaks con 0
		for (int e = 0; e < nCategories; e++) {
			for (int t = 0; t < nTimeslots[e]; t++) {
				// Si el timeslot_t es un break, entonces en él no se puede jugar y se marca como 0
				if (events[e].getTimeslotAt(t).getIsBreak()) {
					for (int p = 0; p < nPlayers[e]; p++) {
						for (int c = 0; c < nLocalizations[e]; c++) {
							x[e][p][c][t] = VariableFactory.fixed(0, solver);
							g[e][p][c][t] = VariableFactory.fixed(0, solver);
						}
					}
				}
			}
		}
	}
	
	/**
	 * Construye todas las restricciones del problema 
	 */
	private void initConstraints() {
		setConstraintsTeams();
		
		if (!fixedMatchups.isEmpty())
			setConstraintsFixedMatchups();
		
		setConstraintsPredefinedRandomMatchups();
		
		setConstraintsMatchesSum();
		
		setConstraintsPlayersMatchesNumber();
		
		setConstraintsMapMatchesBeginning();
		
		setConstraintsMapMatches();
		
		setConstraintsPlayersInCourtsForEachCategory();
		
		if (events.length > 1)
			setConstraintsCourtsCollisions();
		
		setConstraintsPlayersNotSimultaneous();
	}
	
	/**
	 * Asegura que los jugadores que componen un equipo jueguen en el mismo partido
	 */
	private void setConstraintsTeams() {
		for (int e = 0; e < nCategories; e++) {
			if (events[e].hasTeams()) {
				List<Team> teams = events[e].getTeams();
				
				for (Team team : teams) {
					Set<Player> playersInTeam = team.getPlayers();
					int nPlayersInTeam = playersInTeam.size();
					
					int[] pIndex = new int[nPlayersInTeam];
					int i = 0;
					for (Player player : playersInTeam)
						pIndex[i++] = events[e].indexOf(player);
					
					for (int c = 0; c < nLocalizations[e]; c++)
						for (int t = 0; t < nTimeslots[e]; t++)
							for (int p = 0; p < nPlayersInTeam - 1; p++)
								solver.post(IntConstraintFactory.arithm(x[e][pIndex[p]][c][t], "=", x[e][pIndex[p + 1]][c][t]));
				}
			}
		}
	}
	
	/**
	 * Asegura que los emparejamientos predefinidos se producen
	 */
	private void setConstraintsFixedMatchups() {
		for (int e = 0; e < nCategories; e++) {
			if (events[e].hasFixedMatchups()) {
				List<Set<Player>> fixedMatchups = events[e].getFixedMatchups();
				int nPlayersInMatch = nPlayersPerMatch[e];
				
				for (Set<Player> matchup : fixedMatchups) {
					int[] pIndex = new int[nPlayersInMatch];
					int i = 0;
					for (Player player : matchup)
						pIndex[i++] = events[e].indexOf(player);
					
					for (int c = 0; c < nLocalizations[e]; c++)
						for (int t = 0; t < nTimeslots[e]; t++)
							for (int p = 0; p < nPlayersInMatch - 1; p++)
								solver.post(IntConstraintFactory.arithm(x[e][pIndex[p]][c][t], "=", x[e][pIndex[p + 1]][c][t]));
				}
			}
		}
	}
	
	/**
	 * Emparejamientos por sorteo: predefine los jugadores que compondrán un partido. Esto tiene dos consecuencias:
	 * 1. El cálculo del horario es mucho más rápido porque la restricción es más fuerte
	 * 2. Se reducen las posibles combinaciones de horarios porque ya no puede ser emparejado cualquier jugador con cualquier otro
	 * 2a. Puede provocar que no se encuentren soluciones
	 */
	private void setConstraintsPredefinedRandomMatchups() {
		Map<Event, List<List<Player>>> predefinedMatchups = buildPredefinedRandomMatchups();
		
		/*System.out.println("PREDEFINED RANDOM MATCHUPS:");
		for (Event e : predefinedMatchups.keySet()) {
			System.out.println(e + " (" + predefinedMatchups.get(e).size() + ")");
			for (List<Player> matchup : predefinedMatchups.get(e)) {
				System.out.print(matchup.size() + ": ");
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
					pIndex[p] = event.indexOf(matchup.get(p));
				
				for (int c = 0; c < nLocalizations[e]; c++)
					for (int t = 0; t < nTimeslots[e]; t++)
						for (int p = 0; p < nPlayersInMatch - 1; p++)
							solver.post(IntConstraintFactory.arithm(x[e][pIndex[p]][c][t], "=", x[e][pIndex[p + 1]][c][t]));
			}
		}
	}
	
	/**
	 * @return diccionario de emparejamientos aleatorios predefinidos para cada categoría
	 */
	private Map<Event, List<List<Player>>> buildPredefinedRandomMatchups() {
		Map<Event, List<List<Player>>> predefinedMatchups = new HashMap<Event, List<List<Player>>>();
		for (Event event : events) {
			// Si el evento se organiza por sorteo (y hay más de un jugador por partido, luego hay enfrentamientos)
			if (event.getPlayersPerMatch() > 1 && event.getRandomDrawings()) {
				List<Player> players = new ArrayList<Player>(Arrays.asList(event.getPlayers()));
				List<List<Player>> matchups = new ArrayList<List<Player>>(event.getNumberOfMatches());
				
				// Excluimos a los jugadores que pertenecen a partidos fijos del sorteo porque ya están emparejados
				if (event.hasFixedMatchups()) {
					List<Set<Player>> eventFixedMatchups = fixedMatchups.get(event);
					
					for (Set<Player> matchup : eventFixedMatchups)
						for (Player player : matchup)
							players.remove(player);
				}
				
				Random random = new Random();
				int nPlayersPerMatch = event.getPlayersPerMatch();
				
				// Se construyen los enfrentamientos aleatorios para la categoría con equipos (cada enfrentamiento contedrá
				// a todos los jugadores que compongan el equipo)
				if (event.hasTeams()) {
					while (!players.isEmpty()) {
						List<Player> matchup = new ArrayList<Player>(nPlayersPerMatch);
						
						for (int i = 0; i < nPlayersPerMatch; i++) {
							int randIndex = random.ints(0, players.size()).findFirst().getAsInt();
							
							Player player = players.get(randIndex);
							Set<Player> playersInTeam = event.getTeamByPlayer(player).getPlayers();
						
							matchup.add(player);
							players.remove(randIndex);
							
							for (Player playerInTeam : playersInTeam) {
								if (!playerInTeam.equals(player)) {
									matchup.add(playerInTeam);
									players.remove(playerInTeam);
									
									// se incrementa el contador del bucle for superior ya que se ha añadido un jugador al enfrentamiento
									i++;
								}
							}
						}
						
						matchups.add(matchup);
					}
				} else {
					while (!players.isEmpty()) {
						List<Player> matchup = new ArrayList<Player>(nPlayersPerMatch);
						
						// Se crea un enfrentamiento con nPlayersPerMatch aleatorios
						for (int i = 0; i < nPlayersPerMatch; i++) {
							int randIndex = random.ints(0, players.size()).findFirst().getAsInt();
							
							matchup.add(players.get(randIndex));
							players.remove(randIndex);
						}
						
						matchups.add(matchup);
					}
				}
				
				// Se añade el enfrentamiento a la categoría
				predefinedMatchups.put(event, matchups);
				
				// Se incrementa la cuenta de categorías que usan sorteo predefinido
				randomDrawingsCount++;
			}
		}
		
		return predefinedMatchups;
	}
	
	/**
	 * Impone que la suma de timeslots utilizados por cada evento se corresponda con el número de encuentros esperados
	 */
	private void setConstraintsMatchesSum() {
		for (int e = 0; e < nCategories; e++) {
			int eventNumberOfMatches = nPlayers[e] * nMatchesPerPlayer[e];
			
			solver.post(IntConstraintFactory.sum(ArrayUtils.flatten(g[e]), VariableFactory.fixed(eventNumberOfMatches, solver)));
			solver.post(IntConstraintFactory.sum(ArrayUtils.flatten(x[e]), VariableFactory.fixed(eventNumberOfMatches * nTimeslotsPerMatch[e], solver)));
		}
	}
	
	/**
	 * Asegurar que el número de partidos que juega cada jugador es el correspondiente al requerido por cada categoría
	 */
	private void setConstraintsPlayersMatchesNumber() {
		// Que cada jugador juegue nMatchesPerPlayer partidos
		for (int e = 0; e < nCategories; e++) {
			int playerNumberOfTimeslots = nMatchesPerPlayer[e] * nTimeslotsPerMatch[e];
			for (int p = 0; p < nPlayers[e]; p++) {
				solver.post(IntConstraintFactory.sum(ArrayUtils.flatten(g[e][p]), VariableFactory.fixed(nMatchesPerPlayer[e], solver)));	
				solver.post(IntConstraintFactory.sum(ArrayUtils.flatten(x[e][p]), VariableFactory.fixed(playerNumberOfTimeslots, solver)));
			}
		}
	}
	
	/**
	 * Mapea los comienzos de los partidos a partir de la asignación de horas en la matriz del horario
	 */
	private void setConstraintsMapMatchesBeginning() {
		// Mapear entre los comienzos de cada partido (g) y las horas en las que se juega
		for (int e = 0; e < nCategories; e++) {
			for (int p = 0; p < nPlayers[e]; p++) {
				for (int c = 0; c < nLocalizations[e]; c++) {
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
	
	/**
	 * Mapea la matriz del horario a partir de la matriz de los comienzos de partido
	 */
	private void setConstraintsMapMatches() {
		// Mapear x_e,p,c,t a partir de los posibles comienzos de partido (g) cuyo rango "cubre" x_t
		for (int e = 0; e < nCategories; e++) {
			for (int p = 0; p < nPlayers[e]; p++) {
				for (int c = 0; c < nLocalizations[e]; c++) {
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
	
	/**
	 * Para categoría, define la restricción para que solamemente haya dos números posibles de jugadores en una
	 * pista determinada a una hora en concreto: o 0 (nadie) o el número de jugadores por partido de la categoría
	 */
	private void setConstraintsPlayersInCourtsForEachCategory() {
		for (int e = 0; e < nCategories; e++) {
			for (int c = 0; c < nLocalizations[e]; c++) {
				for (int t = 0; t < nTimeslots[e]; t++) {
					// Si la hora_t es un break, no hace falta tenerla en cuenta
					if (!events[e].getTimeslotAt(t).getIsBreak()) {
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
	}
	
	/**
	 * Para todas las categorías del torneo, controla que no se juegue en la misma pista a la misma hora
	 * en partidos de categorías distintas
	 */
	private void setConstraintsCourtsCollisions() {
		Map<Integer, List<Event>> eventsByNumberOfPlayersPerMatch = tournament.groupEventsByNumberOfPlayersPerMatch();
		
		// Posibles números de jugadores que componen un partido del torneo (incluye 0)
		int[] allPossibleNumberOfPlayers = getAllPosibleNumberOfPlayersPerMatchArray(eventsByNumberOfPlayersPerMatch);
		
		int nAllCourts = allLocalizations.size();
		int nAllTimeslots = allTimeslots.size();
		
		// Para cada pista del torneo explorar las participaciones de jugadores en cada categoría
		// y controlar que no se juegue más de un partido en una pista a la misma hora
		for (int c = 0; c < nAllCourts; c++) {
			for (int t = 0; t < nAllTimeslots; t++) {
				// Las posibles ocupaciones de los jugadores de la pista_c a la hora_t
				List<IntVar> playerSum = new ArrayList<IntVar>();
				
				for (int e = 0; e < nCategories; e++)
					// Si en el evento_e se puede jugar en la pista_c y a la hora_t y la hora_t no es un break
					if (courtsIndices[c][e] != -1 && timeslotsIndices[t][e] != -1 && !events[e].getTimeslotAt(timeslotsIndices[t][e]).getIsBreak())
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
	
	/**
	 * Si un jugador_p juega en más de una categoría, evitar que le coincidan partidos a la misma hora
	 */
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
						for (int c = 0; c < nLocalizations[e]; c++) 
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
		
		solver.set(getStrategy(option, ArrayUtils.flatten(vars)));
	}
	
	@SuppressWarnings("rawtypes")
	private AbstractStrategy[] getStrategy(int option, IntVar[] v) {
		AbstractStrategy[] strategies;
		switch (option) {
			case 1:
				strategies = new AbstractStrategy[] { IntStrategyFactory.domOverWDeg(v, System.currentTimeMillis()) };
				searchStrategyName = "domOverWDeg";
				break;
			case 2:
				strategies = new AbstractStrategy[] { IntStrategyFactory.minDom_UB(v) };
				searchStrategyName = "minDom_LB";
				break;
			case 3:
				strategies = new AbstractStrategy[] { IntStrategyFactory.minDom_LB(v) };
				searchStrategyName = "minDom_LB";
				break;
			case 4:
				strategies = new AbstractStrategy[] {
					IntStrategyFactory.minDom_UB(v),
					IntStrategyFactory.domOverWDeg(v, System.currentTimeMillis())
				};
				searchStrategyName = "minDom_UB,domOverWDeg";
				break;
			default:
				strategies = new AbstractStrategy[] { IntStrategyFactory.domOverWDeg(v, System.currentTimeMillis()) };
				searchStrategyName = "domOverWDeg";
				break;
		}
		
		return strategies;
	}
	
	private boolean solve() {
		if (resolutionTimeLimit > 0)
			SearchMonitorFactory.limitTime(solver, resolutionTimeLimit);
		
		boolean solutionFound = solver.findSolution();	
		if (solutionFound)
			resolutionData = new ResolutionData(solver, tournament, searchStrategyName, randomDrawingsCount, true);
		else
			resolutionData = new ResolutionData(solver, tournament, searchStrategyName, randomDrawingsCount, false);
		
		Chatterbox.printStatistics(solver);
		
		if (!solutionFound) {
			if (solver.isFeasible() == ESat.FALSE)
				LOGGER.log(Level.INFO, "Problem infeasible.");
			else if (solver.isFeasible() == ESat.UNDEFINED)
				LOGGER.log(Level.INFO, "A solution has not been found within given limits.");
		}
		
		return solutionFound;
	}
	
	public EventSchedule[] getSchedules() {
		// Cuando se llega a la última solución, si se vuelve a llamar a este método se "limpian" los horarios
		if (lastSolutionFound && schedules != null)
			schedules = null;
		
		else if (solver.isFeasible() != ESat.TRUE) {
			LOGGER.log(Level.INFO, "Solution not found.");
			schedules = null;
			
		} else if (schedules == null) {   // Se ha encontrado la primera solución
			schedules = new EventSchedule[nCategories];
			buildSchedules();
			
		} else {                          // Se ha encontrado una siguiente solución
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