package es.uca.garciachacon.eventscheduler.data.validation.validable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import es.uca.garciachacon.eventscheduler.data.validation.validator.Validator;

import java.util.List;

/**
 * Interfaz que indica que la clase que la implemente está sujeta a validación por medio de un
 * {@link Validator}.
 */
public interface Validable {
    /**
     * Inyecta el validador en la clase validable.
     *
     * @param <T>       tipo de dato que valida el validador
     * @param validator validador no nulo
     */
    <T> void setValidator(Validator<T> validator);

    /**
     * Lanza la validación
     *
     * @throws ValidationException si la validación falla.
     */
    void validate() throws ValidationException;

    /**
     * Devuelve los mensajes de error de validación.
     *
     * @return una lista de mensajes de error de validación, o una lista vacía si no hay ninguno
     */
    @JsonIgnore
    List<String> getMessages();
}
