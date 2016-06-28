package es.uca.garciachacon.eventscheduler.solver;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import es.uca.garciachacon.eventscheduler.data.model.tournament.Tournament;
import es.uca.garciachacon.eventscheduler.solver.TournamentSolver.ResolutionState;
import es.uca.garciachacon.eventscheduler.solver.TournamentSolver.SearchStrategy;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.measure.IMeasures;

import java.io.IOException;

/**
 * Contiene información acerca de un problema que se ha modelado para representar un torneo deportivo cuyos horarios
 * se quieren calcular y, si éstos han sido calculados, es decir, el problema ha sido resuelto y tiene solución,
 * también se incluye información acerca del proceso de resolución, como el tiempo de construcción del modelo o el
 * tiempo de resolución del problema.
 */
public class ResolutionData {

    /**
     * Torneo al cual se le quiere calcular un horario
     */
    @JsonIgnore
    private final Tournament tournament;

    /**
     * Solver interno cuya información y estadísticas contiene esta clase ResolutionData
     */
    @JsonIgnore
    private final Solver solver;

    /**
     * Se ha completado el proceso de resolución
     */
    private final boolean resolutionProcessCompleted;

    /**
     * Estrategia de búsqueda usada para la resolución
     */
    private final SearchStrategy searchStrategy;

    /**
     * Número de variables del modelo
     */
    private int variables;

    /**
     * Número de restricciones del modelo
     */
    private int constraints;

    /**
     * Estado de la resolución
     */
    private ResolutionState resolutionState;

    /**
     * Se ha utilizado la estrategia de búsqueda por defecto
     */
    private boolean isDeafultSearchUsed;

    /**
     * Se ha completado la búsqueda
     */
    private boolean isSearchCompleted;

    /**
     * Número de soluciones
     */
    private long solutions;

    /**
     * Tiempo de construcción del modelo en segundos
     */
    private float buildingTime;

    /**
     * Tiempo de resolución en segundos
     */
    private float resolutionTime;

    /**
     * Número de nodos del árbol de búsqueda
     */
    private long nodes;

    /**
     * Tasa de procesamiento de nodos (número de nodos por segundo)
     */
    private double nodeProcessingRate;

    /**
     * Número backtracks
     */
    private long backtracks;

    /**
     * Número de fails
     */
    private long fails;

    /**
     * Número de restarts
     */
    private long restarts;

    public ResolutionData(TournamentSolver tournamentSolver) {
        solver = tournamentSolver.getInternalSolver();
        tournament = tournamentSolver.getTournament();
        searchStrategy = tournamentSolver.getSearchStrategy();
        resolutionState = tournamentSolver.getResolutionState();
        resolutionProcessCompleted = resolutionState != ResolutionState.INCOMPLETE;

        variables = solver.getNbVars();
        constraints = solver.getNbCstrs();
        isDeafultSearchUsed = solver.getSearchLoop().isDefaultSearchUsed();
        isSearchCompleted = solver.getSearchLoop().isSearchCompleted();

        if (resolutionProcessCompleted) {
            IMeasures measures = solver.getMeasures();
            solutions = measures.getSolutionCount();
            buildingTime = measures.getReadingTimeCount();
            resolutionTime = measures.getTimeCount();
            nodes = measures.getNodeCount();
            nodeProcessingRate = nodes / resolutionTime;
            backtracks = measures.getBackTrackCount();
            fails = measures.getFailCount();
            restarts = measures.getRestartCount();
        }
    }

    public Solver getSolver() {
        return solver;
    }

    public Tournament getTournament() {
        return tournament;
    }

    public String getSolverName() {
        return solver.getName();
    }

    public boolean getResolutionProcessCompleted() {
        return resolutionProcessCompleted;
    }

    public SearchStrategy getSearchStrategy() {
        return searchStrategy;
    }

    public int getVariables() {
        return variables;
    }

    public int getConstraints() {
        return constraints;
    }

    public boolean isDeafultSearchUsed() {
        return isDeafultSearchUsed;
    }

    public boolean isSearchCompleted() {
        return isSearchCompleted;
    }

    public ResolutionState getResolutionState() { return resolutionState; }

    public long getSolutions() {
        return solutions;
    }

    public float getBuildingTime() {
        return buildingTime;
    }

    public float getResolutionTime() {
        return resolutionTime;
    }

    public long getNodes() {
        return nodes;
    }

    public double getNodeProcessingRate() {
        return nodeProcessingRate;
    }

    public long getBacktracks() {
        return backtracks;
    }

    public long getFails() {
        return fails;
    }

    public long getRestarts() {
        return restarts;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format(
                "Solver [%s] features:\n\tTournament: %s\n\tVariables: %,d\n\tConstraints:%,d\n\t" +
                        "Default search strategy: %s\n\tCompleted search strategy: %s" +
                        "\n\tSearch strategy: %s\n\tResolution state: %s\n",
                getSolverName(),
                tournament.getName(),
                variables,
                constraints,
                isDeafultSearchUsed ? "Yes" : "No",
                isSearchCompleted ? "Yes" : "No",
                searchStrategy,
                resolutionState
        ));

        if (resolutionProcessCompleted)
            sb.append(String.format(
                    "Search features:\n\tSolutions: %d\n\tBuilding time: %,.3fs \n\tResolution time: %,.3fs\n\tNodes:" +
                            " %,d (%,.1f n/s)\n\tBacktracks: %,d\n\tFails: %,d\n\tRestarts: %,d\n",
                    solutions,
                    buildingTime,
                    resolutionTime,
                    nodes,
                    nodeProcessingRate,
                    backtracks,
                    fails,
                    restarts
            ));

        return sb.toString();
    }

    public String toJson() throws JsonProcessingException {
        return toJson(false);
    }

    public String toJsonPretty() throws JsonProcessingException {
        return toJson(true);
    }

    private String toJson(boolean pretty) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(ResolutionData.class, new ResolutionDataSerializer());
        mapper.registerModule(module);

        return pretty ? mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this) :
                mapper.writeValueAsString(this);
    }

    class ResolutionDataSerializer extends JsonSerializer<ResolutionData> {
        public void serialize(ResolutionData resolutionData, JsonGenerator jgen, SerializerProvider provider)
                throws IOException {
            jgen.writeStartObject();

            jgen.writeStringField("tournament", tournament.getName());
            jgen.writeStringField("solver", solver.getName());
            jgen.writeNumberField("variables", variables);
            jgen.writeNumberField("constraints", constraints);
            jgen.writeBooleanField("deafultSearchUsed", isDeafultSearchUsed);
            jgen.writeBooleanField("searchCompleted", isSearchCompleted);
            jgen.writeStringField("searchStrategy", searchStrategy.toString());
            jgen.writeNumberField("solutions", solutions);
            jgen.writeStringField("resolutionState", resolutionState.toString());

            jgen.writeBooleanField("resolutionProcessCompleted", resolutionProcessCompleted);

            if (resolutionProcessCompleted) {
                jgen.writeNumberField("buildingTime", buildingTime);
                jgen.writeNumberField("resolutionTime", resolutionTime);
                jgen.writeNumberField("nodes", nodes);
                jgen.writeNumberField("nodeProcessingRate", nodeProcessingRate);
                jgen.writeNumberField("backtracks", backtracks);
                jgen.writeNumberField("fails", fails);
                jgen.writeNumberField("restarts", restarts);
            }

            jgen.writeEndObject();
        }
    }
}
