package es.uca.garciachacon.eventscheduler.rest.dao;

import es.uca.garciachacon.eventscheduler.data.model.schedule.TournamentSchedule;
import es.uca.garciachacon.eventscheduler.data.model.tournament.Tournament;

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
     * Añade un torneo a la colección de forma indefinida. Se le asigna un identificador único.
     *
     * @param tournament torneo a añadir
     * @return identificador asociado al torneo creado
     */
    String create(Tournament tournament);

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
     *
     * @param id identificador del torneo cuyo horario se quiere obtener
     * @return un opcional que incluye el horario, o un opcional vacío si no existe un torneo con el identificador
     * indicado, o no existe un horario para el torneo
     */
    Optional<TournamentSchedule> getSchedule(String id);
}
