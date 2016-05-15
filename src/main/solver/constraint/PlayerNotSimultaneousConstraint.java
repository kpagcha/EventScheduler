package solver.constraint;

import java.util.ArrayList;
import java.util.List;

import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;

import data.model.tournament.Tournament;
import data.model.tournament.event.Event;
import data.model.tournament.event.domain.Player;
import data.model.tournament.event.domain.timeslot.Timeslot;

/**
 * Si un jugador_p juega en más de una categoría, evitar que le coincidan partidos a la misma hora
 */
public class PlayerNotSimultaneousConstraint extends TournamentConstraint {
	public PlayerNotSimultaneousConstraint(Tournament trnmnt) {
		super(trnmnt);
		
		int nAllPlayers = tournament.getAllPlayers().size();
		int nAllTimeslots = tournament.getAllTimeslots().size();
		int nCategories = tournament.getEvents().size();
		
		int[][] playersIndices = getPlayersIndices();
		int[][] timeslotsIndices = getTimeslotsIndices();
		
		// Para cada jugador del torneo explorar las participaciones en cada categoría y
		// controlar colisiones que puedan producirse (mismo jugador, mismo timeslot)
		for (int p = 0; p < nAllPlayers; p++) {
			for (int t = 0; t < nAllTimeslots; t++) {
				// Las posibles ocupaciones de pistas del jugador_p a la hora_t
				List<IntVar> courtSum = new ArrayList<IntVar>();
				
				for (int e = 0; e < nCategories; e++) {
					int nLocalizations = tournament.getEvents().get(e).getLocalizations().size();
					
					// Si el jugador_p juega en la categoría_e a la hora_t
					if (playersIndices[p][e] != -1 && timeslotsIndices[t][e] != -1)
						for (int c = 0; c < nLocalizations; c++) 
							courtSum.add(x[e][playersIndices[p][e]][c][timeslotsIndices[t][e]]);
				}
				
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
	 * Devuelve los índices de cada jugador en el array de jugadores correspondiente a cada categoría
	 * p.e. playersIndices[2][1] = 1 significa que el índice del jugador del torneo número 3, en
	 * la categoría número 2 es 1 (si el jugador no existe en la categoría, el índice es -1)
	 */
	private int[][] getPlayersIndices() {
		List<Player> allPlayers = tournament.getAllPlayers();
		List<Event> events = tournament.getEvents();
		int nCategories = events.size();
		
		int[][] playersIndices = new int[allPlayers.size()][nCategories];
		for (int i = 0; i < allPlayers.size(); i++) {
			for (int e = 0; e < nCategories; e++) {
				List<Player> eventPlayers = events.get(e).getPlayers();
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
		return playersIndices;
	}
	
	/**
	 * Devuelve los índices de cada timeslot en el array de horas de juego (timeslots) correspondiente a cada categoría 
	 */
	private int[][] getTimeslotsIndices() {
		List<Timeslot> allTimeslots = tournament.getAllTimeslots();
		List<Event> events = tournament.getEvents();
		int nCategories = events.size();
		
		int [][] timeslotsIndices = new int[allTimeslots.size()][nCategories];
		for (int i = 0; i < allTimeslots.size(); i++) {
			for (int e = 0; e < nCategories; e++) {
				List<Timeslot> eventTimeslots = events.get(e).getTimeslots();
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
		return timeslotsIndices;
	}
}