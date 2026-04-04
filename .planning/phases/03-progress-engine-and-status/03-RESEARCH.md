# Phase 3: Progress Engine And Status - Research

**Researched:** 2026-04-04
**Domain:** Progress tracking, plan-vs-actual status, backend-owned goal calculations
**Confidence:** HIGH

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions
### Progress update shape
- **D-01:** A progress update should include a date, a progress value, and an optional note.
- **D-02:** Each progress update represents the new cumulative total for the goal rather than an incremental delta.
- **D-03:** Phase 3 should keep a visible list of past progress updates on the goal detail experience, but editing and deleting those entries are out of scope for now.
- **D-04:** If the user enters a lower cumulative value than a previous update, Milestory should allow it but explicitly treat it as a correction rather than a silent normal update.

### Status language and tone
- **D-05:** The primary user-facing status labels should be pace-based: `Behind`, `On pace`, and `Ahead`.
- **D-06:** The backend status model should allow a small tolerance band so values close to the expected checkpoint path still count as on pace.
- **D-07:** When a goal is ahead of plan, the experience should feel strongly motivational and high-energy rather than merely factual.
- **D-08:** When a goal is behind plan, the experience should stay supportive but honest rather than blunt or evasive.

### Goal detail explanation
- **D-09:** The goal detail page should emphasize the current status first, followed by a short explanation.
- **D-10:** Plan-versus-actual explanation should use a compact summary sentence plus a small comparison block rather than a dense metrics panel.
- **D-11:** The most useful primary comparison is `actual so far` versus `expected by today`.
- **D-12:** The checkpoint section should remain mostly read-only in Phase 3, but it should be annotated or framed with progress context so the user can understand how the current state relates to the plan.

### Progress entry workflow
- **D-13:** The primary place to add progress should be directly from the goal detail page.
- **D-14:** Opening progress entry should feel like a modal or drawer layered over the detail page rather than a full navigation away from it.
- **D-15:** After saving a progress update, the user should return to the refreshed detail view with an updated status and a clear success signal.
- **D-16:** Progress history should remain visibly present on the goal detail page rather than being hidden behind tabs or secondary navigation.

### Claude's Discretion
- The exact threshold used for the on-pace tolerance band, as long as it remains understandable and does not make status feel arbitrary
- The precise wording of explanatory copy for correction entries, ahead-of-plan encouragement, and behind-plan support
- Whether the progress entry UI is implemented as a centered modal, side drawer, or responsive variant of the same overlay pattern
- The exact visual treatment used to annotate checkpoints with current progress context, as long as the checkpoint list stays readable and mostly informational

### Deferred Ideas (OUT OF SCOPE)
- Dashboard-level rollups, attention ordering, and cross-goal progress summaries belong to Phase 4 rather than this phase
- Rich edit/delete management for progress history is deferred beyond the initial tracking core
- Manual checkpoint completion toggles or dynamic checkpoint reordering are out of scope for Phase 3
- Category-specific progress modes or mixed cumulative/incremental tracking models would be separate follow-on work if needed
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| PROG-01 | User can record a dated progress update for a goal | Add append-only progress-entry contract, backend command/use case, persistence table, and detail-page overlay workflow |
| PROG-02 | User can view cumulative progress against the target value | Return derived `currentProgress` and `progressPercentOfTarget` in enriched goal responses and render compact comparison block |
| PROG-03 | User can see whether a goal is under plan, on track, or above plan at the current point in time | Add backend pace-status calculation with explicit tolerance band and status explanation fields |
| PROG-04 | User can understand how current status relates to planned checkpoints | Derive expected-by-today from checkpoint path, annotate checkpoint cards, and expose comparison/explanation fields in detail payload |
</phase_requirements>

## Summary

Phase 3 should extend the existing goal slice rather than introducing a separate tracking product area. The repo already has the right seam: contract-first OpenAPI definitions, a `goal` aggregate that owns checkpoints and invariants, controller mappers at the HTTP edge, a JPA persistence adapter, and a signal-based goal-detail store in Angular. The safest plan is to add an append-only progress-entry resource plus a derived progress/status projection that is computed in backend domain/application code and exposed through existing goal detail/list responses.

