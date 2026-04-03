package com.ybritto.milestory.infrastructure.status;

import com.ybritto.milestory.application.status.FoundationRuntimeStatus;
import com.ybritto.milestory.application.status.FoundationRuntimeStatusProvider;
import javax.sql.DataSource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class SystemFoundationStatusService implements FoundationRuntimeStatusProvider {

    static final String BASELINE_ID = "001-foundation-baseline";

    private final Environment environment;
    private final JdbcTemplate jdbcTemplate;
    private final ResourceLoader resourceLoader;
    private final String applicationName;

    public SystemFoundationStatusService(
            Environment environment,
            DataSource dataSource,
            ResourceLoader resourceLoader
    ) {
        this.environment = environment;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.resourceLoader = resourceLoader;
        this.applicationName = environment.getProperty("spring.application.name", "Milestory");
    }

    @Override
    public FoundationRuntimeStatus getCurrentStatus() {
        String databaseStatus = determineDatabaseStatus();
        String migrationStatus = determineMigrationStatus(databaseStatus);

        return new FoundationRuntimeStatus(
                applicationName,
                determineActiveProfile(),
                determineDatabaseName(),
                databaseStatus,
                migrationStatus,
                BASELINE_ID
        );
    }

    private String determineActiveProfile() {
        String[] activeProfiles = environment.getActiveProfiles();
        if (activeProfiles.length == 0) {
            return "default";
        }
        return activeProfiles[0];
    }

    private String determineDatabaseName() {
        try {
            String currentDatabase = jdbcTemplate.queryForObject("select current_database()", String.class);
            if (currentDatabase != null && !currentDatabase.isBlank()) {
                return currentDatabase;
            }
        } catch (RuntimeException ex) {
            // Fall through to configuration parsing when the database is not reachable yet.
        }

        String url = environment.getProperty("spring.datasource.url");
        if (url == null || url.isBlank() || !url.contains("/")) {
            return "unknown";
        }
        String withoutParams = url.substring(url.lastIndexOf('/') + 1);
        int querySeparator = withoutParams.indexOf('?');
        if (querySeparator >= 0) {
            return withoutParams.substring(0, querySeparator);
        }
        return withoutParams;
    }

    private String determineDatabaseStatus() {
        try {
            Integer validation = jdbcTemplate.queryForObject("select 1", Integer.class);
            return validation != null && validation == 1 ? "connected" : "unavailable";
        } catch (RuntimeException ex) {
            return "unavailable";
        }
    }

    private String determineMigrationStatus(String databaseStatus) {
        if (!"connected".equals(databaseStatus)) {
            return "unavailable";
        }
        if (!resourceLoader.getResource("classpath:/db/changelog/changes/" + BASELINE_ID + ".yaml").exists()) {
            return "failed";
        }

        try {
            Integer appliedRows = jdbcTemplate.queryForObject(
                    "select count(*) from databasechangelog where id = ?",
                    Integer.class,
                    BASELINE_ID
            );
            return appliedRows != null && appliedRows > 0 ? "applied" : "baseline-pending";
        } catch (RuntimeException ex) {
            return "baseline-pending";
        }
    }
}
