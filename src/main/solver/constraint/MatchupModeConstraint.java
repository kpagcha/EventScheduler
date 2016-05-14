package solver.constraint;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;

import data.model.tournament.Tournament;
import data.model.tournament.event.Event;
import solver.TournamentSolver.MatchupMode;


/**
 * Para categorías con más de un partido por jugador, asegura que los enfrentamientos para cada jugador sean los esperados
 * según el modo de enfrentamiento que se haya definido sobre el evento. Si el modo es "todos diferentes", se asegurará
 * que un mismo partido no ocurre más de una vez. Si el modo es "todos iguales", se asegurará que el mismo enfrentamiento,
 * si ocurre, ocurra tantas veces como número de partidos por jugador defina el evento.
 */
public class MatchupModeConstraint extends EventConstraint {
	public MatchupModeConstraint(Event event, Tournament tournament) {
		super(event, tournament);
		
		int nPlayers = event.getPlayers().size();
		int nLocalizations = event.getLocalizations().size();
		int nTimeslots = event.getTimeslots().size();
		int nPlayersPerMatch = event.getPlayersPerMatch();
		int nMatchesPerPlayer = event.getMatchesPerPlayer();
		
		List<List<Integer>> combinations = getCombinations(
			IntStream.range(0, nPlayers).toArray(),
			nPlayersPerMatch
		);
		
		// Define cuántas veces un partido debe ocurrir dependiendo del modo de emparejamiento. En el modo "todos diferentes"
		// los enfrentamientos no pueden repetirse, luego el máximo de ocurrencias de un enfrentamiento es 1. Mientras que
		// en el modo "todos iguales", el número de ocurrencias de un mismo enfrentamiento es el número de partidos por jugador
		int nMatches = 0;
		MatchupMode matchupMode = event.getMatchupMode();
		if (matchupMode == MatchupMode.ALL_DIFFERENT)
			nMatches = 1;
		else if (matchupMode == MatchupMode.ALL_EQUAL)
			nMatches = nMatchesPerPlayer;
		else
			return;
		
		for (List<Integer> combination : combinations) {
			// Todos los posibles enfrentamientos en cada pista a cada hora
			IntVar[] possibleMatchups = VariableFactory.boundedArray("PossibleMatchups", nTimeslots * nLocalizations, 0, 1, solver);
			
			int i = 0;
			for (int c = 0; c < nLocalizations; c++) {
				for (int t = 0; t < nTimeslots; t++) {
					// Posible partido en la pista_c a la hora_t entre los jugadores
					IntVar[] possibleMatchup = new IntVar[nPlayersPerMatch];
					for (int p = 0; p < nPlayersPerMatch; p++)
						possibleMatchup[p] = g[eventIndex][combination.get(p)][c][t];
					
					// Cada enfrentamiento será el mínimo entre este enfrentamiento en pista_c a la hora_t. Si hay
					// enfrentamiento, todos los elementos serán 1 luego el mínimo será 1, indicando enfrentamiento,
					// mientras que si al menos uno es 0, el mínimo será 0 indicando que no hay enfrentamiento
					solver.post(IntConstraintFactory.minimum(possibleMatchups[i++], possibleMatchup));
				}
			}
			// Que el partido o una vez, o el número de veces que deba ocurrir según el modo de emparejamiento
			solver.post(IntConstraintFactory.sum(possibleMatchups, VariableFactory.enumerated("NMatchups", new int[]{ 0, nMatches }, solver)));
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
	private List<List<Integer>> getCombinations(int[] players, int combine){
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
}
