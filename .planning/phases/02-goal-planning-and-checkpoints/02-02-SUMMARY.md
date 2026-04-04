---
phase: 02-goal-planning-and-checkpoints
plan: 02
subsystem: backend
tags: [openapi, mapstruct, hexagonal-architecture, jpa, goal-lifecycle]
requires:
  - phase: 02-goal-planning-and-checkpoints
    provides: goal planning contract and backend persistence foundation
provides:
  - backend goal slice for create/list/detail/update/archive/restore
  - backend-owned category handling with custom category creation
  - controller boundary mapping from generated API models through MapStruct
affects: [milestory-backend, phase-02]
tech-stack:
  added: []
  patterns: [hexagonal application ports, generated API DTO boundary, stateful goal lifecycle]
key-files:
  created: [milestory-backend/src/main/java/com/ybritto/milestory/goal/application/model/GoalCategory.java, milestory-backend/src/main/java/com/ybritto/milestory/goal/application/model/GoalCheckpointInput.java, milestory-backend/src/main/java/com/ybritto/milestory/goal/application/model/CreateGoalCommand.java, milestory-backend/src/main/java/com/ybritto/milestory/goal/application/model/UpdateGoalCommand.java, milestory-backend/src/main/java/com/ybritto/milestory/goal/application/model/CreateCustomGoalCategoryCommand.java, milestory-backend/src/main/java/com/ybritto/milestory/goal/application/model/RestoreGoalCommand.java, milestory-backend/src/main/java/com/ybritto/milestory/goal/application/port/out/GoalCategoryPersistencePort.java, milestory-backend/src/main/java/com/ybritto/milestory/goal/application/port/out/GoalPersistencePort.java, milestory-backend/src/main/java/com/ybritto/milestory/goal/application/usecase/CreateGoalUseCase.java, milestory-backend/src/main/java/com/ybritto/milestory/goal/application/usecase/ListGoalCategoriesUseCase.java, milestory-backend/src/main/java/com/ybritto/milestory/goal/application/usecase/CreateCustomGoalCategoryUseCase.java, milestory-backend/src/main/java/com/ybritto/milestory/goal/application/usecase/GetGoalDetailUseCase.java, milestory-backend/src/main/java/com/ybritto/milestory/goal/application/usecase/ListGoalsUseCase.java, milestory-backend/src/main/java/com/ybritto/milestory/goal/application/usecase/UpdateGoalUseCase.java, milestory-backend/src/main/java/com/ybritto/milestory/goal/application/usecase/ArchiveGoalUseCase.java, milestory-backend/src/main/java/com/ybritto/milestory/goal/application/usecase/RestoreGoalUseCase.java, milestory-backend/src/main/java/com/ybritto/milestory/goal/application/usecase/GoalCategoryNotFoundException.java, milestory-backend/src/main/java/com/ybritto/milestory/goal/application/usecase/GoalNotFoundException.java, milestory-backend/src/main/java/com/ybritto/milestory/goal/in/controller/GoalController.java, milestory-backend/src/main/java/com/ybritto/milestory/goal/in/controller/GoalRequestMapper.java, milestory-backend/src/main/java/com/ybritto/milestory/goal/in/controller/GoalResponseMapper.java, milestory-backend/src/main/java/com/ybritto/milestory/goal/in/controller/GoalControllerExceptionHandler.java, milestory-backend/src/main/java/com/ybritto/milestory/goal/out/adapter/GoalCategoryJpaEntity.java, milestory-backend/src/main/java/com/ybritto/milestory/goal/out/adapter/GoalJpaEntity.java, milestory-backend/src/main/java/com/ybritto/milestory/goal/out/adapter/GoalCheckpointJpaEntity.java, milestory-backend/src/main/java/com/ybritto/milestory/goal/out/adapter/GoalCategoryJpaRepository.java, milestory-backend/src/main/java/com/ybritto/milestory/goal/out/adapter/GoalJpaRepository.java, milestory-backend/src/main/java/com/ybritto/milestory/goal/out/adapter/GoalPersistenceAdapter.java, milestory-backend/src/main/java/com/ybritto/milestory/goal/out/adapter/GoalConfiguration.java, milestory-backend/src/test/java/com/ybritto/milestory/goal/application/usecase/CreateGoalUseCaseTest.java, milestory-backend/src/test/java/com/ybritto/milestory/goal/application/usecase/ListGoalCategoriesUseCaseTest.java, milestory-backend/src/test/java/com/ybritto/milestory/goal/application/usecase/CreateCustomGoalCategoryUseCaseTest.java, milestory-backend/src/test/java/com/ybritto/milestory/goal/application/usecase/UpdateGoalUseCaseTest.java, milestory-backend/src/test/java/com/ybritto/milestory/goal/application/usecase/ArchiveGoalUseCaseTest.java, milestory-backend/src/test/java/com/ybritto/milestory/goal/application/usecase/RestoreGoalUseCaseTest.java, milestory-backend/src/test/java/com/ybritto/milestory/goal/in/controller/GoalControllerIntegrationTest.java, milestory-backend/src/test/java/com/ybritto/milestory/goal/support/GoalTestSupport.java]
  modified: [milestory-backend/src/main/java/com/ybritto/milestory/goal/application/usecase/PreviewGoalPlanUseCase.java, milestory-backend/src/main/java/com/ybritto/milestory/goal/domain/Goal.java, milestory-backend/src/main/java/com/ybritto/milestory/goal/domain/GoalCheckpoint.java, milestory-backend/src/test/java/com/ybritto/milestory/goal/application/usecase/PreviewGoalPlanUseCaseTest.java, milestory-backend/pom.xml]
