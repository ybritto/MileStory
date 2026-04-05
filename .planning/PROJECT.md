# Milestory

## What This Is

Milestory is a backend-driven web application for defining and following through on annual goals and New Year resolutions. It helps a single user break goals into checkpoints, record progress across very different goal types, and quickly see whether they are behind, on track, or exceeding plan. The product focus is a motivating, high-quality experience backed by strong domain modeling rather than frontend-heavy business logic.

## Core Value

Milestory must make yearly goals feel concrete, measurable, and motivating by showing clear progress against a plan the user can actually follow.

## Requirements

### Validated

- ✓ Contract-first multi-module project structure exists across API, backend, and frontend modules — existing
- ✓ Spring Boot, PostgreSQL, Liquibase, and Angular scaffolding are already wired into the repository — existing
- ✓ Shared API-driven generation flow exists for backend interfaces and frontend client code — existing
- ✓ User can use the first release in personal single-user mode without authentication — validated in Phase 1: Foundation And Personal Mode
- ✓ Backend-owned domain logic now drives the Phase 1 foundation status flow across the API, backend, and frontend shell — validated in Phase 1: Foundation And Personal Mode
- ✓ User can record progress updates and see whether actual progress is under plan, on track, or above plan — validated in Phase 3: Progress Engine And Status

### Active

- [ ] User can create and manage personal yearly goals across multiple categories such as financial, fitness, reading, and weight
- [ ] User can define a target outcome for each goal and receive suggested checkpoints for the year that remain editable
- [ ] User can view a dashboard that summarizes goal health, progress trends, and motivational accomplishment levels
- [ ] All business rules remain in the backend, with the frontend focused on rendering and interaction quality
- [ ] The first release works in personal single-user mode without authentication

### Out of Scope

- Email/password authentication in the first release — intentionally deferred until the core goal-tracking loop proves valuable
- Multi-user collaboration or household/shared goals — the first release is for one user only
- OAuth, social login, or third-party identity providers — too early before single-user product value is established
- Native mobile apps — web-first delivery keeps the first release focused

## Context

Milestory starts from a scaffolded brownfield repository rather than a feature-complete application. The project already uses Java 25, Spring Boot 4, Angular 21, OpenAPI 3.1.2, Lombok, PostgreSQL, Liquibase, and Docker Compose. Phase 1 replaced the bootstrap-only backend and starter frontend with a real auth-free status foundation, Phase 2 established editable yearly goals and checkpoint plans, and Phase 3 added persisted progress entries plus backend-owned pace status on the goal detail experience. UX excellence and technical architecture remain explicit project drivers, and the intended engineering style is DDD, hexagonal architecture, backend-owned business logic, and TDD.

The first release should optimize for personal usefulness instead of broad market scope. That means the roadmap should prioritize goal definition, checkpoint planning, progress calculations, dashboards, and motivational feedback before identity, collaboration, or platform expansion. The repo also still contains some template-era documentation and metadata that should be cleaned as the real product shape solidifies.

## Constraints

- **Tech stack**: Keep the current Java/Spring Boot/Angular/OpenAPI/PostgreSQL foundation — the repository is already organized around this toolchain
- **Architecture**: Keep domain and progress logic on the backend — this is a deliberate product and maintainability choice
- **Design quality**: UX excellence is a top-level driver — dashboard and planning flows cannot be treated as placeholder admin UI
- **Delivery scope**: First release is single-user and auth-free — focus is proving the core goal loop before access control
- **Engineering style**: DDD, hexagonal architecture, and TDD should guide implementation — this reduces the risk of drifting into an anemic CRUD structure

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Personal single-user first release | Reduces product and architecture scope so the core planning and tracking loop can ship sooner | — Pending |
| Defer authentication until after the first release | Auth is important, but not part of the initial value hypothesis | — Pending |
| Keep all business logic on the backend | Goal progress, checkpointing, and motivational scoring are domain logic, not view logic | — Pending |
| Build on the existing contract-first monorepo | The repository already has API, backend, and frontend generation flow in place | — Pending |
| Keep UI library choice open initially, with Angular Material as the default lean | Angular Material is likely the safer default for accessibility and cohesion, but PrimeNG can be reconsidered if dashboard needs justify it | — Pending |

## Evolution

This document evolves at phase transitions and milestone boundaries.

**After each phase transition** (via `$gsd-transition`):
1. Requirements invalidated? → Move to Out of Scope with reason
2. Requirements validated? → Move to Validated with phase reference
3. New requirements emerged? → Add to Active
4. Decisions to log? → Add to Key Decisions
5. "What This Is" still accurate? → Update if drifted

**After each milestone** (via `$gsd-complete-milestone`):
1. Full review of all sections
2. Core Value check — still the right priority?
3. Audit Out of Scope — reasons still valid?
4. Update Context with current state

---
*Last updated: 2026-04-06 after Phase 3 completion*
