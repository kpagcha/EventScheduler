import java.util.List;
import java.util.Scanner;

import data.model.schedule.TournamentSchedule;
import data.model.schedule.GroupedSchedule;
import data.model.schedule.data.Match;
import data.model.tournament.Tournament;
import data.model.tournament.event.entity.Player;
import data.validation.validable.ValidationException;
import solver.TournamentSolver.SearchStrategy;
import utils.TournamentUtils;

public class Application {
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
	
		System.out.println("0 Zarlon");
		System.out.println("1 Tournament");
		System.out.println("2 ATP");
		System.out.println("3 Different domains");
		System.out.print("Choose tournament: ");
		int tournamentOption = sc.nextInt();
		
		Tournament t = null;
		switch (tournamentOption) {
			case 0:
				t = TournamentUtils.getZarlonTournament();
				break;
			case 1:
				t = TournamentUtils.getSampleTournament();
				break;
			case 2:
				t = TournamentUtils.getSampleAtp();
				break;
			case 3:
				t = TournamentUtils.getSampleWithDifferentDomains();
				break;
			default:
				t = TournamentUtils.getSampleTournament();
				break;
		}
		
		System.out.println("\n1 domOverWDeg");
		System.out.println("2 minDom_UB");
		System.out.println("3 minDom_LB");
		System.out.print("Choose Search Strategy: ");
		int searchStrategyOption = sc.nextInt();
		
		SearchStrategy searchStrategy = null;
		switch (searchStrategyOption) {
		case 1:
			searchStrategy = SearchStrategy.DOMOVERWDEG;
			break;
		case 2:
			searchStrategy = SearchStrategy.MINDOM_UB;
			break;
		case 3:
			searchStrategy = SearchStrategy.MINDOM_LB;
			break;
		}

		t.getSolver().setSearchStrategy(searchStrategy);
		
		
		final Tournament tournament = t;
		
		boolean printSolutions = true;
		boolean printMatches = true;
		boolean askForInput = false;
		boolean printMatchesByPlayer = false;
		int maxSolutions = 1; // 0 -> todas las soluciones
		int foundSolutions = 0;
		
		/*Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				System.out.print("Stop resolution process? (Y/N): ");
				String answer = sc.next();
				if (answer.equalsIgnoreCase("y")) {
					tournament.getSolver().stopResolutionProcess();
				}
			}
		});
		try {
			thread.start();
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}*/
		
		boolean solutionFound = false;
		try {
			solutionFound = tournament.solve();
		} catch (ValidationException e) {
			for (String err : t.getMessages())
				System.out.println(err);
		}
		
		if (solutionFound) {
			do {
				if (printSolutions) {
					System.out.println("-------------------------------------------------------");
					System.out.println(tournament + "\n");
					tournament.printCurrentSchedules(printMatches);
					
					if (tournament.getCurrentSchedules() != null) {
						TournamentSchedule combinedSchedule = tournament.getSchedule();
					
						System.out.println("All schedules combined in one");
						System.out.println(combinedSchedule);
						
						if (printMatches) {
							List<Match> matches = combinedSchedule.getMatches();
							System.out.println("All matches (" + matches.size() + ")");
							for (Match match : matches)
								System.out.println(match);
							System.out.println();
						}
						
						GroupedSchedule groupedSchedule = new GroupedSchedule(tournament);
						System.out.println("Combined schedule grouped by courts");
						System.out.println(groupedSchedule);
						
						int occupation = groupedSchedule.getOccupation();
						int availableTimeslots = groupedSchedule.getAvailableTimeslots();
						System.out.println(
							String.format("Timeslot (%s) occupation: %s/%s (%s %%)\n",
								groupedSchedule.getTotalTimeslots(),
								occupation,
								availableTimeslots,
								(occupation / (double)availableTimeslots) * 100
							)
						);
						
						if (printMatchesByPlayer) {
							for (Player player : tournament.getAllPlayers()) {
								System.out.println(player + " matches:");
								for (Match match : combinedSchedule.getMatchesByPlayer(player))
									System.out.println(match);
								System.out.println();
							}
						}
					}
				}
				
				if (solutionFound)
					foundSolutions++;
			
				if (askForInput) {
					System.out.print("Show next solution (y/n)?: ");
					String input = sc.next();
					if (!input.equalsIgnoreCase("y"))
						break;
				}
				
				if (maxSolutions > 0 && foundSolutions >= maxSolutions)
					break;
			
			} while (tournament.nextSchedules());
		}
		
		sc.close();
		
		System.out.println("\n" + foundSolutions + " solutions found.");
	}
}
