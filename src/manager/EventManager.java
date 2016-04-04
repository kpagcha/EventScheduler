package manager;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Stream;

import models.Event;
import models.Localization;
import models.Player;
import models.Team;
import models.Timeslot;
import models.Tournament;

public class EventManager {
	private static EventManager instance = null;
	
	private EventManager() { }
	
	public static EventManager getInstance() {
		if (instance == null)
			instance = new EventManager();
		return instance;
	}
	
	public Tournament getSampleOneCategoryTournament(boolean randomDrawings) {
		Player[] players = buildGenericPlayers(8, "Player");
		Localization[] localizations = buildGenericLocalizations(2, "Pista");
		Timeslot[] timeslots = buildTimeslots(8, new int[]{});
		
		Event event = new Event("Categoría Principal", players, localizations, timeslots);
		
		Map<Player, Timeslot[]> unavailability = buildUnavailability(
			event,
			new int[][]{
				{ 5, 6, 7 },
				{ 6, 7  },
				{ 0, 1, 2, 3 },
				{ 3, 4 },
				{ 4, 5, 7 },
				{ 1, 2},
				{ 5, 6 },
				{ 0 } 
			}
		);
		
		event.setUnavailableTimeslots(unavailability);
		event.setMatchesPerPlayer(2);
		
		if (randomDrawings)
			event.setRandomDrawings(true);
		
		return new Tournament("Torneo", new Event[]{ event });
	}
	
	public Tournament getSampleTennisTournament(boolean randomDrawings) {
		Player[] atpPlayers = buildPlayers(new String[]{ "Djokovic", "Murray", "Federer", "Wawrinka", "Nadal", "Nishikori", "Berdych", "Ferrer" });
		Player[] wtaPlayers = buildPlayers(new String[]{ "Williams", "Radwanska", "Kerber", "Muguruza", "Halep", "Suárez Navarro", "Kvitova", "Azarenka" });
		Localization[] localizations = buildGenericLocalizations(3, "Pista");
		Timeslot[] timeslots = buildTimeslots(10, new int[]{ });
		
		Player[] allPlayers = new Player[atpPlayers.length + wtaPlayers.length];
		for (int i = 0; i < atpPlayers.length; i++) allPlayers[i] = atpPlayers[i];
		for (int i = 0; i < wtaPlayers.length; i++) allPlayers[i + atpPlayers.length] = wtaPlayers[i];
		
		Event mensDraw = new Event("Men's Draw", atpPlayers, localizations, timeslots);
		Event womensDraw = new Event("Women's Draw", wtaPlayers, localizations, timeslots);
		Event doublesDraw = new Event("Mixed Doubles Draw", allPlayers, localizations, timeslots);
		
		List<Team> teams = new ArrayList<Team>(Arrays.asList(new Team[]{
			new Team(atpPlayers[0], atpPlayers[1]), new Team(atpPlayers[2], atpPlayers[3]), new Team(atpPlayers[4], atpPlayers[5]),
			new Team(atpPlayers[6], atpPlayers[7]), new Team(wtaPlayers[0], wtaPlayers[1]), new Team(wtaPlayers[2], wtaPlayers[3]),
			new Team(wtaPlayers[4], wtaPlayers[5]), new Team(wtaPlayers[6], wtaPlayers[7])
		}));
		
		doublesDraw.setPlayersPerMatch(4);
		doublesDraw.setTeams(teams);
		
		mensDraw.addFixedMatchup(new HashSet<Player>(Arrays.asList(new Player[]{ atpPlayers[2], atpPlayers[0] }))); // Djokovic vs Federer
		mensDraw.addFixedMatchup(new HashSet<Player>(Arrays.asList(new Player[]{ atpPlayers[4], atpPlayers[7] }))); // Nadal vs Ferrer
		
		/*doublesDraw.addFixedMatchup(new HashSet<Player>(Arrays.asList(new Player[]{ 
			atpPlayers[2], atpPlayers[3], wtaPlayers[6], wtaPlayers[7] // Federer-Wawrinka vs Kvitova-Azarenka
		})));*/
		
		// Lo mismo que arriba
		doublesDraw.addFixedTeamsMatchup(new HashSet<Team>(Arrays.asList(new Team[]{ teams.get(1), teams.get(7) })));
		
		// Invalidar pista 1 y 2 a las horas t0, t1 y t2 para el cuadro de hombres
		mensDraw.addDiscardedCourt(localizations[0], timeslots[0]);
		mensDraw.addDiscardedCourt(localizations[0], timeslots[1]);
		mensDraw.addDiscardedCourt(localizations[0], timeslots[2]);
		mensDraw.addDiscardedCourt(localizations[1], timeslots[0]);
		mensDraw.addDiscardedCourt(localizations[1], timeslots[1]);
		mensDraw.addDiscardedCourt(localizations[1], timeslots[2]);
		
		Tournament tournament = new Tournament("Tennis Tournament", new Event[]{ mensDraw, womensDraw, doublesDraw });
		
		// Invalidar la pista 3 para el timeslot t8 en todas las categorías
		tournament.addDiscardedCourt(localizations[2], timeslots[8]);
		
		if (randomDrawings) {
			mensDraw.setRandomDrawings(true);
			womensDraw.setRandomDrawings(true);
			doublesDraw.setRandomDrawings(true);
		}
		
		return tournament;
	}

