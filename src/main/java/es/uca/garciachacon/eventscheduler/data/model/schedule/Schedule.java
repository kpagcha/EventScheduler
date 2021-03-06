package es.uca.garciachacon.eventscheduler.data.model.schedule;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import es.uca.garciachacon.eventscheduler.data.model.schedule.value.AbstractScheduleValue;
import es.uca.garciachacon.eventscheduler.data.model.schedule.value.ScheduleValue;
import es.uca.garciachacon.eventscheduler.data.model.tournament.Localization;
import es.uca.garciachacon.eventscheduler.data.model.tournament.Player;
import es.uca.garciachacon.eventscheduler.data.model.tournament.Timeslot;
import es.uca.garciachacon.eventscheduler.data.model.tournament.Tournament;
import es.uca.garciachacon.eventscheduler.rest.serializer.ScheduleSerializer;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Representa un horario mediante una matriz bidimensional de {@link ScheduleValue} que dota de significado a cada
 * elemento.
 * <p>
 * La primera dimensión de la matriz (filas) corresponde a los jugadores y la segunda dimensión (columnas)
 * corresponde con las horas de juego.
 * <p>
 * Además, mantiene una lista de partidos (ver {@link Match}) que se extraen del procesamiento de la matriz mencionada.
 */
@JsonSerialize(using = ScheduleSerializer.class)
public abstract class Schedule {

    /**
     * Representación del horario con la ayuda de la clase {@link ScheduleValue}
     */
    protected AbstractScheduleValue[][] schedule;

    /**
     * Lista de partidos que se dan en el horario
     */
    protected List<Match> matches;

    /**
     * Lista de jugadores
     */
    protected List<Player> players;

    /**
     * Lista de localizaciones de juego
     */
    protected List<Localization> localizations;

    /**
     * Lista de horas de juego
     */
    protected List<Timeslot> timeslots;

    /**
     * Nombre del evento o torneo al que corresponde el horario
     */
    protected String name;

    /**
     * Torneo al que pertenece el horario
     */
    protected Tournament tournament;

    /**
     * Número de timeslots
     */
    protected int occupation;

    /**
     * Número de timeslots disponibles donde asignar partidos
     */
    protected int availableTimeslots = -1;

    /**
     * Devuelve la matriz bidimensional con la representación del horario
     *
     * @return matriz bidimensional que representa el horario
     */
    public AbstractScheduleValue[][] getScheduleValues() {
        return schedule;
    }

