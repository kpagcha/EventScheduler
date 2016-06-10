package es.uca.garciachacon.eventscheduler.examples.rest;

import es.uca.garciachacon.eventscheduler.rest.dao.ITournamentDao;
import es.uca.garciachacon.eventscheduler.rest.dao.TournamentDao;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import javax.inject.Singleton;

public class Binder extends AbstractBinder {
    @Override
    protected void configure() {
        bind(TournamentDao.class).to(ITournamentDao.class).in(Singleton.class);
    }
}
