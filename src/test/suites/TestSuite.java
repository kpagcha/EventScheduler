package suites;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import data.model.schedule.GroupedScheduleTest;
import data.model.schedule.MatchTest;
import data.model.schedule.ScheduleTest;
import data.model.tournament.TournamentTest;
import data.model.tournament.event.TestEvent;
import data.model.tournament.event.entity.EntityTest;
import data.model.tournament.event.entity.LocalizationTest;
import data.model.tournament.event.entity.PlayerTest;
import data.model.tournament.event.entity.TeamTest;
import data.model.tournament.event.entity.TimeslotTest;

@RunWith(Suite.class)
@SuiteClasses({
	TestEvent.class,
	EntityTest.class,
	LocalizationTest.class,
	PlayerTest.class,
	TeamTest.class,
	TimeslotTest.class,
	TournamentTest.class,
	ScheduleTest.class,
	MatchTest.class,
	GroupedScheduleTest.class
})
public class TestSuite {
	// clase vacía, solamente se usa para las anotaciones de arriba
}
