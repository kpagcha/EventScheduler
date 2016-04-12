package solver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

import data.model.schedule.EventSchedule;
import data.model.tournament.Tournament;
import data.model.tournament.event.Event;
import data.model.tournament.event.entity.Localization;
import data.model.tournament.event.entity.Player;
import data.model.tournament.event.entity.Team;
import data.model.tournament.event.entity.timeslot.Timeslot;

/**
 * @author Pablo
 *
 */
public class TournamentSolver {
	
	/**
	 * Modos de emparejamiento (para categorías con más de un partido por jugador)
	 */
	public enum MatchupMode {
		/**
		 * Todos los emparejamientos deben ser entre distintos jugadores o equipos
		 */
		ALL_DIFFERENT,
		
		/**
		 * Todos los emparejamientos deben ser entre los mismos jugadores o equipos
		 */
		ALL_EQUAL,
		
		/**
		 * Los emparejamientos se pueden dar entre cualquier jugador o equipo disponible. Se pueden repetir emparejamientos
		 */
		ANY
	};
	
	/**
	 * Solver de Choco que modela y resuelve el problema
	 */
	private Solver solver;
	
	/**
	 * Torneo para el que se calcula el horario
	 */
	private Tournament tournament;
	
	/**
	 * Categorías del torneo
	 */
	private Event[] events;
	
	/**
	 * Número de categorías
	 */
	private int nCategories;
	
	/**
	 * Todas las categorías del torneo
	 */
	private List<Player> allPlayers;
	
	/**
	 * Todas las localizaciones de juego del torneo
	 */
	private List<Localization> allLocalizations;
	
	/**
	 * Todos los timeslots del torneo
	 */
	private List<Timeslot> allTimeslots;
	
	/**
	 * Número de jugadores de cada categoría
	 */
	private int[] nPlayers;
	
	/**
	 * Número de localizaciones de juego de cada categoría
	 */
	private int[] nLocalizations;
	
	/**
	 * Número de timeslots de cada categoría
	 */
	private int[] nTimeslots;
	
	/**
	 * Número de partidos que debe jugar cada jugador (para cada categoría)
	 */
	private int[] nMatchesPerPlayer;
	
	/**
	 * Número de horas (timeslots) que ocupa cada partido (para cada categoría)
	 */
	private int[] nTimeslotsPerMatch;
	
	/**
	 * Número de jugadores por partido (lo normal será 2) (para cada categoría)
	 */
	private int[] nPlayersPerMatch;
	 
	/**
	 * Emparejamientos predefinidos
	 */
	private Map<Event, List<Set<Player>>> predefinedMatchups;
	
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
	
	/**
	 * Horario. x_e,p,c,t -> horario_categoria,jugador,pista,hora. Dominio [0, 1] 
	 */
	private IntVar[][][][] x;
	
	/**
	 * Comienzos de partidos. Dominio [0, 1] 
	 */
	private IntVar[][][][] g;
	
	/**
	 * Horarios calculados de la solución actual
	 */
	private EventSchedule[] schedules;
	
	/**
	 * Índices de cada jugador en el array de jugadores correspondiente a cada categoría
	 * p.e. playersIndices[2][1] = 3 significa que el índice del jugador número 3 en
	 * la categoría número 2 es 3 (si el jugador no existe en la categoría, el índice es -1)
	 */
	private int[][] playersIndices;
	
	
	/**
	 * Índices de cada timeslot en el array de timeslots correspondiente a cada categoría 
	 */
	private int[][] timeslotsIndices;
	
	
	/**
	 * Índices de cada pista en el array de localidades de juego (pistas) correspondiente a cada categoría 
	 */
	private int[][] courtsIndices;
	
	/**
	 * Indica si se ha encontrado la última solución
	 */
	private boolean lastSolutionFound = false;
	
	/**
	 * Opción de estrategia de búsqueda empleada en la resolución del problema
	 */
	private int searchStrategyOption = 1;
	
