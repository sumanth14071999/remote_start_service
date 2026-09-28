package com.example.remotestart.events;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import com.example.remotestart.remotestart.RemoteStart;
import com.example.remotestart.remotestart.RemoteStartRepository;
import com.example.remotestart.remotestart.RemoteStartStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommandResultHandlerTest {

    private static final Instant T0 = Instant.parse("2026-01-10T10:00:00Z");

    @Mock
    private RemoteStartRepository remoteStarts;

    private CommandResultHandler handler;

    @BeforeEach
    void setUp() {
        handler = new CommandResultHandler(remoteStarts, Clock.fixed(T0.plusSeconds(10), ZoneOffset.UTC));
    }

    @Test
    void appliesResultToItsRemoteStart() {
        RemoteStart remoteStart = RemoteStart.request("DE-DCS-C12345678-X", "DE*ABC*E1001*1", T0);
        remoteStart.markSent(T0.plusSeconds(1));
        when(remoteStarts.findById(remoteStart.getId())).thenReturn(Optional.of(remoteStart));

        handler.handle(new CommandResultEvent(remoteStart.getId(), CommandResultType.ACCEPTED, null));

        assertThat(remoteStart.getStatus()).isEqualTo(RemoteStartStatus.STARTED);
    }

    @Test
    void resultForUnknownCommandIsRejected() {
        UUID unknown = UUID.randomUUID();
        when(remoteStarts.findById(unknown)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(new CommandResultEvent(unknown, CommandResultType.ACCEPTED, null)))
                .isInstanceOf(UnknownCommandException.class);
    }
}
