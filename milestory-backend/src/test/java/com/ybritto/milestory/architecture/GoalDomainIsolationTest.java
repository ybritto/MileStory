package com.ybritto.milestory.architecture;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class GoalDomainIsolationTest {

    private static final Path GOAL_DOMAIN_PATH = Path.of(
            "src/main/java/com/ybritto/milestory/goal/domain"
    );

    @Test
    void goalDomainDoesNotDependOnFrameworkOrGeneratedTypes() throws IOException {
        try (Stream<Path> files = Files.walk(GOAL_DOMAIN_PATH)) {
            String source = files
                    .filter(path -> path.toString().endsWith(".java"))
                    .map(this::readString)
                    .reduce("", String::concat);

            assertFalse(source.contains("org.springframework"));
            assertFalse(source.contains("jakarta.persistence"));
            assertFalse(source.contains("com.ybritto.milestory.generated"));
        }
    }

    private String readString(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read " + path, ex);
        }
    }
}
