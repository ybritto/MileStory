# Phase 02: Goal Planning And Checkpoints - Research

**Researched:** 2026-04-04
**Domain:** Goal planning domain modeling, checkpoint suggestion flow, contract-first backend/frontend expansion
**Confidence:** MEDIUM

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions
### Goal model shape
- **D-01:** Goal creation should require `title`, `category`, `target value`, and `unit`, with the year implied by the current planning cycle rather than entered explicitly.
- **D-02:** Phase 2 should ship with a controlled starter category set, but the user must also be able to create custom categories.
- **D-03:** Category should lightly influence checkpoint suggestions in Phase 2, but all goals should still use one shared core planning flow rather than category-specific sub-products.
- **D-04:** Goals should include both a notes field and a motivation/why field in Phase 2.

### Checkpoint planning style
- **D-05:** Suggested checkpoints should default to monthly checkpoints across the year.
- **D-06:** Default suggestion pacing should optimize for even progress through the year rather than front-loading or back-loading.
- **D-07:** Suggested checkpoints must be fully editable before save: the user can change dates, target values, add checkpoints, and remove checkpoints.
- **D-08:** When the user edits a suggested checkpoint, the experience should allow adding a note explaining why the checkpoint changed.
- **D-09:** If the system cannot infer a strong category-specific plan, it should fall back to a generic plan and explicitly note that the user should refine it.

### Creation and editing flow
- **D-10:** Goal creation should use a guided flow: enter goal details first, then review and edit suggested checkpoints before the final save.
- **D-11:** Checkpoint review should use a card-based editor rather than a table or timeline editor.
- **D-12:** Before saving, the user should see goal details, the checkpoint list, and a simple planned-path summary.
- **D-13:** When the user diverges meaningfully from the system suggestion, the UI should communicate that the plan has been customized from the original suggestion.

### Archive behavior
- **D-14:** Archiving should remove a goal from active views while keeping it readable in a dedicated archive area.
- **D-15:** Archived goals should be read-only until they are restored.
- **D-16:** Restoring an archived goal should bring it back with its latest checkpoint plan intact, but the restore flow should ask the user to confirm or regenerate checkpoints.
- **D-17:** Archive controls should exist in Phase 2, but remain secondary and low-emphasis rather than visually dominant.

### Claude's Discretion
- The exact starter category list, as long as it clearly covers the intended initial use cases and supports custom additions
- The precise wording and visual treatment of the “customized from suggestion” signal
- The exact shape of the simple planned-path summary, as long as it stays understandable and lightweight
- The specific rules used to generate light category-aware monthly checkpoint suggestions, as long as they remain backend-owned and easy for the user to edit

### Deferred Ideas (OUT OF SCOPE)
- Progress entry, plan-versus-actual status, and dashboard feedback remain in later phases
- Category-specific workflows that materially diverge by domain are out of scope for Phase 2
- Delete semantics, long-term archive analytics, or richer archive management belong in later work if needed
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| GOAL-01 | User can create a yearly goal with title, category, target value, and unit of measure | Goal aggregate fields, preview-create flow, category table, reactive form shape |
| GOAL-02 | User can edit goal details after creation | `PUT /api/v1/goals/{goalId}`, aggregate update rules, read-only archive guard |
| GOAL-03 | User can archive a goal that is no longer active | Soft archive status, archive/restore endpoints, dedicated archive route |
| GOAL-04 | User can define goals across multiple categories such as financial, fitness, reading, and weight | Seeded starter categories plus custom categories; shared cumulative target model |
| PLAN-01 | User receives suggested checkpoints for the year when creating a goal | Backend preview endpoint and monthly suggestion algorithm |
| PLAN-02 | User can edit suggested checkpoints before saving the plan | Editable checkpoint cards, checkpoint provenance fields, finalized create/update payload |
| PLAN-03 | User can review checkpoint schedule and expected progress for a goal after creation | Goal detail/read model with checkpoint list, planned-path summary, suggestion metadata |
</phase_requirements>

## Summary

