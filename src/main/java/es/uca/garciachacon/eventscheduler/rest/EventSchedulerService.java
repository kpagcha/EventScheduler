package es.uca.garciachacon.eventscheduler.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import es.uca.garciachacon.eventscheduler.data.model.schedule.EventSchedule;
import es.uca.garciachacon.eventscheduler.data.model.schedule.InverseSchedule;
import es.uca.garciachacon.eventscheduler.data.model.schedule.Schedule;
import es.uca.garciachacon.eventscheduler.data.model.schedule.TournamentSchedule;
import es.uca.garciachacon.eventscheduler.data.model.tournament.Event;
import es.uca.garciachacon.eventscheduler.data.model.tournament.Tournament;
import es.uca.garciachacon.eventscheduler.data.validation.validable.ValidationException;
import es.uca.garciachacon.eventscheduler.rest.dao.ITournamentDao;
import es.uca.garciachacon.eventscheduler.solver.ResolutionData;
import es.uca.garciachacon.eventscheduler.solver.TournamentSolver;
import es.uca.garciachacon.eventscheduler.solver.TournamentSolver.ResolutionState;
import es.uca.garciachacon.eventscheduler.solver.TournamentSolver.SearchStrategy;
import es.uca.garciachacon.eventscheduler.utils.TournamentUtils;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Servicio web REST que proporciona una API para el manejo de torneos, su tratamiento como recursos, el cálculo de
 * sus horarios sucesivos y los partidos asociados.
 */
@Path("eventscheduler")
public class EventSchedulerService {
    private ITournamentDao dao;

    /**
     * Inyecta automáticamente la implementación del DAO especificada en {@link Binder}.
     *
     * @param tournamentDao implementación del DAO que se usará en el servicio
     */
    @Inject
    public EventSchedulerService(ITournamentDao tournamentDao) {
        dao = tournamentDao;
    }

    /**
     * Petición GET que devuelve un diccionario de los identificadores y torneos asociados que están almacenados en
     * el servidor.
     *
     * @return pares de identificador-torneo almacenados
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> getTournaments() {
        Map<String, String> tournaments = new HashMap<>();
        dao.getAll().forEach((id, t) -> tournaments.put(id, t.getName()));

        return tournaments;
    }

    /**
     * Petición POST para crear un torneo. Se devolverá el identificador con el que se ha asociado al torneo creado
     * en el servidor para futuras referencias.
     *
     * @param tournament torneo a crear
     * @return identificador único del torneo que se ha creado
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public String createTournament(Tournament tournament) {
        return dao.create(tournament);
    }

    /**
     * Petición GET similar a {@link EventSchedulerService#getTournaments()} pero que devuelve una lista de
     * únicamente los identificadores de los torneos almacenados en el servidor.
     *
     * @return lista de identificadores únicos
     */
    @Path("/ids")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Set<String> getIds() {
        return dao.getIds();
    }

    /**
     * Petición GET que devuelve en forma de JSON el torneo cuyo identificador indica mediante un parámetro de ruta.
     * <p>
     * Si no existe un torneo con el identificador enviado, se devolverá una respuesta con código 404.
     *
     * @param id el identificador del torneo que se quiere obtener
     * @return torneo correspondiente al identificador especificado
     */
    @Path("/{id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Tournament getTournament(@PathParam("id") String id) {
        Optional<Tournament> optTournament = dao.get(id);
        if (optTournament.isPresent())
            return optTournament.get();

        throw new NotFoundException();
    }

    /**
     * Petición DELETE que elimina un torneo con el identificador asociado que se especifica en la ruta.
     * <p>
     * Si existe un torneo con el identificador indicado, se devuelve una respuesta HTTP con código 204 <i>
     * (NO_CONTENT)</i>. Si no existe, la respuesta tendrá el código 404 <i>(NOT_FOUND)</i>.
     *
     * @param id el identificador del torneo a eliminar
     * @return respuesta con código 204 si el torneo ha sido eliminador; 404 si no
     */
    @Path("/{id}")
    @DELETE
    public Response deleteTournament(@PathParam("id") String id) {
        if (dao.delete(id))
            return Response.status(Response.Status.NO_CONTENT).build();

        throw new NotFoundException();
    }

