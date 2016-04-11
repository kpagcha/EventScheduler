package data.model.tournament.event.entity.timeslot;

import data.model.tournament.event.entity.Entity;

/**
 * Representa un timeslot abstracto sin representación temporal, simplemente un hueco en un horario
 * <p>
 * Define un orden cronológico que indica la posición del timeslot que permita compararlo con otras
 * instancias de la misma clase. Por ejemplo, para dos timeslots con los períodos '1 día' y '2 días',
 * si el primero define un orden de 1 y el segundo de 2, el primero será anterior al segundo, es decir, mayor.
 * <p>
 * El orden cronológico permitirá el empleo de esta clase para las definiciones de dominios temporales en
 * horarios y planificaciones. Por ejemplo, en un horario con un total de diez timeslots anónimos de una
 * duración de media hora, se podrá identificar el orden del transcurso de cada uno de estos perídos
 * por medio del orden cronológico asociado a cada timeslot anónimo.
 *
 */
public class AbstractTimeslot extends Entity implements Timeslot {
	/**
	 * Orden cronológico o jerárquico que indica la posición de este timeslot en una jerarquía
	 * indefinida de timeslots anónimos. La maginitud de prioridad es inversa al valor numérico (1 > 2)
	 */
	protected final int chronologicalOrder;
	
	public AbstractTimeslot(final int chronologicalOrder) {
		this.chronologicalOrder = chronologicalOrder;
	}

	@Override
	public int getChronologicalOrder() {
		return chronologicalOrder;
	}
	
	public int compareTo(Timeslot o) {
		AbstractTimeslot timeslot = (AbstractTimeslot)o;
		if (chronologicalOrder < timeslot.chronologicalOrder)
			return 1;
		else if (chronologicalOrder > timeslot.chronologicalOrder)
			return -1;
		return 0;
	}
	
	public String toString() {
		return Integer.toString(chronologicalOrder);
	}
}
