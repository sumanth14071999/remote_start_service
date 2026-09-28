package com.example.remotestart.cpo;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** Local development only: accepts every command. Send the CommandResult yourself with scripts/send-result.sh. */
@Component
@Profile("local")
public class StubCpoClient implements CpoClient {

    private static final Logger log = LoggerFactory.getLogger(StubCpoClient.class);

    @Override
    public CommandResponse startSession(UUID commandId, String evseUid, String contractId) {
        log.info("[stub CPO] START_SESSION {} on {} for {} -> ACCEPTED", commandId, evseUid, contractId);
        return new CommandResponse(CommandResponseType.ACCEPTED, 30);
    }
}
