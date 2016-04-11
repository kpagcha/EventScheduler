package data.model.tournament.event.entity.timeslot;

/**
 * Un período de tiempo en un horario
 *
 */
public interface Timeslot extends Comparable<Timeslot> {
	/**
	 * Obtiene el orden cronológico del timeslot como un int.
	 * 
	 * @return el valor del orden cronológico
	 */
	public int getChronologicalOrder();
	
	/**
	 * Compara dos timeslots
	 * 
	 * @param t1
	 * @param t2
	 * @return 1 si t1 es anterior a t2, -1 si t2 es anterior a t1 y 0 si tienen el mismo orden cronológico
	 */
	public static int compare(Timeslot t1, Timeslot t2) {
		return t1.compareTo(t2);
	}
}