package com.example.remotestart.remotestart;

import java.time.Clock;
import java.time.Instant;

import com.example.remotestart.cpo.CommandResponse;
import com.example.remotestart.cpo.CommandResponseType;
import com.example.remotestart.cpo.CpoClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RemoteStartService {

    private static final Logger log = LoggerFactory.getLogger(RemoteStartService.class);

    private final RemoteStartRepository remoteStarts;
    private final CpoClient cpoClient;
    private final Clock clock;

    public RemoteStartService(RemoteStartRepository remoteStarts, CpoClient cpoClient, Clock clock) {
        this.remoteStarts = remoteStarts;
        this.cpoClient = cpoClient;
        this.clock = clock;
    }

    @Transactional
    public RemoteStart requestStart(String contractId, String evseUid) {
        RemoteStart remoteStart = remoteStarts.save(RemoteStart.request(contractId, evseUid, Instant.now(clock)));

        CommandResponse response = cpoClient.startSession(remoteStart.getId(), evseUid, contractId);
        if (response.result() == CommandResponseType.ACCEPTED) {
            remoteStart.markSent(Instant.now(clock));
        } else {
            remoteStart.markFailed("CPO_" + response.result().name(), Instant.now(clock));
        }

        log.info("Remote start {} on {} for {} is {}", remoteStart.getId(), evseUid, contractId,
                remoteStart.getStatus());
        return remoteStart;
    }
}
