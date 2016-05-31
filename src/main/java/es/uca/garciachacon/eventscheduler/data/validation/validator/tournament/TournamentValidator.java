package es.uca.garciachacon.eventscheduler.data.validation.validator.tournament;

import es.uca.garciachacon.eventscheduler.data.model.tournament.Tournament;
import es.uca.garciachacon.eventscheduler.data.model.tournament.event.Event;
import es.uca.garciachacon.eventscheduler.data.validation.validable.ValidationException;
import es.uca.garciachacon.eventscheduler.data.validation.validator.Validator;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Valida el estado final de un torneo.
 * <p>
 * Las reglas de validación son:
 * <ul>
 * <li>Nombre del torneo no <code>null</code>
 * <li>Lista de categorías del torneo no <code>null</code> y con al menos una categoría. Si hay más, deben ser únicas
 * <li>Listas de jugadores, localizaciones y <i>timeslots</i> no <code>null</code> y no vacías
 * </ul>
 * Además, se valida cada una de las categorías de las que el torneo se compone, de forma que si alguna de ellas no
 * es válida, la validación completa del torneo no será satisfactoria. Las reglas de validación de una categoría por
 * defecto, es decir, si no se ha cambiado el objeto que la valida, están definidas en {@link EventValidator}.
 */
public class TournamentValidator implements Validator<Tournament> {
    /**
     * Mensajes de error de la validación
     */
    private final List<String> messages = new ArrayList<>();

    public boolean validate(Tournament t) {
        boolean isValid = validateName(t) && validateEvents(t) && validateLists(t);
        return isValid;
    }

    /**
     * Valida el nombre del torneo.
     *
     * @param t torneo no <code>null</code>
     * @return si la validación es satisfactoria, <code>true</code>, de lo contrario, <code>false</code>
     */
    private boolean validateName(Tournament t) {
        if (t.getName() == null) {
            messages.add("Name cannot be null");
            return false;
        }
        return true;
    }

    /**
     * Valida el conjunto de categorías del torneo, así como cada una de ellas individualmente.
     *
     * @param t torneo no <code>null</code>
     * @return si la validación es satisfactoria, <code>true</code>, de lo contrario, <code>false</code>
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
                    messages.add(String.format("All categories must be unique; event (%s) is duplicated",
                            events.get(i)
                    ));
                }

        if (isValid) {
            for (Event event : events) {
                try {
                    event.validate();
                } catch (ValidationException e) {
                    isValid = false;

                    messages.addAll(event.getMessages()
                            .stream()
                            .map(err -> String.format("Validation error in event (%s): %s", event, err))
                            .collect(Collectors.toList()));
                }
            }
        }

        return isValid;
    }

    /**
     * Valida las listas de jugadores, localizaciones y horas de juego del torneo.
     *
     * @param t torneo no <code>null</code>
     * @return si la validación es satisfactoria, <code>true</code>, de lo contrario, <code>false</code>
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
