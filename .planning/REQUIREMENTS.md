# Requirements: Milestory

**Defined:** 2026-04-03
**Core Value:** Milestory must make yearly goals feel concrete, measurable, and motivating by showing clear progress against a plan the user can actually follow.

## v1 Requirements

### Platform

- [x] **PLAT-01**: User can use the application in personal single-user mode without authentication
- [x] **PLAT-02**: User can rely on backend-owned domain logic for planning, progress, and dashboard status calculations
- [ ] **PLAT-03**: User can interact with a web UI that reflects the real Milestory product instead of starter placeholder content

### Goals

- [ ] **GOAL-01**: User can create a yearly goal with title, category, target value, and unit of measure
- [ ] **GOAL-02**: User can edit goal details after creation
- [ ] **GOAL-03**: User can archive a goal that is no longer active
- [ ] **GOAL-04**: User can define goals across multiple categories such as financial, fitness, reading, and weight

### Checkpoints

- [ ] **PLAN-01**: User receives suggested checkpoints for the year when creating a goal
- [ ] **PLAN-02**: User can edit suggested checkpoints before saving the plan
- [ ] **PLAN-03**: User can review checkpoint schedule and expected progress for a goal after creation

### Progress

- [ ] **PROG-01**: User can record a dated progress update for a goal
- [ ] **PROG-02**: User can view cumulative progress against the target value
- [ ] **PROG-03**: User can see whether a goal is under plan, on track, or above plan at the current point in time
- [ ] **PROG-04**: User can understand how current status relates to planned checkpoints

### Dashboard

- [ ] **DASH-01**: User lands on a dashboard that summarizes current goal health
- [ ] **DASH-02**: User can identify which goals need attention first from the dashboard
- [ ] **DASH-03**: User can see accomplishment levels such as 80%, 100%, and 120% for relevant goals
- [ ] **DASH-04**: User can drill from the dashboard into goal-level progress detail

## v2 Requirements

### Authentication

- **AUTH-01**: User can enter an email address first and be routed to login or signup depending on account state
- **AUTH-02**: User can sign up with email and password
- **AUTH-03**: User can log in with email and password
- **AUTH-04**: User receives email-driven flows for verification and password reset
- **AUTH-05**: User session is protected with JWT-based authorization

### Product Expansion

- **EXP-01**: User can manage multiple accounts instead of personal single-user mode only
- **EXP-02**: User can access more advanced motivational and personalization features once the core loop is validated

## Out of Scope

| Feature | Reason |
|---------|--------|
| Authentication in v1 | Explicitly deferred until the core personal goal loop is working well |
| Shared or collaborative goals | First release is only for one user |
| Social features or community mechanics | Not part of the current product value hypothesis |
| Native mobile apps | Web-first scope keeps the release focused |
| Multiple UI libraries | Unnecessary complexity before the design system direction is set |

## Traceability

| Requirement | Phase | Status |
|-------------|-------|--------|
| PLAT-01 | Phase 1 | Complete |
| PLAT-02 | Phase 1 | Complete |
| PLAT-03 | Phase 4 | Pending |
| GOAL-01 | Phase 2 | Pending |
| GOAL-02 | Phase 2 | Pending |
| GOAL-03 | Phase 2 | Pending |
| GOAL-04 | Phase 2 | Pending |
| PLAN-01 | Phase 2 | Pending |
| PLAN-02 | Phase 2 | Pending |
| PLAN-03 | Phase 2 | Pending |
| PROG-01 | Phase 3 | Pending |
| PROG-02 | Phase 3 | Pending |
| PROG-03 | Phase 3 | Pending |
| PROG-04 | Phase 3 | Pending |
| DASH-01 | Phase 4 | Pending |
| DASH-02 | Phase 4 | Pending |
| DASH-03 | Phase 4 | Pending |
| DASH-04 | Phase 4 | Pending |

**Coverage:**
- v1 requirements: 18 total
- Mapped to phases: 18
- Unmapped: 0 ✓

---
*Requirements defined: 2026-04-03*
*Last updated: 2026-04-03 after initial definition*
