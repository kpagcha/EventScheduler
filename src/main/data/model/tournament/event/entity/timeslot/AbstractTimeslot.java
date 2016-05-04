package data.model.tournament.event.entity.timeslot;

import data.model.tournament.event.entity.Entity;

/**
 * Representa un timeslot abstracto sin representaci�n temporal, simplemente un hueco en un horario
 * <p>
 * Define un orden cronol�gico que indica la posici�n del timeslot que permita compararlo con otras
 * instancias de la misma clase. Por ejemplo, para dos timeslots con los per�odos '1 d�a' y '2 d�as',
 * si el primero define un orden de 1 y el segundo de 2, el primero ser� anterior al segundo, es decir, mayor.
 * <p>
 * El orden cronol�gico permitir� el empleo de esta clase para las definiciones de dominios temporales en
 * horarios y planificaciones. Por ejemplo, en un horario con un total de diez timeslots an�nimos de una
 * duraci�n de media hora, se podr� identificar el orden del transcurso de cada uno de estos per�dos
 * por medio del orden cronol�gico asociado a cada timeslot an�nimo.
 * <p>
 * El orden cronol�gico deber�a ser �nico para cada timeslot en un supuesto conjunto de ellos
 *
 */
public class AbstractTimeslot extends Entity implements Timeslot {
	/**
	 * Orden cronol�gico o jer�rquico que indica la posici�n de este timeslot en una jerarqu�a
	 * indefinida de timeslots an�nimos. La maginitud de prioridad es inversa al valor num�rico
	 */
	protected final int chronologicalOrder;
	
	/**
	 * Constructor de un timeslot abstracto
	 * 
	 * @param chronologicalOrder orden cronol�gico de este timeslot
	 */
	public AbstractTimeslot(final int chronologicalOrder) {
		super("Timeslot [order=" + chronologicalOrder + "]");
		this.chronologicalOrder = chronologicalOrder;
	}

	@Override
	public int getChronologicalOrder() {
		return chronologicalOrder;
	}
	
	public int compareTo(Timeslot o) {
		if (o == null)
			return 1;
		
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