Phase 2 should establish the first full Milestory feature slice around a single `Goal` aggregate with ordered `GoalCheckpoint` children and a separate `GoalCategory` catalog. The simplest design that satisfies the phase is not generic CRUD. It is a backend-owned two-step flow: preview a suggested yearly plan from goal draft details, then persist the finalized goal with the user-edited checkpoints. That keeps checkpoint math, fallback rules, and customization detection on the backend, which is consistent with Phase 1 and the project constraints.

The most important planning decision is to treat Phase 2 checkpoint values as cumulative progress from a zero baseline toward a target, not as arbitrary category-specific absolute states. That keeps one planning model viable across savings, reading, fitness, and custom goals without introducing current-state inputs that do not exist yet. Category should influence explanation copy and minor rule selection, but not fork the product into separate planning engines.

Archive behavior should be a first-class status transition, not deletion. Archived goals remain readable, are excluded from default active queries, reject edits while archived, and restore through an explicit backend action that either keeps the latest checkpoint plan or regenerates it from the current rules.

**Primary recommendation:** Plan Phase 2 around one `goal` feature slice with a preview endpoint, a persisted `Goal` aggregate, seeded plus custom categories, soft archive semantics, and a guided Angular route flow that edits backend-suggested monthly checkpoints before save.

## Standard Stack

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| OpenAPI | 3.1.2 (repo pinned) | Contract-first API for preview/create/edit/archive flows | Already governs backend and frontend generation in this repo |
| Spring Boot | 4.0.4 (repo pinned) | Backend runtime, MVC, dependency management | Already established in Phase 1; changing it now is scope drift |
| Spring Data JPA | Boot-managed via 4.0.4 | Goal/category/checkpoint persistence | Standard persistence layer for relational aggregate storage |
| Liquibase | Boot-managed via 4.0.4 | Schema evolution for goals/categories/checkpoints | Already the project-standard migration mechanism |
| MapStruct | 1.6.3 | Mapping domain/application models to transport and persistence DTOs | Already configured and aligned with backend AGENTS guidance |
| Angular | 21.2.x | Frontend guided flow, lazy routes, signals-based UI | Already established and current in the repo |
| Reactive Forms | Angular 21 built-in | Goal draft and checkpoint editing forms | Official Angular default for structured editable forms |

### Supporting
| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| Vitest via Angular unit-test builder | Repo `4.0.8`, npm latest `4.1.2` | Frontend unit tests | Store, route, and page/component verification |
| `@openapitools/openapi-generator-cli` | Repo `2.31.0` | Generated Angular client | Regenerate after OpenAPI contract expansion |
| PostgreSQL driver | Boot-managed via 4.0.4 | Relational persistence | Goal/category/checkpoint storage in local and test DBs |
| `java.time` (`Year`, `YearMonth`, `LocalDate`) | Java 25 | Planning-cycle and monthly checkpoint dates | Do all checkpoint date math here, not with strings |
| `BigDecimal` | Java 25 | Target and checkpoint numeric values | Avoid floating-point drift in cumulative plans |

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| Seeded + custom category table | Goal category as free text | Simpler schema, but loses starter catalog governance and makes custom-category reuse harder |
| Preview endpoint + final create/update | Persisted draft workflow | Stronger resumability, but adds state and complexity not required by Phase 2 |
| One `goal` slice with checkpoint entity | Separate top-level `checkpoint` feature | More modular later, but premature now because checkpoints are not independent in Phase 2 |
| Reactive forms | Template-driven forms | Lower ceremony, but weaker structure for nested editable checkpoint cards |

**Installation:**
```bash
cd milestory-frontend && npm install
mvn -pl milestory-api,milestory-backend test
```

**Version verification:**
```bash
npm view @angular/core version
npm view @angular/cli version
npm view vitest version
npm view @openapitools/openapi-generator-cli version
curl -s 'https://search.maven.org/solrsearch/select?q=g:%22org.mapstruct%22+AND+a:%22mapstruct%22&core=gav&rows=5&wt=json'
```

