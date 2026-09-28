package com.example.remotestart.remotestart;

import java.net.URI;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** Used by the driver app: start charging, then poll the status. */
@RestController
@RequestMapping("/api/v1/remote-starts")
public class RemoteStartController {

    /** Idempotency-Key -> remote start id, so app retries don't start a second session. */
    private final Map<String, UUID> idempotencyKeys = new ConcurrentHashMap<>();

    private final RemoteStartService service;
    private final RemoteStartRepository remoteStarts;

    public RemoteStartController(RemoteStartService service, RemoteStartRepository remoteStarts) {
        this.service = service;
        this.remoteStarts = remoteStarts;
    }

    @PostMapping
    public ResponseEntity<RemoteStartResponse> start(
            @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody StartRemoteStartRequest request) {
        if (idempotencyKey != null) {
            UUID existing = idempotencyKeys.get(idempotencyKey);
            if (existing != null) {
                return ResponseEntity.ok(RemoteStartResponse.from(find(existing)));
            }
        }

        RemoteStart remoteStart = service.requestStart(request.contractId(), request.evseUid());
        if (idempotencyKey != null) {
            idempotencyKeys.put(idempotencyKey, remoteStart.getId());
        }
        return ResponseEntity.created(URI.create("/api/v1/remote-starts/" + remoteStart.getId()))
                .body(RemoteStartResponse.from(remoteStart));
    }

    @GetMapping("/{id}")
    public RemoteStartResponse get(@PathVariable UUID id) {
        return RemoteStartResponse.from(find(id));
    }

    private RemoteStart find(UUID id) {
        return remoteStarts.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No remote start " + id));
    }
}