The key design choice is to treat user-entered progress as immutable cumulative snapshots, not deltas. That fits the locked decisions, makes correction handling explicit, avoids replay ambiguity in the frontend, and keeps aggregation straightforward: the current state is the latest valid entry by date, while status is derived by comparing that cumulative value to the expected value interpolated from the checkpoint path as of today. The frontend should stay thin: submit entries, render backend-derived status/explanation, and refresh goal detail after saves.

**Primary recommendation:** Add a `goal_progress_entry` table and `POST /api/v1/goals/{goalId}/progress-entries` endpoint, compute current progress and pace status in backend use cases, and enrich `GoalResponse` so the goal detail page can render status, explanation, checkpoint context, and visible history without frontend business logic.

## Standard Stack

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Spring Boot | 4.0.4 in repo; official docs currently show 4.0.5 | Backend application, MVC, JPA, test slices | Already the project baseline and provides the test/runtime primitives Phase 3 needs |
| Liquibase | via `spring-boot-liquibase` on Spring Boot 4 | Schema migration for progress-entry persistence | Existing migration flow is already the repository standard |
| PostgreSQL | existing datasource target | Durable storage for goals, checkpoints, progress entries | Existing production/test backing store |
| MapStruct | 1.6.3 | Boundary mapping between generated DTOs and handwritten application/domain models | Already established in goal controller mappers |
| Angular | 21.2.x in repo; `@angular/core` latest verified `21.2.7` published 2026-04-01 | Goal detail UI, overlay interaction, signal-driven state | Existing frontend baseline and current official guidance aligns with standalone + signals |
| OpenAPI Generator `typescript-angular` | CLI package `2.31.0` published 2026-03-24 | Generated Angular API client from OpenAPI contract | Existing contract-first flow; official generator supports Angular 9.x-21.x |

### Supporting
| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| openapi-processor-maven-plugin | repo pins `2026.1`; official project docs show `2026.1`; Maven Central simple lookup returned `2024.1` | Generate Spring interfaces/models from OpenAPI | Use for all contract changes; treat version metadata as MEDIUM confidence because registries disagree |
| Vitest | repo `4.0.8`; latest verified `4.1.2` published 2026-03-26 | Frontend unit tests for store/page behavior | Extend current goal-detail/store specs |
| MockMvc | from Spring Boot test stack | Controller contract verification without full running server | Keep using for goal controller integration coverage |

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| Append-only cumulative progress entries | Incremental delta entries | Deltas make correction handling and recomputation harder; cumulative entries match the locked decision |
| Derived status at read time | Persisted denormalized status columns on `goal` | Persisting derived status adds invalidation complexity and risks stale reads; Phase 3 can compute from checkpoints + latest entry |
| Extend `GoalResponse` and goal detail | Create separate progress dashboard/detail APIs now | Adds product surface too early and conflicts with the phase boundary |

**Installation:**
```bash
mvn -q -pl milestory-backend test
npm --prefix milestory-frontend test -- --watch=false
```

**Version verification:**
- `npm view @angular/core version time --json` → `21.2.7` published `2026-04-01`
- `npm view vitest version time --json` → `4.1.2` published `2026-03-26`
- `npm view @openapitools/openapi-generator-cli version time --json` → `2.31.0` published `2026-03-24`
- `pom.xml` currently pins Spring Boot `4.0.4`
- Spring Boot official reference index currently shows `4.0.5`
- Maven Central lookup for `org.mapstruct:mapstruct` shows `1.6.3`

## Implementation Seams