Verified on 2026-04-04:
- `@angular/core`: `21.2.7`, published 2026-04-01
- `@angular/cli`: `21.2.6`, published 2026-04-01
- `vitest`: `4.1.2`, published 2026-03-26
- `@openapitools/openapi-generator-cli`: `2.31.0`, published 2026-03-24
- `mapstruct`: `1.6.3`, Maven Central timestamp 2024-11-09

Note: the repo is already pinned to Angular `21.2.0/21.2.3`, Vitest `4.0.8`, and Spring Boot `4.0.4`. Keep repo-pinned backend versions for Phase 2 unless there is a separate maintenance plan.

## Architecture Patterns

### Recommended Project Structure
```text
milestory-backend/src/main/java/com/ybritto/milestory/
└── goal/
    ├── domain/                  # Goal aggregate, GoalCheckpoint entity, value objects, statuses
    ├── application/model/       # Goal draft, preview, summaries, command models
    ├── application/port/out/    # Goal/category persistence ports
    ├── application/usecase/     # Preview, create, update, list, archive, restore use cases
    ├── in/controller/           # Generated API implementation + request/response mappers
    └── out/adapter/             # JPA entities/repos, configuration, persistence adapters

milestory-frontend/src/app/
└── features/goals/
    ├── goal.routes.ts           # Lazy route entry points
    ├── create/                  # Step 1 draft page
    ├── plan-review/             # Step 2 checkpoint card editor
    ├── detail/                  # Active goal detail + planned-path summary
    ├── archive/                 # Dedicated archive area
    └── shared/                  # Form models, card components, presentational helpers
```

### Pattern 1: Goal Aggregate With Ordered Checkpoints
**What:** Model `Goal` as the aggregate root. `GoalCheckpoint` is an internal ordered entity, not a top-level aggregate in this phase.
**When to use:** For create, edit, archive, restore, and post-create review flows where checkpoints must remain consistent with the goal target and category.
**Recommended fields:**

`Goal`
- `goalId: UUID`
- `planningYear: Year`
- `title: String`
- `categoryId: UUID`
- `targetValue: BigDecimal`
- `unit: String`
- `motivation: String`
- `notes: String`
- `status: ACTIVE | ARCHIVED`
- `suggestionBasis: CATEGORY_AWARE | GENERIC_FALLBACK`
- `customizedFromSuggestion: boolean`
- `archivedAt: Instant?`
- `createdAt: Instant`
- `updatedAt: Instant`
- `checkpoints: List<GoalCheckpoint>`

`GoalCheckpoint`
- `checkpointId: UUID`
- `sequenceNumber: int`
- `checkpointDate: LocalDate`
- `targetValue: BigDecimal`
- `note: String?`
- `origin: SUGGESTED | USER_EDITED | USER_ADDED`
- `originalCheckpointDate: LocalDate?`
- `originalTargetValue: BigDecimal?`

**Why this shape:** It preserves the final saved plan, supports D-08 and D-13, and keeps archive/restore simple because the entire plan lives under the goal.

### Pattern 2: Separate Category Catalog, Not Enum-Only Categories
**What:** Keep starter categories in a persisted `goal_category` table with `system_defined = true`; allow user-created rows with `system_defined = false`.
**When to use:** Always. Phase 2 already requires both a controlled starter set and custom additions.
**Starter set recommendation:** `financial`, `fitness`, `reading`, `weight`, `custom`

**Category table minimum shape:**
- `category_id: UUID`
- `key: varchar(64)` nullable for custom rows
- `display_name: varchar(128)`
- `system_defined: boolean`
- `created_at: timestamptz`
- `updated_at: timestamptz`

**Key rule:** Goals should reference categories by ID. Do not store only category display text on the goal row.

### Pattern 3: Preview Then Persist
**What:** Split planning into a non-persisted preview step and a final create/update step.
**When to use:** Goal creation and restore-with-regeneration.

