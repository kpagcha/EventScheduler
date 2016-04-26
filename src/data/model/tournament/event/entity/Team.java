package data.model.tournament.event.entity;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import solver.TournamentSolver;

/**
 * Un equipo es una composici�n de jugadores, como m�nimo dos de ellos.
 * <p>
 * Los jugadores que pertenecen a un mismo equipo toman parte en los mismos enfrentamientos de las categor�as
 * en las que participan y se enfrentan, en conjunto, a otros participantes.
 * <p>Se diferencian de la clase {@link Player} en que, mientras esta
 * �ltima define un equipo como una �nica entidad que juega, esta clase considera los jugadores individuales
 * de los que est� compuesto.
 * <p>
 * El principal objetivo de esta distinci�n es permitir a jugadores participar en potencialmente distintas
 * categor�as, tanto siendo parte de equipos como individualmente, de modo que el solucionador del problema,
 * {@link TournamentSolver}, tenga en cuenta la participaci�n del jugador como entidad individual en m�ltiples
 * categor�as.
 *
 */
public class Team extends Entity {
	/**
	 * Conjunto de jugadores que componene el equipo
	 */
	private Set<Player> players;
	
	/**
	 * Constructor de un equipo, al que le asigna un nombre e inicializa el conjunto de jugadores de los que se compone.
	 * 
	 * @param name una cadena no nula
	 * @param players un conjunto con m�s dos de jugadores, no <code>null</code>
	 * @throws IllegalArgumentException si <code>players</code> es <code>null</code>, o si alguno sus elementos son
	 * <code>null</code> o si su tama�o es inferior a 2
	 */
	public Team(String name, Set<Player> players) {
		super(name);
		
		if (players == null)
			throw new IllegalArgumentException("Players cannot be null");
		
		if (players.size() < 2)
			throw new IllegalArgumentException("A team cannot have less than two players");
		
		if (players.contains(null))
			throw new IllegalArgumentException("A team cannot contain a null player");
		
		this.players = players;
	}
	
	/**
	 * Construye un equipo a partir de un array de jugadores
	 * 
	 * @param playersArray array de jugadores
	 */
	public Team(Player... playersArray) {
		this(StringUtils.join(playersArray, "-"), new HashSet<Player>(Arrays.asList(playersArray)));
	}
	
	public Set<Player> getPlayers() {
		return players;
	}
	
	/**
	 * Comprueba si este equipo contiene un jugador
	 * 
	 * @param player un jugador cualquier
	 * @return <code>true</code> si el jugador pertenece a este equipo, de lo contrario, <code>false</code>
	 */
	public boolean contains(Player player) {
		return players.contains(player);
	}
	
	public String toString() {
		return name;
	}
}
