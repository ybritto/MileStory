package com.ybritto.milestory.goal.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public final class GoalCheckpoint {

    private final UUID checkpointId;
    private final int sequenceNumber;
    private final LocalDate checkpointDate;
    private final BigDecimal targetValue;
    private final String note;
    private final String origin;
    private final LocalDate originalCheckpointDate;
    private final BigDecimal originalTargetValue;

    private GoalCheckpoint(
            UUID checkpointId,
            int sequenceNumber,
            LocalDate checkpointDate,
            BigDecimal targetValue,
            String note,
            String origin,
            LocalDate originalCheckpointDate,
            BigDecimal originalTargetValue
    ) {
        this.checkpointId = Objects.requireNonNull(checkpointId, "checkpointId must not be null");
        this.sequenceNumber = sequenceNumber;
        this.checkpointDate = Objects.requireNonNull(checkpointDate, "checkpointDate must not be null");
        this.targetValue = requirePositive(targetValue, "targetValue");
        this.note = note == null ? "" : note.trim();
        this.origin = requireText(origin, "origin");
        this.originalCheckpointDate = originalCheckpointDate;
        this.originalTargetValue = originalTargetValue;

        if (sequenceNumber < 1) {
            throw new IllegalArgumentException("sequenceNumber must start at 1");
        }
    }

    public static GoalCheckpoint of(
            UUID checkpointId,
            int sequenceNumber,
            LocalDate checkpointDate,
            BigDecimal targetValue,
            String note,
            String origin
    ) {
        return new GoalCheckpoint(checkpointId, sequenceNumber, checkpointDate, targetValue, note, origin, null, null);
    }

    public static GoalCheckpoint of(
            UUID checkpointId,
            int sequenceNumber,
            LocalDate checkpointDate,
            BigDecimal targetValue,
            String note,
            String origin,
            LocalDate originalCheckpointDate,
            BigDecimal originalTargetValue
    ) {
        return new GoalCheckpoint(
                checkpointId,
                sequenceNumber,
                checkpointDate,
                targetValue,
                note,
                origin,
                originalCheckpointDate,
                originalTargetValue
        );
    }

    public UUID checkpointId() {
        return checkpointId;
    }

    public int sequenceNumber() {
        return sequenceNumber;
    }

    public LocalDate checkpointDate() {
        return checkpointDate;
    }

    public BigDecimal targetValue() {
        return targetValue;
    }

    public String note() {
        return note;
    }

    public String origin() {
        return origin;
    }

    public LocalDate originalCheckpointDate() {
        return originalCheckpointDate;
    }

    public BigDecimal originalTargetValue() {
        return originalTargetValue;
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