### Existing seams to extend
| Area | Concrete seam | Why it matters |
|------|---------------|----------------|
| API contract | `milestory-api/rest/schemas/goal-planning.yaml`, `milestory-api/rest/paths/goal-by-id.yaml`, `milestory-api/rest/paths/goals.yaml` | Phase 3 should lead with contract additions, not backend-first changes |
| Backend aggregate | `milestory-backend/src/main/java/com/ybritto/milestory/goal/domain/Goal.java` | Goal already owns checkpoint ordering, target invariants, archive lifecycle |
| Backend persistence | `GoalPersistencePort`, `GoalPersistenceAdapter`, `GoalJpaEntity` | Existing adapter can be extended to read/write progress entries with no architecture break |
| Frontend state | `milestory-frontend/src/app/features/goals/shared/goal-planning.store.ts` | Existing goal-detail store already loads/saves/archives goals and should absorb progress refresh logic |
| Frontend surface | `milestory-frontend/src/app/features/goals/detail/goal-detail.page.*` | Locked decision says this remains the primary progress-entry and status experience |

### Contract implications
- Add a dedicated progress-entry create endpoint under the goal resource. Recommended shape:
```text
POST /api/v1/goals/{goalId}/progress-entries
```
- Add `GoalProgressEntryCreateRequest` with:
  - `entryDate: string (date)`
  - `progressValue: number`
  - `note?: string`
- Add `GoalProgressEntryResponse` with:
  - `progressEntryId`
  - `entryDate`
  - `progressValue`
  - `note`
  - `entryType` enum: `NORMAL`, `CORRECTION`
  - `recordedAt`
- Enrich `GoalResponse` with a derived projection, not raw frontend math:
  - `currentProgressValue`
  - `progressPercentOfTarget`
  - `expectedProgressValueToday`
  - `progressDeltaFromPlan`
  - `paceStatus` enum: `BEHIND`, `ON_PACE`, `AHEAD`
  - `paceStatusSummary`
  - `paceStatusDetail`
  - `progressEntries[]`
  - optional checkpoint annotation fields such as `todayMarker`, `isLatestPastCheckpoint`, `deltaFromActual`
- Keep list responses aligned with detail responses enough for later dashboard reuse. At minimum, list goals should expose `currentProgressValue`, `progressPercentOfTarget`, and `paceStatus`.

## Architecture Patterns

### Recommended Project Structure
```text
milestory-backend/src/main/java/com/ybritto/milestory/goal/
├── domain/                     # Goal aggregate, progress entry value/entity, pace policy
├── application/model/          # CreateProgressEntryCommand, derived progress/status view models
├── application/usecase/        # RecordProgressEntry, GetGoalDetail, ListGoals updates
├── application/port/out/       # progress-aware persistence port methods
├── in/controller/              # request/response mapper additions
└── out/adapter/                # JPA entities/repos for progress entries

milestory-frontend/src/app/features/goals/
├── detail/                     # status block, progress history, checkpoint annotations, overlay host
├── progress-entry/             # focused overlay component + form
└── shared/goal-planning.store.ts
```

### Pattern 1: Append-only cumulative progress entries
**What:** Store every user-submitted cumulative snapshot as its own record. Never overwrite previous entries in Phase 3.
**When to use:** All progress writes in this phase.
**Why:** It preserves history, supports visible logs, and makes correction handling explicit.
**Implementation note:** Determine `entryType` at write time by comparing the new cumulative value to the latest prior cumulative value by `entryDate` then `recordedAt`.
**Example:**
```java
// Source pattern: repo goal use cases + cumulative-entry decision
GoalProgressEntry next = GoalProgressEntry.record(
    UUID.randomUUID(),
    goalId,
    entryDate,
    progressValue,
    note,
    progressValue.compareTo(previous.progressValue()) < 0 ? EntryType.CORRECTION : EntryType.NORMAL,
    Instant.now(clock)
);
```

### Pattern 2: Derived expected-progress interpolation
**What:** Compute `expected by today` from the checkpoint path on the backend.
**When to use:** Goal detail reads, goal list reads, status/explanation generation.
**Recommendation:** Use piecewise linear interpolation between checkpoints, with an implicit start point of `0` at `planningYear-01-01`. This keeps the result intuitive and avoids month-end step jumps that would make status feel arbitrary.
**Tolerance band:** Start with `max(targetValue * 0.03, one checkpoint increment / 2)` capped by product judgment. This is a recommendation, not a locked rule. It is explainable and avoids status churn around exact boundaries.
**Example:**
```java
// Source: derived from Milestory checkpoint model
BigDecimal expectedToday = expectedProgressPath.valueAt(today);
BigDecimal delta = actualCurrent.subtract(expectedToday);
PaceStatus status = tolerance.contains(delta) ? ON_PACE : delta.signum() < 0 ? BEHIND : AHEAD;
```

