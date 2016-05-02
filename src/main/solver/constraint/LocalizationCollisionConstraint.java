package solver.constraint;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;

import data.model.tournament.Tournament;
import data.model.tournament.event.Event;
import data.model.tournament.event.entity.timeslot.Timeslot;

/**
 * Para todas las categor�as del torneo, controla que no se juegue en la misma pista a la misma hora
 * en partidos de categor�as distintas
 */
public class LocalizationCollisionConstraint extends TournamentConstraint {
	public LocalizationCollisionConstraint(Tournament trmnt) {
		super(trmnt);
		
		Map<Integer, List<Event>> eventsByNumberOfPlayersPerMatch = tournament.groupEventsByNumberOfPlayersPerMatch();
		
		// Posibles n�meros de jugadores que componen un partido del torneo (incluye 0)
		int[] allPossibleNumberOfPlayers = getAllPosibleNumberOfPlayersPerMatchArray(eventsByNumberOfPlayersPerMatch);
		
		int nAllCourts = tournament.getAllLocalizations().size();
		int nAllTimeslots = tournament.getAllTimeslots().size();
		int nCategories = tournament.getEvents().size();
		
		int[][] timeslotsIndices = tournamentSolver.getTimeslotsIndices();
		int[][] courtsIndices = tournamentSolver.getLocalizationsIndices();
		
		List<Event> events = tournament.getEvents();
		
		// Para cada pista del torneo explorar las participaciones de jugadores en cada categor�a
		// y controlar que no se juegue m�s de un partido en una pista a la misma hora
		for (int c = 0; c < nAllCourts; c++) {
			for (int t = 0; t < nAllTimeslots; t++) {
				// Las posibles ocupaciones de los jugadores de la pista_c a la hora_t
				List<IntVar> playerSum = new ArrayList<IntVar>();
				
				for (int e = 0; e < nCategories; e++) {
					Event event = events.get(e);
					List<Timeslot> timeslots = event.getTimeslots();
					int nPlayers = event.getPlayers().size();
					
					// Si en el evento_e se puede jugar en la pista_c y a la hora_t y la hora_t no es un break
					if (courtsIndices[c][e] != -1 && timeslotsIndices[t][e] != -1 && !event.isBreak(timeslots.get(t)))
						for (int p = 0; p < nPlayers; p++)
								playerSum.add(x[e][p][courtsIndices[c][e]][timeslotsIndices[t][e]]);
				}
				
				// Que la suma de las participaciones sea o 0 (no se juega en la pista_c a la hora_t)
				// o cualquier valor del conjunto de n�mero de jugadores por partido (cada evento tiene el suyo)
				solver.post(IntConstraintFactory.sum(
					(IntVar[]) playerSum.toArray(new IntVar[playerSum.size()]),
					VariableFactory.enumerated("PossibleNumberOfPlayersPerMatch", allPossibleNumberOfPlayers, solver))
				);
			}
		}
		
		// Caso excepcional: puede ocurrir que se cumpla la condici�n de que la suma de las participaciones de
		// jugadores en la pista_c a la hora_t sea una de las posibilidades, pero a�n as� sea una combinaci�n inv�lida
		// Por ejemplo: en un torneo con 2 categor�as individuales (partidos de 2 jugadores) y 1 categor�a de dobles
		// (partidos de 4 jugadores), puede ocurrir que la suma de las participaciones sea 4, con lo cual seg�n
		// la restricci�n definida es correcto, pero no porque haya un partido de dobles, sino porque hay
		// 2 partidos individuales, con lo cual sumar�an participaciones de jugadores 2+2=4. Adem�s, la restricci�n
		// de jugadores para cada categor�a (m�todo setConstraintsPlayersInCourtsForEachCategory) se cumplir�a
		// porque el n�meo de jugadores por partido para las 2 categor�as individuales ser�a 2, y para la categor�a
		// de dobles ser�a 0.
		// Soluci�n: forzar que la suma de las participaciones en las categor�as con el mismo n�mero de jugadores
		// por partido sea o 0 o el n�mero de jugadores por partido de esa categor�a
		
		// Por cada conjunto de categor�as con el mismo n�mero de jugadores por partido, la suma de participaciones
		// de todos los jugadores en una pista_c a una hora_t es 0 o el n�mero de jugadores por partido
		for (Integer numberOfPlayersPerMatch : eventsByNumberOfPlayersPerMatch.keySet()) {
			for (int c = 0; c < nAllCourts; c++) {
				for (int t = 0; t < nAllTimeslots; t++) {
					// Las posibles ocupaciones de los jugadores de la pista_c a la hora_t
					List<IntVar> playerSum = new ArrayList<IntVar>();
					
					List<Event> eventList = eventsByNumberOfPlayersPerMatch.get(numberOfPlayersPerMatch);
					for (Event event : eventList) {
						int nPlayers = event.getPlayers().size();
						int e = events.indexOf(event);
						
						// Si en el evento_e se puede jugar en la pista_c y a la hora_t
						if (courtsIndices[c][e] != -1 && timeslotsIndices[t][e] != -1)
							for (int p = 0; p < nPlayers; p++)
								playerSum.add(x[e][p][courtsIndices[c][e]][timeslotsIndices[t][e]]);
					}
					
					// Que la suma de las participaciones sea o 0 (no se juega en la pista_c a la hora_t)
					// o el n�mero de jugadores por partido (de este conjunto de categor�as con el mismo n�mero)
					solver.post(IntConstraintFactory.sum(
						(IntVar[]) playerSum.toArray(new IntVar[playerSum.size()]),
						VariableFactory.enumerated("PossibleNumberOfPlayersPerMatch", new int[]{ 0, numberOfPlayersPerMatch }, solver))
					);
				}
			}
		}
	}
	
	/**
	 * @param eventsByNumberOfPlayersPerMatch diccionario donde la clave es un n�mero de jugadores por partido y el valor asociado
	 * el valor asociado la lista de categor�as que definen ese n�mero de jugadores por partido
	 * @return posibles distintos n�meros de jugadores por partido, incluyendo ninguno (0)
	 */
	private int[] getAllPosibleNumberOfPlayersPerMatchArray(Map<Integer, List<Event>> eventsByNumberOfPlayersPerMatch) {		
		Integer[] keysArray = eventsByNumberOfPlayersPerMatch.keySet().toArray(new Integer[eventsByNumberOfPlayersPerMatch.keySet().size()]);
		
		int[] array = new int[keysArray.length + 1];
		array[0] = 0;
		for (int i = 1; i < array.length; i++)
			array[i] = keysArray[i - 1];
		
		return array;
	}
}
