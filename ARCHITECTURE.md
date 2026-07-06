# Architecture & Conventions

Canonical reference for developers and AI agents working on Visita. The public-facing tracker script is documented in [README.md](README.md).

## 1. Core principles

- **Embed-once analytics** — Sites include `visita.js`; the script calls JSON tracking APIs.
- **REST/JSON** — Tracking (`/api/tracking/*`) and domain admin (`/api/domains/*`) are JSON; the dashboard is server-rendered HTML.
- **Qute templates** — Dashboard at `src/main/resources/templates/dashboard.html`; injected via `@Inject Template`.
- **Schema migrations** — Flyway scripts in `src/main/resources/db/migration/` (`quarkus.flyway.*`).
- **Multi-tenant by domain** — Each tracked site is a `Domain` with a secret token; pages belong to a domain.

## 2. Request lifecycle

### Tracking (public)

1. Browser loads `visita.js` with `data-token` for the domain.
2. Script sends `POST /api/tracking/access|view|ping|exit` with headers `VISITA-DOMAIN-TOKEN` and `VISITA-DOMAIN-HOSTNAME`.
3. `TrackingTokenFilter` validates token + hostname against `tb_domains`.
4. `TrackingEndpoint` delegates to `ViewsService`.
5. `ViewsService` resolves or creates `Page`, persists `View` records.

### Domain admin (JWT)

1. Client sends `Authorization: Bearer <jwt>` (issuer from `mp.jwt.verify.issuer`).
2. `@RolesAllowed(RequiredRoles.ADMIN)` gates `/api/domains/*` endpoints.
3. Endpoints use `DomainRepository` directly or via thin orchestration.

### Dashboard (HTML)

1. `GET /dashboard` (optional `startDate`, `endDate`, `/domain/{domain}`, `/referrer/{referrer}`).
2. `DashboardEndpoint` queries `StatsRepository` and renders Qute template with chart data (Chart.js daily charts, D3 Sankey referrer→page flows with page drill-down via `GET /dashboard/api/flows`).

### Stats summary (JSON)

1. `GET /api/stats/summary` (optional `startDate`, `endDate`) — JWT role `domains.admin` or `Domain.Stats.Viewer`.
2. `StatsSummaryEndpoint` delegates to `StatsRepository.buildStatsSummary` for Backoffice home KPIs.

## 3. Domain model

| Entity | Table | Role |
|--------|-------|------|
| `Domain` | `tb_domains` | Tracked hostname + API token; optional ignored path patterns; can be disabled |
| `Page` | `tb_pages` | Path under a domain (`domain_id`, `path` unique) |
| `View` | `tb_views` | Session/page visit: user/tab ids, referrer, timestamps, duration |

- **Views** reference `Page` (not raw URL strings in the normalized schema).
- **Referrer** — per-view referrer from client; `original_referrer` captures the tab session's external entry on the same domain.
- **Duration** — `length` in seconds; updated via `extendDuration` (ping/view) or set on `end_timestamp`.

## 4. Tracking API

| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| `GET` | `/api/tracking/config` | Token | Domain tracking config (ignored path patterns) |
| `POST` | `/api/tracking/access` | Token | Start session (204 when path ignored) |
| `POST` | `/api/tracking/view` | Token | SPA route change / new page in session (204 when path ignored) |
| `POST` | `/api/tracking/ping` | Token | Keep-alive |
| `POST` | `/api/tracking/exit` | Token (header or JSON body) | End session (`sendBeacon` sends credentials in body) |

Request/response records live in `dev.vepo.visita.tracking`. JSON response records used in native builds need `@RegisterForReflection`.

Client script: `src/main/resources/META-INF/resources/visita.js`. The embed is **fail-silent**: server errors must never block or break the host page (degraded mode, no HTTP 4xx/5xx retries).

## 5. Domain admin API

All require JWT role `domains.admin` (`RequiredRoles.ADMIN`).

| Method | Path | Purpose |
|--------|------|---------|
| `POST` | `/api/domains` | Create domain (auto token) |
| `GET` | `/api/domains` | List domains |
| `GET` | `/api/domains/{domainId}` | Get by id |
| `PUT` | `/api/domains/{domainId}` | Update hostname and ignored path patterns |
| `POST` | `/api/domains/{domainId}/enable` | Enable |
| `POST` | `/api/domains/{domainId}/disable` | Disable |
| `POST` | `/api/domains/{domainId}/regenerate-token` | New token |
| `GET` | `/api/domains/search` | Search by hostname |

## 6. Other APIs

| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| `GET` | `/api/stats/summary` | JWT (`domains.admin` or `Domain.Stats.Viewer`) | Analytics summary for Backoffice |
| `GET` | `/api/domain/{domain}/page/{page}/info` | Token | Page metadata |

OpenAPI UI: `/openapi` (dev).

## 7. Design patterns

### Repository

- One per entity; `EntityManager`; `@Transactional` on writes; `Optional` for single results.
- Analytics aggregations in `StatsRepository` (native SQL / JPQL as appropriate).

### Service layer

- `ViewsService` — tracking workflow: resolve page, create/update views.
- Use a `*Service` when logic spans entities or enforces invariants; endpoints may call repositories for trivial CRUD.

### Filters