### Pattern 3: Thin frontend, backend-owned explanation
**What:** The frontend renders backend-provided status labels, summaries, and comparison values.
**When to use:** Goal detail hero, comparison block, checkpoint context labels.
**Why:** Product constraints explicitly keep progress logic on the backend.
**Example:**
```typescript
// Source pattern: Angular signals guide + existing store pattern
readonly goal = this.goalPlanningStore.goal;
readonly paceStatus = computed(() => this.goal()?.paceStatus ?? null);
readonly comparison = computed(() => ({
  actual: this.goal()?.currentProgressValue ?? 0,
  expected: this.goal()?.expectedProgressValueToday ?? 0,
}));
```

### Backend domain/application concerns
- Keep progress rules Spring-free. The new progress/status policy belongs in `goal.domain` or an adjacent domain service, not in controllers or JPA entities.
- Preserve archive semantics: archived goals remain read-only, including progress entry creation.
- Keep generated OpenAPI DTOs at the boundary. Add application commands/models for progress entry creation and derived status payloads.
- Avoid storing denormalized pace status as source of truth in the database for Phase 3. Recompute on reads from persisted checkpoints + progress history.
- `GetGoalDetailUseCase` and `ListGoalsUseCase` become richer read assemblers. They are the natural place to invoke progress/status derivation before mapping to API DTOs.

### Persistence considerations
- Add a new Liquibase change set after `002-goal-planning-foundation.yaml`.
- Recommended table:
```text
goal_progress_entry
  progress_entry_id UUID PK
  goal_id UUID FK -> goal.goal_id
  entry_date DATE not null
  progress_value NUMERIC(19,4) not null
  note TEXT null
  entry_type VARCHAR(32) not null
  recorded_at TIMESTAMPTZ not null
```
- Add indexes:
  - `(goal_id, entry_date desc, recorded_at desc)` for latest-entry lookup
  - `(goal_id, recorded_at desc)` for history rendering
- Do not enforce monotonic progress in the database. Corrections are allowed by product decision.
- Do enforce non-negative `progress_value` unless the product later defines a domain where negative cumulative totals make sense.
- Keep `BigDecimal` scale at `19,4` to match the existing goal/checkpoint persistence and avoid serialization drift.

### Frontend integration approach
- Keep `GoalPlanningStore` as the single feature store for Phase 3 unless file growth becomes unreasonable.
- Add store methods:
  - `createProgressEntry(goalId, request)`
  - `dismissProgressFeedback()`
  - possibly a small local UI signal for overlay open/close if not handled in component state
- After successful save:
  - close overlay
  - refresh goal detail from backend
  - show a brief success message
- Use a lightweight reactive form in a dedicated overlay component.
- Keep progress history on the detail page, ordered newest first.
- Status block layout should be:
  - primary badge/headline
  - single sentence summary
  - small `actual so far` vs `expected by today` block
  - checkpoint list below with context annotations

### Anti-Patterns to Avoid
- **Frontend status math:** Do not compute `paceStatus`, tolerance, or expected progress in Angular.
- **Delta-based writes:** Do not accept “add 5 more” in the Phase 3 contract.
- **Mutating history entries in place:** Edit/delete is deferred; preserve append-only history for now.
- **Persisted stale summaries:** Do not add `goal.current_status` columns that need manual synchronization.
- **Separate progress micro-feature:** Do not create a disconnected route/workflow when the locked UX is goal detail first.

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| HTTP client layer | Handwritten fetch wrappers for each endpoint | Existing OpenAPI-generated Angular client | Already established, strongly typed, and contract-driven |
| DTO mapping | Manual controller field copying | Existing MapStruct mapper approach | Reduces drift as `GoalResponse` grows |
| Schema evolution | Manual SQL outside Liquibase | New Liquibase change set | Current repo already depends on migrations at runtime and in tests |
| Form state wiring | Ad hoc DOM state for the overlay | Angular reactive forms + signals around view state | Easier validation, accessibility, and testability |
| Status derivation in UI | Template/helper math over checkpoints | Backend use case/domain service | Product constraint and future dashboard reuse |

