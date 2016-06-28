package es.uca.garciachacon.eventscheduler.rest.dao;

import es.uca.garciachacon.eventscheduler.data.model.schedule.EventSchedule;
import es.uca.garciachacon.eventscheduler.data.model.schedule.TournamentSchedule;
import es.uca.garciachacon.eventscheduler.data.model.tournament.Event;
import es.uca.garciachacon.eventscheduler.data.model.tournament.Tournament;
import es.uca.garciachacon.eventscheduler.data.validation.validable.ValidationException;
import es.uca.garciachacon.eventscheduler.solver.TournamentSolver.ResolutionState;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.util.UUID.randomUUID;

/**
 * Implementación básica del DAO para manipular torneos, que serían los recursos que manejase el servidor web.
 * <p>
 * El almacenamiento de los recursos es la memoria del servidor, que persiste en un diccionario concurrente
 * (<i>thread-safe</i>) todos los torneos creados y un identificador único asociado generado automáticamente que sirve
 * como referencia para operaciones futuras sobre el torneo.
 * <p>
 * Es posible activar un período de expiración que limitará la duración de cada torneo creado, pues serán eliminados
 * una vez superado tiempo especificado. Por defecto, este sistema de expiración está desactivado.
 */
public class TournamentDao implements ITournamentDao {
    /**
     * Conjunto de torneos almacenados, a los que se asocia un identificador único. Soporta concurrencia.
     */
    private Map<String, Tournament> tournaments = new ConcurrentHashMap<>();

    /**
     * Retardo con el que se eliminará un torneo de la colección una vez sea creado (caducidad del período de
     * expiración)
     */
    private long delay = 5000;

    /**
     * Indica si se activará un período de expiración para cada inserción de un nuevo torneo
     */
    private boolean expirationFlag = false;

    /**
     * Devuelve el período de expiración de la inserción de un nuevo torneo, en milisegundos.
     *
     * @return período de expiración de un nuevo torneo
     */
    public long getDelay() {
        return delay;
    }

    /**
     * Fija el valor del período de expiración de creación de un torneo en milisegundos. Para que tenga efecto, se
     * debe activar la función de expiración mediante {@link TournamentDao#setExpirationFlag(boolean)}.
     *
     * @param delay cantidad positiva de milisegundos tras los que se borrará cada nuevo torneo creado
     * @throws IllegalArgumentException si <code>delay</code> no es positivo
     */
    public void setDelay(long delay) {
        if (delay <= 0)
            throw new IllegalArgumentException();

        this.delay = delay;
    }

    /**
     * Comprueba si el sistema de expiración está activado.
     *
     * @return <code>true</code> si está activado; <code>false</code> si no
     */
    public boolean getExpirationFlag() {
        return expirationFlag;
    }

    /**
     * Activa o desactiva el sistema de expiración.
     *
     * @param expirationFlag <code>true</code> para activarlo, <code>false</code> para desactivarlo.
     */
    public void setExpirationFlag(boolean expirationFlag) {
        this.expirationFlag = expirationFlag;
    }

    /**
     * Devuelve el conjunto de torneos almacenados y sus identificadores asociados.
     *
     * @return pares de identificadores y torneos
     */
    public synchronized Map<String, Tournament> getAll() {
        return tournaments;
    }

    /**
     * Devuelve la lista de identificadores correspondientes a los torneos existentes.
     *
     * @return conjunto de identificadores de torneo únicos
     */
    public synchronized Set<String> getIds() {
        return tournaments.keySet();
    }

    /**
     * Devuelve un torneo con el identificador especificado.
     *
     * @param id identificador del torneo que se quiere obtener
     * @return si existe un torneo con el <code>id</code> especificado se devuelve envuelto en un {@link Optional};
     * si no existe, se devuelve {@link Optional#empty()}
     */
    public synchronized Optional<Tournament> get(String id) {
        return Optional.of(tournaments.get(id));
    }

    /**
     * Añade un torneo a la colección. El identificador único es generado automáticamente mediante
     * {@link UUID#randomUUID()} y asociado al nuevo torneo
     *
     * @param tournament torneo a añadir
     * @return identificador generado asociado al torneo añadido
     */
    public synchronized String create(Tournament tournament) {
        String id = randomUUID().toString();
        tournaments.put(id, tournament);

        if (expirationFlag)
            Executors.newScheduledThreadPool(1).schedule(() -> tournaments.remove(id), delay, TimeUnit.MILLISECONDS);

        return id;
    }

    /**
     * Elimina un torneo con el identificador especificado.
     *
     * @param id identificador del torneo a eliminar
     * @return <code>true</code> si existe el torneo y se ha eliminado; <code>false</code> si no
     */
    public synchronized boolean delete(String id) {
        return tournaments.remove(id) != null;
    }

    /**
     * Devuelve el horario del torneo con el identificador especificado.
     * <p>
     * Si no existe un torneo con ese identificador, o si no existe un horario para el torneo, se devuelve
     * {@link Optional#empty()}.
     *
     * @param id identificador del torneo cuyo horario se quiere obtener
     * @return el horario del torneo envuelto en {@link Optional}, o {@link Optional#empty()} si no existe el torneo
     * o el horario
     */
    @Override
    public Optional<TournamentSchedule> getSchedule(String id) throws ValidationException {
        if (!tournaments.containsKey(id))
            return Optional.empty();

        Tournament tournament = tournaments.get(id);
        tournament.solve();

        return Optional.ofNullable(tournament.getSchedule());
    }

    /**
     * Devuelve el siguiente horario del torneo. Si no hay más, se devuelve un opcional vacío.
     * <p>
     * Si no se ha iniciado aún el proceso de resolución, este método lo hará, llamando a
     * {@link TournamentDao#getSchedule(String)}.
     *
     * @param id identificador del torneo cuyo horario se quiere obtener
     * @return siguiente horario del torneo, u opcional vacío si no existe el torneo o no tiene horario
     */
    @Override
    public Optional<TournamentSchedule> getNextSchedule(String id) throws ValidationException {
        if (!tournaments.containsKey(id))
            return Optional.empty();

        Tournament tournament = tournaments.get(id);
        if (tournament.getSolver().getResolutionState() == ResolutionState.READY)
            tournament.solve();
        else
            tournament.nextSchedules();

        return Optional.ofNullable(tournament.getSchedule());
    }

    /**
     * Devuelve los horarios de los eventos del torneo.
     *
     * @param id el identificador del torneo
     * @return horarios de cada evento del torneo, o un opcional vacío
     */
    @Override
    public Optional<Map<Event, EventSchedule>> getEventSchedules(String id) {
        if (!tournaments.containsKey(id))
            return Optional.empty();

        return Optional.ofNullable(tournaments.get(id).getEventSchedules());
    }

    /**
     * Devuelve el horario del evento especificado del torneo con el identificador que se indica.
     *
     * @param id       identificador del torneo al que pertenece el evento
     * @param position posición del evento en la lista de eventos del torneo
     * @return el horario del evento, o un opcional vacío si no procede
     */
    @Override
    public Optional<EventSchedule> getEventSchedule(String id, int position) {
        Optional<EventSchedule> optSchedule = Optional.empty();

        if (!tournaments.containsKey(id))
            return optSchedule;

        Tournament tournament = tournaments.get(id);
        List<Event> events = tournament.getEvents();

        Map<Event, EventSchedule> schedules = tournament.getEventSchedules();
        if (position >= 1 && position <= events.size() && schedules != null)
            optSchedule = Optional.of(schedules.get(events.get(position - 1)));

        return optSchedule;
    }
}
