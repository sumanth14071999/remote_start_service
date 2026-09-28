package com.example.remotestart.remotestart;

import java.time.Instant;
import java.util.UUID;

public record RemoteStartResponse(
        UUID id,
        String contractId,
        String evseUid,
        RemoteStartStatus status,
        String failureReason,
        Instant createdAt,
        Instant updatedAt) {

    public static RemoteStartResponse from(RemoteStart r) {
        return new RemoteStartResponse(r.getId(), r.getContractId(), r.getEvseUid(), r.getStatus(),
                r.getFailureReason(), r.getCreatedAt(), r.getUpdatedAt());
    }
}
