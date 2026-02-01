package dev.vepo.visita.infra;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.Priority;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
@Priority(0)
public class VisitaWebExceptionMapper implements ExceptionMapper<WebApplicationException> {
    private static final Logger logger = LoggerFactory.getLogger(VisitaWebExceptionMapper.class);

    @Override
    public Response toResponse(WebApplicationException exception) {
        logger.error("Error!!!", exception);
        return Response.status(exception.getResponse().getStatus()).build();
    }

}
