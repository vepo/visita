package dev.vepo.visita.page.info;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.vepo.visita.page.PageRepository;
import dev.vepo.visita.tracking.TokenRequired;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@ApplicationScoped
@Path("/api/domain/{domain}/page/{page}/info")
@TokenRequired
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PageInfoEndpoint {
    private static final Logger logger = LoggerFactory.getLogger(PageInfoEndpoint.class);

    private PageRepository pageRepository;

    @Inject
    public PageInfoEndpoint(PageRepository pageRepository) {
        this.pageRepository = pageRepository;
    }

    @GET
    public PageInfoResponse info(@PathParam("domain") String domain, @PathParam("page") String page) {
        logger.info("Requesting page info! domain={} page={}", domain, page);
        return new PageInfoResponse(this.pageRepository.getInfo(domain, page).orElseThrow(() -> new NotFoundException()));
    }
}
