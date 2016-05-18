package suites;

import data.model.schedule.LocalizationScheduleTest;
import data.model.schedule.MatchTest;
import data.model.schedule.ScheduleTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ ScheduleTest.class, LocalizationScheduleTest.class, MatchTest.class
})
public class ScheduleSuit {
    // clase vacía, solamente se usa para las anotaciones de arriba
}