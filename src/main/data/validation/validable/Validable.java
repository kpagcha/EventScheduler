package data.validation.validable;

import java.util.List;

import data.validation.validator.Validator;

/**
 * Interfaz que indica que la clase que la implemente está sujeta a validación por medio de un {@link data.validation.validator.Validator}
 *
 */
public interface Validable {
	/**
	 * Inyecta el validador en la clase validablez
	 * @param <T> tipo de dato que valida el validador
	 * 
	 * @param validator validador no nulo
	 */
	public <T> void setValidator(Validator<T> validator);
	
	/**
	 * Lanza la validación
	 * 
	 * @throws ValidationException si la validación falla
	 */
	public void validate() throws ValidationException;
	
	/**
	 * Devuelve los mensajes de error de validación
	 * 
	 * @return una lista de mensajes de error de validación, o una lista vacía si no hay ninguno
	 */
	public List<String> getMessages();
}
