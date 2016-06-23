package es.uca.garciachacon.eventscheduler.rest.dao;

import es.uca.garciachacon.eventscheduler.data.model.schedule.TournamentSchedule;
import es.uca.garciachacon.eventscheduler.data.model.tournament.Tournament;
import es.uca.garciachacon.eventscheduler.data.validation.validable.ValidationException;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
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
    private boolean expire = false;

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
     * debe activar la función de expiración mediante {@link TournamentDao#setExpire(boolean)}.
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
    public boolean getExpire() {
        return expire;
    }

    /**
     * Activa o desactiva el sistema de expiración.
     *
     * @param expire <code>true</code> para activarlo, <code>false</code> para desactivarlo.
     */
    public void setExpire(boolean expire) {
        this.expire = expire;
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
     * Añade un torneo a la colección. El identificador único es generado automáticamente mediante
     * {@link UUID#randomUUID()} y asociado al nuevo torneo
     *
     * @param tournament torneo a añadir
     * @return identificador generado asociado al torneo añadido
     */
    public synchronized String create(Tournament tournament) {
        String id = randomUUID().toString();
        tournaments.put(id, tournament);

        if (expire)
            Executors.newScheduledThreadPool(1).schedule(() -> tournaments.remove(id), delay, TimeUnit.MILLISECONDS);

        return id;
    }

    /**
     * Devuelve un torneo con el identificador especificado.
     *
     * @param id identificador del torneo que se quiere obtener
     * @return si existe un torneo con el <code>id</code> especificado se devuelve envuelto en un {@link Optional};
     * si no existe, se devuelve {@link Optional#empty()}
     */
    public synchronized Optional<Tournament> get(String id) {
        if (tournaments.containsKey(id))
            return Optional.of(tournaments.get(id));
        return Optional.empty();
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
    public Optional<TournamentSchedule> getSchedule(String id) {
        Optional<TournamentSchedule> optSchedule = Optional.empty();

        if (tournaments.containsKey(id)) {
            Tournament tournament = tournaments.get(id);

            if (!tournament.allSolutionsReached()) {
                TournamentSchedule schedule = tournament.getSchedule();

                // Aún no se ha iniciado el proceso de resolución, entonces se inicia. También puede ser que sí se
                // haya iniciado anteriormente, pero el torneo no tiene horario posible; esta distinción de casos es
                // imposibles por el momento, y es responsabilidad de la clase Tournament
                if (schedule == null) {
                    try {
                        if (tournament.solve())
                            optSchedule = Optional.of(tournament.getSchedule());
                    } catch (ValidationException e) {
                        e.printStackTrace();
                    }
                } else {
                    if (tournament.nextSchedules())
                        optSchedule = Optional.of(tournament.getSchedule());
                }
            }
        }
        return optSchedule;
    }
}
