package data.validation.validator;

import java.util.List;

/**
 * Interfaz para validar clases que modelan entidades o sujetas a reglas de validación.
 *
 * @param <T> tipo de dato que se valida
 */
public interface Validator<T> {
	/**
	 * Valida el objeto, comprobando si cumple todas las reglas definidas sobre él.
	 * 
	 * @param e objeto no nulo a validar
	 * @return true si la validación es satisfactoria, y false si ha fallado
	 */
	public boolean validate(T e);
	
	
	/**
	 * Devuelve la lista de mensajes de error de la validación.
	 * 
	 * @return una lista con mensajes de error (cadenas), o vacía si no hubo ningún error de validación
	 */
	public List<String> getValidationMessages();
}
