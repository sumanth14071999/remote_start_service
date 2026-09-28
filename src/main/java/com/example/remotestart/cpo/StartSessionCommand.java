package com.example.remotestart.cpo;

import java.util.UUID;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

/** Request body for the OCPI adapter's START_SESSION endpoint (OCPI naming). */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
record StartSessionCommand(UUID commandId, String evseUid, String contractId) {
}
