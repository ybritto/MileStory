package com.ybritto.milestory.goal.in.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.ybritto.milestory.goal.domain.GoalCheckpoint;
import com.ybritto.milestory.goal.domain.GoalProgressStatusService;
import com.ybritto.milestory.goal.support.GoalTestSupport;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class GoalControllerIntegrationTest {

    private static final UUID READING_CATEGORY_ID = UUID.fromString("00000000-0000-0000-0000-000000000103");
    private static final String READING_CATEGORY_ID_JSON = "\"" + READING_CATEGORY_ID + "\"";
    private GoalTestSupport.InMemoryGoalCategoryPersistencePort categoryPort;
    private GoalTestSupport.InMemoryGoalPersistencePort goalPort;
    private GoalTestSupport.InMemoryGoalProgressEntryPersistencePort progressEntryPort;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.categoryPort = new GoalTestSupport.InMemoryGoalCategoryPersistencePort();
        this.goalPort = new GoalTestSupport.InMemoryGoalPersistencePort();
        this.progressEntryPort = new GoalTestSupport.InMemoryGoalProgressEntryPersistencePort();

        PreviewGoalPlanUseCase previewGoalPlanUseCase = new PreviewGoalPlanUseCase(categoryPort, GoalTestSupport.FIXED_CLOCK);
        ListGoalCategoriesUseCase listGoalCategoriesUseCase = new ListGoalCategoriesUseCase(categoryPort);
        CreateCustomGoalCategoryUseCase createCustomGoalCategoryUseCase = new CreateCustomGoalCategoryUseCase(categoryPort);
        CreateGoalUseCase createGoalUseCase = new CreateGoalUseCase(goalPort, categoryPort, GoalTestSupport.FIXED_CLOCK);
        GoalProgressStatusService goalProgressStatusService = new GoalProgressStatusService();
        GetGoalDetailUseCase getGoalDetailUseCase = new GetGoalDetailUseCase(
                goalPort,
                progressEntryPort,
                goalProgressStatusService,
                GoalTestSupport.FIXED_CLOCK
        );
        ListGoalsUseCase listGoalsUseCase = new ListGoalsUseCase(
                goalPort,
                progressEntryPort,
                goalProgressStatusService,
                GoalTestSupport.FIXED_CLOCK
        );
        UpdateGoalUseCase updateGoalUseCase = new UpdateGoalUseCase(goalPort, categoryPort, GoalTestSupport.FIXED_CLOCK);
        RecordProgressEntryUseCase recordProgressEntryUseCase = new RecordProgressEntryUseCase(
                goalPort,
                progressEntryPort,
                GoalTestSupport.FIXED_CLOCK
        );
        ArchiveGoalUseCase archiveGoalUseCase = new ArchiveGoalUseCase(goalPort, GoalTestSupport.FIXED_CLOCK);
        RestoreGoalUseCase restoreGoalUseCase = new RestoreGoalUseCase(
                goalPort,
                categoryPort,
                previewGoalPlanUseCase,
                GoalTestSupport.FIXED_CLOCK
        );

        GoalController controller = new GoalController(
                previewGoalPlanUseCase,
                listGoalCategoriesUseCase,
                createCustomGoalCategoryUseCase,
                createGoalUseCase,
                getGoalDetailUseCase,
                listGoalsUseCase,
                updateGoalUseCase,
                recordProgressEntryUseCase,
                archiveGoalUseCase,
                restoreGoalUseCase,
                Mappers.getMapper(GoalRequestMapper.class),
                Mappers.getMapper(GoalResponseMapper.class)
        );

        this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GoalControllerExceptionHandler())
                .build();
    }

    @Test
    void createsCategoriesPreviewsAndPersistsGoals() throws Exception {
        mockMvc.perform(get("/api/v1/goal-categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[0].key", is("financial")));

        mockMvc.perform(post("/api/v1/goal-categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayName\":\"Creative Practice\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.displayName", is("Creative Practice")))
                .andExpect(jsonPath("$.systemDefined", is(false)));

        mockMvc.perform(get("/api/v1/goal-categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(6)))
                .andExpect(jsonPath("$[4].displayName", is("Creative Practice")));

        previewGoalPlan();
        mockMvc.perform(post("/api/v1/goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createGoalRequestJson("Read 24 books", BigDecimal.valueOf(24))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("Read 24 books")))
                .andExpect(jsonPath("$.status", is("ACTIVE")))
                .andExpect(jsonPath("$.currentProgressValue", is(0.0)))
                .andExpect(jsonPath("$.expectedProgressValueToday", is(6.2667)))
                .andExpect(jsonPath("$.paceStatus", is("BEHIND")))
                .andExpect(jsonPath("$.paceSummary",
                        is("You're behind the pace you planned for today, but the year still has room to recover.")))
                .andExpect(jsonPath("$.checkpoints", hasSize(12)))
                .andExpect(jsonPath("$.plannedPathSummary", notNullValue()));

        UUID goalId = goalPort.goals().keySet().iterator().next();

        mockMvc.perform(get("/api/v1/goals/{goalId}", goalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goalId", is(goalId.toString())))
                .andExpect(jsonPath("$.status", is("ACTIVE")))
                .andExpect(jsonPath("$.checkpoints", hasSize(12)))
                .andExpect(jsonPath("$.plannedPathSummary", notNullValue()));

        mockMvc.perform(get("/api/v1/goals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].goalId", is(goalId.toString())));

        mockMvc.perform(get("/api/v1/goals").param("status", "ARCHIVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void updatesArchivesRestoresAndBlocksArchivedEdits() throws Exception {
        previewGoalPlan();
        mockMvc.perform(post("/api/v1/goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createGoalRequestJson("Read 24 books", BigDecimal.valueOf(24))))
                .andExpect(status().isCreated());

        UUID goalId = goalPort.goals().keySet().iterator().next();

        mockMvc.perform(put("/api/v1/goals/{goalId}", goalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createGoalRequestJson("Read 30 books", BigDecimal.valueOf(30))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Read 30 books")))
                .andExpect(jsonPath("$.targetValue", is(30.0)))
                .andExpect(jsonPath("$.currentProgressValue", is(0.0)))
                .andExpect(jsonPath("$.paceStatus", is("BEHIND")));

        mockMvc.perform(post("/api/v1/goals/{goalId}/archive", goalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ARCHIVED")))
                .andExpect(jsonPath("$.archivedAt", notNullValue()));

        mockMvc.perform(get("/api/v1/goals").param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        mockMvc.perform(get("/api/v1/goals").param("status", "ARCHIVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].goalId", is(goalId.toString())));

        mockMvc.perform(put("/api/v1/goals/{goalId}", goalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createGoalRequestJson("Read 32 books", BigDecimal.valueOf(32))))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/v1/goals/{goalId}/restore", goalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mode\":\"KEEP_EXISTING\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ACTIVE")))
                .andExpect(jsonPath("$.checkpoints", hasSize(12)));

        mockMvc.perform(get("/api/v1/goals/{goalId}", goalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ACTIVE")));
    }

    @Test
    void recordsProgressEntriesClassifiesCorrectionsAndBlocksArchivedWrites() throws Exception {
        previewGoalPlan();
        mockMvc.perform(post("/api/v1/goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createGoalRequestJson("Read 24 books", BigDecimal.valueOf(24))))
                .andExpect(status().isCreated());

        UUID goalId = goalPort.goals().keySet().iterator().next();

        mockMvc.perform(post("/api/v1/goals/{goalId}/progress-entries", goalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "entryDate": "2026-03-10",
                                  "progressValue": 8,
                                  "note": "Finished a strong month"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.progressEntryId", notNullValue()))
                .andExpect(jsonPath("$.entryDate", is("2026-03-10")))
                .andExpect(jsonPath("$.progressValue", is(8.0)))
                .andExpect(jsonPath("$.entryType", is("NORMAL")))
                .andExpect(jsonPath("$.recordedAt", notNullValue()));

        mockMvc.perform(post("/api/v1/goals/{goalId}/progress-entries", goalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "entryDate": "2026-04-04",
                                  "progressValue": 6,
                                  "note": "Corrected an overcount"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.entryType", is("CORRECTION")));

        mockMvc.perform(post("/api/v1/goals/{goalId}/archive", goalId))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/goals/{goalId}/progress-entries", goalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "entryDate": "2026-04-05",
                                  "progressValue": 9,
                                  "note": "Should be rejected"
                                }
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void enrichesGoalDetailAndListWithProgressSnapshotsAndCheckpointAnnotations() throws Exception {
        previewGoalPlan();
        mockMvc.perform(post("/api/v1/goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createGoalRequestJson("Read 24 books", BigDecimal.valueOf(24))))
                .andExpect(status().isCreated());

        UUID goalId = goalPort.goals().keySet().iterator().next();

        mockMvc.perform(post("/api/v1/goals/{goalId}/progress-entries", goalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "entryDate": "2026-03-10",
                                  "progressValue": 8,
                                  "note": "Finished a strong month"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/goals/{goalId}/progress-entries", goalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "entryDate": "2026-04-04",
                                  "progressValue": 6,
                                  "note": "Corrected an overcount"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/goals/{goalId}", goalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentProgressValue", is(6.0)))
                .andExpect(jsonPath("$.expectedProgressValueToday", is(6.2667)))
                .andExpect(jsonPath("$.paceStatus", is("ON_PACE")))
                .andExpect(jsonPath("$.paceSummary", is("You're right where this goal expected you to be today.")))
                .andExpect(jsonPath("$.paceDetail",
                        is("Your progress is tracking the checkpoint path you set for this point in the year.")))
                .andExpect(jsonPath("$.progressEntries", hasSize(2)))
                .andExpect(jsonPath("$.progressEntries[0].entryType", is("CORRECTION")))
                .andExpect(jsonPath("$.progressEntries[0].entryDate", is("2026-04-04")))
                .andExpect(jsonPath("$.checkpoints[0].progressContextLabel", is("Latest checkpoint passed")))
                .andExpect(jsonPath("$.checkpoints[0].progressContextDetail", notNullValue()))
                .andExpect(jsonPath("$.checkpoints[3].progressContextLabel", is("Expected by now")));

        mockMvc.perform(get("/api/v1/goals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].currentProgressValue", is(6.0)))
                .andExpect(jsonPath("$[0].expectedProgressValueToday", is(6.2667)))
                .andExpect(jsonPath("$[0].paceStatus", is("ON_PACE")))
                .andExpect(jsonPath("$[0].paceSummary", is("You're right where this goal expected you to be today.")))
                .andExpect(jsonPath("$[0].paceDetail",
                        is("Your progress is tracking the checkpoint path you set for this point in the year.")))
                .andExpect(jsonPath("$[0].progressEntries[0].entryType", is("CORRECTION")))
                .andExpect(jsonPath("$[0].checkpoints[0].progressContextLabel", is("Latest checkpoint passed")));
    }

    private void previewGoalPlan() throws Exception {
        mockMvc.perform(post("/api/v1/goal-plans/previews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Read 24 books",
                                  "categoryId": %s,
                                  "targetValue": 24,
                                  "unit": "books",
                                  "motivation": "Build a steadier reading habit",
                                  "notes": "Read every week"
                                }
                                """.formatted(READING_CATEGORY_ID_JSON)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.planningYear", is(2026)))
                .andExpect(jsonPath("$.suggestionBasis", is("CATEGORY_AWARE")))
                .andExpect(jsonPath("$.checkpoints", hasSize(12)));
    }

    private String createGoalRequestJson(String title, BigDecimal targetValue) {
        String checkpointsJson = GoalTestSupport.checkpoints(targetValue).stream()
                .map(this::toCheckpointJson)
                .reduce((left, right) -> left + "," + right)
                .orElse("");

        return """
                {
                  "title": "%s",
                  "categoryId": %s,
                  "targetValue": %s,
                  "unit": "books",
                  "motivation": "Build a steadier reading habit",
                  "notes": "Read every week",
                  "suggestionBasis": "CATEGORY_AWARE",
                  "customizedFromSuggestion": false,
                  "checkpoints": [%s]
                }
                """.formatted(title, READING_CATEGORY_ID_JSON, targetValue.toPlainString(), checkpointsJson);
    }

    private String toCheckpointJson(GoalCheckpoint checkpoint) {
        return """
                {
                  "checkpointDate": "%s",
                  "targetValue": %s,
                  "note": "%s",
                  "origin": "%s"
                }
                """.formatted(
                checkpoint.checkpointDate(),
                checkpoint.targetValue().toPlainString(),
                checkpoint.note(),
                checkpoint.origin()
        );
    }
}
