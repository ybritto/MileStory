package com.ybritto.milestory.goal.domain;

import java.math.BigDecimal;
import java.util.Objects;

public record GoalProgressSnapshot(
        BigDecimal currentProgressValue,
        BigDecimal progressPercentOfTarget,
        BigDecimal expectedProgressValueToday,
        GoalPaceStatus paceStatus,
        String paceSummary,
        String paceDetail
) {

    public GoalProgressSnapshot {
        Objects.requireNonNull(currentProgressValue, "currentProgressValue must not be null");
        Objects.requireNonNull(progressPercentOfTarget, "progressPercentOfTarget must not be null");
        Objects.requireNonNull(expectedProgressValueToday, "expectedProgressValueToday must not be null");
        Objects.requireNonNull(paceStatus, "paceStatus must not be null");
        Objects.requireNonNull(paceSummary, "paceSummary must not be null");
        Objects.requireNonNull(paceDetail, "paceDetail must not be null");
    }
}
