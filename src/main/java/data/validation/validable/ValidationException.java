package data.validation.validable;

/**
 * Excepción que se lanza principalmente cuando una clase validable, es decir, que implementa la interfaz
 * {@link Validable}, tiene errores de validación.
 */
public class ValidationException extends Exception {
    private static final long serialVersionUID = 1L;

    /**
     * Constructor de la excepción con un mensaje de error.
     *
     * @param message cadena que describe el error de validación que ha ocurrido
     */
    public ValidationException(String message) {
        super(message);
    }
}
