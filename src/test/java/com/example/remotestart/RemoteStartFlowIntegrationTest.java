package com.example.remotestart;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.util.UUID;

import com.example.remotestart.cpo.CommandResponse;
import com.example.remotestart.cpo.CommandResponseType;
import com.example.remotestart.cpo.CpoClient;
import com.example.remotestart.remotestart.RemoteStart;
import com.example.remotestart.remotestart.RemoteStartRepository;
import com.example.remotestart.remotestart.RemoteStartStatus;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/** End-to-end: POST -> (mocked) CPO -> CommandResult on Kafka -> final status. Requires Docker. */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class RemoteStartFlowIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    @ServiceConnection
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.1"));

    @MockBean
    private CpoClient cpoClient;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private RemoteStartRepository remoteStarts;

    @BeforeEach
    void cpoAcceptsCommands() {
        when(cpoClient.startSession(any(), any(), any()))
                .thenReturn(new CommandResponse(CommandResponseType.ACCEPTED, 30));
    }

    @Test
    void chargerStartsTheSession() throws Exception {
        UUID id = requestStart();

        sendResult(id, "ACCEPTED");

        await().atMost(Duration.ofSeconds(30)).untilAsserted(() ->
                assertThat(find(id).getStatus()).isEqualTo(RemoteStartStatus.STARTED));
    }

    @Test
    void occupiedChargerFailsTheRemoteStart() throws Exception {
        UUID id = requestStart();

        sendResult(id, "EVSE_OCCUPIED");

        await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
            RemoteStart remoteStart = find(id);
            assertThat(remoteStart.getStatus()).isEqualTo(RemoteStartStatus.FAILED);
            assertThat(remoteStart.getFailureReason()).isEqualTo("EVSE_OCCUPIED");
        });
    }

    private UUID requestStart() throws Exception {
        String body = mockMvc.perform(post("/api/v1/remote-starts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"contractId":"DE-DCS-C12345678-X","evseUid":"DE*ABC*E1001*1"}"""))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return UUID.fromString(JsonPath.read(body, "$.id"));
    }

    private void sendResult(UUID commandId, String result) {
        kafkaTemplate.send("cpo.command-results", commandId.toString(),
                "{\"command_id\":\"" + commandId + "\",\"result\":\"" + result + "\"}").join();
    }

    private RemoteStart find(UUID id) {
        return remoteStarts.findById(id).orElseThrow();
    }
}
