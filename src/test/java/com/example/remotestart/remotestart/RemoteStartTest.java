package com.example.remotestart.remotestart;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import com.example.remotestart.events.CommandResultType;
import org.junit.jupiter.api.Test;

class RemoteStartTest {

    private static final Instant T0 = Instant.parse("2026-01-10T10:00:00Z");

    @Test
    void newRequestIsPending() {
        RemoteStart remoteStart = newRemoteStart();

        assertThat(remoteStart.getId()).isNotNull();
        assertThat(remoteStart.getStatus()).isEqualTo(RemoteStartStatus.PENDING);
    }

    @Test
    void acceptedResultMeansTheSessionStarted() {
        RemoteStart remoteStart = sentRemoteStart();

        remoteStart.applyCommandResult(CommandResultType.ACCEPTED, T0.plusSeconds(5));

        assertThat(remoteStart.getStatus()).isEqualTo(RemoteStartStatus.STARTED);
        assertThat(remoteStart.getUpdatedAt()).isEqualTo(T0.plusSeconds(5));
    }

    @Test
    void rejectedResultFails() {
        RemoteStart remoteStart = sentRemoteStart();

        remoteStart.applyCommandResult(CommandResultType.REJECTED, T0.plusSeconds(5));

        assertThat(remoteStart.getStatus()).isEqualTo(RemoteStartStatus.FAILED);
        assertThat(remoteStart.getFailureReason()).isEqualTo("REJECTED");
    }

    @Test
    void occupiedEvseFails() {
        RemoteStart remoteStart = sentRemoteStart();

        remoteStart.applyCommandResult(CommandResultType.EVSE_OCCUPIED, T0.plusSeconds(5));

        assertThat(remoteStart.getStatus()).isEqualTo(RemoteStartStatus.FAILED);
        assertThat(remoteStart.getFailureReason()).isEqualTo("EVSE_OCCUPIED");
    }

    private static RemoteStart newRemoteStart() {
        return RemoteStart.request("DE-DCS-C12345678-X", "DE*ABC*E1001*1", T0);
    }

    private static RemoteStart sentRemoteStart() {
        RemoteStart remoteStart = newRemoteStart();
        remoteStart.markSent(T0.plusSeconds(1));
        return remoteStart;
    }
}
