package com.ybritto.milestory.infrastructure.api;

import com.ybritto.milestory.application.status.GetFoundationStatusUseCase;
import com.ybritto.milestory.domain.status.FoundationStatus;
import com.ybritto.milestory.domain.status.FoundationStatusMode;
import com.ybritto.milestory.generated.api.FoundationStatusApi;
import com.ybritto.milestory.generated.model.FoundationStatusResponse;
import com.ybritto.milestory.generated.model.FoundationStatusResponseDatabase;
import com.ybritto.milestory.generated.model.FoundationStatusResponseMigration;
import com.ybritto.milestory.generated.model.FoundationStatusResponseMode;

import java.time.ZoneOffset;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FoundationStatusController implements FoundationStatusApi {

    private final GetFoundationStatusUseCase getFoundationStatusUseCase;

    public FoundationStatusController(GetFoundationStatusUseCase getFoundationStatusUseCase) {
        this.getFoundationStatusUseCase = getFoundationStatusUseCase;
    }

    @Override
    public ResponseEntity<FoundationStatusResponse> getFoundationStatus() {
        FoundationStatus status = getFoundationStatusUseCase.getStatus();
        return ResponseEntity.ok(
                new FoundationStatusResponse(
                        status.headline(),
                        status.summary(),
                        mapMode(status.mode()),
                        status.apiVersion(),
                        new FoundationStatusResponseDatabase(
                                status.database().status(),
                                status.database().name()
                        ),
                        new FoundationStatusResponseMigration(
                                status.migration().status(),
                                status.migration().baseline()
                        ),
                        status.generatedAt().atOffset(ZoneOffset.UTC),
                        status.notes().toArray(String[]::new)
                )
        );
    }

    private FoundationStatusResponseMode mapMode(FoundationStatusMode mode) {
        return switch (mode) {
            case READY -> FoundationStatusResponseMode.READY;
            case EMPTY -> FoundationStatusResponseMode.EMPTY;
            case DEGRADED -> FoundationStatusResponseMode.DEGRADED;
        };
    }
}
