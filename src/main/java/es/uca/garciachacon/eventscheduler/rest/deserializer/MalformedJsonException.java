package es.uca.garciachacon.eventscheduler.rest.deserializer;

import java.io.IOException;

/**
 * Excepción que indica que el formato del cuerpo JSON no es el esperado, es decir, campos obligatorios que no están
 * incluidos, formato de tipo de dato distinto del esperado (por ejemplo, se espera el valor de una cadena, pero se
 * encuentra una lista) o cualquier otro tipo de irregularidad en el formato.
 */
public class MalformedJsonException extends IOException {
    /**
     * Constructor por defecto de esta excepción, no incluye mensaje con información adicional;
     */
    public MalformedJsonException() {
        super();
    }

    /**
     * Construye una excepción {@link MalformedJsonException} con el mensaje especificado
     *
     * @param message mensaje que describe la excepción; éste se puede obtener más adelante mediante
     *                {@link MalformedJsonException#getMessage()}
     */
    public MalformedJsonException(String message) {
        super(message);
    }
}
