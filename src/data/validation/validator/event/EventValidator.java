package data.validation.validator.event;

import java.util.ArrayList;
import java.util.List;

import data.model.tournament.event.Event;
import data.model.tournament.event.entity.Player;
import data.validation.validator.Validator;

/**
 * Valida un evento o categoría.
 *
 */
public class EventValidator implements Validator<Event> {
	/**
	 * Mensajes de error de la validación
	 */
	private List<String> messages = new ArrayList<String>();
	
	public boolean validate(Event e) {
		return false;
	}
	
	/**
	 * Valida la lista de jugadores del evento. Reglas:
	 * <ul>
	 * <li>No null
	 * <li>Número de jugadores superior a 1
	 * </ul>
	 * 
	 * @return <code>true</code> si la lista de jugadores es válida, <code>false</code> si no
	 */
	public boolean validatePlayers() {
		return false;
	}

	public List<String> getValidationMessages() {
		return messages;
	}
}
