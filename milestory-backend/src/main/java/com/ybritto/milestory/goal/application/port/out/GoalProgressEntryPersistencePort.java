package com.ybritto.milestory.goal.application.port.out;

import com.ybritto.milestory.goal.domain.GoalProgressEntry;
import java.util.List;
import java.util.UUID;

public interface GoalProgressEntryPersistencePort {

    GoalProgressEntry save(GoalProgressEntry entry);

    List<GoalProgressEntry> findByGoalId(UUID goalId);
}
