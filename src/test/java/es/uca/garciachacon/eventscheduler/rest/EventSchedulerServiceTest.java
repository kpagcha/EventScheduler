package es.uca.garciachacon.eventscheduler.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import es.uca.garciachacon.eventscheduler.data.model.tournament.*;
import es.uca.garciachacon.eventscheduler.rest.dao.ITournamentDao;
import es.uca.garciachacon.eventscheduler.rest.dao.TournamentDao;
import es.uca.garciachacon.eventscheduler.utils.TournamentUtils;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.hamcrest.core.StringContains;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Duration;
import java.time.LocalTime;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Tests del servicio web {@link EventSchedulerService} y sus componentes, como el DAO  que usa para manejar
 * los torneos, {@link ITournamentDao} y su implementación básica empleada, {@link TournamentDao}.
 */
public class EventSchedulerServiceTest extends JerseyTest {
    private static List<Tournament> tournaments = new ArrayList<>();
    private static String path = "eventscheduler";

    @BeforeClass
    public static void setUpBeforeClass() {
        Player querry = new Player("Sam Querrey");
        Player mahut = new Player("Nicolas Mahut");
        Player goffin = new Player("David Goffin");
        Player raonic = new Player("Milos Raonic");
        Player federer = new Player("Roger Federer");
        Player johnson = new Player("Steve Johnson");
        Player cilic = new Player("Marin Cilic");
        Player nishikori = new Player("Kei Nishikori");
        Player vesely = new Player("Jiri Vesely");
        Player berdych = new Player("Tomas Berdych");
        Player tomic = new Player("Bernard Tomic");
        Player pouille = new Player("Lucas Pouille");
        Player gasquet = new Player("Richard Gasquet");
        Player tsonga = new Player("Jo-Wilfried Tsonga");
        Player kyrgios = new Player("Nick Kyrgios");
        Player murray = new Player("Andy Murray");

        Localization centreCourt = new Localization("Centre Court");
        Localization no1Court = new Localization("No. 1 Court");
        Localization no2Court = new Localization("No. 2 Court");
        Localization no3Court = new Localization("No. 3 Court");
        Localization court12 = new Localization("Court 12");
        Localization court18 = new Localization("Court 18");

        Timeslot timeslot11am = new Timeslot(1, LocalTime.of(11, 0), Duration.ofHours(1));
        Timeslot timeslot12pm = new Timeslot(1, LocalTime.of(12, 0), Duration.ofHours(1));
        Timeslot timeslot13pm = new Timeslot(1, LocalTime.of(13, 0), Duration.ofHours(1));
        Timeslot timeslot14pm = new Timeslot(1, LocalTime.of(14, 0), Duration.ofHours(1));
        Timeslot timeslot15pm = new Timeslot(1, LocalTime.of(15, 0), Duration.ofHours(1));
        Timeslot timeslot16pm = new Timeslot(1, LocalTime.of(16, 0), Duration.ofHours(1));
        Timeslot timeslot17pm = new Timeslot(1, LocalTime.of(17, 0), Duration.ofHours(1));
        Timeslot timeslot18pm = new Timeslot(1, LocalTime.of(18, 0), Duration.ofHours(1));
        Timeslot timeslot19pm = new Timeslot(1, LocalTime.of(19, 0), Duration.ofHours(1));

        List<Player> atpPlayers = Arrays.asList(
                querry,
                mahut,
                goffin,
                raonic,
                federer,
                johnson,
                cilic,
                nishikori,
                vesely,
                berdych,
                tomic,
                pouille,
                gasquet,
                tsonga,
                kyrgios,
                murray
        );

        List<Localization> wimbledonCourts = Arrays.asList(centreCourt, no1Court, no2Court, no3Court, court12, court18);

        List<Timeslot> monday4July2016 = Arrays.asList(
                timeslot11am,
                timeslot12pm,
                timeslot13pm,
                timeslot14pm,
                timeslot15pm,
                timeslot16pm,
                timeslot17pm,
                timeslot18pm,
                timeslot19pm
        );

        Event gentlemenFourthRound = new Event("Gentlemen R16", atpPlayers, wimbledonCourts, monday4July2016);

        gentlemenFourthRound.setTimeslotsPerMatch(3);

        gentlemenFourthRound.addPlayerInLocalization(federer, centreCourt);
        gentlemenFourthRound.addPlayerInLocalization(murray, centreCourt);

        gentlemenFourthRound.addPlayerAtTimeslotRange(federer, timeslot12pm, timeslot14pm);
        gentlemenFourthRound.addPlayerAtTimeslotRange(murray, timeslot15pm, timeslot17pm);

        gentlemenFourthRound.addMatchup(federer, johnson);
        gentlemenFourthRound.addMatchup(kyrgios, murray);
        gentlemenFourthRound.addMatchup(new Matchup(
                new HashSet<>(Arrays.asList(gasquet, tsonga)),
                new HashSet<>(Arrays.asList(no1Court)),
                new HashSet<>()
        ));
        gentlemenFourthRound.addMatchup(cilic, nishikori);
        gentlemenFourthRound.addMatchup(goffin, raonic);
        gentlemenFourthRound.addMatchup(vesely, berdych);
        gentlemenFourthRound.addMatchup(tomic, pouille);
        gentlemenFourthRound.addMatchup(querry, mahut);


        Player williamsSerena = new Player("S. Williams");
        Player kuznetsova = new Player("S. Kuznetsova");
        Player pavlyuchenkova = new Player("A. Pavlyuchenkova ");
        Player vandeweghe = new Player("C. Vandeweghe");
        Player radwanska = new Player("A. Radwanska");
        Player cibulkova = new Player("D. Cibulkova");
        Player makarova = new Player("E. Makarova");
        Player vesnina = new Player("E. Vesnina");
        Player halep = new Player("S. Halep");
        Player keys = new Player("M. Keys");
        Player doi = new Player("M. Doi");
        Player kerber = new Player("A. Kerber");
        Player williamsVenus = new Player("V. Williams");
        Player suarezNavarro = new Player("C. Suarez Navarro");
        Player shvedova = new Player("Y. Shvedova");
        Player safarova = new Player("L. Safarova");

        List<Player> wtaPlayers = Arrays.asList(
                williamsSerena,
                kuznetsova,
                pavlyuchenkova,
                vandeweghe,
                radwanska,
                cibulkova,
                makarova,
                vesnina,
                halep,
                keys,
                doi,
                kerber,
                williamsVenus,
                suarezNavarro,
                shvedova,
                safarova
        );

        Event ladiesFourthRound = new Event("Lades R16", wtaPlayers, wimbledonCourts, monday4July2016);

        ladiesFourthRound.addMatchup(williamsSerena, kuznetsova);
        ladiesFourthRound.addMatchup(pavlyuchenkova, vandeweghe);
        ladiesFourthRound.addMatchup(radwanska, cibulkova);
        ladiesFourthRound.addMatchup(makarova, vesnina);
        ladiesFourthRound.addMatchup(halep, keys);
        ladiesFourthRound.addMatchup(doi, kerber);
        ladiesFourthRound.addMatchup(williamsVenus, suarezNavarro);
        ladiesFourthRound.addMatchup(shvedova, safarova);

        Tournament wimbledon = new Tournament("Wimbledon 2016", gentlemenFourthRound, ladiesFourthRound);
        tournaments.add(wimbledon);


        Event genericEvent = new Event(
                "Generic Football Event",
                TournamentUtils.buildGenericPlayers(10, "Team"),
                TournamentUtils.buildGenericLocalizations(2, "Field"),
                TournamentUtils.buildDayOfWeekTimeslots(2)
        );
        genericEvent.setTimeslotsPerMatch(1);
        tournaments.add(new Tournament("Generic Football Tournament", genericEvent));
    }

