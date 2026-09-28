package com.example.remotestart.events;

import java.util.UUID;

public class UnknownCommandException extends RuntimeException {

    public UnknownCommandException(UUID commandId) {
        super("CommandResult for unknown command " + commandId);
    }
}
