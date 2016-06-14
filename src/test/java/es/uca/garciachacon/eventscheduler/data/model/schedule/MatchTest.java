package es.uca.garciachacon.eventscheduler.data.model.schedule;

import es.uca.garciachacon.eventscheduler.data.model.tournament.event.domain.Localization;
import es.uca.garciachacon.eventscheduler.data.model.tournament.event.domain.Player;
import es.uca.garciachacon.eventscheduler.data.model.tournament.event.domain.Team;
import es.uca.garciachacon.eventscheduler.data.model.tournament.event.domain.Timeslot;
import es.uca.garciachacon.eventscheduler.utils.TournamentUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests de la clase {@link Match}.
 */
public class MatchTest {
    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    private Match match;
    private List<Player> players;
    private Localization localization;
    private Timeslot start;
    private Timeslot end;

    @Before
    public void setUp() {
        players = TournamentUtils.buildGenericPlayers(2, "Player");
        localization = new Localization("Court 1");
        start = new Timeslot(1, LocalTime.of(15, 0), Duration.ofHours(1));
        end = new Timeslot(1, LocalTime.of(17, 0), Duration.ofHours(1));

        match = new Match(players, localization, start, end, 2);
    }

    @Test
    public void constructorTest() {
        assertEquals(2, match.getPlayers().size());
        assertEquals(localization, match.getLocalization());
        assertEquals(start, match.getStartTimeslot());
        assertEquals(end, match.getEndTimeslot());
        assertEquals(2, match.getDuration());
        assertTrue(match.getTeams().isEmpty());

        players = TournamentUtils.buildGenericPlayers(4, "Player");
        Team team1 = new Team(players.get(0), players.get(2));
        Team team2 = new Team(players.get(3), players.get(1));

        match = new Match(players, localization, start, end, 1);
        match.setTeams(new ArrayList<>(Arrays.asList(team1, team2)));

        assertEquals(2, match.getTeams().size());
    }