	public Tournament getSampleMediumTennisTournament(boolean randomDrawings) {
		Player[] kids = buildGenericPlayers(8, "Kid");
		Player[] men = buildGenericPlayers(16, "Man");
		Player[] women = buildGenericPlayers(12, "Woman");
		
		Localization[] localizations = buildGenericLocalizations(4, "Pista");
		Timeslot[] timeslots = buildTimeslots(12, new int[]{ 5 });
		
		Event eventKids = new Event(
			"Kids' Category",
			kids, new Localization[]{ localizations[0], localizations[1], localizations[2], localizations[3] }, 
			timeslots
		);
		
		Event eventMen = new Event("Men's Category", men, localizations, timeslots);
		
		Event eventWomen = new Event("Women's Category", women, localizations, timeslots);
		
		Player[] doubles = new Player[men.length + women.length];
		for (int i = 0; i < men.length; i++) doubles[i] = men[i];
		for (int i = 0; i < women.length; i++) doubles[i + men.length] = women[i];
		
		Timeslot[] doublesTimeslots = new Timeslot[timeslots.length + 7];
		for (int i = 0; i < timeslots.length; i++) doublesTimeslots[i] = timeslots[i];
		for (int i = timeslots.length; i < doublesTimeslots.length; i++) doublesTimeslots[i] = new Timeslot(3600000 * i, 3600000 * (i + 1));
		doublesTimeslots[timeslots.length].setIsBreak(true);
		
		Event eventDoubles = new Event(
			"Double's Event", doubles, localizations, doublesTimeslots
		);
		
		eventDoubles.setPlayersPerMatch(4);
		
		Map<Player, Timeslot[]> kidsUnavailability = buildUnavailability(
			eventKids,
			new int[][]{
				{ 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 },
				{ 2, 3 },
				{ 0, 1 },
				{ 9, 10, 11 },
				{ },
				{ },
				{ 5, 6 },
				{ 0 } 
			}
		);
		
		eventKids.setUnavailableTimeslots(kidsUnavailability);
		
		if (randomDrawings) {
			eventKids.setRandomDrawings(true);
			eventMen.setRandomDrawings(true);
			eventWomen.setRandomDrawings(true);
			eventDoubles.setRandomDrawings(true);
		}
		
		return new Tournament("Medium Tennis Tournament", new Event[]{ eventKids, eventMen, eventWomen, eventDoubles });
	}
	
	public Tournament getSampleLargeTennisTournament(boolean randomDrawings) {
		Player[] benjamin = buildGenericPlayers(8, "Benj");
		Player[] alevin = buildGenericPlayers(8, "Alev");
		Player[] infantil = buildGenericPlayers(32, "Inf");
		Player[] cadete = buildGenericPlayers(32, "Cad");
		Player[] junior = buildGenericPlayers(8, "Jun");
		Player[] absoluto = buildGenericPlayers(16, "Abs");
		
		Localization[] localizations = buildGenericLocalizations(8, "Pista");
		Timeslot[] timeslots = buildTimeslots(27, new int[]{ 5, 13, 19 }); // 2 días de 9:00 a 21:00 con descanso a las 14:00 (y la noche entre d1 y d2)
		
		Event categoriaBenjamin = new Event("Categoría Benjamín", benjamin, localizations, timeslots);
		Event categoriaAlevin = new Event("Categoría Alevín", alevin, localizations, timeslots);
		Event categoriaInfantil = new Event("Categoría Infantil", infantil, localizations, timeslots);
		Event categoriaCadete = new Event("Categoría Cadete", cadete, localizations, timeslots);
		Event categoriaJunior = new Event("Categoría Junior", junior, localizations, timeslots);
		Event categoriaAbsoluto = new Event("Categoría Absoluto", absoluto, localizations, timeslots);
		
		if (randomDrawings) {
			categoriaBenjamin.setRandomDrawings(true);
			categoriaAlevin.setRandomDrawings(true);
			categoriaInfantil.setRandomDrawings(true);
			categoriaCadete.setRandomDrawings(true);
			categoriaJunior.setRandomDrawings(true);
			categoriaAbsoluto.setRandomDrawings(true);
		}
		
		return new Tournament(
			"Torneo de tenis", 
			new Event[]{ categoriaBenjamin, categoriaAlevin, categoriaInfantil, categoriaCadete, categoriaJunior, categoriaAbsoluto }
		);
	}
	
