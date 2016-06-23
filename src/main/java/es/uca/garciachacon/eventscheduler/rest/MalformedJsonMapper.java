package es.uca.garciachacon.eventscheduler.rest;

import es.uca.garciachacon.eventscheduler.rest.deserializer.MalformedJsonException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Manejo dedicado de las excepciones de tipo {@link MalformedJsonException} que se lanzan desde el servicio web.
 * <p>
 * Cuando se produzca una excepción de este tipo, correspondiente a errores de formato JSON durante la
 * deserialización de un cuerpo JSON (y también durante la serialización, aunque es una situación menos común), la
 * respuesta HTTP tendrá un estatus de código 400 y se mostrará el mensaje de la excepción, si lo hubiera, indicando
 * la razón por la que fue lanzada.
 */
@Provider
public class MalformedJsonMapper implements ExceptionMapper<MalformedJsonException> {
    @Override
    public Response toResponse(MalformedJsonException exception) {
        return Response.status(Response.Status.BAD_REQUEST).entity(exception.getMessage()).build();
    }
}
