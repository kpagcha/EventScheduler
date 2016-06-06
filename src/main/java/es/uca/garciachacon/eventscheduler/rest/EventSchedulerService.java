package es.uca.garciachacon.eventscheduler.rest;

import es.uca.garciachacon.eventscheduler.data.model.tournament.Tournament;
import es.uca.garciachacon.eventscheduler.data.model.tournament.event.Event;
import es.uca.garciachacon.eventscheduler.data.model.tournament.event.domain.Player;
import es.uca.garciachacon.eventscheduler.rest.dao.ITournamentDao;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("eventscheduler")
public class EventSchedulerService {
    private ITournamentDao tournamentDao;

    @Inject
    public EventSchedulerService(ITournamentDao tournamentDao) {
        this.tournamentDao = tournamentDao;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Tournament createTournament(Tournament tournament) {
        return tournamentDao.create(tournament) ? tournament : null;
    }

    @Path("event")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Event createEvent(Event event) {
        return event;
    }
}