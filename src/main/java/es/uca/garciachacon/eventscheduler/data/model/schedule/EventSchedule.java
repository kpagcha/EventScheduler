package es.uca.garciachacon.eventscheduler.data.model.schedule;

import es.uca.garciachacon.eventscheduler.data.model.schedule.value.PlayerScheduleValue;
import es.uca.garciachacon.eventscheduler.data.model.schedule.value.PlayerScheduleValueOccupied;
import es.uca.garciachacon.eventscheduler.data.model.tournament.*;
import es.uca.garciachacon.eventscheduler.solver.TournamentSolver;

import java.util.*;

/**
 * Horario de un evento o categoría en particular.
 */
public class EventSchedule extends Schedule {
    /**
     * Evento al que pertenece el horario
     */
    private Event event;

    /**
     * Construye un horario final de un evento a partir de la información del mismo, y de la solución aportada por la
     * matriz tridimensional de enteros que contiene la información del horario calculado por la clase
     * {@link TournamentSolver}.
     * <p>
     * Este método procesa la matriz de enteros con la solución y construye el horario representado mediante una
     * matriz bidimensional de {@link PlayerScheduleValue}.
     *
     * @param event evento al que pertenece el horario que se va a construir
     * @param x     array de IntVar de tres dimensiones con los valores de la solución calculada por
     *              {@link TournamentSolver}
     */
    public EventSchedule(Event event, int[][][] x) {
        Objects.requireNonNull(event);
        Objects.requireNonNull(x);

        this.event = event;

        name = event.getName();

        players = event.getPlayers();
        localizations = event.getLocalizations();
        timeslots = event.getTimeslots();

        schedule = new PlayerScheduleValue[players.size()][timeslots.size()];
        for (int p = 0; p < players.size(); p++) {
            for (int t = 0; t < timeslots.size(); t++) {
                Timeslot timeslot = event.getTimeslots().get(t);

                if (event.isBreak(timeslot))
                    schedule[p][t] = new PlayerScheduleValue(PlayerScheduleValue.BREAK);
                else if (event.isPlayerUnavailable(event.getPlayers().get(p), timeslot)) {
                    schedule[p][t] = new PlayerScheduleValue(PlayerScheduleValue.UNAVAILABLE);
                } else {
                    schedule[p][t] = new PlayerScheduleValue(PlayerScheduleValue.FREE);

                    boolean matchInCourt = false;
                    for (int c = 0; c < localizations.size(); c++) {
                        if (x[p][c][t] == 1) {
                            schedule[p][t] = new PlayerScheduleValueOccupied(c);
                            matchInCourt = true;
                            break;
                        }
                    }

                    if (!matchInCourt) {
                        for (int c = 0; c < localizations.size(); c++) {
                            if (event.isLocalizationUnavailable(event.getLocalizations().get(c), timeslot)) {
                                schedule[p][t] = new PlayerScheduleValue(PlayerScheduleValue.LIMITED);
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
    private void calculateMatches() {
        int matchDuration = event.getTimeslotsPerMatch();

        // Horario donde sólo se marcan los comienzos de partidos
        PlayerScheduleValue[][] scheduleBeginnings = new PlayerScheduleValue[players.size()][timeslots.size()];
        for (int p = 0; p < players.size(); p++) {
            for (int t = 0; t < timeslots.size(); t++) {
                // si se juega un partido se marcan los siguientes de su rango como libres
                if (schedule[p][t].isOccupied()) {
                    scheduleBeginnings[p][t] = (PlayerScheduleValue) schedule[p][t];
                    for (int i = 1; i < matchDuration; i++)
                        scheduleBeginnings[p][t + i] = new PlayerScheduleValue(PlayerScheduleValue.FREE);
                    t += matchDuration - 1;
                } else {
                    scheduleBeginnings[p][t] = (PlayerScheduleValue) schedule[p][t];
                }
            }
        }
        matches = new ArrayList<>((players.size() / event.getPlayersPerMatch()) * event.getMatchesPerPlayer());


        List<Player> players = event.getPlayers();
        List<Localization> localizations = event.getLocalizations();
        List<Timeslot> timeslots = event.getTimeslots();
        int nPlayersPerMatch = event.getPlayersPerMatch();

        // Recorre cada columna (timeslot) del horario para averiguar qué partidos se juegan en ese momento
        for (int t = 0; t < timeslots.size(); t++) {

            // Lista de control con los jugadores para los que ya se ha encontrado partido en este timeslot
            List<Integer> playersAlreadyMatched = new ArrayList<>();

            // Recorre cada fila (jugadores) (menos las del final) para ver si cada jugador juega o no juega
            for (int thisPlayer = 0; thisPlayer < this.players.size() - nPlayersPerMatch + 1; thisPlayer++) {

                // El jugador actual juega en este momento y no ha sido añadido ya a un partido
                if (scheduleBeginnings[thisPlayer][t].isOccupied() && !playersAlreadyMatched.contains(thisPlayer)) {

                    // Lista que contendrá la composición de jugadores del partido encontrado
                    List<Player> playersInMatch = new ArrayList<>(Arrays.asList(players.get(thisPlayer)));

                    // Busca jugadores que pertenezcan al partido encontrado
                    for (int otherPlayer = thisPlayer + 1; otherPlayer < this.players.size(); otherPlayer++) {

                        // Si el "otro" jugador juega un partido, no se ha añadido ya y ambos valores son iguales (se
                        // juegan en la misma localización), el jugador pertenece al mismo partido y se añade
                        if (scheduleBeginnings[otherPlayer][t].isOccupied() &&
                                !playersAlreadyMatched.contains(otherPlayer) &&
                                scheduleBeginnings[otherPlayer][t].equals(scheduleBeginnings[thisPlayer][t])) {

                            playersAlreadyMatched.add(otherPlayer);

                            playersInMatch.add(players.get(otherPlayer));

                            if (playersInMatch.size() == nPlayersPerMatch)
                                break;
                        }
                    }

                    Match match = new Match(
                            playersInMatch,
                            localizations.get(((PlayerScheduleValueOccupied) scheduleBeginnings[thisPlayer][t])
                                    .getLocalization()),
                            timeslots.get(t),
                            timeslots.get(t + matchDuration - 1),
                            matchDuration
                    );

                    matches.add(match);

                    playersInMatch = new ArrayList<>(playersInMatch);

                    if (event.hasTeams()) {
                        List<Team> teamsInMatch = new ArrayList<>();

                        // Primero se añaden los equipos conocidos
                        for (int i = 0; i < playersInMatch.size(); i++) {
                            Team team = event.filterTeamByPlayer(playersInMatch.get(i));

                            if (team != null && !teamsInMatch.contains(team)) {
                                team.getPlayers().forEach(playersInMatch::remove);
                                teamsInMatch.add(team);
                            }
                        }

                        int nPlayersPerTeam = event.getPlayersPerTeam();

                        // Se crean aleatoriamente los equipos desconocidos
                        Collections.shuffle(playersInMatch);
                        while (!playersInMatch.isEmpty()) {
                            Set<Player> randomTeamPlayers = new HashSet<>(nPlayersPerTeam);

                            for (int i = 0; i < nPlayersPerTeam; i++) {
                                Player randomPlayer = playersInMatch.get(0);
                                randomTeamPlayers.add(randomPlayer);
                                playersInMatch.remove(randomPlayer);
                            }

                            teamsInMatch.add(new Team(randomTeamPlayers));
                        }

                        match.setTeams(teamsInMatch);
                    }
                }
            }
        }
    }

}
