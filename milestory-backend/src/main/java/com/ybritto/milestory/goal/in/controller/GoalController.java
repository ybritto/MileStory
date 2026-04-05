package com.ybritto.milestory.goal.in.controller;

import com.ybritto.milestory.goal.application.usecase.ArchiveGoalUseCase;
import com.ybritto.milestory.goal.application.usecase.CreateCustomGoalCategoryUseCase;
import com.ybritto.milestory.goal.application.usecase.CreateGoalUseCase;
import com.ybritto.milestory.goal.application.usecase.GetGoalDetailUseCase;
import com.ybritto.milestory.goal.application.usecase.ListGoalCategoriesUseCase;
import com.ybritto.milestory.goal.application.usecase.ListGoalsUseCase;
import com.ybritto.milestory.goal.application.usecase.PreviewGoalPlanUseCase;
import com.ybritto.milestory.goal.application.usecase.RecordProgressEntryUseCase;
import com.ybritto.milestory.goal.application.usecase.RestoreGoalUseCase;
import com.ybritto.milestory.goal.application.usecase.UpdateGoalUseCase;
import com.ybritto.milestory.generated.api.GoalCategoriesApi;
import com.ybritto.milestory.generated.api.GoalPlanningApi;
import com.ybritto.milestory.generated.api.GoalsApi;
import com.ybritto.milestory.generated.model.ArchiveGoalResponse;
import com.ybritto.milestory.generated.model.CreateGoalCategoryRequest;
import com.ybritto.milestory.generated.model.CreateGoalRequest;
import com.ybritto.milestory.generated.model.GoalCategoryResponse;
import com.ybritto.milestory.generated.model.GoalDraftRequest;
import com.ybritto.milestory.generated.model.GoalProgressEntryResponse;
import com.ybritto.milestory.generated.model.GoalPlanPreviewResponse;
import com.ybritto.milestory.generated.model.GoalResponse;
import com.ybritto.milestory.generated.model.RecordGoalProgressEntryRequest;
import com.ybritto.milestory.generated.model.RestoreGoalRequest;
import com.ybritto.milestory.generated.model.Status;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class GoalController implements GoalCategoriesApi, GoalPlanningApi, GoalsApi {

    private final PreviewGoalPlanUseCase previewGoalPlanUseCase;
    private final ListGoalCategoriesUseCase listGoalCategoriesUseCase;
    private final CreateCustomGoalCategoryUseCase createCustomGoalCategoryUseCase;
    private final CreateGoalUseCase createGoalUseCase;
    private final GetGoalDetailUseCase getGoalDetailUseCase;
    private final ListGoalsUseCase listGoalsUseCase;
    private final UpdateGoalUseCase updateGoalUseCase;
    private final RecordProgressEntryUseCase recordProgressEntryUseCase;
    private final ArchiveGoalUseCase archiveGoalUseCase;
    private final RestoreGoalUseCase restoreGoalUseCase;
    private final GoalRequestMapper goalRequestMapper;
    private final GoalResponseMapper goalResponseMapper;

    @Override
    public ResponseEntity<GoalCategoryResponse[]> listGoalCategories() {
        return ResponseEntity.ok(goalResponseMapper.mapCategories(listGoalCategoriesUseCase.listGoalCategories()));
    }

    @Override
    public ResponseEntity<GoalCategoryResponse> createGoalCategory(CreateGoalCategoryRequest body) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(goalResponseMapper.mapCategory(createCustomGoalCategoryUseCase.create(
                        goalRequestMapper.toCreateCustomGoalCategoryCommand(body)
                )));
    }

    @Override
    public ResponseEntity<GoalPlanPreviewResponse> previewGoalPlan(GoalDraftRequest body) {
        return ResponseEntity.ok(goalResponseMapper.mapPreview(
                previewGoalPlanUseCase.preview(goalRequestMapper.toPreviewGoalPlanCommand(body))
        ));
    }

    @Override
    public ResponseEntity<GoalResponse[]> listGoals(Status status) {
        return ResponseEntity.ok(goalResponseMapper.mapGoals(listGoalsUseCase.listGoals(goalRequestMapper.toGoalStatus(status))));
    }

    @Override
    public ResponseEntity<GoalResponse> createGoal(CreateGoalRequest body) {
        var created = createGoalUseCase.create(goalRequestMapper.toCreateGoalCommand(body));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(goalResponseMapper.mapGoal(getGoalDetailUseCase.getGoal(created.goalId())));
    }

    @Override
    public ResponseEntity<GoalResponse> getGoal(String goalId) {
        return ResponseEntity.ok(goalResponseMapper.mapGoal(getGoalDetailUseCase.getGoal(goalRequestMapper.toUuid(goalId))));
    }

    @Override
    public ResponseEntity<GoalResponse> updateGoal(String goalId, CreateGoalRequest body) {
        var updated = updateGoalUseCase.update(goalRequestMapper.toUuid(goalId), goalRequestMapper.toUpdateGoalCommand(body));
        return ResponseEntity.ok(goalResponseMapper.mapGoal(
                getGoalDetailUseCase.getGoal(updated.goalId())
        ));
    }

    @Override
    public ResponseEntity<GoalProgressEntryResponse> recordGoalProgressEntry(
            String goalId,
            RecordGoalProgressEntryRequest body
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(goalResponseMapper.mapProgressEntry(recordProgressEntryUseCase.record(
                        goalRequestMapper.toUuid(goalId),
                        goalRequestMapper.toRecordProgressEntryCommand(body)
                )));
    }

    @Override
    public ResponseEntity<ArchiveGoalResponse> archiveGoal(String goalId) {
        return ResponseEntity.ok(goalResponseMapper.mapArchive(archiveGoalUseCase.archive(goalRequestMapper.toUuid(goalId))));
    }

    @Override
    public ResponseEntity<GoalResponse> restoreGoal(String goalId, RestoreGoalRequest body) {
        var restored = restoreGoalUseCase.restore(goalRequestMapper.toUuid(goalId), goalRequestMapper.toRestoreGoalCommand(body));
        return ResponseEntity.ok(goalResponseMapper.mapGoal(
                getGoalDetailUseCase.getGoal(restored.goalId())
        ));
    }
}
