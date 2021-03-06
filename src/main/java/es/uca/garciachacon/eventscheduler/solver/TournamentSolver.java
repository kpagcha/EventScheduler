package es.uca.garciachacon.eventscheduler.solver;

import es.uca.garciachacon.eventscheduler.data.model.schedule.EventSchedule;
import es.uca.garciachacon.eventscheduler.data.model.tournament.*;
import es.uca.garciachacon.eventscheduler.solver.constraint.*;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.objective.ObjectiveManager;
import org.chocosolver.solver.search.loop.monitors.SearchMonitorFactory;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Solucionador del problema que lo modela y resuelve, calculando los horarios de un torneo aplicando las reglas
 * definidas sobre el mismo.
 */
public class TournamentSolver {

    /**
     * Modos de emparejamiento (para categorías con más de un partido por jugador).
     * <p>
     * Si un evento define un modo de emparejamiento distinto de CUSTOM, el número de ocurrencias que cada
     * enfrentamiento predefinido especifique será ignorado y, por tanto, tendrá preferencia el modo de
     * enfrentamiento.
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
         * Los emparejamientos se pueden dar entre cualquier jugador o equipo disponible en cualquier número. Se pueden
         * repetir
         * emparejamientos
         */
        ANY,

        /**
         * El número de emparejamientos entre los jugadores será el indicado por el propio enfrentamiento
         */
        CUSTOM
    }

    /**
     * Estrategia de búsqueda a aplicar en el proceso de resolución
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
     * Estado del proceso de resolución
     */
    public enum ResolutionState {
        /**
         * Aún no ha comenzado
         */
        READY,

        /**
         * Está en progreso, y no se ha llegado a la última solución
         */
        STARTED,

        /**
         * Ha completado y se ha encontrado alguna solución
         */
        FINISHED,

        /**
         * Ha completado, pero no se ha encontrado ninguna solución
         */
        UNFEASIBLE,

        /**
         * No se ha logrado completar el proceso por limitaciones del entorno
         */
        INCOMPLETE,

        /**
         * Se está computando la solución
         */
        COMPUTING
    }

    /**
     * Modo de optimización en la búsqueda de la solución
     */
    public enum OptimizationMode {
        /**
         * Sin optimización. Se seleccionan todas las soluciones al problema
         */
        NONE,

        /**
         * Solución óptima (tanto maximizando como minimizando)
         */
        OPTIMAL,

        /**
         * Cada solución es mejor o igual que la anterior
         */
        STEP,

        /**
         * Cada solución es estrictamente mejor que la anterior
         */
        STEP_STRICT
    }

    /**
     * Logger del solver
     */
    private static final Logger LOGGER = Logger.getLogger(TournamentSolver.class.getName());

    /**
     * Restricciones del problema
     */
    private final List<Constraint> constraints = new ArrayList<>();

    /**
     * Torneo para el que se calcula el horario
     */
    private final Tournament tournament;

    /**
     * Horario. x_e,p,c,t: horario_categoria,jugador,pista,hora. Dominio [0, 1]
     */
    private final IntVar[][][][] x;

    /**
     * Comienzos de partidos. Dominio [0, 1]
     */
    private final IntVar[][][][] g;

    /**
     * Solver de Choco que modela y resuelve el problema
     */
    private Solver solver;

    /**
     * Horarios calculados correspondientes a la solución actual
     */
    private Map<Event, EventSchedule> schedules;

    /**
     * Estado del proceso de la resolución del problema
     */
    private volatile ResolutionState resolutionState = ResolutionState.READY;

    /**
     * Contador de soluciones encontradas
     */
    private long foundSolutions = 0;

    /**
     * Estrategia de búsqueda empleada en la resolución del problema
     */
    private SearchStrategy searchStrategy = SearchStrategy.DOMOVERWDEG;

    /**
     * Para las estrategias de búsqueda minDom_UB y minDom_LB indicar si priorizar <i>timeslots</i>
     * (<code>true</code>) o localizaciones (<code>false</code>) a la hora de hacer las asignaciones de horario
     */
    private boolean prioritizeTimeslots = false;

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
     * Modo de optimización en la búsqueda de la solución
     */
    private OptimizationMode optimizationMode = OptimizationMode.NONE;

    /**
     * Política de resolución en los problemas de optimización
     */
    private ResolutionPolicy resolutionPolicy = ResolutionPolicy.MAXIMIZE;

    /**
     * Puntuación objetivo (para problemas de optimización)
     */
    private IntVar score;

    /**
     * Construye un <i>solver</i> a partir de la información del torneo.
     *
     * @param tournament torneo para el que se va a modelar el problema, calcular horario y formar partidos
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

    /**
     * Constructor de copia que crea una instancia de <i>solver</i> a partir de otro existente. Tendrá el mismo torneo
     * que la copia, y además se reproducen propiedades de configuración como son la estrategia de búsqueda, la
     * priorización de <i>timeslots</i> o jugadores, o el tiempo límite de resolución
     *
     * @param aSolver <i>solver</i> existente a partir del cual se va a crear una copia
     */
    public TournamentSolver(TournamentSolver aSolver) {
        this(aSolver.getTournament());
        searchStrategy = aSolver.getSearchStrategy();
        prioritizeTimeslots = aSolver.getPrioritizeTimeslots();
        resolutionTimeLimit = aSolver.getResolutionTimeLimit();
    }

    public void setLoggerLevel(Level level) {
        LOGGER.setLevel(level);
    }

    /**
     * Devuelve el <i>solver</i> de Choco usado por esta clase para llevar a cabo la resolución del problema
     *
     * @return <i>solver</i> de Choco
     */
    public Solver getInternalSolver() {
        return solver;
    }

    /**
     * Devuelve el torneo para el que este <i>solver</i> calculará sus horarios
     *
     * @return el torneo de este <i>solver</i>
     */
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
     * Devuelve la estrategia de búsqueda empleada en la resolución del problema.
     *
     * @return estrategia de búsqueda empleada en la resolución
     */
    public SearchStrategy getSearchStrategy() {
        return searchStrategy;
    }

    /**
     * Establece la estrategia de búsqueda a emplear en el proceso de resolución. Una vez éste comience, si se cambia
     * la estrategia de búsqueda no tendrá efecto hasta reiniciar el proceso.
     *
     * @param strategy estrategia de búsqueda para la resolución del problema
     */
    public void setSearchStrategy(SearchStrategy strategy) {
        Objects.requireNonNull(strategy);

        searchStrategy = strategy;
    }

    /**
     * Devuelve si en la resolución, al calcular los horarios, se intentará priorizar la ocupación de
     * <i>timeslots</i>, o de localizaciones de juego. Esta priorización solamente tiene efecto si la estrategia de
     * búsqueda configurada es {@link SearchStrategy#MINDOM_UB} o {@link SearchStrategy#MINDOM_LB}.
     *
     * @return <code>true</code> si se priorizan <i>timeslots</i>; <code>false</code> si se priorizan localizaciones
     */
    public boolean getPrioritizeTimeslots() {
        return prioritizeTimeslots;
    }

    /**
     * Fija si al calcular los horarios se priorizará la ocupación de <i>timeslots</i> o de jugadores. Sólamente
     * tiene efecto si la estrategia de búsqueda configurada es {@link SearchStrategy#MINDOM_UB} o
     * {@link SearchStrategy#MINDOM_LB}.
     *
     * @param prioritize <code>true</code> para priorizar <i>timeslots</i>, y <code>false</code> para priorizar
     *                   jugadores
     */
    public void prioritizeTimeslots(boolean prioritize) {
        prioritizeTimeslots = prioritize;
    }

    /**
     * Devuelve el número de soluciones encontradas hasta el momento. Si el torneo no tiene ningún horario posible,
     * devolverá 0.
     *
     * @return número de soluciones encontradas
     */
    public long getFoundSolutions() {
        return foundSolutions;
    }

    /**
     * Devuelve el tiempo máximo de resolución en milisegundos. Si devuelve 0 significa que no hay límite.
     *
     * @return tiempo máximo de resolución en milisegundos positivo, o 0 si no hay límite
     */
    public long getResolutionTimeLimit() {
        return resolutionTimeLimit;
    }

    /**
     * Devuelve el estado del proceso de resolución: si ha comenzado, si está en progreso, si ha terminado y no hay
     * solución o si ha terminado y se encontraron soluciones
     *
     * @return el estado actual del proceso de resolución
     */
    public ResolutionState getResolutionState() {
        return resolutionState;
    }

    /**
     * Comprueba si el proceso de resolución ha comenzado, es decir, si ya se ha invocado
     * {@link TournamentSolver#execute()}.
     * <p>
     * Si el proceso de resolución ha terminado, no ha encontrado solución o ha sido incompleto, devuelve
     * <code>false</code>.
     *
     * @return <code>true</code> si ya ha comenzado el proceso de resolución, <code>false</code> si no
     */
    public boolean hasResolutionProcessStarted() {
        return resolutionState == ResolutionState.STARTED || resolutionState == ResolutionState.COMPUTING;
    }

    /**
     * Comprueba si el proceso de resolución ya ha terminado, es decir, se ha encontrado la última solución posible,
     * o si no se encontró ninguna solución al lanzar el proceso de resolución.
     * <p>
     * Si no se encontró resolución debido a las limitaciones del proceso, es decir, el estado es
     * {@link ResolutionState#INCOMPLETE}, no se considera como proceso terminado, luego se devuelve <code>false</code>.
     *
     * @return <code>true</code> si ya ha terminado el proceso de resolución, <code>false</code> si no
     */
    public boolean hasResolutionProcessFinished() {
        return resolutionState == ResolutionState.FINISHED || resolutionState == ResolutionState.UNFEASIBLE;
    }

    /**
     * Comprueba si el torneo tiene solución, es decir, si se ha encontrado al menos una. Si el proceso de resolución
     * aún no ha comenzado, se devolverá <code>false</code>. Si ha terminado y se encontraron soluciones, se
     * devolverá <code>true</code>.
     *
     * @return <code>true</code> si el torneo tiene solución; <code>false</code> si no tiene, o si no ha comenzado el
     * proceso de resolución
     */
    public boolean hasSolutions() {
        return resolutionState == ResolutionState.STARTED || resolutionState == ResolutionState.FINISHED;
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

    public ResolutionData getResolutionData() {
        return resolutionData;
    }

    /**
     * Establece el modo de optimización en la resolución del problema y si se debe maximizar o minimizar la función
     * objetivo.
     *
     * La función objetivo se define como la mayor concentración de partidos en los <i>timeslots</i> más tempranos de
     * cada categoría del torneo.
     *
     * @param optimizationMode modo de optimización a aplicar
     * @param resolutionPolicy maximización o minimización de la puntuación
     */
    public void setOptimization(OptimizationMode optimizationMode, ResolutionPolicy resolutionPolicy) {
        this.optimizationMode = optimizationMode;
        this.resolutionPolicy = resolutionPolicy;
    }

    /**
     * Establece el modo de optimización en la resolución del problema. Por omisión, se maximizará el objetivo. O si
     * el modo de optimización es {@link OptimizationMode#NONE} se ignorará la maximización o minimización.
     *
     * La función objetivo se define como la mayor concentración de partidos en los <i>timeslots</i> más tempranos de
     * cada categoría del torneo.
     *
     * @param optimizationMode modo de optimización a aplicar
     */
    public void setOptimization(OptimizationMode optimizationMode) {
        setOptimization(optimizationMode, ResolutionPolicy.MAXIMIZE);
    }

    /**
     * Devuelve la puntuación (valor de la función objetivo) de la solución actual. Al llamar este método debe haber
     * configurado un modo de optimización y una solución calculada.
     *
     * @return un entero que representa la puntuación de la solución actual como problema de optimización
     *
     * @throws IllegalStateException si no existe una solución
     * @throws IllegalStateException si el problema actual no es de optimización
     */
    public int getScore() {
        if (resolutionState != ResolutionState.STARTED || foundSolutions == 0)
            throw new IllegalStateException("There cannot be a score if there is no solution");

        if (optimizationMode == OptimizationMode.NONE)
            throw new IllegalStateException("No optimization mode was configured");

        return score.getValue();
    }

    /**
     * Construye y modela el problema, configura las estrategias de búsqueda e inicia el proceso de resolución. Si
     * hay un modo de optimización configurado éste será aplicado en la búsqueda de la solución y afectará al
     * conjunto final de soluciones.
     *
     * @return true si se ha encontrado una solución, false si no
     */
    public boolean execute() {
        solver = new Solver("Tournament Solver [" + tournament.getName() + "]");

        schedules = null;

        resolutionState = ResolutionState.STARTED;
        foundSolutions = 0;
        stop = false;

        buildModel();

        configureSearch();

        if (optimizationMode != OptimizationMode.NONE)
            postObjective();

        return solve();
    }

    /**
     * Inicializa las variables del modelo del problema
     */
    private void buildModel() {
        List<Event> events = tournament.getEvents();

        for (int e = 0; e < events.size(); e++) {
            Event event = events.get(e);
            int nPlayers = event.getPlayers().size();
            int nLocalizations = event.getLocalizations().size();
            int nTimeslots = event.getTimeslots().size();

            for (int p = 0; p < nPlayers; p++)
                for (int c = 0; c < nLocalizations; c++)
                    for (int t = 0; t < nTimeslots; t++) {
                        // Dominio [0, 1]: 0 -> no juega, 1 -> juega
                        x[e][p][c][t] = VariableFactory.bounded("x" + e + "," + p + "," + c + "," + t, 0, 1, solver);

                        // Dominio [0, 1]: 0 -> el partido no empieza a esa hora, 1 -> el partido empieza a esa hora
                        g[e][p][c][t] = VariableFactory.bounded("g" + e + "," + p + "," + c + "," + t, 0, 1, solver);
                    }
        }

        markUnavailablePlayers();
        markUnavailableLocalizations();
        markPlayersNotInLocalizations();
        markPlayersNotAtTimeslots();
        markBreaks();

        setupConstraints();
        postConstraints();
    }

    /**
     * Marca los jugadores no disponibles en las horas especificadas en las matrices del problema
     */

    private void markUnavailablePlayers() {
        List<Event> events = tournament.getEvents();

        // Marcar los jugadores no disponibles con 0
        for (int e = 0; e < events.size(); e++) {
            Event event = events.get(e);

            if (event.hasUnavailablePlayers()) {
                int nTimeslotsPerMatch = event.getTimeslotsPerMatch();

                Map<Player, Set<Timeslot>> unavailablePlayers = event.getUnavailablePlayers();

                for (Player player : unavailablePlayers.keySet()) {
                    List<Player> players = event.getPlayers();
                    Set<Timeslot> unavailableTimeslots = unavailablePlayers.get(player);

                    int nLocalizations = event.getLocalizations().size();
                    int nUnavailableTimeslots = unavailableTimeslots.size();

                    int[] tIndex = new int[nUnavailableTimeslots];

                    int pos = 0;
                    for (Timeslot timeslot : unavailableTimeslots)
                        tIndex[pos++] = event.getTimeslots().indexOf(timeslot);

                    int p = players.indexOf(player);

                    for (int t : tIndex) {
                        int nRange = nTimeslotsPerMatch;
                        if (t + 1 < nTimeslotsPerMatch)
                            nRange -= nTimeslotsPerMatch - t - 1;

                        for (int c = 0; c < nLocalizations; c++) {
                            // Si un jugador no está disponible en t, n no podrá empezar un partido en el rango t-n..t
                            // (siendo n la duración o número de timeslots de un partido)
                            for (int i = 0; i < nRange; i++)
                                g[e][p][c][t - i] = VariableFactory.fixed(0, solver);

                            // Además, se marca con 0 las horas de la matriz de horario/partidos si el jugador no
                            // puede jugar
                            x[e][p][c][t] = VariableFactory.fixed(0, solver);
                        }
                    }
                }
            }
        }
    }

    /**
     * Marca las localizaciones no disponibles a las horas especificadas en las matrices del problema.
     */
    private void markUnavailableLocalizations() {
        List<Event> events = tournament.getEvents();

        // Marcar las localizaciones descartadas con 0
        for (int e = 0; e < events.size(); e++) {
            Event event = events.get(e);

            if (event.hasUnavailableLocalizations()) {
                int nPlayers = event.getPlayers().size();

                Map<Localization, Set<Timeslot>> unavailableLocalizations = event.getUnavailableLocalizations();
                Set<Localization> localizations = unavailableLocalizations.keySet();

                for (Localization localization : localizations) {
                    Set<Timeslot> timeslots = unavailableLocalizations.get(localization);
                    int nUnavailableTimeslots = timeslots.size();

                    int[] tIndex = new int[nUnavailableTimeslots];

                    int i = 0;
                    for (Timeslot timeslot : timeslots)
                        tIndex[i++] = event.getTimeslots().indexOf(timeslot);

                    int c = event.getLocalizations().indexOf(localization);

                    for (int p = 0; p < nPlayers; p++)
                        for (int t = 0; t < nUnavailableTimeslots; t++) {
                            x[e][p][c][tIndex[t]] = VariableFactory.fixed(0, solver);
                            g[e][p][c][tIndex[t]] = VariableFactory.fixed(0, solver);
                        }
                }
            }
        }
    }

    /**
     * Fuerza a que los jugadores indicados jueguen sus partidos en las localizaciones indicadas, marcando todas las
     * demás con 0
     */
    private void markPlayersNotInLocalizations() {
        List<Event> events = tournament.getEvents();

        // Si para el jugador_p en la categoría_e se indica que debe jugar en un conjunto de localizaciones,
        // se marcan con 0 todas las localizaciones del evento que no sean ésas, de este modo invalidándolas
        for (int e = 0; e < events.size(); e++) {
            Event event = events.get(e);

            if (event.hasPlayersInLocalizations()) {
                int nLocalizations = event.getLocalizations().size();
                int nTimeslots = event.getTimeslots().size();
                Map<Player, Set<Localization>> eventPlayersInLocalizations = event.getPlayersInLocalizations();
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
     * Fuerza a que los jugadores indicados jueguen sus partidos en los timeslots indicados, marcando todos los demás
     * con 0.
     */
    private void markPlayersNotAtTimeslots() {
        List<Event> events = tournament.getEvents();

        // Si para el jugador_p en la categoría_e se indica que debe jugar en un conjunto de timeslots,
        // se marcan con 0 todos los timeslots del evento que no esan ésos, de este modo invalidándolos
        for (int e = 0; e < events.size(); e++) {
            Event event = events.get(e);

            if (event.hasPlayersAtTimeslots()) {
                int nLocalizations = event.getLocalizations().size();
                int nTimeslots = event.getTimeslots().size();
                Map<Player, Set<Timeslot>> eventPlayersAtTimeslots = event.getPlayersAtTimeslots();
                Set<Player> players = eventPlayersAtTimeslots.keySet();

                for (Player player : players) {
                    Set<Timeslot> assignedTimeslots = eventPlayersAtTimeslots.get(player);

                    int p = event.getPlayers().indexOf(player);

                    for (int t = 0; t < nTimeslots; t++) {
                        if (!assignedTimeslots.contains(event.getTimeslots().get(t))) {
                            for (int c = 0; c < nLocalizations; c++) {
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
        ConstraintBuilder builder;

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
            if (event.hasPredefinedMatchups()) {
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
        //builder = new ConstraintBuilder(new MatchStartMappingConstraint(tournament));
        //constraints.addAll(builder.getConstraints());

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
     * Publica todas las restricciones del modelo.
     */
    private void postConstraints() {
        solver.post(constraints.toArray(new Constraint[constraints.size()]));
    }

    /**
     * Configura la estrategia de búsqueda a emplear durante el proceso de resolución.
     */
    private void configureSearch() {
        List<Event> events = tournament.getEvents();
        int nCategories = events.size();

        IntVar[][] vars = new IntVar[nCategories][];

        if ((searchStrategy == SearchStrategy.MINDOM_LB || searchStrategy == SearchStrategy.MINDOM_UB) &&
                !prioritizeTimeslots) {
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
     * Lanza por primera vez el proceso de resolución para buscar una solución al problema modelado.
     * <p>
     * Si se encuentra una solución se actualiza el modelo con la solución y se devuelve <code>true</code>. Si no hay
     * una posible solución (el problema no es factible), o se ha alcanzado el tiempo límite de resolución se devuelve
     * <code>false</code>.
     *
     * @return <code>true</code> si se encuentra una solución, o <code>false</code> si no se ha encontrado o no se ha
     * podido encontrar bajo los límites configurados
     * @throws IllegalStateException si el proceso de resolución ya ha sido lanzado (desde otro hilo)
     */
    private boolean solve() {
        if (resolutionState == ResolutionState.COMPUTING)
            throw new IllegalStateException("Resolution process has already been launched");

        if (resolutionTimeLimit > 0)
            SearchMonitorFactory.limitTime(solver, resolutionTimeLimit);

        solver.addStopCriterion(() -> stop);

        resolutionState = ResolutionState.COMPUTING;

        switch (optimizationMode) {
            case OPTIMAL:
                solver.findOptimalSolution(resolutionPolicy, score);
                break;
            case STEP:
                solver.set(new ObjectiveManager(score, resolutionPolicy, false));
                break;
            case STEP_STRICT:
                solver.set(new ObjectiveManager(score, resolutionPolicy, true));
                break;
        }

        boolean solutionFound = solver.findSolution();

        if (solutionFound) {
            resolutionState = ResolutionState.STARTED;
            foundSolutions++;
        } else {
            if (solver.isFeasible() == ESat.FALSE) {
                LOGGER.log(Level.INFO, "Problem unfeasible");
                resolutionState = ResolutionState.UNFEASIBLE;
            } else if (solver.isFeasible() == ESat.UNDEFINED) {
                LOGGER.log(Level.INFO, "Solution could not be found within given limits");
                resolutionState = ResolutionState.INCOMPLETE;
            }
        }

        resolutionData = new ResolutionData(this);

        return solutionFound;
    }

    /**
     * Para el proceso de resolución, dejándolo en estado incompleto y quedando la solución en estado desconocido,
     * pudiendo haber sido calculada si se hubiese empleado más tiempo de computación, o bien puede ocurrir que no
     * hubiera solución posible.
     * <p>
     * Solamente tiene efecto si el cómputo de la solución se está llevando a cabo en este momento.
     */
    public void stopResolutionProcess() {
        if (resolutionState != ResolutionState.COMPUTING)
            return;

        stop = true;
        resolutionState = ResolutionState.INCOMPLETE;
    }

    /**
     * Si no se ha intentado resolver el modelo, es decir, no se ha llamado a {@link TournamentSolver#execute()}, no
     * habrá una solución disponible y se devolverá un opcional vacío, {@link Optional#empty()}.
     * <p>
     * Si es la primera vez que se invoca a este método despues de iniciar el proceso de resolución, se contruye el
     * horario con esa primera solución y se devuelve.
     * <p>
     * Las subsiguientes invocaciones a este método calcularán la siguiente solución al problema, se construirá otro
     * horario alternativo diferente al anterior, sobreescribiendo su valor, y se devuelve este nuevo horario. Si no
     * hay más soluciones disponibles, se devuelve el valor del horario a su estado inicial, es decir,
     * <code>null</code> y se marca el proceso de resolución como finalizado.
     *
     * @return los horarios de cada categoría del torneo envueltos en un {@link Optional}; si los horarios son
     * <code>null</code> se devuelve {@link Optional#empty()}
     * @throws IllegalStateException si la solución aún está siendo calculada
     */
    public Optional<Map<Event, EventSchedule>> getSolution() {
        if (resolutionState == ResolutionState.COMPUTING)
            throw new IllegalStateException("Solution is still being computed");

        if (resolutionState == ResolutionState.STARTED) {
            if (schedules == null && foundSolutions == 1) {
                schedules = new HashMap<>(tournament.getEvents().size());
                buildSchedules();
            } else if (solver.nextSolution()) {
                schedules.clear();
                buildSchedules();
                foundSolutions++;

                resolutionData = new ResolutionData(this);
            } else {
                LOGGER.log(Level.INFO, "All solutions found");
                schedules = null;
                resolutionState = ResolutionState.FINISHED;
            }
        }
        return Optional.ofNullable(schedules);
    }

    /**
     * Inicializa los horarios de cada categoría a partir de la solución calculada por el solver
     */
    private void buildSchedules() {
        List<Event> events = tournament.getEvents();
        for (int e = 0; e < events.size(); e++) {
            Event event = events.get(e);
            schedules.put(event, new EventSchedule(event, internalMatrixToInt(event, x[e])));
        }
    }

    /**
     * Transforma la matriz de IntVar de Choco en una matriz de enteros, tomando el valor actual de la solución
     *
     * @param event evento no nulo
     * @param x     matriz de IntVar inicializada
     * @return matriz de enteros con los correspondientes valores de la solución
     */
    private int[][][] internalMatrixToInt(Event event, IntVar[][][] x) {
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

    /**
     * Devuelve una representación en cadena con la matriz interna de horario que maneja el <i>solver</i>.
     *
     * @return cadena con la matrix interna de horario "x"
     */
    public String internalMatrixToString() {
        StringBuilder sb = new StringBuilder("Internal matrix 'x' (complete matches)\n\n");

        for (int e = 0; e < tournament.getEvents().size(); e++) {
            Event event = tournament.getEvents().get(e);
            List<Player> players = event.getPlayers();
            List<Localization> localizations = event.getLocalizations();
            List<Timeslot> timeslots = event.getTimeslots();

            sb.append(String.format("%s\n\n%8s\n", event.getName(), " "));

            for (int c = 0; c < localizations.size(); c++) {
                sb.append(String.format("(%d) %s\n", c, localizations.get(c)));

                for (int t = 0; t < timeslots.size(); t++)
                    sb.append(String.format("%4s", "t" + t));
                sb.append("\n");

                for (int p = 0; p < players.size(); p++) {
                    String playerStr = players.get(p).toString();
                    if (playerStr.length() > 8)
                        playerStr = playerStr.substring(0, 8);

                    sb.append(String.format("%8s", playerStr));
                    for (int t = 0; t < timeslots.size(); t++)
                        sb.append(String.format("%4s", x[e][p][c][t].getValue() == 0 ? "-" : "x"));
                    sb.append("\n");
                }
                sb.append("\n\n");
            }
        }

        return sb.toString();
    }

    /**
     * Devuelve una representación en cadena con la matriz interna de horario (comienzos de partidos) que maneja el
     * <i>solver</i>.
     *
     * @return cadena con la matrix interna de horario "g"
     */
    public String internalMatrixBeginningsToString() {
        StringBuilder sb = new StringBuilder("Internal matrix 'g' (beginnings of matches)\n\n");

        for (int e = 0; e < tournament.getEvents().size(); e++) {
            Event event = tournament.getEvents().get(e);
            List<Player> players = event.getPlayers();
            List<Localization> localizations = event.getLocalizations();
            List<Timeslot> timeslots = event.getTimeslots();

            sb.append(String.format("%s\n\n%8s\n", event.getName(), " "));

            for (int c = 0; c < localizations.size(); c++) {
                sb.append(String.format("(%d) %s\n\n", c, localizations.get(c)));

                for (int t = 0; t < timeslots.size(); t++)
                    sb.append(String.format("%4s", "t" + t));
                sb.append("\n");

                for (int p = 0; p < players.size(); p++) {
                    String playerStr = players.get(p).toString();
                    if (playerStr.length() > 8)
                        playerStr = playerStr.substring(0, 8);

                    sb.append(String.format("%8s", playerStr));
                    for (int t = 0; t < timeslots.size(); t++)
                        sb.append(String.format("%4s", g[e][p][c][t].getValue() == 0 ? "-" : "x"));
                    sb.append("\n");
                }
            }
        }

        return sb.toString();
    }

    private void postObjective() {
        List<Event> events = tournament.getEvents();

        // Puntuación de cada categoría
        int n = events.size();
        IntVar[] scores = new IntVar[n];

        int objMin = 0;
        int objMax = 0;

        for (int e = 0; e < n; e++) {
            Event event = events.get(e);
            int nPlayers = event.getPlayers().size();
            int nLocalizations = event.getLocalizations().size();
            int nTimeslots = event.getTimeslots().size();

            int nMatches = nPlayers * event.getMatchesPerPlayer();

            // la puntuación mínima de una categoría es que todos los partidos estén concentrados en el timeslot con
            // menor puntuación (1)
            int min = nMatches;

            // la puntuación máxima de uan categoría es que todos los partidos estén concentrados en el timeslot con
            // mayor puntuación (nTimeslots)
            int max = nMatches * nTimeslots;

            objMin += min;
            objMax += max;

            // Puntuación de esta categoría
            scores[e] = VariableFactory.bounded("category_" + e + "_score", min, max, solver);

            // Puntuaciones totales por separado de cada columna (timeslots) de esta categoría. El mínimo es 0 (no
            // hay ningún partido que puntúe) y el máximo es que todos los partidos estén en la columna que más puntúa
            IntVar[] categoryScores =
                    VariableFactory.boundedArray("category_" + e + "_scores", nTimeslots, 0, max, solver);

            for (int t = 0; t < nTimeslots; t++) {
                // Todas las puntuaciones particulares de cada hueco c,p en este t
                List<IntVar> timeslotScores = new ArrayList<>(nLocalizations * nPlayers);

                for (int c = 0; c < nLocalizations; c++) {
                    for (int p = 0; p < nPlayers; p++) {
                        // La puntuación de un hueco está entre 0 (0 * score_t) y el máximo individual (1 * n_timeslots)
                        IntVar value = VariableFactory.bounded("val_" + e + "," + p + "," + c + "," + t,
                                0,
                                nTimeslots,
                                solver
                        );

                        // El valor del hueco score_t es el producto de ese hueco (0 ó 1, según se juegue o no) y el
                        // multiplicador decreciente (n_timeslots - t) según la posición del timeslot
                        solver.post(IntConstraintFactory.times(g[e][p][c][t], nTimeslots - t, value));

                        timeslotScores.add(value);
                    }
                }

                // La puntuación de la categoría para el timeslot_t es la suma de las puntuaciones parciales de cada
                // hueco en t
                solver.post(IntConstraintFactory.sum(timeslotScores.toArray(new IntVar[timeslotScores.size()]),
                        categoryScores[t]
                ));
            }

            // La puntuación de esta categoría es la suma de las puntuaciones parciales de cada columna (timeslot)
            solver.post(IntConstraintFactory.sum(categoryScores, scores[e]));
        }

        // La puntuación total es la suma de las de cada categoría
        score = VariableFactory.bounded("score", objMin, objMax, solver);
        solver.post(IntConstraintFactory.sum(scores, score));
    }
}