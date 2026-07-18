# Feature change requests

Use one file per high-level capability: `feature/<feature-slug>.md` (kebab-case).

Mandatory process: [development-process.mdc](../.cursor/rules/development-process.mdc) (analysis -> architecture -> tasks -> explicit approval -> TDD).

## Template

Copy this into `feature/<feature-slug>.md`:

```markdown
# <Human-readable feature name>

**Feature version:** 1  
**Status:** planned | architecture-ready | tasks-ready | approved | in-progress | done  
**Requested:** YYYY-MM-DD

## Summary

One paragraph describing what is needed and why.

## Impact

| Area | Effect |
|------|--------|
| Packages / files | Main touch points |
| Routes / APIs | Added/changed endpoints |
| Schema / seed | `db/migration/V*.sql`, `dev-import.sql` |
| Tests | `@QuarkusTest`, `@WebTest`, unit |
| Docs | `docs/domain-specification.md`, `ARCHITECTURE.md` |

## Risks

- Risk 1

## Feature questions (FQn)

| # | Question | Status | Answer |
|---|----------|--------|--------|
| FQ1 | ... | open | |

## Architecture

| Area | Design |
|------|--------|
| Layers | Endpoint -> Service -> Repository |
| Routes / APIs | ... |
| Schema / migration | ... |
| Dev seed | `dev-import.sql` impact |
| Tests | planned scenarios |

## Architecture questions (AQn)

| # | Question | Status | Answer |
|---|----------|--------|--------|
| AQ1 | ... | open | |

## Changelog

### <Change name> — YYYY-MM-DD

**Version:** 1  
**Status:** planned | architecture-ready | tasks-ready | approved | in-progress | done

**Description:** ...

#### Feature checklist

| ID | Criterion | Source | Done |
|----|-----------|--------|------|
| FC1 | ... | FQ1 | ☐ |
| FCdev | `dev-import.sql` covers happy path | development-experience | ☐ |

#### Tasks

| ID | Task | Done |
|----|------|------|
| T1 | ... | ☐ |
| Tdev | Update `dev-import.sql` if applicable | development-experience | ☐ |

#### Test coverage

| ID | Test | Covers | Done |
|----|------|--------|------|
| TC1 | ... | T1 | ☐ |

**Development approval:** pending | approved YYYY-MM-DD — tasks: T1

**Implementation notes:** (fill when done)
```
