package com.ybritto.milestory.goal.application.usecase;

import com.ybritto.milestory.goal.application.model.RecordProgressEntryCommand;
import com.ybritto.milestory.goal.application.port.out.GoalPersistencePort;
import com.ybritto.milestory.goal.application.port.out.GoalProgressEntryPersistencePort;
import com.ybritto.milestory.goal.domain.Goal;
import com.ybritto.milestory.goal.domain.GoalProgressEntry;
import com.ybritto.milestory.goal.domain.GoalProgressEntryType;
import com.ybritto.milestory.goal.domain.GoalStatus;
import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;

public class RecordProgressEntryUseCase {

    private static final Comparator<GoalProgressEntry> LATEST_ENTRY_ORDER =
            Comparator.comparing(GoalProgressEntry::entryDate)
                    .thenComparing(GoalProgressEntry::recordedAt);

    private final GoalPersistencePort goalPersistencePort;
    private final GoalProgressEntryPersistencePort goalProgressEntryPersistencePort;
    private final Clock clock;

    public RecordProgressEntryUseCase(
            GoalPersistencePort goalPersistencePort,
            GoalProgressEntryPersistencePort goalProgressEntryPersistencePort,
            Clock clock
    ) {
        this.goalPersistencePort = Objects.requireNonNull(goalPersistencePort, "goalPersistencePort must not be null");
        this.goalProgressEntryPersistencePort = Objects.requireNonNull(
                goalProgressEntryPersistencePort,
                "goalProgressEntryPersistencePort must not be null"
        );
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    public GoalProgressEntry record(UUID goalId, RecordProgressEntryCommand command) {
        UUID id = Objects.requireNonNull(goalId, "goalId must not be null");
        RecordProgressEntryCommand recordCommand = Objects.requireNonNull(command, "command must not be null");

        Goal goal = goalPersistencePort.findById(id).orElseThrow(() -> new GoalNotFoundException(id));
        if (goal.status() != GoalStatus.ACTIVE) {
            throw new IllegalStateException("Cannot record progress on an archived goal");
        }

        GoalProgressEntryType entryType = goalProgressEntryPersistencePort.findByGoalId(id).stream()
                .max(LATEST_ENTRY_ORDER)
                .map(latestEntry -> recordCommand.progressValue().compareTo(latestEntry.progressValue()) < 0
                        ? GoalProgressEntryType.CORRECTION
                        : GoalProgressEntryType.NORMAL)
                .orElse(GoalProgressEntryType.NORMAL);

        GoalProgressEntry entry = GoalProgressEntry.record(
                UUID.randomUUID(),
                id,
                recordCommand.entryDate(),
                recordCommand.progressValue(),
                recordCommand.note(),
                entryType,
                Instant.now(clock)
        );
        return goalProgressEntryPersistencePort.save(entry);
    }
}
