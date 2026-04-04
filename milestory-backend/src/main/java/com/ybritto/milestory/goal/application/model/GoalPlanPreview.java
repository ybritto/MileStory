package com.ybritto.milestory.goal.application.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.UUID;

public record GoalPlanPreview(
        Year planningYear,
        String title,
        UUID categoryId,
        BigDecimal targetValue,
        String unit,
        String motivation,
        String notes,
        SuggestionBasis suggestionBasis,
        boolean customizedFromSuggestion,
        String plannedPathSummary,
        List<Checkpoint> checkpoints
) {

    public enum SuggestionBasis {
        CATEGORY_AWARE,
        GENERIC_FALLBACK
    }

    public record Checkpoint(
            UUID checkpointId,
            int sequenceNumber,
            LocalDate checkpointDate,
            BigDecimal targetValue,
            String note,
            String origin,
            LocalDate originalCheckpointDate,
            BigDecimal originalTargetValue
    ) {
    }
}
