package es.uca.garciachacon.eventscheduler.data.model.tournament;

import es.uca.garciachacon.eventscheduler.data.model.tournament.event.Event;
import es.uca.garciachacon.eventscheduler.data.model.tournament.event.Matchup;
import es.uca.garciachacon.eventscheduler.data.model.tournament.event.domain.Localization;
import es.uca.garciachacon.eventscheduler.data.model.tournament.event.domain.Player;
import es.uca.garciachacon.eventscheduler.data.model.tournament.event.domain.Team;
import es.uca.garciachacon.eventscheduler.data.model.tournament.event.domain.Timeslot;
import es.uca.garciachacon.eventscheduler.data.validation.validable.ValidationException;
import es.uca.garciachacon.eventscheduler.data.validation.validator.tournament.EventValidator;
import es.uca.garciachacon.eventscheduler.solver.TournamentSolver;
import es.uca.garciachacon.eventscheduler.utils.TournamentUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.time.Duration;
import java.time.LocalTime;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Tests de la clase {@link Event}.
 */
public class EventTest {
    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    private Event event;
    private List<Player> players;
    private List<Localization> localizations;
    private List<Timeslot> timeslots;
    private int nMatchesPerPlayer, nTimeslotsPerMatch, nPlayersPerMatch;

    @Before
    public void setUp() {
        players = TournamentUtils.buildGenericPlayers(8, "Player");
        localizations = TournamentUtils.buildGenericLocalizations(2, "Court");
        timeslots = TournamentUtils.buildOneHourTimeslots(8);

        event = new Event("Event", players, localizations, timeslots, 1, 2, 2);

        nMatchesPerPlayer = 1;
        nTimeslotsPerMatch = 2;
        nPlayersPerMatch = 2;
    }

    @Test
    public void constructorsTestSuccess() {
        try {
            event = new Event("Event", players, localizations, timeslots);
            assertEquals(8, event.getPlayers().size());
            assertEquals(2, event.getLocalizations().size());
            assertEquals(8, event.getTimeslots().size());

            players = TournamentUtils.buildGenericPlayers(24, "Player");
            localizations = TournamentUtils.buildGenericLocalizations(5, "Court");
            timeslots = TournamentUtils.buildSimpleTimeslots(16);

            event = new Event("Event", players, localizations, timeslots, 2, 3, 4);
            assertEquals(24, event.getPlayers().size());
            assertEquals(5, event.getLocalizations().size());
            assertEquals(16, event.getTimeslots().size());
            assertEquals(2, event.getMatchesPerPlayer());
            assertEquals(3, event.getTimeslotsPerMatch());
            assertEquals(4, event.getPlayersPerMatch());

        } catch (IllegalArgumentException e) {
            fail("Exception thrown for an event expected to be valid");
        }
    }