**Recommended OpenAPI surface:**
| Method | Path | Purpose |
|--------|------|---------|
| `GET` | `/api/v1/goal-categories` | Return starter and custom categories |
| `POST` | `/api/v1/goal-categories` | Create a custom category |
| `POST` | `/api/v1/goal-plans/previews` | Accept goal draft details and return suggested checkpoints |
| `POST` | `/api/v1/goals` | Persist a goal with finalized checkpoints |
| `GET` | `/api/v1/goals` | List goals, filter by `status=active|archived` |
| `GET` | `/api/v1/goals/{goalId}` | Goal detail with checkpoint plan |
| `PUT` | `/api/v1/goals/{goalId}` | Update goal details and checkpoint plan |
| `POST` | `/api/v1/goals/{goalId}/archive` | Archive goal |
| `POST` | `/api/v1/goals/{goalId}/restore` | Restore goal, body carries `KEEP_EXISTING` or `REGENERATE` |

**Why this surface:** It keeps the preview ephemeral and backend-owned, avoids draft persistence complexity, and makes restore semantics explicit.

### Pattern 4: Even Monthly Checkpoint Suggestion Rules
**What:** Generate 12 cumulative checkpoints, one per month, with even pacing and light category-aware copy.
**When to use:** Goal creation preview, goal update re-planning if offered, restore regeneration.

**Recommended rules:**
1. Use the current planning cycle year from the backend, not a user-entered year.
2. Generate one checkpoint per month using the last calendar day of each month.
3. Treat checkpoint values as cumulative progress from `0` to `targetValue`.
4. Distribute progress evenly across 12 months.
5. Compute with `BigDecimal`; for non-even divisions, carry remainder so the final month equals the exact target.
6. Category should influence explanation text and note copy, not the core pacing curve.
7. When the category has no special handling, return `suggestionBasis = GENERIC_FALLBACK` and an explicit note telling the user to refine the plan.

**Example preview response behavior:**
- Savings goal `1200 USD` -> monthly cumulative targets `100, 200, ... 1200`
- Reading goal `24 books` -> monthly cumulative targets `2, 4, ... 24`
- Custom goal `75 sessions` -> evenly rounded cumulative series ending exactly at `75`

### Pattern 5: Archive As State Transition
**What:** Archive is a soft state change on `Goal`, not deletion or row movement.
**When to use:** GOAL-03 and D-14 through D-16.

**Rules:**
- Archived goals are excluded from default active queries.
- Archived goals remain readable in a dedicated archive route/view.
- Backend rejects detail/checkpoint edits on archived goals.
- Restore requires a user choice:
  - `KEEP_EXISTING`: restore the saved checkpoints unchanged
  - `REGENERATE`: run current suggestion rules and replace checkpoints

### Frontend Flow Shape
**What:** Use a guided routed flow rather than a single CRUD form.
**When to use:** Goal creation and editing.

**Recommended routes:**
```text
/goals/new           -> goal draft step
/goals/new/plan      -> preview + checkpoint card editor
/goals/:goalId       -> saved goal detail + planned-path summary
/goals/:goalId/edit  -> edit details + checkpoint plan
/goals/archive       -> archived goals list
```

**Recommended UI shape:**
- Step 1 form: title, category picker/create custom category, target value, unit, motivation, notes
- Step 2 plan review: vertically stacked checkpoint cards with date, target value, note, remove action, add-card affordance
- Save summary: compact goal facts, checkpoint count, first/last checkpoint, pacing message, customized badge if the saved plan diverges

### Anti-Patterns to Avoid
- **Free-text-only categories:** It satisfies creation but weakens category reuse and starter-category governance.
- **Frontend-generated checkpoints:** Breaks PLAT-02 and makes later progress/status logic harder to trust.
- **Top-level checkpoint CRUD:** Encourages edits that bypass goal-level invariants.
- **Floating-point numeric targets:** Causes cumulative rounding drift.
- **Hard delete disguised as archive:** Breaks restore semantics and history preservation.
- **Category-specific mini-products:** Violates D-03 and bloats the phase.

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Schema changes | Manual SQL drift outside migrations | Liquibase changesets | Keeps database evolution reviewable and repeatable |
| HTTP DTO clients | Handwritten Angular API services | Generated OpenAPI client | Preserves contract-first workflow |
| Form state orchestration | Ad hoc DOM state | Angular reactive forms + signals | Better for nested checkpoint editing and validation |
| DTO mapping | Controller-level field copying everywhere | MapStruct mappers beside controllers/adapters | Keeps transport mapping isolated |
| Date math | String slicing or JS date-only business logic | `Year`, `YearMonth`, `LocalDate`, `BigDecimal` in backend use cases | Avoids timezone and rounding defects |
| Customization detection in UI only | Frontend diff-only badge logic | Persisted checkpoint provenance + backend flag | Makes archive/restore/detail views consistent |