**Key insight:** The deceptively hard part of this phase is not storing one more number. It is keeping progress history, expected-path math, tolerance behavior, archive rules, list/detail consistency, and future dashboard reuse coherent. Reuse the repo’s contract-first, backend-owned seams instead of introducing custom shortcuts.

## Common Pitfalls

### Pitfall 1: Treating cumulative snapshots like deltas
**What goes wrong:** Progress totals drift or corrections double-count.
**Why it happens:** Write-path code appends the entered value to the previous value instead of replacing the “current total” concept.
**How to avoid:** Name the request field `progressValue` and document it as cumulative. Tests should assert that the latest entry becomes the current state directly.
**Warning signs:** A lower correction entry produces a higher total, or the UI needs to “replay” entries to understand the latest value.

### Pitfall 2: Step-function expected progress
**What goes wrong:** Status flips sharply around checkpoint dates and feels unfair.
**Why it happens:** Expected progress is taken from the last checkpoint only, not interpolated across the interval.
**How to avoid:** Use linear interpolation between checkpoints and an explicit tolerance band.
**Warning signs:** A goal looks “behind” for most of the month and suddenly becomes “on pace” only on checkpoint day.

### Pitfall 3: Stale denormalized status
**What goes wrong:** Goal list and goal detail disagree about progress or pace status.
**Why it happens:** Status gets persisted separately or computed in multiple places.
**How to avoid:** Use one backend derivation path shared by detail and list reads.
**Warning signs:** Controller mappers or Angular components start containing duplicated threshold logic.

### Pitfall 4: Archive rule leaks
**What goes wrong:** Archived goals still accept progress updates or show action affordances incorrectly.
**Why it happens:** New progress endpoint omits lifecycle checks or the UI forgets to disable entry controls.
**How to avoid:** Block in the backend first, then mirror in the UI.
**Warning signs:** Archived detail page still renders “Add progress” as an active action.

### Pitfall 5: Ambiguous correction messaging
**What goes wrong:** Users think the app lost progress rather than accepted a correction.
**Why it happens:** Lower cumulative entries are stored silently without explanation.
**How to avoid:** Mark the entry type explicitly and return explanation copy hooks for the UI.
**Warning signs:** History list shows a lower number with no explanation.

## Code Examples

Verified patterns from official sources and the existing repo:

### Angular writable and readonly signals
```typescript
// Source: https://angular.dev/guide/signals
private readonly progressDialogOpenState = signal(false);
readonly progressDialogOpen = this.progressDialogOpenState.asReadonly();

openDialog(): void {
  this.progressDialogOpenState.set(true);
}

closeDialog(): void {
  this.progressDialogOpenState.set(false);
}
```

### Angular computed projection for detail rendering
```typescript
// Source: https://angular.dev/guide/signals
readonly statusSummary = computed(() => {
  const goal = this.goal();
  if (!goal) {
    return null;
  }

  return {
    label: goal.paceStatus,
    actual: goal.currentProgressValue,
    expected: goal.expectedProgressValueToday,
    detail: goal.paceStatusDetail,
  };
});
```

### Angular template control flow for status/history
```html
<!-- Source: https://angular.dev/guide/templates/control-flow -->
@if (goal(); as goal) {
  <section>
    <h2>{{ goal.paceStatusSummary }}</h2>

    @for (entry of goal.progressEntries; track entry.progressEntryId) {
      <article>{{ entry.progressValue }}</article>
    }
  </section>
}
```

