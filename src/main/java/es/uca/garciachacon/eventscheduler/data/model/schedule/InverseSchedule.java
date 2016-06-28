package es.uca.garciachacon.eventscheduler.data.model.schedule;

import es.uca.garciachacon.eventscheduler.data.model.schedule.value.InverseScheduleValue;
import es.uca.garciachacon.eventscheduler.data.model.schedule.value.InverseScheduleValueOccupied;
import es.uca.garciachacon.eventscheduler.data.model.tournament.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Representa un horario agrupado por localizaciones de juego y horas de juego mediante una matriz bidimensional de
 * {@link InverseScheduleValue}.
 */
public class InverseSchedule extends Schedule {

    /**
     * Construye el horario agrupado de un evento, teniendo en cuenta los partidos del horario, los breaks y la
     * indisponibilidad de pistas
     *
     * @param event evento cuyo horario por localizaciones se quiere construir
     * @throws NullPointerException  si el evento es <code>null</code>
     * @throws IllegalStateException si los horarios del torneo al que el evento pertenece aún no se han calculados,
     *                               o son <code>null</code> por otra razón, como que ya se hayan calculado todos ellos
     */
    public InverseSchedule(Event event) {
        Objects.requireNonNull(event);

        Map<Event, EventSchedule> schedules = event.getTournament().getEventSchedules();

        if (schedules == null)
            throw new IllegalStateException("Tournament schedules not calculated");

        name = event.getName();
        players = event.getPlayers();
        localizations = event.getLocalizations();
        timeslots = event.getTimeslots();
        matches = schedules.get(event).getMatches();

        tournament = event.getTournament();

        schedule = new InverseScheduleValue[localizations.size()][timeslots.size()];

        List<Timeslot> breaks = event.getBreaks();
        Map<Localization, Set<Timeslot>> unavailableLocalizations = event.getUnavailableLocalizations();

        for (int i = 0; i < localizations.size(); i++)
            for (int j = 0; j < timeslots.size(); j++)
                schedule[i][j] = new InverseScheduleValue(InverseScheduleValue.FREE);

        for (Timeslot breakTimeslot : breaks)
            for (int i = 0; i < localizations.size(); i++)
                schedule[i][timeslots.indexOf(breakTimeslot)] =
                        new InverseScheduleValue(InverseScheduleValue.UNAVAILABLE);

        for (Localization localization : unavailableLocalizations.keySet()) {
            Set<Timeslot> localizationTimeslots = unavailableLocalizations.get(localization);
            int c = localizations.indexOf(localization);
            for (Timeslot timeslot : localizationTimeslots)
                schedule[c][timeslots.indexOf(timeslot)] = new InverseScheduleValue(InverseScheduleValue.UNAVAILABLE);
        }

        int matchDuration = event.getTimeslotsPerMatch();
        for (Match match : matches) {
            int c = localizations.indexOf(match.getLocalization());
            int t = timeslots.indexOf(match.getStartTimeslot());

            List<Player> matchPlayers = match.getPlayers();
            List<Integer> playersIndices = new ArrayList<>(players.size());
            playersIndices.addAll(matchPlayers.stream().map(players::indexOf).collect(Collectors.toList()));

            schedule[c][t] = new InverseScheduleValueOccupied(playersIndices);

            if (matchDuration > 1)
                for (int i = 1; i < matchDuration; i++)
                    schedule[c][i + t] = new InverseScheduleValue(InverseScheduleValue.CONTINUATION);
        }
    }