**Key insight:** The deceptively hard part of this phase is not CRUD. It is preserving one trustworthy planning model across categories while keeping the user’s final plan editable and explainable. Use the stack already in the repo for transport, persistence, and form structure; spend custom logic only on domain rules.

## Common Pitfalls

### Pitfall 1: Modeling Goal Targets As Absolute End-State Values
**What goes wrong:** Categories like weight or savings end up meaning different things, and checkpoint math becomes ambiguous.
**Why it happens:** The phase does not collect a starting value, but teams still try to model end-state numbers.
**How to avoid:** In Phase 2, define target and checkpoint values as cumulative achieved progress from zero baseline.
**Warning signs:** You need category-specific formulas just to compute month 1.

### Pitfall 2: Losing Suggestion Provenance
**What goes wrong:** The UI cannot reliably show “customized from suggestion,” and restore/regenerate flows become guesswork.
**Why it happens:** Teams only save the final checkpoint list.
**How to avoid:** Store checkpoint origin and original suggested date/value when a suggestion is edited.
**Warning signs:** The customized badge is computed only from transient frontend state.

### Pitfall 3: Letting Archive Mean “Hidden But Still Editable”
**What goes wrong:** Archived goals can drift, and the dedicated archive area stops representing stable history.
**Why it happens:** Archive is implemented as a UI filter only.
**How to avoid:** Enforce archive state in backend use cases and reject write operations while archived.
**Warning signs:** `PUT /goals/{id}` still succeeds on archived goals.

### Pitfall 4: Treating Categories As Static Enum Only
**What goes wrong:** Starter categories work, but GOAL-04 custom categories force a redesign.
**Why it happens:** Enum feels easiest during the first implementation.
**How to avoid:** Persist categories from the start with a `system_defined` flag.
**Warning signs:** The API contract exposes category as a hard-coded enum with no custom creation path.

### Pitfall 5: Even Pacing With Naive Rounding
**What goes wrong:** Monthly cumulative values repeat, overshoot, or fail to land exactly on the target.
**Why it happens:** Integer targets are divided by 12 without remainder handling.
**How to avoid:** Use `BigDecimal` calculations and explicitly guarantee the final checkpoint matches the exact target.
**Warning signs:** The last checkpoint differs from the goal target.

## Code Examples

Verified patterns to reuse:

### Angular Lazy Route Entry
```typescript
import { Routes } from '@angular/router';

export const goalRoutes: Routes = [
  {
    path: 'goals/new',
    loadComponent: () =>
      import('./create/goal-create.page').then((module) => module.GoalCreatePage),
  },
];
```
Source: https://angular.dev/guide/routing/common-router-tasks

### Angular Reactive Form Shape For Goal Draft
```typescript
readonly form = this.formBuilder.group({
  title: ['', [Validators.required, Validators.maxLength(120)]],
  categoryId: ['', Validators.required],
  customCategoryName: [''],
  targetValue: [null, [Validators.required, Validators.min(0.01)]],
  unit: ['', [Validators.required, Validators.maxLength(32)]],
  motivation: [''],
  notes: [''],
});
```
Source: https://angular.dev/guide/forms/reactive-forms

