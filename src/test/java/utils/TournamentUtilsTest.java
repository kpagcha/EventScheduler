package utils;

import data.model.tournament.event.domain.Localization;
import data.model.tournament.event.domain.Player;
import data.model.tournament.event.domain.timeslot.DefiniteTimeslot;
import data.model.tournament.event.domain.timeslot.Timeslot;
import data.model.tournament.event.domain.timeslot.UndefiniteTimeslot;
import org.junit.Test;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests de la clase {@link TournamentUtils}.
 */
public class TournamentUtilsTest {
    @Test
    public void buildPlayersTest() {
        List<Player> players = TournamentUtils.buildPlayers(new String[]{ "Nadal", "Federer", "Djokovic" });
        assertEquals(3, players.size());
        assertEquals(1, players.stream().filter(p -> p.getName().equals("Nadal")).count());
        assertEquals(1, players.stream().filter(p -> p.getName().equals("Federer")).count());
        assertEquals(1, players.stream().filter(p -> p.getName().equals("Djokovic")).count());
        assertEquals(0, players.stream().filter(p -> p.getName().equals("Murray")).count());

        players = TournamentUtils.buildPlayers(null);
        assertTrue(players.isEmpty());

        players = TournamentUtils.buildPlayers(new String[]{});
        assertTrue(players.isEmpty());
    }

    @Test
    public void buildGenericPlayersTest() {
        List<Player> players = TournamentUtils.buildGenericPlayers(32, "Pl");
        assertEquals(32, players.size());
        for (int i = 0; i < players.size(); i++) {
            final String name = "Pl " + (i + 1);
            assertEquals(1, players.stream().filter(p -> p.getName().equals(name)).count());
        }

        players = TournamentUtils.buildGenericPlayers(6, "");
        assertEquals(6, players.size());
        for (int i = 0; i < players.size(); i++) {
            final String name = "Player " + (i + 1);
            assertEquals(1, players.stream().filter(p -> p.getName().equals(name)).count());
        }

        players = TournamentUtils.buildGenericPlayers(8, null);
        assertEquals(8, players.size());
        for (int i = 0; i < players.size(); i++) {
            final String name = "Player " + (i + 1);
            assertEquals(1, players.stream().filter(p -> p.getName().equals(name)).count());
        }

        players = TournamentUtils.buildGenericPlayers(-1, "Player");
        assertTrue(players.isEmpty());
    }

    @Test
    public void buildGenericLocalizationsTest() {
        List<Localization> localizations = TournamentUtils.buildGenericLocalizations(3, "Field");
        assertEquals(3, localizations.size());
        for (int i = 0; i < localizations.size(); i++) {
            final String name = "Field " + (i + 1);
            assertEquals(1, localizations.stream().filter(l -> l.getName().equals(name)).count());
        }

        localizations = TournamentUtils.buildGenericLocalizations(5, "");
        assertEquals(5, localizations.size());
        for (int i = 0; i < localizations.size(); i++) {
            final String name = "Court " + (i + 1);
            assertEquals(1, localizations.stream().filter(l -> l.getName().equals(name)).count());
        }

        localizations = TournamentUtils.buildGenericLocalizations(10, null);
        assertEquals(10, localizations.size());
        for (int i = 0; i < localizations.size(); i++) {
            final String name = "Court " + (i + 1);
            assertEquals(1, localizations.stream().filter(l -> l.getName().equals(name)).count());
        }

        localizations = TournamentUtils.buildGenericLocalizations(-3, "Field");
        assertEquals(0, localizations.size());
    }

    @Test
    public void buildAbstractTimeslotsTest() {
        List<Timeslot> timeslots = TournamentUtils.buildAbstractTimeslots(10);
        assertEquals(10, timeslots.size());
        for (int i = 0; i < timeslots.size(); i++)
            assertEquals(i, timeslots.get(i).getChronologicalOrder());

        timeslots = TournamentUtils.buildAbstractTimeslots(-1);
        assertTrue(timeslots.isEmpty());
    }

    @Test
    public void buildDefiniteDayOfWeekTimeslotsTest() {
        List<Timeslot> timeslots = TournamentUtils.buildDefiniteDayOfWeekTimeslots(14);
        assertEquals(14, timeslots.size());
        for (int i = 0; i < 7; i++)
            assertEquals(0, timeslots.get(i).getChronologicalOrder());
        for (int i = 7; i < 14; i++)
            assertEquals(1, timeslots.get(i).getChronologicalOrder());

        assertTrue(((DefiniteTimeslot) timeslots.get(0)).getStart().equals(DayOfWeek.MONDAY));
        assertTrue(((DefiniteTimeslot) timeslots.get(4)).getStart().equals(DayOfWeek.FRIDAY));
        assertTrue(((DefiniteTimeslot) timeslots.get(7)).getStart().equals(DayOfWeek.MONDAY));

        assertTrue(((DefiniteTimeslot) timeslots.get(12)).getDuration().equals(Duration.ofHours(1)));

        timeslots = TournamentUtils.buildDefiniteDayOfWeekTimeslots(-5);
        assertTrue(timeslots.isEmpty());
    }

    @Test
    public void buildDefiniteLocalTimeTimeslotsTest() {
        List<Timeslot> timeslots = TournamentUtils.buildDefiniteLocalTimeTimeslots(56);
        assertEquals(56, timeslots.size());
        for (int i = 0; i < 24; i++) {
            Timeslot t = timeslots.get(i);
            System.out.println(t + " ==> " + t.getChronologicalOrder());
            assertEquals(0, t.getChronologicalOrder());
            assertEquals(LocalTime.of(i, 0), ((DefiniteTimeslot) t).getStart());
        }
        for (int i = 24; i < 48; i++) {
            Timeslot t = timeslots.get(i);
            assertEquals(1, t.getChronologicalOrder());
            assertEquals(LocalTime.of(i % 24, 0), ((DefiniteTimeslot) t).getStart());
        }
        for (int i = 48; i < 56; i++) {
            Timeslot t = timeslots.get(i);
            assertEquals(2, t.getChronologicalOrder());
            assertEquals(LocalTime.of(i % 24, 0), ((DefiniteTimeslot) t).getStart());
        }

        timeslots = TournamentUtils.buildDefiniteLocalTimeTimeslots(-1);
        assertTrue(timeslots.isEmpty());
    }

    @Test
    public void buildUndefiniteTimeslotsTest() {
        List<Timeslot> timeslots = TournamentUtils.buildUndefiniteTimeslots(12);
        assertEquals(12, timeslots.size());
        for (int i = 0; i < timeslots.size(); i++) {
            UndefiniteTimeslot t = (UndefiniteTimeslot) timeslots.get(i);
            assertEquals(i, t.getChronologicalOrder());
            assertEquals(Duration.ofHours(1), t.getDuration());
        }

        timeslots = TournamentUtils.buildUndefiniteTimeslots(0);
        assertTrue(timeslots.isEmpty());
    }
}
