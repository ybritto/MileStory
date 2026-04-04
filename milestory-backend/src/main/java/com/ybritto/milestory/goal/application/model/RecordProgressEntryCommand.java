package com.ybritto.milestory.goal.application.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public record RecordProgressEntryCommand(
        LocalDate entryDate,
        BigDecimal progressValue,
        String note
) {

    public RecordProgressEntryCommand {
        Objects.requireNonNull(entryDate, "entryDate must not be null");
        Objects.requireNonNull(progressValue, "progressValue must not be null");
        if (progressValue.signum() < 0) {
            throw new IllegalArgumentException("progressValue must not be negative");
        }
        note = note == null ? "" : note.trim();
    }
}
