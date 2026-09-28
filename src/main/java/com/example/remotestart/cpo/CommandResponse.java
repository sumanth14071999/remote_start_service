package com.example.remotestart.cpo;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Synchronous OCPI CommandResponse. {@code ACCEPTED} only means the CPO will forward the command to the charger;
 * the actual outcome arrives later as a CommandResult (see README).
 *
 * @param timeoutSeconds how long the CPO will wait for the charger before it reports a result
 */
public record CommandResponse(
        CommandResponseType result,
        @JsonProperty("timeout") int timeoutSeconds) {
}
