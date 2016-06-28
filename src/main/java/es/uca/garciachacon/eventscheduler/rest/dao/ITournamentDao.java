package es.uca.garciachacon.eventscheduler.rest.dao;

import es.uca.garciachacon.eventscheduler.data.model.schedule.EventSchedule;
import es.uca.garciachacon.eventscheduler.data.model.schedule.TournamentSchedule;
import es.uca.garciachacon.eventscheduler.data.model.tournament.Event;
import es.uca.garciachacon.eventscheduler.data.model.tournament.Tournament;
import es.uca.garciachacon.eventscheduler.data.validation.validable.ValidationException;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * DAO <i>Data Access Object</i> para la abstracción, acceso y manipulación de entidades de tipo torneo almacenadas
 * en cualquier sistema de persistencia. Ofrece operaciones tipo CRUD sobre la colecciones de torneos, además de
 * otras operaciones adicionales de manejo de datos.
 * <p>
 * Gestiona la conexión y la comunicación con el sistema de persistencia, y se encarga de suministrar los datos y
 * aplicar las operaciones solicitadors sobre los mismos de forma transparente.
 */
public interface ITournamentDao {
    /**
     * Devuelve un conjunto de pares de identificadores únicos de tipo {@link String} y torneos asociados, de tipo
     * {@link Tournament}. Estos torneos son los almacenados en el sistema de persistencia.
     * <p>
     * Si no hay ningún torneo presente, se devuelve una estructura vacía.
     *
     * @return diccionario de identificadores y torneos
     */
    Map<String, Tournament> getAll();

    /**
     * Devuelve con conjunto de identificadores únicos de tipo {@link String} correspondientes a cada torneo
     * existente en la colección. Es similar a {@link ITournamentDao#getAll()}, excepto que solamente
     * se obtienen los identificadores.
     * <p>
     * Si no existe ningún torneo, se devuelve un conjunto vacío.
     *
     * @return lista de identificadores de todos los torneos almacenados
     */
    Set<String> getIds();

    /**
     * Devuelve un torneo con el identificador asociado, encapsulado por la clase {@link Optional}. Si no existe un
     * torneo con dicho identificador, se devuelve {@link Optional#empty()}.
     *
     * @param id identificador del torneo que se quiere obtener
     * @return un torneo envuelto por la clase {@link Optional}, que contiene la instancia del torneo y se puede
     * obtener mediante {@link Optional#get()}, si existe un torneo con el <code>id</code> especificado; o una
     * instancia de {@link Optional} vacía.
     */
    Optional<Tournament> get(String id);

    /**
     * Añade un torneo a la colección de forma indefinida. Se le asigna un identificador único.
     *
     * @param tournament torneo a añadir
     * @return identificador asociado al torneo creado
     */
    String create(Tournament tournament);

    /**
     * Elimina de la colección un torneo con el identificador especificado, si existe.
     *
     * @param id identificador del torneo a eliminar
     * @return <code>true</code> si existe un torneo con el identificador especificado y se ha eliminado;
     * <code>false</code> si sucede lo contrario
     */
    boolean delete(String id);

    /**
     * Devuelve el horario del torneo con el identificador especificado, si existe. Si no existe, se devuelve
     * {@link Optional#empty()}.
     * <p>
     * Si no existe un horario para el horario, porque se han encontrado todos los horarios anteriormente o porque
     * simplemente no existe un horario posible dada la configuración del torneo, se devolverá {@link Optional#empty()}.
     * <p>
     * Este método lanza el proceso de resolución del torneo desde el comienzo, es decir, si se ya se había comenzado
     * el proceso de resolución, este se reiniciará. Para obtener los sucesivos horarios, se debería de hacer uso del
     * método se debe usar {@link ITournamentDao#getNextSchedule(String)}.
     *
     * @param id identificador del torneo cuyo horario se quiere obtener
     * @return un opcional que incluye el horario, o un opcional vacío si no existe un torneo con el identificador
     * indicado, o no existe un horario para el torneo
     */
    Optional<TournamentSchedule> getSchedule(String id) throws ValidationException;

    /**
     * Devuelve el siguiente horario del torneo con el identificador especificado, si existe; si no, se devuelve
     * {@link Optional#empty()}.
     * <p>
     * Si no hay más horarios posibles para el torneo, se devuelve {@link Optional#empty()}.
     * <p>
     * Este método asume que el proceso de resolución de un torneo ya ha comenzado (es decir, se ha llamado a
     * {@link ITournamentDao#getSchedule(String)}, pero si es así, la llamará automáticamente.
     *
     * @param id identificador del torneo cuyo horario se quiere obtener
     * @return un opcional con el siguiente horario, o vacío si no existe un torneo con el identificador que se
     * especifica, o no hay más horarios para el torneo
     */
    Optional<TournamentSchedule> getNextSchedule(String id) throws ValidationException;

    /**
     * Devuelve los horarios de cada evento del torneo especificado.
     * <p>
     * Si no existe un torneo con el identificador especificado, o no existen actualmente horarios para el torneo (y
     * por tanto, horarios de cada evento), se devuelve un opcional vacío ({@link Optional#empty()}).
     *
     * @param id el identificador del torneo
     * @return diccionario con cada evento del torneo y su horario; o un opcional vacío si no existe el torneo, o no
     * existen los horarios
     */
    Optional<Map<Event, EventSchedule>> getEventSchedules(String id);

    /**
     * Devuelve el horario del evento correspondiente a la posición especificada en la lista de eventos del torneo con
     * el identificador indicado.
     * <p>
     * Se devolverá el opcional vacío {@link Optional#empty()} si no existe un torneo con el identificador que se
     * especifica, o si la posición de evento es superior al tamaño de la lista de eventos del torneo, o si no existe
     * un horario disponible.
     * <p>
     * Este método no lanza el proceso de resolución del torneo. Se limita únicamente a devolver el horario ya
     * calculado.
     *
     * @param id       identificador del torneo al que pertenece el evento
     * @param position posición del evento en la lista de eventos del torneo
     * @return un opcional con el horario del evento, o vació si no hay horario o no son correctos los argumentos
     */
    Optional<EventSchedule> getEventSchedule(String id, int position);
}