    /**
     * @return los partidos que componen este horario
     */
    public List<Match> getMatches() {
        return matches;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public List<Localization> getLocalizations() {
        return localizations;
    }

    public List<Timeslot> getTimeslots() {
        return timeslots;
    }

    public String getName() {
        return name;
    }

    public Tournament getTournament() {
        return tournament;
    }

    /**
     * Devuelve el conjunto de partidos en los que participa el jugador
     *
     * @param player jugador del que se quieren obtener los partidos
     * @return lista de partidos
     */
    public List<Match> filterMatchesByPlayer(Player player) {
        return matches.stream().filter(m -> m.getPlayers().contains(player)).collect(Collectors.toList());
    }

    /**
     * Devuelve el conjunto de partidos en los que participan los jugadores
     *
     * @param players jugadores de los que se quieren obtener los partidos
     * @return lista de partidos
     */
    public List<Match> filterMatchesByPlayers(List<Player> players) {
        return matches.stream().filter(m -> m.getPlayers().containsAll(players)).collect(Collectors.toList());
    }

    /**
     * Devuelve el conjunto de partidos que tienen lugar en la localización de juego
     *
     * @param localization localización de juego de la que se quiere obtener la lista de partidos
     * @return lista de partidos
     */
    public List<Match> filterMatchesByLocalization(Localization localization) {
        return matches.stream().filter(m -> m.getLocalization().equals(localization)).collect(Collectors.toList());
    }

    /**
     * Devuelve el conjunto de partidos que empiezan en el timeslot indicado
     *
     * @param timeslot hora de comienzo para la que se quiere obtener la lista de partidos
     * @return lista de partidos
     */
    public List<Match> filterMatchesByStartTimeslot(Timeslot timeslot) {
        return matches.stream().filter(m -> m.getStartTimeslot().equals(timeslot)).collect(Collectors.toList());
    }

    /**
     * Devuelve el conjunto de partidos que terminan en el timeslot indicado
     *
     * @param timeslot hora de final para la que se quiere obtener la lista de  partidos
     * @return lista de partidos
     */
    public List<Match> filterMatchesByEndTimeslot(Timeslot timeslot) {
        return matches.stream().filter(m -> m.getEndTimeslot().equals(timeslot)).collect(Collectors.toList());
    }

    /**
     * Devuelve el conjunto de partidos que empiezan en el timeslot inicial y terminan en el final.
     * <p>
     * Si alguno de los <i>timeslots</i> que definine el rango, o ambos, no pertenecen al dominio de horas de juego
     * del horario, se devolverá <code>null</code>
     *
     * @param start timeslot de comienzo
     * @param end   timeslot final
     * @return lista de partidos
     */
    public List<Match> filterMatchesInTimeslotRange(Timeslot start, Timeslot end) {
        return matches.stream()
                .filter(m -> m.getStartTimeslot().equals(start) && m.getEndTimeslot().equals(end))
                .collect(Collectors.toList());
    }

    /**
     * Devuelve el conjunto de partidos cuyo transcurso discurre alguno de los <i>timeslots</i> indicados
     *
     * @param timeslot horas para las que se quieren buscar partidos que discurren sobre alguna de ellas. No
     *                 <code>null</code>
     * @return lista de partidos
     */
    public List<Match> filterMatchesDuringTimeslot(Timeslot timeslot) {
        return matches.stream()
                .filter(m -> timeslot.compareTo(m.getStartTimeslot()) <= 0 &&
                        timeslot.compareTo(m.getEndTimeslot()) >= 0)
                .collect(Collectors.toList());
    }

    /**
     * Devuelve el conjunto de partidos cuyo transcurso discurre sobre el timeslot indicado
     *
     * @param timeslots horas para las que se quiere buscar partidos que discurren
     *                  sobre ella. No <code>null</code>
     * @return lista de partidos
     */
    public List<Match> filterMatchesDuringTimeslots(List<Timeslot> timeslots) {
        Set<Match> timeslotsMatches = new HashSet<>();
        for (Timeslot timeslot : timeslots)
            timeslotsMatches.addAll(filterMatchesDuringTimeslot(timeslot));

        return new ArrayList<>(timeslotsMatches);
    }

    /**
     * Devuelve el conjunto de partidos cuyo transcurso discurre durante el rango de <i>timeslots</i> indicado, ambos
     * <i>timeslots</i> incluidos.
     * <p>
     * Si alguno de los <i>timeslots</i> que definine el rango, o ambos, no pertenecen al dominio de horas de juego
     * del horario, se devolverá <code>null</code>
     * <p>
     * Si <code>start</code> es mayor que <code>end</code>, se invierten los extremos de forma que estén en orden.
     *
     * @param start timeslot de comienzo
     * @param end   timeslot final
     * @return lista de partidos, o <code>null</code> si alguno de los <i>timeslots</i> no pertenece al dominio
     */
    public List<Match> filterMatchesDuringTimeslotRange(Timeslot start, Timeslot end) {
        if (!(timeslots.contains(start) && timeslots.contains(end)))
            return null;

        Timeslot startTimeslot, endTimeslot;
        if (start.compareTo(end) >= 0) {
            startTimeslot = start;
            endTimeslot = end;
        } else {
            startTimeslot = end;
            endTimeslot = end;
        }

        List<Timeslot> timeslotRange = new ArrayList<>();
        for (int i = timeslots.indexOf(startTimeslot); i <= timeslots.indexOf(endTimeslot); i++)
            timeslotRange.add(timeslots.get(i));

        return filterMatchesDuringTimeslots(timeslotRange);
    }

    /**
     * Calcula el número total de <i>timeslots</i> del horario
     *
     * @return el número total de horas de juego (<i>timeslots</i>), no negativo
     */
    public int getTotalTimeslots() {
        return localizations.size() * timeslots.size();
    }

    /**
     * Devuelve el número de <i>timeslots</i> disponibles, es decir, el número total de huecos disponibles en el
     * total de localizaciones de juego donde partidos pueden tener lugar.
     *
     * @return el número de <i>timeslots</i> disponibles, no negativo
     */
    public int getAvailableTimeslots() {
        return availableTimeslots;
    }

    /**
     * Devuelve el número de huecos del horario ocupados por partidos, para todas las pistas
     *
     * @return tasa de ocupación del horario
     */
    public int getOccupation() {
        return occupation;
    }

    /**
     * Devuelve la relación de ocupación de localizaciones de juego disponibles
     *
     * @return un decimal entre 0 y 1
     */
    public double getOccupationRatio() {
        return getOccupation() / (double) getAvailableTimeslots();
    }

    /**
     * Calcula el número de timeslots disponibles
     */
    protected abstract void calculateAvailableTimeslots();

    /**
     * Calcula la ocupación del horario
     */
    protected void calculateOccupation() {
        occupation = Math.toIntExact(Arrays.stream(schedule)
                .flatMap(Arrays::stream)
                .filter(val -> val.isOccupied() || val.isContinuation())
                .count());
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(name);

        int w = 9;

        sb.append(String.format("\n\n%" + w + "s", " "));

        for (int t = 0; t < timeslots.size(); t++)
            sb.append(String.format("%4s", "t" + t));
        sb.append("\n");

        for (int p = 0; p < players.size(); p++) {
            String playerStr = players.get(p).toString();
            if (playerStr.length() > w)
                playerStr = playerStr.substring(0, w);

            sb.append(String.format("%" + w + "s", playerStr));
            for (int t = 0; t < timeslots.size(); t++)
                sb.append(String.format("%4s", schedule[p][t]));
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * Devuelve una cadena con la representación en JSON del horario. La serialización es llevada a cabo por
     * {@link ScheduleSerializer}.
     *
     * @return cadenad JSON del horario
     */
    public String toJson() throws JsonProcessingException {
        return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(this);
    }
}
