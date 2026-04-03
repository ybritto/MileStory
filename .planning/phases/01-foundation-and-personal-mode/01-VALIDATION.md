---
phase: 01
slug: foundation-and-personal-mode
status: ready
nyquist_compliant: true
wave_0_complete: true
created: 2026-04-03
---

# Phase 01 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit 5 + Spring Boot Test; Angular unit-test builder with Vitest 4.x |
| **Config file** | `milestory-frontend/angular.json`; backend uses Spring Boot defaults |
| **Quick run command** | `mvn -q -pl milestory-backend test` |
| **Full suite command** | `mvn -q test` and `cd milestory-frontend && npm run test:ci` |
| **Estimated runtime** | ~45 seconds |

---

## Sampling Rate

- **After every task commit:** Run `mvn -q -pl milestory-backend test` or `cd milestory-frontend && npm run test:ci`, depending on the touched area.
- **After every plan wave:** Run `mvn -q test` and `cd milestory-frontend && npm run test:ci`.
- **Before `$gsd-verify-work`:** Full suite must be green and `cd milestory-frontend && npm run client-gen` must produce no unexpected drift.
- **Max feedback latency:** 60 seconds

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|-----------|-------------------|-------------|--------|
| 01-01-01 | 01 | 1 | PLAT-01 | contract verification | `rg -n "security:|bearerAuth|/api/v1/status|FoundationStatusResponse|FoundationStatusUnavailableResponse" milestory-api/rest/api-v1.yaml milestory-api/rest/paths/status.yaml milestory-api/rest/schemas/status.yaml` | ✅ same task | ⬜ pending |
| 01-01-02 | 01 | 1 | PLAT-01 | docs/config verification | `rg -n "Project Template|template-api|template-backend|template-frontend|Milestory|Phase 1|com\\.ybritto\\.milestory|package-name: com.ybritto.milestory.generated|client-gen" README.md milestory-backend/src/main/resources/openapi-processor-mapping.yaml milestory-frontend/package.json` | ✅ same task | ⬜ pending |
| 01-02-01 | 02 | 2 | PLAT-02 | backend smoke | `mvn -q -pl milestory-backend -Dtest=MilestoryBackendApplicationTests test` | ✅ same task | ⬜ pending |
| 01-02-02 | 02 | 2 | PLAT-01, PLAT-02 | backend unit + integration | `mvn -q -pl milestory-backend -Dtest=PersonalModeStatusIntegrationTest,AppStatusServiceTest,DomainIsolationTest test` | ✅ same task | ⬜ pending |
| 01-03-01 | 03 | 3 | PLAT-01, PLAT-02 | frontend store | `cd milestory-frontend && npm run client-gen && npm run test:ci -- --include src/app/core/status/foundation-status.store.spec.ts` | ✅ same task | ⬜ pending |
| 01-03-02 | 03 | 3 | PLAT-01 | frontend component | `cd milestory-frontend && npm run test:ci` | ✅ same task | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

No separate Wave 0 is required for this revision. Every task in Plans `01` through `03` now verifies with files or tests created in the same task or earlier completed tasks, so execution can proceed in order without pre-seeding extra test scaffolds.

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Milestory home screen matches `01-UI-SPEC.md` typography, color, spacing, and three-zone layout contract | PLAT-01 | Visual contract spans styling and responsive behavior not fully covered by current automated tests | Run the frontend locally, verify `/` shows brand intro, foundation status card, and build notes strip on desktop and mobile widths, and confirm no auth copy or starter content remains |
| Repository identity reads as Milestory rather than a generic template in top-level docs and visible metadata | PLAT-01 | Product-facing cleanup is partly document and naming review work | Read `README.md`, module names, and visible app copy to confirm Milestory branding is primary and Phase 1 scope is described honestly |

---

## Validation Sign-Off

- [ ] All tasks have `<automated>` verify or Wave 0 dependencies
- [ ] All task-level automated commands reference artifacts available at that point in execution
- [ ] Sampling continuity: no 3 consecutive tasks without automated verify
- [ ] No MISSING verification references remain
- [ ] No watch-mode flags
- [ ] Feedback latency < 60s
- [ ] `nyquist_compliant: true` set in frontmatter

**Approval:** pending