### Reactive form for progress entry overlay
```typescript
// Source: https://angular.dev/guide/forms/reactive-forms
readonly form = new FormGroup({
  entryDate: new FormControl('', { nonNullable: true }),
  progressValue: new FormControl(0, { nonNullable: true }),
  note: new FormControl('', { nonNullable: true }),
});
```

### Backend request-to-command mapping pattern
```java
// Source pattern: existing GoalRequestMapper / GoalResponseMapper
public record CreateProgressEntryCommand(
        LocalDate entryDate,
        BigDecimal progressValue,
        String note
) {}
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| Angular NgModule-first app structure | Standalone components are recommended for new Angular code | Current official Angular docs, v21 docs build | Fits repo instructions and avoids module overhead |
| Structural directives everywhere | Native `@if` / `@for` control flow blocks | Current Angular template guidance | Matches current repo templates and keeps templates cleaner |
| Manual REST client layers in frontend apps | OpenAPI-generated Angular clients | Mature standard practice; current generator docs support Angular 21 | Stronger contract fidelity and less handwritten drift |

**Deprecated/outdated:**
- `NgModule` for new code as the default composition style: Angular docs explicitly recommend standalone components for new code.
- Frontend-owned business rules for progress status: outdated for this project because backend-owned domain logic is a locked product constraint.

## Recommended Plan Slices

1. **Contract and migration foundation**
   - Extend OpenAPI schemas/paths for progress entries and derived status fields
   - Regenerate backend/frontend clients
   - Add Liquibase table and indexes for progress entries

2. **Backend progress write path**
   - Add progress-entry domain model and persistence
   - Implement `RecordProgressEntryUseCase`
   - Enforce archived goal read-only behavior
   - Classify correction vs normal entries

3. **Backend read-model enrichment**
   - Add progress aggregation and expected-path interpolation
   - Enrich `GetGoalDetailUseCase` and `ListGoalsUseCase`
   - Map new fields through `GoalResponseMapper`

4. **Frontend goal detail integration**
   - Add status hero block and compact comparison panel
   - Add visible history list
   - Add checkpoint annotations driven by backend fields

5. **Frontend entry workflow**
   - Add overlay component and reactive form
   - Submit via store, refresh detail, show success/error feedback
   - Ensure focus management and AXE-safe interactions

6. **Cross-cutting verification**
   - Add backend domain/use-case/controller tests
   - Add frontend store/page tests
   - Verify list/detail consistency and archive restrictions

## Open Questions

1. **Should progress entry ordering be by `entryDate` only or by `entryDate` plus `recordedAt`?**
   - What we know: entries are dated and correction entries are allowed.
   - What's unclear: same-day multiple updates need a deterministic “latest” rule.
   - Recommendation: use `entryDate` as the user-facing chronology and `recordedAt` as the tie-breaker for current-state aggregation.

2. **How large should the on-pace tolerance band be?**
   - What we know: a small tolerance band is explicitly allowed and should not feel arbitrary.
   - What's unclear: exact product threshold.
   - Recommendation: start with a documented backend constant and cover boundary tests so the planner can tune copy and UX around a stable rule.

3. **Should checkpoint annotations be fully precomputed by backend or partly assembled by frontend?**
   - What we know: backend-owned logic is a hard constraint.
   - What's unclear: whether the backend should return fully phrased checkpoint context or only structural data.
   - Recommendation: return structural data plus short summary strings; keep final layout in frontend but all comparisons in backend.

## Validation Architecture

### Test Framework
| Property | Value |
|----------|-------|
| Framework | JUnit 5 via Spring Boot test stack; Vitest via Angular builder |
| Config file | Backend: [`application-test.yaml`](/Users/ybritto/dev/Personal/Milestory/milestory-backend/src/test/resources/application-test.yaml); Frontend: [`angular.json`](/Users/ybritto/dev/Personal/Milestory/milestory-frontend/angular.json) |
| Quick run command | `mvn -q -pl milestory-backend -Dtest='*Goal*Test,*Goal*IntegrationTest' test` |
| Full suite command | `mvn -q test && npm --prefix milestory-frontend test -- --watch=false` |

### Phase Requirements → Test Map
| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| PROG-01 | Record dated cumulative progress entry, including correction classification | backend unit + controller integration | `mvn -q -pl milestory-backend -Dtest='RecordProgressEntryUseCaseTest,GoalControllerIntegrationTest' test` | ❌ Wave 0 |
| PROG-02 | Aggregate latest cumulative progress and percent of target | backend unit | `mvn -q -pl milestory-backend -Dtest='GoalProgressStatusServiceTest' test` | ❌ Wave 0 |
| PROG-03 | Return `BEHIND` / `ON_PACE` / `AHEAD` with tolerance behavior | backend unit | `mvn -q -pl milestory-backend -Dtest='GoalProgressStatusServiceTest' test` | ❌ Wave 0 |
| PROG-04 | Show status explanation and checkpoint context on goal detail | frontend unit + backend controller integration | `npm --prefix milestory-frontend test -- --watch=false --include='src/app/features/goals/detail/goal-detail.page.spec.ts'` | ⚠️ extend existing |

### Sampling Rate
- **Per task commit:** targeted backend or frontend command for the files touched
- **Per wave merge:** `mvn -q -pl milestory-backend test` or `npm --prefix milestory-frontend test -- --watch=false`
- **Phase gate:** `mvn -q test && npm --prefix milestory-frontend test -- --watch=false`

### Wave 0 Gaps
- [ ] `milestory-backend/src/test/java/com/ybritto/milestory/goal/application/usecase/RecordProgressEntryUseCaseTest.java` — covers PROG-01
- [ ] `milestory-backend/src/test/java/com/ybritto/milestory/goal/domain/GoalProgressStatusServiceTest.java` — covers PROG-02 and PROG-03
- [ ] Extend [`GoalControllerIntegrationTest.java`](/Users/ybritto/dev/Personal/Milestory/milestory-backend/src/test/java/com/ybritto/milestory/goal/in/controller/GoalControllerIntegrationTest.java) — covers progress entry endpoint and enriched detail/list payloads
- [ ] Extend [`goal-planning.store.spec.ts`](/Users/ybritto/dev/Personal/Milestory/milestory-frontend/src/app/features/goals/shared/goal-planning.store.spec.ts) — covers create-progress and refresh flow
- [ ] Extend [`goal-detail.page.spec.ts`](/Users/ybritto/dev/Personal/Milestory/milestory-frontend/src/app/features/goals/detail/goal-detail.page.spec.ts) — covers status block, history, and checkpoint annotations

## Sources

### Primary (HIGH confidence)
- Angular signals guide: https://angular.dev/guide/signals
- Angular control flow guide: https://angular.dev/guide/templates/control-flow
- Angular reactive forms guide: https://angular.dev/guide/forms/reactive-forms
- Angular NgModules guidance: https://angular.dev/guide/ngmodules/overview
- OpenAPI Generator `typescript-angular` docs: https://openapi-generator.tech/docs/generators/typescript-angular/
- Spring Boot reference index: https://docs.spring.io/spring-boot/reference/index.html
- openapi-processor release notes/docs: https://openapiprocessor.io/oap/new/index.html
- npm registry package metadata via `npm view` for `@angular/core`, `vitest`, `@openapitools/openapi-generator-cli`

### Secondary (MEDIUM confidence)
- Maven Central lookup for `org.mapstruct:mapstruct`
- Maven Central lookup for `org.springframework.boot:spring-boot-starter-parent`
- Maven Central lookup for `io.openapiprocessor:openapi-processor-maven-plugin` (not fully aligned with official project docs)

### Tertiary (LOW confidence)
- None

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH - Most of the stack is verified from repo manifests, official docs, and package registries; `openapi-processor` version metadata remains MEDIUM confidence due registry mismatch.
- Architecture: HIGH - The repo already demonstrates the intended goal slice seams, and the phase context locks backend-owned logic and detail-page-first UX.
- Pitfalls: HIGH - Pitfalls come directly from the locked cumulative-entry design, existing archive rules, and common plan-vs-actual modeling mistakes.

**Research date:** 2026-04-04
**Valid until:** 2026-05-04