    /**
     * Construye el horario agrupado de un torneo
     *
     * @param tournament torneo cuyo horario agrupado se va a construir
     * @throws NullPointerException  si el torneo es <code>null</code>
     * @throws IllegalStateException si los horarios del torneo aún no han sido calculados, o ya se han calculado
     *                               todos, es decir, que el horario sea <code>null</code>
     */
    public InverseSchedule(Tournament tournament) {
        Objects.requireNonNull(tournament);

        TournamentSchedule schedule = tournament.getSchedule();
        if (schedule == null)
            throw new IllegalStateException("Tournament schedule not calculated");

        name = tournament.getName();
        players = tournament.getAllPlayers();
        localizations = tournament.getAllLocalizations();
        timeslots = tournament.getAllTimeslots();
        matches = schedule.getMatches();

        this.tournament = tournament;

        this.schedule = new InverseScheduleValue[localizations.size()][timeslots.size()];

        int nTimeslots = timeslots.size();
        int nLocalization = localizations.size();

        // Al principio se marca el horario entero como libre
        for (int i = 0; i < nLocalization; i++)
            for (int j = 0; j < nTimeslots; j++)
                this.schedule[i][j] = new InverseScheduleValue(InverseScheduleValue.FREE);

        List<Event> events = tournament.getEvents();

        Map<Event, List<Timeslot>> breaks = new HashMap<>();
        Map<Event, Map<Localization, Set<Timeslot>>> unavailableLocalizations = new HashMap<>();

        for (Event event : events) {
            if (event.hasBreaks()) {
                List<Timeslot> eventBreaks = event.getBreaks();

                // Al principio se marcan las pistas a las horas de break como limitadas
                for (Timeslot timeslot : eventBreaks) {
                    int t = timeslots.indexOf(timeslot);
                    for (int c = 0; c < nLocalization; c++)
                        this.schedule[c][t] = new InverseScheduleValue(InverseScheduleValue.LIMITED);
                }
                breaks.put(event, eventBreaks);
            }

            if (event.hasUnavailableLocalizations()) {
                Map<Localization, Set<Timeslot>> eventUnavailableLocalizations = event.getUnavailableLocalizations();

                // Al principio se marcan las pistas a las horas no disponibles como limitadas
                for (Localization localization : eventUnavailableLocalizations.keySet()) {
                    int c = localizations.indexOf(localization);
                    Set<Timeslot> unavailableLocalizationTimeslots = eventUnavailableLocalizations.get(localization);

                    // Si solamente hay un evento se marca directamente como no disponible porque el bucle que se
                    // encuentra más adelante solamente funciona para un torneo con más de una categoría
                    for (Timeslot timeslot : unavailableLocalizationTimeslots)
                        if (events.size() > 1)
                            this.schedule[c][timeslots.indexOf(timeslot)] =
                                    new InverseScheduleValue(InverseScheduleValue.LIMITED);
                        else
                            this.schedule[c][timeslots.indexOf(timeslot)] =
                                    new InverseScheduleValue(InverseScheduleValue.UNAVAILABLE);
                }
                unavailableLocalizations.put(event, eventUnavailableLocalizations);
            }
        }

        // Sobreescribe con no disponibles las marcadas anteriormente como limitadas, si lo son para todas las
        // categorías
        for (int t = 0; t < nTimeslots; t++) {
            Timeslot timeslot = timeslots.get(t);

            for (int c = 0; c < nLocalization; c++) {
                boolean all = true;
                Localization localization = localizations.get(c);

                // Si todas las categorías tienen un break a la hora_t, se marca como no disponible
                for (Event event : events) {
                    if (!event.getLocalizations().contains(localization) || !event.getTimeslots().contains(timeslot) ||
                            !breaks.containsKey(event) || !breaks.get(event).contains(timeslot)) {
                        all = false;
                        break;
                    }
                }
                if (all)
                    this.schedule[c][t] = new InverseScheduleValue(InverseScheduleValue.UNAVAILABLE);
            }
        }

        // Si todos los eventos tienen la pista no disponible a la misma hora se marca como no disponible,
        // sobreescribiendo las marcadas como limitadas anteriormente, si se da el caso
        for (int i = 0; i < events.size() - 1; i++) {
            Event thisEvent = events.get(i);

            if (unavailableLocalizations.containsKey(thisEvent)) {
                Map<Localization, Set<Timeslot>> eventUnavailableLocalizations =
                        unavailableLocalizations.get(thisEvent);

                for (Localization localization : eventUnavailableLocalizations.keySet()) {
                    int c = localizations.indexOf(localization);

                    for (Timeslot unavailableLocalizationTimeslot : eventUnavailableLocalizations.get(localization)) {
                        boolean all = true;
                        int t = timeslots.indexOf(unavailableLocalizationTimeslot);

                        for (int j = i + 1; j < events.size(); j++) {
                            Event otherEvent = events.get(j);

                            if (unavailableLocalizations.containsKey(otherEvent) &&
                                    otherEvent.getLocalizations().contains(localization) &&
                                    otherEvent.getTimeslots().contains(unavailableLocalizationTimeslot) &&
                                    !(unavailableLocalizations.get(otherEvent).containsKey(localization) &&
                                            unavailableLocalizations.get(otherEvent)
                                                    .get(localization)
                                                    .contains(unavailableLocalizationTimeslot)
                                    )) {

                                all = false;
                                break;
                            }
                        }

                        if (all)
                            this.schedule[c][t] = new InverseScheduleValue(InverseScheduleValue.UNAVAILABLE);
                    }
                }
            }
        }

        // Se añaden los partidos al horario
        for (Match match : matches) {
            int c = localizations.indexOf(match.getLocalization());
            int t = timeslots.indexOf(match.getStartTimeslot());

            List<Player> matchPlayers = match.getPlayers();
            List<Integer> playersIndices = new ArrayList<>(players.size());
            playersIndices.addAll(matchPlayers.stream().map(players::indexOf).collect(Collectors.toList()));

            this.schedule[c][t] = new InverseScheduleValueOccupied(playersIndices);

            int matchDuration = match.getDuration();
            if (matchDuration > 1)
                for (int i = 1; i < matchDuration; i++)
                    this.schedule[c][i + t] = new InverseScheduleValue(InverseScheduleValue.CONTINUATION);
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(name);

        sb.append(String.format("\n\n%8s", " "));

        int maxPlayersPerMatch = 0;
        for (Match match : matches) {
            int nPlayers = match.getPlayers().size();
            if (nPlayers > maxPlayersPerMatch)
                maxPlayersPerMatch = nPlayers;
        }

        int padding = maxPlayersPerMatch * 2 + 4;
        int nTimeslots = schedule[0].length;
        int nLocalizations = schedule.length;

        for (int t = 0; t < nTimeslots; t++)
            sb.append(String.format("%" + padding + "s", "t" + t));
        sb.append("\n");

        for (int c = 0; c < nLocalizations; c++) {
            sb.append(String.format("%8s", localizations.get(c)));
            for (int t = 0; t < nTimeslots; t++) {
                sb.append(String.format("%" + padding + "s", schedule[c][t]));
            }
            sb.append("\n");
        }

        return sb.toString();
    }
}
