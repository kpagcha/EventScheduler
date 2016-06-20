package es.uca.garciachacon.eventscheduler.rest;

import es.uca.garciachacon.eventscheduler.data.model.tournament.Tournament;
import es.uca.garciachacon.eventscheduler.rest.dao.ITournamentDao;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("eventscheduler")
public class EventSchedulerService {
    private ITournamentDao tournamentDao;

    @Inject
    public EventSchedulerService(ITournamentDao tournamentDao) {
        this.tournamentDao = tournamentDao;
    }

    @Path("tournament")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Tournament createTournament(Tournament tournament) {
        tournamentDao.create(tournament);
        return tournament;
        //return tournamentDao.create(tournament) ? tournament : null;
    }
}