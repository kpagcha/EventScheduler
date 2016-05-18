package data.model.tournament;

import data.model.schedule.EventSchedule;
import data.model.schedule.Match;
import data.model.schedule.TournamentSchedule;
import data.model.tournament.event.Event;
import data.model.tournament.event.domain.Localization;
import data.model.tournament.event.domain.Player;
import data.model.tournament.event.domain.timeslot.Timeslot;
import data.validation.validable.Validable;
import data.validation.validable.ValidationException;
import data.validation.validator.Validator;
import data.validation.validator.tournament.TournamentValidator;
import solver.TournamentSolver;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Representa un torneo deportivo. Un torneo se compone de, al menos, un evento o categoría, ver {@link Event}.
 * <p>
 * <p>El dominio un torneo en cuanto a jugadores, localizaciones de juego y horas de juego, es la suma de estos
 * elementos no repetidos en cada una de las categorías del torneo.</p>
 * <p>
 * <p>A un torneo le corresponde una variable de la clase {@link TournamentSolver} que se encarga del proceso de
 * resolución del problema que se modela gracias a la información que este torneo contiene y, principalmente, sus
 * eventos.
 * <p>
 * <p>Un torneo lleva asociado un conjunto de horarios para cada evento (ver {@link EventSchedule}) y un horario
 * combinado de todos estos que representa el horario del torneo en su totalidad (ver {@link TournamentSchedule}).
 * Inicialmente, el valor de estos horarios es asignado, es decir, son <code>null</code>. Los horarios contendrán
 * valores específicos una vez se ejecute el método {@link #solve()} que inicia por primera vez el proceso de
 * resolución, del que es responsable {@link TournamentSolver}.</p>
 * <p>
 * <p>Si el proceso de resolución ha sido satisfactorio, los horarios de los eventos y, por ende, el horario del
 * torneo combinado, se actualizarán a los valores de la primera solución encontrada. Si no se encuentra ninguna
 * solución, los horarios permanecerán con un valor por defecto <code>null</code> sin asignar.</p>
 * <p>
 * <p>Los horarios se podrán actualizar al valor de la siguiente solución mediante el método
 * {@link #nextSchedules()}. Es importante notar que este método sobrescribirá el valor actual de los horarios, de
 * forma que si se pretenden guardar los valores previos, se deberá implementar una funcionalidad específica para
 * este propósito. Si la clase que ejecuta la resolución del problema ya no encuentra más soluciones y se vuelve a
 * invocar a {@link #nextSchedules()}, el valor de los horarios se volverán a reiniciar con el valor de
 * <code>null</code>, indicando que no hay más horarios disponibles para este torneo.</p>
 */
public class Tournament implements Validable {
    /**
     * Nombre del torneo
     */
    private String name;

    /**
     * Categorías que componen el torneo
     */
    private final List<Event> events;

    /**
     * Todos los jugadores que participan en el torneo. No se repiten los presentes en múltiples categorías
     */
    private final List<Player> allPlayers;

    /**
     * Todos los terrenos de juego en los que se desarrolla en el torneo. No se repiten los presentes en múltiples
     * categorías
     */
    private final List<Localization> allLocalizations;

    /**
     * Todos los timeslots en los que discurre el torneo. No se repiten los presentes en múltiples categorías
     */
    private final List<Timeslot> allTimeslots;

    /**
     * Horarios para cada categoría
     */
    private Map<Event, EventSchedule> currentSchedules;

    /**
     * Horario del torneo que combina los horarios de todas las categorías en uno solo
     */
    private TournamentSchedule schedule;

    /**
     * El solver que obtendrá los horarios de cada categoría el torneo
     */
    private final TournamentSolver solver;

    /**
     * Validador del torneo
     */
    private Validator<Tournament> validator = new TournamentValidator();

    /**
     * Construye del torneo con un nombre y el conjunto de categorías que lo componen.
     *
     * @param name       nombre del torneo, cadena no <code>null</code>
     * @param categories eventos o categorías no repetidas que componen el torneo, debe haber al menos una
     * @throws IllegalArgumentException si alguno de los parámetros es <code>null</code> o si la lista de categorías
     *                                  está vacía
     */
    public Tournament(String name, List<Event> categories) {
        if (name == null || categories == null)
            throw new IllegalArgumentException("The parameters cannot be null");

        if (categories.isEmpty())
            throw new IllegalArgumentException("The list of categories cannot be empty");

        if (categories.contains(null))
            throw new IllegalArgumentException("A category cannot be null");

        this.name = name;
        events = new ArrayList<>(categories);

        allPlayers = new ArrayList<>();
        allTimeslots = new ArrayList<>();
        allLocalizations = new ArrayList<>();
        for (Event event : events) {
            event.getPlayers().stream().filter(p -> !allPlayers.contains(p)).forEach(allPlayers::add);
            event.getTimeslots().stream().filter(t -> !allTimeslots.contains(t)).forEach(allTimeslots::add);
            event.getLocalizations().stream().filter(l -> !allLocalizations.contains(l)).forEach(allLocalizations::add);
            event.setTournament(this);
        }

        solver = new TournamentSolver(this);
    }

    /**
     * Construye un torneo
     *
     * @param name       nombre del torneo, cadena no <code>null</code>
     * @param categories categorías que componen el torneo, debe haber al menos una
     */
    public Tournament(String name, Event... categories) {
        this(name, new ArrayList<>(Arrays.asList(categories)));
    }

    /**
     * Comienza el proceso de resolución para calcular un primer horario.
     *
     * @return true si se ha encontrado una solución, false si ocurre lo contrario
     * @throws ValidationException si la validación del torneo falla
     */
    public boolean solve() throws ValidationException {
        validate();

        boolean solved = solver.execute();

        currentSchedules = solver.getSolvedSchedules();
        schedule = currentSchedules == null ? null : new TournamentSchedule(this);

        return solved;
    }

    /**
     * Actualiza el valor de los horarios con la nueva solución combinada. Si se ha llegado
     * a la última solución y se llama a este método se establece el valor de los horarios a <code>null</code>.
     * <p>
     * Además, se recalcula el horario combinado, o se le asigna <code>null</code> si no hay más soluciones.
     *
     * @return true si se han actualizado los horarios con una nueva solución, y false si ya
     * se ha alcanzado la última solución
     */
    public boolean nextSchedules() {
        currentSchedules = solver.getSolvedSchedules();
        schedule = currentSchedules == null ? null : new TournamentSchedule(this);
        return currentSchedules != null;
    }

    /**
     * Devuelve los horarios de cada categoría con el valor actual. Si no se ha actualizado el valor llamando
     * al método nextSchedules o si el solver ha alcanzado la última solución y se ha llamado seguidamente a
     * nextSchedules, devuelve null.
     *
     * @return los horarios de cada categoría
     */
    public Map<Event, EventSchedule> getCurrentSchedules() {
        return currentSchedules;
    }

    /**
     * Devuelve un único horario combinado del torneo que include todos los jugadores, todas las localizaciones de
     * juego y todas las horas o timeslots de los que compone.
     *
     * @return horario combinado del torneo
     */
    public TournamentSchedule getSchedule() {
        return schedule;
    }

    /**
     * Asigna un nombre no nulo al torneo.
     *
     * @param name nombre no <code>null</code> del torneo
     * @throws IllegalArgumentException si el nombre es <code>null</code>
     */
    public void setName(String name) {
        if (name == null)
            throw new IllegalArgumentException("Name cannot be null");

        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
     * Devuelve la lista no modificable de eventos del torneo.
     *
     * @return lista de eventos del torneo envuelta en un wrapper que la hace no modificable
     */
    public List<Event> getEvents() {
        return Collections.unmodifiableList(events);
    }

    /**
     * Devuelve la lista no modificable de todos los jugadores del torneo.
     *
     * @return lista de jugadores del torneo envuelta en un wrapper que la hace no modificable
     */
    public List<Player> getAllPlayers() {
        return Collections.unmodifiableList(allPlayers);
    }

    /**
     * Devuelve la lista no modificable de todas las localizaciones de juego del torneo.
     *
     * @return lista de localizaciones de juego del torneo envuelta en un wrapper que la hace no modificable
     */
    public List<Localization> getAllLocalizations() {
        return Collections.unmodifiableList(allLocalizations);
    }

    /**
     * Devuelve la lista no modificable de todas las horas de juego del torneo.
     *
     * @return lista de timeslots del torneo envuelta en un wrapper que la hace no modificable
     */
    public List<Timeslot> getAllTimeslots() {
        return Collections.unmodifiableList(allTimeslots);
    }

    /**
     * Devuelve el número de partidos del torneo
     *
     * @return número de partidos que se juegan en el torneo
     */
    public int getNumberOfMatches() {
        return events.stream().collect(Collectors.reducing(0, Event::getNumberOfMatches, Integer::sum));
    }

    /**
     * Devuelve el número de <i>timeslots</i> ocupados por partidos en el torneo.
     *
     * @return número de horas ocupadas
     */
    public int getNumberOfOccupiedTimeslots() {
        return events.stream().collect(Collectors.reducing(0, Event::getNumberOfOccupiedTimeslots, Integer::sum));
    }

    public TournamentSolver getSolver() {
        return solver;
    }

    @SuppressWarnings("unchecked")
    public <T> void setValidator(Validator<T> validator) {
        if (validator == null)
            throw new IllegalArgumentException("The parameter cannot be null");

        this.validator = (Validator<Tournament>) validator;
    }

    public List<String> getMessages() {
        return validator.getValidationMessages();
    }

    public void validate() throws ValidationException {
        if (!validator.validate(this))
            throw new ValidationException(String.format("Validation has failed for this tournament (%s)", name));
    }

    /**
     * Agrupa en un diccionario las categorías del torneo por el número de jugadores por partido.
     *
     * @return un diccionario donde la clave es el número de jugadores por partido y el valor, la lista
     * de categorías que definen dicho número de jugadores por partido
     */
    public Map<Integer, Set<Event>> groupEventsByNumberOfPlayersPerMatch() {
        return events.stream().collect(Collectors.groupingBy(Event::getPlayersPerMatch, Collectors.toSet()));
    }

    /**
     * Añade un timeslot no disponible para el jugador en todas las categorías donde participe. Si el jugador no
     * participa en una categoría o si la hora <code>timeslot</code> no pertenece al dominio de juego de la misma, se
     * ignora para esa categoría, así como si la hora ya ha sido marcada como no disponible para el jugador.
     *
     * @param player   culaquier jugador
     * @param timeslot culquier hora
     */
    public void addUnavailablePlayerAtTimeslot(Player player, Timeslot timeslot) {
        if (player == null || timeslot == null)
            throw new IllegalArgumentException("The parameters cannot be null");

        events.stream()
                .filter(event -> event.getPlayers().contains(player) && event.getTimeslots().contains(timeslot) &&
                        (!event.getUnavailablePlayers().containsKey(player) ||
                                !event.getUnavailablePlayers().get(player).contains(timeslot)
                        ))
                .forEach(event -> event.addUnavailablePlayerAtTimeslot(player, timeslot));
    }

    /**
     * Añade timeslots no disponibles para el jugador en todas las categorías donde participe. Si el jugador no
     * participa en determinadas categorías o si éstas no incluyen en su horas de juego algunas de las horas en el
     * conjunto <code>timeslots</code>, se ignoran esos valores para esa categoría en concreto.
     *
     * @param player    cualquier jugador
     * @param timeslots cualquier conjunto no nulo de horas en las que el jugador no esté disponible
     */
    public void addUnavailablePlayerAtTimeslots(Player player, Set<Timeslot> timeslots) {
        if (player == null || timeslots == null)
            throw new IllegalArgumentException("The parameters cannot be null");

        for (Timeslot timeslot : timeslots)
            addUnavailablePlayerAtTimeslot(player, timeslot);
    }

    /**
     * Añade el rango de <i>timeslots</i> no disponibles para el jugador en todas las categorías donde participe.
     * <p>Si el jugador no participa en determinadas categorías o si éstas no incluyen en su horas de juego algunas
     * de las horas en el conjunto <code>timeslots</code>, se ignoran esos valores para esa categoría en concreto.
     *
     * @param player jugador del torneo
     * @param t1     un extremo del rango, perteneciente a las horas del torneo
     * @param t2     el otro extremo del rango
     * @throws IllegalArgumentException si alguno de los parámetros no existe en el dominio del torneo
     */
    public void addUnavailablePlayerAtTimeslotRange(Player player, Timeslot t1, Timeslot t2) {
        if (!allPlayers.contains(player))
            throw new IllegalArgumentException(String.format("Player (%s) does not exist in the list of players of "
                    + "the tournament",
                    player
            ));

        if (!allTimeslots.contains(t1))
            throw new IllegalArgumentException(String.format("Timeslots (%s) does not exist in the list of timeslots " +
                    "" + "of the tournament", t1));

        if (!allTimeslots.contains(t2))
            throw new IllegalArgumentException(String.format("Timeslots (%s) does not exist in the list of timeslots " +
                    "" + "of the tournament", t2));

        Timeslot start, end;
        if (t1.compareTo(t2) >= 0) {
            start = t1;
            end = t2;
        } else {
            start = t2;
            end = t1;
        }

        for (int t = allTimeslots.indexOf(start); t <= allTimeslots.indexOf(end); t++)
            addUnavailablePlayerAtTimeslot(player, allTimeslots.get(t));
    }

    /**
     * Si el jugador no está disponible a la hora <code>timeslot</code>, se elimina de la lista y vuelve a estar
     * disponible a esa hora, para todas las categorías. Si el jugador no pertenece a una categoría o la hora no
     * existe en el dominio de ésta, no se lleva a cabo ninguna acción para esa categoría, así como si la hora ya ha
     * sido marcada como no disponible para el jugador.
     *
     * @param player   cualquier jugador
     * @param timeslot cualquier hora
     */
    public void removeUnavailablePlayerAtTimeslot(Player player, Timeslot timeslot) {
        if (player == null || timeslot == null)
            throw new IllegalArgumentException("The parameters cannot be null");

        events.stream()
                .filter(event -> event.getPlayers().contains(player) && event.getTimeslots().contains(timeslot))
                .forEach(event -> event.removePlayerUnavailableAtTimeslot(player, timeslot));
    }

    /**
     * Añade el timeslot como un break para todas las categorías. Si una categoría no incluye en su dominio el
     * timeslot o si éste ya ha sido marcado como break, no se lleva a cabo ninguna acción.
     *
     * @param timeslotBreak una hora cualquiera
     */
    public void addBreak(Timeslot timeslotBreak) {
        if (timeslotBreak == null)
            throw new IllegalArgumentException("Break cannot be null");

        events.stream()
                .filter(event -> event.getTimeslots().contains(timeslotBreak) && !event.isBreak(timeslotBreak))
                .forEach(event -> event.addBreak(timeslotBreak));
    }

    /**
     * Marca los <i>timeslots</i> de la lista como breaks para todas las categorías, si la categoría incluye en su
     * dominio de horas de juego cada hora de la lista de breaks; si no lo incluye o si esa hora ya está marcada como
     * break,
     * no se ejecuta ninguna acción para ese timeslot
     *
     * @param timeslotBreaks una conjunto de horas que indican un período de descanso o break
     */
    public void addBreaks(Set<Timeslot> timeslotBreaks) {
        if (timeslotBreaks == null)
            throw new IllegalArgumentException("Breaks cannot be null");

        for (Timeslot timeslot : timeslotBreaks)
            events.stream()
                    .filter(event -> event.getTimeslots().contains(timeslot) && !event.isBreak(timeslot))
                    .forEach(event -> event.addBreak(timeslot));
    }

    /**
     * Elimina el break para todas las categorías, si existe ese timeslot en el dominio del evento y si ha sido
     * marcado como break, de lo contrario no se hace nada.
     *
     * @param timeslot una hora cualquiera que se marca como break
     */
    public void removeBreak(Timeslot timeslot) {
        if (timeslot == null)
            throw new IllegalArgumentException("Timeslot cannot be null");

        events.stream()
                .filter(event -> event.getTimeslots().contains(timeslot) && event.isBreak(timeslot))
                .forEach(event -> event.removeBreak(timeslot));
    }

    /**
     * Invalida una localización a una hora o timeslot para todas las categorías, si la categoría tiene dicha
     * localización y dicha hora, de lo contrario no se toma ninguna acción, así como si la hora ya ha sido marcada
     * como no disponible para la localización.
     *
     * @param localization localización de juego a invalidar
     * @param timeslot     hora a la que invalidar
     */
    public void addUnavailableLocalizationAtTimeslot(Localization localization, Timeslot timeslot) {
        if (localization == null || timeslot == null)
            throw new IllegalArgumentException("The parameters cannot be null");

        events.stream().filter(event -> event.getLocalizations().contains(localization) &&
                event.getTimeslots().contains(timeslot) &&
                (!event.getUnavailableLocalizations().containsKey(localization) ||
                        !event.getUnavailableLocalizations().get(localization).contains(timeslot)
                )).forEach(event -> event.addUnavailableLocalizationAtTimeslot(localization, timeslot));
    }

    /**
     * Añade una localización no disponible a las horas indicadas para todas las categorías, si incluyen la
     * localización y las horas, y si no se han añadido ya.
     *
     * @param localization una localización de juego que se marca como inválida a las horas especificadas
     * @param timeslots    el conjunto de horas a las que se marca la pista inválida
     */
    public void addUnavailableLocalizationAtTimeslots(Localization localization, Set<Timeslot> timeslots) {
        if (localization == null || timeslots == null)
            throw new IllegalArgumentException("The parameters cannot be null");

        for (Timeslot timeslot : timeslots)
            addUnavailableLocalizationAtTimeslot(localization, timeslot);
    }

    /**
     * Añade una pista no disponible en un rango de horas para todas las categorías, si incluyen la
     * localización y las horas, y si no se han añadido ya.
     *
     * @param localization localización de juego del torneo
     * @param t1           un extremo del rango de horas, perteneciente al torneo
     * @param t2           el otro extremo del rango, perteneciente a las horas del torneo
     * @throws IllegalArgumentException si alguno de los parámetros no pertenecen al dominio del torneo
     */
    public void addUnavailableLocalizationAtTimeslotRange(Localization localization, Timeslot t1, Timeslot t2) {
        if (!allLocalizations.contains(localization))
            throw new IllegalArgumentException(String.format(
                    "Localization (%s) does not exist in the list of " + "localizations of the tournament",
                    localization
            ));

        if (!allTimeslots.contains(t1))
            throw new IllegalArgumentException(String.format("Timeslots (%s) does not exist in the list of timeslots " +
                    "" + "of the tournament", t1));

        if (!allTimeslots.contains(t2))
            throw new IllegalArgumentException(String.format("Timeslots (%s) does not exist in the list of timeslots " +
                    "" + "of the tournament", t2));

        Timeslot start, end;
        if (t1.compareTo(t2) >= 0) {
            start = t1;
            end = t2;
        } else {
            start = t2;
            end = t1;
        }

        for (int t = allTimeslots.indexOf(start); t <= allTimeslots.indexOf(end); t++)
            addUnavailableLocalizationAtTimeslot(localization, allTimeslots.get(t));
    }

    /**
     * Revierte la no disponibilidad de una localización de juego, si existe para las categorías que la incluyan en
     * su dominio, es decir, en su lista de localizaciones de juego.
     *
     * @param localization localización que vuelve a estar disponible para todas las horas
     */
    public void removeUnavailableLocalization(Localization localization) {
        if (localization == null)
            throw new IllegalArgumentException("Localization cannot be null");

        events.stream()
                .filter(event -> event.getLocalizations().contains(localization))
                .forEach(event -> event.removeUnavailableLocalization(localization));
    }

    /**
     * Revierte la no disponibilidad de una localización de juego a la hora especificada, si existe para las
     * categorías en las que tanto la localización como la hora existan en sus correspondientes dominios, así como
     * que la localización se encuentre marcada como no disponible a la hora <code>timeslot</code>; de lo contrario,
     * no se tomará ninguna acción.
     *
     * @param localization localización de juego para la que se va a marcar una hora como disponible de nuevo
     * @param timeslot     hora que se va a marcar nuevamente como disponible una localización
     */
    public void removeUnavailableLocalizationAtTimeslot(Localization localization, Timeslot timeslot) {
        if (localization == null || timeslot == null)
            throw new IllegalArgumentException("The parameters cannot be null");

        events.stream()
                .filter(event -> event.getLocalizations().contains(localization) &&
                        event.getTimeslots().contains(timeslot))
                .forEach(event -> event.removeUnavailableLocalizationTimeslot(localization, timeslot));
    }

    /**
     * Muestra por la salida estándar una representación de los horarios de cada categoría.
     *
     * @param printMatches si es <code>true</code> se mostrará adicionalmente un resumen de los partidos para cada
     *                     categoría,
     *                     y si es <code>false</code>, solamente se mostrarán los horarios
     */
    public void printCurrentSchedules(boolean printMatches) {
        StringBuilder sb = new StringBuilder();

        if (currentSchedules == null)
            sb.append("Empty schedule.\n");
        else {
            for (Event event : events) {
                EventSchedule schedule = currentSchedules.get(event);

                sb.append(schedule.toString()).append("\n");

                if (printMatches) {
                    sb.append(String.format("Match duration: %d timelots\n",
                            schedule.getEvent().getTimeslotsPerMatch()
                    ));

                    List<Match> matches = schedule.getMatches();
                    for (Match match : matches)
                        sb.append(match).append("\n");
                }
                sb.append("\n");
            }
        }
        System.out.println(sb.toString());
    }

    /**
     * Muestra los horarios y los partidos por la salida estándar
     */
    public void printCurrentSchedules() {
        printCurrentSchedules(true);
    }

    public String toString() {
        return name;
    }
}
