package com.ybritto.milestory.goal.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ybritto.milestory.goal.application.model.GoalPlanPreview;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PreviewGoalPlanUseCaseTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-04-04T00:00:00Z"), ZoneOffset.UTC);

    @Test
    void generatesEvenMonthlyCumulativeCheckpointsForCurrentPlanningYear() {
        PreviewGoalPlanUseCase useCase = new PreviewGoalPlanUseCase(FIXED_CLOCK);

        GoalPlanPreview preview = useCase.preview(new PreviewGoalPlanUseCase.PreviewGoalPlanCommand(
                "Read 24 books",
                UUID.fromString("00000000-0000-0000-0000-000000000103"),
                "reading",
                BigDecimal.valueOf(24),
                "books",
                "Build a steady reading practice",
                "Favor long-form reading"
        ));

        assertEquals(2026, preview.planningYear().getValue());
        assertEquals(12, preview.checkpoints().size());
        assertEquals(LocalDate.of(2026, 1, 31), preview.checkpoints().getFirst().checkpointDate());
        assertEquals(BigDecimal.valueOf(2), preview.checkpoints().getFirst().targetValue());
        assertEquals(LocalDate.of(2026, 12, 31), preview.checkpoints().getLast().checkpointDate());
        assertEquals(BigDecimal.valueOf(24), preview.checkpoints().getLast().targetValue());
    }

    @Test
    void flagsGenericFallbackWhenNoCategorySpecificHandlingExists() {
        PreviewGoalPlanUseCase useCase = new PreviewGoalPlanUseCase(FIXED_CLOCK);

        GoalPlanPreview preview = useCase.preview(new PreviewGoalPlanUseCase.PreviewGoalPlanCommand(
                "Custom practice",
                UUID.randomUUID(),
                "custom",
                BigDecimal.valueOf(75),
                "sessions",
                "Make time every week",
                "Even pace"
        ));

        assertEquals(GoalPlanPreview.SuggestionBasis.GENERIC_FALLBACK, preview.suggestionBasis());
        assertEquals(BigDecimal.valueOf(75), preview.checkpoints().getLast().targetValue());
        assertEquals(false, preview.customizedFromSuggestion());
    }
}
