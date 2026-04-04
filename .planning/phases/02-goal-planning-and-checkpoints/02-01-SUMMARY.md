---
phase: 02-goal-planning-and-checkpoints
plan: 01
subsystem: api
tags: [openapi, liquibase, java, ddd, goal-planning, client-generation]
requires:
  - phase: 01-foundation-and-personal-mode
    provides: auth-free backend/frontend foundation and generated API conventions
provides:
  - goal planning OpenAPI surface for categories, previews, goal persistence, archive, and restore
  - Liquibase schema and seeded categories for goals and ordered checkpoints
  - framework-free goal aggregate and monthly preview use case with targeted tests
affects: [milestory-api, milestory-backend, milestory-frontend, phase-02]
tech-stack:
  added: []
  patterns: [contract-first goal planning, backend-owned monthly preview rules, immutable goal aggregate invariants]
key-files:
  created: [.planning/phases/02-goal-planning-and-checkpoints/02-01-SUMMARY.md, milestory-api/rest/paths/goal-categories.yaml, milestory-api/rest/paths/goal-plans-preview.yaml, milestory-api/rest/paths/goals.yaml, milestory-api/rest/paths/goal-by-id.yaml, milestory-api/rest/schemas/goal-planning.yaml, milestory-backend/src/main/resources/db/changelog/changes/002-goal-planning-foundation.yaml, milestory-backend/src/main/java/com/ybritto/milestory/goal/domain/Goal.java, milestory-backend/src/main/java/com/ybritto/milestory/goal/application/usecase/PreviewGoalPlanUseCase.java]
  modified: [milestory-api/rest/api-v1.yaml, milestory-backend/src/main/resources/db/changelog/db.changelog-master.yaml]
key-decisions:
  - "Archive and restore received explicit API endpoints now so the backend lifecycle stays contract-backed before controller work starts."
  - "Checkpoint plans are modeled as cumulative monthly targets that must end exactly on the goal target value."
  - "Phase 2 starts with generic fallback preview logic and makes that fallback visible in both the contract and backend model."
patterns-established:
  - "Goal-planning contract changes regenerate the frontend API client as part of verification."
  - "Goal domain types remain framework-free and enforce ordered checkpoint invariants inside the aggregate."
requirements-completed: [GOAL-01, GOAL-04, PLAN-01]
duration: 39 min
completed: 2026-04-04
---

# Phase 02 Plan 01: Goal planning contract, schema, and backend foundation

**Goal-planning API routes, Liquibase persistence foundations, and a framework-free monthly checkpoint preview model now anchor Milestory Phase 2.**

## Performance

- **Duration:** 39 min
- **Started:** 2026-04-04T07:50:41Z
- **Completed:** 2026-04-04T08:28:51Z
- **Tasks:** 2
- **Files modified:** 16

## Accomplishments
- Added the full Phase 2 goal-planning contract for categories, plan previews, goal CRUD detail, archive, and restore flows.
- Created the backend persistence baseline for goals, categories, and ordered checkpoints with seeded starter categories.
- Added a framework-free `Goal` aggregate plus a `PreviewGoalPlanUseCase` that generates twelve cumulative end-of-month checkpoints with exact-target final math.

## Task Commits

Each task was committed atomically:

1. **Task 1: Expand the OpenAPI contract for goal categories, plan preview, and goal lifecycle** - `b4477b1` (feat)
2. **Task 2: Add the database and domain foundation for backend-owned monthly planning** - `24b516a` (feat)

## Files Created/Modified
- `milestory-api/rest/api-v1.yaml` - Registers the new goal-planning, archive, and restore path refs.
- `milestory-api/rest/paths/goal-categories.yaml` - Defines list and create operations for starter and custom goal categories.
- `milestory-api/rest/paths/goal-plans-preview.yaml` - Defines the backend-owned preview endpoint for checkpoint suggestions.
- `milestory-api/rest/paths/goals.yaml` and `milestory-api/rest/paths/goal-by-id.yaml` - Define goal list/create/detail/update/archive/restore operations.
- `milestory-api/rest/schemas/goal-planning.yaml` - Defines preview, goal, checkpoint, archive, and restore payloads.
- `milestory-backend/src/main/resources/db/changelog/changes/002-goal-planning-foundation.yaml` - Adds `goal_category`, `goal`, and `goal_checkpoint` plus starter category seeds.
- `milestory-backend/src/main/java/com/ybritto/milestory/goal/domain/Goal.java` - Implements the immutable goal aggregate and checkpoint ordering rules.
- `milestory-backend/src/main/java/com/ybritto/milestory/goal/application/usecase/PreviewGoalPlanUseCase.java` - Generates twelve cumulative monthly preview checkpoints using `BigDecimal`.

## Decisions Made
- Reserved dedicated archive and restore endpoints in the contract now instead of deferring lifecycle shape to controller implementation.
- Kept preview pacing evenly distributed and cumulative across all categories so one planning model works for financial, fitness, reading, weight, and custom goals.
- Surfaced `GENERIC_FALLBACK` explicitly in the preview model because category-specific pacing is intentionally deferred until later work.

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered
- The first task commit hit a stale `.git/index.lock`; retrying after the lock cleared completed normally and did not require code changes.

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- Backend execution can now build directly against a stable goal-planning contract and seeded schema.
- The frontend API client regenerated successfully from the new contract during package verification, so the Angular work in `02-03` has the right service surface available once the backend controllers land.

## Self-Check: PASSED

---
*Phase: 02-goal-planning-and-checkpoints*
*Completed: 2026-04-04*
