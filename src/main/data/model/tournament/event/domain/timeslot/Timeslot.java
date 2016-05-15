package data.model.tournament.event.domain.timeslot;

/**
 * Representa una hora de juego en la que discurre un partido o enfrentamiento perteneciente a un evento o
 * categoría deportiva en el contexto de un torneo.
 * <p>
 * Una hora de juego se representa mediante el concepto de <i>timeslot</i>, un modelo abstracto de un período
 * o momento en el tiempo, ya sea concreto o indefinido, o con un rango o período establecido o desconocido.
 * <p>
 * Un <i>timeslot</i> puede ser simplemente una representación elemental de un hueco representado por un número
 * en una línea temporal, o puede ser un período con una duración definida o incluso con un comienzo definido
 * en esa línea temporal. Clases derivadas de ésta representan estos casos.
 *
 */
public interface Timeslot extends Comparable<Timeslot> {
	/**
	 * Obtiene el orden cronológico del <i>timeslot<i> como un entero.
	 * 
	 * @return el valor del orden cronológico
	 */
	public int getChronologicalOrder();

	/**
	 * Comprueba si el <i>timeslot<i> se encuentra entre otros dos, extremos incluidos
	 * 
	 * @param t1 un extremo del rango de <i>timeslots</i>
	 * @param t2 el otro extremo del rango
	 * @return <code>true</code> si se encuentra en el rango; <code>false</code> si no se encuentra en el rango
	 * o si alguno de los argumentos es <code>null</code> o si ambos lo son
	 */
	public boolean within(Timeslot t1, Timeslot t2);
	
	/**
	 * Compara dos <i>timeslots<i>
	 * 
	 * @param t1 primer hora a comparar
	 * @param t2 segunda hora a comparar
	 * @return 1 si t1 es anterior a t2, -1 si t2 es anterior a t1 o si t1 es null y 0 si tienen el mismo orden cronológico
	 */
	public static int compare(Timeslot t1, Timeslot t2) {
		if (t1 == null)
			return t2 == null ? 0 : -1;
		return t1.compareTo(t2);
	}
}