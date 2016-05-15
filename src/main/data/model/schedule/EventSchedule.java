package data.model.schedule;

import java.util.ArrayList;
import java.util.List;

import data.model.schedule.value.ScheduleValue;
import data.model.tournament.event.Event;
import data.model.tournament.event.domain.Player;
import data.model.tournament.event.domain.Team;
import data.model.tournament.event.domain.timeslot.Timeslot;
import solver.TournamentSolver;

/**
 * Horario de un evento o categoría en particular.
 *
 */
public class EventSchedule extends Schedule {
	/**
	 * Evento al que pertenece el horario
	 */
	private Event event;

	/**
	 * Construye un horario final de un evento a partir de la información del
	 * mismo, y de la solución aportada por la matriz tridimensional de enteros
	 * que contiene la información del horario calculado por la clase
	 * {@link TournamentSolver}.
	 * <p>
	 * Este método procesa la matriz de enteros con la solución y construye el
	 * horario representado mediante una matriz bidimensional de
	 * {@link ScheduleValue}.
	 * 
	 * @param event
	 *            evento al que pertenece el horario que se va a construir
	 * @param x
	 *            array de IntVar de tres dimensiones con los valores de la
	 *            solución calculada por el EventSolver
	 */
	public EventSchedule(Event event, int[][][] x) {
		if (event == null || x == null)
			throw new IllegalArgumentException("Parameters cannot be null");

		this.event = event;

		name = event.getName();

		players = event.getPlayers();
		localizations = event.getLocalizations();
		timeslots = event.getTimeslots();

		nPlayers = players.size();
		nLocalizations = localizations.size();
		nTimeslots = timeslots.size();

		schedule = new ScheduleValue[nPlayers][nTimeslots];
		for (int p = 0; p < nPlayers; p++) {
			for (int t = 0; t < nTimeslots; t++) {
				Timeslot timeslot = event.getTimeslots().get(t);

				if (event.isBreak(timeslot))
					schedule[p][t] = new ScheduleValue(ScheduleValue.BREAK);
				else if (event.isPlayerUnavailable(event.getPlayers().get(p), timeslot)) {
					schedule[p][t] = new ScheduleValue(ScheduleValue.UNAVAILABLE);
				} else {
					schedule[p][t] = new ScheduleValue(ScheduleValue.FREE);

					boolean matchInCourt = false;
					for (int c = 0; c < nLocalizations; c++) {
						if (x[p][c][t] == 1) {
							schedule[p][t] = new ScheduleValue(ScheduleValue.OCCUPIED, c);
							matchInCourt = true;
							break;
						}
					}

					if (!matchInCourt) {
						for (int c = 0; c < nLocalizations; c++) {
							if (event.isLocalizationUnavailable(event.getLocalizations().get(c), timeslot)) {
								schedule[p][t] = new ScheduleValue(ScheduleValue.LIMITED);
								break;
							}
						}
					}
				}
			}
		}

		calculateMatches();
	}

	public Event getEvent() {
		return event;
	}

	/**
	 * Calcula los partidos que componen el horario basándose en los valores
	 * actuales del horario
	 */
	protected void calculateMatches() {
		int matchDuration = event.getTimeslotsPerMatch();

		// Horario donde solo se marcan los comienzos de partidos
		ScheduleValue[][] scheduleBeginnings = new ScheduleValue[nPlayers][nTimeslots];
		for (int p = 0; p < nPlayers; p++) {
			for (int t = 0; t < nTimeslots; t++) {
				// si se juega un partido se marcan los siguientes de su rango
				// como libres
				if (schedule[p][t].isOccupied()) {
					scheduleBeginnings[p][t] = schedule[p][t];
					for (int i = 1; i < matchDuration; i++)
						scheduleBeginnings[p][t + i] = new ScheduleValue(ScheduleValue.FREE);
					t += matchDuration - 1;
				} else {
					scheduleBeginnings[p][t] = schedule[p][t];
				}
			}
		}
		matches = new ArrayList<Match>((nPlayers / event.getPlayersPerMatch()) * event.getMatchesPerPlayer());

		int nPlayersPerMatch = event.getPlayersPerMatch();
		for (int t = 0; t < nTimeslots; t++) {
			List<Integer> playersAlreadyMatched = new ArrayList<>();

			for (int thisPlayer = 0; thisPlayer < nPlayers - nPlayersPerMatch + 1; thisPlayer++) {
				if (scheduleBeginnings[thisPlayer][t].isOccupied()) {
					List<Integer> playersBelongingToMatch = new ArrayList<>();
					playersBelongingToMatch.add(thisPlayer);

					boolean matchCompleted = false;

					for (int otherPlayer = thisPlayer + 1; otherPlayer < nPlayers; otherPlayer++) {
						if (scheduleBeginnings[otherPlayer][t].isOccupied()
								&& !playersAlreadyMatched.contains(otherPlayer)
								&& scheduleBeginnings[thisPlayer][t].equals(scheduleBeginnings[otherPlayer][t])) {

							playersAlreadyMatched.add(otherPlayer);

							playersBelongingToMatch.add(otherPlayer);

							if (playersBelongingToMatch.size() == nPlayersPerMatch) {
								matchCompleted = true;
								break;
							}
						}
					}

					if (matchCompleted || nPlayersPerMatch == 1) {
						List<Player> playersList = new ArrayList<Player>(nPlayersPerMatch);
						for (int playerIndex : playersBelongingToMatch)
							playersList.add(event.getPlayers().get(playerIndex));

						Match match = new Match(
							playersList,
							event.getLocalizations().get(scheduleBeginnings[thisPlayer][t].getLocalization()),
							event.getTimeslots().get(t), event.getTimeslots().get(t + matchDuration - 1),
							matchDuration
						);

						matches.add(match);

						if (event.hasTeams()) {
							List<Team> teamsInMatch = new ArrayList<Team>();
							for (Player player : playersList) {
								Team team = event.getTeamByPlayer(player);
								if (!teamsInMatch.contains(team))
									teamsInMatch.add(team);
							}
							
							match.setTeams(teamsInMatch);
						}
					}
				}
			}
		}
	}

}
