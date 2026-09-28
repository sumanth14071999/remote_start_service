package com.example.remotestart.remotestart;

import java.time.Instant;
import java.util.UUID;

import com.example.remotestart.events.CommandResultType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * A driver's request to start charging from the app. Its id is also the OCPI command id sent to the CPO, so the
 * asynchronous CommandResult can be matched back to it.
 */
@Entity
@Table(name = "remote_start")
public class RemoteStart {

    @Id
    private UUID id;

    @Column(name = "contract_id", nullable = false)
    private String contractId;

    @Column(name = "evse_uid", nullable = false)
    private String evseUid;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RemoteStartStatus status;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected RemoteStart() {
        // for JPA
    }

    public static RemoteStart request(String contractId, String evseUid, Instant now) {
        RemoteStart remoteStart = new RemoteStart();
        remoteStart.id = UUID.randomUUID();
        remoteStart.contractId = contractId;
        remoteStart.evseUid = evseUid;
        remoteStart.status = RemoteStartStatus.PENDING;
        remoteStart.createdAt = now;
        remoteStart.updatedAt = now;
        return remoteStart;
    }

    public void markSent(Instant now) {
        this.status = RemoteStartStatus.SENT;
        this.updatedAt = now;
    }

    public void markFailed(String reason, Instant now) {
        this.status = RemoteStartStatus.FAILED;
        this.failureReason = reason;
        this.updatedAt = now;
    }

    public void applyCommandResult(CommandResultType result, Instant now) {
        if (result == CommandResultType.REJECTED) {
            this.status = RemoteStartStatus.FAILED;
            this.failureReason = result.name();
        } else {
            this.status = RemoteStartStatus.STARTED;
        }
        this.updatedAt = now;
    }

    public UUID getId() {
        return id;
    }

    public String getContractId() {
        return contractId;
    }

    public String getEvseUid() {
        return evseUid;
    }

    public RemoteStartStatus getStatus() {
        return status;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
