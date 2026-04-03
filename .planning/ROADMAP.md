# Roadmap: Milestory

**Created:** 2026-04-03
**Phases:** 4
**v1 requirements:** 18
**Mapped requirements:** 18
**Unmapped requirements:** 0

## Roadmap Summary

| Phase | Name | Goal | Requirements |
|-------|------|------|--------------|
| 1 | Foundation And Personal Mode | Turn the scaffold into a real backend-driven personal app foundation with migrations, domain boundaries, and auth-free access mode | PLAT-01, PLAT-02 |
| 2 | Goal Planning And Checkpoints | Let the user define yearly goals and manage editable checkpoint plans across different goal categories | GOAL-01, GOAL-02, GOAL-03, GOAL-04, PLAN-01, PLAN-02, PLAN-03 |
| 3 | Progress Engine And Status | Let the user record progress and understand plan-versus-actual status using backend-owned calculations | PROG-01, PROG-02, PROG-03, PROG-04 |
| 4 | Dashboard And Motivational UX | Deliver the first polished Milestory experience with dashboard summaries, attention cues, accomplishment bands, and goal drill-downs | PLAT-03, DASH-01, DASH-02, DASH-03, DASH-04 |

## Phase Details

### Phase 1: Foundation And Personal Mode

**Goal:** Replace scaffold gaps with a real application skeleton that supports personal auth-free use, backend-owned domain logic, and future DDD/hexagonal growth.

**Requirements:** PLAT-01, PLAT-02

**Success criteria:**
1. Backend package structure clearly separates domain, application, and infrastructure concerns
2. Liquibase migrations exist and the application can boot against the configured database baseline
3. API contract and frontend client generation flow still work after foundation cleanup
4. The application can run in a personal mode without sign-in while preserving a path to future auth

### Phase 2: Goal Planning And Checkpoints

**Goal:** Enable creation and maintenance of yearly goals with editable, backend-suggested checkpoints.

**Requirements:** GOAL-01, GOAL-02, GOAL-03, GOAL-04, PLAN-01, PLAN-02, PLAN-03

**Success criteria:**
1. User can create, edit, and archive yearly goals through the product UI
2. Goal model supports multiple categories without forking the entire workflow per category
3. Backend generates sensible checkpoint suggestions for a yearly goal
4. User can review and adjust checkpoints before committing the plan

### Phase 3: Progress Engine And Status

**Goal:** Build the tracking core that compares actual progress to the yearly plan and explains status clearly.

**Requirements:** PROG-01, PROG-02, PROG-03, PROG-04

**Success criteria:**
1. User can record dated progress entries for an existing goal
2. Backend can aggregate recorded progress into a current cumulative state
3. Each goal exposes an understandable under-plan, on-track, or above-plan status
4. Goal detail explains current status in relation to expected checkpoints

### Phase 4: Dashboard And Motivational UX

**Goal:** Turn the working tracking core into a motivating, polished Milestory experience centered on a strong dashboard.

**Requirements:** PLAT-03, DASH-01, DASH-02, DASH-03, DASH-04

**Success criteria:**
1. The starter Angular placeholder is fully replaced by Milestory-specific product screens
2. Dashboard summarizes current goal health and highlights where attention is needed
3. Dashboard and detail views show accomplishment bands such as 80%, 100%, and 120%
4. User can move from summary views into individual goal detail without confusion

## Phase Ordering Rationale

Phase 1 addresses current scaffold risks before feature growth. Phase 2 defines the plan model that every later screen depends on. Phase 3 establishes trustworthy calculations before dashboard polish. Phase 4 turns the underlying engine into the motivating UX that delivers the actual product value.

## Coverage Check

All v1 requirements map to exactly one phase.

---
*Last updated: 2026-04-03 after initial roadmap creation*
