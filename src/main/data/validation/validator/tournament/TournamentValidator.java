package data.validation.validator.tournament;

import java.util.ArrayList;
import java.util.List;

import data.model.tournament.Tournament;
import data.model.tournament.event.Event;
import data.validation.validable.ValidationException;
import data.validation.validator.Validator;

/**
 * Valida el estado final de un torneo.
 * <p>
 * Las reglas de validaci�n son:
 * <ul>
 * <li>Nombre del torneo no <code>null</code>
 * <li>Lista de categor�as del torneo no <code>null</code> y con al menos una categor�a. Si hay m�s, deben ser �nicas
 * <li>Listas de jugadores, localizaciones y <i>timeslots</i> no <code>null</code> y no vac�as
 * </ul>
 * Adem�s, se valida cada una de las categor�as de las que el torneo se compone, de forma que si alguna de ellas no
 * es v�lida, la validaci�n completa del torneo no ser� satisfactoria. Las reglas de validaci�n de una categor�a por
 * defecto, es decir, si no se ha cambiado el objeto que la valida, est�n definidas en {@link EventValidator}.
 *
 */
public class TournamentValidator implements Validator<Tournament> {/**
	 * Mensajes de error de la validaci�n
	 */
	private List<String> messages = new ArrayList<String>();

	public boolean validate(Tournament t) {
		boolean isValid = validateName(t) && validateEvents(t) && validateLists(t);
		return isValid;
	}
	
	/**
	 * Valida el nombre del torneo
	 * 
	 * @param t torneo no <code>null</code>
	 * @return si la validaci�n es satisfactoria, <code>true</code>, de lo contrario, <code>false</code>
	 */
	private boolean validateName(Tournament t) {
		if (t.getName() == null) {
			messages.add("Name cannot be null");
			return false;
		}
		return true;
	}
	
	/**
	 * Valida el conjunto de categor�as del torneo, as� como cada una de ellas individualmente
	 * 
	 * @param t torneo no <code>null</code>
	 * @return si la validaci�n es satisfactoria, <code>true</code>, de lo contrario, <code>false</code>
	 */
	private boolean validateEvents(Tournament t) {
		boolean isValid = true;
		
		List<Event> events = t.getEvents();
		
		if (events == null) {
			messages.add("Categories cannot be null");
			return false;
		}
		
		if (events.contains(null)) {
			messages.add("Category cannot be null");
			return false;
		}
		
		for (int i = 0; i < events.size() - 1; i++)
			for (int j = i + 1; j < events.size(); j++)
				if (events.get(i) == events.get(j)) {
					isValid = false;
					messages.add(String.format("All categories must be unique; event (%s) is duplicated", events.get(i)));
				}
		
		if (isValid) {
			for (Event event : events) {
				try {
					event.validate();
				} catch (ValidationException e) {
					isValid = false;
					
					for (String err : event.getMessages())
						messages.add(String.format("Validation error in event (%s): %s", event, err));
				}
			}
		}
		
		return isValid;
	}
	
	/**
	 * Valida las listas de jugadores, localizaciones y horas de juego del torneo
	 * 
	 * @param t torneo no <code>null</code>
	 * @return si la validaci�n es satisfactoria, <code>true</code>, de lo contrario, <code>false</code>
	 */
	private boolean validateLists(Tournament t) {
		boolean isValid = true;
		
		if (t.getAllPlayers() == null || t.getAllPlayers().isEmpty()) {
			isValid = false;
			messages.add("Players cannot be null or empty");
		}
		
		if (t.getAllLocalizations() == null || t.getAllLocalizations().isEmpty()) {
			isValid = false;
			messages.add("Localizations cannot be null or empty");
		}
		
		if (t.getAllTimeslots() == null || t.getAllTimeslots().isEmpty()) {
			isValid = false;
			messages.add("Timeslots cannot be null or empty");
		}
		
		return isValid;
	}

	public List<String> getValidationMessages() {
		return messages;
	}

}
