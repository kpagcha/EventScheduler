package es.uca.garciachacon.eventscheduler.rest.dao;

import es.uca.garciachacon.eventscheduler.data.model.tournament.Tournament;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TournamentDao implements ITournamentDao {
    private Map<String, Tournament> tournaments = new ConcurrentHashMap<>();

    @Override
    public synchronized boolean create(Tournament tournament) {
        if (tournaments.containsKey(tournament.getName()))
            return false;
        tournaments.put(tournament.getName(), tournament);
        return true;
    }
}
