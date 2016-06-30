package es.uca.garciachacon.eventscheduler.rest;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Manejo dedicado de las excepciones de tipo {@link BadRequestException} que se lanzan desde el servicio web. El
 * mensaje de error será incluido en la respuesta.
 */
@Provider
public class BadRequestMapper implements ExceptionMapper<BadRequestException> {
    @Override
    public Response toResponse(BadRequestException exception) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity("HTTP State 400: Bad Request\n" + exception.getMessage())
                .build();
    }
}
