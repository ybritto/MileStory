package com.ybritto.milestory.goal.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class GoalProgressStatusService {

    static final BigDecimal ON_PACE_TOLERANCE_PERCENT_OF_TARGET = new BigDecimal("0.05");

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    private static final int CALCULATION_SCALE = 8;

    private static final String AHEAD_SUMMARY = "You're ahead of the pace you planned for today.";
    private static final String AHEAD_DETAIL =
            "You've already moved past today's planned mark. Keep pressing while this rhythm is working.";
    private static final String ON_PACE_SUMMARY = "You're right where this goal expected you to be today.";
    private static final String ON_PACE_DETAIL =
            "Your progress is tracking the checkpoint path you set for this point in the year.";
    private static final String BEHIND_SUMMARY =
            "You're behind the pace you planned for today, but the year still has room to recover.";
    private static final String BEHIND_DETAIL =
            "You're a bit under today's planned mark. A focused push before the next checkpoint can close the gap.";

    public GoalProgressSnapshot calculate(
            BigDecimal targetValue,
            List<GoalCheckpoint> checkpoints,
            List<GoalProgressEntry> progressEntries,
            LocalDate today
    ) {
        BigDecimal goalTargetValue = requirePositive(targetValue, "targetValue");
        List<GoalCheckpoint> orderedCheckpoints = List.copyOf(Objects.requireNonNull(checkpoints, "checkpoints must not be null"));
        if (orderedCheckpoints.isEmpty()) {
            throw new IllegalArgumentException("checkpoints must not be empty");
        }
        LocalDate evaluationDate = Objects.requireNonNull(today, "today must not be null");

        BigDecimal currentProgressValue = Objects.requireNonNull(progressEntries, "progressEntries must not be null").stream()
                .max(Comparator.comparing(GoalProgressEntry::entryDate).thenComparing(GoalProgressEntry::recordedAt))
                .map(GoalProgressEntry::progressValue)
                .map(BigDecimal::stripTrailingZeros)
                .orElse(BigDecimal.ZERO);
        BigDecimal expectedProgressValueToday = interpolateExpectedProgress(orderedCheckpoints, evaluationDate);
        BigDecimal tolerance = goalTargetValue.multiply(ON_PACE_TOLERANCE_PERCENT_OF_TARGET);
        BigDecimal deltaFromExpected = currentProgressValue.subtract(expectedProgressValueToday);
        GoalPaceStatus paceStatus = derivePaceStatus(deltaFromExpected, tolerance);

        return new GoalProgressSnapshot(
                currentProgressValue,
                currentProgressValue.multiply(ONE_HUNDRED).divide(goalTargetValue, 4, RoundingMode.HALF_UP),
                expectedProgressValueToday,
                paceStatus,
                summaryFor(paceStatus),
                detailFor(paceStatus)
        );
    }

    private BigDecimal interpolateExpectedProgress(List<GoalCheckpoint> checkpoints, LocalDate today) {
        GoalCheckpoint firstCheckpoint = checkpoints.getFirst();
        LocalDate startDate = Year.from(firstCheckpoint.checkpointDate()).atDay(1);
        if (!today.isAfter(startDate)) {
            return BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
        }

        LocalDate segmentStartDate = startDate;
        BigDecimal segmentStartValue = BigDecimal.ZERO;

        for (GoalCheckpoint checkpoint : checkpoints) {
            if (!today.isAfter(checkpoint.checkpointDate())) {
                return interpolateValue(
                        segmentStartDate,
                        segmentStartValue,
                        checkpoint.checkpointDate(),
                        checkpoint.targetValue(),
                        today
                );
            }
            segmentStartDate = checkpoint.checkpointDate();
            segmentStartValue = checkpoint.targetValue();
        }

        return checkpoints.getLast().targetValue().setScale(4, RoundingMode.HALF_UP);
    }

    private BigDecimal interpolateValue(
            LocalDate startDate,
            BigDecimal startValue,
            LocalDate endDate,
            BigDecimal endValue,
            LocalDate today
    ) {
        long totalDays = ChronoUnit.DAYS.between(startDate, endDate);
        if (totalDays <= 0) {
            return endValue.setScale(4, RoundingMode.HALF_UP);
        }

        long elapsedDays = Math.max(0, Math.min(totalDays, ChronoUnit.DAYS.between(startDate, today)));
        BigDecimal progressRatio = BigDecimal.valueOf(elapsedDays)
                .divide(BigDecimal.valueOf(totalDays), CALCULATION_SCALE, RoundingMode.HALF_UP);
        BigDecimal span = endValue.subtract(startValue);
        return startValue.add(span.multiply(progressRatio)).setScale(4, RoundingMode.HALF_UP);
    }

    private GoalPaceStatus derivePaceStatus(BigDecimal deltaFromExpected, BigDecimal tolerance) {
        if (deltaFromExpected.abs().compareTo(tolerance) <= 0) {
            return GoalPaceStatus.ON_PACE;
        }
        return deltaFromExpected.signum() < 0 ? GoalPaceStatus.BEHIND : GoalPaceStatus.AHEAD;
    }

    private String summaryFor(GoalPaceStatus paceStatus) {
        return switch (paceStatus) {
            case BEHIND -> BEHIND_SUMMARY;
            case ON_PACE -> ON_PACE_SUMMARY;
            case AHEAD -> AHEAD_SUMMARY;
        };
    }

    private String detailFor(GoalPaceStatus paceStatus) {
        return switch (paceStatus) {
            case BEHIND -> BEHIND_DETAIL;
            case ON_PACE -> ON_PACE_DETAIL;
            case AHEAD -> AHEAD_DETAIL;
        };
    }

    private BigDecimal requirePositive(BigDecimal value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");
        if (value.signum() <= 0) {
            throw new IllegalArgumentException(fieldName + " must be positive");
        }
        return value;
    }
}
