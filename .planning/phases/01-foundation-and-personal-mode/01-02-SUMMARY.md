---
phase: 01-foundation-and-personal-mode
plan: 02
subsystem: infra
tags: [spring-boot, liquibase, postgres, openapi, ddd, testing]
requires:
  - phase: 01-01
    provides: auth-free status contract and regenerated backend/frontend API inputs
provides:
  - Liquibase baseline with seeded app metadata
  - Spring-free domain and application status projection
  - Anonymous `/api/v1/status` backend adapter with architecture tests
affects: [01-03, phase-02, phase-03, phase-04]
tech-stack:
  added: [spring-boot-liquibase]
  patterns: [domain-application-infrastructure separation, backend-owned status projection, runtime health adapter]
key-files:
  created:
    - milestory-backend/src/main/resources/db/changelog/db.changelog-master.yaml
    - milestory-backend/src/main/resources/db/changelog/changes/001-foundation-baseline.yaml
    - milestory-backend/src/main/java/com/ybritto/milestory/domain/status/FoundationStatus.java
    - milestory-backend/src/main/java/com/ybritto/milestory/application/status/GetFoundationStatusUseCase.java
    - milestory-backend/src/main/java/com/ybritto/milestory/infrastructure/api/FoundationStatusController.java
  modified:
    - milestory-backend/pom.xml
    - milestory-backend/src/main/java/com/ybritto/milestory/infrastructure/status/SystemFoundationStatusService.java
    - milestory-backend/src/test/java/com/ybritto/milestory/MilestoryBackendApplicationTests.java
key-decisions:
  - "Keep status assembly in a Spring-free use case that returns a domain projection and map to generated OpenAPI records only at the controller edge."
  - "Use Spring Boot 4's explicit `spring-boot-liquibase` module so the runtime actually applies the Liquibase baseline."
  - "Read the live database name from the connected datasource to keep `/api/v1/status` truthful even when config placeholders resolve oddly."
patterns-established:
  - "Backend foundation logic lives in `domain.status` and `application.status`, with infrastructure services providing runtime facts."
  - "Architecture protection can start with source-level dependency assertions before heavier tooling is introduced."
requirements-completed: [PLAT-01, PLAT-02]
duration: 13 min
completed: 2026-04-03
---

# Phase 01 Plan 02: Backend foundation baseline, status projection, and anonymous runtime endpoint Summary

**Liquibase-backed Phase 1 baseline with backend-owned status assembly and an anonymous `/api/v1/status` adapter**

## Performance

- **Duration:** 13 min
- **Started:** 2026-04-03T22:12:00Z
- **Completed:** 2026-04-03T22:24:50Z
- **Tasks:** 2
- **Files modified:** 15

## Accomplishments
- Added a real Liquibase changelog tree with `001-foundation-baseline`, creating and seeding `app_metadata`.
- Established the first backend DDD seam with `domain.status`, `application.status`, and infrastructure wiring for runtime facts.
- Implemented the generated `/api/v1/status` adapter and locked the boundary with unit, integration, and domain-isolation tests.
- Verified the backend suite passes and the live endpoint returns `mode: ready` without any `Authorization` header.

## Task Commits

Each task was committed atomically:

1. **Task 1: Add the Liquibase baseline and package skeleton that Phase 1 actually needs** - `c7143b6` (`feat`)
2. **Task 2: Implement the anonymous status adapter and prove the backend boundary with tests** - `6f76111` (`feat`)
3. **Runtime verification fix: enable Liquibase baseline application under Spring Boot 4** - `b19294c` (`fix`)

## Files Created/Modified

