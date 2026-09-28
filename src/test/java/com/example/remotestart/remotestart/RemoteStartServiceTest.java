package com.example.remotestart.remotestart;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import com.example.remotestart.cpo.CommandResponse;
import com.example.remotestart.cpo.CommandResponseType;
import com.example.remotestart.cpo.CpoClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.ResourceAccessException;

@ExtendWith(MockitoExtension.class)
class RemoteStartServiceTest {

    private static final String CONTRACT = "DE-DCS-C12345678-X";
    private static final String EVSE = "DE*ABC*E1001*1";

    @Mock
    private RemoteStartRepository remoteStarts;
    @Mock
    private CpoClient cpoClient;

    private RemoteStartService service;

    @BeforeEach
    void setUp() {
        service = new RemoteStartService(remoteStarts, cpoClient,
                Clock.fixed(Instant.parse("2026-01-10T10:00:00Z"), ZoneOffset.UTC));
        when(remoteStarts.save(any(RemoteStart.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void commandAcceptedByCpoIsSent() {
        when(cpoClient.startSession(any(), eq(EVSE), eq(CONTRACT)))
                .thenReturn(new CommandResponse(CommandResponseType.ACCEPTED, 30));

        RemoteStart remoteStart = service.requestStart(CONTRACT, EVSE);

        assertThat(remoteStart.getStatus()).isEqualTo(RemoteStartStatus.SENT);
    }

    @Test
    void commandRejectedByCpoFails() {
        when(cpoClient.startSession(any(), eq(EVSE), eq(CONTRACT)))
                .thenReturn(new CommandResponse(CommandResponseType.REJECTED, 0));

        RemoteStart remoteStart = service.requestStart(CONTRACT, EVSE);

        assertThat(remoteStart.getStatus()).isEqualTo(RemoteStartStatus.FAILED);
        assertThat(remoteStart.getFailureReason()).isEqualTo("CPO_REJECTED");
    }

    @Test
    void unreachableCpoIsRecordedAsFailed() {
        when(cpoClient.startSession(any(), eq(EVSE), eq(CONTRACT)))
                .thenThrow(new ResourceAccessException("Read timed out"));

        RemoteStart remoteStart = service.requestStart(CONTRACT, EVSE);

        assertThat(remoteStart.getStatus()).isEqualTo(RemoteStartStatus.FAILED);
        assertThat(remoteStart.getFailureReason()).isEqualTo("CPO_UNREACHABLE");
    }
}
