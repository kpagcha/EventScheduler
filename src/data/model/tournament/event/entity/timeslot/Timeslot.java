package data.model.tournament.event.entity.timeslot;

/**
 * Representa una hora de juego en la que discurre un partido o enfrentamiento perteneciente a un evento o
 * categor�a deportiva en el contexto de un torneo.
 * <p>
 * Una hora de juego se representa mediante el concepto de <i>timeslot</i>, un modelo abstracto de un per�odo
 * o momento en el tiempo, ya sea concreto o indefinido, o con un rango o per�odo establecido o desconocido.
 * <p>
 * Un <i>timeslot</i> puede ser simplemente una representaci�n elemental de un hueco representado por un n�mero
 * en una l�nea temporal, o puede ser un per�odo con una duraci�n definida o incluso con un comienzo definido
 * en esa l�nea temporal. Clases derivadas de �sta representan estos casos.
 *
 */
public interface Timeslot extends Comparable<Timeslot> {
	/**
	 * Obtiene el orden cronol�gico del timeslot como un int.
	 * 
	 * @return el valor del orden cronol�gico
	 */
	public int getChronologicalOrder();
	
	/**
	 * Compara dos timeslots
	 * 
	 * @param t1 primer hora a comparar
	 * @param t2 segunda hora a comparar
	 * @return 1 si t1 es anterior a t2, -1 si t2 es anterior a t1 o si t1 es null y 0 si tienen el mismo orden cronol�gico
	 */
	public static int compare(Timeslot t1, Timeslot t2) {
		if (t1 == null)
			return -1;
		return t1.compareTo(t2);
	}
}