- `TrackingTokenFilter` — `@Provider` + `@TokenRequired`; validates domain token headers on tracking mutations.

### Testing

- **`@QuarkusTest`** — HTTP/DB integration (RestAssured, injected repositories).
- **`@WebTest`** — Browser tests via `WebTestExtension` (Selenium Chrome, always headless).
- **`Given`** — `domain()`, `view()`, `cleanDatabase()`, `withTransaction()`, JWT helpers `admin()` / `nonAdmin()`.

## 8. Package layout

```
dev.vepo.visita/
├── dashboard/        # Analytics dashboard (Qute + StatsRepository)
├── domain/           # Domain CRUD — create, list, find, update, enable, disable, search, token
├── infra/            # DatabaseDevSetup, exception mappers, Qute extensions, migrations helper
├── page/             # Page entity, repository, page info API
├── shared/           # exception mappers, security (RequiredRoles)
├── tracking/         # Public tracking API, token filter, request/response records
├── View.java         # View entity
├── ViewRepository.java
└── ViewsService.java

dev.vepo.infra/       # Test support — Given, builders, WebTest, WebTestExtension, StaticServer
```

Feature endpoints use subpackages by verb: `domain/create/CreateDomainEndpoint`, etc.

## 9. Naming

| Kind | Pattern |
|------|---------|
| Endpoint | `XxxEndpoint` |
| Repository | `XxxRepository` |
| Service | `XxxService` |
| Entity | singular PascalCase |
| Test (browser) | `*Test` with `@WebTest` |
| Test (integration) | `*Test` with `@QuarkusTest` |
| Request/response | `XxxRequest`, `XxxResponse` records |

## 10. Authentication

| Surface | Mechanism |
|---------|-----------|
| Tracking | `VISITA-DOMAIN-TOKEN` + `VISITA-DOMAIN-HOSTNAME` headers |
| Domain admin | JWT Bearer; role `domains.admin` |
| Dashboard | Currently open (no auth) — treat as internal/analytics UI |

Dev JWT public key in `%dev.mp.jwt.verify.publickey` (`application.properties`).

## 11. Database (main tables)

- `tb_domains` — `hostname`, `token`, `disabled`
- `tb_pages` — `domain_id`, `path` (unique per domain)
- `tb_views` — `page_id`, referrer fields, user/tab ids, timestamps, `length`, `timezone`, `user_agent`

Full DDL: `src/main/resources/db/migration/`

## 12. Adding a feature (checklist)

1. Flyway migration if schema changes.
2. Entity + repository (+ service if non-trivial).
3. Endpoint + OpenAPI annotations where applicable.
4. Update `visita.js` if client tracking behavior changes.
5. Extend `dev-import.sql` so the feature is explorable in `%dev` (see `.cursor/rules/development-experience.mdc`).
6. Tests — `@QuarkusTest` and/or `@WebTest` with `Given` builders.
7. **Update this file** — routes, packages, tables, or workflows that changed.

## 13. Development setup

```bash
./mvnw quarkus:dev
```

- Dev port: **8081** (`%dev.quarkus.http.port`).
- `%dev.quarkus.flyway.clean-at-start=true` resets schema; `DatabaseDevSetup` runs `dev-import.sql` on startup.
- Seeded domain for local tracker tests: `localhost` / token `local-dev`.
- **Sankey demo funnels** in `dev-import.sql` Step 3 — try `/dashboard`: click `blog.vepo.dev/` → `/post/getting-started` → `/post/advanced-topics`; also `shop.example.com/products` → products/cart/checkout and `app.example.com/` → dashboard → analytics.

## 14. Configuration (selected)

```properties
quarkus.datasource.db-kind=postgresql
quarkus.flyway.migrate-at-start=true
%dev.quarkus.flyway.clean-at-start=true
%dev.quarkus.http.port=8081
mp.jwt.verify.issuer=${JWT_ISSUER:https://passport.vepo.dev}
quarkus.http.cors.enabled=true
```

See `src/main/resources/application.properties`.

## 15. Common pitfalls

- **Disabled domains** — `TrackingTokenFilter` rejects unknown or disabled domain/token pairs.
- **Native image JSON** — response records returned by REST endpoints need `@RegisterForReflection` or Jackson serialization fails at runtime (HTTP 500 after successful DB write).
- **Fail-silent embed** — `visita.js` must never throw to the host or retry HTTP 4xx/5xx; on failure it enters degraded mode and stops server calls.
- **Page creation** — `ViewsService` auto-creates `Page` only when the domain already exists; register domains first.
- **Exit auth** — `/api/tracking/exit` validates the domain token from headers or from `domainToken` / `domainHostname` in the JSON body (required for `sendBeacon`, which cannot send custom headers). `visita.js` falls back to `fetch` with `keepalive` when `sendBeacon` is unavailable.
- **Dashboard dates** — `endDate` query param is exclusive (start of next day).
- **Tests mutating DB** — call `Given.cleanDatabase()` in `@BeforeEach` when not relying on dev-import isolation.

## 16. CI

GitHub Actions (`.github/workflows/maven.yml`): `mvn clean compile`, `mvn test` (Chrome via `browser-actions/setup-chrome`). Native Docker image build on push to `main` / tags.