    protected Application configure() {
        ResourceConfig config = new ResourceConfig(EventSchedulerService.class);
        config.register(new Binder());
        config.register(new MalformedJsonMapper());
        config.register(new BadRequestMapper());
        //config.register(new GenericExceptionMapper());
        return config;
    }

    @Test
    public void test() throws JsonProcessingException {
        assertTrue(target(path).request().get(Map.class).isEmpty());

        Response response = target(path).request().post(Entity.entity(tournaments.get(0), MediaType.APPLICATION_JSON));
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        String id = response.readEntity(String.class);
        assertNotNull(id);

        Tournament createdWimbledonTournament = target(path + "/" + id).request().get(Tournament.class);
        assertNotNull(createdWimbledonTournament);
        assertEquals("Wimbledon 2016", createdWimbledonTournament.getName());

        assertEquals(1, target(path + "/ids").request().get(List.class).size());
        assertEquals(1, target(path).request().get(Map.class).size());

        target(path).request().post(Entity.entity(tournaments.get(1), MediaType.APPLICATION_JSON));
        assertEquals(2, target(path + "/ids").request().get(List.class).size());
        assertEquals(2, target(path).request().get(Map.class).size());

        Integer foundSolutions = target(path + "/" + id + "/schedule/found-solutions").request().get(Integer.class);
        assertEquals(0, foundSolutions.intValue());

        String resolutionState = target(path + "/" + id + "/schedule/resolution-state").request().get(String.class);
        assertEquals("\"READY\"", resolutionState);

        response = target(path + "/" + id + "/schedule/resolution-data").request().get();
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());

        target(path + "/" + id + "/schedule").request().get();

        foundSolutions = target(path + "/" + id + "/schedule/found-solutions").request().get(Integer.class);
        assertEquals(1, foundSolutions.intValue());

        resolutionState = target(path + "/" + id + "/schedule/resolution-state").request().get(String.class);
        assertEquals("\"STARTED\"", resolutionState);

        response = target(path + "/" + id + "/schedule/resolution-data").request().get();
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        response = target(path + "/" + id + "/schedules").request().get();
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        foundSolutions = target(path + "/" + id + "/schedule/found-solutions").request().get(Integer.class);
        assertEquals(1, foundSolutions.intValue());

        response = target(path + "/" + id + "/schedules").request().get();
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        foundSolutions = target(path + "/" + id + "/schedule/found-solutions").request().get(Integer.class);
        assertEquals(1, foundSolutions.intValue());

