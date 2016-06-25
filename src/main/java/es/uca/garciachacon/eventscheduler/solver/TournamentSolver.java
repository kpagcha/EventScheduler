package es.uca.garciachacon.eventscheduler.solver;

import es.uca.garciachacon.eventscheduler.data.model.schedule.EventSchedule;
import es.uca.garciachacon.eventscheduler.data.model.tournament.*;
import es.uca.garciachacon.eventscheduler.solver.constraint.*;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
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
    private Optional<Map<Event, EventSchedule>> schedules;

    /**
     * Indica si ha comenzado el proceso de resolución
     */
    private boolean resolutionProcessStarted = false;

    /**
     * Indica si se ha encontrado la última solución
     */
    private boolean resolutionProcessFinished = false;

    /**
     * Contador de soluciones encontradas
     */
    private long foundSolutions = 0;

    /**
     * Estrategia de búsqueda empleada en la resolución del problema
     */
    private SearchStrategy searchStrategy = SearchStrategy.DOMOVERWDEG;

    /**
     * Para las estrategias de búsqueda minDom_UB y minDom_LB indicar si priorizar timeslots (true) o pistas (false)
     * a la hora de hacer las asignaciones
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
    public boolean getFillTimeslotsFirst() {
        return fillTimeslotsFirst;
    }

    /**
     * Fija si al calcular los horarios se priorizará la ocupación de <i>timeslots</i> o de jugadores. Sólamente
     * tiene efecto si la estrategia de búsqueda configurada es {@link SearchStrategy#MINDOM_UB} o
     * {@link SearchStrategy#MINDOM_LB}.
     *
     * @param fillFirst <code>true</code> para priorizar <i>timeslots</i>, y <code>false</code> para priorizar jugadores
     */
    public void setFillTimeslotsFirst(boolean fillFirst) {
        fillTimeslotsFirst = fillFirst;
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
     * Comprueba si el proceso de resolución ha comenzado, es decir, si ya se ha invocado a
     * {@link TournamentSolver#execute()}.
     *
     * @return <code>true</code> si ya ha comenzado el proceso de resolución, <code>false</code> si no
     */
    public boolean hasResolutionProcessStarted() {
        return resolutionProcessStarted;
    }

    /**
     * Comprueba si el proceso de resolución ya ha terminado, es decir, se ha encontrado la última solución posible,
     * o si no se encontró ninguna solución al lanzar el proceso de resolución.
     *
     * @return <code>true</code> si ya ha terminado el proceso de resolución, <code>false</code> si no
     */
    public boolean hasResolutionProcessFinished() {
        return resolutionProcessFinished;
    }

    /**
     * Comprueba si el torneo tiene solución, es decir, si se ha encontrado al menos una. Si el proceso de resolución
     * aún no ha comenzado, se devolverá <code>false</code>.
     *
     * @return <code>true</code> si el torneo tiene solución; <code>false</code> si no tiene, o si no ha comenzado el
     * proceso de resolución
     */
    public boolean hasSolutions() {
        return resolutionProcessStarted && foundSolutions > 0;
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
     * Construye y modela el problema, configura las estrategias de búsqueda e inicia el proceso de resolución
     *
     * @return true si se ha encontrado una solución, false si no
     */
    public boolean execute() {
        solver = new Solver("Tournament Solver");

        schedules = Optional.empty();

        resolutionProcessStarted = true;
        resolutionProcessFinished = false;
        foundSolutions = 0;

        buildModel();

        configureSearch();

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
        postAllConstraints();
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
     * Publica todas las restricciones del el modelo
     */
    private void postAllConstraints() {
        solver.post(constraints.toArray(new Constraint[constraints.size()]));
    }

    /**
     * Configura la estrategia de búsqueda
     */
    private void configureSearch() {
        List<Event> events = tournament.getEvents();
        int nCategories = events.size();

        IntVar[][] vars = new IntVar[nCategories][];

        if ((searchStrategy == SearchStrategy.MINDOM_LB || searchStrategy == SearchStrategy.MINDOM_UB) &&
                !fillTimeslotsFirst) {
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
     * Devuelve el nombre de la estrategora de búsqueda empleada. Para domOverWDeg, el valor indicado entre
     * paréntesis se trata de la semilla utilizada, siendo t obtenida mediante {@link System#currentTimeMillis()}.
     *
     * @return el nombre de la estrategia o estrategias de búsqueda empleadas
     */
    private String getSearchStrategyName() {
        String searchStrategyStr;
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
        resolutionData = new ResolutionData(solver, tournament, getSearchStrategyName(), solutionFound);

        if (!solutionFound) {
            if (solver.isFeasible() == ESat.FALSE)
                LOGGER.log(Level.INFO, "Problem infeasible");
            else if (solver.isFeasible() == ESat.UNDEFINED)
                LOGGER.log(Level.INFO, "Solution could not been found within given limits");
        } else
            foundSolutions++;

        return solutionFound;
    }

    /**
     * Para el proceso de resolución, dejándolo incompleto y no habiéndose encontrado la solución.
     */
    public void stopResolutionProcess() {
        stop = true;
    }

    /**
     * Si no se ha intentado resolver el modelo, es decir, no se ha llamado a {@link TournamentSolver#execute()}, no
     * habrá una solución disponible y se devolverá {@link Optional#empty()}.
     * <p>
     * Si es la primera vez que se invoca a este método despues de iniciar el proceso de resolución, se contruye el
     * horario con esa primera solución y se devuelve.
     * <p>
     * Las subsiguientes invocaciones a este método calcularán la siguiente solución al problema, se construirá otro
     * horario alternativo diferente al anterior, sobreescribiendo su valor, y se devuelve este nuevo horario. Si no
     * hay más soluciones disponibles, se devuelve el valor del horario a su estado inicial, es decir, se devuelve un
     * objecto opcional vacío {@link Optional#empty()}, y se marca el proceso de resolución como finalizado.
     *
     * @return los horarios de cada categoría del torneo envueltos en una clase {@link Optional}
     */
    public Optional<Map<Event, EventSchedule>> getSolution() {
        if (!resolutionProcessFinished) {
            if (!schedules.isPresent() && foundSolutions == 1)
                buildSchedules();
            else if (solver.nextSolution()) {
                buildSchedules();
                foundSolutions++;
            } else {
                LOGGER.log(Level.INFO, "All solutions found");
                schedules = Optional.empty();
                resolutionProcessFinished = true;
            }
        }
        return schedules;
    }

    /**
     * Inicializa los horarios de cada categoría a partir de la solución calculada por el solver
     */
    private void buildSchedules() {
        List<Event> events = tournament.getEvents();
        Map<Event, EventSchedule> currentSchedules = new HashMap<>(events.size());

        for (int e = 0; e < events.size(); e++) {
            Event event = events.get(e);
            currentSchedules.put(event, new EventSchedule(event, internalMatrixToInt(event, x[e])));
        }
        schedules = Optional.of(currentSchedules);
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
}