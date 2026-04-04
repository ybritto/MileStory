package com.ybritto.milestory.goal.application.usecase;

import com.ybritto.milestory.goal.application.model.GoalPlanPreview;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class PreviewGoalPlanUseCase {

    private static final BigDecimal MONTHS_IN_YEAR = BigDecimal.valueOf(12);
    private static final String SUGGESTED_ORIGIN = "SUGGESTED";

    private final Clock clock;

    public PreviewGoalPlanUseCase() {
        this(Clock.systemUTC());
    }

    public PreviewGoalPlanUseCase(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    public GoalPlanPreview preview(PreviewGoalPlanCommand command) {
        PreviewGoalPlanCommand draft = Objects.requireNonNull(command, "command must not be null");
        Year planningYear = Year.now(clock);
        BigDecimal targetValue = requirePositive(draft.targetValue(), "targetValue");
        BigDecimal monthlyIncrement = targetValue.divide(MONTHS_IN_YEAR, 4, RoundingMode.HALF_UP);

        List<GoalPlanPreview.Checkpoint> checkpoints = new ArrayList<>();
        for (int monthIndex = 1; monthIndex <= 12; monthIndex++) {
            YearMonth yearMonth = planningYear.atMonth(monthIndex);
            BigDecimal checkpointTarget = monthIndex == 12
                    ? targetValue
                    : monthlyIncrement.multiply(BigDecimal.valueOf(monthIndex)).setScale(4, RoundingMode.HALF_UP);
            checkpoints.add(new GoalPlanPreview.Checkpoint(
                    UUID.randomUUID(),
                    monthIndex,
                    yearMonth.atEndOfMonth(),
                    checkpointTarget.stripTrailingZeros(),
                    fallbackNote(draft.categoryKey(), monthIndex, planningYear),
                    SUGGESTED_ORIGIN,
                    null,
                    null
            ));
        }

        return new GoalPlanPreview(
                planningYear,
                requireText(draft.title(), "title"),
                Objects.requireNonNull(draft.categoryId(), "categoryId must not be null"),
                targetValue.stripTrailingZeros(),
                requireText(draft.unit(), "unit"),
                requireText(draft.motivation(), "motivation"),
                requireText(draft.notes(), "notes"),
                GoalPlanPreview.SuggestionBasis.GENERIC_FALLBACK,
                false,
                "Even monthly milestones from January through December; refine the generic fallback to match your real pace.",
                List.copyOf(checkpoints)
        );
    }

    private String fallbackNote(String categoryKey, int monthIndex, Year planningYear) {
        String categoryLabel = (categoryKey == null || categoryKey.isBlank()) ? "goal" : categoryKey.trim();
        return "Month %d of %d for your %s goal. Generic fallback suggestion; refine as needed."
                .formatted(monthIndex, planningYear.getValue(), categoryLabel);
    }

    private static BigDecimal requirePositive(BigDecimal value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");
        if (value.signum() <= 0) {
            throw new IllegalArgumentException(fieldName + " must be positive");
        }
        return value;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }

    public record PreviewGoalPlanCommand(
            String title,
            UUID categoryId,
            String categoryKey,
            BigDecimal targetValue,
            String unit,
            String motivation,
            String notes
    ) {
    }
}
