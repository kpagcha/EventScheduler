package es.uca.garciachacon.eventscheduler.rest.dao;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import es.uca.garciachacon.eventscheduler.data.model.tournament.Tournament;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.util.UUID.randomUUID;

/**
 * Implementación básica del DAO para manipular torneos, que serían los recursos que manejase el servidor web.
 * <p>
 * El almacenamiento de los recursos es la memoria del servidor, que persiste en un diccionario bidireccional
 * <i>thread-safe</i> todos los torneos creados y un identificador único asociado generado automáticamente que sirve
 * como referencia para operaciones futuras sobre el torneo.
 * <p>
 * Es posible activar un período de expiración que limitará la duración de cada torneo creado, pues serán eliminados
 * una vez superado tiempo especificado.
 */
public class TournamentDao implements ITournamentDao {
    private BiMap<String, Tournament> tournaments = Maps.synchronizedBiMap(HashBiMap.create());

    private long delay = 5000;
    private boolean expire = false;

    public BiMap<String, Tournament> getTournaments() {
        return tournaments;
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public boolean getExpire() {
        return expire;
    }

    public void setExpire(boolean expire) {
        this.expire = expire;
    }

    public synchronized String create(Tournament tournament) {
        String id = randomUUID().toString();
        tournaments.put(id, tournament);

        if (expire)
            Executors.newScheduledThreadPool(1).schedule(() -> tournaments.remove(id), delay, TimeUnit.MILLISECONDS);

        return id;
    }

    public synchronized Optional<String> getTournamentId(Tournament tournament) {
        BiMap<Tournament, String> inverseTournaments = tournaments.inverse();
        if (inverseTournaments.containsKey(tournament))
            return Optional.of(inverseTournaments.get(tournament));
        return Optional.empty();
    }

    public synchronized Optional<Tournament> getTournament(String id) {
        if (tournaments.containsKey(id))
            return Optional.of(tournaments.get(id));
        return Optional.empty();
    }

    public synchronized Set<String> getIds() {
        return tournaments.keySet();
    }
}