	public Tournament getSampleLargeTennisTournamentWithCollisions(boolean randomDrawings) {
		Player[] benjamin = buildGenericPlayers(8, "Benj");
		Player[] alevin = buildGenericPlayers(8, "Alev");
		Player[] infantil = buildGenericPlayers(32, "Inf");
		Player[] cadete = buildGenericPlayers(32, "Cad");
		Player[] junior = buildGenericPlayers(8, "Jun");
		Player[] absoluto = buildGenericPlayers(16, "Abs");
		for (int i = 0; i < 8; i++) absoluto[i] = junior[i];
		
		Player[] dobles = new Player[32];
		for (int i = 0; i < 16; i++) {
			dobles[i] = cadete[i];
			dobles[i + 16] = absoluto[i];
		}
		
		Localization[] localizations = buildGenericLocalizations(8, "Pista");
		Timeslot[] timeslots = buildTimeslots(27, new int[]{ 5, 13, 19 }); // 2 días de 9:00 a 21:00 con descanso a las 14:00 (y la noche entre d1 y d2)
		
		Event categoriaBenjamin = new Event("Categoría Benjamín", benjamin, localizations, timeslots);
		Event categoriaAlevin = new Event("Categoría Alevín", alevin, localizations, timeslots);
		Event categoriaInfantil = new Event("Categoría Infantil", infantil, localizations, timeslots);
		Event categoriaCadete = new Event("Categoría Cadete", cadete, localizations, timeslots);
		Event categoriaJunior = new Event("Categoría Junior", junior, localizations, timeslots);
		Event categoriaAbsoluto = new Event("Categoría Absoluto", absoluto, localizations, timeslots);
		Event categoriaDobles = new Event("Categoría Dobles", dobles, localizations, timeslots);
		
		categoriaDobles.setPlayersPerMatch(4);
		
		if (randomDrawings) {
			categoriaBenjamin.setRandomDrawings(true);
			categoriaAlevin.setRandomDrawings(true);
			categoriaInfantil.setRandomDrawings(true);
			categoriaCadete.setRandomDrawings(true);
			categoriaJunior.setRandomDrawings(true);
			categoriaAbsoluto.setRandomDrawings(true);
			categoriaDobles.setRandomDrawings(true);
		}
		
		return new Tournament(
			"Torneo de tenis", 
			new Event[]{ categoriaBenjamin, categoriaAlevin, categoriaInfantil, categoriaCadete, categoriaJunior, categoriaAbsoluto, categoriaDobles }
		);
	}
	
