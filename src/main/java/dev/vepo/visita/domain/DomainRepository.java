package dev.vepo.visita.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.vepo.visita.dashboard.shared.exception.RepositoryException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@ApplicationScoped
public class DomainRepository {
    public class DomainSearchCriteria {
        private String hostname;
        private Boolean disabled;

        public DomainSearchCriteria hostname(String hostname) {
            this.hostname = hostname;
            return this;
        }

        public DomainSearchCriteria disabled(Boolean disabled) {
            this.disabled = disabled;
            return this;
        }

        public List<Domain> execute() {
            return search(this);
        }
    }
    private static final Logger logger = LoggerFactory.getLogger(DomainRepository.class);

    private final EntityManager entityManager;

    @Inject
    public DomainRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Optional<Domain> findByHostname(String hostname) {
        return entityManager.createQuery("FROM Domain WHERE hostname = :hostname WHERE disabled = false", Domain.class)
                            .setParameter("hostname", hostname)
                            .setMaxResults(1)
                            .getResultStream()
                            .findFirst();
    }

    public Domain save(Domain domain) {
        Objects.requireNonNull(domain, "'domain' cannot be null!");
        this.entityManager.persist(domain);
        return domain;
    }

    public Optional<Domain> findByHostnameAndToken(String hostname, String token) {
        return entityManager.createQuery("FROM Domain WHERE hostname = :hostname AND token = :token AND disabled = false", Domain.class)
                            .setParameter("hostname", hostname)
                            .setParameter("token", token)
                            .setMaxResults(1)
                            .getResultStream()
                            .findFirst();
    }

    public List<Domain> findAll() {
        return entityManager.createQuery("FROM Domain WHERE disabled = false", Domain.class)
                            .getResultStream()
                            .toList();
    }

    public Optional<Domain> findById(long domainId) {
        return entityManager.createQuery("FROM Domain WHERE id = :domainId", Domain.class)
                            .setParameter("domainId", domainId)
                            .getResultStream()
                            .findFirst();
    }

    public List<Domain> search(DomainSearchCriteria criteria) {
        logger.info("Searching for users...");
        var criteriaBuilder = entityManager.getCriteriaBuilder();
        var criteriaQuery = criteriaBuilder.createQuery(Domain.class);
        var domainRoot = criteriaQuery.from(Domain.class);

        var predicates = buildSearchPredicates(criteria, criteriaBuilder, criteriaQuery, domainRoot);

        if (!predicates.isEmpty()) {
            criteriaQuery.where(criteriaBuilder.and(predicates.toArray(new Predicate[0])));
        }

        criteriaQuery.orderBy(criteriaBuilder.asc(domainRoot.get("hostname")));

        try {
            return entityManager.createQuery(criteriaQuery)
                                .getResultStream()
                                .toList();
        } catch (PersistenceException e) {
            logger.error("Failed to execute user search with criteria: {}", criteria, e);
            throw new RepositoryException("Failed to search users", e);
        }
    }

    public DomainSearchCriteria search() {
        return new DomainSearchCriteria();
    }

    private List<Predicate> buildSearchPredicates(DomainSearchCriteria criteria,
                                                  CriteriaBuilder criteriaBuilder,
                                                  CriteriaQuery<Domain> criteriaQuery,
                                                  Root<Domain> domainRoot) {
        var predicates = new ArrayList<Predicate>();

        // Always exclude disabled users
        if (Objects.nonNull(criteria.disabled)) {
            if (criteria.disabled) {
                predicates.add(criteriaBuilder.isTrue(domainRoot.get("disabled")));
            } else {
                predicates.add(criteriaBuilder.isFalse(domainRoot.get("disabled")));
            }
        }

        if (Objects.nonNull(criteria.hostname) && !criteria.hostname.isBlank()) {
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(domainRoot.get("hostname")),
                                                "%%%s%%".formatted(criteria.hostname.toLowerCase())));
        }

        return predicates;
    }
}