        response = target(path + "/" + id + "/schedules/1").request().get();
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        foundSolutions = target(path + "/" + id + "/schedule/found-solutions").request().get(Integer.class);
        assertEquals(1, foundSolutions.intValue());

        target(path + "/" + id + "/schedule").request().get();

        foundSolutions = target(path + "/" + id + "/schedule/found-solutions").request().get(Integer.class);
        assertEquals(2, foundSolutions.intValue());

        target(path + "/" + id + "/schedule").queryParam("onlyGet", true).request().get();

        foundSolutions = target(path + "/" + id + "/schedule/found-solutions").request().get(Integer.class);
        assertEquals(2, foundSolutions.intValue());

        target(path + "/" + id + "/schedule").queryParam("onlyGet", true).queryParam("restart", true).request().get();

        foundSolutions = target(path + "/" + id + "/schedule/found-solutions").request().get(Integer.class);
        assertEquals(2, foundSolutions.intValue());

        target(path + "/" + id + "/schedule").queryParam("restart", true).request().get();

        foundSolutions = target(path + "/" + id + "/schedule/found-solutions").request().get(Integer.class);
        assertEquals(1, foundSolutions.intValue());

        target(path + "/" + id + "/schedule").queryParam("restart", true)
                .queryParam("searchStrategy", "MINDOM_UB")
                .request()
                .get();

        String resolutionData = target(path + "/" + id + "/schedule/resolution-data").request().get(String.class);
        assertThat(resolutionData, StringContains.containsString("MINDOM_UB"));

        String scheduleStr =
                target(path + "/" + id + "/schedule").queryParam("byLocalizations", true).request().get(String.class);
        assertThat(scheduleStr, StringContains.containsString("CONTINUATION"));

        foundSolutions = target(path + "/" + id + "/schedule/found-solutions").request().get(Integer.class);
        assertEquals(2, foundSolutions.intValue());

        scheduleStr =
                target(path + "/" + id + "/schedules").queryParam("byLocalizations", true).request().get(String.class);
        assertThat(scheduleStr, StringContains.containsString("CONTINUATION"));

        scheduleStr = target(path + "/" + id + "/schedules/2").queryParam("byLocalizations", true)
                .request()
                .get(String.class);
        assertThat(scheduleStr, StringContains.containsString("CONTINUATION"));

        response = target(path + "/" + id + "/schedule").queryParam("restart", true)
                .queryParam("prioritizeTimeslots", true)
                .request()
                .get();
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        response = target(path + "/" + id + "/schedule").queryParam("restart", true)
                .queryParam("limit", 10000)
                .request()
                .get();
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        response = target(path + "/" + id).request().delete();
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());

        response = target(path + "/" + id).request().get();
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    public void createTournamentBadRequestTest() throws JsonProcessingException {
        Response response = target(path).request().post(Entity.json(tournaments.get(1).toJson().substring(1)));
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void getTournamentNotFoundTest() {
        Response response = target(path + "/unknown-id").request().get();
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    public void deleteTournamentNotFoundTest() {
        Response response = target(path + "/unknown-id").request().delete();
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    public void getScheduleNotFoundTest() {
        Response response = target(path + "/unknown-id/schedule").request().get();
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    public void getEventSchedulesNotFoundTest() {
        Response response = target(path + "/unknown-id/schedules").request().get();
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    public void getEventScheduleNotFoundTest() {
        Response response = target(path + "/unknown-id/schedules/1").request().get();
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());

        String id = target(path).request()
                .post(Entity.entity(tournaments.get(0), MediaType.APPLICATION_JSON))
                .readEntity(String.class);

        response = target(path + "/" + id + "/schedules/3").request().get();
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());

        response = target(path + "/" + id + "/schedules/0").request().get();
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    public void getFoundSolutionsNotFoundTest() {
        Response response = target(path + "/unknown-id/schedule/found-solutions").request().get();
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    public void getResolutionStateNotFoundTest() {
        Response response = target(path + "/unknown-id/schedule/resolution-state").request().get();
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    public void getResolutionDataNotFoundTest() {
        Response response = target(path + "/unknown-id/schedule/resolution-data").request().get();
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    public void stopResolutionNotFoundTest() {
        Response response = target(path + "/unknown-id/schedule/stop-resolution").request().get();
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    public void getScheduleBadSearchStrategyTest() {
        String id = target(path).request()
                .post(Entity.entity(tournaments.get(0), MediaType.APPLICATION_JSON))
                .readEntity(String.class);

        Response response =
                target(path + "/" + id + "/schedule").queryParam("searchStrategy", "UNKNOWN").request().get();
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void getScheduleUnfeasibleTest() {
        String id = target(path).request()
                .post(Entity.entity(tournaments.get(1), MediaType.APPLICATION_JSON))
                .readEntity(String.class);

        Response response = target(path + "/" + id + "/schedule").request().get();
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());

        response = target(path + "/" + id + "/schedules").request().get();
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());

        response = target(path + "/" + id + "/schedules/1").request().get();
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
    }
}