	public Tournament getSampleVariableDomainsTournamentWithCollisions(boolean randomDrawings) {
		Player[] players = buildGenericPlayers(50, "Jug");
		Localization[] courts = buildGenericLocalizations(5, "Pista");
		Timeslot[] timeslots = buildTimeslots(24, new int[]{ 8 });
		
		Player[] groupAPlayers = Arrays.copyOfRange(players, 0, 8);
		Player[] groupBPlayers = Arrays.copyOfRange(players, 8, 24);
		Player[] groupCPlayers = Arrays.copyOfRange(players, 24, 36);
		Player[] groupDPlayers = Arrays.copyOfRange(players, 36, 50);
		Player[] leaguePlayers = buildRandomSubset(20, players, Player.class);
		Player[] doublePlayers = Stream.concat(
			Stream.concat(Arrays.stream(groupAPlayers), Arrays.stream(groupBPlayers)),
			Arrays.stream(buildRandomSubset(8, groupCPlayers, Player.class))
		).toArray(Player[]::new);
		
		Event groupA = new Event("Group A", groupAPlayers, new Localization[]{ courts[0] }, timeslots);
		Event groupB = new Event("Group B", groupBPlayers, courts, timeslots);
		Event groupC = new Event("Group C", groupCPlayers, courts, timeslots);
		Event groupD = new Event("Group D", groupDPlayers, courts, timeslots);
		Event groupLeague = new Event("League", leaguePlayers, courts, timeslots);
		Event groupDoubles = new Event("Doubles", doublePlayers, courts, timeslots);
		
		groupDoubles.setPlayersPerMatch(4);
		
		if (randomDrawings) {
			groupA.setRandomDrawings(true);
			groupB.setRandomDrawings(true);
			groupC.setRandomDrawings(true);
			groupD.setRandomDrawings(true);
			groupLeague.setRandomDrawings(true);
			groupDoubles.setRandomDrawings(true);
		}
		
		return new Tournament("Tournament", new Event[]{ groupA, groupB, groupC, groupD, groupLeague, groupDoubles });
	}
	
	private Player[] buildPlayers(String[] playersArray) {
		Player[] players = new Player[playersArray.length];
		for (int i = 0; i < playersArray.length; i++)
			players[i] = new Player(playersArray[i]);
		return players;
	}
	
	private Player[] buildGenericPlayers(int n, String placeholder) {
		if (placeholder.isEmpty())
			placeholder = "Player";
		
		Player[] players = new Player[n];
		for (int i = 0; i < n; i++)
			players[i] = new Player(placeholder + " " + (i + 1));
		return players;
	}
	
	@SuppressWarnings("unused")
	private Localization[] buildLocalizations(int[] courtsArray) {
		Localization[] localizations = new Localization[courtsArray.length];
		for (int i = 0; i < courtsArray.length; i++)
			localizations[i] = new Localization("Court " + (i + 1));
		return localizations;
	}
	
	private Localization[] buildGenericLocalizations(int n, String placeholder) {
		if (placeholder.isEmpty())
			placeholder = "Court";
		
		Localization[] localizations = new Localization[n];
		for (int i = 0; i < n ; i++)
			localizations[i] = new Localization(placeholder + " " + (i + 1));
		return localizations;
	}
	
	private Timeslot[] buildTimeslots(int[] timeslotsArray, int[] breaks) {
		Timeslot[] timeslots = new Timeslot[timeslotsArray.length];
		int oneHour = 60 * 60 * 1000;
		for (int i = 0; i < timeslotsArray.length; i++) {
			timeslots[i] = new Timeslot(oneHour * i, oneHour * (i + 1));
			for (int j = 0; j < breaks.length; j++)
				if (i == breaks[j]) {
					timeslots[i].setIsBreak(true);
					break;
				}
		}				
		return timeslots;
	}
	
	private Timeslot[] buildTimeslots(int nTimeslots, int[] breaks) {
		int[] timeslots = new int[nTimeslots];
		for (int i = 0; i < nTimeslots; i++)
			timeslots[i] = i;
		return buildTimeslots(timeslots, breaks);
	}
	
	private Map<Player, Timeslot[]> buildUnavailability(Event event, int[][] unavailabilityArray) {
		Player[] players = event.getPlayers();
		Map<Player, Timeslot[]> unavailability = new HashMap<Player, Timeslot[]>(players.length);
		for (int p = 0; p < unavailabilityArray.length; p++) {
			Player player = players[p];
			Timeslot[] playerUnavailability = new Timeslot[unavailabilityArray[p].length];
			
			int t = 0;
			for (int timeslot : unavailabilityArray[p])
				playerUnavailability[t++] = event.getTimeslotAt(timeslot);
			
			unavailability.put(player, playerUnavailability);
		}
		return unavailability;
	}
	
	@SuppressWarnings("unchecked")
	private <T> T[] buildRandomSubset(int subsetSize, T[] pool, Class<T> c) {
		List<T> poolList = new ArrayList<T>(Arrays.asList(pool));
		
		T[] subset = (T[]) Array.newInstance(c, subsetSize);
		Random rand = new Random();
		
		for (int i = 0; i < subsetSize; i++) {
			int randIndex = rand.nextInt(poolList.size());
			subset[i] = poolList.get(randIndex);
			
			poolList.remove(randIndex);
		}
		
		return subset;
	}
}