### Liquibase Table Addition Pattern
```yaml
databaseChangeLog:
  - changeSet:
      id: 002-goal-baseline
      author: codex
      changes:
        - createTable:
            tableName: goal
            columns:
              - column:
                  name: goal_id
                  type: UUID
                  constraints:
                    primaryKey: true
                    nullable: false
```
Source: project-local established pattern in `001-foundation-baseline.yaml` plus Liquibase change-type reference docs

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| Angular feature NgModules | Standalone lazy-loaded components/routes | Angular 20+ and repo convention | Add goal planning screens as route components, not new modules |
| Frontend-owned feature math | Backend preview and backend save/update use cases | Phase 1 architecture decision, 2026-04-04 | Checkpoint rules belong in backend application/domain code |
| Direct schema edits / Hibernate creation | Liquibase-managed schema changes | Phase 1, 2026-04-04 | Goal/category/checkpoint tables must arrive via changesets |
| One-off text labels for categories | Seeded plus custom persisted categories | Recommended for Phase 2 | Supports both starter taxonomy and user extension without redesign |

**Deprecated/outdated:**
- Template-driven forms for this flow: too weak for nested checkpoint editing and validation.
- Free-text archive filtering only: insufficient for read-only archive semantics.

## Open Questions

1. **Should custom categories be globally reusable or just user-defined labels?**
   - What we know: v1 is single-user and auth-free, so there is only one effective owner.
   - What's unclear: Whether custom categories need later metadata beyond display name.
   - Recommendation: Persist them as reusable category rows now; do not over-model ownership yet.

2. **Should checkpoint dates default to month-end or same day-of-month?**
   - What we know: D-05 requires monthly checkpoints and D-06 requires even pacing.
   - What's unclear: No product decision currently prefers one date anchor.
   - Recommendation: Use month-end dates in Phase 2 because they are deterministic and match end-of-period progress review.

3. **How much category-specific planning logic is worth adding now?**
   - What we know: D-03 and D-09 require only light influence plus generic fallback.
   - What's unclear: Exact copy and rules for each starter category.
   - Recommendation: Keep the math identical across categories; vary only explanation copy and fallback notes in this phase.

## Validation Architecture

### Test Framework
| Property | Value |
|----------|-------|
| Framework | JUnit 5 + Spring Boot Test (backend), Angular unit-test builder with Vitest (frontend) |
| Config file | Backend: `pom.xml`; Frontend: `angular.json` with default Vitest runner; no dedicated `vitest.config.ts` |
| Quick run command | `cd milestory-backend && mvn -q -Dtest=GetFoundationStatusUseCaseTest,FoundationStatusControllerIntegrationTest,DomainIsolationTest test` and `cd milestory-frontend && npm run test:ci -- --include src/app/features/goals/**/*.spec.ts --include src/app/features/goals/**/*.test.ts` |
| Full suite command | `cd milestory-backend && mvn -q test` and `cd milestory-frontend && npm run test:ci` |

Backend targeted command was re-verified on 2026-04-04 and exited with code `0`. Frontend test builder options were verified with `npm run test:ci -- --help`, including support for `--include` and `--filter`.

### Phase Requirements → Test Map
| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| GOAL-01 | Create a goal with title/category/target/unit and saved checkpoints | backend use case + controller integration + frontend form/store | `cd milestory-backend && mvn -q -Dtest=CreateGoalUseCaseTest,GoalControllerIntegrationTest test` | ❌ Wave 0 |
| GOAL-02 | Edit goal details and checkpoint plan after creation | backend use case + controller integration + frontend page/store | `cd milestory-backend && mvn -q -Dtest=UpdateGoalUseCaseTest,GoalControllerIntegrationTest test` | ❌ Wave 0 |
| GOAL-03 | Archive and restore goal with read-only archive behavior | backend use case + controller integration + frontend archive page | `cd milestory-backend && mvn -q -Dtest=ArchiveGoalUseCaseTest,RestoreGoalUseCaseTest test` | ❌ Wave 0 |
| GOAL-04 | Use starter and custom categories | backend category use case + controller integration + frontend category flow | `cd milestory-backend && mvn -q -Dtest=ListGoalCategoriesUseCaseTest,CreateCustomGoalCategoryUseCaseTest test` | ❌ Wave 0 |
| PLAN-01 | Receive suggested monthly checkpoints from backend | backend domain/use case | `cd milestory-backend && mvn -q -Dtest=PreviewGoalPlanUseCaseTest,MonthlyCheckpointSuggestionServiceTest test` | ❌ Wave 0 |
| PLAN-02 | Edit suggested checkpoints before save | frontend component/store + backend update validation | `cd milestory-frontend && npm run test:ci -- --include src/app/features/goals/plan-review` | ❌ Wave 0 |
| PLAN-03 | Review checkpoint schedule and expected progress after creation | backend read model + frontend detail page | `cd milestory-frontend && npm run test:ci -- --include src/app/features/goals/detail` | ❌ Wave 0 |

