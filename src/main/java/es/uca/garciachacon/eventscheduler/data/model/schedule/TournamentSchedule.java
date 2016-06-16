package es.uca.garciachacon.eventscheduler.data.model.schedule;

import es.uca.garciachacon.eventscheduler.data.model.schedule.value.AbstractScheduleValue;
import es.uca.garciachacon.eventscheduler.data.model.schedule.value.PlayerScheduleValue;
import es.uca.garciachacon.eventscheduler.data.model.schedule.value.PlayerScheduleValueOccupied;
import es.uca.garciachacon.eventscheduler.data.model.tournament.Tournament;
import es.uca.garciachacon.eventscheduler.data.model.tournament.event.Event;
import es.uca.garciachacon.eventscheduler.data.model.tournament.event.domain.Localization;
import es.uca.garciachacon.eventscheduler.data.model.tournament.event.domain.Player;
import es.uca.garciachacon.eventscheduler.data.model.tournament.event.domain.Timeslot;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Horario de un torneo formado por la combinación de los horarios de cada categoría que lo compone
 */
public class TournamentSchedule extends Schedule {
    /**
     * Torneo al que el horario combinado pertenece
     */
    private Tournament tournament;

    /**
     * Construye un horario combinado a partir de los horarios de cada categoría del torneo
     *
     * @param tournament torneo al que pertenece el horario que se va a construir
     */
    public TournamentSchedule(Tournament tournament) {
        Objects.requireNonNull(tournament);

        this.tournament = tournament;

        Map<Event, EventSchedule> schedules = tournament.getCurrentSchedules();

        if (schedules == null)
            throw new IllegalStateException("Tournament schedules not calculated");

        name = tournament.getName();

        players = tournament.getAllPlayers();
        localizations = tournament.getAllLocalizations();
        timeslots = tournament.getAllTimeslots();

        schedule = new PlayerScheduleValue[players.size()][timeslots.size()];

        for (int p = 0; p < players.size(); p++)
            for (int t = 0; t < timeslots.size(); t++)
                schedule[p][t] = new PlayerScheduleValue(PlayerScheduleValue.NOT_IN_DOMAIN);

        for (Event event : tournament.getEvents()) {
            AbstractScheduleValue[][] eventSchedule = schedules.get(event).getScheduleValues();

            int nPlayers = event.getPlayers().size();
            int nTimeslots = event.getTimeslots().size();

            List<Localization> eventLocalizations = event.getLocalizations();

            for (int p = 0; p < nPlayers; p++) {
                for (int t = 0; t < nTimeslots; t++) {
                    Player player = event.getPlayers().get(p);
                    Timeslot timeslot = event.getTimeslots().get(t);

                    int playerIndex = players.indexOf(player);
                    int timeslotIndex = timeslots.indexOf(timeslot);

                    // Si no hay ya una pista marcada sobre la hora_t para el jugador_p (esto evita sobreescribir
                    // valores  de pistas ya escritos sobre el horario)
                    if (!schedule[playerIndex][timeslotIndex].isOccupied()) {
                        if (eventSchedule[p][t].isOccupied()) {
                            schedule[playerIndex][timeslotIndex] =
                                    new PlayerScheduleValueOccupied(localizations.indexOf(eventLocalizations.get((
                                            (PlayerScheduleValueOccupied) eventSchedule[p][t])
                                            .getLocalization())));
                        } else if (!schedule[playerIndex][timeslotIndex].isLimited())
                            schedule[playerIndex][timeslotIndex] = eventSchedule[p][t];
                    }
                }
            }
        }

        matches = tournament.getCurrentSchedules()
                .values()
                .stream()
                .flatMap(l -> l.getMatches().stream())
                .sorted((m1, m2) -> -m1.getStartTimeslot().compareTo(m2.getStartTimeslot()))
                .collect(Collectors.toList());
    }

    public Tournament getTournament() {
        return tournament;
    }
}
