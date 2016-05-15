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
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;

import data.model.schedule.EventSchedule;
import data.model.tournament.Tournament;
import data.model.tournament.event.Event;
import data.model.tournament.event.domain.Localization;
import data.model.tournament.event.domain.Player;
import data.model.tournament.event.domain.timeslot.Timeslot;
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
	 * Estrategia de búsqueda a aplicar en el proceso de resolución
	 *
	 */
	public enum SearchStrategy {
		/**
		 * Estrategia domOverWDeg definida por Choco 3 
		 */
		DOMOVERWDEG,
		
		/**
		 * Estrategia minDom_LB definida por Choco 3
		 */
		MINDOM_UB,
		
		/**
		 * Estrategia minDom_UB definida por Choco 3 
		 */
		MINDOM_LB
	}
	
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
	 * Emparejamientos predefinidos
	 */
	private Map<Event, List<Set<Player>>> predefinedMatchups = new HashMap<Event, List<Set<Player>>>();
	
	/**
	 * Por categoría, diccionario de jugadores para cada cual los enfrentamientos de los que forme parte han de tener lugar
	 * en cualquiera de las localizaciones de la lista de localizaciones de juego vinculada a su entrada
	 */
	private Map<Event, Map<Player, Set<Localization>>> playersInLocalizations = new HashMap<Event, Map<Player, Set<Localization>>>();
	
	/**
	 * Por categoría, diccionario de jugadores para cada cual los enfrentamientos de los que forme parte han de tener lugar
	 * en cualquiera de las horas de la lista de timeslots vinculada a su entrada
	 */
	private Map<Event, Map<Player, Set<Timeslot>>> playersAtTimeslots = new HashMap<Event, Map<Player, Set<Timeslot>>>();
	
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
	 * Indica si se ha encontrado la última solución
	 */
	private boolean lastSolutionFound = false;
	
	private long foundSolutionsCount = 0;
	
	/**
	 * Estrategia de búsqueda empleada en la resolución del problema
	 */
	private SearchStrategy searchStrategy = SearchStrategy.DOMOVERWDEG;
	
	/**
	 * Para las estrategias de búsqueda minDom_UB y minDom_LB indicar si priorizar timeslots (true) o
	 * pistas (false) a la hora de hacer las asignaciones
	 */
	private boolean fillTimeslotsFirst = true;
	
	/**
	 * Tiempo máximo de resolución en milisegundos. 0 significa sin límite
	 */
	private long resolutionTimeLimit = 0;
	
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
		
		LOGGER.setLevel(Level.WARNING);

		List<Event> events = tournament.getEvents();
		
		int nCategories = events.size();

		x = new IntVar[nCategories][][][];
		g = new IntVar[nCategories][][][];
		
		for (int e = 0; e < events.size(); e++) {
			Event event = events.get(e);
			int nPlayers = event.getPlayers().size();
			int nLocalizations = event.getLocalizations().size();
			int nTimeslots = event.getTimeslots().size();
			
			x[e] = new IntVar[nPlayers][nLocalizations][nTimeslots];
			g[e] = new IntVar[nPlayers][nLocalizations][nTimeslots];
		}
	}
	
	public Solver getInternalSolver() {
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
	
	public void setSearchStrategy(SearchStrategy strategy) {
		searchStrategy = strategy;
	}
	
	public SearchStrategy getSearchStrategy() {
		return searchStrategy;
	}
	
	public void setFillTimeslotsFirst(boolean fillFirst) {
		fillTimeslotsFirst = fillFirst;
	}
	
	public boolean getFillTimeslotsFirst() {
		return fillTimeslotsFirst;
	}
	
	public long getFoundSolutionsCount() {
		return foundSolutionsCount;
	}
	
	/**
	 * Establece el tiempo máximo de resolución del problema en milisegundos.
	 * <p>
	 * El valor de 0 es el valor por defecto e indica que no hay límite.
	 * 
	 * @param limit número mayor o igual que 0
	 */
	public void setResolutionTimeLimit(long limit) {
		if (limit < 0)
			throw new IllegalArgumentException("Resolution time limit cannot be less than zero");
		resolutionTimeLimit = limit;
	}
	
	public long getResolutionTimeLimit() {
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
		
		configureSearch();
		
		return solve();
	}
	
	/**
	 * Crea el objeto solver de Choco
	 */
	private void createSolver() {
		solver = new Solver("Tournament Solver");
		foundSolutionsCount = 0;
		lastSolutionFound = false;
		schedules = null;
		resolutionData = null;
	}
	
	/**
	 * Inicializa las variables del modelo del problema
	 */
	private void buildModel() {
		List<Event> events = tournament.getEvents();
		
		// Añadir a los emparejamientos predefinidos, jugadores en pista y jugadores en horas a las colecciones correspondientes
		for (Event event : events) {
			if (event.hasPredefinedMatchups())
				predefinedMatchups.put(event, event.getPredefinedMatchups());
			
			if (event.hasPlayersInLocalizations())
				playersInLocalizations.put(event, event.getPlayersInLocalizations());
			
			if (event.hasPlayersAtTimeslots())
				playersAtTimeslots.put(event, event.getPlayersAtTimeslots());
		}
		
		buildMatrices();
		
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
	private void buildMatrices() {
		List<Event> events = tournament.getEvents();
		
		for (int e = 0; e < events.size(); e++) {
			Event event = events.get(e);
			int nPlayers = event.getPlayers().size();
			int nLocalizations = event.getLocalizations().size();
			int nTimeslots = event.getTimeslots().size();
			int nTimeslotsPerMatch = event.getTimeslotsPerMatch();
			
			Map<Player, Set<Timeslot>> eventUnavailabilities = event.getUnavailablePlayers();
			
			for (int p = 0; p < nPlayers; p++) {
				Set<Timeslot> playerUnavailabilities = eventUnavailabilities.get(event.getPlayers().get(p));
				for (int c = 0; c < nLocalizations; c++) {
					for (int t = 0; t < nTimeslots; t++) {
						// Si el jugador_p no está disponible a la hora_t se marca con 0
						if (playerUnavailabilities != null && playerUnavailabilities.contains(event.getTimeslots().get(t))) {
							int nRange = nTimeslotsPerMatch;
							if (t + 1 < nTimeslotsPerMatch)
								nRange -= nTimeslotsPerMatch - t - 1;
							
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
		List<Event> events = tournament.getEvents();
		
		// Marcar las localizaciones descartadas con 0
		for (int e = 0; e < events.size(); e++) {
			Event event = events.get(e);
			int nPlayers = event.getPlayers().size();
			
			Map<Localization, Set<Timeslot>> discardedLocalizations = event.getUnavailableLocalizations();
			Set<Localization> localizations = discardedLocalizations.keySet();
			
			for (Localization localization : localizations) {
				Set<Timeslot> timeslots = discardedLocalizations.get(localization);
				int nDiscardedTimeslots = timeslots.size();
				
				int[] tIndex = new int[nDiscardedTimeslots];
				
				int i = 0;
				for (Timeslot timeslot : timeslots)
					tIndex[i++] = event.getTimeslots().indexOf(timeslot);
				
				int c = event.getLocalizations().indexOf(localization);
				
				for (int p = 0; p < nPlayers; p++)
					for (int t = 0; t < nDiscardedTimeslots; t++) {
						x[e][p][c][tIndex[t]] = VariableFactory.fixed(0, solver);
						g[e][p][c][tIndex[t]] = VariableFactory.fixed(0, solver);
					}
			}
		}
	}
	
	/**
	 * Fuerza a que los jugadores indicados jueguen sus partidos en las localizaciones indicadas, marcando todas las demás
	 * con 0
	 */
	private void markPlayersNotInLocalizations() {
		List<Event> events = tournament.getEvents();
		
		// Si para el jugador_p en la categoría_e se indica que debe jugar en un conjunto de localizaciones,
		// se marcan con 0 todas las localizaciones del evento que no sean ésas, de este modo invalidándolas
		for (int e = 0; e < events.size(); e++) {
			Event event = events.get(e);
			int nLocalizations = event.getLocalizations().size();
			int nTimeslots = event.getTimeslots().size();
			
			if (playersInLocalizations.containsKey(event)) {
				Map<Player, Set<Localization>> eventPlayersInLocalizations = playersInLocalizations.get(event);
				Set<Player> players = eventPlayersInLocalizations.keySet();
				
				// Para cada jugador al que se le ha indicado una lista de pistas donde jugar, "invalidar" las pistas
				// que no pertenecen a esa lista
				for (Player player : players) {
					Set<Localization> assignedLocalizations = eventPlayersInLocalizations.get(player);
					
					int p = event.getPlayers().indexOf(player);
					
					for (int c = 0; c < nLocalizations; c++) {
						// Si la pista no es de las asignadas al jugador
						if (!assignedLocalizations.contains(event.getLocalizations().get(c))) {
							for (int t = 0; t < nTimeslots; t++) {
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
	 * Fuerza a que los jugadores indicados jueguen sus partidos en los timeslots indicados, marcando todos los
	 * demás con 0
	 */
	private void markPlayersNotAtTimeslots() {
		List<Event> events = tournament.getEvents();
		
		// Si para el jugador_p en la categoría_e se indica que debe jugar en un conjunto de timeslots,
		// se marcan con 0 todos los timeslots del evento que no esan ésos, de este modo invalidándolos
		for (int e = 0; e < events.size(); e++) {
			Event event = events.get(e);
			int nLocalizations = event.getLocalizations().size();
			int nTimeslots = event.getTimeslots().size();
			
			if (playersAtTimeslots.containsKey(event)) {
				Map<Player, Set<Timeslot>> eventPlayersAtTimeslots = playersAtTimeslots.get(event);
				Set<Player> players = eventPlayersAtTimeslots.keySet();
				
				for (Player player : players) {
					Set<Timeslot> assignedTimeslots = eventPlayersAtTimeslots.get(player);
					
					int p = event.getPlayers().indexOf(player);
					
					for (int t = 0; t < nTimeslots; t++) {
						if (!assignedTimeslots.contains(event.getTimeslots().get(t))) {
							for (int c = 0; c < nLocalizations; c++) {
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
		List<Event> events = tournament.getEvents();
		
		// Marcar los breaks con 0
		for (int e = 0; e < events.size(); e++) {
			Event event = events.get(e);
			if (event.hasBreaks()) {
				int nPlayers = event.getPlayers().size();
				int nLocalizations = event.getLocalizations().size();
				int nTimeslots = event.getTimeslots().size();
				
				for (int t = 0; t < nTimeslots; t++) {
					// Si el timeslot_t es un break, entonces en él no se puede jugar y se marca como 0
					if (event.isBreak(event.getTimeslots().get(t))) {
						for (int p = 0; p < nPlayers; p++) {
							for (int c = 0; c < nLocalizations; c++) {
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
	 * Construye todas las restricciones del problema 
	 */
	private void setupConstraints() {
		ConstraintBuilder builder = null;
		
		for (Event event : tournament.getEvents()) {
			// Restricciones de equipos
			if (event.hasTeams()) {
				builder = new ConstraintBuilder(new TeamsConstraint(event));
				constraints.addAll(builder.getConstraints());
			}
			
			// Restricciones de modo de enfrentamiento
			if (event.getMatchesPerPlayer() > 1 && event.getPlayersPerMatch() > 1) {
				MatchupMode mode = event.getMatchupMode();
				if (mode == MatchupMode.ALL_DIFFERENT || mode == MatchupMode.ALL_EQUAL) {
					builder = new ConstraintBuilder(new MatchupModeConstraint(event));
					constraints.addAll(builder.getConstraints());
				}
			}
			
			// Restricciones de suma de partidos
			builder = new ConstraintBuilder(new TotalMatchesConstraint(event));
			constraints.addAll(builder.getConstraints());
			
			// Restricciones de emparejamientos predefinidos
			if (!predefinedMatchups.isEmpty() && predefinedMatchups.containsKey(event)) {
				builder = new ConstraintBuilder(new PredefinedMatchupsConstraint(event));
				constraints.addAll(builder.getConstraints());
			}
			
			// Restricciones de número de partidos por jugador
			builder = new ConstraintBuilder(new MatchesPerPlayerConstraint(event));
			constraints.addAll(builder.getConstraints());
			
			// Restricciones de número de jugadores en la misma pista
			builder = new ConstraintBuilder(new LocalizationOccupationConstraint(event));
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
	private void configureSearch() {
		List<Event> events = tournament.getEvents();
		int nCategories = events.size();
		
		IntVar[][][] vars = new IntVar[nCategories][][];
		
		if ((searchStrategy == SearchStrategy.MINDOM_LB || searchStrategy == SearchStrategy.MINDOM_UB) && !fillTimeslotsFirst) {
			IntVar[][][][] v = new IntVar[nCategories][][][];
			for (int e = 0; e < nCategories; e++) {
				Event event = events.get(e);
				int nPlayers = event.getPlayers().size();
				int nLocalizations = event.getLocalizations().size();
				int nTimeslots = event.getTimeslots().size();
				
				v[e] = new IntVar[nPlayers][][];
				for (int p = 0; p < nPlayers; p++) {
					v[e][p] = new IntVar[nTimeslots][];
					for (int t = 0; t < nTimeslots; t++) {
						v[e][p][t] = new IntVar[nLocalizations];
						for (int c = 0; c < nLocalizations; c++) {
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
		
		IntVar[] v = ArrayUtils.flatten(vars);
		switch (searchStrategy) {
			case DOMOVERWDEG:
				solver.set(IntStrategyFactory.domOverWDeg(v, System.currentTimeMillis()));
				break;
			case MINDOM_UB:
				solver.set(IntStrategyFactory.minDom_UB(v));
				break;
			case MINDOM_LB:
				solver.set(IntStrategyFactory.minDom_LB(v));
				break;
			default:
				solver.set(IntStrategyFactory.domOverWDeg(v, 0));
				break;
		}
	}
	
	/**
	 * Devuelve el nombre de la estrategora de búsqueda empleada. Para domOverWDeg, el valor indicado
	 * entre paréntesis se trata de la semilla utilizada, siendo t obtenida mediante {@link System#currentTimeMillis()}
	 * 
	 * @return el nombre de la estrategia o estrategias de búsqueda empleadas
	 */
	private String getSearchStrategyName() {
		String searchStrategyStr = "";
		switch (searchStrategy) {
			case DOMOVERWDEG:
				searchStrategyStr = "domOverWDeg(t)";
				break;
			case MINDOM_UB:
				searchStrategyStr = "minDom_UB";
				break;
			case MINDOM_LB:
				searchStrategyStr = "minDom_LB";
				break;
			default:
				searchStrategyStr = "domOverWDeg(0)";
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
		if (lastSolutionFound)
			schedules = null;
		
		else if (solver.isFeasible() != ESat.TRUE) {
			LOGGER.log(Level.INFO, "No schedules available; solution was not found.");
			schedules = null;
			
		} else if (schedules == null) {   // Se ha encontrado la primera solución
			schedules = new HashMap<Event, EventSchedule>(tournament.getEvents().size());
			buildSchedules();
			
		} else {                          // Se ha encontrado una siguiente solución
			if (solver.nextSolution()) {
				schedules.clear();
				buildSchedules();
			}
			else {
				LOGGER.log(Level.INFO, "All possible schedules have already been found.");
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
		List<Event> events = tournament.getEvents();
		for (int e = 0; e < events.size(); e++) {
			Event event = events.get(e);
			schedules.put(event, new EventSchedule(event, solutionMatrixToInt(event, x[e])));
		}
		foundSolutionsCount++;
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