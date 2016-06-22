package es.uca.garciachacon.eventscheduler.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import es.uca.garciachacon.eventscheduler.data.model.tournament.Tournament;
import es.uca.garciachacon.eventscheduler.data.model.tournament.event.Event;
import es.uca.garciachacon.eventscheduler.rest.dao.ITournamentDao;
import es.uca.garciachacon.eventscheduler.utils.TournamentUtils;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Servicio web REST que proporciona una API para el manejo de torneos, su tratamiento como recursos, el cálculo de
 * sus horarios sucesivos y los partidos asociados.
 */
@Path("eventscheduler")
public class EventSchedulerService {
    private ITournamentDao tournamentDao;

    /**
     * Inyecta automáticamente la implementación del DAO especificada en {@link Binder}.
     *
     * @param tournamentDao implementación del DAO que se usará en el servicio
     */
    @Inject
    public EventSchedulerService(ITournamentDao tournamentDao) {
        this.tournamentDao = tournamentDao;
    }

    public static void main(String[] args) throws JsonProcessingException {
        Event e1 = new Event(
                "Event",
                TournamentUtils.buildGenericPlayers(4, "Player"),
                TournamentUtils.buildGenericLocalizations(1, "Court"),
                TournamentUtils.buildLocalTimeTimeslots(2)
        );
        e1.setTimeslotsPerMatch(1);
        Tournament t1 = new Tournament("Another Tournament", e1);

        Event e2 = new Event(
                "Event",
                TournamentUtils.buildGenericPlayers(8, "Player"),
                TournamentUtils.buildGenericLocalizations(3, "Court"),
                TournamentUtils.buildLocalTimeTimeslots(6)
        );
        Tournament t2 = new Tournament("Tournament", e2);

        System.out.println(t2.toJson());
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
        tournamentDao.getTournaments().forEach((id, t) -> tournaments.put(id, t.getName()));
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
        return tournamentDao.create(tournament);
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
        return tournamentDao.getIds();
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
        Optional<Tournament> optTournament = tournamentDao.getTournament(id);
        if (optTournament.isPresent())
            return optTournament.get();
        throw new NotFoundException();
    }
}