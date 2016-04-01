package solver;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.measure.IMeasures;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import models.Tournament;

public class ResolutionData {
	/**
	 * Solver cuya informaci�n y estad�sticas contiene esta clase ResolutionData
	 */
	@JsonIgnore
	private Solver solver;
	
	/**
	 * Torneo al cual se le quiere calcular un horario
	 */
	@JsonIgnore
	private Tournament tournament;
	
	/**
	 * Nombre del solver
	 */
	private String solverName;
	
	/**
	 * Se ha completado el proceso de resoluci�n
	 */
	private boolean resolutionProcessCompleted;
	
	/**
	 * Nombres de las estrategias de b�squeda usadas para la resoluci�n
	 */
	private List<String> searchStrategies;
	
	/**
	 * N�mero de categor�as del evento que usan emparejamientos por sorteo
	 */
	private int randomDrawingsCount;
	
	/**
	 * N�mero de variables del modelo
	 */
	private int variables;
	
	/**
	 * N�mero de restricciones del modelo
	 */
	private int constraints;
	
	/**
	 * Se ha utilizado la estrategia de b�squeda por defecto
	 */
	private boolean isDeafultSearchUsed;
	
	/**
	 * Se ha completado la b�squeda 
	 */
	private boolean isSearchCompleted;
	
	/**
	 * N�mero de soluciones
	 */
	private long solutions;
	
	/**
	 * Tiempo de construcci�n del modelo en segundos
	 */
	private float buildingTime;
	
	/**
	 * Tiempo de resoluci�n en segundos
	 */
	private float resolutionTime;
	
	/**
	 * N�mero de nodos del �rbol de b�squeda
	 */
	private long nodes;
	
	/**
	 * Tasa de procesamiento de nodos (n�mero de nodos por segundo) 
	 */
	private double nodeProcessingRate;
	
	/**
	 * N�mero backtracks
	 */
	private long backtracks;
	
	/**
	 * N�mero de fails
	 */
	private long fails;
	
	/**
	 * N�mero de restarts
	 */
	private long restarts;
	
	public ResolutionData(Solver solver, Tournament tournament, String searchStrategyName, int randomDrawingsCount, boolean resolutionProcessCompleted) {
		this.solver = solver;
		this.tournament = tournament;
		this.randomDrawingsCount = randomDrawingsCount;
		this.resolutionProcessCompleted = resolutionProcessCompleted;
		
		searchStrategies = Arrays.asList(searchStrategyName.split(","));
		
		update();
	}
	
	public void update() {
		solverName = solver.getName();
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
	
	public void setResolutionProcessCompleted(boolean completed) {
		resolutionProcessCompleted = completed;
	}

	public String getSolverName() {
		return solverName;
	}
	
	public boolean getResolutionProcessCompleted() {
		return resolutionProcessCompleted;
	}
	
	public List<String> getSearchStrategies() {
		return searchStrategies;
	}
	
	public int getRandomDrawingsCount() {
		return randomDrawingsCount;
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
			"Solver [%s] features:\n\tVariables: %,d\n\tConstraints: %,d\n\tDefault search strategy: %s\n\tCompleted search strategy: %s\n" +
			"\tSearch strategy: %s\n\tRandom drawings %d/%d\n",
			solverName, variables, constraints, isDeafultSearchUsed ? "Yes" : "No", isSearchCompleted ? "Yes" : "No",
					StringUtils.join(searchStrategies, ", "), randomDrawingsCount, tournament.getNumberOfEvents()
		));
		
		if (resolutionProcessCompleted)
			sb.append(String.format(
				"Search features:\n\tSolutions: %d\n\tBuilding time: %,.3fs \n\tResolution time: %,.3fs\n\tNodes: %,d (%,.1f n/s)" + 
				"\n\tBacktracks: %,d\n\tFails: %,d\n\tRestarts: %,d\n",
				solutions, buildingTime, resolutionTime, nodes, nodeProcessingRate, backtracks, fails, restarts
			));
		
		return sb.toString();
	}
	
	public String toJson() {
		String jsonStr = "";
		
		ObjectMapper mapper = new ObjectMapper();
		try {
			jsonStr = mapper.writeValueAsString(this);
			
			return jsonStr;
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		
		return jsonStr;
	}
	
	public String toJsonPretty() {
		String jsonStr = "";
		
		ObjectMapper mapper = new ObjectMapper();
		try {
			jsonStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
			
			return jsonStr;
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		
		return jsonStr;
	}
}
