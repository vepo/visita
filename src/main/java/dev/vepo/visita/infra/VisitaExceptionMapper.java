package dev.vepo.visita.infra;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.Priority;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
@Priority(1)
public class VisitaExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger logger = LoggerFactory.getLogger(VisitaExceptionMapper.class);

    @Override
    public Response toResponse(Exception exception) {
        logger.error("Error!!!", exception);
        return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    }

}
