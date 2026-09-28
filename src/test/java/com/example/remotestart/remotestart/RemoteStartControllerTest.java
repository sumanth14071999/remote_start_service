package com.example.remotestart.remotestart;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RemoteStartController.class)
class RemoteStartControllerTest {

    private static final String BODY = """
            {"contractId":"DE-DCS-C12345678-X","evseUid":"DE*ABC*E1001*1"}""";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RemoteStartService service;

    @MockBean
    private RemoteStartRepository remoteStarts;

    @Test
    void startsCharging() throws Exception {
        RemoteStart remoteStart = sent();
        when(service.requestStart("DE-DCS-C12345678-X", "DE*ABC*E1001*1")).thenReturn(remoteStart);

        mockMvc.perform(post("/api/v1/remote-starts").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(remoteStart.getId().toString()))
                .andExpect(jsonPath("$.status").value("SENT"));
    }

    @Test
    void retryWithSameIdempotencyKeyDoesNotStartTwice() throws Exception {
        RemoteStart remoteStart = sent();
        when(service.requestStart("DE-DCS-C12345678-X", "DE*ABC*E1001*1")).thenReturn(remoteStart);
        when(remoteStarts.findById(remoteStart.getId())).thenReturn(Optional.of(remoteStart));
        String key = UUID.randomUUID().toString();

        mockMvc.perform(post("/api/v1/remote-starts").header("Idempotency-Key", key)
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/v1/remote-starts").header("Idempotency-Key", key)
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(remoteStart.getId().toString()));

        verify(service, times(1)).requestStart("DE-DCS-C12345678-X", "DE*ABC*E1001*1");
    }

    @Test
    void requestWithoutContractIdIsRejected() throws Exception {
        mockMvc.perform(post("/api/v1/remote-starts").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"evseUid":"DE*ABC*E1001*1"}"""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unknownRemoteStartReturns404() throws Exception {
        UUID unknown = UUID.randomUUID();
        when(remoteStarts.findById(unknown)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/remote-starts/{id}", unknown))
                .andExpect(status().isNotFound());
    }

    private static RemoteStart sent() {
        RemoteStart remoteStart = RemoteStart.request("DE-DCS-C12345678-X", "DE*ABC*E1001*1",
                Instant.parse("2026-01-10T10:00:00Z"));
        remoteStart.markSent(Instant.parse("2026-01-10T10:00:01Z"));
        return remoteStart;
    }
}
