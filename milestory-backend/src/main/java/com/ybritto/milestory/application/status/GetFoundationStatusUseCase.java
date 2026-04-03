package com.ybritto.milestory.application.status;

import com.ybritto.milestory.domain.status.FoundationStatus;
import com.ybritto.milestory.domain.status.FoundationStatusMode;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class GetFoundationStatusUseCase {

    private final FoundationRuntimeStatusProvider runtimeStatusProvider;
    private final Clock clock;

    public GetFoundationStatusUseCase(FoundationRuntimeStatusProvider runtimeStatusProvider) {
        this(runtimeStatusProvider, Clock.systemUTC());
    }

    public GetFoundationStatusUseCase(FoundationRuntimeStatusProvider runtimeStatusProvider, Clock clock) {
        this.runtimeStatusProvider = runtimeStatusProvider;
        this.clock = clock;
    }

    public FoundationStatus getStatus() {
        FoundationRuntimeStatus runtimeStatus = runtimeStatusProvider.getCurrentStatus();
        FoundationStatusMode mode = determineMode(runtimeStatus);
        String headline = switch (mode) {
            case READY -> runtimeStatus.applicationName() + " foundation is ready";
            case EMPTY -> runtimeStatus.applicationName() + " foundation is waiting for baseline data";
            case DEGRADED -> runtimeStatus.applicationName() + " foundation is degraded";
        };

        List<String> notes = new ArrayList<>();
        notes.add("Active profile: " + valueOrFallback(runtimeStatus.activeProfile(), "default"));
        notes.add("Authentication is intentionally deferred in Phase 1.");
        if (mode == FoundationStatusMode.EMPTY) {
            notes.add("Liquibase baseline " + runtimeStatus.baseline() + " is pending.");
        }
        if (mode == FoundationStatusMode.DEGRADED) {
            notes.add("Database connectivity or migration health needs attention.");
        }

        return new FoundationStatus(
                headline,
                buildSummary(runtimeStatus, mode),
                mode,
                "v1",
                new FoundationStatus.DatabaseStatus(
                        valueOrFallback(runtimeStatus.databaseStatus(), "unknown"),
                        valueOrFallback(runtimeStatus.databaseName(), "unconfigured")
                ),
                new FoundationStatus.MigrationStatus(
                        valueOrFallback(runtimeStatus.migrationStatus(), "unknown"),
                        valueOrFallback(runtimeStatus.baseline(), "001-foundation-baseline")
                ),
                Instant.now(clock),
                notes
        );
    }

    private FoundationStatusMode determineMode(FoundationRuntimeStatus runtimeStatus) {
        if (!"connected".equalsIgnoreCase(runtimeStatus.databaseStatus())
                || "failed".equalsIgnoreCase(runtimeStatus.migrationStatus())
                || "unavailable".equalsIgnoreCase(runtimeStatus.migrationStatus())) {
            return FoundationStatusMode.DEGRADED;
        }
        if ("baseline-pending".equalsIgnoreCase(runtimeStatus.migrationStatus())
                || isBlank(runtimeStatus.databaseName())) {
            return FoundationStatusMode.EMPTY;
        }
        return FoundationStatusMode.READY;
    }

    private String buildSummary(FoundationRuntimeStatus runtimeStatus, FoundationStatusMode mode) {
        return switch (mode) {
            case READY -> "The auth-free Phase 1 stack is running with Liquibase baseline "
                    + valueOrFallback(runtimeStatus.baseline(), "001-foundation-baseline") + ".";
            case EMPTY -> "The foundation is bootable, but baseline data is not fully applied yet.";
            case DEGRADED -> "The foundation endpoint is available, but runtime dependencies are not fully healthy.";
        };
    }

    private String valueOrFallback(String value, String fallback) {
        return isBlank(value) ? fallback : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
