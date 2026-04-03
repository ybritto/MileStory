# Phase 2: Goal Planning And Checkpoints - Context

**Gathered:** 2026-04-04
**Status:** Ready for planning

<domain>
## Phase Boundary

Phase 2 introduces the first real Milestory planning flow. The user can create, edit, and archive yearly goals, define a target outcome with category/unit/value details, receive backend-suggested checkpoints for the year, and adjust those checkpoints before saving. It does not add progress entry flows, dashboard summaries, authentication, or multi-user behavior.

</domain>

<decisions>
## Implementation Decisions

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

### the agent's Discretion
- The exact starter category list, as long as it clearly covers the intended initial use cases and supports custom additions
- The precise wording and visual treatment of the “customized from suggestion” signal
- The exact shape of the simple planned-path summary, as long as it stays understandable and lightweight
- The specific rules used to generate light category-aware monthly checkpoint suggestions, as long as they remain backend-owned and easy for the user to edit

</decisions>

<specifics>
## Specific Ideas

- The year should feel like an implicit planning cycle, not another form field the user has to manage manually in Phase 2.
- Category matters, but it should not fracture the product into separate flows yet; the app should still feel coherent across financial, fitness, reading, weight, and custom goals.
- Checkpoint editing should preserve user agency: the system suggests a path, but the saved plan should feel like the user's plan.
- Archive behavior should preserve history and reduce clutter, not act like deletion.

</specifics>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Phase definition
- `.planning/ROADMAP.md` — Phase 2 goal, requirements, and success criteria
- `.planning/REQUIREMENTS.md` — GOAL-01 through GOAL-04 and PLAN-01 through PLAN-03
- `.planning/PROJECT.md` — product direction, constraints, and validated architecture decisions
- `.planning/STATE.md` — current project status after Phase 1 completion

### Prior phase decisions
- `.planning/phases/01-foundation-and-personal-mode/01-CONTEXT.md` — auth-free, backend-owned, real-product tone decisions that Phase 2 should preserve
- `.planning/phases/01-foundation-and-personal-mode/01-VERIFICATION.md` — what Phase 1 actually established in the codebase

### Repository instructions
- `AGENTS.md` — repository-wide guidance and template-leftover awareness
- `milestory-backend/AGENTS.md` — backend DDD/hexagonal and TDD expectations
- `milestory-frontend/AGENTS.md` — Angular, accessibility, and frontend architecture requirements
- `milestory-api/AGENTS.md` — OpenAPI and REST contract conventions

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `milestory-frontend/src/app/app.routes.ts` — the routed shell is already in place, so Phase 2 can add goal-planning routes without replacing another starter layer
- `milestory-frontend/src/app/features/home/home.page.ts` — existing product-toned page patterns can guide the goal-planning UX tone and state handling
- `milestory-frontend/src/app/core/status/foundation-status.store.ts` — a signal-based store pattern already exists for backend-driven state
- `milestory-api/rest/api-v1.yaml` — the contract currently exposes only `/api/v1/status`, so Phase 2 can extend the API cleanly from a narrow baseline
- `milestory-backend/src/main/java/com/ybritto/milestory/status/` — the first domain/application/infrastructure slice already demonstrates the intended backend layering

### Established Patterns
- Contract-first remains the governing integration pattern: API changes should lead backend and frontend work
- Backend-owned logic is already validated in Phase 1, so goal planning and checkpoint suggestion rules should stay in backend use cases, not frontend helpers
- The frontend now has a real branded shell, so Phase 2 should extend that product surface rather than introducing CRUD/admin scaffolding
- The app is explicitly auth-free and single-user in v1, so goal planning should not introduce account concepts, ownership prompts, or permission logic

### Integration Points
- `milestory-api/rest/` — new goal and checkpoint endpoints should extend the Phase 1 contract here
- `milestory-backend/src/main/resources/db/changelog/` — Phase 2 persistence should build on the new Liquibase baseline rather than bypassing it
- `milestory-backend/src/main/java/com/ybritto/milestory/` — new goal-planning slices should follow the same seam pattern already used by the status feature
- `milestory-frontend/src/app/app.routes.ts` — this is the current entry point for adding goal creation, review, and archive navigation
- `milestory-frontend/src/styles.scss` and the existing home feature — these already define the Phase 1 visual tone and should anchor the Phase 2 planning experience

</code_context>

<deferred>
## Deferred Ideas

- Progress entry, plan-versus-actual status, and dashboard feedback remain in later phases
- Category-specific workflows that materially diverge by domain are out of scope for Phase 2
- Delete semantics, long-term archive analytics, or richer archive management belong in later work if needed

</deferred>

---

*Phase: 02-goal-planning-and-checkpoints*
*Context gathered: 2026-04-04*
