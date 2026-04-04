package com.ybritto.milestory.goal.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GoalTest {

    private static final Instant CREATED_AT = Instant.parse("2026-01-01T00:00:00Z");

    @Test
    void rejectsCheckpointPlansThatAreOutOfOrder() {
        assertThrows(IllegalArgumentException.class, () -> Goal.create(
                UUID.randomUUID(),
                Year.of(2026),
                "Read 24 books",
                UUID.randomUUID(),
                BigDecimal.valueOf(24),
                "books",
                "Grow consistency",
                "Keep fiction and nonfiction balanced",
                "GENERIC_FALLBACK",
                false,
                CREATED_AT,
                List.of(
                        checkpoint(1, LocalDate.of(2026, 2, 28), 4),
                        checkpoint(2, LocalDate.of(2026, 1, 31), 8),
                        checkpoint(3, LocalDate.of(2026, 12, 31), 24)
                )
        ));
    }

    @Test
    void archivesAndPreventsFurtherEdits() {
        Goal activeGoal = Goal.create(
                UUID.randomUUID(),
                Year.of(2026),
                "Save 1200 euros",
                UUID.randomUUID(),
                BigDecimal.valueOf(1200),
                "eur",
                "Build an emergency buffer",
                "Monthly transfers",
                "GENERIC_FALLBACK",
                false,
                CREATED_AT,
                List.of(
                        checkpoint(1, LocalDate.of(2026, 1, 31), 100),
                        checkpoint(2, LocalDate.of(2026, 6, 30), 600),
                        checkpoint(3, LocalDate.of(2026, 12, 31), 1200)
                )
        );

        Goal archivedGoal = activeGoal.archive(Instant.parse("2026-08-01T00:00:00Z"));

        assertEquals(GoalStatus.ARCHIVED, archivedGoal.status());
        assertThrows(IllegalStateException.class, () -> archivedGoal.update(
                "Save 1500 euros",
                archivedGoal.categoryId(),
                BigDecimal.valueOf(1500),
                "eur",
                archivedGoal.motivation(),
                archivedGoal.notes(),
                archivedGoal.suggestionBasis(),
                true,
                Instant.parse("2026-08-02T00:00:00Z"),
                archivedGoal.checkpoints()
        ));
    }

    private GoalCheckpoint checkpoint(int sequenceNumber, LocalDate checkpointDate, int targetValue) {
        return GoalCheckpoint.of(
                UUID.randomUUID(),
                sequenceNumber,
                checkpointDate,
                BigDecimal.valueOf(targetValue),
                "",
                "SUGGESTED"
        );
    }
}