	/**
	 * Para las estrategias de búsqueda minDom_UB y minDom_LB indicar si priorizar timeslots (true) o
	 * pistas (false) a la hora de hacer las asignaciones
	 */
	private boolean fillTimeslotsFirst = true;
	
	/**
	 * Tiempo máximo de resolución. 0 significa sin límite
	 */
	private int resolutionTimeLimit = 0;
	
	/**
	 * Bandera que indica la parada del proceso de resolución
	 */
	private boolean stop = false;
	
	/**
	 * Información sobre el problema y la resolución del mismo
	 */
	private ResolutionData resolutionData;
	
	/**
	 * Logger del solver
	 */
	private static final Logger LOGGER = Logger.getLogger(TournamentSolver.class.getName());
	
	/**
	 * Construye el objeto solver a partir de la información del torneo
	 * 
	 * @param tournament torneo para el que se va a resolver el problema
	 */
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
		
		nMatchesPerPlayer = new int[nCategories];
		nTimeslotsPerMatch = new int[nCategories];
		nPlayersPerMatch = new int[nCategories];
		
		playersInLocalizations = new HashMap<Event, Map<Player, List<Localization>>>();
		playersAtTimeslots = new HashMap<Event, Map<Player, List<Timeslot>>>();
		
		for (int i = 0; i < nCategories; i++) {
			nPlayers[i] = events[i].getNumberOfPlayers();
			nLocalizations[i] = events[i].getNumberOfLocalizations();
			nTimeslots[i] = events[i].getNumberOfTimeslots();
			
			nMatchesPerPlayer[i] = events[i].getMatchesPerPlayer();
			nTimeslotsPerMatch[i] = events[i].getMatchDuration();
			nPlayersPerMatch[i] = events[i].getPlayersPerMatch();
			
			if (events[i].hasPlayersInLocalizations())
				playersInLocalizations.put(events[i], events[i].getPlayersInLocalizations());
			
			if (events[i].hasPlayersAtTimeslots())
				playersAtTimeslots.put(events[i], events[i].getPlayersAtTimeslots());
		}
		
		initPredefinedMatchups();

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
	
	/**
	 * Combina los enfrentamientos fijos predefinidos y los predefinidos por sorteo en un único diccionario. También
	 * sirve para recalcular los enfrentamientos por sorteo, por ejemplo, al darse una combinación fallida
	 */
	public void initPredefinedMatchups() {
		predefinedMatchups = new HashMap<Event, List<Set<Player>>>();
		
		// Añadir a la lista de emparejamientos predefinidos los emparejamientos fijos
		for (int i = 0; i < nCategories; i++)
			if (events[i].hasFixedMatchups())
				predefinedMatchups.put(events[i], events[i].getFixedMatchups());
		
		Map<Event, List<Set<Player>>> predefinedRandomMatchups = getPredefinedRandomMatchups();
		
		// Añadir los emparejamientos por sorteo a la lista combinada de emparejamientos predefinidos
		for (Event event : events)
			if (predefinedMatchups.containsKey(event) && predefinedRandomMatchups.containsKey(event))
				predefinedMatchups.get(event).addAll(predefinedRandomMatchups.get(event));
			else if (predefinedRandomMatchups.containsKey(event))
				predefinedMatchups.put(event, predefinedRandomMatchups.get(event));
		
		System.out.println("\nPredefined matchups:");
		for (Event event : events) {
			if (predefinedMatchups.containsKey(event)) {
				System.out.println("--< " + event + " >--");
				for (Set<Player> matchup : predefinedMatchups.get(event)) {
					for (Player player : matchup)
						System.out.print(player + " ");
					System.out.println();
				}
				System.out.println();
			}
		}
	}
	