    @Test
    public void constructorNullNameTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("name cannot be null");
        event = new Event(null, players, localizations, timeslots);
    }

    @Test
    public void extendedConstructorNullNameTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("name cannot be null");
        event = new Event(null,
                players,
                localizations,
                timeslots,
                nMatchesPerPlayer,
                nTimeslotsPerMatch,
                nPlayersPerMatch
        );
    }

    @Test
    public void constructorNullPlayersTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("players cannot be null");
        event = new Event("Event", null, localizations, timeslots);
    }

    @Test
    public void constructorEmptyPlayersTest() {
        players.clear();

        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("cannot be less than 1");
        event = new Event("Event", players, localizations, timeslots);
    }

    @Test
    public void constructorContainedNullPlayerTest() {
        players.set(3, null);
        players.set(6, null);

        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("player cannot be null");
        event = new Event("Event", players, localizations, timeslots);
    }

    @Test
    public void constructorDuplicatedPlayersTest() {
        players.set(4, players.get(2));

        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("players must be unique");
        event = new Event("Event", players, localizations, timeslots);
    }

    /**
     * Test que comprueba que se lanza excpeción al usar el primer constructor de evento
     * ({@link Event#Event(String, List, List, List)},
     * cuando el número de jugadores no es múltiplo del número por defecto de jugadores por partido
     */
    @Test
    public void constructorDefaultPlayerPerMatchTest() {
        players.add(new Player("Extra Player"));

        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("must be a multiple of the number of players per match");
        event = new Event("Event", players, localizations, timeslots);
    }

    @Test
    public void constructorNullLocalizationsTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("localizations cannot be null");
        event = new Event("Event", players, null, timeslots);
    }

    @Test
    public void constructorEmptyLocalizationsTest() {
        localizations.clear();

        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("cannot be less than 1");
        event = new Event("Event", players, localizations, timeslots);
    }

    @Test
    public void constructorContainedNullLocalizationTest() {
        localizations.set(1, null);

        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("localization cannot be null");
        event = new Event("Event", players, localizations, timeslots);
    }

    @Test
    public void constructorDuplicatedLocalizationsTest() {
        localizations.add(localizations.get(0));

        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("localizations must be unique");
        event = new Event("Event", players, localizations, timeslots);
    }

    @Test
    public void constructorNullTimeslotsTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("timeslots cannot be null");
        event = new Event("Event", players, localizations, null);
    }

    @Test
    public void constructorEmptyTimeslotsTest() {
        timeslots.clear();

        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("cannot be less than 1");
        event = new Event("Event", players, localizations, timeslots);
    }

    @Test
    public void constructorContainedNullTimeslotTest() {
        timeslots.set(7, null);

        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("timeslot cannot be null");
        event = new Event("Event", players, localizations, timeslots);
    }

    @Test
    public void constructorNotEnoughTimeslotTest() {
        timeslots = TournamentUtils.buildDayOfWeekTimeslots(1);

        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage(
                "cannot be less than the product of the number of matches per player and the duration of a match");
        event = new Event("Event", players, localizations, timeslots);
    }

    @Test
    public void constructorDuplicatedTimeslotsTest() {
        timeslots.set(2, timeslots.get(7));
        timeslots.set(5, timeslots.get(7));

        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("timeslots must be unique");
        event = new Event("Event", players, localizations, timeslots);
    }

    @Test
    public void constructorDisorderedTimeslotsTest() {
        Timeslot tmp = timeslots.get(3);
        timeslots.set(3, timeslots.get(4));
        timeslots.set(4, tmp);

        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("must be greater than the next one");
        event = new Event("Event", players, localizations, timeslots);
    }

    @Test
    public void constructorPlayerPerMatchLessThanOneTest() {
        nPlayersPerMatch = 0;

        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("cannot be less than 1");
        event = new Event("Event",
                players,
                localizations,
                timeslots,
                nMatchesPerPlayer,
                nTimeslotsPerMatch,
                nPlayersPerMatch
        );
    }

    @Test
    public void constructorPlayerPerMatchDivisorOfNumberOfPlayersTest() {
        nPlayersPerMatch = 3;

        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("must be a multiple of the number of players per match");
        event = new Event("Event",
                players,
                localizations,
                timeslots,
                nMatchesPerPlayer,
                nTimeslotsPerMatch,
                nPlayersPerMatch
        );
    }

    @Test
    public void constructorMatchesPerPlayerLessThanOneTest() {
        nMatchesPerPlayer = 0;

        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("cannot be less than 1");
        event = new Event("Event",
                players,
                localizations,
                timeslots,
                nMatchesPerPlayer,
                nTimeslotsPerMatch,
                nPlayersPerMatch
        );
    }

    @Test
    public void constructorTimeslotsPerMatchLessThanOneTest() {
        nTimeslotsPerMatch = 0;

        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("cannot be less than 1");
        event = new Event("Event",
                players,
                localizations,
                timeslots,
                nMatchesPerPlayer,
                nTimeslotsPerMatch,
                nPlayersPerMatch
        );
    }

    @Test
    public void setNameTest() {
        event.setName("New Event");
        assertEquals("New Event", event.getName());
    }

    @Test
    public void setNullNameTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Name cannot be null");
        event.setName(null);
    }

    @Test
    public void getPlayersTest() {
        expectedEx.expect(UnsupportedOperationException.class);
        event.getPlayers().add(new Player("New Player"));
    }

    @Test
    public void getLocalizationsTest() {
        expectedEx.expect(UnsupportedOperationException.class);
        event.getLocalizations().add(new Localization("Court 10"));
    }

    @Test
    public void getTimeslotsTest() {
        expectedEx.expect(UnsupportedOperationException.class);
        event.getTimeslots().remove(3);
    }

    @Test
    public void setMatchesPerPlayerTest() {
        event.setMatchesPerPlayer(3);
        assertEquals(3, event.getMatchesPerPlayer());

        event.addMatchup(players.get(0), players.get(1));
        assertTrue(event.hasPredefinedMatchups());

        event.setMatchesPerPlayer(2);
        assertEquals(2, event.getMatchesPerPlayer());
        assertFalse(event.hasPredefinedMatchups());
    }

    @Test
    public void setValidatorTest() {
        event.setValidator(new EventValidator());
        try {
            event.validate();
        } catch (ValidationException e) {
            fail("Unexpected exception for valid event");
        }
    }

    @Test
    public void setNullValidatorTest() {
        expectedEx.expect(IllegalArgumentException.class);
        event.setValidator(null);
    }

    @Test
    public void getMessagesTest() {
        try {
            event.validate();
            assertEquals(0, event.getMessages().size());
        } catch (ValidationException e) {
            fail("Unexpected exception for valid event");
        }
    }

    @Test
    public void setMatchesPerPlayerLessThanOneTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("cannot be less than 1");
        event.setMatchesPerPlayer(0);
    }

    @Test
    public void setMatchesPerPlayerMoreThanTimeslotsTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage(
                "cannot be less than the product of the number of matches per player and the duration of a match");
        event.setMatchesPerPlayer(5);
    }

    @Test
    public void setTimeslotsPerMatchTest() {
        event.setTimeslotsPerMatch(4);
        assertEquals(4, event.getTimeslotsPerMatch());

        event.addMatchup(players.get(4), players.get(2));
        assertTrue(event.hasPredefinedMatchups());

        event.setTimeslotsPerMatch(2);
        assertEquals(2, event.getTimeslotsPerMatch());
        assertFalse(event.hasPredefinedMatchups());
    }

    @Test
    public void setTimeslotsPerMatchLessThanOneTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("cannot be less than 1");
        event.setTimeslotsPerMatch(0);
    }

    @Test
    public void setTimeslotsPerMatchMoreThanTimeslotsTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage(
                "cannot be less than the product of the number of matches per player and the duration of a match");
        event.setTimeslotsPerMatch(9);
    }

    @Test
    public void setPlayersPerMatchTest() {
        event.setPlayersPerMatch(4);
        assertEquals(4, event.getPlayersPerMatch());
        assertFalse(event.hasTeams());
        assertFalse(event.hasPredefinedMatchups());

        event.addTeam(players.get(0), players.get(1));
        event.addTeam(players.get(2), players.get(3));
        event.addMatchup(players.get(0), players.get(1), players.get(3), players.get(2));
        assertTrue(event.hasTeams());
        assertTrue(event.hasPredefinedMatchups());

        event.setPlayersPerMatch(2);
        assertFalse(event.hasTeams());
        assertFalse(event.hasPredefinedMatchups());
    }

    @Test
    public void setPlayersPerMatchLessThanOneTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("cannot be less than 1");
        event.setPlayersPerMatch(0);
    }

    @Test
    public void setPlayersPerMatchDivisorOfNumberOfPlayersTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("must be a multiple of the number of players per match");
        event.setPlayersPerMatch(3);
    }

    @Test
    public void setTeamsTest() {
        players = TournamentUtils.buildGenericPlayers(32, "Player");
        event = new Event("Doubles", players, localizations, timeslots, 1, 2, 4);

        assertFalse(event.hasTeams());

        List<Team> teams = new ArrayList<>(16);
        for (int i = 0; i < 32; i += 2) {
            Player[] teamPlayers = new Player[2];
            for (int j = 0; j < 2; j++)
                teamPlayers[j] = players.get(i + j);
            teams.add(new Team(teamPlayers));
        }

        event.setTeams(teams);

        assertTrue(event.hasTeams());
        assertEquals(16, event.getTeams().size());
        assertTrue(event.getTeams().get(8).contains(players.get(17)));
    }

    @Test
    public void setNullTeamsTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Teams cannot be null");
        event.setTeams(null);
    }

    @Test
    public void setTeamsNullTeamTest() {
        event.setPlayersPerMatch(4);
        List<Team> teams = new ArrayList<>();
        teams.add(new Team(players.get(0), players.get(1)));
        teams.add(null);

        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("team cannot be null");
        event.setTeams(teams);
    }

    @Test
    public void setTeamsEmptyTest() {
        event.setPlayersPerMatch(4);
        List<Team> teams = new ArrayList<>();

        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Teams cannot be empty");
        event.setTeams(teams);
    }

    @Test
    public void setTeamsUnexistingPlayerTest() {
        Player unknownPlayer = new Player("Unknown Player");
        event.setPlayersPerMatch(4);
        List<Team> teams = new ArrayList<>();
        teams.add(new Team(players.get(0), players.get(1)));
        teams.add(new Team(players.get(2), unknownPlayer));

        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("does not exist in the list of players of this event");
        event.setTeams(teams);
    }

    @Test
    public void setTeamsSameNumberOfPlayersTest() {
        event.setPlayersPerMatch(4);
        List<Team> teams = new ArrayList<>();
        teams.add(new Team(players.get(0), players.get(1)));
        teams.add(new Team(players.get(6), players.get(7), players.get(2)));

        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("teams must have the same number of players");
        event.setTeams(teams);
    }

    @Test
    public void setTeamsDivisorNumberOfPlayersPerMatchTest() {
        event.setPlayersPerMatch(4);
        List<Team> teams = new ArrayList<>();
        teams.add(new Team(players.get(0), players.get(3), players.get(1)));
        teams.add(new Team(players.get(5), players.get(4), players.get(2)));

        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("must be a divisor of the number of players per match");
        event.setTeams(teams);
    }

    @Test
    public void setTeamsDuplicatedPlayersTest() {
        event.setPlayersPerMatch(4);
        List<Team> teams = new ArrayList<>();
        teams.add(new Team(players.get(5), players.get(3)));
        teams.add(new Team(players.get(7), players.get(4)));
        teams.add(new Team(players.get(5), players.get(1)));

        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("can only be present in one team");
        event.setTeams(teams);
    }

    @Test
    public void getTeamsTest() {
        List<Team> teams = new ArrayList<>();
        teams.add(new Team(players.get(5), players.get(3)));
        teams.add(new Team(players.get(7), players.get(4)));
        event.setTeams(teams);

        expectedEx.expect(UnsupportedOperationException.class);
        event.getTeams().remove(0);
    }

    @Test
    public void addTeamTest() {
        event.setPlayersPerMatch(4);
        event.addTeam(players.get(0), players.get(1));
        event.addTeam(players.get(2), players.get(3));
        event.addTeam(players.get(4), players.get(5));

        assertTrue(event.hasTeams());
        assertEquals(3, event.getTeams().size());
        assertTrue(event.getTeams().get(1).contains(players.get(2)));

        event.addTeam(new Team(players.get(6), players.get(7)));
        assertEquals(4, event.getTeams().size());
    }

    @Test
    public void addTeamNullTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Team cannot be null");
        event.addTeam((Team) null);
    }

    @Test
    public void addTeamPlayersNullPlayersTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Players cannot be null");
        event.addTeam((Player[]) null);
    }

    @Test
    public void addTeamNullPlayersTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Players cannot be null");
        event.addTeam((Player[]) null);
    }

    @Test
    public void addTeamUnexistingPlayerTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("does not exist in the list of players of this event");
        event.addTeam(new Team(players.get(3), new Player("Unknown Player")));
    }

    @Test
    public void addTeamDivisorOfNumberOfPlayersTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("must be a divisor of the number of players per match");
        event.addTeam(new Team(players.get(3), players.get(4), players.get(7)));
    }

    @Test
    public void addTeamSameNumberOfPlayersTest() {
        event.setPlayersPerMatch(4);
        event.addTeam(players.get(0), players.get(3));

        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("must be the same");
        event.addTeam(players.get(2), players.get(4), players.get(6), players.get(7));
    }

    @Test
    public void addTeamPlayerOnlyInOneTeamTest() {
        event.setPlayersPerMatch(4);
        event.addTeam(players.get(0), players.get(3));

        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("can only be present in one team");
        event.addTeam(players.get(2), players.get(3));
    }

    @Test
    public void removeTeamTest() {
        event.addTeam(players.get(0), players.get(1));
        event.addTeam(players.get(2), players.get(3));
        event.addTeam(players.get(4), players.get(5));
        event.addTeam(players.get(6), players.get(7));

        Team team = event.getTeams().get(3);

        event.removeTeam(team);

        assertFalse(event.getTeams().contains(team));
        assertEquals(3, event.getTeams().size());
    }

    @Test
    public void hasTeamsTest() {
        assertFalse(event.hasTeams());

        event.addTeam(players.get(0), players.get(1));
        assertTrue(event.hasTeams());

        event.removeTeam(event.getTeams().get(0));
        assertFalse(event.hasTeams());
    }

    @Test
    public void clearTeamsTest() {
        assertFalse(event.hasTeams());

        event.addTeam(players.get(0), players.get(1));
        event.addTeam(players.get(2), players.get(3));
        assertTrue(event.hasTeams());

        event.clearTeams();
        assertFalse(event.hasTeams());
    }

    @Test
    public void setUnavailablePlayersTest() {
        Map<Player, Set<Timeslot>> unavailability = new HashMap<>();
        unavailability.put(players.get(3), new HashSet<>(Arrays.asList(timeslots.get(3), timeslots.get(4))));
        unavailability.put(players.get(5), new HashSet<>(Arrays.asList(timeslots.get(7))));
        unavailability.put(players.get(0),
                new HashSet<>(Arrays.asList(timeslots.get(4), timeslots.get(5), timeslots.get(6)))
        );

        event.setUnavailablePlayers(unavailability);

        assertTrue(event.hasUnavailablePlayers());
        assertEquals(3, event.getUnavailablePlayers().size());
        assertTrue(event.getUnavailablePlayers().containsKey(players.get(0)));
        assertEquals(2, event.getUnavailablePlayers().get(players.get(3)).size());
        assertTrue(event.getUnavailablePlayers().get(players.get(5)).contains(timeslots.get(7)));
    }

    @Test
    public void setUnavailablePlayersNullTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Map cannot be null");
        event.setUnavailablePlayers(null);
    }

    @Test
    public void setUnavailablePlayersNullPlayerTest() {
        Map<Player, Set<Timeslot>> unavailability = new HashMap<>();
        unavailability.put(null, new HashSet<>());

        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("player cannot be null");
        event.setUnavailablePlayers(unavailability);
    }

    @Test
    public void setUnavailablePlayersNonexistingPlayerTest() {
        Map<Player, Set<Timeslot>> unavailability = new HashMap<>();
        unavailability.put(new Player("Unknown Player"), new HashSet<>());

        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("does not exist in the list of players of this event");
        event.setUnavailablePlayers(unavailability);
    }

    @Test
    public void setUnavailablePlayersNullSetTest() {
        Map<Player, Set<Timeslot>> unavailability = new HashMap<>();
        unavailability.put(players.get(6), null);

        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("set of unavailable timeslots for a player cannot be null");
        event.setUnavailablePlayers(unavailability);
    }

    @Test
    public void setUnavailablePlayersEmptySetTest() {
        Map<Player, Set<Timeslot>> unavailability = new HashMap<>();
        unavailability.put(players.get(6), new HashSet<>());

        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("set of unavailable timeslots for a player cannot be empty");
        event.setUnavailablePlayers(unavailability);
    }

    @Test
    public void setUnavailablePlayersNullTimeslotTest() {
        Map<Player, Set<Timeslot>> unavailability = new HashMap<>();
        unavailability.put(players.get(6), new HashSet<>(Arrays.asList(timeslots.get(6), null)));

        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("timeslot cannot be null");
        event.setUnavailablePlayers(unavailability);
    }

    @Test
    public void setUnavailablePlayersNonexistingTimeslotTest() {
        Map<Player, Set<Timeslot>> unavailability = new HashMap<>();
        unavailability.put(players.get(6), new HashSet<>(Arrays.asList(timeslots.get(2), new Timeslot(1))));

        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("does not exist in this event");
        event.setUnavailablePlayers(unavailability);
    }

    @Test
    public void getUnavailablePlayersTest() {
        expectedEx.expect(UnsupportedOperationException.class);
        event.getUnavailablePlayers().clear();
    }

    @Test
    public void addUnavailablePlayerTest() {
        assertFalse(event.hasUnavailablePlayers());

        event.addUnavailablePlayerAtTimeslot(players.get(3), timeslots.get(5));

        assertTrue(event.hasUnavailablePlayers());
        assertEquals(1, event.getUnavailablePlayers().size());
        assertEquals(1, event.getUnavailablePlayers().get(players.get(3)).size());
        Assert.assertEquals(timeslots.get(5), event.getUnavailablePlayers().get(players.get(3)).iterator().next());

        event.addUnavailablePlayerAtTimeslot(players.get(3), timeslots.get(6));
        assertEquals(1, event.getUnavailablePlayers().size());
        assertEquals(2, event.getUnavailablePlayers().get(players.get(3)).size());
        assertTrue(event.getUnavailablePlayers().get(players.get(3)).contains(timeslots.get(6)));
    }

    @Test
    public void addUnavailablePlayerNullPlayerTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("parameters cannot be null");
        event.addUnavailablePlayerAtTimeslot(null, timeslots.get(5));
    }

    @Test
    public void addUnavailablePlayerNullTimeslotTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("parameters cannot be null");
        event.addUnavailablePlayerAtTimeslot(players.get(4), null);
    }

    @Test
    public void addUnavailablePlayerNonexistingPlayerTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("does not exist in the list of players of the event");
        event.addUnavailablePlayerAtTimeslot(new Player("Unknown Player"), timeslots.get(5));
    }

    @Test
    public void addUnavailablePlayerNonexistingTimeslotTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("doest not exist in the list of timeslots of the event");
        event.addUnavailablePlayerAtTimeslot(players.get(3),
                new Timeslot(1, LocalTime.of(10, 0), Duration.ofHours(2))
        );
    }

    @Test
    public void addUnavailablePlayerAtTimeslotsTest() {
        assertFalse(event.hasUnavailablePlayers());

        event.addUnavailablePlayerAtTimeslots(players.get(6),
                new HashSet<>(Arrays.asList(timeslots.get(3), timeslots.get(4)))
        );

        assertTrue(event.hasUnavailablePlayers());
        assertEquals(1, event.getUnavailablePlayers().size());
        assertEquals(2, event.getUnavailablePlayers().get(players.get(6)).size());

        event.addUnavailablePlayerAtTimeslots(players.get(0),
                new HashSet<>(Arrays.asList(timeslots.get(3), timeslots.get(4), timeslots.get(6), timeslots.get(7)))
        );
        assertEquals(2, event.getUnavailablePlayers().size());
        assertEquals(4, event.getUnavailablePlayers().get(players.get(0)).size());
        assertTrue(event.getUnavailablePlayers().get(players.get(0)).contains(timeslots.get(4)));
    }

    @Test
    public void addUnavailablePlayerAtTimeslotsNullPlayerTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("parameters cannot be null");
        event.addUnavailablePlayerAtTimeslots(null, new HashSet<>(Arrays.asList(timeslots.get(3), timeslots.get(4))));
    }

    @Test
    public void addUnavailablePlayerAtTimeslotsNullSetTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("parameters cannot be null");
        event.addUnavailablePlayerAtTimeslots(players.get(2), null);
    }

    @Test
    public void addUnavailablePlayerAtTimeslotRangeTest() {
        event.addUnavailablePlayerAtTimeslotRange(players.get(2), timeslots.get(0), timeslots.get(4));

        assertTrue(event.hasUnavailablePlayers());

        Map<Player, Set<Timeslot>> unavailable = event.getUnavailablePlayers();
        assertEquals(1, unavailable.size());

        Set<Timeslot> playerUnavailable = unavailable.get(players.get(2));
        assertEquals(5, playerUnavailable.size());
        for (int t = 0; t <= 4; t++)
            assertTrue(playerUnavailable.contains(timeslots.get(t)));

        event.addUnavailablePlayerAtTimeslotRange(players.get(6), timeslots.get(6), timeslots.get(3));
        assertEquals(2, unavailable.size());

        playerUnavailable = unavailable.get(players.get(6));
        assertEquals(4, playerUnavailable.size());
        for (int t = 3; t <= 6; t++)
            assertTrue(playerUnavailable.contains(timeslots.get(t)));
    }

    @Test
    public void addUnavailablePlayerAtTimeslotRangeNullPlayerTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("parameters cannot be null");
        event.addUnavailablePlayerAtTimeslotRange(null, timeslots.get(0), timeslots.get(4));
    }

    @Test
    public void addUnavailablePlayerAtTimeslotRangeNullTimeslot1Test() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("parameters cannot be null");
        event.addUnavailablePlayerAtTimeslotRange(players.get(2), null, timeslots.get(4));
    }

    @Test
    public void addUnavailablePlayerAtTimeslotRangeNullTimeslot2Test() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("parameters cannot be null");
        event.addUnavailablePlayerAtTimeslotRange(players.get(2), timeslots.get(0), null);
    }

    @Test
    public void addUnavailablePlayerAtTimeslotRangeNonexistingPlayerTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("does not exist in the list of players");
        event.addUnavailablePlayerAtTimeslotRange(new Player("Unknown Player"), timeslots.get(0), timeslots.get(4));
    }

    @Test
    public void addUnavailablePlayerAtTimeslotRangeNonexistingT1Test() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("does not exist in the list of timeslots");
        event.addUnavailablePlayerAtTimeslotRange(players.get(2), new Timeslot(3), timeslots.get(4));
    }

    @Test
    public void addUnavailablePlayerAtTimeslotRangeNonexistingT2Test() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("does not exist in the list of timeslots");
        event.addUnavailablePlayerAtTimeslotRange(players.get(2), timeslots.get(0), new Timeslot(3));
    }

    @Test
    public void removeUnavailablePlayerTest() {
        assertFalse(event.hasUnavailablePlayers());

        event.addUnavailablePlayerAtTimeslot(players.get(3), timeslots.get(5));
        event.addUnavailablePlayerAtTimeslot(players.get(3), timeslots.get(6));

        assertTrue(event.hasUnavailablePlayers());
        assertEquals(2, event.getUnavailablePlayers().get(players.get(3)).size());

        event.removeUnavailablePlayerAtTimeslot(players.get(3), timeslots.get(5));

        assertEquals(1, event.getUnavailablePlayers().get(players.get(3)).size());

        event.removeUnavailablePlayerAtTimeslot(players.get(3), timeslots.get(6));

        assertFalse(event.hasUnavailablePlayers());
    }

    @Test
    public void removeUnavailableNullPlayerTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("parameters cannot be null");
        event.removeUnavailablePlayerAtTimeslot(null, timeslots.get(3));
    }

    @Test
    public void removeUnavailablePlayerNullTimeslotsTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("parameters cannot be null");
        event.removeUnavailablePlayerAtTimeslot(players.get(2), null);
    }

    @Test
    public void removeUnavailableNonexistingPlayerTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("does not exist in the list of players of the event");
        event.removeUnavailablePlayerAtTimeslot(new Player("Unknown Player"), timeslots.get(6));
    }

    @Test
    public void removeUnavailablePlayerNonexistingTimeslotTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("does not exist in the list of timeslots of the event");
        event.removeUnavailablePlayerAtTimeslot(players.get(5), new Timeslot(3));
    }

    @Test
    public void clearUnavailablePlayersTest() {
        assertFalse(event.hasUnavailablePlayers());

        event.addUnavailablePlayerAtTimeslot(players.get(3), timeslots.get(5));
        event.addUnavailablePlayerAtTimeslot(players.get(3), timeslots.get(6));

        assertTrue(event.hasUnavailablePlayers());

        event.clearUnavailablePlayers();
        assertFalse(event.hasUnavailablePlayers());
    }

    @Test
    public void hasUnavailablePlayersTest() {
        assertFalse(event.hasUnavailablePlayers());

        event.addUnavailablePlayerAtTimeslot(players.get(3), timeslots.get(5));

        assertTrue(event.hasUnavailablePlayers());

        event.removeUnavailablePlayerAtTimeslot(players.get(3), timeslots.get(5));

        assertFalse(event.hasUnavailablePlayers());
    }

    @Test
    public void setPredefinedMatchupsTest() {
        assertFalse(event.hasPredefinedMatchups());

        Set<Matchup> predefinedMatchups = new HashSet<>();

        predefinedMatchups.add(new Matchup(event,
                new HashSet<>(Arrays.asList(players.get(0), players.get(1))),
                new HashSet<>(event.getLocalizations()),
                new HashSet<>(event.getTimeslots()),
                1
        ));
        predefinedMatchups.add(new Matchup(event,
                new HashSet<>(Arrays.asList(players.get(4), players.get(7))),
                new HashSet<>(event.getLocalizations()),
                new HashSet<>(event.getTimeslots()),
                1
        ));
        predefinedMatchups.add(new Matchup(event,
                new HashSet<>(Arrays.asList(players.get(6), players.get(3))),
                new HashSet<>(event.getLocalizations()),
                new HashSet<>(event.getTimeslots()),
                1
        ));

        event.setPredefinedMatchups(predefinedMatchups);

        assertTrue(event.hasPredefinedMatchups());
        assertEquals(3, event.getPredefinedMatchups().size());
        assertEquals(1,
                event.getPredefinedMatchups().stream().filter(m -> m.getPlayers().contains(players.get(1))).count()
        );
        for (Matchup matchup : event.getPredefinedMatchups())
            assertEquals(2, matchup.getPlayers().size());

        Map<Player, Set<Localization>> playersInLocalizations = event.getPlayersInLocalizations();
        Map<Player, Set<Timeslot>> playersAtTimeslots = event.getPlayersAtTimeslots();
        int nLocalizations = event.getLocalizations().size();
        int nTimeslots = event.getTimeslots().size();

        assertEquals(6, playersInLocalizations.size());
        assertEquals(6, playersAtTimeslots.size());
        for (int i : new int[]{ 0, 1, 4, 7, 6, 3 }) {
            Player player = players.get(i);
            assertEquals(nLocalizations, playersInLocalizations.get(player).size());
            assertEquals(nTimeslots - 1, playersAtTimeslots.get(player).size());
        }

        event.clearPredefinedMatchups();
        event.clearPlayersInLocalizations();
        event.clearPlayersAtTimeslots();
        event.setMatchesPerPlayer(2);

        assertFalse(event.hasPredefinedMatchups());
        assertFalse(event.hasPlayersInLocalizations());
        assertFalse(event.hasPlayersAtTimeslots());

        event.addPlayerInLocalization(players.get(2), localizations.get(0));
        event.addPlayerInLocalization(players.get(3), localizations.get(1));
        event.addPlayerAtTimeslotRange(players.get(4), timeslots.get(3), timeslots.get(6));
        event.addPlayerAtTimeslotRange(players.get(5), timeslots.get(0), timeslots.get(2));
        event.addPlayerAtTimeslot(players.get(6), timeslots.get(4));
        event.addPlayerAtTimeslotRange(players.get(7), timeslots.get(0), timeslots.get(3));
        event.setPredefinedMatchups(new HashSet<>(Arrays.asList(new Matchup(event,
                new HashSet<>(Arrays.asList(players.get(0), players.get(1))),
                new HashSet<>(Arrays.asList(localizations.get(0))),
                new HashSet<>(Arrays.asList(timeslots.get(3))),
                1
        ), new Matchup(event,
                new HashSet<>(Arrays.asList(players.get(2), players.get(3))),
                new HashSet<>(Arrays.asList(localizations.get(1))),
                new HashSet<>(Arrays.asList(timeslots.get(2), timeslots.get(6))),
                1
        ), new Matchup(event,
                new HashSet<>(Arrays.asList(players.get(4), players.get(5))),
                new HashSet<>(Arrays.asList(localizations.get(0))),
                new HashSet<>(Arrays.asList(timeslots.get(0))),
                1
        ), new Matchup(event,
                new HashSet<>(Arrays.asList(players.get(6), players.get(7))),
                new HashSet<>(Arrays.asList(localizations.get(1), localizations.get(0))),
                new HashSet<>(Arrays.asList(timeslots.get(5))),
                1
        ))));

        assertEquals(8, event.getPlayersInLocalizations().size());
        assertEquals(8, event.getPlayersAtTimeslots().size());
        assertEquals(4, event.getPredefinedMatchups().size());

        assertEquals(event.getLocalizations().size(), event.getPlayersInLocalizations().get(players.get(0)).size());
        assertEquals(event.getLocalizations().size(), event.getPlayersInLocalizations().get(players.get(1)).size());
        assertEquals(2, event.getPlayersInLocalizations().get(players.get(2)).size());
        assertEquals(1, event.getPlayersInLocalizations().get(players.get(3)).size());
        assertEquals(event.getLocalizations().size(), event.getPlayersInLocalizations().get(players.get(4)).size());
        assertEquals(event.getLocalizations().size(), event.getPlayersInLocalizations().get(players.get(5)).size());
        assertEquals(2, event.getPlayersInLocalizations().get(players.get(6)).size());
        assertEquals(2, event.getPlayersInLocalizations().get(players.get(7)).size());

        assertEquals(event.getTimeslots().size() - 1, event.getPlayersAtTimeslots().get(players.get(0)).size());
        assertEquals(event.getTimeslots().size() - 1, event.getPlayersAtTimeslots().get(players.get(1)).size());
        assertEquals(event.getTimeslots().size() - 1, event.getPlayersAtTimeslots().get(players.get(2)).size());
        assertEquals(event.getTimeslots().size() - 1, event.getPlayersAtTimeslots().get(players.get(3)).size());
        assertEquals(5, event.getPlayersAtTimeslots().get(players.get(4)).size());
        assertEquals(3, event.getPlayersAtTimeslots().get(players.get(5)).size());
        assertEquals(2, event.getPlayersAtTimeslots().get(players.get(6)).size());
        assertEquals(5, event.getPlayersAtTimeslots().get(players.get(7)).size());
    }

    @Test
    public void setPredefinedMatchupsNullTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Matchups cannot be null");
        event.setPredefinedMatchups(null);
    }

    @Test
    public void setPredefinedMatchupLimitExceededTest() {
        Set<Matchup> predefinedMatchups = new HashSet<>();

        predefinedMatchups.add(new Matchup(event,
                new HashSet<>(Arrays.asList(players.get(0), players.get(1))),
                new HashSet<>(event.getLocalizations()),
                new HashSet<>(event.getTimeslots()),
                1
        ));
        predefinedMatchups.add(new Matchup(event,
                new HashSet<>(Arrays.asList(players.get(0), players.get(7))),
                new HashSet<>(event.getLocalizations()),
                new HashSet<>(event.getTimeslots()),
                1
        ));
        predefinedMatchups.add(new Matchup(event,
                new HashSet<>(Arrays.asList(players.get(0), players.get(3))),
                new HashSet<>(event.getLocalizations()),
                new HashSet<>(event.getTimeslots()),
                1
        ));
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Player's (Player 1) number of predefined matchups (3) exceeds the limit (1)");
        event.setPredefinedMatchups(predefinedMatchups);
    }

    @Test
    public void addPredefinedMatchupTest() {
        Map<Player, Set<Localization>> playersInLocalizations = event.getPlayersInLocalizations();
        Map<Player, Set<Timeslot>> playersAtTimeslots = event.getPlayersAtTimeslots();

        event.addMatchup(new Matchup(event,
                new HashSet<>(Arrays.asList(players.get(2), players.get(6))),
                new HashSet<>(event.getLocalizations()),
                new HashSet<>(event.getTimeslots()),
                1
        ));

        assertTrue(event.hasPredefinedMatchups());
        assertEquals(1, event.getPredefinedMatchups().size());
        assertEquals(event.getLocalizations().size(), playersInLocalizations.get(players.get(2)).size());
        assertEquals(event.getTimeslots().subList(0, timeslots.size() - 1).size(),
                playersAtTimeslots.get(players.get(6)).size()
        );

        event.addMatchup(new Matchup(event,
                new HashSet<>(Arrays.asList(players.get(1), players.get(3))),
                new HashSet<>(event.getLocalizations()),
                new HashSet<>(event.getTimeslots()),
                1
        ));

        assertEquals(2, event.getPredefinedMatchups().size());
        assertEquals(event.getLocalizations().size(), playersInLocalizations.get(players.get(1)).size());
        assertEquals(event.getTimeslots().subList(0, timeslots.size() - 1).size(),
                playersAtTimeslots.get(players.get(1)).size()
        );

        event.addPlayerInLocalization(players.get(1), localizations.get(0));
        event.addPlayerAtTimeslotRange(players.get(1), timeslots.get(2), timeslots.get(7));

        assertEquals(event.getLocalizations().size(), playersInLocalizations.get(players.get(1)).size());
        assertEquals(event.getTimeslots().subList(0, timeslots.size() - 1).size(),
                playersAtTimeslots.get(players.get(1)).size()
        );


        event.setMatchesPerPlayer(2);
        event.setTimeslotsPerMatch(3);
        event.clearPlayersInLocalizations();
        event.clearPlayersAtTimeslots();

        assertFalse(event.hasPredefinedMatchups());
        assertFalse(event.hasPlayersInLocalizations());
        assertFalse(event.hasPlayersAtTimeslots());

        event.addMatchup(new Matchup(event,
                new HashSet<>(Arrays.asList(players.get(0), players.get(1))),
                new HashSet<>(Arrays.asList(localizations.get(0))),
                new HashSet<>(Arrays.asList(timeslots.get(0), timeslots.get(1))),
                1
        ));

        assertTrue(event.hasPredefinedMatchups());
        assertTrue(event.hasPlayersInLocalizations());
        assertTrue(event.hasPlayersAtTimeslots());

        assertEquals(2, event.getPlayersInLocalizations().size());
        assertEquals(event.getLocalizations().size(), event.getPlayersInLocalizations().get(players.get(0)).size());
        assertEquals(event.getLocalizations().size(), event.getPlayersInLocalizations().get(players.get(1)).size());

        assertEquals(2, event.getPlayersAtTimeslots().size());
        assertEquals(event.getTimeslots().size() - 2, event.getPlayersAtTimeslots().get(players.get(0)).size());
        assertEquals(event.getTimeslots().size() - 2, event.getPlayersAtTimeslots().get(players.get(1)).size());

        event.addPlayerInLocalization(players.get(2), localizations.get(0));
        event.addPlayerInLocalization(players.get(3), localizations.get(1));
        event.addMatchup(new Matchup(event,
                new HashSet<>(Arrays.asList(players.get(2), players.get(3))),
                new HashSet<>(Arrays.asList(localizations.get(0))),
                new HashSet<>(Arrays.asList(timeslots.get(4))),
                1
        ));

        assertEquals(4, event.getPlayersInLocalizations().size());
        assertEquals(1, event.getPlayersInLocalizations().get(players.get(2)).size());
        assertEquals(2, event.getPlayersInLocalizations().get(players.get(3)).size());

        assertEquals(4, event.getPlayersAtTimeslots().size());
        assertEquals(event.getTimeslots().size() - 2, event.getPlayersAtTimeslots().get(players.get(2)).size());
        assertEquals(event.getTimeslots().size() - 2, event.getPlayersAtTimeslots().get(players.get(3)).size());

        event.addPlayerAtTimeslot(players.get(4), timeslots.get(2));
        event.addPlayerAtTimeslotRange(players.get(5), timeslots.get(3), timeslots.get(6));
        event.addMatchup(new Matchup(event,
                new HashSet<>(Arrays.asList(players.get(4), players.get(5))),
                new HashSet<>(localizations),
                new HashSet<>(Arrays.asList(timeslots.get(0))),
                1
        ));

        assertEquals(6, event.getPlayersInLocalizations().size());
        assertEquals(event.getLocalizations().size(), event.getPlayersInLocalizations().get(players.get(4)).size());
        assertEquals(event.getLocalizations().size(), event.getPlayersInLocalizations().get(players.get(5)).size());

        assertEquals(6, event.getPlayersAtTimeslots().size());
        assertEquals(2, event.getPlayersAtTimeslots().get(players.get(4)).size());
        assertEquals(4, event.getPlayersAtTimeslots().get(players.get(5)).size());

        event.addPlayerInLocalization(players.get(6), localizations.get(0));
        event.addPlayerInLocalization(players.get(7), localizations.get(1));
        event.addPlayerAtTimeslot(players.get(6), timeslots.get(4));
        event.addPlayerAtTimeslot(players.get(6), timeslots.get(5));
        event.addPlayerAtTimeslot(players.get(7), timeslots.get(4));

        event.addMatchup(new Matchup(event,
                new HashSet<>(Arrays.asList(players.get(6), players.get(7))),
                new HashSet<>(Arrays.asList(localizations.get(0))),
                new HashSet<>(Arrays.asList(timeslots.get(3))),
                1
        ));

        assertEquals(8, event.getPlayersInLocalizations().size());
        assertEquals(1, event.getPlayersInLocalizations().get(players.get(6)).size());
        assertEquals(2, event.getPlayersInLocalizations().get(players.get(7)).size());

        assertEquals(8, event.getPlayersAtTimeslots().size());
        assertEquals(3, event.getPlayersAtTimeslots().get(players.get(6)).size());
        assertEquals(2, event.getPlayersAtTimeslots().get(players.get(7)).size());
    }

    @Test
    public void addPredefinedMatchupNullMatchupTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Matchup cannot be null");
        event.addMatchup((Matchup) null);
    }

    @Test
    public void addPredefinedMatchupPlayersSetTest() {
        event.addMatchup(new HashSet<>(Arrays.asList(players.get(2), players.get(4))));
        Matchup matchup = event.getPredefinedMatchups().iterator().next();
        assertEquals(event.getLocalizations().size(), matchup.getLocalizations().size());
        assertEquals(event.getTimeslots().size() - 1, matchup.getTimeslots().size());
        assertEquals(1, matchup.getOccurrences());
    }

    @Test
    public void addPredefinedMatchupPlayersTest() {
        event.addMatchup(players.get(6), players.get(7));
        Matchup matchup = event.getPredefinedMatchups().iterator().next();
        assertEquals(event.getLocalizations().size(), matchup.getLocalizations().size());
        assertEquals(event.getTimeslots().size() - 1, matchup.getTimeslots().size());
        assertEquals(1, matchup.getOccurrences());
    }

    @Test
    public void addPredefinedTeamsMatchupTest() {
        event.setPlayersPerMatch(4);
        event.addTeam(players.get(4), players.get(2));
        event.addTeam(players.get(5), players.get(0));

        event.addTeamMatchup(new HashSet<>(Arrays.asList(event.getTeams().get(0), event.getTeams().get(1))));

        Matchup matchup = event.getPredefinedMatchups().iterator().next();
        assertEquals(4, matchup.getPlayers().size());
        assertEquals(event.getLocalizations().size(), matchup.getLocalizations().size());
        assertEquals(event.getTimeslots().size() - 1, matchup.getTimeslots().size());
        assertEquals(1, matchup.getOccurrences());
    }

    @Test
    public void removePredefinedMatchupTest() {
        event.setMatchesPerPlayer(2);

        Set<Player> players = new HashSet<>(Arrays.asList(this.players.get(0), this.players.get(3)));
        Matchup matchup1 = new Matchup(event,
                players,
                new HashSet<>(event.getLocalizations().subList(0, 1)),
                new HashSet<>(event.getTimeslots().subList(3, 7)),
                1
        );
        Matchup matchup2 = new Matchup(event,
                players,
                new HashSet<>(event.getLocalizations().subList(1, 2)),
                new HashSet<>(event.getTimeslots().subList(0, 4)),
                1
        );

        assertFalse(event.hasPredefinedMatchups());

        event.addMatchup(matchup1);
        event.addMatchup(matchup2);

        assertEquals(2, event.getPredefinedMatchups().size());

        event.removeMatchup(matchup1);
        assertEquals(1, event.getPredefinedMatchups().size());

        event.removeMatchup(matchup2);
        assertEquals(0, event.getPredefinedMatchups().size());
    }

    @Test
    public void removePredefinedMatchupPlayersTest() {
        event.setMatchesPerPlayer(3);
        Set<Player> players1 = new HashSet<>(Arrays.asList(this.players.get(2), this.players.get(6)));
        Set<Player> players2 = new HashSet<>(Arrays.asList(this.players.get(0), this.players.get(3)));
        event.addMatchup(players1);
        event.addMatchup(players2);
        event.addMatchup(players2);

        assertTrue(event.hasPredefinedMatchups());

        event.removeMatchup(players1);
        assertEquals(2, event.getPredefinedMatchups().size());

        event.removeMatchup(players2);
        assertFalse(event.hasPredefinedMatchups());
    }

    @Test
    public void removePredefinedTeamsMatchupTest() {
        event.setPlayersPerMatch(4);
        event.addTeam(players.get(3), players.get(5));
        event.addTeam(players.get(1), players.get(4));

        List<Team> teams = event.getTeams();
        event.addTeamMatchup(new HashSet<>(Arrays.asList(teams.get(0), teams.get(1))));

        assertTrue(event.hasPredefinedMatchups());

        event.removeTeamsMatchup(new HashSet<>(Arrays.asList(event.getTeams().get(0), event.getTeams().get(1))));
        assertFalse(event.hasPredefinedMatchups());
    }

    @Test
    public void clearPredefinedMatchupsTest() {
        event.setMatchesPerPlayer(3);
        Set<Player> players1 = new HashSet<>(Arrays.asList(this.players.get(2), this.players.get(6)));
        Set<Player> players2 = new HashSet<>(Arrays.asList(this.players.get(0), this.players.get(3)));
        event.addMatchup(players1);
        event.addMatchup(players2);
        event.addMatchup(players2);

        assertTrue(event.hasPredefinedMatchups());

        event.clearPredefinedMatchups();
        assertFalse(event.hasPredefinedMatchups());
    }

    @Test
    public void hasPredefinedMatchupsTest() {
        assertFalse(event.hasPredefinedMatchups());

        event.addMatchup(new HashSet<>(Arrays.asList(players.get(3), players.get(7))));

        assertTrue(event.hasPredefinedMatchups());
    }

    @Test
    public void getPredefinedMatchupsTest() {
        event.addTeam(players.get(3), players.get(5));
        event.addTeam(players.get(1), players.get(4));

        expectedEx.expect(UnsupportedOperationException.class);
        event.getPredefinedMatchups().clear();
    }

    @Test
    public void setBreaksTest() {
        assertFalse(event.hasBreaks());

        List<Timeslot> breaks = new ArrayList<>();
        breaks.add(timeslots.get(4));
        breaks.add(timeslots.get(5));

        event.setBreaks(breaks);

        assertTrue(event.hasBreaks());
        assertEquals(2, event.getBreaks().size());
    }

    @Test
    public void setBreaksNullTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("list of breaks cannot be null");
        event.setBreaks(null);
    }

    @Test
    public void setBreaksNullBreakTest() {
        List<Timeslot> breaks = new ArrayList<>();
        breaks.add(timeslots.get(4));
        breaks.add(timeslots.get(5));
        breaks.add(null);

        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("break cannot be null");
        event.setBreaks(breaks);
    }

    @Test
    public void setBreaksDuplicatedTest() {
        List<Timeslot> breaks = new ArrayList<>();
        breaks.add(timeslots.get(0));
        breaks.add(timeslots.get(1));
        breaks.add(timeslots.get(1));

        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Break cannot be repeated");
        event.setBreaks(breaks);
    }

    @Test
    public void setBreaksNonexistingTimeslotTest() {
        List<Timeslot> breaks = new ArrayList<>();
        breaks.add(timeslots.get(1));
        breaks.add(new Timeslot(3));

        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("break timeslots must exist in the list of timeslots of this event");
        event.setBreaks(breaks);
    }

    @Test
    public void getBreaksTest() {
        List<Timeslot> breaks = new ArrayList<>();
        breaks.add(timeslots.get(4));

        expectedEx.expect(UnsupportedOperationException.class);
        event.getBreaks().add(timeslots.get(5));
    }

    @Test
    public void addBreakTest() {
        assertFalse(event.hasBreaks());

        event.addBreak(timeslots.get(3));
        event.addBreak(timeslots.get(4));
        event.addBreak(timeslots.get(7));

        assertTrue(event.hasBreaks());
        assertEquals(3, event.getBreaks().size());
    }

    @Test
    public void addBreakNullTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("break cannot be null");
        event.addBreak(null);
    }

    @Test
    public void addBreakNonexistingTimeslotTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("does not exist in this event");
        event.addBreak(new Timeslot(6, LocalTime.of(21, 0), Duration.ofHours(3)));
    }

    @Test
    public void addBreakRangeTest() {
        event.addBreakRange(timeslots.get(1), timeslots.get(3));

        assertTrue(event.hasBreaks());
        assertEquals(3, event.getBreaks().size());
        for (int t = 1; t <= 3; t++)
            event.isBreak(timeslots.get(t));

        event.addBreakRange(timeslots.get(5), timeslots.get(4));
        assertEquals(5, event.getBreaks().size());
        event.isBreak(timeslots.get(4));
        event.isBreak(timeslots.get(5));
    }

    @Test
    public void addBreakRangeNullT1Test() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("parameters cannot be null");
        event.addBreakRange(null, timeslots.get(2));
    }

    @Test
    public void addBreakRangeNullT2Test() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("parameters cannot be null");
        event.addBreakRange(timeslots.get(2), null);
    }

    @Test
    public void addBreakRangeNonexistingT1Test() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("does not exist in the list of timeslots of this event");
        event.addBreakRange(new Timeslot(10), timeslots.get(2));
    }

    @Test
    public void addBreakRangeNonexistingT2Test() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("does not exist in the list of timeslots of this event");
        event.addBreakRange(timeslots.get(2), new Timeslot(1));
    }

    @Test
    public void removeBreakTest() {
        event.addBreak(timeslots.get(2));
        event.addBreak(timeslots.get(3));

        assertEquals(2, event.getBreaks().size());

        event.removeBreak(timeslots.get(3));

        assertEquals(1, event.getBreaks().size());
    }

    @Test
    public void isBreakTest() {
        assertFalse(event.isBreak(timeslots.get(4)));
        assertFalse(event.isBreak(timeslots.get(6)));

        event.addBreak(timeslots.get(4));

        assertTrue(event.isBreak(timeslots.get(4)));

        event.removeBreak(timeslots.get(4));

        assertFalse(event.isBreak(timeslots.get(4)));
    }

    @Test
    public void isBreakNullTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("timeslot cannot be null");
        event.isBreak(null);
    }

    @Test
    public void isBreakNonexistingTimeslotTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("does not exist in this event");
        event.isBreak(new Timeslot(2));
    }

    @Test
    public void clearBreaksTest() {
        event.addBreak(timeslots.get(2));
        event.addBreak(timeslots.get(3));

        assertTrue(event.hasBreaks());

        event.clearBreaks();
        assertFalse(event.hasBreaks());
    }

    @Test
    public void hasBreaksTest() {
        assertFalse(event.hasBreaks());

        event.addBreak(timeslots.get(6));
        event.addBreak(timeslots.get(5));

        assertTrue(event.hasBreaks());

        event.removeBreak(timeslots.get(6));
        event.removeBreak(timeslots.get(5));

        assertFalse(event.hasBreaks());
    }

    @Test
    public void setUnavailableLocalizationsTest() {
        assertFalse(event.hasUnavailableLocalizations());

        Map<Localization, Set<Timeslot>> unavailableLocalizations = new HashMap<>();
        unavailableLocalizations.put(localizations.get(0),
                new HashSet<>(Arrays.asList(timeslots.get(5), timeslots.get(6)))
        );
        unavailableLocalizations.put(localizations.get(1),
                new HashSet<>(Arrays.asList(timeslots.get(0), timeslots.get(1), timeslots.get(2), timeslots.get(3)))
        );

        event.setUnavailableLocalizations(unavailableLocalizations);

        assertTrue(event.hasUnavailableLocalizations());
        assertEquals(2, event.getUnavailableLocalizations().size());
        assertEquals(4, event.getUnavailableLocalizations().get(localizations.get(1)).size());
        assertTrue(event.getUnavailableLocalizations().get(localizations.get(0)).contains(timeslots.get(5)));
    }

    @Test
    public void setUnavailableLocalizationsNullTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("unavailable localizations cannot be null");
        event.setUnavailableLocalizations(null);
    }

    @Test
    public void setUnavailableLocalizationsNullLocalizationTest() {
        Map<Localization, Set<Timeslot>> unavailableLocalizations = new HashMap<>();
        unavailableLocalizations.put(localizations.get(0),
                new HashSet<>(Arrays.asList(timeslots.get(5), timeslots.get(6)))
        );
        unavailableLocalizations.put(null, new HashSet<>(Arrays.asList(timeslots.get(0))));

        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("localization cannot be null");
        event.setUnavailableLocalizations(unavailableLocalizations);
    }

    @Test
    public void setUnavailableLocalizationsNonexistingLocalizationTest() {
        Map<Localization, Set<Timeslot>> unavailableLocalizations = new HashMap<>();
        unavailableLocalizations.put(localizations.get(0),
                new HashSet<>(Arrays.asList(timeslots.get(5), timeslots.get(6)))
        );
        unavailableLocalizations.put(new Localization("Unknown Localization"),
                new HashSet<>(Arrays.asList(timeslots.get(0)))
        );

        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("does not exist in this event");
        event.setUnavailableLocalizations(unavailableLocalizations);
    }

    @Test
    public void setUnavailableLocalizationsNullTimeslotsTest() {
        Map<Localization, Set<Timeslot>> unavailableLocalizations = new HashMap<>();
        unavailableLocalizations.put(localizations.get(0), null);

        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("unavailable timeslots set for the localization");
        event.setUnavailableLocalizations(unavailableLocalizations);
    }

    @Test
    public void setUnavailableLocalizationsNonexistingTimeslotTest() {
        Map<Localization, Set<Timeslot>> unavailableLocalizations = new HashMap<>();
        unavailableLocalizations.put(localizations.get(1),
                new HashSet<>(Arrays.asList(timeslots.get(5), new Timeslot(4)))
        );

        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("does not exist in this event");
        event.setUnavailableLocalizations(unavailableLocalizations);
    }

    @Test
    public void getUnavailableLocalizationsTest() {
        expectedEx.expect(UnsupportedOperationException.class);
        event.getUnavailableLocalizations().put(localizations.get(1), new HashSet<>(Arrays.asList(timeslots.get(4))));
    }

    @Test
    public void addUnavailableLocalizationTest() {
        assertFalse(event.hasUnavailableLocalizations());

        event.addUnavailableLocalizationAtTimeslot(localizations.get(1), timeslots.get(3));
        event.addUnavailableLocalizationAtTimeslot(localizations.get(1), timeslots.get(4));
        event.addUnavailableLocalizationAtTimeslot(localizations.get(1), timeslots.get(5));
        event.addUnavailableLocalizationAtTimeslot(localizations.get(1), timeslots.get(6));

        assertTrue(event.hasUnavailableLocalizations());
        assertEquals(1, event.getUnavailableLocalizations().size());
        assertEquals(4, event.getUnavailableLocalizations().get(localizations.get(1)).size());
    }

    @Test
    public void addUnavailableLocalizationNullLocalizationTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("parameters cannot be null");
        event.addUnavailableLocalizationAtTimeslot(null, timeslots.get(2));
    }

    @Test
    public void addUnavailableLocalizationNullTimeslotTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("parameters cannot be null");
        event.addUnavailableLocalizationAtTimeslot(localizations.get(0), null);
    }

    @Test
    public void addUnavailableLocalizationNonexistingLocalizationTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("does not exist in this event");
        event.addUnavailableLocalizationAtTimeslot(new Localization("Unknown Localization"), timeslots.get(2));
    }

    @Test
    public void addUnavailableLocalizationNonexistingTimeslotTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("does not exist in this event");
        event.addUnavailableLocalizationAtTimeslot(localizations.get(0), new Timeslot(6));
    }

    @Test
    public void addUnavailableLocalizationAtTimeslotsTest() {
        assertFalse(event.hasUnavailableLocalizations());

        event.addUnavailableLocalizationAtTimeslots(localizations.get(1),
                new HashSet<>(Arrays.asList(timeslots.get(4), timeslots.get(5), timeslots.get(6)))
        );
        event.addUnavailableLocalizationAtTimeslots(localizations.get(0),
                new HashSet<>(Arrays.asList(timeslots.get(6)))
        );

        assertTrue(event.hasUnavailableLocalizations());
        assertEquals(2, event.getUnavailableLocalizations().size());
        assertEquals(3, event.getUnavailableLocalizations().get(localizations.get(1)).size());
    }

    @Test
    public void addUnavailableLocalizationAtTimeslotsNullLocalizationTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("parameters cannot be null");
        event.addUnavailableLocalizationAtTimeslots(null,
                new HashSet<>(Arrays.asList(timeslots.get(4), timeslots.get(5), timeslots.get(6)))
        );
    }

    @Test
    public void addUnavailableLocalizationAtTimeslotsNullTimeslotsTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("parameters cannot be null");
        event.addUnavailableLocalizationAtTimeslots(localizations.get(1), null);
    }

    @Test
    public void removeUnavailableLocalizationTest() {
        event.addUnavailableLocalizationAtTimeslot(localizations.get(0), timeslots.get(2));
        event.addUnavailableLocalizationAtTimeslot(localizations.get(0), timeslots.get(3));
        event.addUnavailableLocalizationAtTimeslot(localizations.get(0), timeslots.get(4));

        assertEquals(3, event.getUnavailableLocalizations().get(localizations.get(0)).size());

        event.removeUnavailableLocalization(localizations.get(0));

        assertNull(event.getUnavailableLocalizations().get(localizations.get(0)));
    }

    @Test
    public void addUnavailableLocalizationAtTimeslotRangeTest() {
        event.addUnavailableLocalizationAtTimeslotRange(localizations.get(0), timeslots.get(2), timeslots.get(4));

        assertTrue(event.hasUnavailableLocalizations());

        Map<Localization, Set<Timeslot>> unavailable = event.getUnavailableLocalizations();
        assertEquals(1, unavailable.size());

        Set<Timeslot> localizationUnavailable = unavailable.get(localizations.get(0));
        assertEquals(3, localizationUnavailable.size());
        for (int t = 2; t <= 4; t++)
            assertTrue(localizationUnavailable.contains(timeslots.get(t)));

        event.addUnavailableLocalizationAtTimeslotRange(localizations.get(1), timeslots.get(6), timeslots.get(3));
        assertEquals(2, unavailable.size());

        localizationUnavailable = unavailable.get(localizations.get(1));
        assertEquals(4, localizationUnavailable.size());
        for (int t = 3; t <= 6; t++)
            assertTrue(localizationUnavailable.contains(timeslots.get(t)));
    }

    @Test
    public void addUnavailableLocalizationAtTimeslotRangeNullPlayerTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("parameters cannot be null");
        event.addUnavailableLocalizationAtTimeslotRange(null, timeslots.get(0), timeslots.get(4));
    }

    @Test
    public void addUnavailableLocalizationAtTimeslotRangeNullTimeslot1Test() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("parameters cannot be null");
        event.addUnavailableLocalizationAtTimeslotRange(localizations.get(0), null, timeslots.get(4));
    }

    @Test
    public void addUnavailableLocalizationAtTimeslotRangeNullTimeslot2Test() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("parameters cannot be null");
        event.addUnavailableLocalizationAtTimeslotRange(localizations.get(1), timeslots.get(0), null);
    }

    @Test
    public void addUnavailableLocalizationAtTimeslotRangeNonexistingPlayerTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("does not exist in the list of localizations");
        event.addUnavailableLocalizationAtTimeslotRange(new Localization("Unknown Localization"),
                timeslots.get(0),
                timeslots.get(4)
        );
    }

    @Test
    public void addUnavailableLocalizationAtTimeslotRangeNonexistingT1Test() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("does not exist in the list of timeslots");
        event.addUnavailableLocalizationAtTimeslotRange(localizations.get(1),
                new Timeslot(3),
                timeslots.get(4)
        );
    }

    @Test
    public void addUnavailableLocalizationAtTimeslotRangeNonexistingT2Test() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("does not exist in the list of timeslots");
        event.addUnavailableLocalizationAtTimeslotRange(localizations.get(1),
                timeslots.get(0),
                new Timeslot(3)
        );
    }

    @Test
    public void removeUnavailableLocalizationTimeslotTest() {
        event.addUnavailableLocalizationAtTimeslot(localizations.get(1), timeslots.get(0));
        event.addUnavailableLocalizationAtTimeslot(localizations.get(1), timeslots.get(1));
        event.addUnavailableLocalizationAtTimeslot(localizations.get(1), timeslots.get(2));
        event.addUnavailableLocalizationAtTimeslot(localizations.get(1), timeslots.get(3));

        assertEquals(4, event.getUnavailableLocalizations().get(localizations.get(1)).size());

        event.removeUnavailableLocalizationTimeslot(localizations.get(0), timeslots.get(3));

        assertEquals(4, event.getUnavailableLocalizations().get(localizations.get(1)).size());

        event.removeUnavailableLocalizationTimeslot(localizations.get(1), timeslots.get(3));

        assertEquals(3, event.getUnavailableLocalizations().get(localizations.get(1)).size());

        event.removeUnavailableLocalizationTimeslot(localizations.get(1), timeslots.get(2));
        event.removeUnavailableLocalizationTimeslot(localizations.get(1), timeslots.get(0));
        event.removeUnavailableLocalizationTimeslot(localizations.get(1), timeslots.get(1));

        assertNull(event.getUnavailableLocalizations().get(localizations.get(1)));
        assertFalse(event.hasUnavailableLocalizations());
    }

    @Test
    public void removeUnavailableLocalizationTimeslotNullLocalizationTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("parameters cannot be null");
        event.removeUnavailableLocalizationTimeslot(null, timeslots.get(3));
    }

    @Test
    public void removeUnavailableLocalizationTimeslotNullTimeslotTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("parameters cannot be null");
        event.removeUnavailableLocalizationTimeslot(localizations.get(1), null);
    }

    @Test
    public void removeUnavailableLocalizationTimeslotNonexistingLocalizationTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("does not exist in this event");
        event.removeUnavailableLocalizationTimeslot(new Localization("Unknown Localization"), timeslots.get(3));
    }

    @Test
    public void removeUnavailableLocalizationTimeslotNonexistingTimeslotTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("does not exist in this event");
        event.removeUnavailableLocalizationTimeslot(localizations.get(1), new Timeslot(0));
    }

    @Test
    public void clearUnavailableLocalizationsTest() {
        event.addUnavailableLocalizationAtTimeslot(localizations.get(1), timeslots.get(3));
        event.addUnavailableLocalizationAtTimeslot(localizations.get(1), timeslots.get(4));
        event.addUnavailableLocalizationAtTimeslot(localizations.get(1), timeslots.get(5));
        event.addUnavailableLocalizationAtTimeslot(localizations.get(1), timeslots.get(6));

        assertTrue(event.hasUnavailableLocalizations());

        event.clearUnavailableLocalizations();
        assertFalse(event.hasUnavailableLocalizations());
    }

    @Test
    public void hasUnavailableLocalizationsTest() {
        assertFalse(event.hasUnavailableLocalizations());

        event.addUnavailableLocalizationAtTimeslot(localizations.get(1), timeslots.get(1));
        event.addUnavailableLocalizationAtTimeslot(localizations.get(1), timeslots.get(2));
        event.addUnavailableLocalizationAtTimeslot(localizations.get(1), timeslots.get(3));

        assertTrue(event.hasUnavailableLocalizations());
    }

    @Test
    public void setPlayersInLocalizationsTest() {
        Map<Player, Set<Localization>> playersInLocalizations = new HashMap<>();
        playersInLocalizations.put(players.get(4), new HashSet<>(Arrays.asList(localizations.get(0))));
        playersInLocalizations.put(players.get(1),
                new HashSet<>(Arrays.asList(localizations.get(1), localizations.get(0)))
        );

        event.setPlayersInLocalizations(playersInLocalizations);

        assertTrue(event.hasPlayersInLocalizations());
        assertEquals(2, event.getPlayersInLocalizations().size());
        assertEquals(2, event.getPlayersInLocalizations().get(players.get(1)).size());
    }

    @Test
    public void setPlayersInLocalizationsNullTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("parameter cannot be null");
        event.setPlayersInLocalizations(null);
    }

    @Test
    public void setPlayersInLocalizationsNullPlayerTest() {
        Map<Player, Set<Localization>> playersInLocalizations = new HashMap<>();
        playersInLocalizations.put(null, new HashSet<>(Arrays.asList(localizations.get(0))));
        playersInLocalizations.put(players.get(1),
                new HashSet<>(Arrays.asList(localizations.get(1), localizations.get(0)))
        );

        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("player cannot be null");
        event.setPlayersInLocalizations(playersInLocalizations);
    }

    @Test
    public void setPlayersInLocalizationsNonexistingPlayerTest() {
        Map<Player, Set<Localization>> playersInLocalizations = new HashMap<>();
        playersInLocalizations.put(new Player("Unknown Player"), new HashSet<>(Arrays.asList(localizations.get(0))));
        playersInLocalizations.put(players.get(1),
                new HashSet<>(Arrays.asList(localizations.get(1), localizations.get(0)))
        );

        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("does not exist in the list of players of this event");
        event.setPlayersInLocalizations(playersInLocalizations);
    }

    @Test
    public void setPlayersInLocalizationsNullLocalizationsTest() {
        Map<Player, Set<Localization>> playersInLocalizations = new HashMap<>();
        playersInLocalizations.put(players.get(1), null);

        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("localizations assigned to the player cannot be null");
        event.setPlayersInLocalizations(playersInLocalizations);
    }

    @Test
    public void setPlayersInLocalizationsNonexistingLocalizationTest() {
        Map<Player, Set<Localization>> playersInLocalizations = new HashMap<>();
        playersInLocalizations.put(players.get(1),
                new HashSet<>(Arrays.asList(new Localization("Unknown Localization")))
        );

        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("does not exist in this event");
        event.setPlayersInLocalizations(playersInLocalizations);
    }

    @Test
    public void getPlayersInLocalizationsTest() {
        expectedEx.expect(UnsupportedOperationException.class);
        event.getPlayersInLocalizations().put(players.get(1), new HashSet<>(Arrays.asList(localizations.get(1))));
    }

    @Test
    public void addPlayerInLocalizationTest() {
        event.addPlayerInLocalization(players.get(6), localizations.get(0));
        event.addPlayerInLocalization(players.get(7), localizations.get(0));
        event.addPlayerInLocalization(players.get(6), localizations.get(1));

        assertEquals(2, event.getPlayersInLocalizations().size());
        assertTrue(event.getPlayersInLocalizations().get(players.get(7)).contains(localizations.get(0)));
    }

    @Test
    public void addPlayerInLocalizationNullPlayerTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("cannot be null");
        event.addPlayerInLocalization(null, localizations.get(1));
    }

    @Test
    public void addPlayerInLocalizationNullLocalizationTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("cannot be null");
        event.addPlayerInLocalization(players.get(7), null);
    }

    @Test
    public void addPlayerInLocalizationNonexistingPlayerTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("does not exist in the list of players of this event");
        event.addPlayerInLocalization(new Player("Unknown Player"), localizations.get(1));
    }

    @Test
    public void addPlayerInLocalizationNonexistingLocalizationTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("does not exist");
        event.addPlayerInLocalization(players.get(2), new Localization("Unknown Localization"));
    }

    @Test
    public void removePlayerInLocalizationTest() {
        event.addPlayerInLocalization(players.get(3), localizations.get(1));
        event.addPlayerInLocalization(players.get(3), localizations.get(0));

        assertEquals(2, event.getPlayersInLocalizations().get(players.get(3)).size());

        event.removePlayerInLocalization(players.get(3), localizations.get(0));

        assertEquals(1, event.getPlayersInLocalizations().get(players.get(3)).size());

        event.removePlayerInLocalization(players.get(3), localizations.get(1));

        assertNull(event.getPlayersInLocalizations().get(players.get(3)));
    }

    @Test
    public void clearPlayersInLocalizationsTest() {
        event.addPlayerInLocalization(players.get(3), localizations.get(1));
        event.addPlayerInLocalization(players.get(3), localizations.get(0));

        assertTrue(event.hasPlayersInLocalizations());

        event.clearPlayersInLocalizations();
        assertFalse(event.hasPlayersInLocalizations());
    }

    @Test
    public void hasPlayersInLocalizationsTest() {
        assertFalse(event.hasPlayersInLocalizations());

        event.addPlayerInLocalization(players.get(3), localizations.get(1));

        assertTrue(event.hasPlayersInLocalizations());

        event.removePlayerInLocalization(players.get(3), localizations.get(1));

        assertFalse(event.hasPlayersInLocalizations());
    }

    @Test
    public void setPlayersAtTimeslotsTest() {
        assertFalse(event.hasPlayersAtTimeslots());

        Map<Player, Set<Timeslot>> playersAtTimeslots = new HashMap<>();
        playersAtTimeslots.put(players.get(7),
                new HashSet<>(Arrays.asList(timeslots.get(0),
                        timeslots.get(1),
                        timeslots.get(2),
                        timeslots.get(6),
                        timeslots.get(7)
                ))
        );
        playersAtTimeslots.put(players.get(4), new HashSet<>(Arrays.asList(timeslots.get(3))));
        playersAtTimeslots.put(players.get(3), new HashSet<>(Arrays.asList(timeslots.get(3), timeslots.get(5))));
        playersAtTimeslots.put(players.get(0), new HashSet<>(Arrays.asList(timeslots.get(5), timeslots.get(7))));

        event.setPlayersAtTimeslots(playersAtTimeslots);

        assertTrue(event.hasPlayersAtTimeslots());

        playersAtTimeslots = event.getPlayersAtTimeslots();
        assertEquals(4, playersAtTimeslots.size());

        assertEquals(4, playersAtTimeslots.get(players.get(7)).size());
        assertEquals(1, playersAtTimeslots.get(players.get(4)).size());
        assertEquals(2, playersAtTimeslots.get(players.get(3)).size());
        assertEquals(1, playersAtTimeslots.get(players.get(0)).size());

        assertFalse(playersAtTimeslots.get(players.get(7)).contains(timeslots.get(7)));
        assertFalse(playersAtTimeslots.get(players.get(0)).contains(timeslots.get(7)));
    }

    @Test
    public void setPlayersAtTimeslotsNullTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("parameter cannot be null");
        event.setPlayersAtTimeslots(null);
    }

    @Test
    public void setPlayersAtTimeslotsNullPlayerTest() {
        assertFalse(event.hasPlayersAtTimeslots());

        Map<Player, Set<Timeslot>> playersAtTimeslots = new HashMap<>();
        playersAtTimeslots.put(players.get(4), new HashSet<>(Arrays.asList(timeslots.get(3))));
        playersAtTimeslots.put(null, new HashSet<>(Arrays.asList(timeslots.get(3))));
        playersAtTimeslots.put(players.get(0), new HashSet<>(Arrays.asList(timeslots.get(5), timeslots.get(7))));

        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("player cannot be null");
        event.setPlayersAtTimeslots(playersAtTimeslots);
    }

    @Test
    public void setPlayersAtTimeslotsNonexistingPlayerTest() {
        assertFalse(event.hasPlayersAtTimeslots());

        Map<Player, Set<Timeslot>> playersAtTimeslots = new HashMap<>();
        playersAtTimeslots.put(players.get(4), new HashSet<>(Arrays.asList(timeslots.get(3))));
        playersAtTimeslots.put(new Player("Unknown Player"), new HashSet<>(Arrays.asList(timeslots.get(3))));
        playersAtTimeslots.put(players.get(0), new HashSet<>(Arrays.asList(timeslots.get(5), timeslots.get(7))));

        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("does not exist in the list of players of this event");
        event.setPlayersAtTimeslots(playersAtTimeslots);
    }

    @Test
    public void setPlayersAtTimeslotsNullTimeslotsTest() {
        assertFalse(event.hasPlayersAtTimeslots());

        Map<Player, Set<Timeslot>> playersAtTimeslots = new HashMap<>();
        playersAtTimeslots.put(players.get(4), new HashSet<>(Arrays.asList(timeslots.get(3))));
        playersAtTimeslots.put(players.get(0), null);

        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("timeslots assigned to the player cannot be null");
        event.setPlayersAtTimeslots(playersAtTimeslots);
    }

    @Test
    public void setPlayersAtTimeslotsNonexistingTimeslotTest() {
        assertFalse(event.hasPlayersAtTimeslots());

        Map<Player, Set<Timeslot>> playersAtTimeslots = new HashMap<>();
        playersAtTimeslots.put(players.get(4), new HashSet<>(Arrays.asList(timeslots.get(3))));
        playersAtTimeslots.put(players.get(0), new HashSet<>(Arrays.asList(timeslots.get(3), new Timeslot(3))));

        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("does not exist in this event");
        event.setPlayersAtTimeslots(playersAtTimeslots);
    }

    @Test
    public void getPlayersAtTimeslotsTest() {
        expectedEx.expect(UnsupportedOperationException.class);
        event.getPlayersAtTimeslots().put(players.get(4), new HashSet<>(Arrays.asList(timeslots.get(3))));
    }

    @Test
    public void addPlayerAtTimeslotTest() {
        event.setTimeslotsPerMatch(3);

        event.addPlayerAtTimeslot(players.get(5), timeslots.get(4));
        event.addPlayerAtTimeslot(players.get(5), timeslots.get(5));
        event.addPlayerAtTimeslot(players.get(5), timeslots.get(6));
        event.addPlayerAtTimeslot(players.get(5), timeslots.get(7));

        assertTrue(event.hasPlayersAtTimeslots());
        assertEquals(1, event.getPlayersAtTimeslots().size());
        assertEquals(2, event.getPlayersAtTimeslots().get(players.get(5)).size());
        assertFalse(event.getPlayersAtTimeslots().get(players.get(5)).contains(timeslots.get(6)));
        assertFalse(event.getPlayersAtTimeslots().get(players.get(5)).contains(timeslots.get(7)));
    }

    @Test
    public void addPlayerAtTimeslotNullPlayerTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Player cannot be null");
        event.addPlayerAtTimeslot(null, timeslots.get(5));
    }

    @Test
    public void addPlayerAtTimeslotNullTimeslotTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Timeslot cannot be null");
        event.addPlayerAtTimeslot(players.get(4), null);
    }

    @Test
    public void addPlayerAtTimeslotNonexistingPlayerTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("does not exist in the list of players of this event");
        event.addPlayerAtTimeslot(new Player("Unknown Player"), timeslots.get(5));
    }

    @Test
    public void addPlayerAtTimeslotNonexistingTimeslotTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("does not exist in this event");
        event.addPlayerAtTimeslot(players.get(4), new Timeslot(4));
    }

    @Test
    public void addPlayerAtTimeslotsTest() {
        event.setTimeslotsPerMatch(4);

        event.addPlayerAtTimeslots(players.get(4),
                new HashSet<>(Arrays.asList(timeslots.get(3), timeslots.get(4), timeslots.get(5), timeslots.get(6)))
        );

        assertEquals(2, event.getPlayersAtTimeslots().get(players.get(4)).size());
    }

    @Test
    public void addPlayerAtTimeslotRangeTest() {
        event.addPlayerAtTimeslotRange(players.get(2), timeslots.get(0), timeslots.get(4));

        assertTrue(event.hasPlayersAtTimeslots());

        Map<Player, Set<Timeslot>> playersAtTimeslots = event.getPlayersAtTimeslots();
        assertEquals(1, playersAtTimeslots.size());

        Set<Timeslot> playerTimeslots = playersAtTimeslots.get(players.get(2));
        assertEquals(5, playerTimeslots.size());
        for (int t = 0; t <= 4; t++)
            assertTrue(playerTimeslots.contains(timeslots.get(t)));

        event.addPlayerAtTimeslotRange(players.get(6), timeslots.get(7), timeslots.get(3));
        assertEquals(2, playersAtTimeslots.size());

        playerTimeslots = playersAtTimeslots.get(players.get(6));
        assertEquals(4, playerTimeslots.size());
        for (int t = 3; t <= 6; t++)
            assertTrue(playerTimeslots.contains(timeslots.get(t)));
        assertFalse(playerTimeslots.contains(timeslots.get(7)));
    }

    @Test
    public void addPlayerAtTimeslotRangeNullPlayerTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("parameters cannot be null");
        event.addPlayerAtTimeslotRange(null, timeslots.get(0), timeslots.get(4));
    }

    @Test
    public void addPlayerAtTimeslotRangeNullTimeslot1Test() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("parameters cannot be null");
        event.addPlayerAtTimeslotRange(players.get(2), null, timeslots.get(4));
    }

    @Test
    public void addPlayerAtTimeslotRangeNullTimeslot2Test() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("parameters cannot be null");
        event.addPlayerAtTimeslotRange(players.get(2), timeslots.get(0), null);
    }

    @Test
    public void addPlayerAtTimeslotRangeNonexistingPlayerTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("does not exist in the list of players");
        event.addPlayerAtTimeslotRange(new Player("Unknown Player"), timeslots.get(0), timeslots.get(4));
    }

    @Test
    public void addPlayerAtTimeslotRangeNonexistingT1Test() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("does not exist in the list of timeslots");
        event.addPlayerAtTimeslotRange(players.get(2), new Timeslot(3), timeslots.get(4));
    }

    @Test
    public void addPlayerAtTimeslotRangeNonexistingT2Test() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("does not exist in the list of timeslots");
        event.addPlayerAtTimeslotRange(players.get(2), timeslots.get(0), new Timeslot(3));
    }

    @Test
    public void addPlayerAtTimeslotsNullTimeslotsTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Timeslots cannot be null");
        event.addPlayerAtTimeslots(players.get(4), null);
    }

    @Test
    public void addPlayersAtTimeslotsTest() {
        event.setTimeslotsPerMatch(3);

        event.addPlayersAtTimeslots(new HashSet<>(Arrays.asList(players.get(4), players.get(7))),
                new HashSet<>(Arrays.asList(timeslots.get(0), timeslots.get(1), timeslots.get(5), timeslots.get(6)))
        );

        assertEquals(2, event.getPlayersAtTimeslots().size());
        assertEquals(3, event.getPlayersAtTimeslots().get(players.get(4)).size());
    }

    @Test
    public void addPlayersAtTimeslotsTestNullPlayersTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("parameters cannot be null");
        event.addPlayersAtTimeslots(null,
                new HashSet<>(Arrays.asList(timeslots.get(0), timeslots.get(1), timeslots.get(5), timeslots.get(6)))
        );
    }

    @Test
    public void addPlayersAtTimeslotsTestNullTimeslotsTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("parameters cannot be null");
        event.addPlayersAtTimeslots(new HashSet<>(Arrays.asList(players.get(4), players.get(7))), null);
    }

    @Test
    public void removePlayerAtTimeslotTest() {
        event.addPlayersAtTimeslots(new HashSet<>(Arrays.asList(players.get(4), players.get(7))),
                new HashSet<>(Arrays.asList(timeslots.get(0), timeslots.get(1), timeslots.get(5), timeslots.get(6)))
        );

        assertEquals(4, event.getPlayersAtTimeslots().get(players.get(4)).size());

        event.removePlayerAtTimeslot(players.get(4), timeslots.get(0));
        event.removePlayerAtTimeslot(players.get(4), timeslots.get(1));

        assertEquals(2, event.getPlayersAtTimeslots().get(players.get(4)).size());

        event.removePlayerAtTimeslot(players.get(4), timeslots.get(6));
        event.removePlayerAtTimeslot(players.get(4), timeslots.get(5));

        assertNull(event.getPlayersAtTimeslots().get(players.get(4)));
    }

    @Test
    public void removePlayerAtTimeslotsTest() {
        event.addPlayersAtTimeslots(new HashSet<>(Arrays.asList(players.get(4), players.get(7))),
                new HashSet<>(Arrays.asList(timeslots.get(0), timeslots.get(1), timeslots.get(5), timeslots.get(6)))
        );

        assertEquals(4, event.getPlayersAtTimeslots().get(players.get(4)).size());

        event.removePlayerAtTimeslots(players.get(4), new HashSet<>(Arrays.asList(timeslots.get(0), timeslots.get(1))));

        assertEquals(2, event.getPlayersAtTimeslots().get(players.get(4)).size());

        event.removePlayerAtTimeslots(players.get(4), new HashSet<>(Arrays.asList(timeslots.get(6), timeslots.get(5))));

        assertNull(event.getPlayersAtTimeslots().get(players.get(4)));
    }

    @Test
    public void removePlayerAtTimeslotsNullTimeslotsTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Timeslots cannot be null");
        event.removePlayerAtTimeslots(players.get(4), null);
    }

    @Test
    public void clearPlayersAtTimeslotsTest() {
        event.addPlayerAtTimeslot(players.get(4), timeslots.get(4));
        assertTrue(event.hasPlayersAtTimeslots());

        event.clearPlayersAtTimeslots();
        assertFalse(event.hasPlayersAtTimeslots());
    }

    @Test
    public void hasPlayersAtTimeslotsTest() {
        assertFalse(event.hasPlayersAtTimeslots());

        event.addPlayersAtTimeslots(new HashSet<>(Arrays.asList(players.get(4), players.get(7))),
                new HashSet<>(Arrays.asList(timeslots.get(0), timeslots.get(1), timeslots.get(5), timeslots.get(6)))
        );

        assertTrue(event.hasPlayersAtTimeslots());
    }

    @Test
    public void matchupModeTest() {
        event.setMatchupMode(TournamentSolver.MatchupMode.ALL_DIFFERENT);

        Assert.assertEquals(TournamentSolver.MatchupMode.ANY, event.getMatchupMode());

        event.setMatchesPerPlayer(2);
        event.setMatchupMode(TournamentSolver.MatchupMode.ALL_DIFFERENT);

        Assert.assertEquals(TournamentSolver.MatchupMode.ALL_DIFFERENT, event.getMatchupMode());

        event.setMatchesPerPlayer(1);
        event.setMatchesPerPlayer(2);
        event.setMatchupMode(TournamentSolver.MatchupMode.ALL_EQUAL);

        Assert.assertEquals(TournamentSolver.MatchupMode.ALL_EQUAL, event.getMatchupMode());

        event.setPlayersPerMatch(1);
        event.setMatchupMode(TournamentSolver.MatchupMode.ALL_DIFFERENT);

        Assert.assertEquals(TournamentSolver.MatchupMode.ANY, event.getMatchupMode());
    }

    @Test
    public void setMatchupModeNullTest() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Matchup mode cannot be null");
        event.setMatchupMode(null);
    }

    @Test
    public void getTeamByPlayerTest() {
        event.setPlayersPerMatch(4);
        event.addTeam(players.get(0), players.get(1));
        event.addTeam(players.get(2), players.get(3));

        Assert.assertEquals(event.getTeams().get(0), event.filterTeamByPlayer(players.get(1)));
        Assert.assertEquals(event.getTeams().get(0), event.filterTeamByPlayer(players.get(0)));
        Assert.assertEquals(event.getTeams().get(1), event.filterTeamByPlayer(players.get(3)));

        Assert.assertEquals(null, event.filterTeamByPlayer(players.get(4)));
        Assert.assertEquals(null, event.filterTeamByPlayer(players.get(7)));
    }

    @Test
    public void isPlayerUnavailableTest() {
        event.addUnavailablePlayerAtTimeslot(players.get(3), timeslots.get(0));
        event.addUnavailablePlayerAtTimeslot(players.get(3), timeslots.get(1));
        event.addUnavailablePlayerAtTimeslot(players.get(3), timeslots.get(2));
        event.addUnavailablePlayerAtTimeslot(players.get(7), timeslots.get(7));

        assertTrue(event.isPlayerUnavailable(players.get(3), timeslots.get(1)));
        assertTrue(event.isPlayerUnavailable(players.get(3), timeslots.get(2)));
        assertTrue(event.isPlayerUnavailable(players.get(7), timeslots.get(7)));
        assertFalse(event.isPlayerUnavailable(players.get(4), timeslots.get(1)));
        assertFalse(event.isPlayerUnavailable(players.get(6), timeslots.get(5)));
    }

    @Test
    public void isLocalizationUnavailableTest() {
        event.addUnavailableLocalizationAtTimeslot(localizations.get(0), timeslots.get(0));
        event.addUnavailableLocalizationAtTimeslot(localizations.get(0), timeslots.get(1));
        event.addUnavailableLocalizationAtTimeslot(localizations.get(0), timeslots.get(2));

        assertTrue(event.isLocalizationUnavailable(localizations.get(0), timeslots.get(0)));
        assertTrue(event.isLocalizationUnavailable(localizations.get(0), timeslots.get(1)));
        assertTrue(event.isLocalizationUnavailable(localizations.get(0), timeslots.get(2)));
        assertFalse(event.isLocalizationUnavailable(localizations.get(0), timeslots.get(3)));
        assertFalse(event.isLocalizationUnavailable(localizations.get(0), timeslots.get(4)));
        assertFalse(event.isLocalizationUnavailable(localizations.get(1), timeslots.get(0)));
        assertFalse(event.isLocalizationUnavailable(localizations.get(1), timeslots.get(1)));
    }

    @Test
    public void getNumberOfMatchesTest() {
        assertEquals(event.getNumberOfMatches() * event.getTimeslotsPerMatch() / event.getPlayersPerMatch(),
                event.getNumberOfMatches()
        );
    }

    @Test
    public void setAndGetTournamentTest() {
        assertNull(event.getTournament());
        Tournament t = new Tournament("Tournament", event);
        assertNotNull(event.getTournament());
        Assert.assertEquals(t, event.getTournament());
    }

    @Test
    public void toStringTest() {
        assertEquals("Event", event.toString());
        assertNotEquals("", event.toString());
    }

    @Test
    public void getNumberOfOccupiedTimeslotsTest() {
        assertEquals(players.size() * event.getMatchesPerPlayer() * event.getTimeslotsPerMatch(),
                event.getNumberOfOccupiedTimeslots()
        );
    }
}