    /**
     * Petición GET para consultar el número de soluciones (distintos horarios) encontradas hasta el momento.
     * <p>
     * Si no existe un torneo con ese identificador, se responde con código 404.
     *
     * @param id el identificador del torneo
     * @return número de distintos horarios calculados
     */
    @Path("/{id}/schedule/found-solutions")
    @GET
    public long getFoundSolutions(@PathParam("id") String id) {
        Optional<Tournament> optTournament = dao.get(id);

        if (optTournament.isPresent())
            return optTournament.get().getSolver().getFoundSolutions();

        throw new NotFoundException();
    }

    /**
     * Petición GET para consultar el estado del proceso de la resolución del torneo cuyo identificador se incluye en
     * la ruta. Si no existe un torneo con ese identificador, se responde con código 404.
     *
     * @param id el identificador del torneo
     * @return el estado del proceso de resolución
     */
    @Path("/{id}/schedule/resolution-state")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ResolutionState getResolutionState(@PathParam("id") String id) {
        Optional<Tournament> optTournament = dao.get(id);

        if (optTournament.isPresent())
            return optTournament.get().getSolver().getResolutionState();

        throw new NotFoundException();
    }

    /**
     * Petición GET para consultar las estadísticas del proceso de resolución del torneo especificado.
     * <p>
     * Si no existe un torneo con ese identificador, se responde con código 404.
     *
     * @param id el identificador del torneo
     * @return estadísticas del proceso de resolución del torneo
     */
    @Path("/{id}/schedule/resolution-data")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ResolutionData getResolutionData(@PathParam("id") String id) {
        Optional<Tournament> optTournament = dao.get(id);

        if (optTournament.isPresent())
            return optTournament.get().getSolver().getResolutionData();

        throw new NotFoundException();
    }

    /**
     * Petición GET que para la computación del proceso de resolución del torneo indicado, si éste esta siendo
     * ejecutado en el momento de la petición. Si no, no se producirá ninguna modificación y se devolverá el estado
     * de la resolución actual.
     * <p>
     * Si la resolución es parada de forma satisfactoria, se devolverá
     * {@link es.uca.garciachacon.eventscheduler.solver.TournamentSolver.ResolutionState#INCOMPLETE}.
     * <p>
     * Si no existe un torneo con el identificador especificado se responderá con código 404.
     *
     * @param id el identificador del torneo
     * @return estado de la resolución
     */
    @Path("/{id}/schedule/stop-resolution")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ResolutionState stopResolutionProcess(@PathParam("id") String id) {
        Optional<Tournament> optTournament = dao.get(id);

        if (optTournament.isPresent()) {
            TournamentSolver solver = optTournament.get().getSolver();
            if (solver.getResolutionState() == ResolutionState.COMPUTING)
                solver.stopResolutionProcess();
            return solver.getResolutionState();
        }

        throw new NotFoundException();
    }

