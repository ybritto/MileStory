package com.ybritto.milestory.application.status;

public record FoundationRuntimeStatus(
        String applicationName,
        String activeProfile,
        String databaseName,
        String databaseStatus,
        String migrationStatus,
        String baseline
) {
}