    @Test
    public void constructorNullPlayersTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("parameters cannot be null");
        new Match(null, localization, start, end, 1);
    }

    @Test
    public void constructorNullLocalizationTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("parameters cannot be null");
        new Match(players, null, start, end, 1);
    }

    @Test
    public void constructorNullStartTimeslotTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("parameters cannot be null");
        new Match(players, localization, null, end, 1);
    }

    @Test
    public void constructorNullEndTimeslotTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("parameters cannot be null");
        new Match(players, localization, start, null, 1);
    }

    @Test
    public void constructorPlayersEmptyTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Players cannot be empty");
        new Match(new ArrayList<>(), localization, start, end, 1);
    }

    @Test
    public void constructorDurationLessThanOneTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("duration of the match cannot amount to a timeslot span lesser than 1");
        new Match(players, localization, start, end, 0);
    }

    @Test
    public void constructorInvalidTimeslotOrderTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("end timeslot cannot precede the start timeslot");
        new Match(players, localization, end, start, 1);
    }

    @Test
    public void constructorInvalidDurationTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage(
                "duration of the match cannot amount to a timeslot span greater than 1 if the start timeslot is the " +
                        "same than the end timeslot");
        new Match(players, localization, start, start, 2);
    }

    @Test
    public void constructorDuplicatedPlayersTest() {
        Player player = new Player("Player");
        players = new ArrayList<>(Arrays.asList(player, player));

        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Players cannot be duplicated");
        new Match(players, localization, start, end, 2);
    }

    @Test
    public void setTeamsTest() {
        players = TournamentUtils.buildGenericPlayers(4, "Player");
        match = new Match(players, localization, start, end, 2);
        match.setTeams(new ArrayList<>(Arrays.asList(new Team(players.get(0), players.get(1)),
                new Team(players.get(2), players.get(3))
        )));

        assertEquals(2, match.getTeams().size());
    }

    @Test
    public void setTeamsNullTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Teams cannot be null");
        match.setTeams(null);
    }

    @Test
    public void setTeamEmtpyTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("List of teams cannot be empty");
        match.setTeams(new ArrayList<>());
    }

    @Test
    public void setTeamsNullTeamTest() {
        List<Team> teams = new ArrayList<>();
        teams.add(null);
        teams.add(new Team(players.get(0), players.get(1)));

        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Teams cannot contain a null team");
        match.setTeams(teams);
    }

    @Test
    public void setTeamsDuplicatedTeamsTest() {
        Team team = new Team(players.get(0), players.get(1));
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Teams cannot be duplicated");
        match.setTeams(new ArrayList<>(Arrays.asList(team, team)));
    }

    @Test
    public void setTeamsDifferentNumberOfPlayersTest() {
        players = TournamentUtils.buildGenericPlayers(4, "Player");
        match = new Match(players, localization, start, end, 2);

        Team team1 = new Team(players.get(0), players.get(3), players.get(2), players.get(1));
        Team team2 = new Team(players.get(1), players.get(2));

        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Teams cannot have different number of players");
        match.setTeams(new ArrayList<>(Arrays.asList(team1, team2)));
    }

    @Test
    public void setTeamsDivisorTest() {
        players = TournamentUtils.buildGenericPlayers(4, "Player");
        match = new Match(players, localization, start, end, 2);

        Team team1 = new Team(players.get(0), players.get(3), players.get(2));
        Team team2 = new Team(players.get(1), players.get(2), players.get(3));

        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("must be a divisor of the number of players of the match");
        match.setTeams(new ArrayList<>(Arrays.asList(team1, team2)));
    }


    @Test
    public void setTeamsNonexistingPlayerTest() {
        Team team1 = new Team(players.get(0), new Player("Unknown Player"));
        Team team2 = new Team(players.get(1), players.get(0));

        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("All players in a team must be contained in the list of players of this match");
        match.setTeams(new ArrayList<>(Arrays.asList(team1, team2)));
    }

    @Test
    public void getPlayersTest() {
        expectedEx.expect(UnsupportedOperationException.class);
        match.getPlayers().clear();
    }

    @Test
    public void getTeamsTest() {
        expectedEx.expect(UnsupportedOperationException.class);
        match.getTeams().add(new Team(players.get(0), players.get(1)));
    }

    @Test
    public void duringTest() {
        start = new Timeslot(1, LocalTime.of(10, 0), Duration.ofHours(1));
        end = new Timeslot(1, LocalTime.of(14, 0), Duration.ofHours(1));
        // La hora del partido es 10:00 - 14:00 (4 timeslots)
        match = new Match(players, localization, start, end, 4);

        // Dentro del rango del partido
        assertTrue(match.during(new Timeslot(1, LocalTime.of(13, 0), Duration.ofHours(1))));

        // Dentro del rango del partido; mismo timeslot de comienzo
        assertTrue(match.during(new Timeslot(1, LocalTime.of(10, 0), Duration.ofHours(1))));

        // Dentro del rango del partido; mismo timeslot de fin
        assertTrue(match.during(new Timeslot(1, LocalTime.of(14, 0), Duration.ofHours(1))));

        // Fuera del rango
        assertFalse(match.during(new Timeslot(1, LocalTime.of(9, 0), Duration.ofHours(1))));

        // Fuera del rango
        assertFalse(match.during(new Timeslot(1, LocalTime.of(15, 0), Duration.ofHours(1))));

        // Mismo rango
        assertTrue(match.during(start, end));
        assertTrue(match.during(end, start));

        // Rango incluido en el partido
        Timeslot t1 = new Timeslot(1, LocalTime.of(11, 0), Duration.ofHours(1));
        Timeslot t2 = new Timeslot(1, LocalTime.of(13, 0), Duration.ofHours(1));
        assertTrue(match.during(t1, t2));
        assertTrue(match.during(t2, t1));

        // Rango incluido en el partido; mismo timeslot de comienzo
        t1 = new Timeslot(1, LocalTime.of(10, 0), Duration.ofHours(1));
        t2 = new Timeslot(1, LocalTime.of(13, 0), Duration.ofHours(1));
        assertTrue(match.during(t1, t2));
        assertTrue(match.during(t2, t1));

        // Rango incluido en el partido; mismo timeslot de fin
        t1 = new Timeslot(1, LocalTime.of(12, 0), Duration.ofHours(1));
        t2 = new Timeslot(1, LocalTime.of(14, 0), Duration.ofHours(1));
        assertTrue(match.during(t1, t2));
        assertTrue(match.during(t2, t1));

        // Partido incluido en el rango
        t1 = new Timeslot(1, LocalTime.of(9, 0), Duration.ofHours(1));
        t2 = new Timeslot(1, LocalTime.of(16, 0), Duration.ofHours(1));
        assertTrue(match.during(t1, t2));
        assertTrue(match.during(t2, t1));

        // Partido incluido en el rango por el comienzo
        t1 = new Timeslot(1, LocalTime.of(9, 0), Duration.ofHours(1));
        t2 = new Timeslot(1, LocalTime.of(12, 0), Duration.ofHours(1));
        assertTrue(match.during(t1, t2));
        assertTrue(match.during(t2, t1));

        // Partido incluido en el rango por el final
        t1 = new Timeslot(1, LocalTime.of(13, 0), Duration.ofHours(1));
        t2 = new Timeslot(1, LocalTime.of(17, 0), Duration.ofHours(1));
        assertTrue(match.during(t1, t2));
        assertTrue(match.during(t2, t1));

        // Partido incluido en el rango; mismo timeslot de comienzo
        t1 = new Timeslot(1, LocalTime.of(10, 0), Duration.ofHours(1));
        t2 = new Timeslot(1, LocalTime.of(15, 0), Duration.ofHours(1));
        assertTrue(match.during(t1, t2));
        assertTrue(match.during(t2, t1));

        // Partido incluido en el rango; mismo timeslot de fin
        t1 = new Timeslot(1, LocalTime.of(9, 0), Duration.ofHours(1));
        t2 = new Timeslot(1, LocalTime.of(14, 0), Duration.ofHours(1));
        assertTrue(match.during(t1, t2));
        assertTrue(match.during(t2, t1));

        // Partido no incluido en el rango
        t1 = new Timeslot(1, LocalTime.of(6, 0), Duration.ofHours(1));
        t2 = new Timeslot(1, LocalTime.of(8, 0), Duration.ofHours(1));
        assertFalse(match.during(t1, t2));
        assertFalse(match.during(t2, t1));

        // Partido no incluido en el rango
        t1 = new Timeslot(1, LocalTime.of(15, 0), Duration.ofHours(1));
        t2 = new Timeslot(1, LocalTime.of(18, 0), Duration.ofHours(1));
        assertFalse(match.during(t1, t2));
        assertFalse(match.during(t2, t1));
    }

    @Test
    public void withinTest() {
        start = new Timeslot(1, LocalTime.of(10, 0), Duration.ofHours(1));
        end = new Timeslot(1, LocalTime.of(14, 0), Duration.ofHours(1));
        // La hora del partido es 10:00 - 14:00 (4 timeslots)
        match = new Match(players, localization, start, end, 4);

        // Mismo rango
        assertTrue(match.within(start, end));
        assertTrue(match.within(end, start));

        // Dentro del rango, holgado
        Timeslot t1 = new Timeslot(1, LocalTime.of(9, 0), Duration.ofHours(1));
        Timeslot t2 = new Timeslot(1, LocalTime.of(15, 0), Duration.ofHours(1));
        assertTrue(match.within(t1, t2));

        // Dentro del rango, mismo comienzo
        t1 = new Timeslot(1, LocalTime.of(10, 0), Duration.ofHours(1));
        t2 = new Timeslot(1, LocalTime.of(15, 0), Duration.ofHours(1));
        assertTrue(match.within(t1, t2));

        // Dentro del rango, mismo final
        t1 = new Timeslot(1, LocalTime.of(6, 0), Duration.ofHours(1));
        t2 = new Timeslot(1, LocalTime.of(14, 0), Duration.ofHours(1));
        assertTrue(match.within(t1, t2));

        // "Fuera" del rango (rango incluido en el partido)
        t1 = new Timeslot(1, LocalTime.of(11, 0), Duration.ofHours(1));
        t2 = new Timeslot(1, LocalTime.of(13, 0), Duration.ofHours(1));
        assertFalse(match.within(t1, t2));
        assertFalse(match.within(t2, t1));

        // "Fuera" del rango (rango incluido en el partido); mismo comienzo
        t1 = new Timeslot(1, LocalTime.of(10, 0), Duration.ofHours(1));
        t2 = new Timeslot(1, LocalTime.of(13, 0), Duration.ofHours(1));
        assertFalse(match.within(t1, t2));

        // "Fuera" del rango (rango incluido en el partido); mismo final
        t1 = new Timeslot(1, LocalTime.of(12, 0), Duration.ofHours(1));
        t2 = new Timeslot(1, LocalTime.of(14, 0), Duration.ofHours(1));
        assertFalse(match.within(t1, t2));

        // Fuera del rango por la izquierda
        t1 = new Timeslot(1, LocalTime.of(7, 0), Duration.ofHours(1));
        t2 = new Timeslot(1, LocalTime.of(9, 0), Duration.ofHours(1));
        assertFalse(match.within(t1, t2));

        // Fuera del rango por la izquierda con timeslot coincidente (fin rango = comienzo partido)
        t1 = new Timeslot(1, LocalTime.of(7, 0), Duration.ofHours(1));
        t2 = new Timeslot(1, LocalTime.of(10, 0), Duration.ofHours(1));
        assertFalse(match.within(t1, t2));

        // Fuera del rango por la izquierda con solapamiento (parte final del rango se superpone con el parte del
        // comienzo del partido)
        t1 = new Timeslot(1, LocalTime.of(7, 0), Duration.ofHours(1));
        t2 = new Timeslot(1, LocalTime.of(12, 0), Duration.ofHours(1));
        assertFalse(match.within(t1, t2));

        // Fuera del rango por la derecha
        t1 = new Timeslot(1, LocalTime.of(15, 0), Duration.ofHours(1));
        t2 = new Timeslot(1, LocalTime.of(19, 0), Duration.ofHours(1));
        assertFalse(match.within(t1, t2));

        // Fuera del rango por la derecha con timeslot coincidente (fin timeslot = comienzo partido)
        t1 = new Timeslot(1, LocalTime.of(14, 0), Duration.ofHours(1));
        t2 = new Timeslot(1, LocalTime.of(17, 0), Duration.ofHours(1));
        assertFalse(match.within(t1, t2));

        // Fuera del rango por la izquierda con solapamiento (del comienzo del rango se superpone con parte del final
        // del partido)
        t1 = new Timeslot(1, LocalTime.of(13, 0), Duration.ofHours(1));
        t2 = new Timeslot(1, LocalTime.of(17, 0), Duration.ofHours(1));
        assertFalse(match.within(t1, t2));
    }

    @Test
    public void toStringTest() {
        assertEquals("At Timeslot [order=1, start=15:00, duration=PT1H] in Court 1: Player 1 vs Player 2",
                match.toString()
        );

        players = TournamentUtils.buildGenericPlayers(4, "Player");
        match = new Match(players, localization, start, end, 2);
        match.setTeams(new ArrayList<>(Arrays.asList(new Team(players.get(0), players.get(1)),
                new Team(players.get(2), players.get(3))
        )));

        assertEquals(
                "At Timeslot [order=1, start=15:00, duration=PT1H] in Court 1: Player 1-Player 2 vs Player 3-Player 4",
                match.toString()
        );
    }
}
