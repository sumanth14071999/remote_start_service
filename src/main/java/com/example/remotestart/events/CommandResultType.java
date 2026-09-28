package com.example.remotestart.events;

/** OCPI 2.2.1 CommandResultType: the outcome of a command, reported by the CPO after the charger answered. */
public enum CommandResultType {
    ACCEPTED,
    CANCELED_RESERVATION,
    EVSE_OCCUPIED,
    EVSE_INOPERATIVE,
    FAILED,
    NOT_SUPPORTED,
    REJECTED,
    TIMEOUT,
    UNKNOWN_RESERVATION
}
