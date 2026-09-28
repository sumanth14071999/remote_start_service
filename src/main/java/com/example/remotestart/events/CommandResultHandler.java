package com.example.remotestart.events;

import java.time.Clock;
import java.time.Instant;

import com.example.remotestart.remotestart.RemoteStart;
import com.example.remotestart.remotestart.RemoteStartRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommandResultHandler {

    private static final Logger log = LoggerFactory.getLogger(CommandResultHandler.class);

    private final RemoteStartRepository remoteStarts;
    private final Clock clock;

    public CommandResultHandler(RemoteStartRepository remoteStarts, Clock clock) {
        this.remoteStarts = remoteStarts;
        this.clock = clock;
    }

    @Transactional
    public void handle(CommandResultEvent event) {
        RemoteStart remoteStart = remoteStarts.findById(event.commandId())
                .orElseThrow(() -> new UnknownCommandException(event.commandId()));
        remoteStart.applyCommandResult(event.result(), Instant.now(clock));
        log.info("Remote start {}: CPO reported {} -> {}", remoteStart.getId(), event.result(),
                remoteStart.getStatus());
    }
}
