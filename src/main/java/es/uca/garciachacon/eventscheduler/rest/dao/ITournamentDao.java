package es.uca.garciachacon.eventscheduler.rest.dao;

import es.uca.garciachacon.eventscheduler.data.model.tournament.Tournament;

public interface ITournamentDao {
    boolean create(Tournament tournament);
}
