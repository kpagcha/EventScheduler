package solver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.search.loop.monitors.SearchMonitorFactory;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.trace.Chatterbox;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;

import data.model.schedule.EventSchedule;
import data.model.tournament.Tournament;
import data.model.tournament.event.Event;
import data.model.tournament.event.entity.Localization;
import data.model.tournament.event.entity.Player;
import data.model.tournament.event.entity.timeslot.Timeslot;
import solver.constraint.ConstraintBuilder;
import solver.constraint.LocalizationCollisionConstraint;
import solver.constraint.LocalizationOccupationConstraint;
import solver.constraint.MatchMappingConstraint;
import solver.constraint.MatchStartMappingConstraint;
import solver.constraint.MatchesPerPlayerConstraint;
import solver.constraint.MatchupModeConstraint;
import solver.constraint.PlayerNotSimultaneousConstraint;
import solver.constraint.PredefinedMatchupsConstraint;
import solver.constraint.TeamsConstraint;
import solver.constraint.TotalMatchesConstraint;

/**
 * Solucionador del problema que lo modela y resuelve, calculando los horarios de un torneo
 * aplicando las reglas definidas sobre el mismo
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
	 * Restricciones del problema
	 */
	private List<Constraint> constraints = new ArrayList<Constraint>();
	
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
	 * Todas los jugadores del torneo
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
	private Map<Event, Map<Player, Set<Localization>>> playersInLocalizations;
	
	/**
	 * Por categoría, diccionario de jugadores para cada cual los enfrentamientos de los que forme parte han de tener lugar
	 * en cualquiera de las horas de la lista de timeslots vinculada a su entrada
	 */
	private Map<Event, Map<Player, Set<Timeslot>>> playersAtTimeslots;
	
	/**
	 * Horario. x_e,p,c,t: horario_categoria,jugador,pista,hora. Dominio [0, 1] 
	 */
	private IntVar[][][][] x;
	
	/**
	 * Comienzos de partidos. Dominio [0, 1] 
	 */
	private IntVar[][][][] g;
	
	/**
	 * Horarios calculados de la solución actual
	 */
	private Map<Event, EventSchedule> schedules;
	
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
		initialize();
	}
	
	/**
	 * Inicializa el solver
	 */
	private void initialize() {
		List<Event> eventList = tournament.getEvents();
		events = eventList.toArray(new Event[eventList.size()]);
		
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
		
		playersInLocalizations = new HashMap<Event, Map<Player, Set<Localization>>>();
		playersAtTimeslots = new HashMap<Event, Map<Player, Set<Timeslot>>>();
		
		for (int i = 0; i < nCategories; i++) {
			nPlayers[i] = events[i].getPlayers().size();
			nLocalizations[i] = events[i].getLocalizations().size();
			nTimeslots[i] = events[i].getTimeslots().size();
			
			nMatchesPerPlayer[i] = events[i].getMatchesPerPlayer();
			nTimeslotsPerMatch[i] = events[i].getTimeslotsPerMatch();
			nPlayersPerMatch[i] = events[i].getPlayersPerMatch();
			
			if (events[i].hasPlayersInLocalizations())
				playersInLocalizations.put(events[i], events[i].getPlayersInLocalizations());
			
			if (events[i].hasPlayersAtTimeslots())
				playersAtTimeslots.put(events[i], events[i].getPlayersAtTimeslots());
		}
		
		predefinedMatchups = new HashMap<Event, List<Set<Player>>>();
		
		// Añadir a la lista de emparejamientos predefinidos los emparejamientos fijos
		for (int i = 0; i < nCategories; i++)
			if (events[i].hasFixedMatchups())
				predefinedMatchups.put(events[i], events[i].getFixedMatchups());

		x = new IntVar[nCategories][][][];
		g = new IntVar[nCategories][][][];
		
		playersIndices = new int[allPlayers.size()][nCategories];
		for (int i = 0; i < allPlayers.size(); i++) {
			for (int e = 0; e < nCategories; e++) {
				List<Player> eventPlayers = events[e].getPlayers();
				Player player = allPlayers.get(i);
				
				for (int j = 0; j < eventPlayers.size(); j++) {
					if (player.equals(eventPlayers.get(j))) {
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
				List<Timeslot> eventTimeslots = events[e].getTimeslots();
				Timeslot timeslot = allTimeslots.get(i);
				
				for (int j = 0; j < eventTimeslots.size(); j++) {
					if (timeslot.equals(eventTimeslots.get(j))) {
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
				List<Localization> eventCourts = events[e].getLocalizations();
				Localization court = allLocalizations.get(i);
				
				for (int j = 0; j < eventCourts.size(); j++) {
					if (court.equals(eventCourts.get(j))) {
						courtsIndices[i][e] = j;
						break;
					}
					courtsIndices[i][e] = -1;
				}
			}
		}
	}
	
	public Solver getSolver() {
		return solver;
	}
	
	public Tournament getTournament() {
		return tournament;
	}
	
	public IntVar[][][][] getMatchesModel() {
		return x;
	}
	
	public IntVar[][][][] getMatchesBeginningsModel() {
		return g;
	}
	
	/**
	 * Devuelve el diccionario de emparejamientos predefinidos envuelto en un wrapper no modificable
	 * 
	 * @return diccionario no modificable de emparejamientos predefinidos
	 */
	public Map<Event, List<Set<Player>>> getPredefinedMatchups() {
		return Collections.unmodifiableMap(predefinedMatchups);
	}
	
	public int[][] getPlayersIndices() {
		return playersIndices;
	}
	
	public int[][] getLocalizationsIndices() {
		return courtsIndices;
	}
	
	public int[][] getTimeslotsIndices() {
		return timeslotsIndices;
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
		initialize();
		
		initializeMatrices();
		
		markUnavailableLocalizations();
		
		if (!playersInLocalizations.isEmpty())
			markPlayersNotInLocalizations();
		
		if (!playersAtTimeslots.isEmpty())
			markPlayersNotAtTimeslots();
		
		markBreaks();
		
		setupConstraints();
		postAllConstraints();
	}
	
	/**
	 * Inicializa las matrices IntVar del problema, teniendo en cuenta la indisponibilidad de los jugadores a ciertas horas
	 */
	private void initializeMatrices() {
		for (int e = 0; e < events.length; e++) {
			x[e] = new IntVar[nPlayers[e]][nLocalizations[e]][nTimeslots[e]];
			g[e] = new IntVar[nPlayers[e]][nLocalizations[e]][nTimeslots[e]];
		}
		
		for (int e = 0; e < nCategories; e++) {
			Map<Player, Set<Timeslot>> eventUnavailabilities = events[e].getUnavailablePlayers();
			for (int p = 0; p < nPlayers[e]; p++) {
				Set<Timeslot> playerUnavailabilities = eventUnavailabilities.get(events[e].getPlayers().get(p));
				for (int c = 0; c < nLocalizations[e]; c++) {
					for (int t = 0; t < nTimeslots[e]; t++) {
						// Si el jugador_p no está disponible a la hora_t se marca con 0
						if (playerUnavailabilities != null && playerUnavailabilities.contains(events[e].getTimeslots().get(t))) {
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
	private void markUnavailableLocalizations() {
		// Marcar las localizaciones descartadas con 0
		for (int e = 0; e < nCategories; e++) {
			Map<Localization, Set<Timeslot>> discardedLocalizations = events[e].getUnavailableLocalizations();
			Set<Localization> localizations = discardedLocalizations.keySet();
			
			for (Localization localization : localizations) {
				Set<Timeslot> timeslots = discardedLocalizations.get(localization);
				int nDiscardedTimeslots = timeslots.size();
				
				int[] tIndex = new int[nDiscardedTimeslots];
				
				int i = 0;
				for (Timeslot timeslot : timeslots)
					tIndex[i++] = events[e].getTimeslots().indexOf(timeslot);
				
				int c = events[e].getLocalizations().indexOf(localization);
				
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
			if (playersInLocalizations.containsKey(events[e])) {
				Map<Player, Set<Localization>> eventPlayersInLocalizations = playersInLocalizations.get(events[e]);
				Set<Player> players = eventPlayersInLocalizations.keySet();
				
				// Para cada jugador al que se le ha indicado una lista de pistas donde jugar, "invalidar" las pistas
				// que no pertenecen a esa lista
				for (Player player : players) {
					Set<Localization> assignedLocalizations = eventPlayersInLocalizations.get(player);
					
					int p = events[e].getPlayers().indexOf(player);
					
					for (int c = 0; c < nLocalizations[e]; c++) {
						// Si la pista no es de las asignadas al jugador
						if (!assignedLocalizations.contains(events[e].getLocalizations().get(c))) {
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
			if (playersAtTimeslots.containsKey(events[e])) {
				Map<Player, Set<Timeslot>> eventPlayersAtTimeslots = playersAtTimeslots.get(events[e]);
				Set<Player> players = eventPlayersAtTimeslots.keySet();
				
				for (Player player : players) {
					Set<Timeslot> assignedTimeslots = eventPlayersAtTimeslots.get(player);
					
					int p = events[e].getPlayers().indexOf(player);
					
					for (int t = 0; t < nTimeslots[e]; t++) {
						if (!assignedTimeslots.contains(events[e].getTimeslots().get(t))) {
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
				if (events[e].isBreak(events[e].getTimeslots().get(t))) {
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
	private void setupConstraints() {
		ConstraintBuilder builder = null;
		
		for (Event event : events) {
			// Restricciones de equipos
			if (event.hasTeams()) {
				builder = new ConstraintBuilder(new TeamsConstraint(event, tournament));
				constraints.addAll(builder.getConstraints());
			}
			
			// Restricciones de modo de enfrentamiento
			if (event.getMatchesPerPlayer() > 1 && event.getPlayersPerMatch() > 1) {
				MatchupMode mode = event.getMatchupMode();
				if (mode == MatchupMode.ALL_DIFFERENT || mode == MatchupMode.ALL_EQUAL) {
					builder = new ConstraintBuilder(new MatchupModeConstraint(event, tournament));
					constraints.addAll(builder.getConstraints());
				}
			}
			
			// Restricciones de suma de partidos
			builder = new ConstraintBuilder(new TotalMatchesConstraint(event, tournament));
			constraints.addAll(builder.getConstraints());
			
			// Restricciones de emparejamientos predefinidos
			if (!predefinedMatchups.isEmpty() && predefinedMatchups.containsKey(event)) {
				builder = new ConstraintBuilder(new PredefinedMatchupsConstraint(event, tournament));
				constraints.addAll(builder.getConstraints());
			}
			
			// Restricciones de número de partidos por jugador
			builder = new ConstraintBuilder(new MatchesPerPlayerConstraint(event, tournament));
			constraints.addAll(builder.getConstraints());
			
			// Restricciones de número de jugadores en la misma pista
			builder = new ConstraintBuilder(new LocalizationOccupationConstraint(event, tournament));
			constraints.addAll(builder.getConstraints());
		}
		
		// Restricciones que mapean los comienzos de los partidos
		builder = new ConstraintBuilder(new MatchStartMappingConstraint(tournament));
		constraints.addAll(builder.getConstraints());
		
		// Restricciones que mapean los partidos
		builder = new ConstraintBuilder(new MatchMappingConstraint(tournament));
		constraints.addAll(builder.getConstraints());
		
		// Restricciones de jugadores de distintas categorías en la misma pista
		builder = new ConstraintBuilder(new LocalizationCollisionConstraint(tournament));
		constraints.addAll(builder.getConstraints());
		
		// Restricciones de jugador en la misma pista a la misma hora en distintas categorías
		builder = new ConstraintBuilder(new PlayerNotSimultaneousConstraint(tournament));
		constraints.addAll(builder.getConstraints());
	}
	
	/**
	 * Publica todas las restricciones del el modelo
	 */
	private void postAllConstraints() {
		solver.post(constraints.toArray(new Constraint[constraints.size()]));
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
	 * @param v variables del problema
	 * @return estrategia o estrategias de búsqueda a aplicar
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
			default:
				searchStrategyStr = "domOverWDeg";
				break;
		}
		return searchStrategyStr;
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
			resolutionData = new ResolutionData(solver, tournament, getSearchStrategyName(), true);
		else
			resolutionData = new ResolutionData(solver, tournament, getSearchStrategyName(), false);
		
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
	public Map<Event, EventSchedule> getSolvedSchedules() {
		// Cuando se llega a la última solución, si se vuelve a llamar a este método se "limpian" los horarios
		if (lastSolutionFound && schedules != null)
			schedules = null;
		
		else if (solver.isFeasible() != ESat.TRUE) {
			LOGGER.log(Level.INFO, "Solution not found.");
			schedules = null;
			
		} else if (schedules == null) {   // Se ha encontrado la primera solución
			schedules = new HashMap<Event, EventSchedule>(nCategories);
			buildSchedules();
			
		} else {                          // Se ha encontrado una siguiente solución
			if (solver.nextSolution()) {
				schedules.clear();
				buildSchedules();
			}
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
			schedules.put(events[i], new EventSchedule(events[i], solutionMatrixToInt(events[i], x[i])));
	}
	
	/**
	 * Transforma la matriz de IntVar de Choco en una matriz de enteros, tomando el valor actual de
	 * la solución
	 * 
	 * @param event evento no nulo
	 * @param x matriz de IntVar inicializada
	 * @return matriz de enteros con los correspondientes valores de la solución
	 */
	private int[][][] solutionMatrixToInt(Event event, IntVar[][][] x) {
		int nPlayers = event.getPlayers().size();
		int nLocalizations = event.getLocalizations().size();
		int nTimeslots = event.getTimeslots().size();
		
		int[][][] matrix = new int[nPlayers][][];
		for (int p = 0; p < nPlayers; p++) {
			matrix[p] = new int[nLocalizations][];
			for (int c = 0; c < nLocalizations; c++) {
				matrix[p][c] = new int[nTimeslots];
				for (int t = 0; t < nTimeslots; t++)
					matrix[p][c][t] = x[p][c][t].getValue();
			}
		}
		
		return matrix;
	}
}