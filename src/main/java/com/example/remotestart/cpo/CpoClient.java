package com.example.remotestart.cpo;

import java.util.UUID;

/** Sends OCPI commands to the CPO (through the OCPI adapter). */
public interface CpoClient {

    /**
     * Asks the CPO to start a session. {@code commandId} comes back on the asynchronous CommandResult.
     *
     * @throws org.springframework.web.client.RestClientException if the CPO cannot be reached or answers with an
     *                                                             error status
     */
    CommandResponse startSession(UUID commandId, String evseUid, String contractId);
}