	/**
	 * @return diccionario de emparejamientos aleatorios predefinidos para cada categoría
	 */
	private Map<Event, List<Set<Player>>> getPredefinedRandomMatchups() {
		Map<Event, List<Set<Player>>> randomMatchups = new HashMap<Event, List<Set<Player>>>();
		for (Event event : events) {
			// Si el evento se organiza por sorteo (y hay más de un jugador por partido, luego hay enfrentamientos)
			if (event.getPlayersPerMatch() > 1 && event.getRandomDrawings()) {
				List<Player> players = new ArrayList<Player>(Arrays.asList(event.getPlayers()));
				List<Set<Player>> matchups = new ArrayList<Set<Player>>(event.getNumberOfMatches());
				
				// Excluimos a los jugadores que pertenecen a partidos fijos del sorteo porque ya están emparejados
				if (event.hasFixedMatchups()) {
					List<Set<Player>> eventFixedMatchups = event.getFixedMatchups();
					
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
						Set<Player> matchup = new HashSet<Player>(nPlayersPerMatch);
						
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
						Set<Player> matchup = new HashSet<Player>(nPlayersPerMatch);
						
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
				randomMatchups.put(event, matchups);
			}
		}
		
		return randomMatchups;
	}
	
	public void setSearchStrategy(int option) {
		searchStrategyOption = option;
	}
	
	public void setFillTimeslotsFirst(boolean fillFirst) {
		fillTimeslotsFirst = fillFirst;
	}
	
	public boolean getFillTimeslotsFirst() {
		return fillTimeslotsFirst;
	}
	
	public void setResolutionTimeLimit(int limit) {
		resolutionTimeLimit = limit;
	}
	
	public int getResolutionTimeLimit() {
		return resolutionTimeLimit;
	}
	
	public ResolutionData getResolutionData() {
		return resolutionData;
	}
	
	/**
	 * Construye y modela el problema, configura las estrategias de búsqueda e inicia el proceso de resolución
	 * 
	 * @return true si se ha encontrado una solución, false si no
	 */
	public boolean execute() {
		createSolver();
		buildModel();
		configureSearch(searchStrategyOption);
		return solve();
	}
	
	/**
	 * Crea el objeto solver de Choco
	 */
	private void createSolver() {
		solver = new Solver("Tournament Solver");
	}
	
	/**
	 * Inicializa las variables del modelo del problema
	 */
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
			Map<Player, List<Timeslot>> eventUnavailabilities = events[e].getUnavailableTimeslots();
			for (int p = 0; p < nPlayers[e]; p++) {
				List<Timeslot> playerUnavailabilities = eventUnavailabilities.get(events[e].getPlayerAt(p));
				for (int c = 0; c < nLocalizations[e]; c++) {
					for (int t = 0; t < nTimeslots[e]; t++) {
						// Si el jugador_p no está disponible a la hora_t se marca con 0
						if (playerUnavailabilities != null && playerUnavailabilities.contains(events[e].getTimeslotAt(t))) {
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
				if (events[e].isBreak(events[e].getTimeslotAt(t))) {
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
		for (Event event : events) {
			if (event.hasTeams())
				setConstraintsTeams(event);
			
			if (event.getMatchesPerPlayer() > 1 && event.getMatchupMode() == MatchupMode.ALL_DIFFERENT)
				setConstraintsMatchupsAllDifferent(event);
		}
		
		if (!predefinedMatchups.isEmpty())
			for (Event event : events)
				if (predefinedMatchups.containsKey(event))
					setConstraintsPredefinedMatchups(event);
		
		setConstraintsMatchesSum();
		
		setConstraintsMatchesPerPlayer();
		
		setConstraintsMapMatchesBeginnings();
		
		setConstraintsMapMatches();
		
		for (Event event : events)
			setConstraintsPlayersInCourts(event);
		
		if (events.length > 1)
			setConstraintsCourtsCollisions();
		
		setConstraintsPlayersNotSimultaneous();
	}
	
	/**
	 * Asegura que los jugadores que componen un equipo jueguen en el mismo partido
	 */
	private void setConstraintsTeams(Event event) {
		List<Team> teams = event.getTeams();
		int e = getEventIndex(event);
		
		for (Team team : teams) {
			Set<Player> playersInTeam = team.getPlayers();
			int nPlayersInTeam = playersInTeam.size();
			
			int[] pIndex = new int[nPlayersInTeam];
			int i = 0;
			for (Player player : playersInTeam)
				pIndex[i++] = event.indexOf(player);
			
			for (int c = 0; c < nLocalizations[e]; c++)
				for (int t = 0; t < nTimeslots[e]; t++)
					for (int p = 0; p < nPlayersInTeam - 1; p++)
						solver.post(IntConstraintFactory.arithm(x[e][pIndex[p]][c][t], "=", x[e][pIndex[p + 1]][c][t]));
		}
	
	}
	
	/**
	 * Para categorías con más de un partido por jugador, forzar que los enfrentamientos no se repitan
	 */
	private void setConstraintsMatchupsAllDifferent(Event event) {
		int e = getEventIndex(event);
		List<List<Integer>> combinations = getCombinations(
			IntStream.range(0, event.getNumberOfPlayers()).toArray(),
			nPlayersPerMatch[e]
		);
		
		for (List<Integer> combination : combinations) {
			for (int c = 0; c < nLocalizations[e]; c++) {
				IntVar[] possibleMatchups = VariableFactory.boundedArray("PossibleMatchups", nTimeslots[e], 0, 1, solver);
				
				for (int t = 0; t < nTimeslots[e]; t++) {
					IntVar[] possibleMatchup = new IntVar[nPlayersPerMatch[e]];
					for (int i = 0; i < nPlayersPerMatch[e]; i++)
						possibleMatchup[i] = x[e][combination.get(i)][c][t];
							
					solver.post(IntConstraintFactory.minimum(possibleMatchups[t], possibleMatchup));
				}
				solver.post(IntConstraintFactory.sum(possibleMatchups, "<=", VariableFactory.fixed(1, solver)));
			}	
		}
	}
	
	/**
	 * Devuelve una lista con todas las combinaciones únicas de k elementos de un conjunto de
	 * jugadores, representados por enteros
	 * 
	 * @param players jugadores de los que se van a generar combinaciones
	 * @param combine número de jugadores por combinación
	 * @return una lista de lista de enteros con las combinaciones únicas de enfrentamientos
	 */
	List<List<Integer>> getCombinations(int[] players, int combine){
		List<List<Integer>> combinations = new ArrayList<List<Integer>>();
		
		combinations(players, combine, 0, new int[combine], combinations);
		
		return combinations;
	}
	
	/**
	 * Lleva a cabo el cálculo recursivo de todas las combinaciones de k elementos de un conjunto,
	 * almacenando en una lista cada combinación completa
	 * 
	 * @param arr           conjunto de enteros sobre los que calcular cada combinación
	 * @param len           longitud de la combinación
	 * @param startPosition posición de comienzo
	 * @param result        array con la combinación parcial o completa
	 * @param list          lista que almacena todas las combinaciones
	 */
	private static void combinations(int[] arr, int len, int startPosition, int[] result, List<List<Integer>> list) {
		if (len == 0) {
			list.add(IntStream.of(result.clone()).boxed().collect(Collectors.toList()));
			return;
		}
		for (int i = startPosition; i <= arr.length - len; i++) {
			result[result.length - len] = arr[i];
			combinations(arr, len - 1, i + 1, result, list);
		}
	}
	
	/**
	 * Asegura que los emparejamientos predefinidos se producen. Incluye tanto emparejamientos fijos predefinidos como
	 * emparejamientos por sorteo. Hacer emparejamientos predefinidos tiene dos consecuencias:
	 * 1. El cálculo del horario es mucho más rápido (especialmente en problemas con pocas colisiones) porque la restricción es más fuerte
	 * 2. Se reducen las posibles combinaciones de horarios porque ya no puede ser emparejado cualquier jugador con cualquier otro
	 * 2a. Puede provocar que no se encuentren soluciones
	 */
	private void setConstraintsPredefinedMatchups(Event event) {
		int e = getEventIndex(event);
		
		List<Set<Player>> matchups = predefinedMatchups.get(event);
		int nPlayersInMatch = nPlayersPerMatch[e];
		
		for (Set<Player> matchup : matchups) {
			int[] pIndex = new int[nPlayersInMatch];
			int i = 0;
			for (Player player : matchup)
				pIndex[i++] = event.indexOf(player);
			
			for (int c = 0; c < nLocalizations[e]; c++)
				for (int t = 0; t < nTimeslots[e]; t++)
					for (int p = 0; p < nPlayersInMatch - 1; p++)
						solver.post(IntConstraintFactory.arithm(x[e][pIndex[p]][c][t], "=", x[e][pIndex[p + 1]][c][t]));
	
		}
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
	private void setConstraintsMatchesPerPlayer() {
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
	private void setConstraintsMapMatchesBeginnings() {
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
	private void setConstraintsPlayersInCourts(Event event) {
		int e = getEventIndex(event);
		
		for (int c = 0; c < nLocalizations[e]; c++) {
			for (int t = 0; t < nTimeslots[e]; t++) {
				// Si la hora_t es un break, no hace falta tenerla en cuenta
				if (!events[e].isBreak(events[e].getTimeslotAt(t))) {
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
					if (courtsIndices[c][e] != -1 && timeslotsIndices[t][e] != -1 && !events[e].isBreak(events[e].getTimeslotAt(t)))
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
	
	/**
	 * @param eventsByNumberOfPlayersPerMatch diccionario donde la clave es un número de jugadores por partido y el valor asociado
	 * el valor asociado la lista de categorías que definen ese número de jugadores por partido
	 * @return posibles distintos números de jugadores por partido, incluyendo ninguno (0)
	 */
	private int[] getAllPosibleNumberOfPlayersPerMatchArray(Map<Integer, List<Event>> eventsByNumberOfPlayersPerMatch) {		
		Integer[] keysArray = eventsByNumberOfPlayersPerMatch.keySet().toArray(new Integer[eventsByNumberOfPlayersPerMatch.keySet().size()]);
		
		int[] array = new int[keysArray.length + 1];
		array[0] = 0;
		for (int i = 1; i < array.length; i++)
			array[i] = keysArray[i - 1];
		
		return array;
	}
	
	/**
	 * Devuelve el índice del evento
	 * @param event un evento o categoría
	 * @return valor del índice, o -1 si no existe
	 */
	private int getEventIndex(Event event) {
		for (int i = 0; i < events.length; i++)
			if (events[i].equals(event))
				return i;
		return -1;
	}
	
	/**
	 * Configura la estrategia de búsqueda
	 * 
	 * @param option opción de estrategia de búsqueda
	 */
	private void configureSearch(int option) {	
		IntVar[][][] vars = new IntVar[nCategories][][];
		
		if ((searchStrategyOption == 2 || searchStrategyOption == 3) && !fillTimeslotsFirst) {
			IntVar[][][][] v = new IntVar[nCategories][][][];
			for (int e = 0; e < nCategories; e++) {
				v[e] = new IntVar[nPlayers[e]][][];
				for (int p = 0; p < nPlayers[e]; p++) {
					v[e][p] = new IntVar[nTimeslots[e]][];
					for (int t = 0; t < nTimeslots[e]; t++) {
						v[e][p][t] = new IntVar[nLocalizations[e]];
						for (int c = 0; c < nLocalizations[e]; c++) {
							v[e][p][t][c] = x[e][p][c][t];
						}
					}
				}
			}
			
			for (int i = 0; i < nCategories; i++)
				vars[i] = ArrayUtils.flatten(v[i]);
		} else {
			for (int i = 0; i < nCategories; i++)
				vars[i] = ArrayUtils.flatten(x[i]);
		}
		
		
		solver.set(getStrategy(option, ArrayUtils.flatten(vars)));
	}
	
	/**
	 * @param option opción de estrategia de búsqueda
	 * @param v      variables del problema
	 * @return       estrategia o estrategias de búsqueda a aplicar
	 */
	@SuppressWarnings("rawtypes")
	private AbstractStrategy[] getStrategy(int option, IntVar[] v) {
		AbstractStrategy[] strategies;
		switch (option) {
			case 1:
				strategies = new AbstractStrategy[] { IntStrategyFactory.domOverWDeg(v, System.currentTimeMillis()) };
				break;
			case 2:
				strategies = new AbstractStrategy[] { IntStrategyFactory.minDom_UB(v) };
				break;
			case 3:
				strategies = new AbstractStrategy[] { IntStrategyFactory.minDom_LB(v) };
				break;
			case 4:
				strategies = new AbstractStrategy[] {
					IntStrategyFactory.minDom_UB(v),
					IntStrategyFactory.domOverWDeg(v, System.currentTimeMillis())
				};
				break;
			default:
				strategies = new AbstractStrategy[] { IntStrategyFactory.domOverWDeg(v, 0) };
				break;
		}
		
		return strategies;
	}
	
	/**
	 * @return el nombre de la estrategia o estrategias de búsqueda empleadas
	 */
	private String getSearchStrategyName() {
		String searchStrategyStr = "";
		switch (searchStrategyOption) {
			case 1:
				searchStrategyStr = "domOverWDeg";
				break;
			case 2:
				searchStrategyStr = "minDom_LB";
				break;
			case 3:
				searchStrategyStr = "minDom_LB";
				break;
			case 4:
				searchStrategyStr = "minDom_UB,domOverWDeg";
				break;
			default:
				searchStrategyStr = "domOverWDeg";
				break;
		}
		return searchStrategyStr;
	}
	
	/**
	 * @return el número de categorías que usan sorteo
	 */
	private int getRandomDrawingsCount() {
		int count = 0;
		for (Event event : events)
			if (event.getRandomDrawings())
				count++;
		return count;
	}
	
	/**
	 * Inicia el proceso de resolución y guarda datos del problema y de la resolución
	 * 
	 * @return true si se ha encontrado una solución, y false si no
	 */
	private boolean solve() {
		if (resolutionTimeLimit > 0)
			SearchMonitorFactory.limitTime(solver, resolutionTimeLimit);
		
		solver.addStopCriterion(() -> stop); 
		
		boolean solutionFound = solver.findSolution();	
		if (solutionFound)
			resolutionData = new ResolutionData(solver, tournament, getSearchStrategyName(), getRandomDrawingsCount(), true);
		else
			resolutionData = new ResolutionData(solver, tournament, getSearchStrategyName(), getRandomDrawingsCount(), false);
		
		Chatterbox.printStatistics(solver);
		
		if (!solutionFound) {
			if (solver.isFeasible() == ESat.FALSE)
				LOGGER.log(Level.INFO, "Problem infeasible.");
			else if (solver.isFeasible() == ESat.UNDEFINED)
				LOGGER.log(Level.INFO, "A solution has not been found within given limits.");
		}
		
		return solutionFound;
	}
	
	/**
	 * Levanta la bandera para parar el proceso de resolución
	 */
	public void stopResolutionProcess() {
		stop = true;
	}
	
	/**
	 * Actualiza y devuelve los horarios de cada categoría. Si se ha llegado a la última solución a los horarios
	 * se les establece el valor de null; también si no se ha encontrado una solución. Si se ha encontrado una
	 * solución y es la primera, se inicializa el valor de los horarios, mientras que si la solución encontrada
	 * es una solución más (la siguiente encontrada) se actualiza el valor de los horarios. Si no hay solución
	 * siguiente se marca que se ha encontrado la última solución.
	 * 
	 * @return los horarios de cada categoría
	 */
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
	
	/**
	 * Inicializa los horarios de cada categoría a partir de la solución calculada por el solver
	 */
	private void buildSchedules() {
		for (int i = 0; i < nCategories; i++)
			schedules[i] = new EventSchedule(events[i], x[i]);
	}
}