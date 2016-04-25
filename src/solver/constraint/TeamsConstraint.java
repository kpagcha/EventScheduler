package solver.constraint;

import java.util.List;
import java.util.Set;

import org.chocosolver.solver.constraints.IntConstraintFactory;

import data.model.tournament.Tournament;
import data.model.tournament.event.Event;
import data.model.tournament.event.entity.Player;
import data.model.tournament.event.entity.Team;

/**
 * Asegura que los jugadores que componen un equipo jueguen en el mismo partido
 */
public class TeamsConstraint extends EventConstraint {

	public TeamsConstraint(Event e, Tournament tournament) {
		super(e, tournament);
		
		List<Team> teams = event.getTeams();
		int nLocalizations = e.getLocalizations().size();
		int nTimeslots = e.getTimeslots().size();
		
		for (Team team : teams) {
			Set<Player> playersInTeam = team.getPlayers();
			int nPlayersInTeam = playersInTeam.size();
			
			int[] pIndex = new int[nPlayersInTeam];
			int i = 0;
			for (Player player : playersInTeam)
				pIndex[i++] = event.getPlayers().indexOf(player);
			
			for (int c = 0; c < nLocalizations; c++)
				for (int t = 0; t < nTimeslots; t++)
					for (int p = 0; p < nPlayersInTeam - 1; p++)
						solver.post(IntConstraintFactory.arithm(x[eventIndex][pIndex[p]][c][t], "=", x[eventIndex][pIndex[p + 1]][c][t]));
		}
	}
}