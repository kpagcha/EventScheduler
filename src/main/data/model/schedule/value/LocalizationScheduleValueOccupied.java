package data.model.schedule.value;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * Representa un valor de horario agrupado por localizaciones ocupado, donde en una localización concreta a una hora determinada
 * un conjunto de jugadores especificado por el valor que esta clase almacena se enfrentan en un partido.
 *
 */
public class LocalizationScheduleValueOccupied extends LocalizationScheduleValue {
	protected static final List<ScheduleValue> possibleValues = Collections.unmodifiableList(new ArrayList<>(Arrays.asList(OCCUPIED)));
	
	/**
	 * Índices de los jugadores que se enfrentan en un partido
	 */
	private List<Integer> playersIndices;

	public LocalizationScheduleValueOccupied(List<Integer> playersIndices) {
		super(OCCUPIED);
		
		if (playersIndices == null)
			throw new IllegalArgumentException("Players indices cannot be null");
		
		if (playersIndices.contains(null))
			throw new IllegalArgumentException("Player index cannot be null");
		
		for (int i = 0; i < playersIndices.size() - 1; i++)
			for (int j = i + 1; j < playersIndices.size(); j++)
				if (playersIndices.get(i).equals(playersIndices.get(j)))
					throw new IllegalArgumentException("Player indices cannot be duplicated; " + playersIndices.get(i) + " is");
		
		this.playersIndices = playersIndices;
	}
	
	protected List<ScheduleValue> getPossibleValues() {
		return possibleValues;
	}
	
	public List<Integer> getPlayers() {
		return playersIndices;
	}

	public String toString() {
		return  StringUtils.join(playersIndices, ",");
	}
	
	public boolean equals(Object o) {
		if (o == null)
			return false;
		
		return o instanceof LocalizationScheduleValueOccupied && ((LocalizationScheduleValueOccupied)o).getPlayers().equals(playersIndices);
	}
}
