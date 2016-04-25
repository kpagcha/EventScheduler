package data.validation.validator.event;

import java.util.ArrayList;
import java.util.List;

import data.model.tournament.event.Event;
import data.validation.validator.Validator;

/**
 * Valida un evento o categor�a.
 *
 */
public class EventValidator implements Validator<Event> {
	/**
	 * Mensajes de error de la validaci�n
	 */
	private List<String> messages = new ArrayList<String>();
	
	public boolean validate(Event event) {
		return validateName(event) && validatePlayers(event);
	}
	
	/**
	 * Valida un nombre para el evento. No puede ser null
	 * 
	 * @param e event no nulo
	 * @return <code>true</code> si la pasa la validaci�n, <code>false</code> si es fallida
	 */
	private boolean validateName(Event e) {
		boolean isValid = e.getName() != null;
		if (!isValid)
			messages.add("Name cannot be null");
		return isValid;
	}
	
	/**
	 * Valida la lista de jugadores del evento. Reglas:
	 * <ul>
	 * <li>No null
	 * <li>N�mero de jugadores superior a 1
	 * <li>N�mero de jugadores m�ltiplo del n�mero de jugadores por partido que el evento especifica
	 * </ul>
	 * 
	 * @param e evento no nulo
	 * @return <code>true</code> si la lista de jugadores es v�lida, <code>false</code> si no
	 */
	private boolean validatePlayers(Event e) {
		return false;
	}

	public List<String> getValidationMessages() {
		return messages;
	}
}
