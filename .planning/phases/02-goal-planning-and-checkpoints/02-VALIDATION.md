---
phase: 02
slug: goal-planning-and-checkpoints
status: ready
nyquist_compliant: true
wave_0_complete: true
created: 2026-04-04
---

# Phase 02 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit 5 + Spring Boot Test (backend), Angular unit-test builder with Vitest (frontend) |
| **Config file** | Backend: `milestory-backend/pom.xml`; Frontend: `milestory-frontend/angular.json` |
| **Quick run command** | `cd milestory-backend && mvn -q -Dtest=GetFoundationStatusUseCaseTest,FoundationStatusControllerIntegrationTest,DomainIsolationTest test` |
| **Full suite command** | `cd milestory-backend && mvn -q test` and `cd milestory-frontend && npm run test:ci` |
| **Estimated runtime** | ~90 seconds |

---

## Sampling Rate

- **After every task commit:** Run the targeted backend/frontend tests for the touched requirement.
- **After every plan wave:** Run `cd milestory-backend && mvn -q test` and `cd milestory-frontend && npm run test:ci`.
- **Before `$gsd-verify-work`:** Full backend and frontend suites must be green.
- **Max feedback latency:** 90 seconds

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|-----------|-------------------|-------------|--------|
| 02-01-01 | 01 | 1 | GOAL-01, GOAL-04, PLAN-01 | contract verification | `rg -n "/api/v1/goal-categories|/api/v1/goal-plans/previews|/api/v1/goals|GoalPlanPreviewResponse|RestoreGoalRequest" milestory-api/rest/api-v1.yaml milestory-api/rest/paths/*.yaml milestory-api/rest/schemas/goal-planning.yaml` | ✅ same task | ✅ green |
| 02-01-02 | 01 | 1 | GOAL-01, GOAL-04, PLAN-01 | backend domain/use case | `cd milestory-backend && mvn -q -Dtest=GoalTest,PreviewGoalPlanUseCaseTest,GoalDomainIsolationTest test` | ✅ same task | ✅ green |
| 02-02-01 | 02 | 2 | GOAL-01, GOAL-04, PLAN-01, PLAN-03 | backend use case + controller | `cd milestory-backend && mvn -q -Dtest=CreateGoalUseCaseTest,ListGoalCategoriesUseCaseTest,CreateCustomGoalCategoryUseCaseTest,GoalControllerIntegrationTest test` | ✅ same task | ⬜ pending |
| 02-02-02 | 02 | 2 | GOAL-02, GOAL-03, PLAN-03 | backend update/archive/restore | `cd milestory-backend && mvn -q -Dtest=UpdateGoalUseCaseTest,ArchiveGoalUseCaseTest,RestoreGoalUseCaseTest,GoalControllerIntegrationTest test` | ✅ same task | ⬜ pending |
| 02-03-01 | 03 | 3 | GOAL-01, GOAL-04, PLAN-01, PLAN-02 | frontend create/review | `cd milestory-frontend && npm run client-gen && npm run test:ci -- --include src/app/features/goals/create/goal-create.page.spec.ts --include src/app/features/goals/plan-review/goal-plan-review.page.spec.ts --include src/app/features/goals/shared/goal-planning.store.spec.ts` | ✅ same task | ⬜ pending |
| 02-03-02 | 03 | 3 | GOAL-02, GOAL-03, PLAN-03 | frontend detail/archive | `cd milestory-frontend && npm run test:ci -- --include src/app/features/goals/detail/goal-detail.page.spec.ts --include src/app/features/goals/archive/goal-archive.page.spec.ts` | ✅ same task | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

No separate Wave 0 is required for this plan set. The backend domain, controller, and frontend route specs are created in the same tasks that first verify them, so execution can proceed in order without pre-seeding standalone test scaffolding.

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Goal creation flow feels coherent and product-quality across draft, checkpoint review, and final save | GOAL-01, PLAN-02 | Interaction quality, copy, and visual pacing across a multi-step flow are not fully provable via unit tests | Run the frontend with the backend live, create a new goal from start to finish, and confirm the guided flow, card editor, summary, and customized messaging feel clear and intentional |
| Archive and restore behavior is understandable for a real user | GOAL-03 | Human judgment is needed to confirm archive controls are discoverable but secondary, and restore choices are understandable | Archive a goal from the active experience, view it in the archive area, then restore once with keep-existing and once with regenerate to confirm the behavior matches the copy and intent |

---

## Validation Sign-Off

- [ ] All tasks have `<automated>` verify or Wave 0 dependencies
- [ ] All task-level automated commands reference artifacts available at that point in execution
- [ ] Sampling continuity: no 3 consecutive tasks without automated verify
- [ ] No MISSING verification references remain
- [ ] No watch-mode flags
- [ ] Feedback latency < 90s
- [ ] `nyquist_compliant: true` set in frontmatter

**Approval:** pending