key-decisions:
  - "Keep the controller boundary on generated OpenAPI models and MapStruct mappers, while the application layer stays command-based and framework-free."
  - "Use a single persistence adapter for both goals and categories so category lookup, custom category creation, and goal rehydration stay consistent."
  - "Preserve numeric scale in the goal aggregate and checkpoint fixtures so the model stays stable under equality-based tests and JSON serialization."
patterns-established:
  - "Goal lifecycle transitions are enforced in the application and domain layers, not in the controller."
  - "Archived goals remain read-only until restored, and restore supports KEEP_EXISTING versus REGENERATE semantics."
requirements-completed: [GOAL-02, GOAL-03, GOAL-04, PLAN-03]
duration: 17 min
completed: 2026-04-04
---

# Phase 02 Plan 02: Backend goal lifecycle implementation

**Milestory now owns goal creation, listing, detail, editing, archive, restore, and custom category handling behind the contract-defined API.**

## Performance

- **Duration:** 17 min
- **Started:** 2026-04-04T08:30:00Z
- **Completed:** 2026-04-04T08:46:45Z
- **Tasks:** 2

## Task Commits

1. **Plan 02-02 backend implementation** - `2e39afe` (feat)

## Accomplishments
- Implemented the backend goal slice for categories, goal creation, goal detail, list filtering, update, archive, and restore.
- Added the persistence adapter and JPA entities for goals, categories, and ordered checkpoints.
- Wired the controller layer to generated OpenAPI interfaces and kept the application layer hexagonal through ports and use cases.
- Expanded test coverage with focused use-case tests and a controller integration test that exercises the contract payloads end to end.

## Files Created/Modified
- `milestory-backend/src/main/java/com/ybritto/milestory/goal/application/port/out/GoalCategoryPersistencePort.java` and `milestory-backend/src/main/java/com/ybritto/milestory/goal/application/port/out/GoalPersistencePort.java` - Outbound ports for category and goal persistence.
- `milestory-backend/src/main/java/com/ybritto/milestory/goal/application/usecase/CreateGoalUseCase.java`, `ListGoalCategoriesUseCase.java`, `CreateCustomGoalCategoryUseCase.java`, `GetGoalDetailUseCase.java`, `ListGoalsUseCase.java`, `UpdateGoalUseCase.java`, `ArchiveGoalUseCase.java`, and `RestoreGoalUseCase.java` - Backend goal lifecycle use cases.
- `milestory-backend/src/main/java/com/ybritto/milestory/goal/in/controller/GoalController.java`, `GoalRequestMapper.java`, `GoalResponseMapper.java`, and `GoalControllerExceptionHandler.java` - Controller implementation and API-boundary mapping.
- `milestory-backend/src/main/java/com/ybritto/milestory/goal/out/adapter/GoalPersistenceAdapter.java` plus the goal/category JPA entities and repositories - Persistence layer for the goal slice.
- `milestory-backend/src/test/java/com/ybritto/milestory/goal/application/usecase/*.java`, `milestory-backend/src/test/java/com/ybritto/milestory/goal/in/controller/GoalControllerIntegrationTest.java`, and `milestory-backend/src/test/java/com/ybritto/milestory/goal/support/GoalTestSupport.java` - New and updated tests.

## Decisions Made
- Kept the domain framework-free and moved contract translation to MapStruct at the controller edge.
- Preserved archived-goal read-only behavior as a backend rule and mapped conflicts to HTTP errors.
- Implemented restore as explicit `KEEP_EXISTING` and `REGENERATE` behavior so the frontend can choose the lifecycle path intentionally.

## Issues Encountered
- `BigDecimal` scale normalization initially caused equality-based test failures. I fixed that by preserving the original scale in the domain model and removing trailing-zero stripping from the test support checkpoint factory.

## Verification
- `cd milestory-backend && mvn -q -Dtest=CreateGoalUseCaseTest,ListGoalCategoriesUseCaseTest,CreateCustomGoalCategoryUseCaseTest,GoalControllerIntegrationTest test`
- `cd milestory-backend && mvn -q -Dtest=UpdateGoalUseCaseTest,ArchiveGoalUseCaseTest,RestoreGoalUseCaseTest,GoalControllerIntegrationTest test`
- `cd milestory-backend && mvn -q test`

## Self-Check: PASSED

---
*Phase: 02-goal-planning-and-checkpoints*
*Completed: 2026-04-04*
