package com.example.remotestart.remotestart;

/** See README "Remote start lifecycle". */
public enum RemoteStartStatus {
    /** Created, command not yet accepted by the CPO. */
    PENDING,
    /** The CPO accepted the command and forwarded it to the charger; waiting for the CommandResult. */
    SENT,
    /** The charger started the session. */
    STARTED,
    /** The command was rejected or failed; see failureReason. */
    FAILED,
    /** No CommandResult arrived in time. */
    TIMED_OUT
}
