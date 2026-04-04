package com.ybritto.milestory.goal.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public record GoalProgressEntry(
        UUID progressEntryId,
        UUID goalId,
        LocalDate entryDate,
        BigDecimal progressValue,
        String note,
        GoalProgressEntryType entryType,
        Instant recordedAt
) {

    public GoalProgressEntry {
        Objects.requireNonNull(progressEntryId, "progressEntryId must not be null");
        Objects.requireNonNull(goalId, "goalId must not be null");
        Objects.requireNonNull(entryDate, "entryDate must not be null");
        Objects.requireNonNull(progressValue, "progressValue must not be null");
        Objects.requireNonNull(entryType, "entryType must not be null");
        Objects.requireNonNull(recordedAt, "recordedAt must not be null");
        if (progressValue.signum() < 0) {
            throw new IllegalArgumentException("progressValue must not be negative");
        }
        note = note == null ? "" : note.trim();
    }

    public static GoalProgressEntry record(
            UUID progressEntryId,
            UUID goalId,
            LocalDate entryDate,
            BigDecimal progressValue,
            String note,
            GoalProgressEntryType entryType,
            Instant recordedAt
    ) {
        return new GoalProgressEntry(progressEntryId, goalId, entryDate, progressValue, note, entryType, recordedAt);
    }
}
