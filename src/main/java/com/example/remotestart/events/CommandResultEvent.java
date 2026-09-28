package com.example.remotestart.events;

import java.util.UUID;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

/**
 * Asynchronous OCPI CommandResult, published by the OCPI adapter to {@code cpo.command-results}.
 * {@code commandId} is the id we sent with the command (= remote start id).
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record CommandResultEvent(UUID commandId, CommandResultType result, String message) {
}
