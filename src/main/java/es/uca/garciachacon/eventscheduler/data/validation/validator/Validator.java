package es.uca.garciachacon.eventscheduler.data.validation.validator;

import java.util.List;

/**
 * Interfaz para validar clases que modelan entidades o sujetas a reglas de validación.
 */
public interface Validator<T> {
    /**
     * Valida el objeto, comprobando si cumple todas las reglas definidas sobre él.
     *
     * @param o objeto que se valida
     * @return true si la validación es satisfactoria, y false si ha fallado
     */
    boolean validate(T o);

    /**
     * Devuelve la lista de mensajes de error de la validación.
     *
     * @return una lista con mensajes de error, o vacía si no hubo ningún error de validación
     */
    List<String> getValidationMessages();
}