### Sampling Rate
- **Per task commit:** run the targeted backend/frontend tests for the touched requirement
- **Per wave merge:** `cd milestory-backend && mvn -q test` and `cd milestory-frontend && npm run test:ci`
- **Phase gate:** full backend and frontend suites green before `/gsd:verify-work`

### Wave 0 Gaps
- [ ] `milestory-backend/src/test/java/com/ybritto/milestory/goal/domain/GoalTest.java` — aggregate invariants, archive guard, checkpoint ordering
- [ ] `milestory-backend/src/test/java/com/ybritto/milestory/goal/application/usecase/PreviewGoalPlanUseCaseTest.java` — monthly suggestion and fallback behavior
- [ ] `milestory-backend/src/test/java/com/ybritto/milestory/goal/in/controller/GoalControllerIntegrationTest.java` — preview/create/update/archive/restore endpoints
- [ ] `milestory-backend/src/test/java/com/ybritto/milestory/architecture/GoalDomainIsolationTest.java` — goal domain stays framework-free
- [ ] `milestory-frontend/src/app/features/goals/create/goal-create.page.spec.ts` — draft form and preview handoff
- [ ] `milestory-frontend/src/app/features/goals/plan-review/goal-plan-review.page.spec.ts` — checkpoint card editing and customized badge
- [ ] `milestory-frontend/src/app/features/goals/detail/goal-detail.page.spec.ts` — planned-path summary and read model rendering
- [ ] `milestory-frontend/src/app/features/goals/archive/goal-archive.page.spec.ts` — archive list and restore affordance

## Sources

### Primary (HIGH confidence)
- Local repo context: `.planning/phases/02-goal-planning-and-checkpoints/02-CONTEXT.md`, `.planning/PROJECT.md`, `.planning/ROADMAP.md`, `.planning/REQUIREMENTS.md`
- Local repo implementation patterns: `milestory-backend/src/main/java/com/ybritto/milestory/status/**`, `milestory-frontend/src/app/**`, `milestory-api/rest/api-v1.yaml`
- Angular reactive forms guide: https://angular.dev/guide/forms/reactive-forms
- Angular router tasks guide: https://angular.dev/guide/routing/common-router-tasks
- Spring Data JPA reference: https://docs.spring.io/spring-data/jpa/reference/
- Spring Boot data initialization/Liquibase guidance: https://docs.spring.io/spring-boot/reference/howto/data-initialization.html
- OpenAPI Generator TypeScript Angular docs: https://openapi-generator.tech/docs/generators/typescript-angular/

### Secondary (MEDIUM confidence)
- `npm view @angular/core version time --json`
- `npm view @angular/cli version time --json`
- `npm view vitest version time --json`
- `npm view @openapitools/openapi-generator-cli version time --json`
- Maven Central query for `org.mapstruct:mapstruct`

### Tertiary (LOW confidence)
- Maven Central availability for the repo’s `io.openapiprocessor` `2026.x` versions was not independently verified; keep the repo-pinned plugin versions unchanged for this phase.

## Metadata

**Confidence breakdown:**
- Standard stack: MEDIUM - Angular/frontend versions were verified directly; backend plugin/version currentness is partly repo-pinned and not fully cross-verified
- Architecture: HIGH - strongly constrained by Phase 2 context, Phase 1 code, and repo AGENTS guidance
- Pitfalls: MEDIUM - grounded in the phase constraints and current architecture, but some are domain-model recommendations rather than official framework rules

**Research date:** 2026-04-04
**Valid until:** 2026-05-04
