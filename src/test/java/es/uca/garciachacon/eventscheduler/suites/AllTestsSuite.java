package es.uca.garciachacon.eventscheduler.suites;

import es.uca.garciachacon.eventscheduler.data.model.schedule.LocalizationScheduleTest;
import es.uca.garciachacon.eventscheduler.data.model.schedule.MatchTest;
import es.uca.garciachacon.eventscheduler.data.model.schedule.ScheduleTest;
import es.uca.garciachacon.eventscheduler.data.model.tournament.*;
import es.uca.garciachacon.eventscheduler.data.validation.ValidationTest;
import es.uca.garciachacon.eventscheduler.solver.TournamentSolverTest;
import es.uca.garciachacon.eventscheduler.utils.TournamentUtilsTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ EventTest.class, EntityTest.class, LocalizationTest.class, PlayerTest.class, TeamTest.class,
        TimeslotTest.class, TournamentTest.class, ScheduleTest.class, MatchTest.class, LocalizationScheduleTest.class,
        ValidationTest.class, TournamentSolverTest.class, TournamentUtilsTest.class, MatchupTest.class
})
public class AllTestsSuite {
    // clase vacía, solamente se usa para las anotaciones de arriba
}