- `milestory-backend/src/main/resources/db/changelog/db.changelog-master.yaml` - Liquibase master changelog for backend startup.
- `milestory-backend/src/main/resources/db/changelog/changes/001-foundation-baseline.yaml` - Baseline schema and seed row for `app_metadata`.
- `milestory-backend/src/main/java/com/ybritto/milestory/domain/status/FoundationStatus.java` - Framework-free status projection model.
- `milestory-backend/src/main/java/com/ybritto/milestory/domain/status/FoundationStatusMode.java` - Domain readiness mode enum.
- `milestory-backend/src/main/java/com/ybritto/milestory/application/status/GetFoundationStatusUseCase.java` - Status assembly use case.
- `milestory-backend/src/main/java/com/ybritto/milestory/application/status/FoundationRuntimeStatus.java` - Runtime fact snapshot for the use case.
- `milestory-backend/src/main/java/com/ybritto/milestory/application/status/FoundationRuntimeStatusProvider.java` - Application port for runtime facts.
- `milestory-backend/src/main/java/com/ybritto/milestory/infrastructure/status/FoundationStatusConfiguration.java` - Spring wiring for the use case clock and provider.
- `milestory-backend/src/main/java/com/ybritto/milestory/infrastructure/status/SystemFoundationStatusService.java` - Runtime adapter for active profile, database, and baseline facts.
- `milestory-backend/src/main/java/com/ybritto/milestory/infrastructure/api/FoundationStatusController.java` - Generated API implementation for `GET /api/v1/status`.
- `milestory-backend/src/test/java/com/ybritto/milestory/MilestoryBackendApplicationTests.java` - Deterministic smoke test for the changelog foundation.
- `milestory-backend/src/test/java/com/ybritto/milestory/application/status/AppStatusServiceTest.java` - Ready/empty/degraded use-case coverage.
- `milestory-backend/src/test/java/com/ybritto/milestory/architecture/DomainIsolationTest.java` - Domain dependency guardrail.
- `milestory-backend/src/test/java/com/ybritto/milestory/infrastructure/api/PersonalModeStatusIntegrationTest.java` - Anonymous endpoint verification.
- `milestory-backend/pom.xml` - Explicit Spring Boot 4 Liquibase runtime support.

## Decisions Made

- Kept the controller thin and delegated status assembly to `GetFoundationStatusUseCase`, with mapping to generated DTOs only in infrastructure.
- Used a simple application port (`FoundationRuntimeStatusProvider`) instead of letting Spring or JDBC leak into `application` and `domain`.
- Chose source-level architecture assertions in `DomainIsolationTest` as the smallest useful guardrail for Phase 1.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Backend source generation was reading a stale installed API artifact**
- **Found during:** Task 2
- **Issue:** `mvn ... generate-sources` produced the old hello-world API because the current `milestory-api` module had not been installed into the local Maven repository.
- **Fix:** Installed the current `milestory-api` module, then regenerated backend sources so the generated interface/model set matched `/api/v1/status`.
- **Files modified:** none in repo state
- **Verification:** `mvn -q -pl milestory-backend generate-sources`
- **Committed in:** no code-change commit required

**2. [Rule 3 - Blocking] Spring Boot 4 package moves broke initial runtime/test assumptions**
- **Found during:** Task 2
- **Issue:** Older package assumptions for datasource properties and MockMvc auto-configuration failed during verification.
- **Fix:** Removed hard dependencies on moved Boot classes, used `Environment` plus standalone `MockMvc`, and kept the endpoint verification intact.
- **Files modified:** `milestory-backend/src/main/java/com/ybritto/milestory/infrastructure/status/SystemFoundationStatusService.java`, `milestory-backend/src/test/java/com/ybritto/milestory/infrastructure/api/PersonalModeStatusIntegrationTest.java`
- **Verification:** `mvn -q -pl milestory-backend -Dtest=PersonalModeStatusIntegrationTest,AppStatusServiceTest,DomainIsolationTest test`
- **Committed in:** `6f76111`

**3. [Rule 3 - Blocking] Liquibase baseline did not apply at runtime under Spring Boot 4**
- **Found during:** Overall verification
- **Issue:** The app booted and served `/api/v1/status`, but reported `baseline-pending` because Boot 4 keeps Liquibase auto-configuration in `spring-boot-liquibase`, which was not yet on the classpath.
- **Fix:** Added `spring-boot-liquibase` and switched database-name reporting to query the live datasource.
- **Files modified:** `milestory-backend/pom.xml`, `milestory-backend/src/main/java/com/ybritto/milestory/infrastructure/status/SystemFoundationStatusService.java`
- **Verification:** `mvn -q -pl milestory-backend test`; `mvn -q -pl milestory-backend spring-boot:run`; `curl -s http://localhost:8080/api/v1/status`
- **Committed in:** `b19294c`

---

**Total deviations:** 3 auto-fixed (3 blocking)
**Impact on plan:** All fixes were required to make the plan's runtime outcome true under the current Spring Boot 4 environment. No feature scope was added beyond the planned backend foundation work.

## Issues Encountered

- The local Postgres container already contained older tables and changelog history from other work, so runtime verification had to confirm the new baseline applied cleanly into an existing dev database rather than assuming an empty instance.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- Phase 01-03 can consume `/api/v1/status` directly from the backend and rely on backend-owned `ready/empty/degraded` semantics.
- Later domain work now has explicit `domain`, `application`, and infrastructure seams to extend instead of starting from flat adapter code.

## Self-Check: PASSED

- Found summary file `.planning/phases/01-foundation-and-personal-mode/01-02-SUMMARY.md`
- Found commit `c7143b6`
- Found commit `6f76111`
- Found commit `b19294c`

---
*Phase: 01-foundation-and-personal-mode*
*Completed: 2026-04-03*
