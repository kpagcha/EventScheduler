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

import models.tournaments.Tournament;
import models.tournaments.events.Event;
import models.tournaments.events.entities.Localization;
import models.tournaments.events.entities.Player;
import models.tournaments.events.entities.Team;
import models.tournaments.events.entities.Timeslot;
import solver.TournamentSolver.MatchupMode;

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
		Timeslot[] timeslots = buildTimeslots(8);
		
		Event event = new Event("Categor�a Principal", players, localizations, timeslots);
		
		Map<Player, List<Timeslot>> unavailability = buildUnavailability(
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
		Player[] wtaPlayers = buildPlayers(new String[]{ "Williams", "Radwanska", "Kerber", "Muguruza", "Halep", "Su�rez Navarro", "Kvitova", "Azarenka" });
		Localization[] localizations = buildGenericLocalizations(3, "Pista");
		Timeslot[] timeslots = buildTimeslots(10);
		
		Player[] allPlayers = new Player[atpPlayers.length + wtaPlayers.length];
		for (int i = 0; i < atpPlayers.length; i++) allPlayers[i] = atpPlayers[i];
		for (int i = 0; i < wtaPlayers.length; i++) allPlayers[i + atpPlayers.length] = wtaPlayers[i];
		
		Event mensDraw = new Event("Men's Draw", atpPlayers, localizations, timeslots);
		Event womensDraw = new Event("Women's Draw", wtaPlayers, localizations, timeslots);
		Event doublesDraw = new Event("Mixed Doubles Draw", allPlayers, localizations, timeslots);
		
		// 0 Djokovic-Murray, 1 Federer-Wawrinka, 2 Nadal-Nishikori, 3 Berdych-Ferrer,
		// 4 Williams-Randwanska, 5 Kerber-Muguruza, 6 Halep-Su�rez Navarro, 7 Kvitova-Azarenka
		List<Team> teams = new ArrayList<Team>(Arrays.asList(new Team[]{
			new Team(atpPlayers[0], atpPlayers[1]), new Team(atpPlayers[2], atpPlayers[3]), new Team(atpPlayers[4], atpPlayers[5]),
			new Team(atpPlayers[6], atpPlayers[7]), new Team(wtaPlayers[0], wtaPlayers[1]), new Team(wtaPlayers[2], wtaPlayers[3]),
			new Team(wtaPlayers[4], wtaPlayers[5]), new Team(wtaPlayers[6], wtaPlayers[7])
		}));
		
		doublesDraw.setPlayersPerMatch(4);
		doublesDraw.setTeams(teams);
		
		// Djokovic vs Federer
		mensDraw.addFixedMatchup(new HashSet<Player>(Arrays.asList(new Player[]{ atpPlayers[2], atpPlayers[0] })));
		
		// Nadal vs Ferrer
		mensDraw.addFixedMatchup(new HashSet<Player>(Arrays.asList(new Player[]{ atpPlayers[4], atpPlayers[7] })));
		
		// Federer-Wawrinka vs Kvitova-Azarenka
		doublesDraw.addFixedTeamsMatchup(new HashSet<Team>(Arrays.asList(new Team[]{ teams.get(1), teams.get(7) })));
		
		// Invalidar pista 1 y 2 a las horas t0, t1 y t2 para el cuadro de hombres
		mensDraw.addDiscardedLocalization(localizations[0], timeslots[0]);
		mensDraw.addDiscardedLocalization(localizations[0], timeslots[1]);
		mensDraw.addDiscardedLocalization(localizations[0], timeslots[2]);
		mensDraw.addDiscardedLocalization(localizations[1], timeslots[0]);
		mensDraw.addDiscardedLocalization(localizations[1], timeslots[1]);
		mensDraw.addDiscardedLocalization(localizations[1], timeslots[2]);
		
		// Que Muguruza juegue en la pista 2 en el cuadro de mujeres
		womensDraw.addPlayerInLocalization(wtaPlayers[3], localizations[1]);
		
		
		// Que Murray y Nishikori jueguen en la pista 1 (esto NO quiere decir que Murray y Nishikori se vayan a enfrentar)
		mensDraw.addPlayersInLocalizations(
			new ArrayList<Player>(Arrays.asList(new Player[] {
				atpPlayers[1], atpPlayers[5]
			})),
			new ArrayList<Localization>(Arrays.asList(new Localization[]{
				localizations[0]
			}))
		);
		
		// Que Muguruza juegue en la pista 2 en el cuadro de dobles (por tanto su pareja de dobles tambi�n)
		doublesDraw.addPlayerInLocalizations(
			wtaPlayers[3],
			new ArrayList<Localization>(Arrays.asList(new Localization[]{
				localizations[1]
			}))
		);
		
		// Que Berdych juegue en los timeslots 0, 1, 8, 9
		mensDraw.addPlayerAtTimeslots(
			atpPlayers[6],
			new ArrayList<Timeslot>(Arrays.asList(new Timeslot[]{
				timeslots[0], timeslots[1], timeslots[8], timeslots[9]
			}))
		);
		
		if (randomDrawings) {
			mensDraw.setRandomDrawings(true);
			womensDraw.setRandomDrawings(true);
			doublesDraw.setRandomDrawings(true);
		}
		
		Tournament tournament = new Tournament("Tennis Tournament", new Event[]{ mensDraw, womensDraw, doublesDraw });
		
		// Djokovic no disponible en el cuadro de hombres en los timeslots 6 y 7
		mensDraw.addPlayerUnavailableTimeslots(atpPlayers[0], new ArrayList<Timeslot>(Arrays.asList(timeslots[6], timeslots[7])));
				
		// Djokovic no disponible a la hora 8 en todas las categor�as donde juegue
		tournament.addPlayerUnavailableTimeslot(atpPlayers[0], timeslots[8]);
		
		// Invalidar la pista 3 para el timeslot t8 en todas las categor�as
		tournament.addDiscardedLocalization(localizations[2], timeslots[8]);

		return tournament;
	}

	public Tournament getSampleMediumTennisTournament(boolean randomDrawings) {
		Player[] kids = buildGenericPlayers(8, "Kid");
		Player[] men = buildGenericPlayers(16, "Man");
		Player[] women = buildGenericPlayers(12, "Woman");
		
		Localization[] localizations = buildGenericLocalizations(4, "Pista");
		Timeslot[] timeslots = buildTimeslots(12);
		
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

		Event eventDoubles = new Event(
			"Double's Event", doubles, localizations, doublesTimeslots
		);
		
		eventDoubles.setPlayersPerMatch(4);
		eventDoubles.addBreak(doublesTimeslots[timeslots.length]);
		
		Map<Player, List<Timeslot>> kidsUnavailability = buildUnavailability(
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
		
		Tournament tournament = new Tournament("Medium Tennis Tournament", new Event[]{ eventKids, eventMen, eventWomen, eventDoubles });
		
		tournament.addBreaks(new ArrayList<Timeslot>(Arrays.asList(timeslots[5])));
		
		return tournament;
	}
	
	public Tournament getSampleLargeTennisTournament(boolean randomDrawings) {
		Player[] benjamin = buildGenericPlayers(8, "Benj");
		Player[] alevin = buildGenericPlayers(8, "Alev");
		Player[] infantil = buildGenericPlayers(32, "Inf");
		Player[] cadete = buildGenericPlayers(32, "Cad");
		Player[] junior = buildGenericPlayers(8, "Jun");
		Player[] absoluto = buildGenericPlayers(16, "Abs");
		
		Localization[] localizations = buildGenericLocalizations(8, "Pista");
		Timeslot[] timeslots = buildTimeslots(27); // 2 d�as de 9:00 a 21:00 con descanso a las 14:00 (y la noche entre d1 y d2)
		
		Event categoriaBenjamin = new Event("Categor�a Benjam�n", benjamin, localizations, timeslots);
		Event categoriaAlevin = new Event("Categor�a Alev�n", alevin, localizations, timeslots);
		Event categoriaInfantil = new Event("Categor�a Infantil", infantil, localizations, timeslots);
		Event categoriaCadete = new Event("Categor�a Cadete", cadete, localizations, timeslots);
		Event categoriaJunior = new Event("Categor�a Junior", junior, localizations, timeslots);
		Event categoriaAbsoluto = new Event("Categor�a Absoluto", absoluto, localizations, timeslots);
		
		if (randomDrawings) {
			categoriaBenjamin.setRandomDrawings(true);
			categoriaAlevin.setRandomDrawings(true);
			categoriaInfantil.setRandomDrawings(true);
			categoriaCadete.setRandomDrawings(true);
			categoriaJunior.setRandomDrawings(true);
			categoriaAbsoluto.setRandomDrawings(true);
		}
		
		Tournament tournament = new Tournament(
			"Torneo de tenis", 
			new Event[]{ categoriaBenjamin, categoriaAlevin, categoriaInfantil, categoriaCadete, categoriaJunior, categoriaAbsoluto }
		);
		
		tournament.addBreaks(new ArrayList<Timeslot>(Arrays.asList(timeslots[5], timeslots[13], timeslots[19])));
		
		return tournament;
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
		Timeslot[] timeslots = buildTimeslots(27); // 2 d�as de 9:00 a 21:00 con descanso a las 14:00 (y la noche entre d1 y d2)
		
		Event categoriaBenjamin = new Event("Categor�a Benjam�n", benjamin, localizations, timeslots);
		Event categoriaAlevin = new Event("Categor�a Alev�n", alevin, localizations, timeslots);
		Event categoriaInfantil = new Event("Categor�a Infantil", infantil, localizations, timeslots);
		Event categoriaCadete = new Event("Categor�a Cadete", cadete, localizations, timeslots);
		Event categoriaJunior = new Event("Categor�a Junior", junior, localizations, timeslots);
		Event categoriaAbsoluto = new Event("Categor�a Absoluto", absoluto, localizations, timeslots);
		Event categoriaDobles = new Event("Categor�a Dobles", dobles, localizations, timeslots);
		
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
		
		Tournament tournament = new Tournament(
			"Torneo de tenis", 
			new Event[]{ categoriaBenjamin, categoriaAlevin, categoriaInfantil, categoriaCadete, categoriaJunior, categoriaAbsoluto, categoriaDobles }
		);
		
		tournament.addBreaks(new ArrayList<Timeslot>(Arrays.asList(timeslots[5], timeslots[13], timeslots[19])));
	
		return tournament;
	}
	
	public Tournament getSampleVariableDomainsTournamentWithCollisions(boolean randomDrawings) {
		Player[] players = buildGenericPlayers(50, "Jug");
		Localization[] courts = buildGenericLocalizations(5, "Pista");
		Timeslot[] timeslots = buildTimeslots(24);
		
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
		
		Tournament tournament = new Tournament("Tournament", new Event[]{ groupA, groupB, groupC, groupD, groupLeague, groupDoubles });
		
		tournament.addBreaks(new ArrayList<Timeslot>(Arrays.asList(timeslots[8])));
		
		return tournament;
	}
	
	public Tournament getSampleLeague(boolean randomDrawings) {
		Player[] players = buildPlayers(new String[]{
			"Djokovic", "Murray", "Federer", "Wawrinka", "Nadal", "Nishikori", "Berdych", "Ferrer",
			"Tsonga", "Gasquet", "Cilic", "Raonic", "Goffin", "Thiem", "Isner", "Monfils"
		});
		Localization[] courts = buildGenericLocalizations(16, "Pista");
		Timeslot[] timeslots = buildTimeslots(players.length + 8); // una semana por partido + flexibilidad
		
		Event league = new Event("Liga de 16 jugadores", players, courts, timeslots);
		
		league.setMatchesPerPlayer(players.length - 1);
		league.setMatchDuration(1); // partido en 1 d�a
		
		/*league.setUnavailableTimeslots(buildUnavailability(league, new int[][]{
			{ 3 }, { }, { }, { 0 }, { }, { }, { 8 }, { },
			{ }, { 2 }, { }, { }, { }, { 5, 6 }, { }, { }
		}));*/
		
		// Federer vs Nadal
		//league.addFixedMatchup(new HashSet<Player>(Arrays.asList(new Player[]{ players[2], players[4] })));
		
		league.setMatchupMode(MatchupMode.ALL_DIFFERENT);
		
		if (randomDrawings)
			league.setRandomDrawings(randomDrawings);
		
		return new Tournament("Liga", new Event[]{ league });
	}
	
	public Tournament getSampleSmallLeague(boolean randomDrawings) {
		Player[] players = buildPlayers(new String[]{
			"Djokovic", "Murray", "Federer", "Wawrinka"
		});
		Localization[] courts = buildGenericLocalizations(1, "Pista");
		Timeslot[] timeslots = buildTimeslots(6);
		
		Event league = new Event("Liga", players, courts, timeslots);
		
		league.setMatchesPerPlayer(3);
		league.setMatchDuration(1);
		
		league.setMatchupMode(MatchupMode.ALL_DIFFERENT);
		
		if (randomDrawings)
			league.setRandomDrawings(randomDrawings);
		
		return new Tournament("Liga", new Event[]{ league });
	}
	
	public Tournament getSampleBigTournament(boolean randomDrawings) {
		Player[] players = buildGenericPlayers(64, "J");
		Localization[] localizations = buildGenericLocalizations(10, "Pista");
		Timeslot[] timeslots = buildTimeslots(13);
		
		Event event = new Event("Evento", players, localizations, timeslots);
		
		return new Tournament("Torneo", new Event[]{ event });
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
	
	private Timeslot[] buildTimeslots(int[] timeslotsArray) {
		Timeslot[] timeslots = new Timeslot[timeslotsArray.length];
		int oneHour = 60 * 60 * 1000;
		for (int i = 0; i < timeslotsArray.length; i++) {
			timeslots[i] = new Timeslot(oneHour * i, oneHour * (i + 1));
		}				
		return timeslots;
	}
	
	private Timeslot[] buildTimeslots(int nTimeslots) {
		int[] timeslots = new int[nTimeslots];
		for (int i = 0; i < nTimeslots; i++)
			timeslots[i] = i;
		return buildTimeslots(timeslots);
	}
	
	private Map<Player, List<Timeslot>> buildUnavailability(Event event, int[][] unavailabilityArray) {
		Player[] players = event.getPlayers();
		Map<Player, List<Timeslot>> unavailability = new HashMap<Player, List<Timeslot>>(players.length);
		for (int p = 0; p < unavailabilityArray.length; p++) {
			if (unavailabilityArray[p].length > 0) {
				List<Timeslot> playerUnavailability = new ArrayList<Timeslot>(unavailabilityArray[p].length);
				for (int timeslot : unavailabilityArray[p])
					playerUnavailability.add(event.getTimeslotAt(timeslot));
				
				unavailability.put(players[p], playerUnavailability);
			}
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
