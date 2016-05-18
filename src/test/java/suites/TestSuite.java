package suites;

import data.model.schedule.LocalizationScheduleTest;
import data.model.schedule.MatchTest;
import data.model.schedule.ScheduleTest;
import data.model.tournament.*;
import data.validation.ValidationTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import solver.TournamentSolverTest;
import utils.TournamentUtilsTest;

@RunWith(Suite.class)
@SuiteClasses({ EventTest.class, EntityTest.class, LocalizationTest.class, PlayerTest.class, TeamTest.class,
        TimeslotTest.class, TournamentTest.class, ScheduleTest.class, MatchTest.class, LocalizationScheduleTest.class,
        ValidationTest.class, TournamentSolverTest.class, TournamentUtilsTest.class
})
public class TestSuite {
    // clase vacía, solamente se usa para las anotaciones de arriba
}
