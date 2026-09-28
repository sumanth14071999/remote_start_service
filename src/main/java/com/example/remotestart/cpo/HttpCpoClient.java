package com.example.remotestart.cpo;

import java.time.Duration;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Profile("!local")
public class HttpCpoClient implements CpoClient {

    private final RestClient restClient;

    public HttpCpoClient(RestClient.Builder builder,
                         @Value("${app.cpo.base-url}") String baseUrl,
                         @Value("${app.cpo.connect-timeout}") Duration connectTimeout,
                         @Value("${app.cpo.read-timeout}") Duration readTimeout) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout((int) connectTimeout.toMillis());
        requestFactory.setReadTimeout((int) readTimeout.toMillis());
        this.restClient = builder.baseUrl(baseUrl).requestFactory(requestFactory).build();
    }

    @Override
    public CommandResponse startSession(UUID commandId, String evseUid, String contractId) {
        return restClient.post()
                .uri("/ocpi/2.2.1/commands/START_SESSION")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new StartSessionCommand(commandId, evseUid, contractId))
                .retrieve()
                .body(CommandResponse.class);
    }
}
