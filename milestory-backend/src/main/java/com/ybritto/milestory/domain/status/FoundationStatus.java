package com.ybritto.milestory.domain.status;

import java.time.Instant;
import java.util.List;

public record FoundationStatus(
        String headline,
        String summary,
        FoundationStatusMode mode,
        String apiVersion,
        DatabaseStatus database,
        MigrationStatus migration,
        Instant generatedAt,
        List<String> notes
) {
    public FoundationStatus {
        notes = List.copyOf(notes);
    }

    public record DatabaseStatus(String status, String name) {
    }

    public record MigrationStatus(String status, String baseline) {
    }
}
