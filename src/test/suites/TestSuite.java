package suites;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import data.model.schedule.GroupedScheduleTest;
import data.model.schedule.MatchTest;
import data.model.schedule.ScheduleTest;
import data.model.tournament.EntityTest;
import data.model.tournament.LocalizationTest;
import data.model.tournament.PlayerTest;
import data.model.tournament.TeamTest;
import data.model.tournament.EventTest;
import data.model.tournament.TimeslotTest;
import data.model.tournament.TournamentTest;
import data.validation.ValidationTest;
import solver.TournamentSolverTest;

@RunWith(Suite.class)
@SuiteClasses({
	EventTest.class,
	EntityTest.class,
	LocalizationTest.class,
	PlayerTest.class,
	TeamTest.class,
	TimeslotTest.class,
	TournamentTest.class,
	ScheduleTest.class,
	MatchTest.class,
	GroupedScheduleTest.class,
	ValidationTest.class,
	TournamentSolverTest.class
})
public class TestSuite {
	// clase vacía, solamente se usa para las anotaciones de arriba
}
