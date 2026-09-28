package com.example.remotestart.remotestart;

import jakarta.validation.constraints.NotBlank;

public record StartRemoteStartRequest(
        @NotBlank String contractId,
        @NotBlank String evseUid) {
}
