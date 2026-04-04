package com.ybritto.milestory.goal.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.Year;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class Goal {

    private final UUID goalId;
    private final Year planningYear;
    private final String title;
    private final UUID categoryId;
    private final BigDecimal targetValue;
    private final String unit;
    private final String motivation;
    private final String notes;
    private final GoalStatus status;
    private final String suggestionBasis;
    private final boolean customizedFromSuggestion;
    private final Instant archivedAt;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final List<GoalCheckpoint> checkpoints;

    private Goal(
            UUID goalId,
            Year planningYear,
            String title,
            UUID categoryId,
            BigDecimal targetValue,
            String unit,
            String motivation,
            String notes,
            GoalStatus status,
            String suggestionBasis,
            boolean customizedFromSuggestion,
            Instant archivedAt,
            Instant createdAt,
            Instant updatedAt,
            List<GoalCheckpoint> checkpoints
    ) {
        this.goalId = Objects.requireNonNull(goalId, "goalId must not be null");
        this.planningYear = Objects.requireNonNull(planningYear, "planningYear must not be null");
        this.title = requireText(title, "title");
        this.categoryId = Objects.requireNonNull(categoryId, "categoryId must not be null");
        this.targetValue = requirePositive(targetValue, "targetValue");
        this.unit = requireText(unit, "unit");
        this.motivation = requireText(motivation, "motivation");
        this.notes = requireText(notes, "notes");
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.suggestionBasis = requireText(suggestionBasis, "suggestionBasis");
        this.customizedFromSuggestion = customizedFromSuggestion;
        this.archivedAt = archivedAt;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt must not be null");
        this.checkpoints = List.copyOf(validateCheckpoints(checkpoints, this.targetValue));

        if (status == GoalStatus.ARCHIVED && archivedAt == null) {
            throw new IllegalArgumentException("archived goals must have archivedAt");
        }
        if (status == GoalStatus.ACTIVE && archivedAt != null) {
            throw new IllegalArgumentException("active goals cannot have archivedAt");
        }
    }

    public static Goal create(
            UUID goalId,
            Year planningYear,
            String title,
            UUID categoryId,
            BigDecimal targetValue,
            String unit,
            String motivation,
            String notes,
            String suggestionBasis,
            boolean customizedFromSuggestion,
            Instant createdAt,
            List<GoalCheckpoint> checkpoints
    ) {
        return new Goal(
                goalId,
                planningYear,
                title,
                categoryId,
                targetValue,
                unit,
                motivation,
                notes,
                GoalStatus.ACTIVE,
                suggestionBasis,
                customizedFromSuggestion,
                null,
                createdAt,
                createdAt,
                checkpoints
        );
    }

    public Goal update(
            String title,
            UUID categoryId,
            BigDecimal targetValue,
            String unit,
            String motivation,
            String notes,
            String suggestionBasis,
            boolean customizedFromSuggestion,
            Instant updatedAt,
            List<GoalCheckpoint> checkpoints
    ) {
        assertActiveForMutation("update");
        return new Goal(
                goalId,
                planningYear,
                title,
                categoryId,
                targetValue,
                unit,
                motivation,
                notes,
                GoalStatus.ACTIVE,
                suggestionBasis,
                customizedFromSuggestion,
                null,
                createdAt,
                Objects.requireNonNull(updatedAt, "updatedAt must not be null"),
                checkpoints
        );
    }

    public Goal archive(Instant archivedAt) {
        assertActiveForMutation("archive");
        Instant archiveTimestamp = Objects.requireNonNull(archivedAt, "archivedAt must not be null");
        return new Goal(
                goalId,
                planningYear,
                title,
                categoryId,
                targetValue,
                unit,
                motivation,
                notes,
                GoalStatus.ARCHIVED,
                suggestionBasis,
                customizedFromSuggestion,
                archiveTimestamp,
                createdAt,
                archiveTimestamp,
                checkpoints
        );
    }

    public Goal restore(Instant restoredAt, List<GoalCheckpoint> checkpoints) {
        if (status != GoalStatus.ARCHIVED) {
            throw new IllegalStateException("Only archived goals can be restored");
        }
        return new Goal(
                goalId,
                planningYear,
                title,
                categoryId,
                targetValue,
                unit,
                motivation,
                notes,
                GoalStatus.ACTIVE,
                suggestionBasis,
                customizedFromSuggestion,
                null,
                createdAt,
                Objects.requireNonNull(restoredAt, "restoredAt must not be null"),
                checkpoints
        );
    }

    public UUID goalId() {
        return goalId;
    }

    public Year planningYear() {
        return planningYear;
    }

    public String title() {
        return title;
    }

    public UUID categoryId() {
        return categoryId;
    }

    public BigDecimal targetValue() {
        return targetValue;
    }

    public String unit() {
        return unit;
    }

    public String motivation() {
        return motivation;
    }

    public String notes() {
        return notes;
    }

    public GoalStatus status() {
        return status;
    }

    public String suggestionBasis() {
        return suggestionBasis;
    }

    public boolean customizedFromSuggestion() {
        return customizedFromSuggestion;
    }

    public Instant archivedAt() {
        return archivedAt;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    public List<GoalCheckpoint> checkpoints() {
        return checkpoints;
    }

    private void assertActiveForMutation(String action) {
        if (status != GoalStatus.ACTIVE) {
            throw new IllegalStateException("Cannot " + action + " an archived goal");
        }
    }

    private static List<GoalCheckpoint> validateCheckpoints(List<GoalCheckpoint> checkpoints, BigDecimal targetValue) {
        if (checkpoints == null || checkpoints.isEmpty()) {
            throw new IllegalArgumentException("checkpoints must not be empty");
        }

        List<GoalCheckpoint> ordered = new ArrayList<>(checkpoints);
        ordered.sort(Comparator.comparingInt(GoalCheckpoint::sequenceNumber));

        GoalCheckpoint previous = null;
        for (int index = 0; index < ordered.size(); index++) {
            GoalCheckpoint current = ordered.get(index);
            int expectedSequence = index + 1;
            if (current.sequenceNumber() != expectedSequence) {
                throw new IllegalArgumentException("checkpoint sequence must be contiguous starting at 1");
            }

            if (previous != null) {
                if (!current.checkpointDate().isAfter(previous.checkpointDate())) {
                    throw new IllegalArgumentException("checkpoint dates must be strictly increasing");
                }
                if (current.targetValue().compareTo(previous.targetValue()) <= 0) {
                    throw new IllegalArgumentException("checkpoint targets must increase");
                }
            }
            previous = current;
        }

        GoalCheckpoint lastCheckpoint = ordered.getLast();
        if (lastCheckpoint.targetValue().compareTo(targetValue.stripTrailingZeros()) != 0) {
            throw new IllegalArgumentException("final checkpoint must equal target value");
        }
        return ordered;
    }

    private static BigDecimal requirePositive(BigDecimal value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");
        if (value.signum() <= 0) {
            throw new IllegalArgumentException(fieldName + " must be positive");
        }
        return value.stripTrailingZeros();
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }
}