    /**
     * Petición GET para conseguir los horarios del torneo con el identificador especificado. Se devuelve
     * <code>null</code> (respuesta 204) si no existe un horario asociado al torneo (no tiene solución, o ya se han
     * encontrado todas ellas).
     * <p>
     * Si no hay un torneo con el identificado indicado la respuesta será 404.
     * <p>
     * Este método lanza el proceso de resolución de un torneo si no se había lanzado anteriormente, mientras que si
     * sí había comenzado ya, se calculará la siguiente solución y se devolverá el nuevo horario (si hay).
     * <p>
     * Mediante parámetros en la URL, <i>query parameters</i>, que son opcionales, se puede conseguir un
     * comportamiento adicional en este método:
     * <ul>
     * <li><i>restart</i>, con valor <i>true</i> fuerza a que el proceso de resolución se reinicie, reconstruyéndose el
     * modelo del torneo y recalculándose los horarios desde cero.</li>
     * <li><i>onlyGet</i>, con valor <i>true</i> hace que el método no active el proceso de resolución y calcule
     * un horario, sino que simplemente devuelva el existente. Tiene prioridad sobre la opción <i>restart</i>, de
     * modo que será ignorada, es decir, no se reiniciará el proceso de resolución, si <i>onlyGet</i> está presente y
     * es <i>true</i>.
     * </li>
     * </ul>
     * <p>
     * Los siguientes parámetros de URL opcionales permiten configurar opciones del proceso de resolución. Nótese que
     * al reiniciar el proceso mediante el método anteriormente descrito (parámetro <i>restart</i>), esta
     * configuración persistirá, y si se desea deshacer debe ser de forma explícita, por ejemplo,
     * <i>prioritizeTimeslots=false</i>.
     * <ul>
     * <li><i>searchStrategy</i> permite elegir la estrategia de búsqueda a emplear, siendo las opciones
     * <i>DOMOVERWDEG</i>, <i>MINDOM_UB</i> y <i>MINDOM_LB</i>.</li>
     * <li><i>prioritizeTimeslots</i> permite priorizar la asignación de partidos sobre <i>timeslots</i> antes
     * que sobre localizaciones, es decir, si su valor es <i>true</i> se intentará hacer uso antes de los
     * <i>timeslots</i> disponibles que de las localizaciones, mientras que si es <i>false</i> será lo
     * contrario. Esto solamente tiene efecto si la estrategia de búsqueda empleada es <i>MINDOM_UB</i> o
     * <i>MINDOM_LB</i>.</li>
     * <li>limit</li> indica el tiempo límite de resolución máximo en milisegundos, por ejemplo;
     * <i>limit=5000</i> parará la resolución del torneo si se superan los 5 segundos de cálculo en la
     * computación de la solución.
     * </ul>
     * <p>
     * Este último grupo de parámetros solamente se aplicarán si se va a reiniciar el proceso de resolución (mediante
     * el parámetro <i>restart</i>) o si no está en progreso, es decir, ha terminado (con o sin solución).
     *
     * @param id el identificador del torneo
     * @return horario del torneo, que puede ser <code>null</code> si no tiene
     * @throws ValidationException si la validación del torneo es fallida
     */
    @Path("/{id}/schedule")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Schedule getTournamentSchedule(@PathParam("id") String id,
            @QueryParam("restart") Boolean restart,
            @QueryParam("onlyGet") Boolean onlyGet,
            @QueryParam("byLocalizations") Boolean byLocalizations,
            @QueryParam("searchStrategy") String searchStrategy,
            @QueryParam("prioritizeTimeslots") Boolean prioritizeTimeslots,
            @QueryParam("limit") Long resolutionTimeLimit) throws ValidationException {

        Optional<Tournament> optTournament = dao.get(id);
        if (!optTournament.isPresent())
            throw new NotFoundException();

        Tournament tournament = optTournament.get();
        TournamentSolver solver = tournament.getSolver();

        // Los parámetros de configuración del solver solamente se aplican cuando se intenta reiniciar el proceso de
        // resolución del torneo o cuando éste ha terminado; de lo contrario, no tiene sentido configurar estas
        // opciones cuando la resolución aún esta en progreso y no ha terminado
        if (Boolean.TRUE.equals(restart) || !solver.hasResolutionProcessStarted()) {
            if (searchStrategy != null) {
                try {
                    solver.setSearchStrategy(SearchStrategy.valueOf(searchStrategy));
                } catch (IllegalArgumentException e) {
                    throw new BadRequestException(e.getMessage());
                }
            }

            if (prioritizeTimeslots != null)
                solver.prioritizeTimeslots(prioritizeTimeslots);

            if (resolutionTimeLimit != null)
                solver.setResolutionTimeLimit(resolutionTimeLimit);
        }

        Optional<TournamentSchedule> optSchedule;

        if (Boolean.TRUE.equals(onlyGet))
            optSchedule = Optional.ofNullable(tournament.getSchedule());
        else if (Boolean.TRUE.equals(restart))
            try {
                optSchedule = dao.getSchedule(id);
            } catch (IllegalStateException e) {
                throw new BadRequestException(e.getMessage());
            }
        else
            optSchedule = dao.getNextSchedule(id);

        if (optSchedule.isPresent()) {
            if (Boolean.TRUE.equals(byLocalizations))
                return new InverseSchedule(tournament);
            else
                return optSchedule.get();
        }

        return null;
    }

