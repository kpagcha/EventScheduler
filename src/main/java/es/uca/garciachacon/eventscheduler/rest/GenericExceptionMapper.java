package es.uca.garciachacon.eventscheduler.rest;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * <i>Mapper</i> de todas las excepciones {@link Throwable} para la depuración del servicio. Se mostrará una traza de
 * errores cuando se lance una excepción distinta de las correspondientes a <i>mappers</i> ya existentes para tipos
 * de excepciones más específicas.
 */
@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {
    @Override
    public Response toResponse(Throwable exception) {
        exception.printStackTrace();
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(exception.getMessage()).build();
    }
}
