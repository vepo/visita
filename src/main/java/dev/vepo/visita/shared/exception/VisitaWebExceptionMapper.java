package dev.vepo.visita.shared.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class VisitaWebExceptionMapper implements ExceptionMapper<WebApplicationException> {
    private static final Logger logger = LoggerFactory.getLogger(VisitaWebExceptionMapper.class);

    @Override
    public Response toResponse(WebApplicationException exception) {
        logger.error("An errro happen!", exception);
        return Response.status(exception.getResponse()
                                        .getStatus())
                       .type(MediaType.APPLICATION_JSON)
                       .entity(new ErrorResponse(exception.getResponse()
                                                          .getStatus(),
                                                 exception.getMessage()))
                       .build();
    }

}