    /**
     * Petición GET que devuelve los horarios de cada evento del torneo especificado mediante un identificador como
     * parámetro de ruta.
     * <p>
     * Si no existe un horario con ese identificador, se provocará una respuesta 404. Si no existen horarios para el
     * torneo, la respuesta será 204.
     * <p>
     * Se puede añadir un parámetro de URL adicional, <i>byLocalizations</i> para que los horarios tengan
     * formato inverso o por localizaciones en lugar de tener el formato normal o por jugadores (si el valor del
     * parámetro es <i>true</i>).
     *
     * @param id el identificador del torneo
     * @return horarios de los eventos del torneo, o <code>null</code> si no hay
     */
    @Path("/{id}/schedules")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<Event, Schedule> getEventSchedules(@PathParam("id") String id,
            @QueryParam("byLocalizations") Boolean byLocalizations) {

        Optional<Tournament> optTournament = dao.get(id);
        if (!optTournament.isPresent())
            throw new NotFoundException();

        Optional<Map<Event, EventSchedule>> optSchedules = dao.getEventSchedules(id);
        if (optSchedules.isPresent()) {
            if (Boolean.TRUE.equals(byLocalizations))
                return optSchedules.get()
                        .keySet()
                        .stream()
                        .collect(Collectors.toMap(Function.identity(), InverseSchedule::new));
            else
                return optSchedules.get()
                        .entrySet()
                        .stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }

        return null;
    }

    /**
     * Petición GET que devuelve el horario de un evento perteneciente a un torneo concreto. Si no existe el torneo,
     * o tal evento en ese torneo, la respuesta será de código 404. Si existen ambos, pero no hay un horario para el
     * torneo, y por tanto, para el evento, se responderá con código 204.
     * <p>
     * Mediante un parámetro de URL opcional, <i>byLocalizations</i>, se puede devolver el horario calculado en
     * formato inverso o por localizaciones ({@link InverseSchedule}), si se incluye con valor <i>true</i>.
     *
     * @param id  el identificador del torneo
     * @param pos la posición del evento en la lista de eventos del torneo
     * @return horario del evento; o <code>null</code> si no existe el horario
     */
    @Path("/{id}/schedules/{pos}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Schedule getEventSchedule(@PathParam("id") String id,
            @PathParam("pos") int pos,
            @QueryParam("byLocalizations") Boolean byLocalizations) {

        Optional<Tournament> optTournament = dao.get(id);
        if (!optTournament.isPresent() || pos < 1 || pos > optTournament.get().getEvents().size())
            throw new NotFoundException();

        Optional<EventSchedule> schedule = dao.getEventSchedule(id, pos);
        if (schedule.isPresent()) {
            if (Boolean.TRUE.equals(byLocalizations))
                return new InverseSchedule(schedule.get().getEvent());
            else
                return schedule.get();
        }

        return null;
    }

    public static void main(String[] args) throws JsonProcessingException, ValidationException {

        /*List<Player> players = TournamentUtils.buildGenericPlayers(32, "Player");
        List<Localization> localizations = TournamentUtils.buildGenericLocalizations(5, "Court");
        List<Timeslot> timeslots = TournamentUtils.buildSimpleTimeslots(13);

        Event event1 = new Event("Event 1", players, localizations, timeslots);
        Event event2 = new Event("Event 2", players.subList(0, 8), localizations.subList(2, 4), timeslots);

        event2.setMatchesPerPlayer(3);

        Tournament tournament = new Tournament("Tournament", event1, event2);
        TournamentSolver solver = tournament.getSolver();
        solver.setSearchStrategy(SearchStrategy.MINDOM_UB);
        solver.setResolutionTimeLimit(100);

        tournament.solve();

        int tries = 0;
        while (solver.getResolutionState() == ResolutionState.INCOMPLETE) {
            tournament.solve();
            System.out.println(++tries);
        }

        System.out.println(solver.getResolutionState());*/

        //System.out.println(tournament.getSolver().getResolutionData().getResolutionTime());

        //ObjectMapper mapper = new ObjectMapper();
        //String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schedule);

        Event event = new Event("Event",
                TournamentUtils.buildGenericPlayers(18, "Player"),
                TournamentUtils.buildGenericLocalizations(1, "Court"),
                TournamentUtils.buildSimpleTimeslots(6)
        );
        event.setPlayersPerMatch(6);
        event.setPlayersPerTeam(3);

        Tournament tournament = new Tournament("Tournament", event);
        TournamentSolver solver = tournament.getSolver();
        //solver.setSearchStrategy(SearchStrategy.MINDOM_UB);
        solver.setResolutionTimeLimit(100);

        tournament.solve();

        // ESTE ULTIMO TORNEO POR EQUIPOS AL HACER EL BUCLE DE ABAJO A VECES DA UNFEASIBLE Y OTRAS ENCUENTRA SOLUCION
        // ?????????????????????
        int tries = 0;
        while (solver.getResolutionState() == ResolutionState.INCOMPLETE) {
            tournament.solve();
            System.out.println(++tries);
        }

        System.out.println(solver.getResolutionState());
    }
}