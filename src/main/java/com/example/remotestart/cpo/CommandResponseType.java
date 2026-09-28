package com.example.remotestart.cpo;

/** OCPI 2.2.1 CommandResponseType: the CPO's synchronous answer to a command request. */
public enum CommandResponseType {
    ACCEPTED,
    NOT_SUPPORTED,
    REJECTED,
    UNKNOWN_SESSION
}
