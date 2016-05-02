package data.validation.validator;

import java.util.List;

/**
 * Interfaz para validar clases que modelan entidades o sujetas a reglas de validaci�n.
 *
 */
public interface Validator<T> {
	/**
	 * Valida el objeto, comprobando si cumple todas las reglas definidas sobre �l.
	 * 
	 * @param o objeto no nulo que se valida
	 * @return true si la validaci�n es satisfactoria, y false si ha fallado
	 */
	public boolean validate(T o);
	
	/**
	 * Devuelve la lista de mensajes de error de la validaci�n.
	 * 
	 * @return una lista con mensajes de error (cadenas), o vac�a si no hubo ning�n error de validaci�n
	 */
	public List<String> getValidationMessages();
}
