package es.uca.garciachacon.eventscheduler.rest.dao;

import com.google.common.collect.BiMap;
import es.uca.garciachacon.eventscheduler.data.model.tournament.Tournament;

import java.util.Optional;
import java.util.Set;

public interface ITournamentDao {
    BiMap<String, Tournament> getTournaments();

    String create(Tournament tournament);

    Optional<String> getTournamentId(Tournament tournament);

    Optional<Tournament> getTournament(String id);

    Set<String> getIds();
}
